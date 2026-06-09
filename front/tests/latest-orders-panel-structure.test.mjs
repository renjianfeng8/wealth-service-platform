import { readFileSync, existsSync } from 'fs';
import { join } from 'path';
import { fileURLToPath } from 'url';

const root = fileURLToPath(new URL('..', import.meta.url));
const panelPath = join(root, 'src/views/admin/dashboard/components/LatestOrdersPanel.vue');
const composablePath = join(root, 'src/composables/useAdminDashboard.ts');
const dashboardPath = join(root, 'src/views/admin/dashboard/index.vue');

function assert(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

assert(existsSync(panelPath), 'LatestOrdersPanel.vue should exist');

const panel = readFileSync(panelPath, 'utf8');
const composable = readFileSync(composablePath, 'utf8');
const dashboard = readFileSync(dashboardPath, 'utf8');

assert(panel.includes('近期订单'), 'LatestOrdersPanel should expose visible title');
assert(panel.includes('el-table'), 'LatestOrdersPanel should render an Element Plus table');
assert(panel.includes('orderStatusTag'), 'LatestOrdersPanel should use status tag formatting');
assert(panel.includes('tradeTypeText'), 'LatestOrdersPanel should show trade type text');
assert(panel.includes('/admin/trade'), 'LatestOrdersPanel should link to trade management');
assert(composable.includes('getTradeOrderPage'), 'useAdminDashboard should load latest orders from existing trade page API');
assert(composable.includes('latestOrders'), 'useAdminDashboard should expose latestOrders state');
assert(composable.includes('loadLatestOrders'), 'useAdminDashboard should expose loadLatestOrders');
assert(dashboard.includes("import LatestOrdersPanel from './components/LatestOrdersPanel.vue'"), 'Dashboard should import LatestOrdersPanel');
assert(dashboard.includes('<LatestOrdersPanel :orders="latestOrders" />'), 'Dashboard should render LatestOrdersPanel');

console.log('Latest orders panel structure checks passed');
