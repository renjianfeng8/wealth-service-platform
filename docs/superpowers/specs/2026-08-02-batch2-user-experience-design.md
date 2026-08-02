# 第二批升级设计 — 用户端投资体验补全

- **日期**：2026-08-02
- **范围**：前端 6 项体验补全，**后端零改动**
- **验收**：逐项验证（每项完成跑前端构建 + 手动点验关键交互）

---

## 一、背景

依据「后端接口冗余审计报告」（30 个闲置接口），第一批已完成安全认证完善（identifyLogin 校验验证码 + 双 token + refresh/logout/captcha 前端接入 + 静默续期）。经复盘确认报告中 3 个接口（`/system/umsAdmin/refresh`、`/system/umsAdmin/logout`、`/system/captcha`）已被第一批接入，不再闲置；真正闲置者 27 个。

本批聚焦**用户端体验补全**：前端消费已有闲置 GET 接口、补全缺失交互。全部为 `front/src` 改动。

### 复盘关键结论
- 产品/资讯/消息详情弹窗**已存在**但均使用列表行数据（可能过期），未消费 `GET /{id}`。
- 行情页表格行**完全不可点击**，无产品级交互；`KlinePanel.vue` 组件已写好但**从未被挂载**。
- 交易页订单列表**无详情查看**，`GET /trade/wea-trade-order/{id}` 闲置。
- 自选卡片只有「交易」入口，无「详情」；首页/个人中心卡片点击仅跳列表页。
- `GET /system/dashboard/kline/{productCode}` 在 `AuthConstant.PERMISSION_BYPASS_URLS` 中（仅需登录，普通用户可访问），返回结构 `DashboardKlineVO.candles` 与前端 `Candle` 类型完全一致，可直接复用。

---

## 二、6 项改动明细

### A1 · 行情详情 + K 线弹窗（`front/src/views/market/index.vue`）
- **缺口**：行情表只读，行不可点击。
- **改动**：
  - 行情行点击 → 打开详情弹窗，展示当前价/涨跌/开高低收等 + K 线图。
  - 复用 `front/src/views/admin/dashboard/components/KlinePanel.vue`（props：`products`、`klineData`、`loadKline`，零新增图表代码）。
  - 新增 API：`getDashboardKline` 已在 `front/src/api/dashboard.ts` 定义，直接 import 使用。
- **激活接口**：`GET /system/dashboard/kline/{code}?period=1D|1W|1M`（需登录）。
- **注意**：KlinePanel 期望 `products: WeaProduct[]`；行情行数据为 `WeaMarketData`，需构造单产品数组（`productCode` + `productName`）传入，或抽取可复用包装。

### A2 · 委托单详情弹窗（`front/src/views/trade/index.vue`）
- **缺口**：订单列表只有「撤销」，无法看完整委托信息。
- **改动**：
  - 订单行点击 → 详情弹窗，展示 orderNo、方向、委托价、数量、委托金额、状态、下单时间。
  - 打开时 `getTradeOrderById(id)` 拉取最新数据（`front/src/api/trade.ts` 已定义）。
- **激活接口**：`GET /trade/wea-trade-order/{id}`。

### A3 · 产品详情升级为实时拉取（`front/src/views/products/index.vue`）
- **缺口**：详情弹窗使用列表行数据。
- **改动**：`showDetail(item)` 时 `getProductById(item.id)` 拉取最新数据并渲染，弹窗内增加 loading 态；字段与现有展示一致，数据源升级为权威实时。
- **激活接口**：`GET /product/wea-product/{id}`。

### A4 · 自选卡片 → 产品详情联动（`front/src/views/favorite/index.vue`）
- **缺口**：自选卡片只有「交易」，无法查看产品详情。
- **改动**：卡片新增「详情」按钮 → 打开产品详情弹窗（复用 A3 逻辑，建议将产品详情弹窗抽为共享组件，如 `front/src/components/ProductDetailDialog.vue`）。
- **激活接口**：`GET /product/wea-product/{id}`。

### A5 · 首页/个人中心卡片详情深链（`front/src/views/home/index.vue`、`front/src/views/dashboard/index.vue`）
- **缺口**：产品卡/资讯卡点击仅跳列表页。
- **改动**：产品卡点击打开产品详情弹窗；资讯卡点击打开资讯详情弹窗（复用共享组件）。
- **激活接口**：`GET /product/wea-product/{id}`、`GET /message/wea-news/{id}`。

### A6 · 资讯详情升级为实时拉取（`front/src/views/news/index.vue`）
- **缺口**：详情弹窗使用列表行数据。
- **改动**：`showDetail(item)` 时 `getNewsById(item.id)` 拉取全文并渲染，增加 loading 态。
- **激活接口**：`GET /message/wea-news/{id}`。

---

## 三、共享组件抽取（降低重复）

A3/A4/A5 均需产品详情弹窗，抽取共享组件：

- **新建** `front/src/components/ProductDetailDialog.vue`：props `productId`（或 `visible` + `productId`），内部 `getProductById` 拉取 + 收藏/去交易操作保留（沿用 `products/index.vue` 现有弹窗交互）。
- **新建** `front/src/components/NewsDetailDialog.vue`：props `newsId`，内部 `getNewsById` 拉取。
- A1 的 K 线弹窗若与 admin KlinePanel 复用面不足，允许为行情场景新建轻量 `MarketDetailDialog.vue` 内嵌 K 线图。

> 若抽取成本高于收益（如各页交互差异大），允许退化为各页独立实现，不强制共享 —— 以「三处以上重复才抽」为准。

---

## 四、不改动范围（明确排除）

- **后端任何文件**：零改动（含 `wealth-service` / `wealth-common`）。
- **订单状态 off-by-one bug**（撤销实际置为「已成交」）：属正确性修复方向，用户未纳入本批，记录在案留第三批。
- **死代码清理**（未引用 API 函数、孤儿组件）：未纳入本批。
- **管理端补全**（批量删除、admin 仪表盘、关联关系编辑）：未纳入本批。

---

## 五、验证清单（逐项）

每项完成后执行：
1. `cd front && npm run build`（类型检查 + 构建通过）。
2. `npx vite` 起前端，登录普通用户逐一点验：
   - A1：行情页点击行 → 弹窗显示 K 线，切换 1D/1W/1M 有数据。
   - A2：交易页点击订单行 → 详情字段正确。
   - A3：产品页点卡片 → 弹窗加载实时数据。
   - A4：自选页「详情」→ 弹出产品详情。
   - A5：首页/个人中心产品卡、资讯卡 → 直达详情。
   - A6：资讯页点条目 → 弹窗显示全文。
3. 回归：行情 SSE 实时刷新、产品收藏/去交易、消息已读 等原有交互不受影响。

---

## 六、风险与备注

- `GET /system/dashboard/kline` 的 `productName` 后端暂返回产品代码（`DashboardServiceImpl.getKline` 注释「待产品表联表后完善」），弹窗标题建议以列表行 `productName` 兜底。
- K 线数据依赖 `wea_market_data` 表按产品按时间区间取数；若某产品无历史行情，`candles` 为空，弹窗需空态提示。
- 各弹窗涉及未登录访问（`/products`、`/news` 为公共路由），`GET /{id}` 接口需要登录；未登录用户点详情需提示登录（弹窗可先渲染，接口 401 时走全局拦截器跳登录）。
