package com.wealth.common.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 仪表盘 K 线所需行情快照。
 */
@Data
public class DashboardMarketDataDTO {
    private LocalDateTime marketTime;
    private BigDecimal openPrice;
    private BigDecimal highestPrice;
    private BigDecimal lowestPrice;
    private BigDecimal currentPrice;
}
