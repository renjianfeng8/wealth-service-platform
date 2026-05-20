package com.wealth.platform.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 产品与行情微服务启动类。
 */
@SpringBootApplication(scanBasePackages = "com.wealth")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.wealth.common.feign")
@MapperScan("com.wealth.platform.product.mapper")
@EnableScheduling
public class WealthProductApplication {

    /**
     * Spring Boot 启动入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(WealthProductApplication.class, args);
    }
}

