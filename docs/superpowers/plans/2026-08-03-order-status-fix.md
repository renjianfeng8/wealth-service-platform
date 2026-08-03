# 订单状态 off-by-one 修复 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 修复前端订单状态字典 0/1/2 与后端 1/2/3 的 off-by-one,恢复撤销功能,修正撤销按钮可见性与 admin 待成交统计。

**Architecture:** 纯前端修复,后端零改动。后端枚举(`OrderStatusEnum`: 1=已提交 2=已成交 3=已撤销)与状态机端点 `PUT /{id}/status`(仅允许 1→2、1→3)已是正确权威。将前端三处状态字典改为 1/2/3,撤销动作改走状态机端点发 `orderStatus: 3`。

**Tech Stack:** Vue 3 + TypeScript + Element Plus;结构测试用 Node ESM(`.test.mjs`,匹配项目现有 `front/tests/` 风格)。

**规格:** `docs/superpowers/specs/2026-08-03-order-status-fix-design.md`

> **提交约束:** 按项目 CLAUDE.md,git commit/push 必须等用户明确指令。本计划各任务不自动提交;全部改完、测试通过后由用户决定统一提交。执行者不得执行 `git commit`/`git push`。

**测试命令(在 `front/` 目录下):**
```bash
node tests/order-status-fix-structure.test.mjs   # 结构测试
npx vue-tsc --noEmit                              # 类型检查
```

---

### Task 1: 编写失败的结构测试(Red)

**Files:**
- Create: `front/tests/order-status-fix-structure.test.mjs`

- [ ] **Step 1: 创建测试文件**

```js
import { readFileSync, existsSync } from 'fs';
import { join } from 'path';
import { fileURLToPath } from 'url';

const root = fileURLToPath(new URL('..', import.meta.url));

function assert(condition, message) {
  if (!condition) throw new Error(message);
}

function read(rel) {
  const p = join(root, rel);
  assert(existsSync(p), `${rel} 不存在`);
  return readFileSync(p, 'utf8');
}

const format = read('src/utils/format.ts');
const types = read('src/types/index.ts');
const tradeApi = read('src/api/trade.ts');
const tradeView = read('src/views/trade/index.vue');
const adminDashboard = read('src/composables/useAdminDashboard.ts');
const adminTrade = read('src/views/admin/trade/index.vue');

// --- format.ts: orderStatusText / orderStatusTag 必须为 1/2/3,不得含 0 ---
assert(format.includes("{ 1: '已提交', 2: '已成交', 3: '已撤销' }"), 'orderStatusText 应为 1/2/3(已提交/已成交/已撤销)');
assert(format.includes("{ 1: 'warning', 2: 'success', 3: 'info' }"), 'orderStatusTag 应为 1/2/3(warning/success/info)');
assert(!format.includes("{ 0: '待成交'"), 'orderStatusText 不应含 0 键(off-by-one 回归)');

// --- types/index.ts: ORDER_STATUS_OPTIONS 必须为 1/2/3 ---
assert(types.includes("{ label: '已提交', value: 1 }"), 'ORDER_STATUS_OPTIONS 应含 value:1(已提交)');
assert(types.includes("{ label: '已成交', value: 2 }"), 'ORDER_STATUS_OPTIONS 应含 value:2(已成交)');
assert(types.includes("{ label: '已撤销', value: 3 }"), 'ORDER_STATUS_OPTIONS 应含 value:3(已撤销)');
assert(!types.includes("{ label: '待成交', value: 0 }"), 'ORDER_STATUS_OPTIONS 不应含 value:0');

// --- api/trade.ts: cancelTradeOrder 必须走状态机端点且撤销值为 3 ---
assert(tradeApi.includes('/trade/wea-trade-order/${id}/status'), 'cancelTradeOrder 应调用状态机端点 /status');
assert(tradeApi.includes('orderStatus: 3'), 'cancelTradeOrder 撤销值应为 3');
assert(!tradeApi.includes('orderStatus: 2'), 'cancelTradeOrder 不应再发 2');

// --- trade/index.vue: 撤销按钮仅对已提交(1)可见,handleCancel 对 API 失败有提示 ---
assert(tradeView.includes('row.orderStatus === 1'), '撤销按钮应仅对 orderStatus===1 可见');
assert(tradeView.includes('撤销失败，订单可能已成交'), 'handleCancel 应对撤销失败给出提示');

// --- useAdminDashboard.ts: 待成交统计应为 1 ---
assert(adminDashboard.includes('o.orderStatus === 1'), 'admin 待成交统计应计 status===1');

// --- admin/trade/index.vue: orderStatusOptions 必须为 1/2/3 ---
assert(adminTrade.includes("{ label: '已提交', value: 1 }"), 'admin orderStatusOptions 应含 value:1');
assert(adminTrade.includes("{ label: '已成交', value: 2 }"), 'admin orderStatusOptions 应含 value:2');
assert(adminTrade.includes("{ label: '已撤销', value: 3 }"), 'admin orderStatusOptions 应含 value:3');
assert(!adminTrade.includes("{ label: '待成交', value: 0 }"), 'admin orderStatusOptions 不应含 value:0');

console.log('order-status off-by-one 修复结构检查通过');
```

