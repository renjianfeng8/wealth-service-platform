package com.wealth.common.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 仪表盘趋势所需交易快照。
 */
@Data
public class DashboardTradeOrderDTO {
    private LocalDateTime createTime;
    private BigDecimal entrustPrice;
    private Integer entrustNum;
}
