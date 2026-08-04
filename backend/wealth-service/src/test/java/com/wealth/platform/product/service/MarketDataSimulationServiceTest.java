package com.wealth.platform.product.service;

import com.wealth.platform.product.entity.WeaMarketData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MarketDataSimulationServiceTest {

    @Mock
    private MarketDataService marketDataService;

    @Mock
    private MarketDataPushService pushService;

    private MarketDataSimulationService simulationService;

    @BeforeEach
    void setUp() {
        simulationService = new MarketDataSimulationService(marketDataService, pushService);
    }

    @Test
    void simulateTickDb_should_handle_null_price_fields() {
        WeaMarketData data = new WeaMarketData();
        data.setId(1L);
        data.setCurrentPrice(null);
        data.setClosePrice(null);
        data.setHighestPrice(null);
        data.setLowestPrice(null);

        ReflectionTestUtils.setField(simulationService, "cachedMarketData", List.of(data));

        simulationService.simulateTickDb();

        assertNotNull(data.getCurrentPrice());
        assertNotNull(data.getRiseFall());
        assertNotNull(data.getRiseFallRate());
        assertNotNull(data.getHighestPrice());
        assertNotNull(data.getLowestPrice());
        verify(marketDataService).updateById(any(WeaMarketData.class));
    }

    @Test
    void simulateTickDb_should_use_close_price_when_current_price_is_null() {
        WeaMarketData data = new WeaMarketData();
        data.setId(1L);
        data.setCurrentPrice(null);
        data.setClosePrice(new BigDecimal("100.00"));

        ReflectionTestUtils.setField(simulationService, "cachedMarketData", List.of(data));

        simulationService.simulateTickDb();

        assertNotNull(data.getCurrentPrice());
        assertNotNull(data.getHighestPrice());
        assertNotNull(data.getLowestPrice());
        verify(marketDataService).updateById(any(WeaMarketData.class));
    }
}
