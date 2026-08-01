package com.wealth.platform.user.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.wealth.common.dto.LoginDTO;
import com.wealth.common.exception.ServiceException;
import com.wealth.common.utils.JwtUtil;
import com.wealth.platform.user.dto.UserDTO;
import com.wealth.platform.user.entity.User;
import com.wealth.platform.user.mapper.UserMapper;
import com.wealth.platform.user.vo.LoginVO;
import com.wealth.platform.user.vo.UserVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    @SuppressWarnings({"unchecked", "rawtypes"})
    void register_Success() {
        UserDTO dto = new UserDTO();
        dto.setUsername("newuser");
        dto.setPassword("rawPassword");

        when(userMapper.selectCount(any())).thenReturn(0L);

        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        doReturn(1).when(userMapper).insert(any(User.class));

        Boolean result = userService.register(dto);

        assertTrue(result);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(captor.capture());
        assertEquals("newuser", captor.getValue().getUsername());
        assertEquals("encodedPassword", captor.getValue().getPassword());
    }

    @Test
    @DisplayName("创建用户成功-密码加密")
    void createUser_Success() {
        UserDTO dto = new UserDTO();
        dto.setUsername("newuser");
        dto.setPassword("rawPassword");

        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        doReturn(1).when(userMapper).insert(any(User.class));

        boolean result = userService.createUser(dto);

        assertTrue(result);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(captor.capture());
        assertEquals("newuser", captor.getValue().getUsername());
        assertEquals("encodedPassword", captor.getValue().getPassword());
    }

    @Test
    @DisplayName("注册失败-用户名为空")
    void register_EmptyUsername() {
        UserDTO dto = new UserDTO();
        dto.setUsername("");
        dto.setPassword("password");

        assertThrows(ServiceException.class, () -> userService.register(dto));
        verify(userMapper, never()).insert(isA(User.class));
    }

    @Test
    @DisplayName("注册失败-密码为空")
    void register_EmptyPassword() {
        UserDTO dto = new UserDTO();
        dto.setUsername("testuser");
        dto.setPassword("");

        assertThrows(ServiceException.class, () -> userService.register(dto));
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
        when(jwtUtil.generateToken("testuser", "user")).thenReturn("jwt.token.here");
        when(jwtUtil.getAccessExpire()).thenReturn(1800000L);

        LoginVO result = userService.login(dto);

        assertNotNull(result);
        assertEquals("jwt.token.here", result.getToken());
        assertEquals(1L, result.getUserId());
        assertEquals("测试用户", result.getNickname());
        assertEquals(1800, result.getExpiresInSeconds());
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

        when(userMapper.selectById(1L)).thenReturn(mockUser);
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        Boolean result = userService.resetPassword(user, "oldPassword");

        assertTrue(result);
        verify(uc).update();
    }

    @Test
    @DisplayName("重置密码失败-无旧密码")
    void resetPassword_NullOldPassword() {
        User user = new User();
        user.setId(1L);
        user.setPassword("newPassword");

        assertThrows(ServiceException.class, () -> userService.resetPassword(user, ""));
    }

    @Test
    @DisplayName("重置密码失败-新密码为空")
    void resetPassword_EmptyPassword() {
        User user = new User();
        user.setId(1L);
        user.setPassword("");

        assertThrows(ServiceException.class, () -> userService.resetPassword(user, "oldPassword"));
    }

    @Test
    @DisplayName("根据ID查询用户-成功")
    void getUserById_Found() {
        when(userMapper.selectById(1L)).thenReturn(mockUser);

        UserVO result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("测试用户", result.getNickname());
    }

    @Test
    @DisplayName("根据ID查询用户-不存在抛404")
    void getUserById_NotFound() {
        when(userMapper.selectById(99L)).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class, () ->
                userService.getUserById(99L));

        assertEquals(404, exception.getCode());
    }

    @Test
    @DisplayName("更新用户成功-密码被清空")
    void updateUser_Success() {
        UserDTO dto = new UserDTO();
        dto.setNickname("更新后的昵称");
        dto.setPassword("newPassword");

        when(userMapper.selectById(1L)).thenReturn(mockUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        boolean result = userService.updateUser(1L, dto);

        assertTrue(result);
        verify(userMapper).updateById(argThat((User user) ->
                user.getId() == 1L &&
                "更新后的昵称".equals(user.getNickname()) &&
                user.getPassword() == null
        ));
    }

    @Test
    @DisplayName("更新用户-不存在抛404")
    void updateUser_NotFound() {
        when(userMapper.selectById(99L)).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class, () ->
                userService.updateUser(99L, new UserDTO()));

        assertEquals(404, exception.getCode());
        verify(userMapper, never()).updateById(isA(User.class));
    }

    @Test
    @DisplayName("删除用户成功")
    void deleteUser_Success() {
        when(userMapper.selectById(1L)).thenReturn(mockUser);
        when(userMapper.deleteById(1L)).thenReturn(1);

        boolean result = userService.deleteUser(1L);

        assertTrue(result);
        verify(userMapper).deleteById(1L);
    }

    @Test
    @DisplayName("删除用户-不存在抛404")
    void deleteUser_NotFound() {
        when(userMapper.selectById(99L)).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class, () ->
                userService.deleteUser(99L));

        assertEquals(404, exception.getCode());
        verify(userMapper, never()).deleteById(isA(User.class));
    }
}
