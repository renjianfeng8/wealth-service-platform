package com.wealth.common.contract;

import com.wealth.common.dto.DashboardTradeOrderDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 仪表盘交易只读契约，供 system 域聚合展示数据。
 */
public interface DashboardTradeOrderProvider {
    BigDecimal sumCompletedAmount();

    long countCompletedOrders();

    BigDecimal sumFirstHalf(int limit);

    BigDecimal sumLastHalf(int limit);

    BigDecimal sumTodayCompletedAmount();

    List<DashboardTradeOrderDTO> findCompletedOrders(LocalDateTime startTime, LocalDateTime endTime);
}
