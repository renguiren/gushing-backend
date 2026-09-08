package com.example.gushingbackend.model.dto;

import lombok.Data;

/**
 * DeepSeek 消息体 DTO（请求/响应共用结构）。
 * <p>
 * 提升为顶层类，避免在请求与响应 DTO 中各嵌套一个同名内部类导致引用歧义。
 */
@Data
public class DeepSeekMessageDTO {

    /** 角色：system / user / assistant */
    private String role;

    /** 内容 */
    private String content;
}
