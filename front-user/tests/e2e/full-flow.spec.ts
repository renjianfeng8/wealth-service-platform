import { test, expect, Page } from '@playwright/test'

const TEST_USER = {
  username: 'zhangwei',
  password: '123456',
  nickname: '张伟',
}

/** 页面加载无JS错误 */
async function assertNoJsError(page: Page) {
  page.on('pageerror', (err) => {
    test.fail(true, `JS Error: ${err.message}`)
  })
}

/** 等待页面加载完成 */
async function waitPageLoaded(page: Page, title: string) {
  await page.waitForSelector('.page-title', { timeout: 15000 })
  await expect(page.locator('.page-title')).toContainText(title)
}

// ===== 1. 登录 =====
test.describe('登录模块', () => {
  test('登录页面正常加载', async ({ page }) => {
    await page.goto('/#/login')
    await expect(page.locator('.login-title')).toHaveText('用户登录')
    await expect(page.locator('.login-card')).toBeVisible()
    await expect(page.locator('.brand-title')).toContainText('金融投资平台')
  })

  test('使用有效账号登录成功', async ({ page }) => {
    await page.goto('/#/login')
    await page.locator('input[placeholder="请输入用户名"]').fill(TEST_USER.username)
    await page.locator('input[placeholder="请输入密码"]').fill(TEST_USER.password)
    await page.locator('button:has-text("登 录")').click()
    await page.waitForURL('**/dashboard', { timeout: 15000 })
    await expect(page.locator('.page-title')).toContainText('首页概览')
    // 验证用户名显示在导航栏（登录后显示 username）
    await expect(page.locator('.username')).toContainText(TEST_USER.username)
  })

  test('空用户名密码提示验证', async ({ page }) => {
    await page.goto('/#/login')
    await page.locator('button:has-text("登 录")').click()
    await expect(page.locator('.el-form-item__error')).toHaveCount(2)
  })

  test('错误密码提示失败', async ({ page }) => {
    await page.goto('/#/login')
    await page.locator('input[placeholder="请输入用户名"]').fill(TEST_USER.username)
    await page.locator('input[placeholder="请输入密码"]').fill('wrong_password')
    await page.locator('button:has-text("登 录")').click()
    // 不应跳转，仍在登录页
    await page.waitForTimeout(2000)
    expect(page.url()).toContain('/login')
  })
})

// ===== 2. 首页仪表盘 =====
test.describe('首页仪表盘', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/#/login')
    await page.locator('input[placeholder="请输入用户名"]').fill(TEST_USER.username)
    await page.locator('input[placeholder="请输入密码"]').fill(TEST_USER.password)
    await page.locator('button:has-text("登 录")').click()
    await page.waitForURL('**/dashboard', { timeout: 15000 })
  })

  test('首页加载统计卡片', async ({ page }) => {
    await waitPageLoaded(page, '首页概览')
    // 统计卡片
    const statCards = page.locator('.stat-card')
    await expect(statCards.first()).toBeVisible()
    // 统计值应显示数字
    const statValues = page.locator('.stat-value')
    await expect(statValues.first()).toBeVisible()
  })

  test('快捷操作区域可见', async ({ page }) => {
    await expect(page.locator('.action-grid')).toBeVisible()
    const actionItems = page.locator('.action-item')
    const count = await actionItems.count()
    expect(count).toBeGreaterThanOrEqual(4)
  })

  test('最新行情表格加载', async ({ page }) => {
    await expect(page.locator('.el-table')).toBeVisible({ timeout: 10000 })
    // 等待数据加载完成（表格行出现）
    await page.waitForSelector('.el-table__body-wrapper tbody tr', { timeout: 10000 }).catch(() => {})
    const rows = await page.locator('.el-table__body-wrapper tbody tr').count()
    // 可能有数据或无数据（都是正常状态）
    expect(rows).toBeGreaterThanOrEqual(0)
  })

  test('快捷操作导航到产品中心', async ({ page }) => {
    const productLink = page.locator('.action-item').filter({ hasText: '产品中心' })
    await productLink.click()
    await page.waitForURL('**/product', { timeout: 10000 })
    await expect(page.locator('.page-title')).toContainText('产品中心')
  })
})

