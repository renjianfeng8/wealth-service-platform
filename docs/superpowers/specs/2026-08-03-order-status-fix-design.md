# 订单状态 off-by-one 修复 — 设计规格

> 日期:2026-08-03 · 状态:待实施 · 范围:批次3 第 1 项

## 背景与根因

前端交易委托(orderStatus)状态字典使用 **0/1/2**,而后端枚举与数据库使用 **1/2/3**,整体左移一位,导致连锁错误:

- 状态标签错位:前端 `1=已成交` 实为后端 `1=已提交`;前端 `2=已撤销` 实为后端 `2=已成交`。
- 撤销按钮不可见:用户端 `v-if row.orderStatus === 0`(trade/index.vue:132),后端新建订单固定 `SUBMITTED=1`,几乎不产出 0。
- 撤销动作失效:前端 `cancelTradeOrder` 走通用 `PUT /{id}`(trade.ts:55-57),该端点 DTO 无 orderStatus 字段,`copyNonNullProperties` 不写入 → **静默 no-op**。
- admin 待成交统计恒 0:`useAdminDashboard.ts:54` `filter(o => o.orderStatus === 0)`。

**权威取值(三方一致)**:数据库 `init.sql:97` 注释 `1待委托 2已完成 3已撤销`;后端 `OrderStatusEnum.java:12-14` `SUBMITTED(1) MATCHED(2) CANCELLED(3)`,且状态机 `TradeOrderServiceImpl.java:115-133` 仅允许 `1→2`、`1→3`。

## 修复方案(纯前端,后端零改动)

后端枚举 + 状态机端点 `PUT /{id}/status` 已是正确权威,只需修正前端映射与撤销调用。不采用"给 TradeOrderDTO 加 orderStatus"(绕过状态机校验)或"后端枚举改 0 基"(与存量数据错位)。

## 改动清单(6 个前端文件)

| 文件 | 改动 |
|------|------|
| `front/src/utils/format.ts:37-45` | `orderStatusText` → `{1:'已提交',2:'已成交',3:'已撤销'}`;`orderStatusTag` → `{1:'warning',2:'success',3:'info'}` |
| `front/src/types/index.ts:140-144` | `ORDER_STATUS_OPTIONS` → `[{1,已提交},{2,已成交},{3,已撤销}]` |
| `front/src/api/trade.ts:55-57` | `cancelTradeOrder` → `PUT /trade/wea-trade-order/${id}/status` body `{orderStatus: 3}` |
| `front/src/views/trade/index.vue:132` | 撤销按钮 `v-if row.orderStatus === 0` → `=== 1` |
| `front/src/views/trade/index.vue:347-354` | `handleCancel` 拆分 catch:确认取消与 API 失败分开;失败 `ElMessage.error('撤销失败，请稍后重试')` 并刷新列表 |
| `front/src/composables/useAdminDashboard.ts:54` | pendingOrders 统计 `=== 0` → `=== 1` |
| `front/src/views/admin/trade/index.vue` | 合并重复映射:从 `@/types` 导入 `ORDER_STATUS_OPTIONS`、从 `@/utils/format` 导入 `orderStatusText`/`orderStatusTag`,删除局部 `orderStatusOptions` 与局部 `orderStatusText`/`orderStatusTag`;`filterFields` 改用 `ORDER_STATUS_OPTIONS`。消除 off-by-one 根源的多份拷贝 |

## 数据流

用户撤销 → 确认框 → `PUT /{id}/status {orderStatus:3}` → 后端查单(404)→ 校验 3 ∈ [1,3] → 校验当前状态 → 3 合法转换 → 更新落库 → 前端提示「已撤销」并刷新。

## 错误处理

- 状态机 400「非法状态转换」:仅在竞态(订单已被标记成交/撤销)时发生,不再被静默吞掉,前端提示用户并刷新列表。
- 按钮仅对 `orderStatus === 1`(已提交)显示,正常场景不会触发 400。

## 测试

- 后端:无改动,现有 `TradeOrderServiceImplTest`(断言新单 status=1)不受影响。
- 前端:现有 Playwright 结构测试 `latest-orders-panel-structure.test.mjs` 仅断言使用 `orderStatusTag`,不校验具体值,不受影响。
- 新增:轻量 Node 结构测试(`.test.mjs`,匹配项目现有风格),断言 `format.ts`、`types/index.ts` 中 orderStatus 映射为 1/2/3 且不含 0,`admin/trade/index.vue` 不再定义局部状态映射(改为从 types/format 导入),撤销失败提示为中立文案。

## 非目标

- 不改后端枚举/状态机/DB。
- 不清理其余闲置接口(批次3 后续项另行规划)。
- 不调整「已提交」之外的状态文案(用户已确认对齐后端枚举:1已提交 2已成交 3已撤销)。
