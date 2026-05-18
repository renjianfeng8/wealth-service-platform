package com.wealth.common.interceptor;

import com.wealth.common.feign.PermissionCheckFeignClient;
import com.wealth.common.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/**
 * 权限校验拦截器 — 对 POST/PUT/DELETE 等写操作调用 system 模块校验资源访问权限。
 * 需在 LoginInterceptor（JWT 认证）之后注册。
 */
@Slf4j
public class PermissionCheckInterceptor implements HandlerInterceptor {

    /** 需要校验权限的 HTTP 方法（写操作） */
    private static final Set<String> CHECKED_METHODS = Set.of("POST", "PUT", "DELETE", "PATCH");

    private final PermissionCheckFeignClient permissionCheckFeignClient;

    public PermissionCheckInterceptor(PermissionCheckFeignClient permissionCheckFeignClient) {
        this.permissionCheckFeignClient = permissionCheckFeignClient;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        // 仅拦截写操作，GET/HEAD/OPTIONS 放行
        if (!CHECKED_METHODS.contains(request.getMethod().toUpperCase())) {
            return true;
        }

        String uri = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("权限校验失败：无Token | uri={}", uri);
            response.setStatus(403);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"message\":\"无权限访问\"}");
            return false;
        }

        try {
            Result<Boolean> result = permissionCheckFeignClient.checkPermission(uri, authHeader);
            if (result == null || !Boolean.TRUE.equals(result.getData())) {
                log.warn("权限校验未通过 | uri={}", uri);
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":403,\"message\":\"无权限访问\"}");
                return false;
            }
            log.debug("权限校验通过 | uri={}", uri);
            return true;
        } catch (Exception e) {
            // Feign 调用异常（熔断/超时），安全起见默认拒绝（fail closed）
            log.error("权限校验调用失败，拒绝访问 | uri={} | error={}", uri, e.getMessage());
            response.setStatus(503);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":503,\"message\":\"权限服务暂不可用\"}");
            return false;
        }
    }
}
