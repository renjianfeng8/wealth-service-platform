package com.finance.platform.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * ES搜索微服务启动类
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.finance.common.feign")
@EnableElasticsearchRepositories(basePackages = "com.finance.platform.search.repository")
public class FinanceSearchApplication {
    public static void main(String[] args) {
        SpringApplication.run(FinanceSearchApplication.class, args);
    }
}