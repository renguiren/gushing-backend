package com.example.gushingbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

/**
 * 当前阶段尚未启用数据库，先排除 DataSource 自动装配，避免无数据源配置时启动失败。
 * 后续接入数据库后移除 exclude 即可。
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class GushingBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(GushingBackendApplication.class, args);
        print();
    }
    public static void print() {
        System.out.println("Gushing Backend Application Started");
    }

}
