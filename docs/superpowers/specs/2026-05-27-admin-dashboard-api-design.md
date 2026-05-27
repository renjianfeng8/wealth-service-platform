# 管理后台仪表盘 — 替换 Mock 数据为真实 API

## 背景

管理后台仪表盘 (`/admin/dashboard`) 当前仅产品列表来源于真实 API，其余 6 个卡片指标（资产总值、账户余额、今日收益等）和 3 张图表（资产趋势、余额走势、K线）全部是前端 `Math.random()` 生成的 Mock 数据。

## 方案

保留现有卡片文案和图表布局，后端从数据库表聚合计算真实数据，前端删除所有 mock generator，改为调用新增的 API。

## 后端

### 模块结构

```
com.wealth.platform.system
  ├── controller/DashboardController.java
  ├── service/DashboardService.java
  ├── service/impl/DashboardServiceImpl.java
  └── vo/
      ├── DashboardOverviewVO.java
      └── DashboardTrendVO.java
```

### 接口定义

#### ① 仪表盘概况

```
GET /system/dashboard/overview
```

返回：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalAsset": 13824567.89,
    "assetChange": 2.35,
    "balanceValue": 5862300.00,
    "balanceChange": 1.28,
    "dailyIncome": 125600.00,
    "dailyIncomeRate": 0.91
  }
}
```

计算逻辑：

| 字段 | 来源 |
|------|------|
| `totalAsset` | `SELECT COALESCE(SUM(price * volume), 0) FROM wea_market_data WHERE del_flag = 0` |
| `assetChange` | `(当前totalAsset - 上一周期totalAsset) / 上一周期totalAsset * 100`（从 `wea_market_data` 按时间分组计算，无历史数据时返回 0） |
| `balanceValue` | `SELECT COALESCE(SUM(deal_amount), 0) FROM wea_trade_order WHERE order_status = 2 AND del_flag = 0` |
| `balanceChange` | `(本周期变化额 / 上周期余额) * 100`（按 30 天为周期） |
| `dailyIncome` | `SELECT COALESCE(SUM(deal_amount), 0) FROM wea_trade_order WHERE order_status = 2 AND del_flag = 0 AND create_time >= CURDATE()` |
| `dailyIncomeRate` | `dailyIncome / totalAsset`（totalAsset 为 0 时返回 0） |

#### ② 趋势图数据

```
GET /system/dashboard/trend?period=7D
```

参数：`period` — `7D`（默认）、`30D`

返回：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "series": [
      { "date": "2026-05-20", "assetValue": 13500000.00, "balanceValue": 5600000.00, "income": 110000.00 },
      { "date": "2026-05-21", "assetValue": 13620000.00, "balanceValue": 5650000.00, "income": 118000.00 }
    ]
  }
}
```

计算逻辑：从 `wea_market_data` 和 `wea_trade_order` 按 `create_time` 日期分组聚合。

#### ③ K线图数据

```
GET /system/dashboard/kline/{productCode}?period=1D
```

参数：`period` — `1D`（默认）、`1W`、`1M`

返回：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "productCode": "AU99",
    "productName": "黄金99",
    "candles": [
      { "time": "09:30", "open": 385.20, "high": 386.50, "low": 384.80, "close": 386.10, "volume": 12500 }
    ]
  }
}
```

计算逻辑：从 `wea_market_data` 直接读取对应 `product_code` 的记录，按时间排序。

### 现有接口不变

- `GET /product/wea-product` — 产品列表（产品涨跌卡 + 行情列表仍使用此接口）

## 前端

### 新增文件

`front/src/api/dashboard.ts` — 3 个 API 方法。

### 修改文件

`front/src/views/admin/dashboard/index.vue`：

| 改动项 | 说明 |
|--------|------|
| 删除 `generateTrendData()` | 不再需要 Mock 趋势数据生成器 |
| 删除 `generateCandleData()` | 不再需要 Mock K线数据生成器 |
| 删除 `totalAsset` / `assetChange` / `balanceValue` / `balanceChange` / `dailyIncome` / `dailyIncomeRate` 六个 computed | 改为从 API 返回的 `overview` 对象读取 |
| 新增 `fetchOverview()` | 调用 `/system/dashboard/overview`，更新 `overview` ref |
| 新增 `fetchTrend()` | 调用 `/system/dashboard/trend`，更新趋势图 |
| 新增 `fetchKline(code)` | 调用 `/system/dashboard/kline/{code}`，更新 K线图 |
| 资产趋势图/余额走势图渲染 | `buildAssetOption()` / `buildBalanceOption()` 从 API 数据读取而非 mock |
| K线图渲染 | `buildKlineOption()` 从 API 数据读取而非 mock |
| `onMounted` | 并行调用 `fetchOverview()` + `fetchTrend()` + `fetchProducts()` |
| `symbolSel` 切换 | `watch` 中触发 `fetchKline()` 而非 `updateKlineChart()` |

### 新增依赖

无（`echarts` 和 `@/api/product` 已是现有依赖）。

## 不涉及

- 路由、权限
- 数据迁移或表结构变更
- 第三方依赖

## 工作量估算

| 文件 | 操作 | 行数 |
|------|------|------|
| `DashboardServiceImpl.java` | 新增 | ~100 行 |
| `DashboardService.java` | 新增 | ~10 行 |
| `DashboardController.java` | 新增 | ~40 行 |
| `DashboardOverviewVO.java` | 新增 | ~30 行 |
| `DashboardTrendVO.java` | 新增 | ~20 行 |
| `front/src/api/dashboard.ts` | 新增 | ~15 行 |
| `front/src/views/admin/dashboard/index.vue` | 修改 | ~60 行（删除 mock + 替换 API 调用） |
