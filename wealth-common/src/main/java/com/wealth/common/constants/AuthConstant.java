package com.wealth.common.constants;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

public class AuthConstant {

    /** Bearer Token 前缀（含尾随空格） */
    private static final String BEARER_PREFIX = "Bearer ";

    // 无需权限直接放行的接口
    // 开发环境 Swagger/Knife4j 路径加入白名单
    public static final String TOKEN_COOKIE_NAME = "wealth_token";

    /** refresh_token jti 在 Redis 中的 key 前缀（管理端/统一登录共用，供 refresh 接口校验与登出黑名单） */
    public static final String REFRESH_JTI_KEY_PREFIX = "refresh:jti:";

    /**
     * 从 Authorization: Bearer 头中提取 Token。
     * 头缺失或不以 "Bearer " 开头时返回 null（仅提取，不校验 Token 有效性）。
     */
    public static String extractBearerToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return authHeader.substring(BEARER_PREFIX.length());
    }

    /**
     * 从请求中提取 Token：优先从 Authorization: Bearer 头获取，降级到 httpOnly Cookie。
     */
    public static String extractToken(HttpServletRequest request) {
        String token = extractBearerToken(request.getHeader("Authorization"));
        if (token != null) {
            return token;
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public static final String[] PERMIT_ALL_URLS = {
            "/system/umsAdmin/refresh",
            "/system/captcha",
            "/user/register",
            "/user/identify-login",
            "/product/wea-product/page",
            "/product/wea-market-data",
            "/product/wea-market-data/page",
            "/product/wea-market-data/sse/**",
            "/message/wea-news/page",
            /* Swagger / Knife4j */
            "/doc.html",
            "/webjars/**",
            "/v3/api-docs/**",
            "/*/v3/api-docs",
    };

    /**
     * 后台权限拦截器（PermissionInterceptor）放行路径：需登录（LoginInterceptor 仍校验 Token）
     * 但免权限校验的路径。与 PERMIT_ALL_URLS 语义不同，此清单仅用于权限校验放行，不得并入公共白名单。
     */
    public static final String[] PERMISSION_BYPASS_URLS = {
            "/system/dashboard/**",
            "/error",
            "/actuator/**",
            "/system/v3/api-docs",
    };
}