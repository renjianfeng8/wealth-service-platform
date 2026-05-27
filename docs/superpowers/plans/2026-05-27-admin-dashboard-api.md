# 管理后台仪表盘 API 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 管理后台仪表盘所有 Mock 数据替换为真实后端 API

**Architecture:** 后端 system 域新增 DashboardController + DashboardService，从 `wea_market_data` 和 `wea_trade_order` 表聚合计算真实数据。前端删除所有 mock generator，改为调用新 API。

**Tech Stack:** SpringBoot 3.3.13 + MyBatis-Plus + Vue 3 + ECharts

---

### Task 1: 后端 — VO 类

**Files:**
- Create: `wealth-service/src/main/java/com/wealth/platform/system/vo/DashboardOverviewVO.java`
- Create: `wealth-service/src/main/java/com/wealth/platform/system/vo/DashboardTrendVO.java`

- [ ] **Step 1: 创建 `DashboardOverviewVO.java`**

```java
package com.wealth.platform.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "仪表盘概况VO")
public class DashboardOverviewVO {

    @Schema(description = "资产总值（估算市值）")
    private BigDecimal totalAsset;

    @Schema(description = "资产变化率（%）")
    private BigDecimal assetChange;

    @Schema(description = "账户余额")
    private BigDecimal balanceValue;

    @Schema(description = "余额变化率（%）")
    private BigDecimal balanceChange;

    @Schema(description = "今日收益")
    private BigDecimal dailyIncome;

    @Schema(description = "日收益率（%）")
    private BigDecimal dailyIncomeRate;
}
```

- [ ] **Step 2: 创建 `DashboardTrendVO.java`**

```java
package com.wealth.platform.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "趋势图VO")
public class DashboardTrendVO {

    @Schema(description = "趋势数据点列表")
    private List<TrendPoint> series;

    @Data
    @Schema(description = "趋势数据点")
    public static class TrendPoint {
        @Schema(description = "日期")
        private String date;

        @Schema(description = "资产总值")
        private BigDecimal assetValue;

        @Schema(description = "余额")
        private BigDecimal balanceValue;

        @Schema(description = "收益")
        private BigDecimal income;
    }
}
```

- [ ] **Step 3: 创建 `DashboardKlineVO.java`**

```java
package com.wealth.platform.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "K线图VO")
public class DashboardKlineVO {

    @Schema(description = "产品代码")
    private String productCode;

    @Schema(description = "产品名称")
    private String productName;

    @Schema(description = "K线数据")
    private List<Candle> candles;

    @Data
    @Schema(description = "K线数据点")
    public static class Candle {
        @Schema(description = "时间")
        private String time;

        @Schema(description = "开盘价")
        private BigDecimal open;

        @Schema(description = "最高价")
        private BigDecimal high;

        @Schema(description = "最低价")
        private BigDecimal low;

        @Schema(description = "收盘价")
        private BigDecimal close;

        @Schema(description = "成交量")
        private Long volume;
    }
}
```

---

### Task 2: 后端 — Service 接口 + 实现

**Files:**
- Create: `wealth-service/src/main/java/com/wealth/platform/system/service/DashboardService.java`
- Create: `wealth-service/src/main/java/com/wealth/platform/system/service/impl/DashboardServiceImpl.java`

- [ ] **Step 1: 创建 `DashboardService.java`**

```java
package com.wealth.platform.system.service;

import com.wealth.platform.system.vo.DashboardKlineVO;
import com.wealth.platform.system.vo.DashboardOverviewVO;
import com.wealth.platform.system.vo.DashboardTrendVO;

public interface DashboardService {

    DashboardOverviewVO getOverview();

    DashboardTrendVO getTrend(String period);

    DashboardKlineVO getKline(String productCode, String period);
}
```

- [ ] **Step 2: 创建 `DashboardServiceImpl.java`**

