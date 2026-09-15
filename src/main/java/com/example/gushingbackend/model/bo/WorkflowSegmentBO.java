package com.example.gushingbackend.model.bo;

import lombok.Data;

/**
 * 工作流单个视频片段的业务对象 BO。
 * <p>
 * 每个片段对应一段 5 秒视频，独立提交给图生视频模型生成。
 * 所有片段共享同一张参考图（subject_reference），但 prompt 各不相同。
 */
@Data
public class WorkflowSegmentBO {

    /** 片段序号（从 0 开始，决定拼接顺序） */
    private int index;

    /** 该片段的视频描述（来自文生文节点的 videoDescriptions[i]） */
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
