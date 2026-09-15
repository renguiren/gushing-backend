package com.example.gushingbackend.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * MiniMax V2 视频生成提交响应 DTO。
 * 接口：POST /v2/video_generation
 * <p>
 * V2 提交响应直接返回 task_id，无 base_resp 包装。
 */
@Data
public class MiniMaxI2VSubmitRespDTO {

    /** 异步任务 ID，用于后续查询 */
    @JsonProperty("task_id")
    private String taskId;
}
