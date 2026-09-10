package com.example.gushingbackend.model.vo;

import lombok.Data;

/**
 * 文生文→文生图→图生视频 工作流响应 VO（面向前端）。
 * 汇总各阶段产出的关键信息，便于前端展示与排查。
 */
@Data
public class WorkflowTextToVideoRespVO {

    /** 原始输入提示词 */
    private String prompt;

    /** 文生文节点生成的画面描述文本（原始完整输出） */
    private String textContent;

    /** 文生文节点输出的图片描述（输入文生图节点） */
    private String imageDescription;

    /** 文生文节点输出的视频描述（输入图生视频节点） */
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
