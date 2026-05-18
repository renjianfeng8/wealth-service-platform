package com.wealth.platform.account.config;

import com.wealth.common.feign.PermissionCheckFeignClient;
import com.wealth.common.interceptor.LoginInterceptor;
import com.wealth.common.interceptor.PermissionCheckInterceptor;
import com.wealth.common.utils.JwtUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 账户模块 Web 配置 — 注册 JWT 登录拦截器 + 权限校验拦截器 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final JwtUtil jwtUtil;
    private final PermissionCheckFeignClient permissionCheckFeignClient;

    public WebConfig(JwtUtil jwtUtil, PermissionCheckFeignClient permissionCheckFeignClient) {
        this.jwtUtil = jwtUtil;
        this.permissionCheckFeignClient = permissionCheckFeignClient;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. JWT 认证
        registry.addInterceptor(new LoginInterceptor(jwtUtil))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/doc.html",
                        "/webjars/**",
                        "/swagger-resources/**",
                        "/v3/api-docs/**"
                );
        // 2. 权限校验（仅校验 POST/PUT/DELETE 写操作）
        registry.addInterceptor(new PermissionCheckInterceptor(permissionCheckFeignClient))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/doc.html",
                        "/webjars/**",
                        "/swagger-resources/**",
                        "/v3/api-docs/**"
                );
    }
}
