package com.example.gushingbackend.model.dto;

import lombok.Data;

/**
 * MiniMax 图生视频提交响应 DTO。
 * 字段映射遵循 MiniMax V1 接口的 snake_case 约定。
 */
@Data
public class MiniMaxI2VSubmitRespDTO {

    /** 异步任务 ID，用于后续查询 */
    @com.fasterxml.jackson.annotation.JsonProperty("task_id")
    private String taskId;

    /** 提交结果状态（含错误码与错误信息） */
    @com.fasterxml.jackson.annotation.JsonProperty("base_resp")
    private BaseRespDTO baseResp;

    /**
     * 提交结果基础状态 DTO（提交与查询共用结构）。
     */
    @Data
    public static class BaseRespDTO {
        /** 状态码，0 表示成功 */
        @com.fasterxml.jackson.annotation.JsonProperty("status_code")
        private int statusCode;
        /** 状态描述 */
        @com.fasterxml.jackson.annotation.JsonProperty("status_msg")
        private String statusMsg;
    }
}
