package com.wealth.platform.system.service.impl;

import com.wealth.common.utils.JwtUtil;
import com.wealth.platform.system.entity.UmsAdmin;
import com.wealth.platform.system.service.PermissionCacheService;
import com.wealth.platform.system.service.UmsAdminCrudService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionQueryServiceImplTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UmsAdminCrudService crudService;

    @Mock
    private PermissionCacheService permissionCacheService;

    private PermissionQueryServiceImpl permissionQueryService;

    private UmsAdmin mockAdmin;

    @BeforeEach
    void setUp() {
        permissionQueryService = new PermissionQueryServiceImpl(jwtUtil, crudService, permissionCacheService);

        mockAdmin = new UmsAdmin();
        mockAdmin.setId(1L);
        mockAdmin.setUsername("admin");
        mockAdmin.setStatus(1);
    }

    @Test
    @DisplayName("checkPermissionForToken-有效token且有权限")
    void checkPermissionForToken_ShouldReturnTrue() {
        when(jwtUtil.getUsernameFromToken("valid.token")).thenReturn("admin");
        when(crudService.getActiveByUsername("admin")).thenReturn(mockAdmin);
        when(permissionCacheService.hasPermission(1L, "/system/umsRole/page")).thenReturn(true);

        boolean result = permissionQueryService.checkPermissionForToken("valid.token", "/system/umsRole/page");

        assertTrue(result);
    }

    @Test
    @DisplayName("checkPermissionForToken-管理员已删除返回false")
    void checkPermissionForToken_ShouldReturnFalseWhenAdminDeleted() {
        when(jwtUtil.getUsernameFromToken("deleted.token")).thenReturn("deletedadmin");
        when(crudService.getActiveByUsername("deletedadmin")).thenReturn(null);

        boolean result = permissionQueryService.checkPermissionForToken("deleted.token", "/system/umsRole/page");

        assertFalse(result);
    }

    @Test
    @DisplayName("checkPermission-有效请求头且有权限")
    void checkPermission_ShouldReturnTrueWithValidToken() {
        when(jwtUtil.validateToken("valid.token")).thenReturn(true);
        when(jwtUtil.getUsernameFromToken("valid.token")).thenReturn("admin");
        when(crudService.getActiveByUsername("admin")).thenReturn(mockAdmin);
        when(permissionCacheService.hasPermission(1L, "/system/umsRole/page")).thenReturn(true);

        boolean result = permissionQueryService.checkPermission("/system/umsRole/page", "Bearer valid.token");

        assertTrue(result);
    }

    @Test
    @DisplayName("checkPermission-缺少token返回false")
    void checkPermission_ShouldReturnFalseWhenTokenMissing() {
        boolean result = permissionQueryService.checkPermission("/system/umsRole/page", null);

        assertFalse(result);
    }
}
