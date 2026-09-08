package com.example.gushingbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MiniMax（海螺 图生视频）配置属性。
 * 值通过 application.yml 注入，application.yml 中的占位符由 .env 文件提供。
 */
@Data
@ConfigurationProperties(prefix = "ai.minimax")
public class MiniMaxProperties {

    /** MiniMax API Key */
    private String apiKey;

    /** MiniMax 服务基础地址（国内 https://api.minimax.cn，海外 https://api.minimax.io） */
    private String baseUrl;

    /** 图生视频默认模型名称 */
    private String i2vModel;

    /** 默认视频时长（秒），6 或 10 */
    private Integer i2vDuration;

    /** 默认视频分辨率，如 1080P / 768P */
    private String i2vResolution;

    /** 查询轮询间隔（秒） */
    private long pollIntervalSeconds;

    /** 轮询最大等待时长（秒），超时抛异常 */
    private long pollMaxSeconds;
}
