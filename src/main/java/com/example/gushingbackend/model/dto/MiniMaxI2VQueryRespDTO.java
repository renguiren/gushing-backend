package com.example.gushingbackend.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * MiniMax V2 视频生成查询响应 DTO。
 * 接口：GET /v2/query/video_generation/{task_id}
 * <p>
 * V2 查询响应将任务信息嵌套在 task 对象中，成功后 content.url 即为视频下载地址，
 * 无需再像 V1 那样用 file_id 二次换取。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MiniMaxI2VQueryRespDTO {

    /** 任务信息 */
    private TaskDTO task;

    /**
     * 任务信息 DTO。
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TaskDTO {
        /** 任务状态：queued / running / succeeded / failed / cancelled */
        private String status;
        /** 成片内容（status=succeeded 时包含视频下载地址） */
        private ContentDTO content;
        /** 失败时的错误信息 */
        private String error;
    }

    /**
     * 成片内容 DTO。
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContentDTO {
        /** 视频下载地址 */
        private String url;
    }
}
