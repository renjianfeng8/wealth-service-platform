import { readFileSync } from 'fs';
import { join } from 'path';
import { fileURLToPath } from 'url';

const root = fileURLToPath(new URL('..', import.meta.url));
const productPath = join(root, 'src/views/admin/product/index.vue');
const tradePath = join(root, 'src/views/admin/trade/index.vue');

function assert(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

function assertCrudPage(source, pageName) {
  assert(source.includes("import AdminPageShell from '@/components/admin/AdminPageShell.vue'"), `${pageName} should import AdminPageShell`);
  assert(source.includes("import AdminFilterBar from '@/components/admin/AdminFilterBar.vue'"), `${pageName} should import AdminFilterBar`);
  assert(source.includes("import AdminDataTable from '@/components/admin/AdminDataTable.vue'"), `${pageName} should import AdminDataTable`);
  assert(source.includes("import AdminFormDialog from '@/components/admin/AdminFormDialog.vue'"), `${pageName} should import AdminFormDialog`);
  assert(source.includes('<AdminPageShell'), `${pageName} should render AdminPageShell`);
  assert(source.includes('<AdminFilterBar'), `${pageName} should render AdminFilterBar`);
  assert(source.includes('<AdminDataTable'), `${pageName} should render AdminDataTable`);
  assert(source.includes('<AdminFormDialog'), `${pageName} should render AdminFormDialog`);
  assert(!source.includes('<el-card shadow="never" class="search-card">'), `${pageName} should remove raw search card duplication`);
  assert(!source.includes('<el-dialog v-model="dialogVisible"'), `${pageName} should delegate dialog shell to AdminFormDialog`);
}

const productPage = readFileSync(productPath, 'utf8');
const tradePage = readFileSync(tradePath, 'utf8');

assertCrudPage(productPage, 'Product page');
assertCrudPage(tradePage, 'Trade page');
assert(productPage.includes('产品管理'), 'Product page should restore readable Chinese title');
assert(tradePage.includes('交易委托管理'), 'Trade page should restore readable Chinese title');

console.log('Admin product/trade primitive structure checks passed');
