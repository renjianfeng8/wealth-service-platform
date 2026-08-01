package com.wealth.platform.product.service.impl;

import com.wealth.common.exception.ServiceException;
import com.wealth.platform.product.entity.WeaUserFavorite;
import com.wealth.platform.product.mapper.UserFavoriteMapper;
import com.wealth.platform.product.vo.UserFavoriteVO;
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
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserFavoriteServiceImplTest {

    @Mock
    private UserFavoriteMapper userFavoriteMapper;

    private UserFavoriteServiceImpl userFavoriteService;

    private WeaUserFavorite mockFavorite;

    @BeforeEach
    void setUp() {
        userFavoriteService = spy(new UserFavoriteServiceImpl());
        ReflectionTestUtils.setField(userFavoriteService, "baseMapper", userFavoriteMapper);

        mockFavorite = new WeaUserFavorite();
        mockFavorite.setId(1L);
        mockFavorite.setUserId(100L);
        mockFavorite.setProductCode("P001");
    }

    @Test
    @DisplayName("根据ID查询自选-成功")
    void getFavoriteById_Found() {
        when(userFavoriteMapper.selectById(1L)).thenReturn(mockFavorite);

        UserFavoriteVO result = userFavoriteService.getFavoriteById(1L);

        assertNotNull(result);
        assertEquals(100L, result.getUserId());
        assertEquals("P001", result.getProductCode());
    }

    @Test
    @DisplayName("根据ID查询自选-不存在抛404")
    void getFavoriteById_NotFound() {
        when(userFavoriteMapper.selectById(99L)).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class, () ->
                userFavoriteService.getFavoriteById(99L));

        assertEquals(404, exception.getCode());
    }
}
