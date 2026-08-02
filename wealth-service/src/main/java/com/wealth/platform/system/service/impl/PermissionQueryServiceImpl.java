package com.wealth.platform.system.service.impl;

import com.wealth.common.constants.AuthConstant;
import com.wealth.common.utils.JwtUtil;
import com.wealth.platform.system.entity.UmsAdmin;
import com.wealth.platform.system.service.PermissionCacheService;
import com.wealth.platform.system.service.PermissionQueryService;
import com.wealth.platform.system.service.UmsAdminCrudService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 权限判定实现：由 Token 解析用户名 → 定位未删除管理员 → 委托权限缓存判定。
 */
@Service
@RequiredArgsConstructor
public class PermissionQueryServiceImpl implements PermissionQueryService {

    private final JwtUtil jwtUtil;
    private final UmsAdminCrudService umsAdminCrudService;
    private final PermissionCacheService permissionCacheService;

    @Override
    public boolean checkPermission(String uri, String authHeader) {
        String token = AuthConstant.extractBearerToken(authHeader);
        if (token == null) {
            return false;
        }
        if (!jwtUtil.validateToken(token)) {
            return false;
        }
        return checkPermissionForToken(token, uri);
    }

    @Override
    public boolean checkPermissionForToken(String token, String uri) {
        String username = jwtUtil.getUsernameFromToken(token);
        UmsAdmin admin = umsAdminCrudService.getActiveByUsername(username);
        return admin != null && permissionCacheService.hasPermission(admin.getId(), uri);
    }
}