```java
package com.wealth.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wealth.common.utils.BeanConvertUtil;
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
                    BigDecimal volume = BigDecimal.valueOf(1000); // 估算乘数，无实际volume时用固定值
                    return price.multiply(volume);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTotalAsset(totalAsset);

        // 2. assetChange: 对比最新两条market_time的记录
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

        // 3. balanceValue = SUM(deal_amount) from completed trade orders
        List<WeaTradeOrder> completedOrders = finTradeOrderService.lambdaQuery()
                .eq(WeaTradeOrder::getOrderStatus, 2) // 2 = 已成交
                .list();
        BigDecimal balanceValue = completedOrders.stream()
                .map(o -> o.getDealAmount() != null ? o.getDealAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setBalanceValue(balanceValue);

        // 4. balanceChange: 按创建时间分两半对比
        if (completedOrders.size() >= 4) {
            List<WeaTradeOrder> sortedOrders = completedOrders.stream()
                    .filter(o -> o.getCreateTime() != null)
                    .sorted(Comparator.comparing(WeaTradeOrder::getCreateTime))
                    .collect(Collectors.toList());
            int mid = sortedOrders.size() / 2;
            BigDecimal firstHalf = sortedOrders.subList(0, mid).stream()
                    .map(o -> o.getDealAmount() != null ? o.getDealAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal secondHalf = sortedOrders.subList(mid, sortedOrders.size()).stream()
                    .map(o -> o.getDealAmount() != null ? o.getDealAmount() : BigDecimal.ZERO)
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

        // 5. dailyIncome = SUM(deal_amount) from today's completed orders
        LocalDate today = LocalDate.now();
        BigDecimal dailyIncome = completedOrders.stream()
                .filter(o -> o.getCreateTime() != null && o.getCreateTime().toLocalDate().equals(today))
                .map(o -> o.getDealAmount() != null ? o.getDealAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setDailyIncome(dailyIncome);

        // 6. dailyIncomeRate = dailyIncome / totalAsset
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

        // 按周期确定天数
        int days = "30D".equals(period) ? 30 : 7;
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        // 获取已完成订单
        List<WeaTradeOrder> orders = finTradeOrderService.lambdaQuery()
                .eq(WeaTradeOrder::getOrderStatus, 2)
                .ge(WeaTradeOrder::getCreateTime, startDate.atStartOfDay())
                .le(WeaTradeOrder::getCreateTime, endDate.atTime(23, 59, 59))
                .list();

        // 按日期分组
        Map<LocalDate, List<WeaTradeOrder>> orderByDate = orders.stream()
                .filter(o -> o.getCreateTime() != null)
                .collect(Collectors.groupingBy(o -> o.getCreateTime().toLocalDate()));

        // 填充每日数据（无数据的日期填 0）
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            List<WeaTradeOrder> dayOrders = orderByDate.getOrDefault(date, Collections.emptyList());
            BigDecimal dayAmount = dayOrders.stream()
                    .map(o -> o.getDealAmount() != null ? o.getDealAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            DashboardTrendVO.TrendPoint point = new DashboardTrendVO.TrendPoint();
            point.setDate(date.format(fmt));
            point.setAssetValue(BigDecimal.ZERO); // 无日级市值数据，暂填 0
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
        List<DashboardKlineVO.Candle> candles = new ArrayList<>();

        // 查询该产品的行情记录
        List<WeaMarketData> records = finMarketDataMapper.selectList(
                new LambdaQueryWrapper<WeaMarketData>()
                        .eq(WeaMarketData::getProductCode, productCode)
                        .orderByAsc(WeaMarketData::getMarketTime)
        );

        if (records.isEmpty()) {
            vo.setProductCode(productCode);
            vo.setProductName("");
            vo.setCandles(candles);
            return vo;
        }

        vo.setProductCode(productCode);
        vo.setProductName(productCode); // productCode 作为 name 兜底

        for (WeaMarketData m : records) {
            DashboardKlineVO.Candle c = new DashboardKlineVO.Candle();
            c.setTime(m.getMarketTime() != null ? m.getMarketTime().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")) : "");
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
```

