package com.wealth.common.constants;

public class AuthConstant {

    // 无需权限直接放行的接口
    // 开发环境 Swagger/Knife4j 路径加入白名单
    public static final String[] PERMIT_ALL_URLS = {
            "/system/umsAdmin/login",
            "/system/captcha",
            "/user/login",
            "/user/register",
            "/user/identify-login",
            "/product/wea-product/page",
            "/product/wea-market-data",
            "/message/wea-news/page",
            "/product/wea-market-data/sse/**",
            "/actuator/**",
            /* Swagger / Knife4j */
            "/doc.html",
            "/webjars/**",
            "/v3/api-docs/**",
            "/*/v3/api-docs",
    };
}