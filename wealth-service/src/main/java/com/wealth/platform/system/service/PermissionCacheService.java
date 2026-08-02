package com.wealth.platform.system.service;

import com.wealth.common.utils.PathMatchers;
import com.wealth.common.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 权限缓存服务，统一管理权限的加载、缓存与校验。
 * 收敛 PermissionInterceptor 和 PermissionQueryServiceImpl 中两套独立的权限加载逻辑。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionCacheService {

    private static final String CACHE_KEY_PREFIX = PermissionCacheCleaner.CACHE_KEY_PREFIX;
    private static final long CACHE_TTL_HOURS = 1;

    private final UmsAdminRoleRelationService adminRoleRelationService;
    private final UmsRoleResourceRelationService roleResourceRelationService;
    private final UmsResourceService resourceService;
    private final RedisUtil redisUtil;
    private final PermissionCacheCleaner cacheCleaner;

    /**
     * 获取指定管理员的权限 URL 列表（优先缓存，降级到数据库）。
     */
    public List<String> getAllowedUrls(Long adminId) {
        String cacheKey = CACHE_KEY_PREFIX + adminId;

        // 1. 尝试从缓存读取
        List<String> cached = getCached(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 2. 从数据库加载角色 → 资源 → URL
        List<Long> roleIds = adminRoleRelationService.getRoleIdByAdminId(adminId);
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> resourceIds = roleResourceRelationService.getResourceIdByRoleIds(roleIds);
        if (resourceIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> urlPatterns = resourceService.getUrlByResourceIds(resourceIds);

        // 3. 写入缓存
        setCache(cacheKey, urlPatterns);

        return urlPatterns;
    }

    /**
     * 校验指定管理员是否有权访问指定 URI。
     */
    public boolean hasPermission(Long adminId, String uri) {
        List<String> urls = getAllowedUrls(adminId);
        return urls.stream().anyMatch(pattern -> PathMatchers.INSTANCE.match(pattern, uri));
    }

    /**
     * 清除指定管理员的权限缓存。
     */
    public void clearCache(Long adminId) {
        cacheCleaner.clear(adminId);
    }

    private List<String> getCached(String cacheKey) {
        return redisUtil.safeExecute(() -> {
            Object cached = redisUtil.get(cacheKey);
            if (cached instanceof List<?> list && list.stream().allMatch(String.class::isInstance)) {
                return list.stream().map(String.class::cast).collect(Collectors.toList());
            }
            return null;
        }, null, "降级到数据库查询");
    }

    private void setCache(String cacheKey, List<String> urls) {
        if (urls == null) return;
        redisUtil.safeExecuteVoid(() -> redisUtil.set(cacheKey, urls, CACHE_TTL_HOURS, TimeUnit.HOURS), "无法写入权限缓存");
    }
}
