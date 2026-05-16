package com.wealth.platform.system.config;

import com.wealth.common.constants.AuthConstant;
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
                // 娉ㄦ剰锛歛ddPathPatterns 鍖归厤鐨勬槸 context-path 鍓ョ鍚庣殑璺緞锛屼笉鑳藉姞 /system 鍓嶇紑
                .addPathPatterns("/**")
                // excludePathPatterns 涔熸槸 context-path 鍓ョ鍚庣殑璺緞
                .excludePathPatterns(
                        "/umsAdmin/login",
                        "/doc.html",
                        "/webjars/**",
                        "/swagger-resources/**",
                        "/v3/api-docs/**"
                );
    }
}