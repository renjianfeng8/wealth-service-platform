package com.finance.common.constants;

public class AuthConstant {

    // 无需权限直接放行的接口
    public static final String[] PERMIT_ALL_URLS = {
            "/system/umsAdmin/login",
            "/doc.html",
            "/webjars/**",
            "/swagger-resources/**",
            "/v3/api-docs/**"
    };
}