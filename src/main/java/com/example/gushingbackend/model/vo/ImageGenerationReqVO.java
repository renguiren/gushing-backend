package com.example.gushingbackend.model.vo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 文生图请求 VO（面向前端）。
 */
@Data
public class ImageGenerationReqVO {

    /** 用户提示词，必填 */
    @NotBlank(message = "提示词不能为空")
    @Size(max = 2100, message = "提示词长度超出限制")
    private String prompt;

    /** 负向提示词，可选 */
    @Size(max = 500, message = "负向提示词长度超出限制")
    private String negativePrompt;

    /** 输出图像尺寸，如 1280*1280；不传使用默认配置 */
    private String size;

    /** 生成图片数量 1~4，不传默认 1 */
    @Min(value = 1, message = "生成数量至少为 1")
    @Max(value = 4, message = "生成数量最多为 4")
    private Integer n;

    /** 是否启用提示词改写，不传使用默认 */
    private Boolean promptExtend;

    /** 是否添加 AI 水印，不传使用默认 */
    private Boolean watermark;

    /** 随机种子，可选 */
    private Long seed;
}
