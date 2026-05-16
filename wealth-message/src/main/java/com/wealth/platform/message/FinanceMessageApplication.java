package com.wealth.platform.message;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 资讯与消息微服务启动类。
 */
@SpringBootApplication(scanBasePackages = "com.wealth")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.wealth.common.feign")
@MapperScan("com.wealth.platform.message.mapper")
public class FinanceMessageApplication {

    /**
     * Spring Boot 启动入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(FinanceMessageApplication.class, args);
    }
}

