package com.example.gushingbackend.model.vo;

import lombok.Data;

/**
 * 文生图响应 VO（面向前端）。
 */
@Data
public class ImageGenerationRespVO {

    /** 生成图片 URL（有效期 24 小时） */
    private String imageUrl;

    /** 实际使用的模型名称 */
    private String model;

    /** 生成图片数量 */
    private long imageCount;

    /** 总消耗的 token 数 */
    private long totalTokens;

    /** 实际输出图像尺寸 */
    private String outputSize;

    /** 请求 ID */
    private String requestId;
}
