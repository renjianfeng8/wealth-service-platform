package com.wealth.platform.system.interceptor;

import com.wealth.common.constants.AuthConstant;
import com.wealth.common.result.ResultCode;
import com.wealth.common.utils.HttpResponseUtil;
import com.wealth.common.utils.JwtUtil;
import com.wealth.platform.system.service.PermissionQueryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Slf4j
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final PermissionQueryService permissionQueryService;

    public PermissionInterceptor(JwtUtil jwtUtil, PermissionQueryService permissionQueryService) {
        this.jwtUtil = jwtUtil;
        this.permissionQueryService = permissionQueryService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        log.info("权限拦截器 | 请求地址: {}", uri);

        String token = AuthConstant.extractToken(request);
        if (token == null) {
            log.warn("请求未携带 Token");
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "未登录");
            return false;
        }

        if (!jwtUtil.validateToken(token)) {
            log.warn("Token 无效或已过期");
            writeError(response, ResultCode.TOKEN_INVALID.getCode(), ResultCode.TOKEN_INVALID.getMessage());
            return false;
        }

        if (!permissionQueryService.checkPermissionForToken(token, uri)) {
            log.warn("权限校验未通过, uri={}", uri);
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "无权限访问");
            return false;
        }

        log.info("权限校验通过, uri={}", uri);
        return true;
    }

    private void writeError(HttpServletResponse response, int code, String message) throws IOException {
        HttpResponseUtil.writeJson(response, code, code, message);
    }
}
