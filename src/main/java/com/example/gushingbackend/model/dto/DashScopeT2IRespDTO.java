package com.example.gushingbackend.model.dto;

import java.util.List;
import lombok.Data;

/**
 * 阿里云万相文生图（同步接口）响应 DTO。
 * 字段映射遵循 DashScope OpenAI 兼容接口的 snake_case 约定。
 */
@Data
public class DashScopeT2IRespDTO {

    /** 请求 ID */
    @com.fasterxml.jackson.annotation.JsonProperty("request_id")
    private String requestId;

    /** 输出内容 */
    private OutputDTO output;

    /** 用量统计 */
    private UsageDTO usage;

    /**
     * 输出内容 DTO。
     */
    @Data
    public static class OutputDTO {
        /** 生成项列表 */
        private List<ChoiceDTO> choices;
        /** 是否完成 */
        private Boolean finished;
    }

    /**
     * 生成项 DTO。
     */
    @Data
    public static class ChoiceDTO {
        /** 结束原因：stop */
        @com.fasterxml.jackson.annotation.JsonProperty("finish_reason")
        private String finishReason;
        /** 消息体 */
        private RespMessageDTO message;
    }

    /**
     * 消息 DTO。
     */
    @Data
    public static class RespMessageDTO {
        /** 内容数组 */
        private List<RespContentDTO> content;
        /** 角色 */
        private String role;
    }

    /**
     * 内容项 DTO（文生图场景为 image 类型）。
     */
    @Data
    public static class RespContentDTO {
        /** 内容类型：image */
        private String type;
        /** 图片 URL */
        private String image;
    }

    /**
     * 用量统计 DTO。
     */
    @Data
    public static class UsageDTO {
        /** 生成图片数量 */
        @com.fasterxml.jackson.annotation.JsonProperty("image_count")
        private long imageCount;
        /** 输入 token 数 */
        @com.fasterxml.jackson.annotation.JsonProperty("input_tokens")
        private long inputTokens;
        /** 输出 token 数 */
        @com.fasterxml.jackson.annotation.JsonProperty("output_tokens")
        private long outputTokens;
        /** 总 token 数 */
        @com.fasterxml.jackson.annotation.JsonProperty("total_tokens")
        private long totalTokens;
        /** 图像尺寸 */
        private String size;
    }
}
