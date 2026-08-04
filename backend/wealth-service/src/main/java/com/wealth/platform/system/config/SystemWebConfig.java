package com.wealth.platform.system.config;

import com.wealth.common.constants.AuthConstant;
import com.wealth.common.interceptor.LoginInterceptor;
import com.wealth.common.utils.JwtUtil;
import com.wealth.platform.system.interceptor.PermissionInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 合并后的统一 Web 配置。
 *
 * <p>职责：
 * <ol>
 *   <li>JWT 认证拦截器 — 全局拦截所有请求，白名单路径在拦截器内部通过 AuthConstant.PERMIT_ALL_URLS 放行</li>
 *   <li>RBAC 权限校验拦截器 — 仅拦截 /system/** 后台管理路径</li>
 * </ol>
 *
 * <p>原 5 个业务模块的独立 WebConfig 已删除，统一由此类管理。
 */
@Configuration
public class SystemWebConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;
    private final PermissionInterceptor permissionInterceptor;

    public SystemWebConfig(JwtUtil jwtUtil, PermissionInterceptor permissionInterceptor) {
        this.loginInterceptor = new LoginInterceptor(jwtUtil);
        this.permissionInterceptor = permissionInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. JWT 认证 — 全局拦截，白名单路径在 LoginInterceptor 内部放行
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**");

        // 2. RBAC 权限校验 — 仅拦截 /system/** 后台管理路径
        //    放行清单收敛自 AuthConstant：公共白名单 + 需登录但免权限校验的后台路径
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/system/**")
                .excludePathPatterns(AuthConstant.PERMIT_ALL_URLS)
                .excludePathPatterns(AuthConstant.PERMISSION_BYPASS_URLS);
    }
}