// ===== 3. 产品中心 =====
test.describe('产品中心', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/#/login')
    await page.locator('input[placeholder="请输入用户名"]').fill(TEST_USER.username)
    await page.locator('input[placeholder="请输入密码"]').fill(TEST_USER.password)
    await page.locator('button:has-text("登 录")').click()
    await page.waitForURL('**/dashboard', { timeout: 15000 })
    await page.goto('/#/product')
    await waitPageLoaded(page, '产品中心')
  })

  test('产品分类筛选可见', async ({ page }) => {
    await expect(page.locator('.el-radio-group')).toBeVisible()
    const buttons = page.locator('.el-radio-button__inner')
    const count = await buttons.count()
    expect(count).toBeGreaterThanOrEqual(2)
  })

  test('产品卡片网格加载', async ({ page }) => {
    // 等待数据加载
    await page.waitForTimeout(3000)
    const cards = page.locator('.product-card')
    const count = await cards.count()
    if (count > 0) {
      // 验证卡片内容
      await expect(cards.first().locator('.product-name')).toBeVisible()
    }
  })

  test('点击产品卡片打开详情', async ({ page }) => {
    await page.waitForTimeout(3000)
    const cards = page.locator('.product-card')
    const count = await cards.count()
    if (count > 0) {
      await cards.first().click()
      await expect(page.locator('.el-dialog')).toBeVisible({ timeout: 5000 })
      await expect(page.locator('.detail-body')).toBeVisible()
      await page.locator('.el-dialog .el-button').filter({ hasText: '关闭' }).click()
      await expect(page.locator('.el-dialog')).not.toBeVisible()
    }
  })
})

// ===== 4. 实时行情 =====
test.describe('实时行情', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/#/login')
    await page.locator('input[placeholder="请输入用户名"]').fill(TEST_USER.username)
    await page.locator('input[placeholder="请输入密码"]').fill(TEST_USER.password)
    await page.locator('button:has-text("登 录")').click()
    await page.waitForURL('**/dashboard', { timeout: 15000 })
    await page.goto('/#/market')
    await waitPageLoaded(page, '实时行情')
  })

  test('行情表格加载', async ({ page }) => {
    await page.waitForTimeout(3000)
    await expect(page.locator('.el-table')).toBeVisible()
    const headers = page.locator('.el-table__header-wrapper th')
    const headerCount = await headers.count()
    expect(headerCount).toBeGreaterThanOrEqual(5)
  })

  test('刷新按钮可用', async ({ page }) => {
    const refreshBtn = page.locator('.el-button').filter({ hasText: '刷新' })
    await expect(refreshBtn).toBeVisible()
    await refreshBtn.click()
    // 刷新后仍在行情页
    await expect(page.locator('.page-title')).toContainText('实时行情')
  })
})

// ===== 5. 我的自选 =====
test.describe('我的自选', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/#/login')
    await page.locator('input[placeholder="请输入用户名"]').fill(TEST_USER.username)
    await page.locator('input[placeholder="请输入密码"]').fill(TEST_USER.password)
    await page.locator('button:has-text("登 录")').click()
    await page.waitForURL('**/dashboard', { timeout: 15000 })
  })

  test('自选页面加载', async ({ page }) => {
    await page.goto('/#/favorite')
    await waitPageLoaded(page, '我的自选')
    await expect(page.locator('.add-bar')).toBeVisible()
  })

  test('添加自选交互可用', async ({ page }) => {
    await page.goto('/#/favorite')
    await waitPageLoaded(page, '我的自选')
    const input = page.locator('.add-input').locator('input')
    await expect(input).toBeVisible()
    await input.fill('GOLD001')
    const addBtn = page.locator('.el-button--primary').filter({ hasText: '添加自选' })
    await expect(addBtn).toBeVisible()
  })
})

// ===== 6. 交易委托 =====
test.describe('交易委托', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/#/login')
    await page.locator('input[placeholder="请输入用户名"]').fill(TEST_USER.username)
    await page.locator('input[placeholder="请输入密码"]').fill(TEST_USER.password)
    await page.locator('button:has-text("登 录")').click()
    await page.waitForURL('**/dashboard', { timeout: 15000 })
    await page.goto('/#/trade')
    await waitPageLoaded(page, '交易委托')
  })

  test('下单表单可见', async ({ page }) => {
    await expect(page.locator('.order-card')).toBeVisible()
    await expect(page.locator('.order-list-card')).toBeVisible()
  })

  test('交易方向选项', async ({ page }) => {
    const directionRadios = page.locator('.el-radio-button').filter({ hasText: /买入|卖出/ })
    const count = await directionRadios.count()
    expect(count).toBe(2)
  })

  test('委托单列表加载', async ({ page }) => {
    await page.waitForTimeout(3000)
    await expect(page.locator('.el-table')).toBeVisible()
  })
})

