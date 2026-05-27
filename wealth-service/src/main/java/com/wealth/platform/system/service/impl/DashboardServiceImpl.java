package com.wealth.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wealth.platform.product.entity.WeaMarketData;
import com.wealth.platform.product.mapper.FinMarketDataMapper;
import com.wealth.platform.product.service.FinMarketDataService;
import com.wealth.platform.system.service.DashboardService;
import com.wealth.platform.system.vo.DashboardKlineVO;
import com.wealth.platform.system.vo.DashboardOverviewVO;
import com.wealth.platform.system.vo.DashboardTrendVO;
import com.wealth.platform.trade.entity.WeaTradeOrder;
import com.wealth.platform.trade.service.FinTradeOrderService;
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

    private final FinMarketDataMapper finMarketDataMapper;
    private final FinMarketDataService finMarketDataService;
    private final FinTradeOrderService finTradeOrderService;

    @Override
    public DashboardOverviewVO getOverview() {
        DashboardOverviewVO vo = new DashboardOverviewVO();

        // 1. totalAsset = SUM(price * volume) from wea_market_data
        List<WeaMarketData> marketList = finMarketDataService.list();
        BigDecimal totalAsset = marketList.stream()
                .map(m -> {
                    BigDecimal price = m.getCurrentPrice() != null ? m.getCurrentPrice() : BigDecimal.ZERO;
                    return price.multiply(ESTIMATED_VOLUME);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTotalAsset(totalAsset);

        // 2. assetChange based on latest two market_time records
        if (marketList.size() >= 2) {
            List<WeaMarketData> sorted = marketList.stream()
                    .filter(m -> m.getMarketTime() != null)
                    .sorted(Comparator.comparing(WeaMarketData::getMarketTime).reversed())
                    .collect(Collectors.toList());
            if (sorted.size() >= 2) {
                BigDecimal latest = sorted.get(0).getCurrentPrice() != null ? sorted.get(0).getCurrentPrice() : BigDecimal.ZERO;
                BigDecimal prev = sorted.get(1).getCurrentPrice() != null ? sorted.get(1).getCurrentPrice() : BigDecimal.ZERO;
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
        } else {
            vo.setAssetChange(BigDecimal.ZERO);
        }

        // 3. balanceValue = SUM(entrust_price * entrust_num) from completed orders
        List<WeaTradeOrder> completedOrders = finTradeOrderService.lambdaQuery()
                .eq(WeaTradeOrder::getOrderStatus, 2)
                .list();
        BigDecimal balanceValue = completedOrders.stream()
                .map(o -> o.getEntrustPrice() != null && o.getEntrustNum() != null
                        ? o.getEntrustPrice().multiply(BigDecimal.valueOf(o.getEntrustNum()))
                        : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setBalanceValue(balanceValue);

        // 4. balanceChange
        if (completedOrders.size() >= 4) {
            List<WeaTradeOrder> sortedOrders = completedOrders.stream()
                    .filter(o -> o.getCreateTime() != null)
                    .sorted(Comparator.comparing(WeaTradeOrder::getCreateTime))
                    .collect(Collectors.toList());
            int mid = sortedOrders.size() / 2;
            BigDecimal firstHalf = sortedOrders.subList(0, mid).stream()
                    .map(o -> o.getEntrustPrice() != null && o.getEntrustNum() != null
                            ? o.getEntrustPrice().multiply(BigDecimal.valueOf(o.getEntrustNum()))
                            : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal secondHalf = sortedOrders.subList(mid, sortedOrders.size()).stream()
                    .map(o -> o.getEntrustPrice() != null && o.getEntrustNum() != null
                            ? o.getEntrustPrice().multiply(BigDecimal.valueOf(o.getEntrustNum()))
                            : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
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

        // 5. dailyIncome = SUM(entrust_price * entrust_num) from today's completed orders
        LocalDate today = LocalDate.now();
        BigDecimal dailyIncome = completedOrders.stream()
                .filter(o -> o.getCreateTime() != null && o.getCreateTime().toLocalDate().equals(today))
                .map(o -> o.getEntrustPrice() != null && o.getEntrustNum() != null
                        ? o.getEntrustPrice().multiply(BigDecimal.valueOf(o.getEntrustNum()))
                        : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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

        List<WeaTradeOrder> orders = finTradeOrderService.lambdaQuery()
                .eq(WeaTradeOrder::getOrderStatus, 2)
                .ge(WeaTradeOrder::getCreateTime, startDate.atStartOfDay())
                .le(WeaTradeOrder::getCreateTime, endDate.atTime(23, 59, 59))
                .list();

        Map<LocalDate, List<WeaTradeOrder>> orderByDate = orders.stream()
                .filter(o -> o.getCreateTime() != null)
                .collect(Collectors.groupingBy(o -> o.getCreateTime().toLocalDate()));

        // 资产趋势用相对指数（起始100），按日收益率累乘，避免纵坐标过大掩盖波动
        List<WeaMarketData> marketList = finMarketDataService.list();
        BigDecimal baseTotalAsset = marketList.stream()
                .filter(m -> m.getCurrentPrice() != null)
                .map(m -> m.getCurrentPrice().multiply(ESTIMATED_VOLUME))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        BigDecimal assetIndex = BigDecimal.valueOf(100);
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            List<WeaTradeOrder> dayOrders = orderByDate.getOrDefault(date, Collections.emptyList());
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

        List<WeaMarketData> records = finMarketDataMapper.selectList(
                new LambdaQueryWrapper<WeaMarketData>()
                        .eq(WeaMarketData::getProductCode, productCode)
                        .ge(WeaMarketData::getMarketTime, startTime)
                        .le(WeaMarketData::getMarketTime, endTime)
                        .orderByAsc(WeaMarketData::getMarketTime)
        );

        List<DashboardKlineVO.Candle> candles = new ArrayList<>();
        for (WeaMarketData m : records) {
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
}
