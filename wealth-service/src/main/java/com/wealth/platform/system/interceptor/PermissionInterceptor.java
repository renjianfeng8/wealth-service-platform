package com.wealth.platform.system.interceptor;

import com.wealth.common.constants.AuthConstant;
import com.wealth.common.utils.JwtUtil;
import com.wealth.common.utils.RedisUtil;
import com.wealth.platform.system.entity.UmsAdmin;
import com.wealth.platform.system.entity.UmsAdminRoleRelation;
import com.wealth.platform.system.entity.UmsRoleResourceRelation;
import com.wealth.platform.system.service.UmsAdminRoleRelationService;
import com.wealth.platform.system.service.UmsAdminService;
import com.wealth.platform.system.service.UmsRoleResourceRelationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final String CACHE_KEY_PREFIX = "permission:urls:";
    private static final long CACHE_TTL_MINUTES = 5;
    private static final String TOKEN_COOKIE_NAME = "wealth_token";

    private final JwtUtil jwtUtil;
    private final UmsAdminService adminService;
    private final UmsAdminRoleRelationService adminRoleRelationService;
    private final UmsRoleResourceRelationService roleResourceRelationService;
    private final RedisUtil redisUtil;

    public PermissionInterceptor(JwtUtil jwtUtil,
                                 UmsAdminService adminService,
                                 UmsAdminRoleRelationService adminRoleRelationService,
                                 UmsRoleResourceRelationService roleResourceRelationService,
                                 ObjectProvider<RedisUtil> redisUtilProvider) {
        this.jwtUtil = jwtUtil;
        this.adminService = adminService;
        this.adminRoleRelationService = adminRoleRelationService;
        this.roleResourceRelationService = roleResourceRelationService;
        this.redisUtil = redisUtilProvider.getIfAvailable();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        log.info("权限拦截器 | 请求地址：{}", uri);

        // 1. 从请求头或 httpOnly Cookie 获取 Token
        String token = extractToken(request);
        if (token == null) {
            log.warn("无Token，返回401");
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"未登录\"}");
            response.getWriter().flush();
            return false;
        }

        // 2. 校验Token
        if (!jwtUtil.validateToken(token)) {
            log.warn("Token无效，返回401");
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"Token无效或已过期\"}");
            response.getWriter().flush();
            return false;
        }

        // 3. 获取当前用户
        String username = jwtUtil.getUsernameFromToken(token);
        UmsAdmin admin = adminService.lambdaQuery()
                .eq(UmsAdmin::getUsername, username)
                .eq(UmsAdmin::getDelFlag, 0)
                .one();

        if (admin == null) {
            log.warn("用户不存在，返回401");
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"用户不存在\"}");
            response.getWriter().flush();
            return false;
        }

        // ======================= 权限校验开始（Redis 缓存加速） =======================
        // 4. 从缓存获取该用户的权限 URL 列表
        String cacheKey = CACHE_KEY_PREFIX + admin.getId();
        List<String> allowedUrls = null;

        if (redisUtil != null) {
            try {
                Object cached = redisUtil.get(cacheKey);
                if (cached instanceof List) {
                    allowedUrls = (List<String>) cached;
                    log.debug("权限缓存命中 | adminId={}", admin.getId());
                }
            } catch (DataAccessException e) {
                log.warn("Redis 不可用，降级到数据库查询 | error={}", e.getMessage());
            }
        }

        // 5. 缓存未命中，从数据库查询
        if (allowedUrls == null) {
            List<Long> roleIds = adminRoleRelationService.lambdaQuery()
                    .eq(UmsAdminRoleRelation::getAdminId, admin.getId())
                    .list()
                    .stream()
                    .map(UmsAdminRoleRelation::getRoleId)
                    .collect(Collectors.toList());

            if (roleIds.isEmpty()) {
                log.warn("用户无角色，返回403");
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(403);
                response.getWriter().write("{\"code\":403,\"message\":\"无权限访问\"}");
                response.getWriter().flush();
                return false;
            }

            List<Long> resourceIds = roleResourceRelationService.lambdaQuery()
                    .in(UmsRoleResourceRelation::getRoleId, roleIds)
                    .list()
                    .stream()
                    .map(UmsRoleResourceRelation::getResourceId)
                    .collect(Collectors.toList());

            if (resourceIds.isEmpty()) {
                log.warn("角色未分配资源，返回403");
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(403);
                response.getWriter().write("{\"code\":403,\"message\":\"无权限访问\"}");
                response.getWriter().flush();
                return false;
            }

            allowedUrls = adminService.getResourceUrlsByIds(resourceIds);
            log.info("用户拥有的权限：{}", allowedUrls);

            // 写入缓存（TTL 5 分钟）
            if (redisUtil != null) {
                try {
                    redisUtil.set(cacheKey, allowedUrls, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
                } catch (DataAccessException e) {
                    log.warn("Redis 不可用，无法写入缓存 | error={}", e.getMessage());
                }
            }
        }

        // 6. 使用 AntPathMatcher 进行 Ant 风格路径匹配
        boolean hasPermission = allowedUrls.stream().anyMatch(pattern -> pathMatcher.match(pattern, uri));
        if (!hasPermission) {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(403);
            response.getWriter().write("{\"code\":403,\"message\":\"无权限访问\"}");
            response.getWriter().flush();
            return false;
        }

        log.info("权限校验通过，放行！");
        return true;
    }

    private String extractToken(HttpServletRequest request) {
        // 优先从 Authorization header 获取
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        // 降级：从 httpOnly Cookie 获取
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
