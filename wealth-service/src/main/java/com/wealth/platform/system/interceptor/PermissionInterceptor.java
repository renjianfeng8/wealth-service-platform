package com.wealth.platform.system.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wealth.common.result.Result;
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

import java.io.IOException;
import java.util.Collections;
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
    private final ObjectMapper objectMapper;

    public PermissionInterceptor(JwtUtil jwtUtil,
                                 UmsAdminService adminService,
                                 UmsAdminRoleRelationService adminRoleRelationService,
                                 UmsRoleResourceRelationService roleResourceRelationService,
                                 ObjectProvider<RedisUtil> redisUtilProvider,
                                 ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.adminService = adminService;
        this.adminRoleRelationService = adminRoleRelationService;
        this.roleResourceRelationService = roleResourceRelationService;
        this.redisUtil = redisUtilProvider.getIfAvailable();
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        log.info("权限拦截器 | 请求地址: {}", uri);

        String token = extractToken(request);
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

        List<String> allowedUrls = getAllowedUrls(admin);
        if (allowedUrls.isEmpty()) {
            log.warn("用户无可用资源权限，adminId={}", admin.getId());
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "无权限访问");
            return false;
        }

        boolean hasPermission = allowedUrls.stream().anyMatch(pattern -> pathMatcher.match(pattern, uri));
        if (!hasPermission) {
            log.warn("权限校验未通过，adminId={}, uri={}", admin.getId(), uri);
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "无权限访问");
            return false;
        }

        log.info("权限校验通过，adminId={}, uri={}", admin.getId(), uri);
        return true;
    }

    private List<String> getAllowedUrls(UmsAdmin admin) {
        String cacheKey = CACHE_KEY_PREFIX + admin.getId();
        List<String> cachedUrls = getCachedAllowedUrls(cacheKey, admin.getId());
        if (cachedUrls != null) {
            return cachedUrls;
        }

        List<Long> roleIds = adminRoleRelationService.lambdaQuery()
                .eq(UmsAdminRoleRelation::getAdminId, admin.getId())
                .list()
                .stream()
                .map(UmsAdminRoleRelation::getRoleId)
                .collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> resourceIds = roleResourceRelationService.lambdaQuery()
                .in(UmsRoleResourceRelation::getRoleId, roleIds)
                .list()
                .stream()
                .map(UmsRoleResourceRelation::getResourceId)
                .collect(Collectors.toList());
        if (resourceIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> allowedUrls = adminService.getResourceUrlsByIds(resourceIds);
        cacheAllowedUrls(cacheKey, allowedUrls);
        return allowedUrls != null ? allowedUrls : Collections.emptyList();
    }

    private List<String> getCachedAllowedUrls(String cacheKey, Long adminId) {
        if (redisUtil == null) {
            return null;
        }
        try {
            Object cached = redisUtil.get(cacheKey);
            if (cached instanceof List<?> list && list.stream().allMatch(String.class::isInstance)) {
                log.debug("权限缓存命中 | adminId={}", adminId);
                return list.stream().map(String.class::cast).collect(Collectors.toList());
            }
        } catch (DataAccessException e) {
            log.warn("Redis 不可用，降级到数据库查询 | error={}", e.getMessage());
        }
        return null;
    }

    private void cacheAllowedUrls(String cacheKey, List<String> allowedUrls) {
        if (redisUtil == null || allowedUrls == null) {
            return;
        }
        try {
            redisUtil.set(cacheKey, allowedUrls, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (DataAccessException e) {
            log.warn("Redis 不可用，无法写入权限缓存 | error={}", e.getMessage());
        }
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

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

    private void writeError(HttpServletResponse response, int code, String message) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(code);
        objectMapper.writeValue(response.getWriter(), Result.error(code, message));
    }
}
