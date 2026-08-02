package com.wealth.platform.system.service;

/**
 * 管理员权限判定服务：校验请求或已认证 Token 是否有权访问指定 URI。
 * 供 {@link com.wealth.platform.system.interceptor.PermissionInterceptor} 与 Feign 权限校验接口复用，
 * 权限判定逻辑单一来源。
 */
public interface PermissionQueryService {

    // 校验请求权限（从 Authorization 头解析 token 并校验）
    boolean checkPermission(String uri, String authHeader);

    // 校验已认证 Token 是否有权访问指定 URI（供拦截器复用）
    boolean checkPermissionForToken(String token, String uri);
}
