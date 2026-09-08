package com.example.gushingbackend.model.vo;

import lombok.Data;

/**
 * 图生视频响应 VO（面向前端）。
 */
@Data
public class VideoGenerationRespVO {

    /** 生成视频的下载 URL */
    private String videoUrl;

    /** 实际使用的模型名称 */
    private String model;

    /** 任务 ID */
    private String taskId;

    /** 任务最终状态：Success / Failed */
    private String status;
}
