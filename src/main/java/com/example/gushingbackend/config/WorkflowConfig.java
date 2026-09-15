package com.example.gushingbackend.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 工作流相关配置。
 * <p>
 * 注册 {@link WorkflowProperties}，并配置工作流异步执行线程池。
 * 工作流为长耗时任务（文生文→文生图→多段图生视频→拼接），
 * 需在独立线程池中异步执行，避免阻塞 HTTP 请求线程。
 */
@Configuration
@EnableAsync
@EnableConfigurationProperties(WorkflowProperties.class)
public class WorkflowConfig {

    /**
     * 工作流异步执行线程池。
     * 核心线程数 4，最大 8，队列容量 100，适合 IO 密集型的 AI 调用场景。
     */
    @Bean(name = "workflowExecutor")
    public Executor workflowExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("workflow-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
