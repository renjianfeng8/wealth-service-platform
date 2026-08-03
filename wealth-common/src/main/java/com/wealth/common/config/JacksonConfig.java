package com.wealth.common.config;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson 全局配置 — LocalDateTime 统一 yyyy-MM-dd HH:mm:ss（空串→null）。
 * 分页序列化（Page → pageNum/pageSize）独立 bean，仅类路径含 MyBatis-Plus Page 的模块生效，
 * 避免 gateway 等无数据源模块启动报 NoClassDefFoundError。
 */
@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer javaTimeModuleCustomizer() {
        return builder -> {
            builder.modulesToInstall(new JavaTimeModule());
            builder.serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(DATETIME_FORMAT));
            builder.deserializerByType(LocalDateTime.class, new FlexibleLocalDateTimeDeserializer());
        };
    }

    @Bean
    @ConditionalOnClass(name = "com.baomidou.mybatisplus.extension.plugins.pagination.Page")
    public Jackson2ObjectMapperBuilderCustomizer pageModuleCustomizer() {
        return builder -> builder.serializerByType(Page.class, new PageSerializer());
    }
}
