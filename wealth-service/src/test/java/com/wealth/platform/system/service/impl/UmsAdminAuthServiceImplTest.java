package com.wealth.platform.system.service.impl;

import com.wealth.common.dto.LoginDTO;
import com.wealth.common.exception.ServiceException;
import com.wealth.common.utils.JwtUtil;
import com.wealth.common.utils.JwtUtil.TokenPair;
import com.wealth.common.utils.RedisUtil;
import com.wealth.platform.system.entity.UmsAdmin;
import com.wealth.platform.system.service.CaptchaService;
import com.wealth.platform.system.service.UmsAdminCrudService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UmsAdminAuthServiceImplTest {

    @Mock
    private UmsAdminCrudService crudService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private RedisUtil redisUtil;

    @Mock
    private CaptchaService captchaService;

    private UmsAdminAuthServiceImpl authService;

    private UmsAdmin mockAdmin;

    @BeforeEach
    void setUp() {
        authService = new UmsAdminAuthServiceImpl(crudService, jwtUtil, passwordEncoder, redisUtil, captchaService);

        mockAdmin = new UmsAdmin();
        mockAdmin.setId(1L);
        mockAdmin.setUsername("admin");
        mockAdmin.setPassword("encodedPassword");
        mockAdmin.setStatus(1);
    }

    @Test
    @DisplayName("管理员登录成功-返回双Token")
    void login_Success() {
        when(crudService.getActiveByUsername("admin")).thenReturn(mockAdmin);
        when(passwordEncoder.matches("rawPassword", "encodedPassword")).thenReturn(true);
        when(crudService.updateById(any(UmsAdmin.class))).thenReturn(true);

        TokenPair mockPair = new TokenPair("access.token", "refresh.token", 1800000);
        when(jwtUtil.generateTokenPair("admin")).thenReturn(mockPair);
        when(jwtUtil.getTokenIdFromToken("refresh.token")).thenReturn("test-jti");

        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("rawPassword");

        TokenPair result = authService.login(dto);

        assertNotNull(result);
        assertEquals("access.token", result.accessToken());
        assertEquals("refresh.token", result.refreshToken());
    }

    @Test
    @DisplayName("管理员登录失败-参数为空")
    void login_EmptyParams() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("");

        assertThrows(ServiceException.class, () -> authService.login(dto));
    }

    @Test
    @DisplayName("管理员登录失败-用户不存在")
    void login_UserNotFound() {
        when(crudService.getActiveByUsername("nonexist")).thenReturn(null);

        LoginDTO dto = new LoginDTO();
        dto.setUsername("nonexist");
        dto.setPassword("password");

        assertThrows(ServiceException.class, () -> authService.login(dto));
    }

    @Test
    @DisplayName("管理员登录失败-密码错误")
    void login_WrongPassword() {
        when(crudService.getActiveByUsername("admin")).thenReturn(mockAdmin);
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("wrongPassword");

        assertThrows(ServiceException.class, () -> authService.login(dto));
    }

    @Test
    @DisplayName("管理员登录失败-账号被禁用")
    void login_DisabledAccount() {
        when(crudService.getActiveByUsername("admin")).thenReturn(mockAdmin);
        mockAdmin.setStatus(0);

        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("rawPassword");

        ServiceException exception = assertThrows(ServiceException.class, () -> authService.login(dto));
        assertEquals(401, exception.getCode());
    }
}
