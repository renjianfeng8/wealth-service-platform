package com.wealth.platform.product.service.impl;

import com.wealth.common.exception.ServiceException;
import com.wealth.platform.product.entity.WeaMarketData;
import com.wealth.platform.product.mapper.MarketDataMapper;
import com.wealth.platform.product.vo.MarketDataVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarketDataServiceImplTest {

    @Mock
    private MarketDataMapper marketDataMapper;

    private MarketDataServiceImpl marketDataService;

    private WeaMarketData mockMarketData;

    @BeforeEach
    void setUp() {
        marketDataService = spy(new MarketDataServiceImpl());
        ReflectionTestUtils.setField(marketDataService, "baseMapper", marketDataMapper);

        mockMarketData = new WeaMarketData();
        mockMarketData.setId(1L);
        mockMarketData.setProductCode("P001");
        mockMarketData.setCurrentPrice(new BigDecimal("10.50"));
    }

    @Test
    @DisplayName("根据ID查询行情-成功")
    void getMarketDataById_Found() {
        when(marketDataMapper.selectById(1L)).thenReturn(mockMarketData);

        MarketDataVO result = marketDataService.getMarketDataById(1L);

        assertNotNull(result);
        assertEquals("P001", result.getProductCode());
        assertEquals(new BigDecimal("10.50"), result.getCurrentPrice());
    }

    @Test
    @DisplayName("根据ID查询行情-不存在抛404")
    void getMarketDataById_NotFound() {
        when(marketDataMapper.selectById(99L)).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class, () ->
                marketDataService.getMarketDataById(99L));

        assertEquals(404, exception.getCode());
    }
}
