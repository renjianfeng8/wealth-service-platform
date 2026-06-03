package com.wealth.platform.system.service.impl;

import com.wealth.common.contract.DashboardMarketDataProvider;
import com.wealth.common.contract.DashboardTradeOrderProvider;
import com.wealth.common.dto.DashboardMarketDataDTO;
import com.wealth.common.dto.DashboardTradeOrderDTO;
import com.wealth.platform.system.service.DashboardService;
import com.wealth.platform.system.vo.DashboardKlineVO;
import com.wealth.platform.system.vo.DashboardOverviewVO;
import com.wealth.platform.system.vo.DashboardTrendVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    /** 无实际成交量时使用的估算乘数 */
    private static final BigDecimal ESTIMATED_VOLUME = BigDecimal.valueOf(1000);
    private static final DateTimeFormatter KLINE_TIME_FMT = DateTimeFormatter.ofPattern("MM-dd HH:mm");

    private final DashboardMarketDataProvider marketDataProvider;
    private final DashboardTradeOrderProvider tradeOrderProvider;

    @Override
    public DashboardOverviewVO getOverview() {
        DashboardOverviewVO vo = new DashboardOverviewVO();

        // 1. totalAsset = SUM(price) * ESTIMATED_VOLUME（聚合查询，避免全表扫描 OOM）
        BigDecimal totalPrice = zeroIfNull(marketDataProvider.sumPrice());
        BigDecimal totalAsset = totalPrice.multiply(ESTIMATED_VOLUME);
        vo.setTotalAsset(totalAsset);

        // 2. assetChange based on latest two price records
        List<BigDecimal> latestPrices = safeList(marketDataProvider.findLatestTwoPrices());
        if (latestPrices.size() >= 2) {
            BigDecimal latest = latestPrices.get(0) != null ? latestPrices.get(0) : BigDecimal.ZERO;
            BigDecimal prev = latestPrices.get(1) != null ? latestPrices.get(1) : BigDecimal.ZERO;
            if (prev.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal change = latest.subtract(prev)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(prev, 2, RoundingMode.HALF_UP);
                vo.setAssetChange(change);
            } else {
                vo.setAssetChange(BigDecimal.ZERO);
            }
        } else {
            vo.setAssetChange(BigDecimal.ZERO);
        }

        // 3. balanceValue = SUM(order_price * order_quantity) from completed orders
        BigDecimal balanceValue = zeroIfNull(tradeOrderProvider.sumCompletedAmount());
        vo.setBalanceValue(balanceValue);

        // 4. balanceChange: compare first half vs second half of completed orders
        long completedCount = tradeOrderProvider.countCompletedOrders();
        if (completedCount >= 4) {
            int halfSize = (int) (completedCount / 2);
            BigDecimal firstHalf = zeroIfNull(tradeOrderProvider.sumFirstHalf(halfSize));
            BigDecimal secondHalf = zeroIfNull(tradeOrderProvider.sumLastHalf(halfSize));
            if (firstHalf.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal change = secondHalf.subtract(firstHalf)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(firstHalf, 2, RoundingMode.HALF_UP);
                vo.setBalanceChange(change);
            } else {
                vo.setBalanceChange(BigDecimal.ZERO);
            }
        } else {
            vo.setBalanceChange(BigDecimal.ZERO);
        }

        // 5. dailyIncome = today's completed order amount (聚合查询)
        BigDecimal dailyIncome = zeroIfNull(tradeOrderProvider.sumTodayCompletedAmount());
        vo.setDailyIncome(dailyIncome);

        // 6. dailyIncomeRate
        if (totalAsset.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal rate = dailyIncome.multiply(BigDecimal.valueOf(100))
                    .divide(totalAsset, 2, RoundingMode.HALF_UP);
            vo.setDailyIncomeRate(rate);
        } else {
            vo.setDailyIncomeRate(BigDecimal.ZERO);
        }

        return vo;
    }

    @Override
    public DashboardTrendVO getTrend(String period) {
        DashboardTrendVO vo = new DashboardTrendVO();
        List<DashboardTrendVO.TrendPoint> series = new ArrayList<>();

        int days = "30D".equals(period) ? 30 : 7;
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        List<DashboardTradeOrderDTO> orders = safeList(tradeOrderProvider.findCompletedOrders(
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59)
        ));

        Map<LocalDate, List<DashboardTradeOrderDTO>> orderByDate = orders.stream()
                .filter(o -> o.getCreateTime() != null)
                .collect(Collectors.groupingBy(o -> o.getCreateTime().toLocalDate()));

        // 资产趋势基值：聚合查询代替全表扫描
        BigDecimal baseTotalAsset = zeroIfNull(marketDataProvider.sumPrice()).multiply(ESTIMATED_VOLUME);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        BigDecimal assetIndex = BigDecimal.valueOf(100);
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            List<DashboardTradeOrderDTO> dayOrders = orderByDate.getOrDefault(date, Collections.emptyList());
            BigDecimal dayAmount = dayOrders.stream()
                    .map(o -> o.getEntrustPrice() != null && o.getEntrustNum() != null
                            ? o.getEntrustPrice().multiply(BigDecimal.valueOf(o.getEntrustNum()))
                            : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 日收益率 = 当日成交额 / 总市值，放大 30 倍使波动可见
            if (baseTotalAsset.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal dailyReturn = dayAmount.multiply(BigDecimal.valueOf(30))
                        .divide(baseTotalAsset, 6, RoundingMode.HALF_UP);
                assetIndex = assetIndex.multiply(BigDecimal.ONE.add(dailyReturn))
                        .setScale(2, RoundingMode.HALF_UP);
            }

            DashboardTrendVO.TrendPoint point = new DashboardTrendVO.TrendPoint();
            point.setDate(date.format(fmt));
            point.setAssetValue(assetIndex);
            point.setBalanceValue(dayAmount);
            point.setIncome(dayAmount);
            series.add(point);
        }

        vo.setSeries(series);
        return vo;
    }

    @Override
    public DashboardKlineVO getKline(String productCode, String period) {
        DashboardKlineVO vo = new DashboardKlineVO();
        vo.setProductCode(productCode);
        vo.setProductName(productCode); // 产品名待产品表联表后完善

        // 按 period 确定时间范围
        LocalDateTime startTime;
        LocalDateTime endTime = LocalDateTime.now();
        switch (period) {
            case "1W":  startTime = endTime.minusWeeks(1);  break;
            case "1M":  startTime = endTime.minusMonths(1); break;
            default:    startTime = endTime.minusDays(1);    break; // 1D
        }

        List<DashboardMarketDataDTO> records = safeList(marketDataProvider.findCandles(productCode, startTime, endTime));

        List<DashboardKlineVO.Candle> candles = new ArrayList<>();
        for (DashboardMarketDataDTO m : records) {
            if (m == null) {
                continue;
            }
            DashboardKlineVO.Candle c = new DashboardKlineVO.Candle();
            c.setTime(m.getMarketTime() != null ? m.getMarketTime().format(KLINE_TIME_FMT) : "");
            c.setOpen(m.getOpenPrice() != null ? m.getOpenPrice() : BigDecimal.ZERO);
            c.setHigh(m.getHighestPrice() != null ? m.getHighestPrice() : BigDecimal.ZERO);
            c.setLow(m.getLowestPrice() != null ? m.getLowestPrice() : BigDecimal.ZERO);
            c.setClose(m.getCurrentPrice() != null ? m.getCurrentPrice() : BigDecimal.ZERO);
            c.setVolume(0L);
            candles.add(c);
        }

        vo.setCandles(candles);
        return vo;
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private <T> List<T> safeList(List<T> list) {
        return list != null ? list : Collections.emptyList();
    }
}
