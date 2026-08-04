package com.wealth.platform.system.service.impl;

import com.wealth.platform.system.dto.UmsRoleResourceRelationDTO;
import com.wealth.platform.system.entity.UmsRoleResourceRelation;
import com.wealth.platform.system.mapper.UmsRoleResourceRelationMapper;
import com.wealth.platform.system.service.PermissionCacheCleaner;
import com.wealth.platform.system.service.UmsAdminRoleRelationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UmsRoleResourceRelationServiceImplTest {

    @Mock
    private UmsRoleResourceRelationMapper relationMapper;

    @Mock
    private UmsAdminRoleRelationService adminRoleRelationService;

    @Mock
    private PermissionCacheCleaner permissionCacheCleaner;

    private UmsRoleResourceRelationServiceImpl relationService;

    @BeforeEach
    void setUp() {
        relationService = mock(UmsRoleResourceRelationServiceImpl.class, CALLS_REAL_METHODS);
        ReflectionTestUtils.setField(relationService, "baseMapper", relationMapper);
        ReflectionTestUtils.setField(relationService, "adminRoleRelationService", adminRoleRelationService);
        ReflectionTestUtils.setField(relationService, "permissionCacheCleaner", permissionCacheCleaner);
    }

    @Test
    @DisplayName("创建角色资源关联-清除关联管理员缓存")
    void createRelation_shouldClearAdminsByRole() {
        UmsRoleResourceRelationDTO dto = new UmsRoleResourceRelationDTO();
        dto.setRoleId(3L);
        dto.setResourceId(20L);

        when(relationMapper.insert(any(UmsRoleResourceRelation.class))).thenReturn(1);
        when(adminRoleRelationService.getAdminIdByRoleId(3L)).thenReturn(List.of(5L, 7L));

        boolean result = relationService.createRelation(dto);

        assertTrue(result);
        ArgumentCaptor<UmsRoleResourceRelation> captor = ArgumentCaptor.forClass(UmsRoleResourceRelation.class);
        verify(relationMapper).insert(captor.capture());
        assertEquals(3L, captor.getValue().getRoleId());
        assertEquals(20L, captor.getValue().getResourceId());
        verify(permissionCacheCleaner).clear(5L);
        verify(permissionCacheCleaner).clear(7L);
    }

    @Test
    @DisplayName("更新角色资源关联-清除新旧角色关联管理员缓存")
    void updateRelation_shouldClearOldAndNewRoles() {
        UmsRoleResourceRelation existing = new UmsRoleResourceRelation();
        existing.setId(1L);
        existing.setRoleId(3L);
        existing.setResourceId(20L);

        UmsRoleResourceRelationDTO dto = new UmsRoleResourceRelationDTO();
        dto.setRoleId(4L);
        dto.setResourceId(20L);

        when(relationMapper.selectById(1L)).thenReturn(existing);
        when(relationMapper.updateById(any(UmsRoleResourceRelation.class))).thenReturn(1);
        when(adminRoleRelationService.getAdminIdByRoleId(3L)).thenReturn(List.of(5L));
        when(adminRoleRelationService.getAdminIdByRoleId(4L)).thenReturn(List.of(6L));

        boolean result = relationService.updateRelation(1L, dto);

        assertTrue(result);
        verify(permissionCacheCleaner).clear(5L);
        verify(permissionCacheCleaner).clear(6L);
    }

    @Test
    @DisplayName("删除角色资源关联-清除关联管理员缓存")
    void deleteRelation_shouldClearAdminsByRole() {
        UmsRoleResourceRelation existing = new UmsRoleResourceRelation();
        existing.setId(1L);
        existing.setRoleId(3L);

        when(relationMapper.selectById(1L)).thenReturn(existing);
        when(relationMapper.deleteById(1L)).thenReturn(1);
        when(adminRoleRelationService.getAdminIdByRoleId(3L)).thenReturn(List.of(5L));

        boolean result = relationService.deleteRelation(1L);

        assertTrue(result);
        verify(permissionCacheCleaner).clear(5L);
    }
}
