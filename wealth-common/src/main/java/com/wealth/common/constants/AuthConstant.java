package com.wealth.common.constants;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

public class AuthConstant {

    // 无需权限直接放行的接口
    // 开发环境 Swagger/Knife4j 路径加入白名单
    public static final String TOKEN_COOKIE_NAME = "wealth_token";

    /**
     * 从请求中提取 Token：优先从 Authorization: Bearer 头获取，降级到 httpOnly Cookie。
     */
    public static String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
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
            "/system/umsAdmin/login",
            "/system/umsAdmin/refresh",
            "/system/captcha",
            "/user/login",
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
}