# 前后端接口兼容性修复设计

> 日期：2026-08-03
> 依据：《前后端接口兼容性专项审计报告》（JIEKOU.md）
> 范围：全部 10 项（H1/H2/M1/M2/M3/L1~L5）
> 已确认决策：
> 1. 日期统一 `yyyy-MM-dd HH:mm:ss`（后端全局 + 前端录入对齐）
> 2. H2 采用「拦截器返回 `res.data`」方案，API 类型收口
> 3. L4 做代码改动（VO id → String 序列化 + 前端类型放宽）

---

## 一、高危

### H1 + M3：日期格式统一（同一根因）

- 后端 `wealth-common/.../config/JacksonConfig.java`：注册全局 `LocalDateTimeSerializer` / `LocalDateTimeDeserializer`。
  - 序列化格式 `yyyy-MM-dd HH:mm:ss`。
  - 反序列化：空串 → `null`（根治空串 400）；优先主格式解析，兜底兼容 ISO-8601（带 `T`）。
- 前端 `el-date-picker` 的 `value-format` 改 `YYYY-MM-DD HH:mm:ss`：
  - `front/src/views/admin/market/index.vue:103`
  - `front/src/views/admin/news/index.vue:79`
- 展示层 `formatDateTime` 已是空格格式，无需改。

### H2：拦截器返回 `res.data` + API 类型收口

- `front/src/api/index.ts`：
  - 响应拦截器成功分支 `return res.data`（401 判定 / 续期重放 / 错误提示逻辑保留）。
  - 导出 `request` 类型改为自定义 `ApiClient` 接口（`get/post/put/delete<T>` 返回 `Promise<T>`）。
  - `tryRefresh` / `logoutApi` 使用裸 `axios`，保持 `AxiosResponse` 语义，不受影响。
- 全量调用点 `res.data.xxx` → `res.xxx`（约 30 处：views、`composables/useAdminDashboard.ts`、`components/CaptchaField.vue`、`components/ProductDetailDialog.vue`、`components/MessageNoticePopover.vue`、`layouts/Navbar.vue`）。
- 验证：`vue-tsc` 兜底无遗漏。

---

## 二、中风险

### M1：分页响应命名统一 `pageNum/pageSize`

- 后端新增 `wealth-common/.../config/PageSerializer.java`（`@JsonComponent` 自动注册）：`Page` 输出 `{records, total, pageNum, pageSize, pages}`，替换默认 `current/size`。已核实 `BeanConvertUtil.convertPage` 运行时返回 `Page`。
- 前端 `front/src/types/index.ts` `PageResult` 改为 `{records, total, pageNum, pageSize, pages}`；调用点只用 `records/total`，零破坏。

### M2：涨跌幅精度对齐 `DECIMAL(8,4)`

- `init.sql`：`wea_product`、`wea_market_data` 两处 `rise_fall_rate DECIMAL(5,2)` → `DECIMAL(8,4)`；附 `ALTER TABLE` 语句。
- `MarketDataSimulationService.java`：`riseFallRate` 计算 `setScale(6)` → `setScale(4)`。
- `MarketDataDTO` / `ProductDTO` / 对应实体：`riseFallRate` 统一 `setScale(4)`。
- 前端 `precision=4` 已对齐，无需改。
- 三方对齐：`init.sql` + 实体 + DTO。

---

## 三、低风险

- **L1**：`com.wealth.common.dto.UserFavoriteDTO` 重命名 → `UserFavoriteProviderDTO`（common 模块当前无引用；wealth-service 均引用 product 包同名类，互不干扰）。
- **L2**：`BatchReadDTO.ids` 加 `@NotEmpty`；`MessageController.batchMarkAsRead` 改 `@Valid @RequestBody`，删手写判空。
- **L3**：SSE 例外文档化——`MarketDataController` `@Operation` 标注裸数组载荷；`front/src/store/marketSSE.ts` 注释；`docs/ARCHITECTURE.md` 补一行。
- **L4**：12 个响应 VO 的 `id` 字段加 `@JsonSerialize(using = ToStringSerializer.class)`（UmsAdminVO/UmsRoleVO/UmsResourceVO/UmsAdminRoleRelationVO/UmsRoleResourceRelationVO/UserVO/MessageVO/NewsVO/MarketDataVO/ProductVO/UserFavoriteVO/TradeOrderVO）及 `LoginVO.userId`；前端 `id` 类型放宽 `number | string`。**不做全局 Long→String**（避免误伤 total/count）。
- **L5**：`admin/market/index.vue` label 改「涨跌幅（小数，如 0.05=5%）」；`MarketDataDTO` / `ProductDTO` 的 `riseFallRate` 加 `@DecimalMin("-1")` / `@DecimalMax("1")` 与中文提示。

---

## 验证

- 前端：`vue-tsc` + `vite build`；回归 admin 行情/资讯新增编辑（空日期、有日期两路径）。
- 后端：`mvn clean install -pl wealth-common -DskipTests` → 全量编译；启动冒烟验证 LocalDateTime 出入参、分页响应字段、id 字符串化。
- 敏感项：`init.sql`（M2，需审批）、响应契约（M1 pageNum/pageSize、L4 id 转字符串）。
