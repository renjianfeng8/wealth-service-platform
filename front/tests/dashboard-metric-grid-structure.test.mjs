import { readFileSync, existsSync } from 'fs';
import { join } from 'path';
import { fileURLToPath } from 'url';

const root = fileURLToPath(new URL('..', import.meta.url));
const componentPath = join(root, 'src/views/admin/dashboard/components/DashboardMetricGrid.vue');
const dashboardPath = join(root, 'src/views/admin/dashboard/index.vue');

function assert(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

assert(existsSync(componentPath), 'DashboardMetricGrid.vue should exist');

const component = readFileSync(componentPath, 'utf8');
const dashboard = readFileSync(dashboardPath, 'utf8');

assert(component.includes('defineProps'), 'DashboardMetricGrid should define props');
assert(component.includes('overview'), 'DashboardMetricGrid should accept overview');
assert(component.includes('formatNumber'), 'DashboardMetricGrid should accept formatNumber');
assert(component.includes('fl-stats-row'), 'DashboardMetricGrid should own stats row markup');
assert(dashboard.includes("import DashboardMetricGrid from './components/DashboardMetricGrid.vue'"), 'Dashboard should import DashboardMetricGrid');
assert(dashboard.includes('<DashboardMetricGrid :overview="overview" :format-number="formatNumber" />'), 'Dashboard should render DashboardMetricGrid');
assert(!dashboard.includes('<div class="fl-stats-row">'), 'Dashboard should not keep inline stats row markup');

console.log('Dashboard metric grid structure checks passed');
