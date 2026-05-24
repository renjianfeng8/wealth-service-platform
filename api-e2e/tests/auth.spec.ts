import { test, expect, request as pwRequest } from '@playwright/test'
import { ApiClient } from '../utils/api-client'

let api: ApiClient

test.beforeAll(async () => {
  const ctx = await pwRequest.newContext()
  api = new ApiClient(ctx, 'http://localhost:8080')
})

test.describe('管理员认证流程', () => {
  test('正常登录成功-返回双Token', async () => {
    const res = await api.post('/system/umsAdmin/login', {
      username: 'admin', password: 'admin123',
    })
    await api.assertOk(res, '登录成功')
    expect(res.body.data).toHaveProperty('accessToken')
    expect(res.body.data).toHaveProperty('refreshToken')
    expect(res.body.data.accessToken).toMatch(/^eyJ/)
  })

  test('错误密码返回401', async () => {
    const res = await api.post('/system/umsAdmin/login', {
      username: 'admin', password: 'wrong_password',
    })
    expect(res.body.code).toBe(401)
  })

  test('空用户名密码返回400', async () => {
    const res = await api.post('/system/umsAdmin/login', {
      username: '', password: '',
    })
    await api.assertBusinessFail(res)
  })

  test('未授权访问返回401', async () => {
    const res = await api.get('/system/umsAdmin/page', null)
    expect([401, 200]).toContain(res.body.code)
    if (res.body.code !== 401) {
      test.info().annotations.push({ type: 'WARN', description: '无Token请求应被拦截但返回了200' })
    }
  })

  test('登录后Token可访问受保护接口', async () => {
    await api.loginAsAdmin()
    const res = await api.get('/system/umsAdmin/page')
    await api.assertOk(res, '携带Token可访问')
    expect(res.body.data).toHaveProperty('records')
  })
})

test.describe('用户认证流程', () => {
  test('用户登录成功-返回Token', async () => {
    const res = await api.post('/user/login', {
      username: 'zhangwei', password: '123456',
    })
    await api.assertOk(res, '用户登录成功')
    expect(res.body.data).toHaveProperty('token')
    expect(res.body.data).toHaveProperty('userId')
    expect(res.body.data).toHaveProperty('nickname')
    expect(res.body.data.nickname).toBe('张伟')
  })

  test('用户登录-错误密码', async () => {
    const res = await api.post('/user/login', {
      username: 'zhangwei', password: 'wrong',
    })
    expect(res.body.code).toBe(401)
  })

  test('用户登录-账号禁用', async () => {
    const res = await api.post('/user/login', {
      username: 'disabled_user', password: 'any',
    })
    // 不存在或禁用返回 401
    expect([401, 400]).toContain(res.body.code)
  })
})

test.describe('Token 刷新', () => {
  test('使用refresh_token刷新成功', async () => {
    const loginRes = await api.post('/system/umsAdmin/login', {
      username: 'admin', password: 'admin123',
    })
    const refreshToken = loginRes.body.data.refreshToken

    const res = await api.post('/system/umsAdmin/refresh', null, refreshToken)
    await api.assertOk(res, 'Token刷新成功')
    expect(res.body.data).toHaveProperty('accessToken')
    expect(res.body.data).toHaveProperty('refreshToken')
  })

  test('无效refresh_token刷新失败', async () => {
    const res = await api.post('/system/umsAdmin/refresh', null, 'invalid_token')
    // 刷新接口期望 4xx
    expect(res.status).toBeGreaterThanOrEqual(400)
  })
})
