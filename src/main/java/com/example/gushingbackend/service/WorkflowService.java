package com.example.gushingbackend.service;

import com.example.gushingbackend.model.bo.VideoGenerationBO;
import com.example.gushingbackend.model.bo.WorkflowTextToVideoBO;

/**
 * AI 工作流服务接口。
 * <p>
 * 编排多个 AI 能力节点的串联/并行执行。当前定义文生文→文生图→图生视频（提交）的串联工作流，
 * 以及按 taskId 查询视频结果的接口。
 */
public interface WorkflowService {

    /**
     * 文生文 → 文生图 → 图生视频（仅提交）串联工作流。
     * <p>
     * 流程：输入 prompt → 调用文生文生成画面描述文本 → 将文本作为 prompt 调用文生图
     * 生成图片 → 将图片 URL 作为首帧提交图生视频任务。
     * <p>
     * 视频生成耗时长，第 3 步仅提交任务即返回 taskId，不轮询等待；
     * 调用方需用 {@link #queryVideoTask(String)} 按 taskId 查询最终视频结果。
     *
     * @param bo 工作流业务对象，至少需提供 prompt（原始输入）
     * @return 写入各阶段结果的工作流业务对象（textContent / imageUrl / videoTaskId 等，videoUrl 未填充）
     */
    WorkflowTextToVideoBO textToVideo(WorkflowTextToVideoBO bo);

    /**
     * 按 taskId 查询图生视频任务状态与结果。
     * <p>
     * 单次查询，不轮询。配合 {@link #textToVideo(WorkflowTextToVideoBO)} 返回的 taskId 使用，
     * 前端可轮询调用本方法直至 status=Success 拿到 videoUrl。
     *
     * @param taskId 图生视频任务 ID
     * @return 写入 status / videoUrl 的视频业务对象
     */
    VideoGenerationBO queryVideoTask(String taskId);
}
