package com.wealth.common.utils;

import com.wealth.common.constants.AuthConstant;
import org.springframework.http.ResponseCookie;

/**
 * HTTP Cookie 构建工具：统一登录 Token Cookie 的命名与属性（httpOnly / path=/）。
 */
public final class CookieUtil {

    private CookieUtil() {
    }

    /**
     * 构建登录 Token Cookie（httpOnly + path=/），名称统一取自 AuthConstant.TOKEN_COOKIE_NAME。
     *
     * @param token         登录 Token
     * @param maxAgeSeconds Cookie 有效期（秒），应与 Token 有效期一致
     */
    public static ResponseCookie buildTokenCookie(String token, long maxAgeSeconds) {
        return ResponseCookie.from(AuthConstant.TOKEN_COOKIE_NAME, token)
                .httpOnly(true)
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
    }
}
