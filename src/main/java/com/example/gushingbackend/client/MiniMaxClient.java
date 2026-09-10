package com.example.gushingbackend.client;

import com.example.gushingbackend.config.MiniMaxProperties;
import com.example.gushingbackend.model.dto.MiniMaxFileRetrieveRespDTO;
import com.example.gushingbackend.model.dto.MiniMaxI2VQueryRespDTO;
import com.example.gushingbackend.model.dto.MiniMaxI2VSubmitReqDTO;
import com.example.gushingbackend.model.dto.MiniMaxI2VSubmitRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * MiniMax 海螺 图生视频 API 客户端。
 * 封装对 MiniMax V1 异步接口的底层调用：提交任务 + 查询任务状态 + 文件下载地址换取。
 * 对上层（Service）屏蔽 HTTP 细节。
 */
@Component
@RequiredArgsConstructor
public class MiniMaxClient {

    /** 图生视频提交任务路径 */
    private static final String I2V_SUBMIT_PATH = "/v1/video_generation";
    /** 视频任务查询路径 */
    private static final String I2V_QUERY_PATH = "/v1/query/video_generation";
    /** 文件下载地址换取路径 */
    private static final String FILE_RETRIEVE_PATH = "/v1/files/retrieve";

    private final RestClient miniMaxRestClient;
    private final MiniMaxProperties miniMaxProperties;

    /**
     * 提交图生视频任务。
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
     * 查询任务状态。
     *
     * @param taskId 提交任务返回的 task_id
     * @return 查询响应 DTO（含 status 与 file_id；视频下载地址需用 file_id 调 retrieveFileUrl 换取）
     */
    public MiniMaxI2VQueryRespDTO queryTask(String taskId) {
        return miniMaxRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(I2V_QUERY_PATH)
                        .queryParam("task_id", taskId)
                        .build())
                .retrieve()
                .body(MiniMaxI2VQueryRespDTO.class);
    }

    /**
     * 根据 file_id 换取文件（视频）下载地址。
     * <p>
     * MiniMax 查询任务仅返回 file_id，需调用本接口换取有时效的 download_url。
     *
     * @param fileId 查询任务返回的 file_id
     * @return 视频下载地址；换取失败时返回 null
     */
    public String retrieveFileUrl(String fileId) {
        MiniMaxFileRetrieveRespDTO resp = miniMaxRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(FILE_RETRIEVE_PATH)
                        .queryParam("file_id", fileId)
                        .build())
                .retrieve()
                .body(MiniMaxFileRetrieveRespDTO.class);
        if (resp != null && resp.getFile() != null) {
            return resp.getFile().getDownloadUrl();
        }
        return null;
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
