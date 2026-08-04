package com.wealth.platform.system.service.impl;

import com.wealth.platform.system.dto.UmsAdminRoleRelationDTO;
import com.wealth.platform.system.entity.UmsAdminRoleRelation;
import com.wealth.platform.system.mapper.UmsAdminRoleRelationMapper;
import com.wealth.platform.system.service.PermissionCacheCleaner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UmsAdminRoleRelationServiceImplTest {

    @Mock
    private UmsAdminRoleRelationMapper relationMapper;

    @Mock
    private PermissionCacheCleaner permissionCacheCleaner;

    private UmsAdminRoleRelationServiceImpl relationService;

    @BeforeEach
    void setUp() {
        relationService = mock(UmsAdminRoleRelationServiceImpl.class, CALLS_REAL_METHODS);
        ReflectionTestUtils.setField(relationService, "baseMapper", relationMapper);
        ReflectionTestUtils.setField(relationService, "permissionCacheCleaner", permissionCacheCleaner);
    }

    @Test
    @DisplayName("创建管理员角色关联-保存并清除缓存")
    void createRelation_shouldSaveAndClearCache() {
        UmsAdminRoleRelationDTO dto = new UmsAdminRoleRelationDTO();
        dto.setAdminId(5L);
        dto.setRoleId(10L);

        when(relationMapper.insert(any(UmsAdminRoleRelation.class))).thenReturn(1);

        boolean result = relationService.createRelation(dto);

        assertTrue(result);
        ArgumentCaptor<UmsAdminRoleRelation> captor = ArgumentCaptor.forClass(UmsAdminRoleRelation.class);
        verify(relationMapper).insert(captor.capture());
        assertEquals(5L, captor.getValue().getAdminId());
        assertEquals(10L, captor.getValue().getRoleId());
        verify(permissionCacheCleaner).clear(5L);
    }

    @Test
    @DisplayName("更新管理员角色关联-清除新旧管理员缓存")
    void updateRelation_shouldClearOldAndNew() {
        UmsAdminRoleRelation existing = new UmsAdminRoleRelation();
        existing.setId(1L);
        existing.setAdminId(5L);
        existing.setRoleId(10L);

        UmsAdminRoleRelationDTO dto = new UmsAdminRoleRelationDTO();
        dto.setAdminId(6L);
        dto.setRoleId(10L);

        when(relationMapper.selectById(1L)).thenReturn(existing);
        when(relationMapper.updateById(any(UmsAdminRoleRelation.class))).thenReturn(1);

        boolean result = relationService.updateRelation(1L, dto);

        assertTrue(result);
        verify(permissionCacheCleaner).clear(5L);
        verify(permissionCacheCleaner).clear(6L);
    }

    @Test
    @DisplayName("删除管理员角色关联-清除缓存")
    void deleteRelation_shouldClearCache() {
        UmsAdminRoleRelation existing = new UmsAdminRoleRelation();
        existing.setId(1L);
        existing.setAdminId(5L);

        when(relationMapper.selectById(1L)).thenReturn(existing);
        when(relationMapper.deleteById(1L)).thenReturn(1);

        boolean result = relationService.deleteRelation(1L);

        assertTrue(result);
        verify(permissionCacheCleaner).clear(5L);
    }
}
