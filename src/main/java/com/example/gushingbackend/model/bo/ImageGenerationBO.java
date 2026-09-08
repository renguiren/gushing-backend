package com.example.gushingbackend.model.bo;

import lombok.Data;

/**
 * 文生图业务对象 BO。
 * 后端逻辑处理的中间实体：既承载输入（prompt/negativePrompt/size/n），
 * 也承载调用结果（imageUrl/model/usage）。
 * <p>
 * 该对象也是工作流节点间串行/并行传递数据的载体：
 * 例如上游文本节点输出的描述可作为本节点的 prompt 输入；
 * 本节点的 imageUrl 又可作为下游图片处理节点的输入。
 */
@Data
public class ImageGenerationBO {

    // ===== 输入 =====

    /** 用户提示词 */
    private String prompt;

    /** 负向提示词（描述不想出现的内容） */
    private String negativePrompt;

    /** 输出图像尺寸，如 1280*1280 */
    private String size;

    /** 生成图片数量，1~4 */
    private Integer n;

    /** 是否启用提示词改写 */
    private Boolean promptExtend;

    /** 是否添加 AI 水印 */
    private Boolean watermark;

    /** 随机种子 */
    private Long seed;

    // ===== 输出 =====

    /** 生成图片 URL */
    private String imageUrl;

    /** 实际使用的模型名称 */
    private String model;

    /** 生成图片数量 */
    private long imageCount;

    /** 总消耗的 token 数 */
    private long totalTokens;

    /** 实际输出图像尺寸 */
    private String outputSize;

    /** 请求 ID（用于排查） */
    private String requestId;
}
