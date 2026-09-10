package com.example.gushingbackend.model.vo;

import jakarta.validation.constraints.NotBlank;
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
}
