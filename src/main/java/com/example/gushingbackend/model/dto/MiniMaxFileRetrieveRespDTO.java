package com.example.gushingbackend.model.dto;

import lombok.Data;

/**
 * MiniMax 文件查询响应 DTO。
 * 接口：GET /v1/files/retrieve?file_id=xxx
 * 用于在视频任务完成后，通过 file_id 换取视频的下载地址 download_url。
 * 字段映射遵循 MiniMax V1 接口的 snake_case 约定。
 */
@Data
public class MiniMaxFileRetrieveRespDTO {

    /** 文件信息 */
    private FileDTO file;

    /** 基础状态 */
    @com.fasterxml.jackson.annotation.JsonProperty("base_resp")
    private MiniMaxI2VSubmitRespDTO.BaseRespDTO baseResp;

    /**
     * 文件信息 DTO。
     */
    @Data
    public static class FileDTO {
        /** 文件 ID */
        @com.fasterxml.jackson.annotation.JsonProperty("file_id")
        private long fileId;
        /** 文件字节数 */
        private long bytes;
        /** 创建时间（Unix 秒） */
        @com.fasterxml.jackson.annotation.JsonProperty("created_at")
        private long createdAt;
        /** 文件名 */
        private String filename;
        /** 用途，如 video_generation */
        private String purpose;
        /** 下载地址（有时效） */
        @com.fasterxml.jackson.annotation.JsonProperty("download_url")
        private String downloadUrl;
    }
}
