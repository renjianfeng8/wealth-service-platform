package com.wealth.platform.system.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wealth.common.constants.AuthConstant;
import com.wealth.common.result.Result;
import com.wealth.common.utils.JwtUtil;
import com.wealth.platform.system.entity.UmsAdmin;
import com.wealth.platform.system.service.PermissionCacheService;
import com.wealth.platform.system.service.UmsAdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final JwtUtil jwtUtil;
    private final UmsAdminService adminService;
    private final PermissionCacheService permissionCacheService;
    private final ObjectMapper objectMapper;

    public PermissionInterceptor(JwtUtil jwtUtil,
                                 UmsAdminService adminService,
                                 PermissionCacheService permissionCacheService,
                                 ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.adminService = adminService;
        this.permissionCacheService = permissionCacheService;
        this.objectMapper = objectMapper;
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
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token 无效或已过期");
            return false;
        }

        String username = jwtUtil.getUsernameFromToken(token);
        UmsAdmin admin = adminService.lambdaQuery()
                .eq(UmsAdmin::getUsername, username)
                .eq(UmsAdmin::getDelFlag, 0)
                .one();

        if (admin == null) {
            log.warn("用户不存在，username={}", username);
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "用户不存在");
            return false;
        }

        List<String> allowedUrls = permissionCacheService.getAllowedUrls(admin.getId());
        if (allowedUrls.isEmpty()) {
            log.warn("用户无可用资源权限，adminId={}", admin.getId());
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "无权限访问");
            return false;
        }

        boolean hasPermission = allowedUrls.stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, uri));
        if (!hasPermission) {
            log.warn("权限校验未通过，adminId={}, uri={}", admin.getId(), uri);
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "无权限访问");
            return false;
        }

        log.info("权限校验通过，adminId={}, uri={}", admin.getId(), uri);
        return true;
    }

    private void writeError(HttpServletResponse response, int code, String message) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(code);
        objectMapper.writeValue(response.getWriter(), Result.error(code, message));
    }
}
