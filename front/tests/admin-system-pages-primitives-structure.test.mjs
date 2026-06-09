import { readFileSync } from 'fs';
import { join } from 'path';
import { fileURLToPath } from 'url';

const root = fileURLToPath(new URL('..', import.meta.url));

function assert(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

function readSystemPage(relativePath) {
  return readFileSync(join(root, 'src/views/admin/system', relativePath, 'index.vue'), 'utf8');
}

function assertSystemCrud(source, pageName, expectedTitle) {
  assert(source.includes("import AdminPageShell from '@/components/admin/AdminPageShell.vue'"), `${pageName} should import AdminPageShell`);
  assert(source.includes("import AdminFilterBar from '@/components/admin/AdminFilterBar.vue'"), `${pageName} should import AdminFilterBar`);
  assert(source.includes("import AdminDataTable from '@/components/admin/AdminDataTable.vue'"), `${pageName} should import AdminDataTable`);
  assert(source.includes("import AdminFormDialog from '@/components/admin/AdminFormDialog.vue'"), `${pageName} should import AdminFormDialog`);
  assert(source.includes('<AdminPageShell'), `${pageName} should render AdminPageShell`);
  assert(source.includes('<AdminFilterBar'), `${pageName} should render AdminFilterBar`);
  assert(source.includes('<AdminDataTable'), `${pageName} should render AdminDataTable`);
  assert(source.includes('<AdminFormDialog'), `${pageName} should render AdminFormDialog`);
  assert(source.includes(expectedTitle), `${pageName} should expose readable title ${expectedTitle}`);
  assert(!source.includes('<el-card shadow="never" class="search-card">'), `${pageName} should remove raw search card duplication`);
  assert(!source.includes('<el-dialog v-model="dialogVisible"'), `${pageName} should delegate dialog shell to AdminFormDialog`);
}

assertSystemCrud(readSystemPage('admin'), 'Admin account page', '\u7ba1\u7406\u5458\u7ba1\u7406');
assertSystemCrud(readSystemPage('role'), 'Role page', '\u89d2\u8272\u7ba1\u7406');
assertSystemCrud(readSystemPage('resource'), 'Resource page', '\u8d44\u6e90\u7ba1\u7406');
assertSystemCrud(readSystemPage('adminRole'), 'Admin role relation page', '\u7ba1\u7406\u5458\u89d2\u8272\u5173\u8054');
assertSystemCrud(readSystemPage('roleResource'), 'Role resource relation page', '\u89d2\u8272\u8d44\u6e90\u5173\u8054');

console.log('Admin system page primitive structure checks passed');
