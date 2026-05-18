package com.wealth.common.utils;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnClass(name = "org.springframework.data.redis.core.RedisTemplate")
public class RedisUtil {
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 娉ㄥ叆 RedisTemplate锛圫tring/Object 娉涘瀷锛夈€?     *
     * @param redisTemplate RedisTemplate 瀹炰緥
     */
    public RedisUtil(RedisTemplate<String, Object> redisTemplate) {
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
}