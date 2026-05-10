import { test as setup, expect } from '@playwright/test'

const AUTH_FILE = 'playwright/.auth/user.json'

/** 测试账号 */
export const TEST_USER = {
  username: 'zhangwei',
  password: '123456',
  nickname: '张伟',
}

setup('登录并保存认证状态', async ({ page }) => {
  await page.goto('/#/login')
  await expect(page.locator('.login-title')).toHaveText('用户登录')

  await page.locator('input[placeholder="请输入用户名"]').fill(TEST_USER.username)
  await page.locator('input[placeholder="请输入密码"]').fill(TEST_USER.password)
  await page.locator('button:has-text("登 录")').click()

  // 等待跳转到首页
  await page.waitForURL('**/dashboard', { timeout: 15000 })
  await expect(page.locator('.page-title')).toContainText('首页概览')

  // 保存认证状态
  await page.context().storageState({ path: AUTH_FILE })
})

export { AUTH_FILE }
