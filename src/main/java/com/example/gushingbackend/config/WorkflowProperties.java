package com.example.gushingbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 工作流相关配置属性。
 * 值通过 application.yml 注入，application.yml 中的占位符由 .env 文件提供。
 */
@Data
@ConfigurationProperties(prefix = "workflow")
public class WorkflowProperties {

    /** 单个视频片段的最小生成单元时长（秒），固定为 5 */
    private int segmentDurationSeconds = 5;

    /** 拼接后视频的本地存储目录 */
    private String videoOutputDir = "./data/videos";

    /** FFmpeg 可执行文件路径，默认从 PATH 查找 */
    private String ffmpegPath = "ffmpeg";

    /** 视频片段下载超时时间（秒） */
    private long downloadTimeoutSeconds = 120;
}
