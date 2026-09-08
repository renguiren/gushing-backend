package com.example.gushingbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * DeepSeek API 配置属性。
 * 值通过 application.yml 注入，application.yml 中的占位符由 .env 文件提供。
 */
@Data
@ConfigurationProperties(prefix = "ai.deepseek")
public class DeepSeekProperties {

    /** DeepSeek API Key */
    private String apiKey;

    /** DeepSeek 服务基础地址 */
    private String baseUrl;

    /** 默认调用的模型名称 */
    private String model;

    /** 请求超时时间（秒） */
    private long timeoutSeconds;
}
