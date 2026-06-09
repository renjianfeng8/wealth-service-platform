import { readFileSync, existsSync } from 'fs';
import { join } from 'path';
import { fileURLToPath } from 'url';

const root = fileURLToPath(new URL('..', import.meta.url));
const componentPath = join(root, 'src/views/admin/dashboard/components/DashboardQuickEntries.vue');
const dashboardPath = join(root, 'src/views/admin/dashboard/index.vue');

function assert(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

assert(existsSync(componentPath), 'DashboardQuickEntries.vue should exist');

const component = readFileSync(componentPath, 'utf8');
const dashboard = readFileSync(dashboardPath, 'utf8');

assert(component.includes('useRouter'), 'DashboardQuickEntries should own route navigation');
assert(component.includes('fl-entry-grid'), 'DashboardQuickEntries should own entry grid markup');
assert(component.includes('/admin/message'), 'DashboardQuickEntries should include admin message entry');
assert(component.includes('@element-plus/icons-vue'), 'DashboardQuickEntries should own entry icons');
assert(dashboard.includes("import DashboardQuickEntries from './components/DashboardQuickEntries.vue'"), 'Dashboard should import DashboardQuickEntries');
assert(dashboard.includes('<DashboardQuickEntries />'), 'Dashboard should render DashboardQuickEntries');
assert(!dashboard.includes('<div class="fl-entry-grid">'), 'Dashboard should not keep inline entry grid markup');
assert(!dashboard.includes('const entries = ['), 'Dashboard should not own entry configuration');

console.log('Dashboard quick entries structure checks passed');
