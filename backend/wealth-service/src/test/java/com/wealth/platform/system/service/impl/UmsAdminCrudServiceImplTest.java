package com.wealth.platform.system.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.wealth.common.dto.AdminIdentityDTO;
import com.wealth.common.exception.ServiceException;
import com.wealth.platform.system.dto.UmsAdminDTO;
import com.wealth.platform.system.entity.UmsAdmin;
import com.wealth.platform.system.mapper.UmsAdminMapper;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UmsAdminCrudServiceImplTest {

    @Mock
    private UmsAdminMapper umsAdminMapper;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    private UmsAdminCrudServiceImpl adminCrudService;

    private UmsAdmin mockAdmin;

    @BeforeEach
    void setUp() {
        adminCrudService = spy(new UmsAdminCrudServiceImpl(passwordEncoder));
        ReflectionTestUtils.setField(adminCrudService, "baseMapper", umsAdminMapper);

        mockAdmin = new UmsAdmin();
        mockAdmin.setId(1L);
        mockAdmin.setUsername("admin");
        mockAdmin.setPassword("encodedPassword");
        mockAdmin.setStatus(1);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private LambdaQueryChainWrapper<UmsAdmin> mockLambdaQuery() {
        LambdaQueryChainWrapper<UmsAdmin> qc = mock(LambdaQueryChainWrapper.class);
        when(qc.eq(any(), any())).thenReturn(qc);
        when(qc.one()).thenReturn(mockAdmin);
        doReturn(qc).when(adminCrudService).lambdaQuery();
        return qc;
    }

    @Test
    @DisplayName("创建管理员成功")
    void createAdmin_Success() {
        UmsAdminDTO dto = new UmsAdminDTO();
        dto.setUsername("newadmin");
        dto.setPassword("rawPassword");

        when(umsAdminMapper.selectCount(any())).thenReturn(0L);
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(umsAdminMapper.insert(any(UmsAdmin.class))).thenReturn(1);

        Boolean result = adminCrudService.createAdmin(dto);

        assertTrue(result);
        ArgumentCaptor<UmsAdmin> captor = ArgumentCaptor.forClass(UmsAdmin.class);
        verify(umsAdminMapper).insert(captor.capture());
        assertEquals("newadmin", captor.getValue().getUsername());
        assertEquals("encodedPassword", captor.getValue().getPassword());
    }

    @Test
    @DisplayName("按用户名查询未删除管理员")
    void getActiveByUsername_ShouldReturnAdmin() {
        mockLambdaQuery();

        UmsAdmin admin = adminCrudService.getActiveByUsername("admin");

        assertNotNull(admin);
        assertEquals("admin", admin.getUsername());
    }

    @Test
    @DisplayName("按用户名查询身份-返回管理员的AdminIdentityDTO")
    void findByUsername_ShouldReturnIdentity() {
        mockLambdaQuery();
        mockAdmin.setNickName("管理员昵称");

        AdminIdentityDTO identity = adminCrudService.findByUsername("admin");

        assertNotNull(identity);
        assertEquals("admin", identity.getUsername());
        assertEquals("管理员昵称", identity.getNickname());
    }

    @Test
    @DisplayName("按用户名查询身份-管理员不存在返回null")
    void findByUsername_ShouldReturnNullWhenNotFound() {
        LambdaQueryChainWrapper<UmsAdmin> qc = mockLambdaQuery();
        when(qc.one()).thenReturn(null);

        AdminIdentityDTO identity = adminCrudService.findByUsername("ghost");

        assertNull(identity);
    }

    @Test
    @DisplayName("根据ID查询管理员-成功")
    void getAdminById_Found() {
        when(umsAdminMapper.selectById(1L)).thenReturn(mockAdmin);

        UmsAdminVO result = adminCrudService.getAdminById(1L);

        assertNotNull(result);
        assertEquals("admin", result.getUsername());
        assertEquals(1, result.getStatus());
    }

    @Test
    @DisplayName("根据ID查询管理员-不存在抛404")
    void getAdminById_NotFound() {
        when(umsAdminMapper.selectById(99L)).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class, () ->
                adminCrudService.getAdminById(99L));

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

        boolean result = adminCrudService.updateAdmin(1L, dto);

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
                adminCrudService.updateAdmin(99L, new UmsAdminDTO()));

        assertEquals(404, exception.getCode());
        verify(umsAdminMapper, never()).updateById(isA(UmsAdmin.class));
    }

    @Test
    @DisplayName("删除管理员成功")
    void deleteAdmin_Success() {
        when(umsAdminMapper.selectById(1L)).thenReturn(mockAdmin);
        when(umsAdminMapper.deleteById(1L)).thenReturn(1);

        boolean result = adminCrudService.deleteAdmin(1L);

        assertTrue(result);
        verify(umsAdminMapper).deleteById(1L);
    }

    @Test
    @DisplayName("删除管理员-不存在抛404")
    void deleteAdmin_NotFound() {
        when(umsAdminMapper.selectById(99L)).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class, () ->
                adminCrudService.deleteAdmin(99L));

        assertEquals(404, exception.getCode());
        verify(umsAdminMapper, never()).deleteById(isA(UmsAdmin.class));
    }
}
