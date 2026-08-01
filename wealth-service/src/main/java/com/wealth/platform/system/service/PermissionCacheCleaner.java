package com.wealth.platform.system.service;

import com.wealth.common.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 权限缓存清理器：缓存 key 单一来源。只依赖 RedisUtil，
 * 供 PermissionCacheService 与关联表 Service 复用，避免"关联表 Service → PermissionCacheService → 关联表 Service"循环依赖。
 */
@Service
@RequiredArgsConstructor
public class PermissionCacheCleaner {

    public static final String CACHE_KEY_PREFIX = "permission:urls:";

    private final RedisUtil redisUtil;

    public void clear(Long adminId) {
        redisUtil.safeExecuteVoid(() -> redisUtil.delete(CACHE_KEY_PREFIX + adminId), "无法清除权限缓存");
    }
}
