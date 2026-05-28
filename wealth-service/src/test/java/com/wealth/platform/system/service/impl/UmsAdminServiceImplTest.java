package com.wealth.platform.system.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.wealth.common.dto.LoginDTO;
import com.wealth.common.exception.ServiceException;
import com.wealth.common.utils.JwtUtil;
import com.wealth.common.utils.JwtUtil.TokenPair;
import com.wealth.common.utils.RedisUtil;
import com.wealth.platform.system.entity.UmsAdmin;
import com.wealth.platform.system.entity.UmsResource;
import com.wealth.platform.system.mapper.UmsAdminMapper;
import com.wealth.platform.system.service.UmsResourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UmsAdminServiceImplTest {

    @Mock
    private UmsAdminMapper umsAdminMapper;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UmsResourceService resourceService;

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
    @DisplayName("创建管理员成功")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void createAdmin_Success() {
        UmsAdmin admin = new UmsAdmin();
        admin.setUsername("newadmin");
        admin.setPassword("rawPassword");

        LambdaQueryChainWrapper<UmsAdmin> qc = mock(LambdaQueryChainWrapper.class);
        when(qc.eq(any(), any())).thenReturn(qc);
        when(qc.count()).thenReturn(0L);
        doReturn(qc).when(adminService).lambdaQuery();

        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        doReturn(1).when(umsAdminMapper).insert(any(UmsAdmin.class));

        Boolean result = adminService.createAdmin(admin);

        assertTrue(result);
        assertEquals("encodedPassword", admin.getPassword());
        verify(umsAdminMapper).insert(any(UmsAdmin.class));
    }

    @Test
    @DisplayName("根据资源ID获取URL列表-有数据")
    void getResourceUrlsByIds_WithData() {
        UmsResource res1 = new UmsResource();
        res1.setId(1L);
        res1.setUrl("/api/v1/user/**");

        UmsResource res2 = new UmsResource();
        res2.setId(2L);
        res2.setUrl("/api/v1/product/**");

        @SuppressWarnings("unchecked")
        LambdaQueryChainWrapper<UmsResource> resourceChain = mock(LambdaQueryChainWrapper.class);
        when(resourceService.lambdaQuery()).thenReturn(resourceChain);
        when(resourceChain.in(any(), anyCollection())).thenReturn(resourceChain);
        when(resourceChain.list()).thenReturn(Arrays.asList(res1, res2));

        List<String> urls = adminService.getResourceUrlsByIds(Arrays.asList(1L, 2L));

        assertEquals(2, urls.size());
        assertEquals("/api/v1/user/**", urls.get(0));
        assertEquals("/api/v1/product/**", urls.get(1));
    }

    @Test
    @DisplayName("根据空资源ID列表获取URL-返回空列表")
    void getResourceUrlsByIds_EmptyIds() {
        @SuppressWarnings("unchecked")
        LambdaQueryChainWrapper<UmsResource> resourceChain = mock(LambdaQueryChainWrapper.class);
        when(resourceService.lambdaQuery()).thenReturn(resourceChain);
        when(resourceChain.in(any(), anyCollection())).thenReturn(resourceChain);
        when(resourceChain.list()).thenReturn(List.of());

        List<String> urls = adminService.getResourceUrlsByIds(List.of());

        assertNotNull(urls);
        assertTrue(urls.isEmpty());
    }
}
