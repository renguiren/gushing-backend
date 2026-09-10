package com.example.gushingbackend.common;

import com.example.gushingbackend.model.vo.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * 全局异常处理。
 * <p>
 * 统一封装各类异常为 {@link ErrorResponse}，避免向前端泄露堆栈，
 * 同时对上游 AI 接口的 4xx/5xx 透传为 502（网关）语义。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 参数校验失败：400 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, msg, request);
    }

    /** 请求体不可读/格式错误：400 */
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(org.springframework.http.converter.HttpMessageNotReadableException ex,
                                                          HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "请求体格式错误或缺失", request);
    }

    /** 上游 AI 接口返回 4xx：作为网关返回 502 */
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ErrorResponse> handleUpstreamClient(HttpClientErrorException ex,
                                                             HttpServletRequest request) {
        log.warn("上游 AI 接口返回 4xx：{} {}", ex.getStatusCode(), ex.getResponseBodyAsString());
        return build(HttpStatus.BAD_GATEWAY,
                "上游 AI 服务返回 " + ex.getStatusCode().value() + "：" + ex.getResponseBodyAsString(),
                request);
    }

    /** 上游 AI 接口返回 5xx：作为网关返回 502 */
    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<ErrorResponse> handleUpstreamServer(HttpServerErrorException ex,
                                                              HttpServletRequest request) {
        log.error("上游 AI 接口返回 5xx：{} {}", ex.getStatusCode(), ex.getResponseBodyAsString());
        return build(HttpStatus.BAD_GATEWAY,
                "上游 AI 服务异常 " + ex.getStatusCode().value() + "：" + ex.getResponseBodyAsString(),
                request);
    }

    /** 其他 RestClient 响应异常兜底：502 */
    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<ErrorResponse> handleRestClientResponse(RestClientResponseException ex,
                                                                 HttpServletRequest request) {
        log.warn("上游响应异常：{} {}", ex.getStatusCode(), ex.getResponseBodyAsString());
        return build(HttpStatus.BAD_GATEWAY,
                "上游 AI 服务响应异常 " + ex.getStatusCode().value(),
                request);
    }

    /** 业务异常（如工作流中断、轮询超时）：500 */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex,
                                                      HttpServletRequest request) {
        log.error("业务处理异常", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    /** 找不到对应路由：404 */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoHandlerFoundException ex,
                                                       HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "接口不存在", request);
    }

    /** 兜底异常：500 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex,
                                                   HttpServletRequest request) {
        log.error("未捕获异常", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "服务器内部错误", request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message,
                                               HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse();
        body.setTimestamp(Instant.now());
        body.setStatus(status.value());
        body.setError(status.getReasonPhrase());
        body.setMessage(message);
        body.setPath(request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
