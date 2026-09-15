package com.example.gushingbackend.client;

import com.example.gushingbackend.config.MiniMaxProperties;
import com.example.gushingbackend.model.dto.MiniMaxI2VQueryRespDTO;
import com.example.gushingbackend.model.dto.MiniMaxI2VSubmitReqDTO;
import com.example.gushingbackend.model.dto.MiniMaxI2VSubmitRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * MiniMax 海螺 视频生成 API 客户端。
 * 封装对 MiniMax V2 异步接口的底层调用：提交任务（多模态 content 数组）+ 查询任务状态。
 * V2 成功后直接返回 content.url，无需像 V1 那样用 file_id 二次换取。
 */
@Component
@RequiredArgsConstructor
public class MiniMaxClient {

    /** V2 视频生成提交任务路径 */
    private static final String I2V_SUBMIT_PATH = "/v2/video_generation";
    /** V2 视频任务查询路径（task_id 作为 path variable） */
    private static final String I2V_QUERY_PATH = "/v2/query/video_generation/";

    private final RestClient miniMaxRestClient;
    private final MiniMaxProperties miniMaxProperties;

    /**
     * 提交视频生成任务（V2 多模态 content 数组）。
     *
     * @param reqDTO 已组装好的请求 DTO
     * @return 提交响应 DTO（含 task_id）
     */
    public MiniMaxI2VSubmitRespDTO submitTask(MiniMaxI2VSubmitReqDTO reqDTO) {
        return miniMaxRestClient.post()
                .uri(I2V_SUBMIT_PATH)
                .body(reqDTO)
                .retrieve()
                .body(MiniMaxI2VSubmitRespDTO.class);
    }

    /**
     * 查询任务状态（V2，task_id 作为 path variable）。
     * 成功后 task.content.url 即为视频下载地址。
     *
     * @param taskId 提交任务返回的 task_id
     * @return 查询响应 DTO（含 task.status 与 task.content.url）
     */
    public MiniMaxI2VQueryRespDTO queryTask(String taskId) {
        return miniMaxRestClient.get()
                .uri(I2V_QUERY_PATH + taskId)
                .retrieve()
                .body(MiniMaxI2VQueryRespDTO.class);
    }

    /** 获取默认模型名称，供 Service 组装请求时使用。 */
    public String getDefaultI2VModel() {
        return miniMaxProperties.getI2vModel();
    }

    /** 获取默认视频时长，供 Service 组装请求时使用。 */
    public Integer getDefaultI2VDuration() {
        return miniMaxProperties.getI2vDuration();
    }

    /** 获取默认视频分辨率，供 Service 组装请求时使用。 */
    public String getDefaultI2VResolution() {
        return miniMaxProperties.getI2vResolution();
    }

    /** 获取轮询间隔（秒）。 */
    public long getPollIntervalSeconds() {
        return miniMaxProperties.getPollIntervalSeconds();
    }

    /** 获取轮询最大等待时长（秒）。 */
    public long getPollMaxSeconds() {
        return miniMaxProperties.getPollMaxSeconds();
    }
}
