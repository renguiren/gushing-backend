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

    /** 文件 ID（部分响应携带，可用于下载） */
    @com.fasterxml.jackson.annotation.JsonProperty("file_id")
    private String fileId;

    /** 任务状态：Submitted / Processing / Success / Failed */
    private String status;

    /** 生成视频的下载 URL（status=Success 时返回） */
    @com.fasterxml.jackson.annotation.JsonProperty("download_url")
    private String downloadUrl;

    /** 视频在线播放/下载地址（部分版本返回在该字段） */
    @com.fasterxml.jackson.annotation.JsonProperty("video_url")
    private String videoUrl;

    /** 查询结果基础状态 */
    @com.fasterxml.jackson.annotation.JsonProperty("base_resp")
    private MiniMaxI2VSubmitRespDTO.BaseRespDTO baseResp;
}
