package com.wealth.common.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * RedisTemplate 通用配置（JSON 序列化）。
 *
 * <p>放在 wealth-common 中，确保所有业务模块在扫描到公共组件时
 * 都能获得 JSON 序列化的 {@code RedisTemplate<String, Object>} Bean。</p>
 * <p>标注 {@code @ConditionalOnClass} 防止无 Redis 依赖的模块（如 wealth-search）启动失败。</p>
 * <p>Bean 名使用 {@code jsonRedisTemplate} 避免与 {@link RedisAutoConfiguration}
 * 的默认 {@code redisTemplate} 冲突。</p>
 */
@AutoConfiguration(after = RedisAutoConfiguration.class)
@ConditionalOnClass(name = "org.springframework.data.redis.core.RedisTemplate")
public class RedisConfig {

    /**
     * 创建 JSON 序列化的 RedisTemplate。
     * <p>使用 {@link GenericJackson2JsonRedisSerializer}（内置类型白名单），
     * 比自定义 ObjectMapper 默认类型绑定更安全。</p>
     *
     * @param factory Redis 连接工厂
     * @return RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> jsonRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
