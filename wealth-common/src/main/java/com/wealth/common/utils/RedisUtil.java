package com.wealth.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Component
@ConditionalOnClass(name = "org.springframework.data.redis.core.RedisTemplate")
public class RedisUtil {
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 注入 RedisTemplate（String/Object 泛型）。
     *
     * @param redisTemplate RedisTemplate 实例
     */
    public RedisUtil(@Qualifier("jsonRedisTemplate") RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    public Long delete(Collection<String> keys) {
        return redisTemplate.delete(keys);
    }

    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /** 仅当 key 不存在时设置值（SET NX），用于分布式锁/防重放等场景 */
    public Boolean setIfAbsent(String key, Object value, long timeout, TimeUnit unit) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
    }

    /** 原子自增，返回自增后的值 */
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * 安全执行 Redis 操作，当 Redis 不可用时记录警告并返回默认值。
     *
     * @param operation Redis 操作
     * @param fallback  Redis 不可用时返回的默认值
     * @param warnMsg  警告日志中的操作描述
     * @param <T>      返回值类型
     */
    public <T> T safeExecute(Supplier<T> operation, T fallback, String warnMsg) {
        try {
            return operation.get();
        } catch (DataAccessException e) {
            log.warn("Redis 不可用，{}: {}", warnMsg, e.getMessage());
            return fallback;
        }
    }

    /**
     * 安全执行 Redis 操作，无需返回值。
     *
     * @param operation Redis 操作
     * @param warnMsg   警告日志中的操作描述
     */
    public void safeExecuteVoid(Runnable operation, String warnMsg) {
        try {
            operation.run();
        } catch (DataAccessException e) {
            log.warn("Redis 不可用，{}: {}", warnMsg, e.getMessage());
        }
    }
}