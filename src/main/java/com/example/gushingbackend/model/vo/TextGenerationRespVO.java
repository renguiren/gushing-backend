package com.example.gushingbackend.model.vo;

import lombok.Data;

/**
 * 文生文响应 VO（面向前端）。
 */
@Data
public class TextGenerationRespVO {

    /** 模型生成的文本内容 */
    private String content;

    /** 实际使用的模型名称 */
    private String model;

    /** 生成内容角色，通常为 assistant */
    private String role;

    /** 提示词消耗的 token 数 */
    private long promptTokens;

    /** 生成内容消耗的 token 数 */
    private long completionTokens;

    /** 总消耗的 token 数 */
    private long totalTokens;
}
