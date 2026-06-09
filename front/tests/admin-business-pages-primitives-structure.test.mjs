import { readFileSync } from 'fs';
import { join } from 'path';
import { fileURLToPath } from 'url';

const root = fileURLToPath(new URL('..', import.meta.url));

function assert(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

function readAdminPage(relativePath) {
  return readFileSync(join(root, 'src/views/admin', relativePath, 'index.vue'), 'utf8');
}

function assertSharedListShell(source, pageName, expectedTitle) {
  assert(source.includes("import AdminPageShell from '@/components/admin/AdminPageShell.vue'"), `${pageName} should import AdminPageShell`);
  assert(source.includes("import AdminFilterBar from '@/components/admin/AdminFilterBar.vue'"), `${pageName} should import AdminFilterBar`);
  assert(source.includes("import AdminDataTable from '@/components/admin/AdminDataTable.vue'"), `${pageName} should import AdminDataTable`);
  assert(source.includes('<AdminPageShell'), `${pageName} should render AdminPageShell`);
  assert(source.includes('<AdminFilterBar'), `${pageName} should render AdminFilterBar`);
  assert(source.includes('<AdminDataTable'), `${pageName} should render AdminDataTable`);
  assert(source.includes(expectedTitle), `${pageName} should expose readable title ${expectedTitle}`);
  assert(!source.includes('<el-card shadow="never" class="search-card">'), `${pageName} should remove raw search card duplication`);
}

function assertCrudShell(source, pageName, expectedTitle) {
  assertSharedListShell(source, pageName, expectedTitle);
  assert(source.includes("import AdminFormDialog from '@/components/admin/AdminFormDialog.vue'"), `${pageName} should import AdminFormDialog`);
  assert(source.includes('<AdminFormDialog'), `${pageName} should render AdminFormDialog`);
  assert(!source.includes('<el-dialog v-model="dialogVisible"'), `${pageName} should delegate dialog shell to AdminFormDialog`);
}

assertCrudShell(readAdminPage('user'), 'User page', '\u7528\u6237\u7ba1\u7406');
assertCrudShell(readAdminPage('market'), 'Market page', '\u884c\u60c5\u6570\u636e');
assertCrudShell(readAdminPage('favorite'), 'Favorite page', '\u81ea\u9009\u7ba1\u7406');
assertCrudShell(readAdminPage('message'), 'Message page', '\u7ad9\u5185\u6d88\u606f');
assertCrudShell(readAdminPage('news'), 'News page', '\u8d44\u8baf\u7ba1\u7406');
assertSharedListShell(readAdminPage('search'), 'Search page', '\u4ea7\u54c1\u641c\u7d22');

console.log('Admin business page primitive structure checks passed');
