package com.example.gushingbackend.model.vo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 文生文→文生图→图生视频 工作流请求 VO（面向前端）。
 */
@Data
public class WorkflowTextToVideoReqVO {

    /** 原始提示词，作为文生文节点输入，必填 */
    @NotBlank(message = "提示词不能为空")
    @Size(max = 32000, message = "提示词长度超出限制")
    private String prompt;

    /** 文生文阶段的系统提示词，可选 */
    private String systemPrompt;

    /**
     * 视频总时长（秒），必填。
     * 后端会按 5 秒最小生成单元拆分，循环生成后拼接。
     * 建议为 5 的倍数；非 5 的倍数会向上取整为 5 的倍数。
     */
    @NotNull(message = "视频时长不能为空")
    @Min(value = 5, message = "视频时长至少为 5 秒")
    @Max(value = 300, message = "视频时长不能超过 300 秒")
    private Integer duration;
}
