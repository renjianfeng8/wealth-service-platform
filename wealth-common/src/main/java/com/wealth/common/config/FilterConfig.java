package com.wealth.common.config;

import com.wealth.common.filter.XssFilter;
import jakarta.servlet.Filter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Servlet 过滤器注册配置。
 */
@Configuration
public class FilterConfig {

    /**
     * XSS 过滤器 — 对所有请求参数进行 HTML 转义。
     * order = Ordered.HIGHEST_PRECEDENCE + 1，位于 CharacterEncodingFilter 之后。
     */
    @Bean
    public FilterRegistrationBean<Filter> xssFilterRegistration() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new XssFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        registration.setName("xssFilter");
        return registration;
    }
}
