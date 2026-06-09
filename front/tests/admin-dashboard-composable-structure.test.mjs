import { readFileSync, existsSync } from 'fs';
import { join } from 'path';
import { fileURLToPath } from 'url';

const root = fileURLToPath(new URL('..', import.meta.url));
const composablePath = join(root, 'src/composables/useAdminDashboard.ts');
const dashboardPath = join(root, 'src/views/admin/dashboard/index.vue');

function assert(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

assert(existsSync(composablePath), 'useAdminDashboard.ts should exist');

const composable = readFileSync(composablePath, 'utf8');
const dashboard = readFileSync(dashboardPath, 'utf8');

assert(composable.includes('export function useAdminDashboard'), 'Composable should export useAdminDashboard');
assert(composable.includes('loadOverview'), 'Composable should expose loadOverview');
assert(composable.includes('loadTrend'), 'Composable should expose loadTrend');
assert(composable.includes('loadKline'), 'Composable should expose loadKline');
assert(composable.includes('loadProducts'), 'Composable should expose loadProducts');
assert(dashboard.includes("import { useAdminDashboard } from '@/composables/useAdminDashboard'"), 'Dashboard should import useAdminDashboard');
assert(!dashboard.includes("import { getProductList } from '@/api/product'"), 'Dashboard should not import product API directly');
assert(!dashboard.includes("getDashboardOverview, getDashboardTrend, getDashboardKline"), 'Dashboard should not import dashboard APIs directly');

console.log('Admin dashboard composable structure checks passed');
