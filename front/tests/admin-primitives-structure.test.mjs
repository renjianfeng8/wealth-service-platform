import { readFileSync, existsSync } from 'fs';
import { join } from 'path';
import { fileURLToPath } from 'url';

const root = fileURLToPath(new URL('..', import.meta.url));
const componentDir = join(root, 'src/components/admin');

function assert(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

function readComponent(name) {
  const filePath = join(componentDir, name);
  assert(existsSync(filePath), `${name} should exist`);
  return readFileSync(filePath, 'utf8');
}

const pageShell = readComponent('AdminPageShell.vue');
const filterBar = readComponent('AdminFilterBar.vue');
const dataTable = readComponent('AdminDataTable.vue');
const formDialog = readComponent('AdminFormDialog.vue');

assert(pageShell.includes('defineProps'), 'AdminPageShell should accept props');
assert(pageShell.includes('title'), 'AdminPageShell should expose title prop');
assert(pageShell.includes('description'), 'AdminPageShell should expose description prop');
assert(pageShell.includes('name="toolbar"'), 'AdminPageShell should expose toolbar slot');

assert(filterBar.includes('fields'), 'AdminFilterBar should be field-schema driven');
assert(filterBar.includes('defineEmits'), 'AdminFilterBar should emit search/reset');
assert(filterBar.includes("'search'"), 'AdminFilterBar should emit search');
assert(filterBar.includes("'reset'"), 'AdminFilterBar should emit reset');
assert(filterBar.includes('el-form'), 'AdminFilterBar should render Element Plus form');
assert(filterBar.includes('el-input-number'), 'AdminFilterBar should support numeric filters');

assert(dataTable.includes('el-table'), 'AdminDataTable should wrap Element Plus table');
assert(dataTable.includes('el-pagination'), 'AdminDataTable should own pagination');
assert(dataTable.includes('pagination'), 'AdminDataTable should expose pagination state');
assert(dataTable.includes("'page-change'"), 'AdminDataTable should emit page-change');
assert(dataTable.includes('name="toolbar"'), 'AdminDataTable should expose toolbar slot');

assert(formDialog.includes('el-dialog'), 'AdminFormDialog should wrap Element Plus dialog');
assert(formDialog.includes('el-form'), 'AdminFormDialog should validate Element Plus form');
assert(formDialog.includes('saving'), 'AdminFormDialog should expose saving state');
assert(formDialog.includes("'submit'"), 'AdminFormDialog should emit submit after validation');

console.log('Admin primitive component structure checks passed');
