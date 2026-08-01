package com.wealth.platform.system.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.wealth.common.dto.LoginDTO;
import com.wealth.common.exception.ServiceException;
import com.wealth.common.utils.JwtUtil;
import com.wealth.common.utils.JwtUtil.TokenPair;
import com.wealth.common.utils.RedisUtil;
import com.wealth.platform.system.dto.UmsAdminDTO;
import com.wealth.platform.system.entity.UmsAdmin;
import com.wealth.platform.system.mapper.UmsAdminMapper;
import com.wealth.platform.system.service.PermissionCacheService;
import com.wealth.platform.system.service.UmsResourceService;
import com.wealth.platform.system.vo.UmsAdminVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UmsAdminServiceImplTest {

    @Mock
    private UmsAdminMapper umsAdminMapper;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UmsResourceService resourceService;

    @Mock
    private PermissionCacheService permissionCacheService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private RedisUtil redisUtil;

    private UmsAdminServiceImpl adminService;

    private UmsAdmin mockAdmin;

    @BeforeEach
    void setUp() {
        adminService = mock(UmsAdminServiceImpl.class, CALLS_REAL_METHODS);
        ReflectionTestUtils.setField(adminService, "jwtUtil", jwtUtil);
        ReflectionTestUtils.setField(adminService, "resourceService", resourceService);
        ReflectionTestUtils.setField(adminService, "permissionCacheService", permissionCacheService);
        ReflectionTestUtils.setField(adminService, "passwordEncoder", passwordEncoder);
        ReflectionTestUtils.setField(adminService, "baseMapper", umsAdminMapper);
        ReflectionTestUtils.setField(adminService, "redisUtil", redisUtil);

        mockAdmin = new UmsAdmin();
        mockAdmin.setId(1L);
        mockAdmin.setUsername("admin");
        mockAdmin.setPassword("encodedPassword");
        mockAdmin.setStatus(1);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private LambdaQueryChainWrapper<UmsAdmin> setupLoginMocks() {
        LambdaQueryChainWrapper<UmsAdmin> qc = mock(LambdaQueryChainWrapper.class);
        when(qc.eq(any(), any())).thenReturn(qc);
        when(qc.one()).thenReturn(null);
        doReturn(qc).when(adminService).lambdaQuery();
        return qc;
    }

    @Test
    @DisplayName("管理员登录成功-返回双Token")
    void login_Success() {
        LambdaQueryChainWrapper<UmsAdmin> qc = setupLoginMocks();
        when(qc.one()).thenReturn(mockAdmin);

        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("rawPassword");

        TokenPair mockPair = new TokenPair("access.token", "refresh.token", 1800000);
        when(passwordEncoder.matches("rawPassword", "encodedPassword")).thenReturn(true);
        when(umsAdminMapper.updateById(any(UmsAdmin.class))).thenReturn(1);
        when(jwtUtil.generateTokenPair("admin")).thenReturn(mockPair);
        when(jwtUtil.getTokenIdFromToken("refresh.token")).thenReturn("test-jti");

        TokenPair result = adminService.login(dto);

        assertNotNull(result);
        assertEquals("access.token", result.accessToken());
        assertEquals("refresh.token", result.refreshToken());
    }

    @Test
    @DisplayName("管理员登录失败-参数为空")
    void login_EmptyParams() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("");

        assertThrows(ServiceException.class, () -> adminService.login(dto));
    }

    @Test
    @DisplayName("管理员登录失败-用户不存在")
    void login_UserNotFound() {
        LambdaQueryChainWrapper<UmsAdmin> qc = setupLoginMocks();
        when(qc.one()).thenReturn(null);

        LoginDTO dto = new LoginDTO();
        dto.setUsername("nonexist");
        dto.setPassword("password");

        assertThrows(ServiceException.class, () -> adminService.login(dto));
    }

    @Test
    @DisplayName("管理员登录失败-密码错误")
    void login_WrongPassword() {
        LambdaQueryChainWrapper<UmsAdmin> qc = setupLoginMocks();
        when(qc.one()).thenReturn(mockAdmin);

        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("wrongPassword");

        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThrows(ServiceException.class, () -> adminService.login(dto));
    }

    @Test
    @DisplayName("管理员登录失败-账号被禁用")
    void login_DisabledAccount() {
        LambdaQueryChainWrapper<UmsAdmin> qc = setupLoginMocks();
        when(qc.one()).thenReturn(mockAdmin);

        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("rawPassword");

        mockAdmin.setStatus(0);

        ServiceException exception = assertThrows(ServiceException.class, () -> adminService.login(dto));
        assertEquals(401, exception.getCode());
    }

    @Test
    @DisplayName("checkPermissionForToken-有效token且有权限")
    void checkPermissionForToken_ShouldReturnTrue() {
        when(jwtUtil.getUsernameFromToken("valid.token")).thenReturn("admin");
        LambdaQueryChainWrapper<UmsAdmin> qc = setupLoginMocks();
        when(qc.one()).thenReturn(mockAdmin);
        when(permissionCacheService.hasPermission(1L, "/system/umsRole/page")).thenReturn(true);

        boolean result = adminService.checkPermissionForToken("valid.token", "/system/umsRole/page");

        assertTrue(result);
    }

    @Test
    @DisplayName("checkPermissionForToken-管理员已删除返回false")
    void checkPermissionForToken_ShouldReturnFalseWhenAdminDeleted() {
        when(jwtUtil.getUsernameFromToken("deleted.token")).thenReturn("deletedadmin");
        LambdaQueryChainWrapper<UmsAdmin> qc = setupLoginMocks();
        when(qc.one()).thenReturn(null);

        boolean result = adminService.checkPermissionForToken("deleted.token", "/system/umsRole/page");

        assertFalse(result);
    }

    @Test
    @DisplayName("创建管理员成功")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void createAdmin_Success() {
        UmsAdminDTO dto = new UmsAdminDTO();
        dto.setUsername("newadmin");
        dto.setPassword("rawPassword");

        when(umsAdminMapper.selectCount(any())).thenReturn(0L);

        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        doReturn(1).when(umsAdminMapper).insert(any(UmsAdmin.class));

        Boolean result = adminService.createAdmin(dto);

        assertTrue(result);
        ArgumentCaptor<UmsAdmin> captor = ArgumentCaptor.forClass(UmsAdmin.class);
        verify(umsAdminMapper).insert(captor.capture());
        assertEquals("newadmin", captor.getValue().getUsername());
        assertEquals("encodedPassword", captor.getValue().getPassword());
    }

    @Test
    @DisplayName("根据资源ID获取URL列表-有数据")
    void getResourceUrlsByIds_WithData() {
        when(resourceService.getUrlByResourceIds(Arrays.asList(1L, 2L)))
                .thenReturn(Arrays.asList("/api/v1/user/**", "/api/v1/product/**"));

        List<String> urls = adminService.getResourceUrlsByIds(Arrays.asList(1L, 2L));

        assertEquals(2, urls.size());
        assertEquals("/api/v1/user/**", urls.get(0));
        assertEquals("/api/v1/product/**", urls.get(1));
    }

    @Test
    @DisplayName("根据空资源ID列表获取URL-返回空列表")
    void getResourceUrlsByIds_EmptyIds() {
        when(resourceService.getUrlByResourceIds(List.of())).thenReturn(List.of());

        List<String> urls = adminService.getResourceUrlsByIds(List.of());

        assertNotNull(urls);
        assertTrue(urls.isEmpty());
    }

    @Test
    @DisplayName("按用户名查询未删除管理员")
    void getActiveByUsername_ShouldReturnAdmin() {
        LambdaQueryChainWrapper<UmsAdmin> qc = setupLoginMocks();
        when(qc.one()).thenReturn(mockAdmin);

        UmsAdmin admin = adminService.getActiveByUsername("admin");

        assertNotNull(admin);
        assertEquals("admin", admin.getUsername());
    }

    @Test
    @DisplayName("根据ID查询管理员-成功")
    void getAdminById_Found() {
        when(umsAdminMapper.selectById(1L)).thenReturn(mockAdmin);

        UmsAdminVO result = adminService.getAdminById(1L);

        assertNotNull(result);
        assertEquals("admin", result.getUsername());
        assertEquals(1, result.getStatus());
    }

    @Test
    @DisplayName("根据ID查询管理员-不存在抛404")
    void getAdminById_NotFound() {
        when(umsAdminMapper.selectById(99L)).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class, () ->
                adminService.getAdminById(99L));

        assertEquals(404, exception.getCode());
    }

    @Test
    @DisplayName("更新管理员成功-密码被清空")
    void updateAdmin_Success() {
        UmsAdminDTO dto = new UmsAdminDTO();
        dto.setNickName("更新后的昵称");
        dto.setPassword("newPassword");

        when(umsAdminMapper.selectById(1L)).thenReturn(mockAdmin);
        when(umsAdminMapper.updateById(any(UmsAdmin.class))).thenReturn(1);

        boolean result = adminService.updateAdmin(1L, dto);

        assertTrue(result);
        verify(umsAdminMapper).updateById(argThat((UmsAdmin admin) ->
                admin.getId() == 1L &&
                "更新后的昵称".equals(admin.getNickName()) &&
                admin.getPassword() == null
        ));
    }

    @Test
    @DisplayName("更新管理员-不存在抛404")
    void updateAdmin_NotFound() {
        when(umsAdminMapper.selectById(99L)).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class, () ->
                adminService.updateAdmin(99L, new UmsAdminDTO()));

        assertEquals(404, exception.getCode());
        verify(umsAdminMapper, never()).updateById(isA(UmsAdmin.class));
    }

    @Test
    @DisplayName("删除管理员成功")
    void deleteAdmin_Success() {
        when(umsAdminMapper.selectById(1L)).thenReturn(mockAdmin);
        when(umsAdminMapper.deleteById(1L)).thenReturn(1);

        boolean result = adminService.deleteAdmin(1L);

        assertTrue(result);
        verify(umsAdminMapper).deleteById(1L);
    }

    @Test
    @DisplayName("删除管理员-不存在抛404")
    void deleteAdmin_NotFound() {
        when(umsAdminMapper.selectById(99L)).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class, () ->
                adminService.deleteAdmin(99L));

        assertEquals(404, exception.getCode());
        verify(umsAdminMapper, never()).deleteById(isA(UmsAdmin.class));
    }
}
