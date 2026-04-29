package com.finance.platform.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 产品与行情微服务启动类。
 */
@SpringBootApplication(scanBasePackages = "com.finance")
@EnableDiscoveryClient
@MapperScan("com.finance.platform.product.mapper")
public class FinanceProductApplication {

    /**
     * Spring Boot 启动入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(FinanceProductApplication.class, args);
    }
}

