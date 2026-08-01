package com.wealth.platform.system.service.impl;

import com.wealth.common.exception.ServiceException;
import com.wealth.platform.system.dto.UmsRoleDTO;
import com.wealth.platform.system.entity.UmsRole;
import com.wealth.platform.system.mapper.UmsRoleMapper;
import com.wealth.platform.system.vo.UmsRoleVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UmsRoleServiceImplTest {

    @Mock
    private UmsRoleMapper umsRoleMapper;

    private UmsRoleServiceImpl umsRoleService;

    private UmsRole mockRole;

    @BeforeEach
    void setUp() {
        umsRoleService = spy(new UmsRoleServiceImpl());
        ReflectionTestUtils.setField(umsRoleService, "baseMapper", umsRoleMapper);

        mockRole = new UmsRole();
        mockRole.setId(1L);
        mockRole.setName("管理员");
        mockRole.setDescription("系统管理员");
        mockRole.setStatus(1);
        mockRole.setSort(1);
    }

    @Test
    @DisplayName("根据ID查询角色-成功")
    void getRoleById_Found() {
        when(umsRoleMapper.selectById(1L)).thenReturn(mockRole);

        UmsRoleVO result = umsRoleService.getRoleById(1L);

        assertNotNull(result);
        assertEquals("管理员", result.getName());
        assertEquals(1, result.getStatus());
    }

    @Test
    @DisplayName("根据ID查询角色-不存在抛404")
    void getRoleById_NotFound() {
        when(umsRoleMapper.selectById(99L)).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class, () ->
                umsRoleService.getRoleById(99L));

        assertEquals(404, exception.getCode());
    }

    @Test
    @DisplayName("更新角色成功-null安全更新")
    void updateRole_Success() {
        UmsRoleDTO dto = new UmsRoleDTO();
        dto.setName("更新后的角色");

        when(umsRoleMapper.selectById(1L)).thenReturn(mockRole);
        when(umsRoleMapper.updateById(any(UmsRole.class))).thenReturn(1);

        boolean result = umsRoleService.updateRole(1L, dto);

        assertTrue(result);
        verify(umsRoleMapper).updateById(argThat((UmsRole role) ->
                role.getId() == 1L && "更新后的角色".equals(role.getName())
        ));
    }

    @Test
    @DisplayName("更新角色-不存在抛404")
    void updateRole_NotFound() {
        when(umsRoleMapper.selectById(99L)).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class, () ->
                umsRoleService.updateRole(99L, new UmsRoleDTO()));

        assertEquals(404, exception.getCode());
        verify(umsRoleMapper, never()).updateById(isA(UmsRole.class));
    }

    @Test
    @DisplayName("删除角色成功")
    void deleteRole_Success() {
        when(umsRoleMapper.selectById(1L)).thenReturn(mockRole);
        when(umsRoleMapper.deleteById(1L)).thenReturn(1);

        boolean result = umsRoleService.deleteRole(1L);

        assertTrue(result);
        verify(umsRoleMapper).deleteById(1L);
    }

    @Test
    @DisplayName("删除角色-不存在抛404")
    void deleteRole_NotFound() {
        when(umsRoleMapper.selectById(99L)).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class, () ->
                umsRoleService.deleteRole(99L));

        assertEquals(404, exception.getCode());
        verify(umsRoleMapper, never()).deleteById(isA(UmsRole.class));
    }
}
