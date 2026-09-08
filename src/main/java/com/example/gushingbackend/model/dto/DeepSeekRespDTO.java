package com.example.gushingbackend.model.dto;

import java.util.List;
import lombok.Data;

/**
 * DeepSeek Chat Completions 响应 DTO（后端间/与外部 AI 交互的传输实体）。
 * 字段映射遵循 DeepSeek OpenAI 兼容接口的 snake_case 约定。
 */
@Data
public class DeepSeekRespDTO {

    /** 响应 ID */
    private String id;

    /** 实际使用的模型名称 */
    private String model;

    /** 生成结果列表 */
    private List<DeepSeekChoiceDTO> choices;

    /** token 用量统计 */
    private DeepSeekUsageDTO usage;

    /**
     * 生成项 DTO。
     */
    @Data
    public static class DeepSeekChoiceDTO {
        /** 序号 */
        private Integer index;
        /** 消息体 */
        private DeepSeekMessageDTO message;
        /** 结束原因：stop / length / content_filter / tool_calls */
        @com.fasterxml.jackson.annotation.JsonProperty("finish_reason")
        private String finishReason;
    }

    /**
     * token 用量 DTO。
     */
    @Data
    public static class DeepSeekUsageDTO {
        @com.fasterxml.jackson.annotation.JsonProperty("prompt_tokens")
        private long promptTokens;
        @com.fasterxml.jackson.annotation.JsonProperty("completion_tokens")
        private long completionTokens;
        @com.fasterxml.jackson.annotation.JsonProperty("total_tokens")
        private long totalTokens;
    }
}