注意：`LambdaQueryWrapper` 的 `ge`/`le` 方法需要导入 `com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper`；`WeaMarketData` 的 `getMarketTime()` 返回 `LocalDateTime`。

- [ ] **Step 3: 验证编译通过**

Run: `cd /d/demo/wealth-service-platform && mvn compile -pl wealth-service -q 2>&1`
Expected: 无错误输出

---

### Task 3: 后端 — Controller

**Files:**
- Create: `wealth-service/src/main/java/com/wealth/platform/system/controller/DashboardController.java`

- [ ] **Step 1: 创建 `DashboardController.java`**

```java
package com.wealth.platform.system.controller;

import com.wealth.common.result.Result;
import com.wealth.platform.system.service.DashboardService;
import com.wealth.platform.system.vo.DashboardKlineVO;
import com.wealth.platform.system.vo.DashboardOverviewVO;
import com.wealth.platform.system.vo.DashboardTrendVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "仪表盘管理", description = "管理后台仪表盘数据接口")
@RequestMapping("/system/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "获取仪表盘概况（资产总值、账户余额、今日收益等）")
    @GetMapping("/overview")
    public Result<DashboardOverviewVO> overview() {
        return Result.success(dashboardService.getOverview());
    }

    @Operation(summary = "获取趋势图数据")
    @GetMapping("/trend")
    public Result<DashboardTrendVO> trend(
            @RequestParam(defaultValue = "7D") String period) {
        return Result.success(dashboardService.getTrend(period));
    }

    @Operation(summary = "获取K线图数据")
    @GetMapping("/kline/{productCode}")
    public Result<DashboardKlineVO> kline(
            @PathVariable String productCode,
            @RequestParam(defaultValue = "1D") String period) {
        return Result.success(dashboardService.getKline(productCode, period));
    }
}
```

- [ ] **Step 2: 验证编译通过**

Run: `cd /d/demo/wealth-service-platform && mvn compile -pl wealth-service -q 2>&1`
Expected: 无错误输出

---

### Task 4: 前端 — API 文件

**Files:**
- Create: `front/src/api/dashboard.ts`

- [ ] **Step 1: 创建 `dashboard.ts`**

```typescript
import request from '@/utils/request'

export interface DashboardOverview {
  totalAsset: number
  assetChange: number
  balanceValue: number
  balanceChange: number
  dailyIncome: number
  dailyIncomeRate: number
}

export interface TrendPoint {
  date: string
  assetValue: number
  balanceValue: number
  income: number
}

export interface DashboardTrend {
  series: TrendPoint[]
}

export interface Candle {
  time: string
  open: number
  high: number
  low: number
  close: number
  volume: number
}

export interface DashboardKline {
  productCode: string
  productName: string
  candles: Candle[]
}

export function getDashboardOverview() {
  return request.get<DashboardOverview>('/system/dashboard/overview')
}

export function getDashboardTrend(period: string) {
  return request.get<DashboardTrend>('/system/dashboard/trend', { params: { period } })
}

export function getDashboardKline(productCode: string, period: string) {
  return request.get<DashboardKline>(`/system/dashboard/kline/${productCode}`, { params: { period } })
}
```

- [ ] **Step 2: 验证 TypeScript 编译**

Run: `cd front && npx vue-tsc --noEmit 2>&1`
Expected: 无错误

---

### Task 5: 前端 — 仪表盘组件改造

**Files:**
- Modify: `front/src/views/admin/dashboard/index.vue`

**改动概述：**
1. `<script>` 顶部新增 `getDashboardOverview`、`getDashboardTrend`、`getDashboardKline` 导入
2. 删除 `generateTrendData()` 和 `generateCandleData()` 两个 mock 函数
3. 删除 `totalAsset`/`assetChange`/`balanceValue`/`balanceChange`/`dailyIncome`/`dailyIncomeRate` 六个 mock computed
4. 新增 `overview`、`trendData` ref 和 `fetchOverview()`、`fetchTrend()`、`fetchKline()` 方法
5. 图表构建函数从 API 数据读取而非 mock
6. `onMounted` 并行调用三个 fetch
7. `<template>` 中变量的引用路径不变（`totalAsset` → `overview.totalAsset` 等）

