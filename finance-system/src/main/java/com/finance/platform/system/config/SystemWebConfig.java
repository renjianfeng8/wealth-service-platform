package com.finance.platform.system.config;

import com.finance.common.constants.AuthConstant;
import com.finance.platform.system.interceptor.PermissionInterceptor;
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
                .addPathPatterns("/system/**")
                .excludePathPatterns(AuthConstant.PERMIT_ALL_URLS);
    }
}