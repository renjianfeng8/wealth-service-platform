package com.finance.platform.system.interceptor;

import com.finance.common.utils.JwtUtil;
import com.finance.platform.system.entity.UmsAdmin;
import com.finance.platform.system.entity.UmsAdminRoleRelation;
import com.finance.platform.system.entity.UmsRoleResourceRelation;
import com.finance.platform.system.service.UmsAdminRoleRelationService;
import com.finance.platform.system.service.UmsAdminService;
import com.finance.platform.system.service.UmsRoleResourceRelationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PermissionInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final UmsAdminService adminService;
    private final UmsAdminRoleRelationService adminRoleRelationService;
    private final UmsRoleResourceRelationService roleResourceRelationService;

    // 这里注入需要的Service，从数据库查关联关系
    public PermissionInterceptor(JwtUtil jwtUtil,
                                 UmsAdminService adminService,
                                 UmsAdminRoleRelationService adminRoleRelationService,
                                 UmsRoleResourceRelationService roleResourceRelationService) {
        this.jwtUtil = jwtUtil;
        this.adminService = adminService;
        this.adminRoleRelationService = adminRoleRelationService;
        this.roleResourceRelationService = roleResourceRelationService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        System.out.println("========================================");
        System.out.println("🔐 权限拦截器 | 请求地址：" + uri);

        // 1. 获取Token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("❌ 无Token，返回401");
            response.setStatus(401);
            return false;
        }

        String token = authHeader.replace("Bearer ", "");

        // 2. 校验Token
        if (!jwtUtil.validateToken(token)) {
            System.out.println("❌ Token无效，返回401");
            response.setStatus(401);
            return false;
        }

        // 3. 获取当前用户
        String username = jwtUtil.getUsernameFromToken(token);
        UmsAdmin admin = adminService.lambdaQuery()
                .eq(UmsAdmin::getUsername, username)
                .eq(UmsAdmin::getDelFlag, 0)
                .one();

        if (admin == null) {
            response.setStatus(401);
            return false;
        }

        // ======================= 权限校验开始 =======================
        // 4. 从数据库查询用户的角色ID
        List<Long> roleIds = adminRoleRelationService.lambdaQuery()
                .eq(UmsAdminRoleRelation::getAdminId, admin.getId())
                .list()
                .stream()
                .map(UmsAdminRoleRelation::getRoleId)
                .collect(Collectors.toList());

        // 5. 从数据库查询角色对应的资源ID
        List<Long> resourceIds = roleResourceRelationService.lambdaQuery()
                .in(UmsRoleResourceRelation::getRoleId, roleIds)
                .list()
                .stream()
                .map(UmsRoleResourceRelation::getResourceId)
                .collect(Collectors.toList());

        // 6. 获取允许访问的URL
        List<String> allowedUrls = adminService.getResourceUrlsByIds(resourceIds);
        System.out.println("用户拥有的权限：" + allowedUrls);

        // 7. 判断是否有权限
        if (!allowedUrls.contains(uri)) {
            System.out.println("❌ 无权限访问！返回403");
            response.setStatus(403);
            return false;
        }

        System.out.println("✅ 权限校验通过，放行！");
        return true;
    }
}