package com.wealth.common.contract;

import com.wealth.common.dto.DashboardMarketDataDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 仪表盘行情只读契约，供 system 域聚合展示数据。
 */
public interface DashboardMarketDataProvider {
    BigDecimal sumPrice();

    List<BigDecimal> findLatestTwoPrices();

    List<DashboardMarketDataDTO> findCandles(String productCode, LocalDateTime startTime, LocalDateTime endTime);
}
