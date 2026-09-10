package com.example.gushingbackend.service.impl;

import com.example.gushingbackend.model.bo.ImageGenerationBO;
import com.example.gushingbackend.model.bo.TextGenerationBO;
import com.example.gushingbackend.model.bo.VideoGenerationBO;
import com.example.gushingbackend.model.bo.WorkflowTextToVideoBO;
import com.example.gushingbackend.model.dto.TextDescriberOutputDTO;
import com.example.gushingbackend.service.AiService;
import com.example.gushingbackend.service.WorkflowService;
import tools.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * WorkflowService 实现：编排文生文→文生图→图生视频（提交）的工作流。
 * <p>
 * 复用 {@link AiService} 暴露的单节点能力，通过 BO 在节点间传递数据：
 * <ol>
 *   <li>原始 prompt → 文生文节点（加载 skill.md 作为系统提示词）→ 输出图片描述 + 视频描述</li>
 *   <li>图片描述 → 文生图节点 → 输出 imageUrl</li>
 *   <li>imageUrl 作为首帧 + 视频描述作为 prompt → 图生视频节点（仅提交，不轮询）→ 输出 taskId</li>
 * </ol>
 * 第一层输出拆分为两个互补描述：图片描述侧重静态画面构成（供文生图），
 * 视频描述侧重动态演进、镜头运动、光影变化（供图生视频），保证视频提示词不被图片提示词淹没。
 * 视频生成耗时长，第 3 步只提交任务即返回 taskId，由前端按 taskId 调用
 * {@link #queryVideoTask(String)} 查询最终视频结果，避免接口同步等待超时。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowServiceImpl implements WorkflowService {

    /** 文生文节点的 skill 提示词文件路径（classpath） */
    private static final String SKILL_PATH = "classpath:skills/text-to-video-describer.md";

    private final AiService aiService;
    private final ObjectMapper objectMapper;
    private final org.springframework.core.io.ResourceLoader resourceLoader;

    /** 加载后的 skill 提示词内容，作为文生文节点的 system prompt */
    private String describerSkill;

    @PostConstruct
    public void init() throws IOException {
        Resource resource = resourceLoader.getResource(SKILL_PATH);
        this.describerSkill = resource.getContentAsString(StandardCharsets.UTF_8);
    }

    @Override
    public WorkflowTextToVideoBO textToVideo(WorkflowTextToVideoBO bo) {
        // 1. 文生文：原始 prompt → 图片描述 + 视频描述（skill 作为系统提示词）
        TextGenerationBO textBO = new TextGenerationBO();
        textBO.setPrompt(bo.getPrompt());
        textBO.setSystemPrompt(describerSkill);
        aiService.textGeneration(textBO);
        String textContent = textBO.getContent();
        bo.setTextContent(textContent);
        bo.setTextModel(textBO.getModel());

        if (!StringUtils.hasText(textContent)) {
            throw new RuntimeException("文生文节点未生成内容，工作流中断");
        }

        // 解析结构化输出，拆分为图片描述与视频描述
        TextDescriberOutputDTO output = parseDescriberOutput(textContent);
        String imageDescription = output.getImageDescription();
        String videoDescription = output.getVideoDescription();
        bo.setImageDescription(imageDescription);
        bo.setVideoDescription(videoDescription);

        if (!StringUtils.hasText(imageDescription)) {
            throw new RuntimeException("文生文节点未输出图片描述，工作流中断");
        }
        if (!StringUtils.hasText(videoDescription)) {
            throw new RuntimeException("文生文节点未输出视频描述，工作流中断");
        }

        // 2. 文生图：图片描述 → 图片
        ImageGenerationBO imageBO = new ImageGenerationBO();
        imageBO.setPrompt(imageDescription);
        aiService.imageGeneration(imageBO);
        String imageUrl = imageBO.getImageUrl();
        bo.setImageUrl(imageUrl);
        bo.setImageModel(imageBO.getModel());

        if (!StringUtils.hasText(imageUrl)) {
            throw new RuntimeException("文生图节点未生成图片，工作流中断");
        }

        // 3. 图生视频（仅提交，不轮询）：图片作为首帧 + 视频描述作为 prompt → taskId
        // 视频生成耗时长，提交后立即返回 taskId，由前端按 taskId 查询最终结果
        VideoGenerationBO videoBO = new VideoGenerationBO();
        videoBO.setPrompt(videoDescription);
        videoBO.setFirstFrameImage(imageUrl);
        aiService.submitVideoGeneration(videoBO);
        bo.setVideoModel(videoBO.getModel());
        bo.setVideoTaskId(videoBO.getTaskId());
        bo.setVideoStatus(videoBO.getStatus());
        // videoUrl 此时尚未生成，置空，待前端查询
        bo.setVideoUrl(null);

        return bo;
    }

    @Override
    public VideoGenerationBO queryVideoTask(String taskId) {
        VideoGenerationBO bo = new VideoGenerationBO();
        bo.setTaskId(taskId);
        return aiService.queryVideoTask(bo);
    }

    /**
     * 解析文生文节点的 JSON 输出为图片描述与视频描述。
     * 兼容模型可能包裹的 ```json 代码块或前后多余文字。
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
        // 去除 ```json ... ``` 代码块包裹
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
        // 定位首个 { 与末个 }，截取最外层 JSON 对象
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }
        return trimmed;
    }
}