- [ ] **Step 1: 修改 `<script>` 部分——新增导入和 state**

在第 195 行后（`getProductList` 导入之后）添加：

```ts
import { getDashboardOverview, getDashboardTrend, getDashboardKline } from '@/api/dashboard'
import type { DashboardOverview, TrendPoint } from '@/api/dashboard'
```

在 `const products = ref<WeaProduct[]>([])` 之后添加：

```ts
const overview = ref<DashboardOverview>({
  totalAsset: 0, assetChange: 0, balanceValue: 0,
  balanceChange: 0, dailyIncome: 0, dailyIncomeRate: 0,
})
const trendData = ref<DashboardTrendVO | null>(null)
```

- [ ] **Step 2: 删除 6 个 mock computed**

删除第 251-265 行（`const totalAsset = computed(...)` 到 `const dailyIncomeRate = computed(...)`）

- [ ] **Step 3: 删除 2 个 mock generator 函数**

删除第 287-323 行（`generateTrendData` 和 `generateCandleData` 两个函数）

- [ ] **Step 4: 替换模板中的数据绑定**

模板中引用替换：
- `{{ formatNumber(totalAsset) }}` → `{{ formatNumber(overview.totalAsset) }}`
- `assetChange >= 0` → `overview.assetChange >= 0`
- `{{ Math.abs(assetChange).toFixed(2) }}` → `{{ Math.abs(overview.assetChange).toFixed(2) }}`
- `{{ formatNumber(balanceValue) }}` → `{{ formatNumber(overview.balanceValue) }}`
- `balanceChange >= 0` → `overview.balanceChange >= 0`
- `{{ Math.abs(balanceChange).toFixed(2) }}` → `{{ Math.abs(overview.balanceChange).toFixed(2) }}`
- `dailyIncome >= 0` → `overview.dailyIncome >= 0`
- `{{ formatNumber(dailyIncome) }}` → `{{ formatNumber(overview.dailyIncome) }}`
- `{{ Math.abs(dailyIncomeRate).toFixed(2) }}` → `{{ Math.abs(overview.dailyIncomeRate).toFixed(2) }}`

- [ ] **Step 5: 修改 `buildAssetOption` 函数**

从：

```ts
function buildAssetOption(): echarts.EChartsOption {
  const count = tmActive.value === '30m' ? 48 : ...
  const data = generateTrendData(totalAsset.value, count, 0.018, assetChange.value)
  ...
}
```

改为：

```ts
function buildAssetOption(): echarts.EChartsOption {
  const data = trendData.value?.series?.map(s => s.assetValue) ?? []
  const times = trendData.value?.series?.map(s => s.date) ?? []
  if (!data.length) return {}
  const isUp = data[data.length - 1] >= data[0]
  const color = isUp ? '#19be6b' : '#1a6dff'
  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255,255,255,0.96)',
      borderColor: '#e4e7ed', borderWidth: 1,
      textStyle: { color: '#303133', fontSize: 12 },
      formatter: (params: any) => {
        const p = params[0]
        return `<div style="font-size:12px;color:#909399;margin-bottom:4px">${p.axisValue}</div>
                <div style="font-size:14px;font-weight:600;color:#303133">¥${formatNumber(p.value)}</div>`
      },
    },
    grid: { left: 50, right: 16, top: 10, bottom: 24 },
    xAxis: { type: 'category', data: times, boundaryGap: false, ...lightAxis(), axisLabel: { color: '#909399', fontSize: 10, interval: Math.floor(data.length / 6) } },
    yAxis: { type: 'value', ...lightAxis(), axisLabel: { color: '#909399', fontSize: 10, formatter: (v: number) => v >= 10000 ? `${(v / 10000).toFixed(0)}万` : v.toFixed(0) }, splitNumber: 4 },
    series: [{ type: 'line', smooth: true, symbol: 'none', lineStyle: { color, width: 2 }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: isUp ? 'rgba(25,190,107,0.15)' : 'rgba(26,109,255,0.12)' }, { offset: 1, color: isUp ? 'rgba(25,190,107,0)' : 'rgba(26,109,255,0)' }]) }, data }],
  }
}
```

