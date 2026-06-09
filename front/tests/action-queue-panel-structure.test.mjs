import { readFileSync, existsSync } from 'fs';
import { join } from 'path';
import { fileURLToPath } from 'url';

const root = fileURLToPath(new URL('..', import.meta.url));
const panelPath = join(root, 'src/views/admin/dashboard/components/ActionQueuePanel.vue');
const dashboardPath = join(root, 'src/views/admin/dashboard/index.vue');

function assert(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

assert(existsSync(panelPath), 'ActionQueuePanel.vue should exist');

const panel = readFileSync(panelPath, 'utf8');
const dashboard = readFileSync(dashboardPath, 'utf8');

assert(panel.includes('运营任务'), 'ActionQueuePanel should expose visible title');
assert(panel.includes('defineProps'), 'ActionQueuePanel should accept action items');
assert(panel.includes('action.done'), 'ActionQueuePanel should render done state');
assert(panel.includes('action.path'), 'ActionQueuePanel should support navigation path');
assert(dashboard.includes("import ActionQueuePanel from './components/ActionQueuePanel.vue'"), 'Dashboard should import ActionQueuePanel');
assert(dashboard.includes('<ActionQueuePanel :actions="actionQueue" />'), 'Dashboard should render ActionQueuePanel');
assert(dashboard.includes('const actionQueue = computed'), 'Dashboard should derive an action queue from existing data');
assert(dashboard.includes('fl-ops-row'), 'Dashboard should group latest orders and action queue in an operations row');

console.log('Action queue panel structure checks passed');
