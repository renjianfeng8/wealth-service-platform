package com.wealth.platform.system.service.impl;

import com.wealth.common.contract.DashboardMarketDataProvider;
import com.wealth.common.contract.DashboardTradeOrderProvider;
import com.wealth.platform.system.vo.DashboardKlineVO;
import com.wealth.platform.system.vo.DashboardOverviewVO;
import com.wealth.platform.system.vo.DashboardTrendVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private DashboardMarketDataProvider marketDataProvider;

    @Mock
    private DashboardTradeOrderProvider tradeOrderProvider;

    private DashboardServiceImpl dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardServiceImpl(marketDataProvider, tradeOrderProvider);
    }

    @Test
    void getOverview_should_use_zero_when_provider_returns_null_aggregates() {
        when(marketDataProvider.sumPrice()).thenReturn(null);
        when(marketDataProvider.findLatestTwoPrices()).thenReturn(null);
        when(tradeOrderProvider.sumCompletedAmount()).thenReturn(null);
        when(tradeOrderProvider.sumTodayCompletedAmount()).thenReturn(null);

        DashboardOverviewVO overview = dashboardService.getOverview();

        assertThat(overview.getTotalAsset()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(overview.getAssetChange()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(overview.getBalanceValue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(overview.getBalanceChange()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(overview.getDailyIncome()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(overview.getDailyIncomeRate()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getTrend_should_use_empty_series_source_when_provider_returns_null_orders() {
        when(tradeOrderProvider.findCompletedOrders(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(null);
        when(marketDataProvider.sumPrice()).thenReturn(null);

        DashboardTrendVO trend = dashboardService.getTrend("7D");

        assertThat(trend.getSeries()).hasSize(7);
        assertThat(trend.getSeries())
                .allSatisfy(point -> {
                    assertThat(point.getAssetValue()).isEqualByComparingTo(BigDecimal.valueOf(100));
                    assertThat(point.getBalanceValue()).isEqualByComparingTo(BigDecimal.ZERO);
                    assertThat(point.getIncome()).isEqualByComparingTo(BigDecimal.ZERO);
                });
    }

    @Test
    void getKline_should_use_empty_candles_when_provider_returns_null_records() {
        when(marketDataProvider.findCandles(org.mockito.ArgumentMatchers.eq("P001"),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()))
                .thenReturn(null);

        DashboardKlineVO kline = dashboardService.getKline("P001", "1D");

        assertThat(kline.getCandles()).isEmpty();
    }
}
