package com.finance.platform.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication(scanBasePackages = "com.finance")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.finance.common.feign")
@MapperScan("com.finance.platform.user.mapper")
public class FinanceUserApplication {
    /**
     * Spring Boot 启动入口
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(FinanceUserApplication.class, args);
    }
}