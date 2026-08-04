package com.wealth.platform.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 全局 OpenAPI 文档配置。
 *
 * <p>替代 application.yml 中 knife4j.openapi.* 配置，使用标准 springdoc Java API，
 * 避免 IDE 因缺少 Knife4j 配置元数据而报"无法解析配置属性"的警告。
 *
 * <p>Knife4j 4.x 基于 springdoc-openapi，会自动拾取此 OpenAPI Bean 的配置。
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("理财服务平台 — 单体聚合接口")
                        .description("原 6 个业务模块合并后的统一 API 文档")
                        .version("v1.8.2"));
    }
}
