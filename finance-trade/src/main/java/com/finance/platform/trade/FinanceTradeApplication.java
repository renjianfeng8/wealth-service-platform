package com.finance.platform.trade;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 交易模块微服务启动类。
 */
@SpringBootApplication(scanBasePackages = "com.finance")
@EnableDiscoveryClient
@MapperScan("com.finance.platform.trade.mapper")
public class FinanceTradeApplication {

    /**
     * Spring Boot 启动入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(FinanceTradeApplication.class, args);
    }
}

