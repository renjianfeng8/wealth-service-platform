import { readFileSync, existsSync } from 'fs';
import { join } from 'path';
import { fileURLToPath } from 'url';

const root = fileURLToPath(new URL('..', import.meta.url));
const componentPath = join(root, 'src/views/admin/dashboard/components/MiniProductStrip.vue');
const dashboardPath = join(root, 'src/views/admin/dashboard/index.vue');

function assert(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

assert(existsSync(componentPath), 'MiniProductStrip.vue should exist');

const component = readFileSync(componentPath, 'utf8');
const dashboard = readFileSync(dashboardPath, 'utf8');

assert(component.includes('defineProps'), 'MiniProductStrip should define props');
assert(component.includes('products'), 'MiniProductStrip should accept products');
assert(component.includes('formatPrice'), 'MiniProductStrip should accept formatPrice');
assert(component.includes('formatRate'), 'MiniProductStrip should accept formatRate');
assert(component.includes('fl-mini-row'), 'MiniProductStrip should own mini card markup');
assert(dashboard.includes("import MiniProductStrip from './components/MiniProductStrip.vue'"), 'Dashboard should import MiniProductStrip');
assert(dashboard.includes('<MiniProductStrip :products="miniProducts" :format-price="formatPrice" :format-rate="formatRate" />'), 'Dashboard should render MiniProductStrip');
assert(!dashboard.includes('<div class="fl-mini-row">'), 'Dashboard should not keep inline mini product markup');

console.log('Mini product strip structure checks passed');
