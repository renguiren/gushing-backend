package com.example.gushingbackend.model.dto;

import java.util.List;
import lombok.Data;

/**
 * DeepSeek Chat Completions 请求 DTO（后端间/与外部 AI 交互的传输实体）。
 * 字段映射遵循 DeepSeek OpenAI 兼容接口的 snake_case 约定。
 */
@Data
public class DeepSeekReqDTO {

    /** 调用的模型名称 */
    private String model;

    /** 消息列表 */
    private List<DeepSeekMessageDTO> messages;

    /** 是否流式返回，文生文节点固定 false */
    private Boolean stream;

    /** 采样温度，可选 */
    private Double temperature;

    /** 生成 token 上限，可选 */
    @com.fasterxml.jackson.annotation.JsonProperty("max_tokens")
    private Integer maxTokens;
}
