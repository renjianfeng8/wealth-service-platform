package com.wealth.common.constants;

public class AuthConstant {

    // 无需权限直接放行的接口
    // 注意：Swagger/Knife4j 路径不在此白名单中，需登录后访问；生产环境 application-prod.yml 已禁用
    public static final String[] PERMIT_ALL_URLS = {
            "/system/umsAdmin/login",
            "/system/captcha",
            "/user/login",
            "/user/register",
            "/product/wea-market-data/sse/**",
            "/actuator/**",
    };
}