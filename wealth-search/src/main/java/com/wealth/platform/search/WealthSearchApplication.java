package com.wealth.platform.search;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * ES搜索微服务启动类
 */
@SpringBootApplication(scanBasePackages = "com.wealth")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.wealth.common.feign")
@EnableElasticsearchRepositories(basePackages = "com.wealth.platform.search.repository")
@MapperScan("com.wealth.platform.search.mapper")
public class WealthSearchApplication {
    public static void main(String[] args) {
        SpringApplication.run(WealthSearchApplication.class, args);
    }
}