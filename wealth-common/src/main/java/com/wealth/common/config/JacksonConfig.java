package com.wealth.common.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.wealth.common.filter.StringXssDeserializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson 全局配置 — 注册 XSS 安全的字符串反序列化器。
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer xssStringDeserializerCustomizer() {
        SimpleModule module = new SimpleModule("StringXssModule");
        module.addDeserializer(String.class, new StringXssDeserializer());
        return builder -> builder.modules(module);
    }
}
