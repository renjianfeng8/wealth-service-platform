package com.wealth.common.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * XSS 过滤器 — 包装 HttpServletRequest 以转义请求参数中的 HTML 特殊字符。
 * <p>
 * 作用于所有请求路径，在 Spring 的 CharacterEncodingFilter 之后、DispatcherServlet 之前执行。
 */
@Slf4j
public class XssFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        String method = req.getMethod();

        // 对 GET/POST 等可能携带参数的请求进行 XSS 过滤
        if (!"OPTIONS".equalsIgnoreCase(method)) {
            log.debug("XSS Filter 处理请求 | method={} | uri={}", method, req.getRequestURI());
            chain.doFilter(new XssHttpServletRequestWrapper(req), response);
        } else {
            chain.doFilter(request, response);
        }
    }
}
