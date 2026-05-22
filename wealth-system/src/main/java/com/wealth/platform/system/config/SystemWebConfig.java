package com.wealth.platform.system.config;

import com.wealth.platform.system.interceptor.PermissionInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SystemWebConfig implements WebMvcConfigurer {

    private final PermissionInterceptor permissionInterceptor;

    public SystemWebConfig(PermissionInterceptor permissionInterceptor) {
        this.permissionInterceptor = permissionInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(permissionInterceptor)
                // 注意：addPathPatterns 匹配的是 context-path 剥离后的路径，不能加 /system 前缀
                .addPathPatterns("/**")
                // excludePathPatterns 也是 context-path 剥离后的路径
                .excludePathPatterns(
                        "/umsAdmin/login",
                        "/umsAdmin/refresh",
                        "/umsAdmin/checkPermission",
                        "/captcha",
                        "/error",
                        "/actuator/**"
                );
    }
}