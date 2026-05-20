package com.wealth.platform.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * ES搜索微服务启动类
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class, scanBasePackages = "com.wealth")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.wealth.common.feign")
@EnableElasticsearchRepositories(basePackages = "com.wealth.platform.search.repository")
public class WealthSearchApplication {
    public static void main(String[] args) {
        SpringApplication.run(WealthSearchApplication.class, args);
    }
}