package com.example.gushingbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 阿里云百炼 / DashScope（万相文生图）配置属性。
 * 值通过 application.yml 注入，application.yml 中的占位符由 .env 文件提供。
 */
@Data
@ConfigurationProperties(prefix = "ai.dashscope")
public class DashScopeProperties {

    /** DashScope API Key */
    private String apiKey;

    /** DashScope 服务基础地址 */
    private String baseUrl;

    /** 文生图默认模型名称（同步接口仅 wan2.6-t2i 支持） */
    private String t2iModel;

    /** 默认输出图像尺寸，格式 宽*高，如 1280*1280 */
    private String t2iSize;

    /** 请求超时时间（秒） */
    private long timeoutSeconds;
}
