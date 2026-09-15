package com.example.gushingbackend.service.impl;

import com.example.gushingbackend.config.WorkflowProperties;
import com.example.gushingbackend.model.bo.ImageGenerationBO;
import com.example.gushingbackend.model.bo.TextGenerationBO;
import com.example.gushingbackend.model.bo.VideoGenerationBO;
import com.example.gushingbackend.model.bo.WorkflowSegmentBO;
import com.example.gushingbackend.model.bo.WorkflowTextToVideoBO;
import com.example.gushingbackend.model.dto.TextDescriberOutputDTO;
import com.example.gushingbackend.service.AiService;
import com.example.gushingbackend.service.VideoConcatService;
import com.example.gushingbackend.service.WorkflowService;
import com.example.gushingbackend.store.WorkflowStateStore;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

/**
 * WorkflowService 实现：编排文生文→文生图→多段图生视频→拼接的工作流。
 * <p>
 * 核心设计：
 * <ol>
 *   <li>文生文只跑一次，输出共享图片描述 + N 段视频描述（LLM 自动分镜）</li>
 *   <li>文生图只跑一次，生成一张共享参考图（保证多片段风格一致）</li>
 *   <li>图生视频循环提交 N 个任务，每个任务用共享参考图 + 对应片段视频描述</li>
 *   <li>轮询所有片段直至全部成功</li>
 *   <li>FFmpeg 按顺序拼接所有片段为完整视频</li>
 * </ol>
 * 工作流为后端驱动的异步任务：提交后立即返回 workflowId，
 * 前端通过 {@link #queryWorkflow(String)} 轮询整体状态。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowServiceImpl implements WorkflowService {

    /** 文生文节点的 skill 提示词文件路径（classpath） */
    private static final String SKILL_PATH = "classpath:skills/text-to-video-describer.md";

    /** 工作流状态：处理中 */
    private static final String STATUS_PROCESSING = "PROCESSING";
    /** 工作流状态：成功 */
    private static final String STATUS_SUCCESS = "SUCCESS";
    /** 工作流状态：失败 */
    private static final String STATUS_FAILED = "FAILED";

    /** MiniMax 视频任务成功状态 */
    private static final String VIDEO_STATUS_SUCCESS = "succeeded";
    /** MiniMax 视频任务失败状态 */
    private static final String VIDEO_STATUS_FAILED = "failed";

    private final AiService aiService;
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    private final WorkflowStateStore workflowStateStore;
    private final VideoConcatService videoConcatService;
    private final WorkflowProperties workflowProperties;
    private final Executor workflowExecutor;

    /** 加载后的 skill 提示词内容，作为文生文节点的 system prompt */
    private String describerSkill;

    @jakarta.annotation.PostConstruct
    public void init() throws IOException {
        Resource resource = resourceLoader.getResource(SKILL_PATH);
        this.describerSkill = resource.getContentAsString(StandardCharsets.UTF_8);
    }

    @Override
    public WorkflowTextToVideoBO textToVideo(WorkflowTextToVideoBO bo) {
        // 1. 生成工作流 ID 并计算片段数
        String workflowId = UUID.randomUUID().toString().replace("-", "");
        bo.setWorkflowId(workflowId);

        int segmentDuration = workflowProperties.getSegmentDurationSeconds();
        int segmentCount = (int) Math.ceil((double) bo.getDuration() / segmentDuration);
        bo.setSegmentCount(segmentCount);

        // 2. 初始化工作流状态
        bo.setWorkflowStatus(STATUS_PROCESSING);
        bo.setSegments(new ArrayList<>());
        workflowStateStore.save(bo);

        // 3. 异步执行工作流，立即返回
        CompletableFuture.runAsync(() -> executeWorkflow(bo), workflowExecutor);

        log.info("工作流已提交，workflowId={}, duration={}s, segmentCount={}",
                workflowId, bo.getDuration(), segmentCount);
        return bo;
    }

    @Override
    public WorkflowTextToVideoBO queryWorkflow(String workflowId) {
        WorkflowTextToVideoBO bo = workflowStateStore.get(workflowId);
        if (bo == null) {
            throw new RuntimeException("工作流不存在，workflowId=" + workflowId);
        }
        return bo;
    }

    /**
     * 工作流实际执行逻辑（在异步线程中运行）。
     */
    private void executeWorkflow(WorkflowTextToVideoBO bo) {
        try {
            // 阶段 1：文生文（生成共享图片描述 + 多段视频描述）
            runTextGeneration(bo);

            // 阶段 2：文生图（生成共享参考图）
            runImageGeneration(bo);

            // 阶段 3：循环提交所有视频生成任务
            submitAllVideoTasks(bo);

            // 阶段 4：轮询所有视频任务直至完成
            pollAllVideoTasks(bo);

            // 阶段 5：拼接所有片段
            runVideoConcatenation(bo);

            // 完成
            bo.setWorkflowStatus(STATUS_SUCCESS);
            workflowStateStore.save(bo);
            log.info("工作流完成，workflowId={}", bo.getWorkflowId());

        } catch (Exception e) {
            log.error("工作流执行失败，workflowId={}", bo.getWorkflowId(), e);
            bo.setWorkflowStatus(STATUS_FAILED);
            bo.setErrorMessage(e.getMessage());
            workflowStateStore.save(bo);
        }
    }

    /**
     * 阶段 1：文生文。
     * 将原始 prompt 与片段数信息合并后输入，输出共享图片描述 + N 段视频描述。
     */
    private void runTextGeneration(WorkflowTextToVideoBO bo) {
        TextGenerationBO textBO = new TextGenerationBO();
        textBO.setPrompt(buildUserPrompt(bo));
        textBO.setSystemPrompt(describerSkill);
        aiService.textGeneration(textBO);

        String textContent = textBO.getContent();
        bo.setTextContent(textContent);
        bo.setTextModel(textBO.getModel());

        if (!StringUtils.hasText(textContent)) {
            throw new RuntimeException("文生文节点未生成内容，工作流中断");
        }

        TextDescriberOutputDTO output = parseDescriberOutput(textContent);
        String imageDescription = output.getImageDescription();
        List<String> videoDescriptions = output.getVideoDescriptions();

        if (!StringUtils.hasText(imageDescription)) {
            throw new RuntimeException("文生文节点未输出图片描述，工作流中断");
        }
        if (videoDescriptions == null || videoDescriptions.isEmpty()) {
            throw new RuntimeException("文生文节点未输出视频描述列表，工作流中断");
        }

        // 校验片段数量：如果 LLM 输出的片段数与预期不符，取较小值并记录日志
        int expected = bo.getSegmentCount();
        if (videoDescriptions.size() != expected) {
            log.warn("文生文输出片段数({})与预期({})不符，将使用实际输出数量",
                    videoDescriptions.size(), expected);
            bo.setSegmentCount(videoDescriptions.size());
        }

        bo.setImageDescription(imageDescription);
        bo.setVideoDescriptions(videoDescriptions);
        workflowStateStore.save(bo);
        log.info("文生文完成，workflowId={}, 图片描述长度={}, 视频描述段数={}",
                bo.getWorkflowId(), imageDescription.length(), videoDescriptions.size());
    }

    /**
     * 阶段 2：文生图（生成所有片段共享的参考图）。
     */
    private void runImageGeneration(WorkflowTextToVideoBO bo) {
        ImageGenerationBO imageBO = new ImageGenerationBO();
        imageBO.setPrompt(bo.getImageDescription());
        aiService.imageGeneration(imageBO);

        String imageUrl = imageBO.getImageUrl();
        if (!StringUtils.hasText(imageUrl)) {
            throw new RuntimeException("文生图节点未生成图片，工作流中断");
        }

        bo.setImageUrl(imageUrl);
        bo.setImageModel(imageBO.getModel());
        workflowStateStore.save(bo);
        log.info("文生图完成，workflowId={}, imageUrl={}", bo.getWorkflowId(), imageUrl);
    }

    /**
     * 阶段 3：循环提交所有视频生成任务。
     * 所有片段共享同一张参考图，但使用各自的视频描述作为 prompt。
     */
    private void submitAllVideoTasks(WorkflowTextToVideoBO bo) {
        List<WorkflowSegmentBO> segments = new ArrayList<>();
        List<String> videoDescriptions = bo.getVideoDescriptions();

        for (int i = 0; i < videoDescriptions.size(); i++) {
            WorkflowSegmentBO segment = new WorkflowSegmentBO();
            segment.setIndex(i);
            segment.setVideoDescription(videoDescriptions.get(i));

            VideoGenerationBO videoBO = new VideoGenerationBO();
            videoBO.setPrompt(videoDescriptions.get(i));
            videoBO.setSubjectReference(java.util.Collections.singletonList(bo.getImageUrl()));
            videoBO.setDuration(workflowProperties.getSegmentDurationSeconds());

            aiService.submitVideoGeneration(videoBO);

            segment.setVideoTaskId(videoBO.getTaskId());
            segment.setVideoModel(videoBO.getModel());
            segment.setVideoStatus(videoBO.getStatus());
            segments.add(segment);

            log.info("提交视频片段 {}/{}，workflowId={}, taskId={}",
                    i + 1, videoDescriptions.size(), bo.getWorkflowId(), videoBO.getTaskId());
        }

        bo.setSegments(segments);
        workflowStateStore.save(bo);
    }

    /**
     * 阶段 4：轮询所有视频任务直至全部成功或任一失败。
     */
    private void pollAllVideoTasks(WorkflowTextToVideoBO bo) {
        List<WorkflowSegmentBO> segments = bo.getSegments();
        int total = segments.size();

        while (true) {
            int succeeded = 0;
            boolean anyFailed = false;

            for (WorkflowSegmentBO segment : segments) {
                if (VIDEO_STATUS_SUCCESS.equals(segment.getVideoStatus())) {
                    succeeded++;
                    continue;
                }
                if (VIDEO_STATUS_FAILED.equals(segment.getVideoStatus())) {
                    anyFailed = true;
                    continue;
                }

                // 查询该片段任务状态
                VideoGenerationBO queryBO = new VideoGenerationBO();
                queryBO.setTaskId(segment.getVideoTaskId());
                aiService.queryVideoTask(queryBO);

                segment.setVideoStatus(queryBO.getStatus());
                if (StringUtils.hasText(queryBO.getVideoUrl())) {
                    segment.setVideoUrl(queryBO.getVideoUrl());
                }

                if (VIDEO_STATUS_SUCCESS.equals(segment.getVideoStatus())) {
                    succeeded++;
                    log.info("视频片段 {}/{} 生成成功，workflowId={}, taskId={}",
                            segment.getIndex() + 1, total, bo.getWorkflowId(), segment.getVideoTaskId());
                } else if (VIDEO_STATUS_FAILED.equals(segment.getVideoStatus())) {
                    anyFailed = true;
                    log.error("视频片段 {}/{} 生成失败，workflowId={}, taskId={}",
                            segment.getIndex() + 1, total, bo.getWorkflowId(), segment.getVideoTaskId());
                }
            }

            workflowStateStore.save(bo);

            // 全部成功
            if (succeeded == total) {
                log.info("所有视频片段生成成功，workflowId={}", bo.getWorkflowId());
                return;
            }

            // 任一失败则中断
            if (anyFailed) {
                throw new RuntimeException("存在视频片段生成失败，workflowId=" + bo.getWorkflowId());
            }

            // 等待后继续轮询
            try {
                Thread.sleep(workflowProperties.getSegmentDurationSeconds() * 1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("轮询被中断，workflowId=" + bo.getWorkflowId(), e);
            }
        }
    }

    /**
     * 阶段 5：按顺序拼接所有视频片段。
     */
    private void runVideoConcatenation(WorkflowTextToVideoBO bo) {
        List<WorkflowSegmentBO> segments = bo.getSegments();
        List<String> segmentUrls = new ArrayList<>();
        for (WorkflowSegmentBO segment : segments) {
            if (!StringUtils.hasText(segment.getVideoUrl())) {
                throw new RuntimeException("片段 " + segment.getIndex() + " 缺少视频 URL，无法拼接");
            }
            segmentUrls.add(segment.getVideoUrl());
        }

        String outputFilename = "workflow_" + bo.getWorkflowId() + ".mp4";
        String finalVideoPath = videoConcatService.concatenate(segmentUrls, outputFilename);

        // 存储本地绝对路径，Controller 层转换为可访问的 URL
        bo.setFinalVideoUrl(finalVideoPath);
        workflowStateStore.save(bo);
        log.info("视频拼接完成，workflowId={}, finalVideoPath={}", bo.getWorkflowId(), finalVideoPath);
    }

    /**
     * 构建文生文节点的用户输入：原始 prompt + 片段数要求。
     */
    private String buildUserPrompt(WorkflowTextToVideoBO bo) {
        return bo.getPrompt()
                + "\n\n视频总时长 " + bo.getDuration() + " 秒，"
                + "需拆分为 " + bo.getSegmentCount() + " 个 5 秒片段，"
                + "请输出 " + bo.getSegmentCount() + " 段视频描述。";
    }

    /**
     * 解析文生文节点的 JSON 输出为图片描述与视频描述列表。
     */
    private TextDescriberOutputDTO parseDescriberOutput(String content) {
        String json = extractJson(content);
        try {
            return objectMapper.readValue(json, TextDescriberOutputDTO.class);
        } catch (Exception e) {
            log.error("解析文生文节点输出失败，原文：{}", content, e);
            throw new RuntimeException("文生文节点输出格式异常，无法解析为图片/视频描述", e);
        }
    }

    /** 从可能含 ```json 代码块或前后文字的内容中提取 JSON 字符串。 */
    private String extractJson(String content) {
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            if (firstNewline > 0) {
                trimmed = trimmed.substring(firstNewline + 1);
            }
            int lastFence = trimmed.lastIndexOf("```");
            if (lastFence >= 0) {
                trimmed = trimmed.substring(0, lastFence);
            }
            trimmed = trimmed.trim();
        }
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }
        return trimmed;
    }
}
