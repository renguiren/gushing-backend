package com.example.gushingbackend.model.bo;

import lombok.Data;

/**
 * 文生文业务对象 BO。
 * 后端逻辑处理的中间实体：既承载输入（prompt/systemPrompt），
 * 也承载调用结果（content/model/usage）。
 * <p>
 * 该对象也是工作流节点间串行/并行传递数据的载体：
 * 上游节点的输出 content 可作为下游节点的输入 prompt。
 */
@Data
public class TextGenerationBO {

    // ===== 输入 =====

    /** 用户提示词 */
    private String prompt;

    /** 系统提示词 */
    private String systemPrompt;

    // ===== 输出 =====

    /** 模型生成的文本内容 */
    private String content;

    /** 实际使用的模型名称 */
    private String model;

    /** 生成内容角色 */
    private String role;

    /** 提示词消耗的 token 数 */
    private long promptTokens;

    /** 生成内容消耗的 token 数 */
    private long completionTokens;

    /** 总消耗的 token 数 */
    private long totalTokens;
}
