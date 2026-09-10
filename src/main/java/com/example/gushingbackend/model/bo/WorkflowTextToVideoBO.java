package com.example.gushingbackend.model.bo;

import lombok.Data;

/**
 * 文生文→文生图→图生视频 串联工作流业务对象 BO。
 * <p>
 * 既是工作流的入参载体（原始 prompt + 可选各阶段参数），
 * 也承载各节点执行后回写的中间结果与最终结果，便于编排与排查。
 */
@Data
public class WorkflowTextToVideoBO {

    // ===== 原始输入 =====

    /** 原始用户输入提示词，作为文生文节点的输入 */
    private String prompt;

    /** 文生文阶段的系统提示词，可选 */
    private String systemPrompt;

    // ===== 各阶段结果回写 =====

    /** 文生文节点生成的画面描述文本（原始完整输出，便于排查） */
    private String textContent;

    /** 文生文节点输出的图片描述（输入第二层文生图） */
    private String imageDescription;

    /** 文生文节点输出的视频描述（输入第三层图生视频） */
    private String videoDescription;

    /** 文生文节点使用的模型 */
    private String textModel;

    /** 文生图节点生成的图片 URL */
    private String imageUrl;

    /** 文生图节点使用的模型 */
    private String imageModel;

    /** 图生视频节点生成的视频 URL */
    private String videoUrl;

    /** 图生视频节点使用的模型 */
    private String videoModel;

    /** 图生视频节点任务 ID */
    private String videoTaskId;

    /** 图生视频节点任务状态 */
    private String videoStatus;
}
