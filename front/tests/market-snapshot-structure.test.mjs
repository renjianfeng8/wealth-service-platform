import { readFileSync, existsSync } from 'fs';
import { join } from 'path';
import { fileURLToPath } from 'url';

const root = fileURLToPath(new URL('..', import.meta.url));
const componentPath = join(root, 'src/views/admin/dashboard/components/MarketSnapshot.vue');
const dashboardPath = join(root, 'src/views/admin/dashboard/index.vue');

function assert(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

assert(existsSync(componentPath), 'MarketSnapshot.vue should exist');

const component = readFileSync(componentPath, 'utf8');
const dashboard = readFileSync(dashboardPath, 'utf8');

assert(component.includes('defineProps'), 'MarketSnapshot should define props');
assert(component.includes('products'), 'MarketSnapshot should accept products');
assert(component.includes('formatPrice'), 'MarketSnapshot should accept formatPrice');
assert(component.includes('formatRate'), 'MarketSnapshot should accept formatRate');
assert(component.includes('filteredList'), 'MarketSnapshot should own filteredList');
assert(component.includes('mktFilter'), 'MarketSnapshot should own mktFilter');
assert(dashboard.includes("import MarketSnapshot from './components/MarketSnapshot.vue'"), 'Dashboard should import MarketSnapshot');
assert(dashboard.includes('<MarketSnapshot :products="products" :format-price="formatPrice" :format-rate="formatRate" />'), 'Dashboard should render MarketSnapshot');
assert(!dashboard.includes('const mktFilter = ref'), 'Dashboard should not own mktFilter');
assert(!dashboard.includes('const filteredList = computed'), 'Dashboard should not own filteredList');
assert(!dashboard.includes('class="fl-card fl-card-list"'), 'Dashboard should not keep inline market list card');

console.log('Market snapshot structure checks passed');
