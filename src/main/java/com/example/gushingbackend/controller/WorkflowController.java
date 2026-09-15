package com.example.gushingbackend.controller;

import com.example.gushingbackend.config.WorkflowProperties;
import com.example.gushingbackend.model.bo.WorkflowSegmentBO;
import com.example.gushingbackend.model.bo.WorkflowTextToVideoBO;
import com.example.gushingbackend.model.vo.WorkflowSegmentVO;
import com.example.gushingbackend.model.vo.WorkflowTextToVideoReqVO;
import com.example.gushingbackend.model.vo.WorkflowTextToVideoRespVO;
import com.example.gushingbackend.service.WorkflowService;
import jakarta.validation.Valid;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 工作流编排 RESTful 接口。
 * <p>
 * 与 {@link AiController} 分离：本接口只负责多 AI 节点的串联编排，
 * 内部复用 AiService 的单节点能力，对外暴露工作流级接口。
 * <p>
 * 工作流为后端驱动的异步任务：提交后立即返回 workflowId，
 * 前端轮询 {@link #queryWorkflow(String)} 直至 workflowStatus=SUCCESS 获取 finalVideoUrl。
 */
@Slf4j
@RestController
@RequestMapping("/api/workflow")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;
    private final WorkflowProperties workflowProperties;

    /**
     * 提交文生文 → 文生图 → 多段图生视频 → 拼接 工作流（异步）。
     * <p>
     * POST /api/workflow/text-to-video
     * <p>
     * 输入 prompt 与 duration（总时长秒），后端按 5 秒最小生成单元拆分，
     * 异步执行完整工作流，立即返回 workflowId（workflowStatus=PROCESSING）。
     * <p>
     * 前端拿到 workflowId 后，轮询调用 {@link #queryWorkflow(String)}
     * 直至 workflowStatus=SUCCESS 获取 finalVideoUrl。
     *
     * @param reqVO 前端入参（prompt 必填，duration 必填）
     * @return 工作流 ID 与初始状态
     */
    @PostMapping("/text-to-video")
    public WorkflowTextToVideoRespVO textToVideo(@Valid @RequestBody WorkflowTextToVideoReqVO reqVO) {
        WorkflowTextToVideoBO bo = toBO(reqVO);
        workflowService.textToVideo(bo);
        return toVO(bo);
    }

    /**
     * 按 workflowId 查询工作流整体状态与各片段进度。
     * <p>
     * GET /api/workflow/text-to-video/{workflowId}
     * <p>
     * 返回工作流当前状态、各片段生成进度，以及最终拼接视频 URL（成功后）。
     *
     * @param workflowId 工作流 ID
     * @return 工作流状态与结果
     */
    @GetMapping("/text-to-video/{workflowId}")
    public WorkflowTextToVideoRespVO queryWorkflow(@PathVariable String workflowId) {
        WorkflowTextToVideoBO bo = workflowService.queryWorkflow(workflowId);
        return toVO(bo);
    }

    /**
     * 下载工作流拼接完成的最终视频文件。
     * <p>
     * GET /api/workflow/videos/{filename}
     * <p>
     * 拼接后的视频存储在本地，通过此接口提供下载/播放。
     *
     * @param filename 视频文件名（如 workflow_xxx.mp4）
     * @return 视频文件流
     */
    @GetMapping("/videos/{filename}")
    public ResponseEntity<Resource> downloadVideo(@PathVariable String filename) {
        Path videoDir = Path.of(workflowProperties.getVideoOutputDir()).toAbsolutePath();
        Path videoFile = videoDir.resolve(filename).normalize();

        // 防止路径遍历攻击
        if (!videoFile.startsWith(videoDir)) {
            return ResponseEntity.badRequest().build();
        }
        if (!Files.exists(videoFile)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(videoFile);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("video/mp4"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }

    // ===== BO ↔ VO 转换 =====

    private WorkflowTextToVideoBO toBO(WorkflowTextToVideoReqVO reqVO) {
        WorkflowTextToVideoBO bo = new WorkflowTextToVideoBO();
        bo.setPrompt(reqVO.getPrompt());
        bo.setSystemPrompt(reqVO.getSystemPrompt());
        bo.setDuration(reqVO.getDuration());
        return bo;
    }

    private WorkflowTextToVideoRespVO toVO(WorkflowTextToVideoBO bo) {
        WorkflowTextToVideoRespVO vo = new WorkflowTextToVideoRespVO();
        vo.setWorkflowId(bo.getWorkflowId());
        vo.setWorkflowStatus(bo.getWorkflowStatus());
        vo.setErrorMessage(bo.getErrorMessage());
        vo.setPrompt(bo.getPrompt());
        vo.setDuration(bo.getDuration());
        vo.setSegmentCount(bo.getSegmentCount());
        vo.setTextContent(bo.getTextContent());
        vo.setImageDescription(bo.getImageDescription());
        vo.setVideoDescriptions(bo.getVideoDescriptions());
        vo.setTextModel(bo.getTextModel());
        vo.setImageUrl(bo.getImageUrl());
        vo.setImageModel(bo.getImageModel());
        vo.setSegments(toSegmentVOList(bo.getSegments()));
        // 将本地绝对路径转换为可访问的 URL
        vo.setFinalVideoUrl(toVideoUrl(bo.getFinalVideoUrl()));
        return vo;
    }

    private List<WorkflowSegmentVO> toSegmentVOList(List<WorkflowSegmentBO> segments) {
        if (segments == null) {
            return null;
        }
        List<WorkflowSegmentVO> voList = new ArrayList<>();
        for (WorkflowSegmentBO seg : segments) {
            WorkflowSegmentVO vo = new WorkflowSegmentVO();
            vo.setIndex(seg.getIndex());
            vo.setVideoDescription(seg.getVideoDescription());
            vo.setVideoTaskId(seg.getVideoTaskId());
            vo.setVideoUrl(seg.getVideoUrl());
            vo.setVideoModel(seg.getVideoModel());
            vo.setVideoStatus(seg.getVideoStatus());
            voList.add(vo);
        }
        return voList;
    }

    /**
     * 将拼接后视频的本地绝对路径转换为可访问的 URL。
     * 例如 /data/videos/workflow_xxx.mp4 → /api/workflow/videos/workflow_xxx.mp4
     */
    private String toVideoUrl(String localPath) {
        if (localPath == null || localPath.isEmpty()) {
            return null;
        }
        Path path = Path.of(localPath);
        return "/api/workflow/videos/" + path.getFileName();
    }
}
