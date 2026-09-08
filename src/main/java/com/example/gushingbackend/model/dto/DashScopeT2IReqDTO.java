package com.example.gushingbackend.model.dto;

import java.util.List;
import lombok.Data;

/**
 * 阿里云万相文生图（同步接口）请求 DTO。
 * 接口：POST /api/v1/services/aigc/multimodal-generation/generation
 * 字段映射遵循 DashScope OpenAI 兼容接口的 snake_case 约定。
 */
@Data
public class DashScopeT2IReqDTO {

    /** 模型名称，同步接口固定 wan2.6-t2i */
    private String model;

    /** 输入内容 */
    private InputDTO input;

    /** 生成参数 */
    private ParametersDTO parameters;

    /**
     * 输入内容 DTO。
     */
    @Data
    public static class InputDTO {
        /** 消息列表，仅支持单轮 */
        private List<MessageDTO> messages;
    }

    /**
     * 消息 DTO。
     */
    @Data
    public static class MessageDTO {
        /** 角色，固定 user */
        private String role;
        /** 内容数组 */
        private List<ContentDTO> content;
    }

    /**
     * 内容项 DTO（文生图场景仅使用 text）。
     */
    @Data
    public static class ContentDTO {
        /** 内容类型：text / image */
        private String type;
        /** 文本提示词（type=text 时） */
        private String text;
        /** 图片 URL（type=image 时，文生图场景不使用） */
        private String image;
    }

    /**
     * 生成参数 DTO。
     */
    @Data
    public static class ParametersDTO {
        /** 输出图像尺寸，格式 宽*高，如 1280*1280 */
        private String size;
        /** 生成图片数量，1~4 */
        private Integer n;
        /** 负向提示词 */
        @com.fasterxml.jackson.annotation.JsonProperty("negative_prompt")
        private String negativePrompt;
        /** 是否启用提示词改写 */
        @com.fasterxml.jackson.annotation.JsonProperty("prompt_extend")
        private Boolean promptExtend;
        /** 是否添加 AI 水印 */
        private Boolean watermark;
        /** 随机种子，可不传 */
        private Long seed;
    }
}
