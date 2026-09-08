package com.example.gushingbackend.model.dto;

import lombok.Data;

/**
 * MiniMax 图生视频提交请求 DTO。
 * 接口：POST /v1/video_generation
 * 字段映射遵循 MiniMax V1 接口的 snake_case 约定。
 */
@Data
public class MiniMaxI2VSubmitReqDTO {

    /** 视频文本描述，必填 */
    private String prompt;

    /** 首帧图片 URL，图生视频必填 */
    @com.fasterxml.jackson.annotation.JsonProperty("first_frame_image")
    private String firstFrameImage;

    /** 尾帧图片 URL，可选（用于首尾帧生成） */
    @com.fasterxml.jackson.annotation.JsonProperty("last_frame_image")
    private String lastFrameImage;

    /** 模型名称，如 MiniMax-Hailuo-2.3 */
    private String model;

    /** 视频时长（秒），6 或 10 */
    private Integer duration;

    /** 视频分辨率，如 1080P / 768P */
    private String resolution;
}
