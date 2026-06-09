import { readFileSync, existsSync } from 'fs';
import { join } from 'path';
import { fileURLToPath } from 'url';

const root = fileURLToPath(new URL('..', import.meta.url));
const headerPath = join(root, 'src/views/admin/dashboard/components/OperationsConsoleHeader.vue');
const dashboardPath = join(root, 'src/views/admin/dashboard/index.vue');

function assert(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

assert(existsSync(headerPath), 'OperationsConsoleHeader.vue should exist');

const header = readFileSync(headerPath, 'utf8');
const dashboard = readFileSync(dashboardPath, 'utf8');

assert(header.includes('运营控制台'), 'Header should expose visible operations console title');
assert(header.includes('全局运营状态'), 'Header should include visible status copy');
assert(header.includes('defineProps'), 'Header should accept dashboard metrics');
assert(header.includes('pendingOrders'), 'Header should show pending orders');
assert(header.includes('unreadMessages'), 'Header should show unread messages');
assert(header.includes('defineEmits'), 'Header should emit refresh');
assert(dashboard.includes("import OperationsConsoleHeader from './components/OperationsConsoleHeader.vue'"), 'Dashboard should import OperationsConsoleHeader');
assert(dashboard.includes('<OperationsConsoleHeader'), 'Dashboard should render OperationsConsoleHeader before metrics');
assert(dashboard.indexOf('<OperationsConsoleHeader') < dashboard.indexOf('<DashboardMetricGrid'), 'Header should render above metric grid');

console.log('Admin dashboard visible redesign structure checks passed');
