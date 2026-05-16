import { chromium } from '@playwright/test';
import { createServer } from 'http';
import { readFileSync, existsSync } from 'fs';
import { join, extname } from 'path';
import { fileURLToPath } from 'url';

const __dirname = fileURLToPath(new URL('.', import.meta.url));
const DIST = join(__dirname, 'dist');
const PORT = 4177;

// Minimal static server
function serve(req, res) {
  let p = req.url === '/' ? '/index.html' : req.url;
  // hash routing — serve index.html for all non-file paths
  if (!extname(p)) p = '/index.html';
  const file = join(DIST, p);
  if (!existsSync(file)) { res.writeHead(404); res.end('not found'); return; }
  const types = { '.html':'text/html','.js':'text/javascript','.css':'text/css','.svg':'image/svg+xml','.png':'image/png','.ico':'image/x-icon' };
  res.writeHead(200, { 'Content-Type': types[extname(file)] || 'text/plain' });
  res.end(readFileSync(file));
}

const server = createServer(serve);
await new Promise(r => server.listen(PORT, r));
console.log(`Static server on http://localhost:${PORT}`);

const browser = await chromium.launch({ headless: true });
const context = await browser.newContext({
  viewport: { width: 1440, height: 900 },
  deviceScaleFactor: 1,
});
const page = await context.newPage();

const errors = [];
page.on('pageerror', err => errors.push(err.message));
page.on('console', msg => { if (msg.type() === 'error') errors.push(`console.error: ${msg.text()}`); });

try {
  // Inject auth token + mock API
  await context.addInitScript(() => {
    localStorage.setItem('wealth_admin_token', 'test-jwt-token');
    localStorage.setItem('wealth_admin_user', JSON.stringify({ id: 1, username: 'admin' }));
  });

  const mockProducts = [
    { id: 1, productName: '沪深300', productCode: '000300', price: 3890.50, riseFallRate: 0.85 },
    { id: 2, productName: '中证500', productCode: '000905', price: 5620.30, riseFallRate: -0.32 },
    { id: 3, productName: '上证50', productCode: '000016', price: 2450.80, riseFallRate: 1.12 },
    { id: 4, productName: '创业板指', productCode: '399006', price: 1850.20, riseFallRate: -0.65 },
    { id: 5, productName: '科创50', productCode: '000688', price: 920.45, riseFallRate: 2.05 },
    { id: 6, productName: '恒生指数', productCode: 'HSI', price: 18700.00, riseFallRate: 0.45 },
    { id: 7, productName: '纳斯达克', productCode: 'IXIC', price: 16200.50, riseFallRate: 1.55 },
    { id: 8, productName: '道琼斯', productCode: 'DJI', price: 38900.00, riseFallRate: -0.18 },
  ];

  await page.route(/\/api\//, async route => {
    const url = route.request().url();
    if (url.includes('/product/finProduct')) {
      return route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ code: 200, data: mockProducts }) });
    }
    if (url.includes('/umsAdmin/info')) {
      return route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ code: 200, data: { id: 1, username: 'admin' } }) });
    }
    if (url.includes('/admin/permission')) {
      return route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ code: 200, data: [] }) });
    }
    return route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ code: 200, data: {} }) });
  });

  // 1. Navigate and wait for page to load
  await page.goto(`http://localhost:${PORT}/#/dashboard`, { waitUntil: 'networkidle' });
  await page.waitForTimeout(2000); // wait for charts to initialize

  // 2. Check page loaded — stat cards visible
  const statCards = page.locator('.fl-stat-card');
  const statCount = await statCards.count();
  console.log(`✅ Stat cards found: ${statCount}`);

  // 3. Check sidebar visible
  const sidebar = page.locator('.sidebar');
  await sidebar.waitFor({ state: 'visible', timeout: 5000 });
  console.log('✅ Sidebar is visible');

  // 4. Check sidebar menu items
  const menuItems = page.locator('.el-menu-item');
  const menuCount = await menuItems.count();
  console.log(`✅ Menu items count: ${menuCount}`);

  // 5. Check dashboard menu has active state
  const activeItem = page.locator('.el-menu-item.is-active');
  const activeText = await activeItem.textContent();
  console.log(`✅ Active menu: "${activeText?.trim()}"`);

  // 6. Check navbar visible with username
  const navbar = page.locator('.navbar');
  await navbar.waitFor({ state: 'visible', timeout: 3000 });
  const navUser = await page.locator('.navbar-user').textContent();
  console.log(`✅ Navbar user: "${navUser?.trim()}"`);

  // 7. Check sidebar collapse / expand
  const subMenuTitles = page.locator('.el-sub-menu__title');
  const titleCount = await subMenuTitles.count();
  console.log(`✅ Sub-menu titles count: ${titleCount}`);

  // Click first sub-menu to expand
  if (titleCount > 0) {
    await subMenuTitles.first().click();
    await page.waitForTimeout(400);
    const expanded = await page.locator('.el-sub-menu.is-opened').count();
    console.log(`✅ Expanded sub-menus after click: ${expanded}`);
  }

  // 8. Check chart canvases rendered
  const canvases = page.locator('canvas');
  const canvasCount = await canvases.count();
  console.log(`✅ ECharts canvases found: ${canvasCount}`);

  // Report errors
  if (errors.length > 0) {
    console.log('\n❌ Console errors:');
    errors.forEach(e => console.log(`  - ${e}`));
  } else {
    console.log('\n✅ No console errors');
  }

  const passed = statCount >= 3 && canvasCount >= 3 && errors.length === 0;
  console.log(`\n${'='.repeat(50)}`);
  console.log(passed ? '✅ ALL CHECKS PASSED' : '❌ SOME CHECKS FAILED');
  console.log(`${'='.repeat(50)}`);

} catch (err) {
  console.error('Test error:', err.message);
} finally {
  await browser.close();
  server.close();
}
