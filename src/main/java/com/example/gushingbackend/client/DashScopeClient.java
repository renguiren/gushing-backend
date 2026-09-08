package com.example.gushingbackend.client;

import com.example.gushingbackend.config.DashScopeProperties;
import com.example.gushingbackend.model.dto.DashScopeT2IReqDTO;
import com.example.gushingbackend.model.dto.DashScopeT2IRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * 阿里云百炼 / 万相文生图 API 客户端。
 * 封装对万相同步文生图接口的底层调用，对上层（Service）屏蔽 HTTP 细节。
 */
@Component
@RequiredArgsConstructor
public class DashScopeClient {

    /** 万相文生图（同步）接口路径 */
    private static final String T2I_SYNC_PATH = "/api/v1/services/aigc/multimodal-generation/generation";

    private final RestClient dashScopeRestClient;
    private final DashScopeProperties dashScopeProperties;

    /**
     * 调用万相文生图（同步）接口。
     *
     * @param reqDTO 已组装好的请求 DTO
     * @return 万相返回的响应 DTO（含图片 URL）
     */
    public DashScopeT2IRespDTO textToImage(DashScopeT2IReqDTO reqDTO) {
        return dashScopeRestClient.post()
                .uri(T2I_SYNC_PATH)
                .body(reqDTO)
                .retrieve()
                .body(DashScopeT2IRespDTO.class);
    }

    /** 获取默认模型名称，供 Service 组装请求时使用。 */
    public String getDefaultT2IModel() {
        return dashScopeProperties.getT2iModel();
    }

    /** 获取默认输出图像尺寸，供 Service 组装请求时使用。 */
    public String getDefaultT2ISize() {
        return dashScopeProperties.getT2iSize();
    }
}
