package com.example.gushingbackend.model.dto;

import lombok.Data;

/**
 * MiniMax 视频生成查询响应 DTO。
 * 接口：GET /v1/query/video_generation?task_id=xxx
 * 字段映射遵循 MiniMax V1 接口的 snake_case 约定。
 */
@Data
public class MiniMaxI2VQueryRespDTO {

    /** 任务 ID */
    @com.fasterxml.jackson.annotation.JsonProperty("task_id")
    private String taskId;

    /** 文件 ID（任务成功后返回，用于调用 file retrieve 换取视频下载地址） */
    @com.fasterxml.jackson.annotation.JsonProperty("file_id")
    private String fileId;

    /** 任务状态：Submitted / Processing / Success / Failed */
    private String status;

    /** 查询结果基础状态 */
    @com.fasterxml.jackson.annotation.JsonProperty("base_resp")
    private MiniMaxI2VSubmitRespDTO.BaseRespDTO baseResp;
}
