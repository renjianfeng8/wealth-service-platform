package com.wealth.common.config;

import feign.Request;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * OpenFeign 全局配置：超时 + 重试。
 * 各 FeignClient 通过 {@code @FeignClient(configuration = FeignConfig.class)} 引用。
 */
@Configuration
public class FeignConfig {

    @Bean
    public Request.Options options() {
        return new Request.Options(
                5000, TimeUnit.MILLISECONDS,   // connectTimeout：5 秒
                10000, TimeUnit.MILLISECONDS,  // readTimeout：10 秒
                true                            // followRedirects
        );
    }

    @Bean
    public Retryer retryer() {
        // period=100ms, maxPeriod=1000ms, maxAttempts=3
        return new Retryer.Default(100, 1000, 3);
    }
}
