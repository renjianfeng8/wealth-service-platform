package com.wealth.common.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wealth.common.constants.AuthConstant;
import com.wealth.common.result.Result;
import com.wealth.common.utils.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private static final String TOKEN_COOKIE_NAME = "wealth_token";
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        log.info("进入拦截器 | 请求地址：{}", uri);

        // 使用 PathMatcher 匹配放行路径（支持 Ant 风格通配符）
        for (String permitUrl : AuthConstant.PERMIT_ALL_URLS) {
            if (PATH_MATCHER.match(permitUrl, uri)) {
                return true;
            }
        }

        // 从请求头或 httpOnly Cookie 获取 Token
        String token = extractToken(request);
        if (token == null) {
            log.warn("无Token，返回401");
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            objectMapper.writeValue(response.getWriter(), Result.error(401, "未登录"));
            return false;
        }

        if (!jwtUtil.validateToken(token)) {
            log.warn("Token无效，返回401");
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            objectMapper.writeValue(response.getWriter(), Result.error(401, "Token无效或已过期"));
            return false;
        }

        log.info("Token校验通过，放行！");
        return true;
    }

    private String extractToken(HttpServletRequest request) {
        // 优先从 Authorization header 获取
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        // 降级：从 httpOnly Cookie 获取（防 XSS 窃取）
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
