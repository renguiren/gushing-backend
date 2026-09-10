package com.example.gushingbackend.model.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import lombok.Data;

/**
 * 统一错误响应 VO。
 * 用于全局异常处理器返回，替代 Spring 默认带堆栈的错误体。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /** 时间戳 */
    private Instant timestamp;

    /** HTTP 状态码 */
    private int status;

    /** 错误类型简述 */
    private String error;

    /** 错误详情信息 */
    private String message;

    /** 请求路径 */
    private String path;
}
