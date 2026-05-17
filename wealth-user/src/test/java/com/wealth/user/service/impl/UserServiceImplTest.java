package com.wealth.user.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.wealth.common.dto.LoginDTO;
import com.wealth.common.exception.ServiceException;
import com.wealth.common.utils.JwtUtil;
import com.wealth.user.entity.User;
import com.wealth.user.mapper.UserMapper;
import com.wealth.user.vo.LoginVO;
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
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    private UserServiceImpl userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        userService = mock(UserServiceImpl.class, CALLS_REAL_METHODS);
        ReflectionTestUtils.setField(userService, "jwtUtil", jwtUtil);
        ReflectionTestUtils.setField(userService, "passwordEncoder", passwordEncoder);
        ReflectionTestUtils.setField(userService, "baseMapper", userMapper);

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setPassword("encodedPassword");
        mockUser.setNickname("测试用户");
        mockUser.setStatus(1);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private LambdaQueryChainWrapper<User> setupQueryChain() {
        LambdaQueryChainWrapper<User> qc = mock(LambdaQueryChainWrapper.class);
        when(qc.eq(any(), any())).thenReturn(qc);
        when(qc.one()).thenReturn(null);
        doReturn(qc).when(userService).lambdaQuery();
        return qc;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private LambdaUpdateChainWrapper<User> setupUpdateChain() {
        LambdaUpdateChainWrapper<User> uc = mock(LambdaUpdateChainWrapper.class);
        when(uc.eq(any(), any())).thenReturn(uc);
        when(uc.set(any(), any())).thenReturn(uc);
        when(uc.update()).thenReturn(true);
        doReturn(uc).when(userService).lambdaUpdate();
        return uc;
    }

    @Test
    @DisplayName("注册成功")
    void register_Success() {
        User user = new User();
        user.setUsername("newuser");
        user.setPassword("rawPassword");

        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        doReturn(1).when(userMapper).insert(any(User.class));

        Boolean result = userService.register(user);

        assertTrue(result);
        assertEquals("encodedPassword", user.getPassword());
        verify(userMapper).insert(any(User.class));
    }

    @Test
    @DisplayName("注册失败-用户名为空")
    void register_EmptyUsername() {
        User user = new User();
        user.setUsername("");
        user.setPassword("password");

        assertThrows(ServiceException.class, () -> userService.register(user));
        verify(userMapper, never()).insert(isA(User.class));
    }

    @Test
    @DisplayName("注册失败-密码为空")
    void register_EmptyPassword() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("");

        assertThrows(ServiceException.class, () -> userService.register(user));
        verify(userMapper, never()).insert(isA(User.class));
    }

    @Test
    @DisplayName("登录成功")
    void login_Success() {
        LambdaQueryChainWrapper<User> qc = setupQueryChain();
        when(qc.one()).thenReturn(mockUser);

        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser");
        dto.setPassword("rawPassword");

        when(passwordEncoder.matches("rawPassword", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken("testuser")).thenReturn("jwt.token.here");

        LoginVO result = userService.login(dto);

        assertNotNull(result);
        assertEquals("jwt.token.here", result.getToken());
        assertEquals(1L, result.getUserId());
        assertEquals("测试用户", result.getNickname());
    }

    @Test
    @DisplayName("登录失败-参数为空")
    void login_EmptyParams() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("");
        dto.setPassword("");

        assertThrows(ServiceException.class, () -> userService.login(dto));
    }

    @Test
    @DisplayName("登录失败-用户不存在")
    void login_UserNotFound() {
        LambdaQueryChainWrapper<User> qc = setupQueryChain();
        when(qc.one()).thenReturn(null);

        LoginDTO dto = new LoginDTO();
        dto.setUsername("nonexist");
        dto.setPassword("password");

        assertThrows(ServiceException.class, () -> userService.login(dto));
    }

    @Test
    @DisplayName("登录失败-密码错误")
    void login_WrongPassword() {
        LambdaQueryChainWrapper<User> qc = setupQueryChain();
        when(qc.one()).thenReturn(mockUser);

        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser");
        dto.setPassword("wrongPassword");

        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThrows(ServiceException.class, () -> userService.login(dto));
    }

    @Test
    @DisplayName("登录失败-账号被禁用")
    void login_DisabledAccount() {
        LambdaQueryChainWrapper<User> qc = setupQueryChain();
        when(qc.one()).thenReturn(mockUser);

        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser");
        dto.setPassword("rawPassword");

        mockUser.setStatus(0);

        assertThrows(ServiceException.class, () -> userService.login(dto));
    }

    @Test
    @DisplayName("重置密码成功")
    void resetPassword_Success() {
        LambdaUpdateChainWrapper<User> uc = setupUpdateChain();
        when(uc.update()).thenReturn(true);

        User user = new User();
        user.setId(1L);
        user.setPassword("newPassword");

        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        Boolean result = userService.resetPassword(user);

        assertTrue(result);
        verify(uc).update();
    }

    @Test
    @DisplayName("重置密码失败-ID为空")
    void resetPassword_NullId() {
        User user = new User();
        user.setPassword("newPassword");

        assertThrows(ServiceException.class, () -> userService.resetPassword(user));
    }

    @Test
    @DisplayName("重置密码失败-密码为空")
    void resetPassword_EmptyPassword() {
        User user = new User();
        user.setId(1L);
        user.setPassword("");

        assertThrows(ServiceException.class, () -> userService.resetPassword(user));
    }
}
