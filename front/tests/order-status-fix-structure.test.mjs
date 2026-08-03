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
assert(tradeView.includes('撤销失败，请稍后重试'), 'handleCancel 撤销失败提示应为中立文案');

// --- useAdminDashboard.ts: 待成交统计应为 1 ---
assert(adminDashboard.includes('o.orderStatus === 1'), 'admin 待成交统计应计 status===1');

// --- admin/trade/index.vue: 状态映射合并为单一来源(从 types/format 导入) ---
assert(adminTrade.includes('ORDER_STATUS_OPTIONS'), 'admin 应复用 types 的 ORDER_STATUS_OPTIONS');
assert(adminTrade.includes('orderStatusText, orderStatusTag'), 'admin 应从 @/utils/format 导入 orderStatusText/orderStatusTag');
assert(!adminTrade.includes('const orderStatusOptions'), 'admin 不应再定义局部 orderStatusOptions');
assert(!adminTrade.includes('function orderStatusTag'), 'admin 不应再定义局部 orderStatusTag');

console.log('order-status off-by-one 修复结构检查通过');
