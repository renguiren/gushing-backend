package com.example.gushingbackend.model.bo;

import java.util.List;
import lombok.Data;

/**
 * 文生文→文生图→图生视频（多片段）串联工作流业务对象 BO。
 * <p>
 * 支持按总时长拆分为多个 5 秒最小生成单元，循环生成后按顺序拼接。
 * 所有片段共享同一张参考图（保证风格一致），但视频描述各不相同（LLM 分镜）。
 */
@Data
public class WorkflowTextToVideoBO {

    // ===== 原始输入 =====

    /** 原始用户输入提示词，作为文生文节点的输入 */
    private String prompt;

    /** 文生文阶段的系统提示词，可选 */
    private String systemPrompt;

    /** 视频总时长（秒），前端传入 */
    private Integer duration;

    /** 5 秒片段数量 = ceil(duration / 5)，由后端计算 */
    private Integer segmentCount;

    // ===== 工作流元信息 =====

    /** 工作流唯一 ID，用于前端轮询查询 */
    private String workflowId;

    /** 工作流整体状态：PROCESSING / SUCCESS / FAILED */
    private String workflowStatus;

    /** 失败时的错误信息 */
    private String errorMessage;

    // ===== 各阶段结果回写 =====

    /** 文生文节点生成的画面描述文本（原始完整输出，便于排查） */
    private String textContent;

    /** 文生文节点输出的图片描述（所有片段共享，输入第二层文生图） */
    private String imageDescription;

    /** 文生文节点输出的视频描述列表（每个元素对应一个片段） */
    private List<String> videoDescriptions;

    /** 文生文节点使用的模型 */
    private String textModel;

    /** 文生图节点生成的图片 URL（所有片段共享的参考图） */
    private String imageUrl;

    /** 文生图节点使用的模型 */
    private String imageModel;

    /** 各视频片段的生成结果（按 index 排序，决定拼接顺序） */
    private List<WorkflowSegmentBO> segments;

    /** 所有片段拼接完成后的最终视频 URL */
    private String finalVideoUrl;
}
