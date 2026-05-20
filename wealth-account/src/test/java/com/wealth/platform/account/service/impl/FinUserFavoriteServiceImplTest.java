package com.wealth.platform.account.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.wealth.platform.account.dto.FinUserFavoriteDTO;
import com.wealth.platform.account.entity.WeaUserFavorite;
import com.wealth.platform.account.mapper.FinUserFavoriteMapper;
import com.wealth.platform.account.vo.FinUserFavoriteVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinUserFavoriteServiceImplTest {

    @Mock
    private FinUserFavoriteMapper favoriteMapper;

    private FinUserFavoriteServiceImpl favoriteService;

    private WeaUserFavorite mockFavorite;

    @BeforeEach
    void setUp() {
        favoriteService = mock(FinUserFavoriteServiceImpl.class, CALLS_REAL_METHODS);
        ReflectionTestUtils.setField(favoriteService, "baseMapper", favoriteMapper);

        mockFavorite = new WeaUserFavorite();
        mockFavorite.setId(1L);
        mockFavorite.setUserId(100L);
        mockFavorite.setProductCode("P001");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private LambdaQueryChainWrapper<WeaUserFavorite> setupQueryChain(long countResult) {
        LambdaQueryChainWrapper<WeaUserFavorite> qc = mock(LambdaQueryChainWrapper.class);
        when(qc.eq(any(), any())).thenReturn(qc);
        when(qc.count()).thenReturn(countResult);
        doReturn(qc).when(favoriteService).lambdaQuery();
        return qc;
    }

    @Test
    @DisplayName("创建自选成功")
    void createFavorite_Success() {
        setupQueryChain(0L);
        doReturn(1).when(favoriteMapper).insert(any(WeaUserFavorite.class));

        FinUserFavoriteDTO dto = new FinUserFavoriteDTO();
        dto.setUserId(100L);
        dto.setProductCode("P001");

        boolean result = favoriteService.createFavorite(dto);

        assertTrue(result);
        verify(favoriteMapper).insert(argThat((WeaUserFavorite fav) ->
                100L == fav.getUserId() && "P001".equals(fav.getProductCode())
        ));
    }

    @Test
    @DisplayName("创建自选-重复返回false")
    void createFavorite_Duplicate() {
        setupQueryChain(1L);

        FinUserFavoriteDTO dto = new FinUserFavoriteDTO();
        dto.setUserId(100L);
        dto.setProductCode("P001");

        boolean result = favoriteService.createFavorite(dto);

        assertFalse(result);
        verify(favoriteMapper, never()).insert(isA(WeaUserFavorite.class));
    }

    @Test
    @DisplayName("根据ID查询自选-成功")
    void getFavoriteById_Found() {
        when(favoriteMapper.selectById(1L)).thenReturn(mockFavorite);

        FinUserFavoriteVO result = favoriteService.getFavoriteById(1L);

        assertNotNull(result);
        assertEquals(100L, result.getUserId());
        assertEquals("P001", result.getProductCode());
    }

    @Test
    @DisplayName("根据ID查询自选-不存在返回null")
    void getFavoriteById_NotFound() {
        when(favoriteMapper.selectById(99L)).thenReturn(null);

        FinUserFavoriteVO result = favoriteService.getFavoriteById(99L);

        assertNull(result);
    }

    @SuppressWarnings("unchecked")
    private LambdaQueryChainWrapper<WeaUserFavorite> setupListQueryChain(List<WeaUserFavorite> resultList) {
        LambdaQueryChainWrapper<WeaUserFavorite> qc = mock(LambdaQueryChainWrapper.class);
        when(qc.eq(any(), any())).thenReturn(qc);
        when(qc.list()).thenReturn(resultList);
        doReturn(qc).when(favoriteService).lambdaQuery();
        return qc;
    }

    @Test
    @DisplayName("查询自选列表-有数据")
    void getFavoriteList_WithData() {
        setupListQueryChain(List.of(mockFavorite));

        List<FinUserFavoriteVO> result = favoriteService.getFavoriteList(100L);

        assertEquals(1, result.size());
        assertEquals("P001", result.get(0).getProductCode());
    }

    @Test
    @DisplayName("查询自选列表-空数据")
    void getFavoriteList_Empty() {
        setupListQueryChain(List.of());

        List<FinUserFavoriteVO> result = favoriteService.getFavoriteList(100L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("更新自选成功-null安全更新")
    void updateFavorite_Success() {
        FinUserFavoriteDTO dto = new FinUserFavoriteDTO();
        dto.setProductCode("P002");

        when(favoriteMapper.selectById(1L)).thenReturn(mockFavorite);
        when(favoriteMapper.updateById(any(WeaUserFavorite.class))).thenReturn(1);

        boolean result = favoriteService.updateFavorite(1L, dto);

        assertTrue(result);
        verify(favoriteMapper).updateById(argThat((WeaUserFavorite fav) ->
                fav.getId() == 1L && "P002".equals(fav.getProductCode())
        ));
    }

    @Test
    @DisplayName("更新自选-不存在返回false")
    void updateFavorite_NotFound() {
        when(favoriteMapper.selectById(99L)).thenReturn(null);

        boolean result = favoriteService.updateFavorite(99L, new FinUserFavoriteDTO());

        assertFalse(result);
        verify(favoriteMapper, never()).updateById(isA(WeaUserFavorite.class));
    }

    @Test
    @DisplayName("删除自选成功")
    void deleteFavorite_Success() {
        when(favoriteMapper.deleteById(1L)).thenReturn(1);

        boolean result = favoriteService.deleteFavorite(1L);

        assertTrue(result);
        verify(favoriteMapper).deleteById(1L);
    }
}
