package com.finance.platform.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 后台权限模块微服务启动类。
 */
@SpringBootApplication(scanBasePackages = "com.finance")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.finance.common.feign")
@MapperScan("com.finance.platform.system.mapper")
public class FinanceSystemApplication {

    /**
     * Spring Boot 启动入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(FinanceSystemApplication.class, args);
    }
}