- [ ] **Step 2: 运行确认失败**

Run: `node tests/order-status-fix-structure.test.mjs`
Expected: FAIL。当前代码仍是 0/1/2,第一个失败断言为 `orderStatusText 应为 1/2/3(已提交/已成交/已撤销)`,node 抛出 Error,退出码非 0。

---

### Task 2: 修正 format.ts 状态字典与标签

**Files:**
- Modify: `front/src/utils/format.ts:37-45`

- [ ] **Step 1: 修改 orderStatusText**

Old:
```ts
export function orderStatusText(status: number | undefined | null): string {
  const map: Record<number, string> = { 0: '待成交', 1: '已成交', 2: '已撤销' }
  return status !== null && status !== undefined ? map[status] || '-' : '-'
}
```
New:
```ts
export function orderStatusText(status: number | undefined | null): string {
  const map: Record<number, string> = { 1: '已提交', 2: '已成交', 3: '已撤销' }
  return status !== null && status !== undefined ? map[status] || '-' : '-'
}
```

- [ ] **Step 2: 修改 orderStatusTag**

Old:
```ts
export function orderStatusTag(status: number | undefined | null): string {
  const map: Record<number, string> = { 0: 'warning', 1: 'success', 2: 'info' }
  return status !== null && status !== undefined ? map[status] || '' : ''
}
```
New:
```ts
export function orderStatusTag(status: number | undefined | null): string {
  const map: Record<number, string> = { 1: 'warning', 2: 'success', 3: 'info' }
  return status !== null && status !== undefined ? map[status] || '' : ''
}
```

- [ ] **Step 3: 运行测试确认 format 断言通过**

Run: `node tests/order-status-fix-structure.test.mjs`
Expected: format.ts 三条断言(已提交/已成交/已撤销、warning/success/info、无 0 键)通过;其余文件断言仍失败(尚未修改)。

---

### Task 3: 修正 ORDER_STATUS_OPTIONS

**Files:**
- Modify: `front/src/types/index.ts:140-144`

- [ ] **Step 1: 修改状态选项**

Old:
```ts
export const ORDER_STATUS_OPTIONS: DictItem[] = [
  { label: '待成交', value: 0 },
  { label: '已成交', value: 1 },
  { label: '已撤销', value: 2 },
]
```
New:
```ts
export const ORDER_STATUS_OPTIONS: DictItem[] = [
  { label: '已提交', value: 1 },
  { label: '已成交', value: 2 },
  { label: '已撤销', value: 3 },
]
```

- [ ] **Step 2: 运行测试确认 types 断言通过**

Run: `node tests/order-status-fix-structure.test.mjs`
Expected: types 三条正向 + 一条负向断言通过;api/trade、trade/index.vue、useAdminDashboard、admin/trade 断言仍失败。

---

### Task 4: 修正 cancelTradeOrder 走状态机端点

**Files:**
- Modify: `front/src/api/trade.ts:55-57`

- [ ] **Step 1: 修改撤销调用**

Old:
```ts
export function cancelTradeOrder(id: number) {
  return request.put(`/trade/wea-trade-order/${id}`, { orderStatus: 2 })
}
```
New:
```ts
export function cancelTradeOrder(id: number) {
  return request.put(`/trade/wea-trade-order/${id}/status`, { orderStatus: 3 })
}
```

- [ ] **Step 2: 运行测试确认 api 断言通过**

Run: `node tests/order-status-fix-structure.test.mjs`
Expected: api/trade.ts 三条断言(走 /status、orderStatus: 3、无 orderStatus: 2)通过;trade/index.vue、useAdminDashboard、admin/trade 断言仍失败。

---

### Task 5: 修正用户端撤销按钮与 handleCancel

**Files:**
- Modify: `front/src/views/trade/index.vue:132`
- Modify: `front/src/views/trade/index.vue:347-354`

- [ ] **Step 1: 修改撤销按钮可见条件**

Old:
```html
                <el-button
                  v-if="row.orderStatus === 0"
                  text
                  type="danger"
                  size="small"
                  @click="handleCancel(row)"
                >
                  撤销
                </el-button>
```
New:
```html
                <el-button
                  v-if="row.orderStatus === 1"
                  text
                  type="danger"
                  size="small"
                  @click="handleCancel(row)"
                >
                  撤销
                </el-button>
```

- [ ] **Step 2: 拆分 handleCancel 的确认与 API 失败处理**

Old:
```ts
async function handleCancel(order: WeaTradeOrder) {
  try {
    await ElMessageBox.confirm('确定要撤销该委托单吗？', '确认', { type: 'warning' })
    await cancelTradeOrder(order.id!)
    ElMessage.success('已撤销')
    fetchOrders()
  } catch { /* cancelled */ }
}
```
New:
```ts
async function handleCancel(order: WeaTradeOrder) {
  try {
    await ElMessageBox.confirm('确定要撤销该委托单吗？', '确认', { type: 'warning' })
  } catch {
    return // 用户取消确认框
  }
  try {
    await cancelTradeOrder(order.id!)
    ElMessage.success('已撤销')
  } catch {
    ElMessage.error('撤销失败，请稍后重试')
  } finally {
    fetchOrders()
  }
}
```

