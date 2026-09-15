package com.example.gushingbackend.model.vo;

import java.util.List;
import lombok.Data;

/**
 * 文生文→文生图→图生视频（多片段）工作流响应 VO（面向前端）。
 * <p>
 * 工作流为后端驱动的异步任务：提交后立即返回 workflowId，
 * 前端通过 {@code /api/workflow/text-to-video/{workflowId}} 轮询整体状态，
 * 直至 workflowStatus=SUCCESS 时 finalVideoUrl 可用。
 */
@Data
public class WorkflowTextToVideoRespVO {

    /** 工作流唯一 ID，用于轮询查询 */
    private String workflowId;

    /** 工作流整体状态：PROCESSING / SUCCESS / FAILED */
    private String workflowStatus;

    /** 失败时的错误信息 */
    private String errorMessage;

    /** 原始输入提示词 */
    private String prompt;

    /** 视频总时长（秒） */
    private Integer duration;

    /** 5 秒片段数量 */
    private Integer segmentCount;

    /** 文生文节点生成的画面描述文本（原始完整输出） */
    private String textContent;

    /** 文生文节点输出的图片描述（所有片段共享） */
    private String imageDescription;

    /** 文生文节点输出的视频描述列表 */
    private List<String> videoDescriptions;

    /** 文生文节点使用的模型 */
    private String textModel;

    /** 文生图节点生成的图片 URL（所有片段共享的参考图） */
    private String imageUrl;

    /** 文生图节点使用的模型 */
    private String imageModel;

    /** 各视频片段的生成状态与结果 */
    private List<WorkflowSegmentVO> segments;

    /** 所有片段拼接完成后的最终视频 URL */
    private String finalVideoUrl;
}
