package com.example.gushingbackend.controller;

import com.example.gushingbackend.model.bo.VideoGenerationBO;
import com.example.gushingbackend.model.bo.WorkflowTextToVideoBO;
import com.example.gushingbackend.model.vo.VideoGenerationRespVO;
import com.example.gushingbackend.model.vo.WorkflowTextToVideoReqVO;
import com.example.gushingbackend.model.vo.WorkflowTextToVideoRespVO;
import com.example.gushingbackend.service.WorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
 */
@RestController
@RequestMapping("/api/workflow")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    /**
     * 文生文 → 文生图 → 图生视频（仅提交）串联工作流。
     * <p>
     * POST /api/workflow/text-to-video
     * <p>
     * 输入一个 prompt，内部依次执行：文生文生成画面描述 → 文生图生成图片 → 提交图生视频任务，
     * 快速返回各阶段结果与 videoTaskId（视频尚未生成完成，videoUrl 为空）。
     * <p>
     * 视频生成耗时长，避免同步等待超时：前端拿到 videoTaskId 后，
     * 轮询调用 {@link #queryVideoTask(String)} 直至 status=Success 获取 videoUrl。
     *
     * @param reqVO 前端入参（prompt 必填，systemPrompt 可选）
     * @return 各阶段产出的关键信息（描述文本、图片 URL、视频 taskId 等）
     */
    @PostMapping("/text-to-video")
    public WorkflowTextToVideoRespVO textToVideo(@Valid @RequestBody WorkflowTextToVideoReqVO reqVO) {
        WorkflowTextToVideoBO bo = toBO(reqVO);
        workflowService.textToVideo(bo);
        return toVO(bo);
    }

    /**
     * 按 taskId 查询图生视频任务状态与结果。
     * <p>
     * GET /api/workflow/video-task/{taskId}
     * <p>
     * 单次查询，不轮询。配合 {@link #textToVideo(WorkflowTextToVideoReqVO)} 返回的 taskId 使用。
     *
     * @param taskId 图生视频任务 ID
     * @return 视频 URL、状态与模型信息（status=Success 时 videoUrl 已填充）
     */
    @GetMapping("/video-task/{taskId}")
    public VideoGenerationRespVO queryVideoTask(@PathVariable String taskId) {
        VideoGenerationBO bo = workflowService.queryVideoTask(taskId);
        return toVideoVO(bo);
    }

    private WorkflowTextToVideoBO toBO(WorkflowTextToVideoReqVO reqVO) {
        WorkflowTextToVideoBO bo = new WorkflowTextToVideoBO();
        bo.setPrompt(reqVO.getPrompt());
        bo.setSystemPrompt(reqVO.getSystemPrompt());
        return bo;
    }

    private WorkflowTextToVideoRespVO toVO(WorkflowTextToVideoBO bo) {
        WorkflowTextToVideoRespVO vo = new WorkflowTextToVideoRespVO();
        vo.setPrompt(bo.getPrompt());
        vo.setTextContent(bo.getTextContent());
        vo.setImageDescription(bo.getImageDescription());
        vo.setVideoDescription(bo.getVideoDescription());
        vo.setTextModel(bo.getTextModel());
        vo.setImageUrl(bo.getImageUrl());
        vo.setImageModel(bo.getImageModel());
        vo.setVideoUrl(bo.getVideoUrl());
        vo.setVideoModel(bo.getVideoModel());
        vo.setVideoTaskId(bo.getVideoTaskId());
        vo.setVideoStatus(bo.getVideoStatus());
        return vo;
    }

    private VideoGenerationRespVO toVideoVO(VideoGenerationBO bo) {
        VideoGenerationRespVO vo = new VideoGenerationRespVO();
        vo.setVideoUrl(bo.getVideoUrl());
        vo.setModel(bo.getModel());
        vo.setTaskId(bo.getTaskId());
        vo.setStatus(bo.getStatus());
        return vo;
    }
}
