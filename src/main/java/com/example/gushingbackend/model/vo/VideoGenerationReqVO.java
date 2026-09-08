package com.example.gushingbackend.model.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 图生视频请求 VO（面向前端）。
 */
@Data
public class VideoGenerationReqVO {

    /** 视频文本描述，必填 */
    @NotBlank(message = "提示词不能为空")
    @Size(max = 2000, message = "提示词长度超出限制")
    private String prompt;

    /** 首帧图片 URL，图生视频必填 */
    @NotBlank(message = "首帧图片不能为空")
    private String firstFrameImage;

    /** 尾帧图片 URL，可选 */
    private String lastFrameImage;

    /** 视频时长（秒），6 或 10；不传使用默认配置 */
    private Integer duration;

    /** 视频分辨率，如 1080P / 768P；不传使用默认配置 */
    private String resolution;
}
