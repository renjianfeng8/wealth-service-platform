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
        System.out.println("请求地址：" + uri);

        // 1. 获取请求头
        String authHeader = request.getHeader("Authorization");
        System.out.println("请求头 Authorization：" + authHeader);

        // 2. 判断是否为空
        if (authHeader == null) {
            System.out.println("❌ 拦截原因：没有携带 Token");
            response.setStatus(401);
            return false;
        }

        // 3. 判断是否以 Bearer 开头
        if (!authHeader.startsWith("Bearer ")) {
            System.out.println("❌ 拦截原因：Token 格式错误，必须以 Bearer 开头");
            response.setStatus(401);
            return false;
        }

        // 4. 提取 Token
        String token = authHeader.replace("Bearer ", "");
        System.out.println("提取后的 Token：" + token);

        // 5. 校验 Token
        boolean isValid = jwtUtil.validateToken(token);
        if (!isValid) {
            System.out.println("❌ 拦截原因：Token 无效（密钥错误/过期/算法不匹配）");
            response.setStatus(401);
            return false;
        }

        // 6. 全部验证通过
        System.out.println("✅ 验证通过，放行请求");
        System.out.println("========================================");
        return true;
    }
}