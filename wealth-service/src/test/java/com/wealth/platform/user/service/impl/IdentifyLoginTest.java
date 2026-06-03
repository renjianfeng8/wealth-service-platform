package com.wealth.platform.user.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.wealth.common.contract.AdminIdentityProvider;
import com.wealth.common.dto.AdminIdentityDTO;
import com.wealth.common.dto.LoginDTO;
import com.wealth.common.exception.ServiceException;
import com.wealth.common.utils.JwtUtil;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(jwtUtil, passwordEncoder, adminIdentityProvider);
        ReflectionTestUtils.setField(userService, "baseMapper", userMapper);
    }

    @Test
    @DisplayName("统一登录-管理员成功")
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

        LoginDTO dto = new LoginDTO();
        dto.setUsername("admin");
        dto.setPassword("admin123");

        LoginVO result = userService.identifyLogin(dto);

        assertNotNull(result);
        assertEquals("admin", result.getUserType());
        assertEquals("admin.jwt.token", result.getToken());
        assertEquals(1L, result.getUserId());
        verify(adminIdentityProvider).findByUsername("admin");
    }

    @Test
    @DisplayName("统一登录-普通用户成功")
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

        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser");
        dto.setPassword("user123");

        LoginVO result = spy.identifyLogin(dto);

        assertNotNull(result);
        assertEquals("user", result.getUserType());
        assertEquals("user.jwt.token", result.getToken());
        assertEquals(2L, result.getUserId());
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
