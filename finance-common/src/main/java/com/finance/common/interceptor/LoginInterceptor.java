package com.finance.common.interceptor;

import com.finance.common.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public LoginInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        System.out.println("========================================");
        System.out.println("📥 进入拦截器 | 请求地址：" + uri);

        // 放行登录相关接口
        if (uri.equals("/system/umsAdmin/login") || uri.equals("/user/login")) {
            return true;
        }

        // 校验Token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("❌ 无Token，返回401");
            response.setStatus(401);
            return false;
        }

        String token = authHeader.replace("Bearer ", "");
        if (!jwtUtil.validateToken(token)) {
            System.out.println("❌ Token无效，返回401");
            response.setStatus(401);
            return false;
        }

        System.out.println("✅ Token校验通过，放行！");
        return true;
    }
}