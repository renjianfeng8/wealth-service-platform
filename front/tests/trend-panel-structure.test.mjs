import { readFileSync, existsSync } from 'fs';
import { join } from 'path';
import { fileURLToPath } from 'url';

const root = fileURLToPath(new URL('..', import.meta.url));
const panelPath = join(root, 'src/views/admin/dashboard/components/TrendPanel.vue');
const dashboardPath = join(root, 'src/views/admin/dashboard/index.vue');

function assert(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

assert(existsSync(panelPath), 'TrendPanel.vue should exist');

const panel = readFileSync(panelPath, 'utf8');
const dashboard = readFileSync(dashboardPath, 'utf8');

assert(panel.includes('import * as echarts'), 'TrendPanel should own ECharts');
assert(panel.includes('assetChartRef'), 'TrendPanel should own asset chart ref');
assert(panel.includes('balanceChartRef'), 'TrendPanel should own balance chart ref');
assert(panel.includes('loadTrend'), 'TrendPanel should accept loadTrend callback');
assert(panel.includes('formatNumber'), 'TrendPanel should accept formatNumber callback');
assert(panel.includes('onUnmounted'), 'TrendPanel should dispose charts on unmount');
assert(dashboard.includes("import TrendPanel from './components/TrendPanel.vue'"), 'Dashboard should import TrendPanel');
assert(dashboard.includes('<TrendPanel :trend-data="trendData" :load-trend="loadTrend" :format-number="formatNumber" />'), 'Dashboard should render TrendPanel');
assert(!dashboard.includes('ref="assetChartRef"'), 'Dashboard should not keep asset chart DOM');
assert(!dashboard.includes('ref="balanceChartRef"'), 'Dashboard should not keep balance chart DOM');

console.log('Trend panel structure checks passed');
