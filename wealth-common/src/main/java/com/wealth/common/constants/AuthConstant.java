package com.wealth.common.constants;

public class AuthConstant {

    // 无需权限直接放行的接口
    public static final String[] PERMIT_ALL_URLS = {
            "/system/umsAdmin/login",
            "/system/captcha",
            "/user/login",
            "/user/register",
            "/product/WeaMarketData/sse/**",
            "/doc.html",
            "/webjars/**",
            "/swagger-resources/**",
            "/v3/api-docs/**"
    };
}