package com.wealth.platform.user.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.wealth.common.contract.AdminIdentityProvider;
import com.wealth.common.dto.AdminIdentityDTO;
import com.wealth.common.dto.LoginDTO;
import com.wealth.common.exception.ServiceException;
import com.wealth.common.utils.JwtUtil;
import com.wealth.common.utils.RedisUtil;
import com.wealth.platform.system.service.CaptchaService;
import com.wealth.platform.user.entity.User;
import com.wealth.platform.user.mapper.UserMapper;
import com.wealth.platform.user.vo.LoginVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdentifyLoginTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private AdminIdentityProvider adminIdentityProvider;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    @Mock
    private CaptchaService captchaService;
    @Mock
    private RedisUtil redisUtil;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(jwtUtil, passwordEncoder, adminIdentityProvider, captchaService, redisUtil);
        ReflectionTestUtils.setField(userService, "baseMapper", userMapper);
    }

    @Test
    @DisplayName("统一登录-管理员成功-返回双Token")
    void identifyLogin_AdminSuccess() {
        AdminIdentityDTO admin = new AdminIdentityDTO();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setPassword("encodedAdminPwd");
        admin.setStatus(1);
        admin.setNickname("管理员");

        when(adminIdentityProvider.findByUsername("admin")).thenReturn(admin);
        when(passwordEncoder.matches("admin123", "encodedAdminPwd")).thenReturn(true);
        when(jwtUtil.generateToken("admin", "admin")).thenReturn("admin.jwt.token");
        when(jwtUtil.generateRefreshToken("admin")).thenReturn("admin.refresh.token");
        when(jwtUtil.getTokenIdFromToken("admin.refresh.token")).thenReturn("admin-jti");
        when(jwtUtil.getAccessExpire()).thenReturn(1800000L);

        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("admin123");

        LoginVO result = userService.identifyLogin(dto);

        assertNotNull(result);
        assertEquals("admin", result.getUserType());
        assertEquals("admin.jwt.token", result.getToken());
        assertEquals("admin.refresh.token", result.getRefreshToken());
        assertEquals(1L, result.getUserId());
        assertEquals(1800, result.getExpiresInSeconds());
        verify(adminIdentityProvider).findByUsername("admin");
    }

    @Test
    @DisplayName("统一登录-普通用户成功-返回双Token")
    void identifyLogin_UserSuccess() {
        when(adminIdentityProvider.findByUsername("testuser")).thenReturn(null);

        User user = new User();
        user.setId(2L);
        user.setUsername("testuser");
        user.setPassword("encodedUserPwd");
        user.setStatus(1);
        user.setNickname("测试用户");

        LambdaQueryChainWrapper<User> userWrapper = mock(LambdaQueryChainWrapper.class);
        when(userWrapper.eq(any(), any())).thenReturn(userWrapper);
        when(userWrapper.one()).thenReturn(user);

        UserServiceImpl spy = spy(userService);
        doReturn(userWrapper).when(spy).lambdaQuery();

        when(passwordEncoder.matches("user123", "encodedUserPwd")).thenReturn(true);
        when(jwtUtil.generateToken("testuser", "user")).thenReturn("user.jwt.token");
        when(jwtUtil.generateRefreshToken("testuser")).thenReturn("user.refresh.token");
        when(jwtUtil.getTokenIdFromToken("user.refresh.token")).thenReturn("user-jti");
        when(jwtUtil.getAccessExpire()).thenReturn(1800000L);

        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser");
        dto.setPassword("user123");

        LoginVO result = spy.identifyLogin(dto);

        assertNotNull(result);
        assertEquals("user", result.getUserType());
        assertEquals("user.jwt.token", result.getToken());
        assertEquals("user.refresh.token", result.getRefreshToken());
        assertEquals(2L, result.getUserId());
        assertEquals(1800, result.getExpiresInSeconds());
    }

    @Test
    @DisplayName("统一登录-提供验证码时校验")
    void identifyLogin_should_verify_captcha_when_key_provided() {
        AdminIdentityDTO admin = new AdminIdentityDTO();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setPassword("encodedAdminPwd");
        admin.setStatus(1);

        when(adminIdentityProvider.findByUsername("admin")).thenReturn(admin);
        when(passwordEncoder.matches("admin123", "encodedAdminPwd")).thenReturn(true);
        when(jwtUtil.generateToken("admin", "admin")).thenReturn("admin.jwt.token");
        when(jwtUtil.generateRefreshToken("admin")).thenReturn("admin.refresh.token");
        when(jwtUtil.getTokenIdFromToken("admin.refresh.token")).thenReturn("admin-jti");
        when(jwtUtil.getAccessExpire()).thenReturn(1800000L);

        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("admin123");
        dto.setCaptchaKey("k-123");
        dto.setCaptchaCode("abcd");

        userService.identifyLogin(dto);

        verify(captchaService).verify("k-123", "abcd");
    }

    @Test
    @DisplayName("统一登录-验证码错误抛出异常")
    void identifyLogin_should_fail_when_captcha_wrong() {
        doThrow(new ServiceException(400, "验证码错误")).when(captchaService).verify("k-123", "wrong");

        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("admin123");
        dto.setCaptchaKey("k-123");
        dto.setCaptchaCode("wrong");

        assertThrows(ServiceException.class, () -> userService.identifyLogin(dto));
        verify(adminIdentityProvider, never()).findByUsername(anyString());
    }

    @Test
    @DisplayName("统一登录-未提供验证码时跳过校验")
    void identifyLogin_should_skip_captcha_when_key_blank() {
        when(adminIdentityProvider.findByUsername("nobody")).thenReturn(null);

        LambdaQueryChainWrapper<User> userWrapper = mock(LambdaQueryChainWrapper.class);
        when(userWrapper.eq(any(), any())).thenReturn(userWrapper);
        when(userWrapper.one()).thenReturn(null);

        UserServiceImpl spy = spy(userService);
        doReturn(userWrapper).when(spy).lambdaQuery();

        LoginDTO dto = new LoginDTO();
        dto.setUsername("nobody");
        dto.setPassword("pwd");

        assertThrows(ServiceException.class, () -> spy.identifyLogin(dto));
        verify(captchaService, never()).verify(anyString(), anyString());
    }

    @Test
    @DisplayName("统一登录-刷新jti持久化到Redis")
    void identifyLogin_should_persist_refresh_jti_to_redis() {
        AdminIdentityDTO admin = new AdminIdentityDTO();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setPassword("encodedAdminPwd");
        admin.setStatus(1);

        when(adminIdentityProvider.findByUsername("admin")).thenReturn(admin);
        when(passwordEncoder.matches("admin123", "encodedAdminPwd")).thenReturn(true);
        when(jwtUtil.generateToken("admin", "admin")).thenReturn("admin.jwt.token");
        when(jwtUtil.generateRefreshToken("admin")).thenReturn("admin.refresh.token");
        when(jwtUtil.getTokenIdFromToken("admin.refresh.token")).thenReturn("admin-jti");
        when(jwtUtil.getAccessExpire()).thenReturn(1800000L);

        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("admin123");

        userService.identifyLogin(dto);

        verify(redisUtil).safeExecuteVoid(any(Runnable.class), anyString());
    }

    @Test
    @DisplayName("统一登录-账号不存在抛异常")
    void identifyLogin_NotFound() {
        when(adminIdentityProvider.findByUsername("nobody")).thenReturn(null);

        LambdaQueryChainWrapper<User> userWrapper = mock(LambdaQueryChainWrapper.class);
        when(userWrapper.eq(any(), any())).thenReturn(userWrapper);
        when(userWrapper.one()).thenReturn(null);

        UserServiceImpl spy = spy(userService);
        doReturn(userWrapper).when(spy).lambdaQuery();

        LoginDTO dto = new LoginDTO();
        dto.setUsername("nobody");
        dto.setPassword("pwd");

        assertThrows(ServiceException.class, () -> spy.identifyLogin(dto));
    }
}