- [ ] **Step 6: 修改 `buildBalanceOption` 函数**

类似地，将 `generateTrendData` 调用替换为从 `trendData.value?.series` 读取 `balanceValue`：

```ts
function buildBalanceOption(): echarts.EChartsOption {
  const data = trendData.value?.series?.map(s => s.balanceValue) ?? []
  const times = trendData.value?.series?.map(s => s.date) ?? []
  if (!data.length) return {}
  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255,255,255,0.96)',
      borderColor: '#e4e7ed', borderWidth: 1,
      textStyle: { color: '#303133', fontSize: 12 },
      formatter: (params: any) => {
        const p = params[0]
        return `<div style="font-size:12px;color:#909399;margin-bottom:4px">${p.axisValue}</div>
                <div style="font-size:14px;font-weight:600;color:#f5a623">¥${formatNumber(p.value)}</div>`
      },
    },
    grid: { left: 50, right: 16, top: 10, bottom: 24 },
    xAxis: { type: 'category', data: times, boundaryGap: false, ...lightAxis(), axisLabel: { color: '#909399', fontSize: 10, interval: Math.floor(data.length / 4) } },
    yAxis: { type: 'value', ...lightAxis(), axisLabel: { color: '#909399', fontSize: 10, formatter: (v: number) => v >= 10000 ? `${(v / 10000).toFixed(0)}万` : v.toFixed(0) }, splitNumber: 3 },
    series: [{ type: 'line', smooth: true, symbol: 'none', lineStyle: { color: '#f5a623', width: 2 }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(245,166,35,0.12)' }, { offset: 1, color: 'rgba(245,166,35,0)' }]) }, data }],
  }
}
```

- [ ] **Step 7: 修改 `buildKlineOption` 函数**

从使用 `generateCandleData` 改为读取 API 返回的 candle 数据。添加一个 `klineData` ref。

声明：

```ts
const klineData = ref<Candle[]>([])
```

修改 `curSym` computed：当选中产品变化时，触发 kline 数据过滤。或者直接让 `buildKlineOption` 从 `klineData` 读取。

```ts
function buildKlineOption(): echarts.EChartsOption {
  const data = klineData.value
  if (!data.length) return {}
  const ohlc = data.map(d => [d.open, d.close, d.low, d.high])
  const times = data.map(d => d.time)
  const ma5 = data.map((_, i, arr) => {
    if (i < 4) return '-'
    return (arr.slice(i - 4, i + 1).reduce((s, d) => s + d.close, 0) / 5).toFixed(2)
  })
  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255,255,255,0.96)',
      borderColor: '#e4e7ed', borderWidth: 1,
      textStyle: { color: '#303133', fontSize: 12 },
      formatter: (params: any) => {
        const idx = params[0].dataIndex
        const d = data[idx]
        const c = d.close >= d.open ? '#19be6b' : '#ed4014'
        return `<div style="font-size:12px;color:#909399;margin-bottom:6px">${d.time}</div>
          <div style="display:flex;justify-content:space-between;gap:16px;margin:2px 0;font-size:12px">
            <span style="color:#909399">开盘</span><span style="color:#303133;font-weight:500">${d.open.toFixed(2)}</span>
          </div>
          <div style="display:flex;justify-content:space-between;gap:16px;margin:2px 0;font-size:12px">
            <span style="color:#909399">收盘</span><span style="color:${c};font-weight:600">${d.close.toFixed(2)}</span>
          </div>
          <div style="display:flex;justify-content:space-between;gap:16px;margin:2px 0;font-size:12px">
            <span style="color:#909399">最高</span><span style="color:#303133">${d.high.toFixed(2)}</span>
          </div>
          <div style="display:flex;justify-content:space-between;gap:16px;margin:2px 0;font-size:12px">
            <span style="color:#909399">最低</span><span style="color:#303133">${d.low.toFixed(2)}</span>
          </div>
          <div style="display:flex;justify-content:space-between;gap:16px;margin:2px 0;font-size:12px">
            <span style="color:#909399">成交量</span><span style="color:#303133">${d.volume.toLocaleString()}</span>
          </div>`
      },
    },
    grid: { left: 50, right: 16, top: 12, bottom: 28 },
    xAxis: { type: 'category', data: times, ...lightAxis(), axisLabel: { color: '#909399', fontSize: 10, interval: Math.floor(times.length / 6) } },
    yAxis: { type: 'value', scale: true, ...lightAxis(), axisLabel: { color: '#909399', fontSize: 10 }, splitNumber: 4 },
    dataZoom: [{ type: 'inside', start: 0, end: 100 }],
    series: [
      { type: 'candlestick', name: 'K线', data: ohlc, itemStyle: { color: '#19be6b', color0: '#ed4014', borderColor: '#19be6b', borderColor0: '#ed4014' } },
      { type: 'line', name: 'MA5', data: ma5, smooth: true, symbol: 'none', lineStyle: { color: '#f5a623', width: 1.5, opacity: 0.7 } },
    ],
  }
}
```

