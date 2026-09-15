package com.example.gushingbackend.model.vo;

import lombok.Data;

/**
 * 工作流单个视频片段的响应 VO（面向前端）。
 */
@Data
public class WorkflowSegmentVO {

    /** 片段序号（从 0 开始，决定拼接顺序） */
    private int index;

    /** 该片段的视频描述 */
    private String videoDescription;

    /** 图生视频任务 ID */
    private String videoTaskId;

    /** 生成视频的下载 URL */
    private String videoUrl;

    /** 图生视频使用的模型 */
    private String videoModel;

    /** 任务状态：queued / processing / succeeded / failed */
    private String videoStatus;
}
