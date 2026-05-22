package com.wealth.platform.search;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * ES搜索微服务启动类
 * ES 不可用时自动降级 MySQL LIKE 查询，无需 @EnableElasticsearchRepositories
 */
@SpringBootApplication(scanBasePackages = "com.wealth")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.wealth.common.feign")
@MapperScan("com.wealth.platform.search.mapper")
public class WealthSearchApplication {
    public static void main(String[] args) {
        SpringApplication.run(WealthSearchApplication.class, args);
    }
}