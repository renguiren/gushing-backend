package com.example.gushingbackend.service;

import com.example.gushingbackend.model.bo.WorkflowTextToVideoBO;

/**
 * AI 工作流服务接口。
 * <p>
 * 编排多个 AI 能力节点的串联执行。当前定义文生文→文生图→多段图生视频→拼接的工作流，
 * 支持按总时长拆分为 5 秒最小生成单元，循环生成后按顺序拼接。
 */
public interface WorkflowService {

    /**
     * 提交文生文 → 文生图 → 多段图生视频 → 拼接 工作流（异步执行）。
     * <p>
     * 方法立即返回，workflowId 已生成，workflowStatus=PROCESSING。
     * 实际工作流在后台线程异步执行：
     * <ol>
     *   <li>文生文：生成共享图片描述 + 多段视频描述（LLM 分镜）</li>
     *   <li>文生图：用图片描述生成一张共享参考图</li>
     *   <li>图生视频：用共享参考图 + 各段视频描述，循环提交 N 个视频生成任务</li>
     *   <li>轮询所有片段直至全部成功</li>
     *   <li>FFmpeg 拼接所有片段为完整视频</li>
     * </ol>
     * 调用方需用 {@link #queryWorkflow(String)} 按 workflowId 查询最终结果。
     *
     * @param bo 工作流业务对象，需提供 prompt 与 duration
     * @return 写入 workflowId 与 workflowStatus=PROCESSING 的工作流 BO
     */
    WorkflowTextToVideoBO textToVideo(WorkflowTextToVideoBO bo);

    /**
     * 按 workflowId 查询工作流整体状态与结果。
     *
     * @param workflowId 工作流 ID
     * @return 工作流 BO（包含各片段进度与最终拼接视频 URL）
     */
    WorkflowTextToVideoBO queryWorkflow(String workflowId);
}