- [ ] **Step 8: 修改 `onMounted` 和新增 fetch 方法**

在 `fetchAll` 函数旁新增：

```ts
async function fetchOverview() {
  try {
    const res = await getDashboardOverview()
    if (res.data) overview.value = res.data
  } catch { /* ignore */ }
}

async function fetchTrend() {
  try {
    const res = await getDashboardTrend('7D')
    if (res.data) trendData.value = res.data
  } catch { /* ignore */ }
}

async function fetchKline(code: string) {
  try {
    const res = await getDashboardKline(code, '1D')
    if (res.data?.candles) klineData.value = res.data.candles
  } catch { /* ignore */ }
}
```

修改 `fetchAll` 为 `fetchData`，在获取产品列表后也获取 overview 和 trend：

```ts
async function fetchData() {
  loading.value = true
  try {
    const [prodRes] = await Promise.all([
      getProductList(),
      fetchOverview(),
      fetchTrend(),
    ])
    products.value = (prodRes.data || []) as WeaProduct[]
    // ... rest of logic
  } catch { /* ignore */ }
  finally {
    loading.value = false
    await nextTick()
    if (products.value.length && !symbolSel.value) symbolSel.value = products.value[0].productCode
    requestAnimationFrame(initAllCharts)
  }
}
```

修改 `watch(symbolSel, ...)`：

```ts
watch(symbolSel, (code) => {
  if (code) {
    fetchKline(code)
    nextTick(() => updateKlineChart())
  }
})
```

修改 `onMounted` 为：

```ts
onMounted(fetchData)
```

- [ ] **Step 9: 删除 CandleData 接口**

删除第 207 行的 `interface CandleData` 定义，因为前端现在从 `@/api/dashboard` 导入类型。

- [ ] **Step 10: 验证 TypeScript 编译**

Run: `cd front && npx vue-tsc --noEmit 2>&1`
Expected: 无错误

---

### 验证清单

- [ ] `mvn compile -pl wealth-service -q` 无错误
- [ ] `npx vue-tsc --noEmit` 无错误
- [ ] `GET /system/dashboard/overview` 返回正确 JSON
- [ ] `GET /system/dashboard/trend?period=7D` 返回趋势数据
- [ ] `GET /system/dashboard/kline/AU99?period=1D` 返回 K 线数据
- [ ] 前端仪表盘显示真实数据而非随机数
- [ ] 时间筛选按钮切换时图表更新
- [ ] 产品切换时 K 线图更新
- [ ] 无数据时不报错（API 返回 0 或空数组）
