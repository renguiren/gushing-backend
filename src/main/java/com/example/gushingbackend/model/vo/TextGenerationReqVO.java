package com.example.gushingbackend.model.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 文生文请求 VO（面向前端）。
 */
@Data
public class TextGenerationReqVO {

    /** 用户提示词，必填 */
    @NotBlank(message = "提示词不能为空")
    @Size(max = 32000, message = "提示词长度超出限制")
    private String prompt;

    /** 系统提示词，可选，用于设定模型角色/约束 */
    private String systemPrompt;
}