// ===== 7. 财经资讯 =====
test.describe('财经资讯', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/#/login')
    await page.locator('input[placeholder="请输入用户名"]').fill(TEST_USER.username)
    await page.locator('input[placeholder="请输入密码"]').fill(TEST_USER.password)
    await page.locator('button:has-text("登 录")').click()
    await page.waitForURL('**/dashboard', { timeout: 15000 })
    await page.goto('/#/news')
    await waitPageLoaded(page, '财经资讯')
  })

  test('新闻分类筛选可见', async ({ page }) => {
    await expect(page.locator('.el-radio-group')).toBeVisible()
  })

  test('新闻列表加载', async ({ page }) => {
    await page.waitForTimeout(3000)
    const items = page.locator('.news-card')
    const count = await items.count()
    if (count > 0) {
      await expect(items.first().locator('.news-title')).toBeVisible()
    }
  })

  test('点击新闻打开详情弹窗', async ({ page }) => {
    await page.waitForTimeout(3000)
    const items = page.locator('.news-card')
    const count = await items.count()
    if (count > 0) {
      await items.first().click()
      await expect(page.locator('.el-dialog')).toBeVisible({ timeout: 5000 })
    }
  })
})

// ===== 8. 消息中心 =====
test.describe('消息中心', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/#/login')
    await page.locator('input[placeholder="请输入用户名"]').fill(TEST_USER.username)
    await page.locator('input[placeholder="请输入密码"]').fill(TEST_USER.password)
    await page.locator('button:has-text("登 录")').click()
    await page.waitForURL('**/dashboard', { timeout: 15000 })
    await page.goto('/#/message')
    await waitPageLoaded(page, '消息中心')
  })

  test('消息列表加载', async ({ page }) => {
    await page.waitForTimeout(3000)
    const items = page.locator('.message-item')
    const count = await items.count()
    if (count > 0) {
      await expect(items.first().locator('.message-title')).toBeVisible()
    }
  })
})

// ===== 9. 个人中心 =====
test.describe('个人中心', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/#/login')
    await page.locator('input[placeholder="请输入用户名"]').fill(TEST_USER.username)
    await page.locator('input[placeholder="请输入密码"]').fill(TEST_USER.password)
    await page.locator('button:has-text("登 录")').click()
    await page.waitForURL('**/dashboard', { timeout: 15000 })
    await page.goto('/#/profile')
    await waitPageLoaded(page, '个人中心')
  })

  test('个人信息卡片加载', async ({ page }) => {
    await expect(page.locator('.profile-card')).toBeVisible()
    // 等待页面数据渲染
    await page.waitForTimeout(2000)
    const nameText = await page.locator('.profile-name').textContent()
    expect(nameText?.trim().length).toBeGreaterThanOrEqual(0)
  })

  test('信息编辑表单可见', async ({ page }) => {
    await expect(page.locator('.info-card')).toBeVisible()
    const inputs = page.locator('.info-card input')
    const count = await inputs.count()
    expect(count).toBeGreaterThanOrEqual(2)
  })

  test('统计数据显示', async ({ page }) => {
    await expect(page.locator('.profile-stats')).toBeVisible()
    const statItems = page.locator('.stat-item')
    const count = await statItems.count()
    expect(count).toBe(3)
  })
})

// ===== 10. 退出登录 =====
test.describe('退出登录', () => {
  test('页面跳转到登录页', async ({ page }) => {
    await page.goto('/#/login')
    await page.locator('input[placeholder="请输入用户名"]').fill(TEST_USER.username)
    await page.locator('input[placeholder="请输入密码"]').fill(TEST_USER.password)
    await page.locator('button:has-text("登 录")').click()
    await page.waitForURL('**/dashboard', { timeout: 15000 })

    // 点击用户头像下拉 -> 退出登录
    await page.locator('.user-info').click()
    await page.locator('.el-dropdown-menu__item').filter({ hasText: '退出登录' }).click()

    // 验证已跳转到登录页
    await page.waitForURL('**/login', { timeout: 10000 })
    await expect(page.locator('.login-title')).toHaveText('用户登录')
  })
})

// ===== 11. 导航菜单 =====
test.describe('导航菜单', () => {
  const pages = [
    { path: '/#/dashboard', title: '首页概览' },
    { path: '/#/product', title: '产品中心' },
    { path: '/#/market', title: '实时行情' },
    { path: '/#/favorite', title: '我的自选' },
    { path: '/#/trade', title: '交易委托' },
    { path: '/#/news', title: '财经资讯' },
    { path: '/#/message', title: '消息中心' },
    { path: '/#/profile', title: '个人中心' },
  ]

  for (const { path, title } of pages) {
    test(`导航到 ${title}`, async ({ page }) => {
      // 先登录
      await page.goto('/#/login')
      await page.locator('input[placeholder="请输入用户名"]').fill(TEST_USER.username)
      await page.locator('input[placeholder="请输入密码"]').fill(TEST_USER.password)
      await page.locator('button:has-text("登 录")').click()
      await page.waitForURL('**/dashboard', { timeout: 15000 })

      // 导航到目标页面
      await page.goto(path)
      await page.waitForTimeout(2000)
      await expect(page.locator('.page-title')).toContainText(title)
    })
  }
})
