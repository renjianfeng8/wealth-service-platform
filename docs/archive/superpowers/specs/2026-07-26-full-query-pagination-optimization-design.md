# 全量查询分页优化 — 设计文档

## 概述

将前端 4 处全量列表查询替换为已有分页接口，避免数据增长后页面加载变慢。后端不动，纯前端改造。

## 变更清单

| # | 文件 | 当前调用 | 替换为 | pageSize | 原因 |
|---|------|---------|--------|----------|------|
| 1 | `front/src/views/home/index.vue:135` | `getMarketDataList()` | `getMarketDataPage()` | 4 | 首页只展示 4 张行情卡片 |
| 2 | `front/src/views/dashboard/index.vue:128` | `getProductList()` | `getProductPage()` | 8 | hotProducts 取前 8 条，统计指标基于当页数据 |
| 3 | `front/src/composables/useAdminDashboard.ts:50` | `getProductList()` | `getProductPage()` | 200 | 覆盖 demo 全部产品，ActionQueue 计数无实质差异 |
| 4 | `front/src/views/products/index.vue:237` | `getFavoriteList()` | `getFavoritePage()` | 200 | 普通用户收藏数远少于此值 |

## 改后行为

### 1. 首页行情 — `home/index.vue`

- 导入: `getMarketDataList` → `getMarketDataPage`
- 调用: `getMarketDataPage({ pageNum: 1, pageSize: 4 })`
- 结果: 与改造前展示一致（原本就只展示前 N 条的视觉，由数据库表数量决定）

### 2. 用户仪表盘 — `dashboard/index.vue`

- 导入: `getProductList` → `getProductPage`
- 调用: `getProductPage({ pageNum: 1, pageSize: 8 })`
- 统计指标变化:
  - 产品总数 → 从 `products.value.length` 变为当前页返回的记录数（最多 8 条）
  - 上涨/下跌/在售数 → 基于当前页计算
- hotProducts: 不再需要 `slice(0, 8)`，products 本身就是 8 条
- 背景: 这个仪表盘本身是概览性质，基于当页的近似统计足够反映市场概况

### 3. 管理端仪表盘 — `useAdminDashboard.ts`

- 导入: `getProductList` → `getProductPage`
- 调用: `getProductPage({ pageNum: 1, pageSize: 200 })`
- actionQueue 计数: 下跌产品数/禁用产品数基于取到的产品计算
- 后端已限制 pageSize 上限，200 不会触发性能问题
- 传递给 KlinePanel/MarketSnapshot 的产品列表在 Demo 环境可覆盖全部数据

### 4. 产品页收藏 — `products/index.vue`

- 导入: `getFavoriteList` → `getFavoritePage`
- 调用: `getFavoritePage({ pageNum: 1, pageSize: 200, userId })`
- 响应结构: `getFavoritePage` 返回 `{records, total}`，从 `records` 构建 `favoritedMap`
- 背景: 普通用户收藏通常 < 50 条，200 的 pageSize 足够覆盖

## 未改动

- 后端代码
- 页面模板、样式、功能逻辑

## 风险

- 用户仪表盘统计值改为基于当前页计算，数据量大时与真实值有偏差。Demo 场景下可接受。
