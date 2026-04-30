package com.finance.platform.account;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 用户自选微服务启动类。
 */
@SpringBootApplication(scanBasePackages = "com.finance")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.finance.common.feign")
@MapperScan("com.finance.platform.account.mapper")
public class FinanceAccountApplication {

    /**
     * Spring Boot 启动入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(FinanceAccountApplication.class, args);
    }
}