- [ ] **Step 3: 运行测试确认 trade/index.vue 断言通过**

Run: `node tests/order-status-fix-structure.test.mjs`
Expected: trade/index.vue 两条断言(`row.orderStatus === 1`、`撤销失败，请稍后重试`)通过;useAdminDashboard、admin/trade 断言仍失败。

---

### Task 6: 修正 admin 待成交统计

**Files:**
- Modify: `front/src/composables/useAdminDashboard.ts:54`

- [ ] **Step 1: 修改统计条件**

Old:
```ts
    pendingOrders.value = latestOrders.value.filter(o => o.orderStatus === 0).length
```
New:
```ts
    pendingOrders.value = latestOrders.value.filter(o => o.orderStatus === 1).length
```

- [ ] **Step 2: 运行测试确认 useAdminDashboard 断言通过**

Run: `node tests/order-status-fix-structure.test.mjs`
Expected: useAdminDashboard 断言通过;仅 admin/trade 断言仍失败。

---

### Task 7: 合并 admin 交易页重复状态映射

> 代码质量审查(Important)要求消除多份状态映射拷贝(off-by-one 根源),用户已批准。此任务将 admin 页的局部映射改为从单一来源导入。

**Files:**
- Modify: `front/src/views/admin/trade/index.vue`(新增导入 + 删除局部映射 + filterFields 改引用)
- Test: `front/tests/order-status-fix-structure.test.mjs`(admin 段改为单一来源断言)

- [ ] **Step 1: 更新结构测试 admin 断言为「单一来源」目标(红)**

将测试文件 admin/trade 段(现有 orderStatusOptions + 局部 orderStatusTag 断言)整体替换为:

```js
// --- admin/trade/index.vue: 状态映射合并为单一来源(从 types/format 导入) ---
assert(adminTrade.includes('ORDER_STATUS_OPTIONS'), 'admin 应复用 types 的 ORDER_STATUS_OPTIONS');
assert(adminTrade.includes('orderStatusText, orderStatusTag'), 'admin 应从 @/utils/format 导入 orderStatusText/orderStatusTag');
assert(!adminTrade.includes('const orderStatusOptions'), 'admin 不应再定义局部 orderStatusOptions');
assert(!adminTrade.includes('function orderStatusTag'), 'admin 不应再定义局部 orderStatusTag');
```

同时把撤销失败提示断言改为 `tradeView.includes('撤销失败，请稍后重试')`。

运行 `node tests/order-status-fix-structure.test.mjs` → 预期红(如 `handleCancel 撤销失败提示应为中立文案` 或 admin 单一来源断言)。

- [ ] **Step 2: 合并 admin 页映射(绿)**

在 `front/src/views/admin/trade/index.vue`:
- 在 `@/api/trade` import 后新增 `import { ORDER_STATUS_OPTIONS } from '@/types'` 与 `import { orderStatusText, orderStatusTag } from '@/utils/format'`
- 删除局部 `orderStatusOptions`(原 113-117)
- `filterFields` 中 `options: orderStatusOptions` → `options: ORDER_STATUS_OPTIONS`
- 删除局部 `orderStatusText`、`orderStatusTag` 函数(原 228-236)

不触碰局部 `tradeTypeText`/`formatPriceColumn`/`formatDateColumn`(预先存在,超范围)。

运行 `node tests/order-status-fix-structure.test.mjs` → 绿,输出 `order-status off-by-one 修复结构检查通过`。

---

### Task 8: 全量验证

- [ ] **Step 1: 结构测试**

Run: `node tests/order-status-fix-structure.test.mjs`
Expected: PASS(如上)。

- [ ] **Step 2: 类型检查**

Run: `npx vue-tsc --noEmit`
Expected: 无输出、退出码 0(无类型错误)。

- [ ] **Step 3: 汇报改动清单并等待提交指令**

向后端/主控汇报:改动 6 个前端文件(见各 Task 的 Files),后端零改动。**不执行 git commit/push**,等待用户明确指令后统一提交。

---

## 自检记录

- **规格覆盖**:spec 改动清单 6 文件(7 行)全部映射到 Task 2-7;数据流/错误处理在 Task 4、5 落地;测试要求(spec「新增轻量 Node 结构测试」)在 Task 1 落地,Task 8 全量验证。✔
- **占位符**:无 TBD/TODO,所有步骤含完整代码。✔
- **类型/命名一致**:`orderStatusText`/`orderStatusTag`/`ORDER_STATUS_OPTIONS`/`orderStatusOptions`/`cancelTradeOrder`/`handleCancel`/`pendingOrders` 在各 Task 中命名一致;`/status` 端点与 `orderStatus: 3` 在 Task 1 断言与 Task 4 实现一致。✔
- **提交约束**:按 CLAUDE.md 不自动 commit,Task 8 Step 3 明确等待用户指令。✔
