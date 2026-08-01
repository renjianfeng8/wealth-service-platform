package com.wealth.platform.system.service.impl;

import com.wealth.common.exception.ServiceException;
import com.wealth.platform.system.dto.UmsResourceDTO;
import com.wealth.platform.system.entity.UmsResource;
import com.wealth.platform.system.mapper.UmsResourceMapper;
import com.wealth.platform.system.vo.UmsResourceVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class UmsResourceServiceImplTest {

    @Mock
    private UmsResourceMapper umsResourceMapper;

    private UmsResourceServiceImpl umsResourceService;

    private UmsResource mockResource;

    @BeforeEach
    void setUp() {
        umsResourceService = spy(new UmsResourceServiceImpl());
        ReflectionTestUtils.setField(umsResourceService, "baseMapper", umsResourceMapper);

        mockResource = new UmsResource();
        mockResource.setId(1L);
        mockResource.setName("产品查询");
        mockResource.setUrl("/api/v1/product/**");
        mockResource.setDescription("产品查询接口");
        mockResource.setCategoryId(1L);
    }

    @Test
    @DisplayName("根据ID查询资源-成功")
    void getResourceById_Found() {
        when(umsResourceMapper.selectById(1L)).thenReturn(mockResource);

        UmsResourceVO result = umsResourceService.getResourceById(1L);

        assertNotNull(result);
        assertEquals("产品查询", result.getName());
        assertEquals("/api/v1/product/**", result.getUrl());
    }

    @Test
    @DisplayName("根据ID查询资源-不存在抛404")
    void getResourceById_NotFound() {
        when(umsResourceMapper.selectById(99L)).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class, () ->
                umsResourceService.getResourceById(99L));

        assertEquals(404, exception.getCode());
    }

    @Test
    @DisplayName("更新资源成功-null安全更新")
    void updateResource_Success() {
        UmsResourceDTO dto = new UmsResourceDTO();
        dto.setName("更新后的资源");

        when(umsResourceMapper.selectById(1L)).thenReturn(mockResource);
        when(umsResourceMapper.updateById(any(UmsResource.class))).thenReturn(1);

        boolean result = umsResourceService.updateResource(1L, dto);

        assertTrue(result);
        verify(umsResourceMapper).updateById(argThat((UmsResource resource) ->
                resource.getId() == 1L && "更新后的资源".equals(resource.getName())
        ));
    }

    @Test
    @DisplayName("更新资源-不存在抛404")
    void updateResource_NotFound() {
        when(umsResourceMapper.selectById(99L)).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class, () ->
                umsResourceService.updateResource(99L, new UmsResourceDTO()));

        assertEquals(404, exception.getCode());
        verify(umsResourceMapper, never()).updateById(isA(UmsResource.class));
    }

    @Test
    @DisplayName("创建资源成功")
    void createResource_Success() {
        UmsResourceDTO dto = new UmsResourceDTO();
        dto.setName("新资源");
        dto.setUrl("/api/v1/new/**");

        when(umsResourceMapper.insert(any(UmsResource.class))).thenReturn(1);

        boolean result = umsResourceService.createResource(dto);

        assertTrue(result);
        ArgumentCaptor<UmsResource> captor = ArgumentCaptor.forClass(UmsResource.class);
        verify(umsResourceMapper).insert(captor.capture());
        assertEquals("新资源", captor.getValue().getName());
    }

    @Test
    @DisplayName("删除资源成功")
    void deleteResource_Success() {
        when(umsResourceMapper.selectById(1L)).thenReturn(mockResource);
        when(umsResourceMapper.deleteById(1L)).thenReturn(1);

        boolean result = umsResourceService.deleteResource(1L);

        assertTrue(result);
        verify(umsResourceMapper).deleteById(1L);
    }

    @Test
    @DisplayName("删除资源-不存在抛404")
    void deleteResource_NotFound() {
        when(umsResourceMapper.selectById(99L)).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class, () ->
                umsResourceService.deleteResource(99L));

        assertEquals(404, exception.getCode());
        verify(umsResourceMapper, never()).deleteById(isA(UmsResource.class));
    }
}
