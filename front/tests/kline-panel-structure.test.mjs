import { readFileSync, existsSync } from 'fs';
import { join } from 'path';
import { fileURLToPath } from 'url';

const root = fileURLToPath(new URL('..', import.meta.url));
const panelPath = join(root, 'src/views/admin/dashboard/components/KlinePanel.vue');
const dashboardPath = join(root, 'src/views/admin/dashboard/index.vue');

function assert(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

assert(existsSync(panelPath), 'KlinePanel.vue should exist');

const panel = readFileSync(panelPath, 'utf8');
const dashboard = readFileSync(dashboardPath, 'utf8');

assert(panel.includes('import * as echarts'), 'KlinePanel should own ECharts');
assert(panel.includes('klineChartRef'), 'KlinePanel should own K-line chart ref');
assert(panel.includes('loadKline'), 'KlinePanel should accept loadKline callback');
assert(panel.includes('symbolSel'), 'KlinePanel should own symbol selection');
assert(panel.includes('klineRanges'), 'KlinePanel should own range controls');
assert(panel.includes('onUnmounted'), 'KlinePanel should dispose chart on unmount');
assert(dashboard.includes("import KlinePanel from './components/KlinePanel.vue'"), 'Dashboard should import KlinePanel');
assert(dashboard.includes('<KlinePanel :products="products" :kline-data="klineData" :load-kline="loadKline" />'), 'Dashboard should render KlinePanel');
assert(!dashboard.includes('ref="klineChartRef"'), 'Dashboard should not keep K-line chart DOM');
assert(!dashboard.includes('const klineChartRef'), 'Dashboard should not own K-line chart ref');
assert(!dashboard.includes('import * as echarts'), 'Dashboard should not import ECharts after extraction');

console.log('K-line panel structure checks passed');
