package com.example.gushingbackend.client;

import com.example.gushingbackend.config.DeepSeekProperties;
import com.example.gushingbackend.model.dto.DeepSeekReqDTO;
import com.example.gushingbackend.model.dto.DeepSeekRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * DeepSeek API 客户端。
 * 封装对 DeepSeek Chat Completions 接口的底层调用，
 * 对上层（Service）屏蔽 HTTP 细节，只关心 DTO 进出。
 */
@Component
@RequiredArgsConstructor
public class DeepSeekClient {

    private static final String CHAT_COMPLETIONS_PATH = "/chat/completions";

    private final RestClient deepSeekRestClient;
    private final DeepSeekProperties deepSeekProperties;

    /**
     * 调用 DeepSeek 文生文接口。
     *
     * @param reqDTO 已组装好的请求 DTO
     * @return DeepSeek 返回的响应 DTO
     */
    public DeepSeekRespDTO chatCompletion(DeepSeekReqDTO reqDTO) {
        return deepSeekRestClient.post()
                .uri(CHAT_COMPLETIONS_PATH)
                .body(reqDTO)
                .retrieve()
                .body(DeepSeekRespDTO.class);
    }

    /**
     * 获取默认模型名称，供 Service 组装请求时使用。
     */
    public String getDefaultModel() {
        return deepSeekProperties.getModel();
    }
}
