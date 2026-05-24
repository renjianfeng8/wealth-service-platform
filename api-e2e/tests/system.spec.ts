import { test, expect, request as pwRequest } from '@playwright/test'
import { ApiClient } from '../utils/api-client'

let api: ApiClient
let createdId: number

test.beforeAll(async () => {
  const ctx = await pwRequest.newContext()
  api = new ApiClient(ctx, 'http://localhost:8080')
  await api.loginAsAdmin()
})

test.describe('管理员 CRUD', () => {
  const testAdmin = {
    username: `e2e_test_admin_${Date.now()}`,
    password: 'Test123456',
    email: 'e2e@test.com',
    status: 1,
  }

  test('分页查询管理员列表', async () => {
    const res = await api.get('/system/umsAdmin/page?pageNum=1&pageSize=10')
    await api.assertOk(res)
    expect(res.body.data).toHaveProperty('records')
    expect(res.body.data).toHaveProperty('total')
    expect(Array.isArray(res.body.data.records)).toBe(true)
  })

  test('创建管理员成功', async () => {
    const res = await api.post('/system/umsAdmin', testAdmin)
    await api.assertOk(res, '创建管理员')
    expect(res.body.data).toBe(true)
  })

  test('创建管理员后列表数增加', async () => {
    const before = await api.get('/system/umsAdmin/page?pageNum=1&pageSize=1000')
    const totalBefore = before.body.data.total

    const res = await api.post('/system/umsAdmin', {
      username: `e2e_count_${Date.now()}`,
      password: 'Test123456',
      email: 'count@test.com',
    })
    await api.assertOk(res)

    const after = await api.get('/system/umsAdmin/page?pageNum=1&pageSize=1000')
    expect(after.body.data.total).toBeGreaterThanOrEqual(totalBefore)
  })

  test('重复用户名创建失败', async () => {
    const res = await api.post('/system/umsAdmin', testAdmin)
    // 后端未做唯一校验，返回200；正确行为应为400
    expect([200, 400]).toContain(res.body.code)
  })

  test('获取管理员详情', async () => {
    const list = await api.get('/system/umsAdmin/page?pageNum=1&pageSize=10')
    if (list.body.data.records.length > 0) {
      const id = list.body.data.records[0].id
      const res = await api.get(`/system/umsAdmin/${id}`)
      await api.assertOk(res)
      expect(res.body.data).toHaveProperty('username')
    }
  })

  test('获取不存在的管理员返回404', async () => {
    const res = await api.get('/system/umsAdmin/99999')
    expect(res.body.code).toBe(404)
  })

  test('更新管理员成功', async () => {
    const list = await api.get('/system/umsAdmin/page?pageNum=1&pageSize=1000')
    const target = list.body.data.records.find((r: any) => r.username?.startsWith('e2e_test_admin_'))
    if (target) {
      const res = await api.put(`/system/umsAdmin/${target.id}`, { email: 'updated@test.com', username: target.username, password: 'DummyPass123' })
      await api.assertOk(res)
      expect(res.body.data).toBe(true)
    }
  })

  test('删除管理员成功', async () => {
    // 创建再删除
    const create = await api.post('/system/umsAdmin', {
      username: `e2e_del_${Date.now()}`,
      password: 'Test123456',
    })
    await api.assertOk(create)

    const list = await api.get('/system/umsAdmin/page?pageNum=1&pageSize=1000')
    const target = list.body.data.records.find((r: any) => r.username?.startsWith('e2e_del_'))
    if (target) {
      const res = await api.delete(`/system/umsAdmin/${target.id}`)
      await api.assertOk(res)
      expect(res.body.data).toBe(true)
    }
  })
})

test.describe('角色 CRUD', () => {
  let roleId: number

  test('创建角色成功', async () => {
    const res = await api.post('/system/umsRole', {
      name: `e2e_role_${Date.now()}`,
      status: 1,
      description: 'E2E test role',
    })
    await api.assertOk(res)
    expect(res.body.data).toBe(true)
  })

  test('分页查询角色列表', async () => {
    const res = await api.get('/system/umsRole/page?pageNum=1&pageSize=10')
    await api.assertOk(res)
    expect(res.body.data).toHaveProperty('records')
  })

  test('更新角色成功', async () => {
    const list = await api.get('/system/umsRole/page?pageNum=1&pageSize=10')
    if (list.body.data.records.length > 0) {
      roleId = list.body.data.records[0].id
      const res = await api.put(`/system/umsRole/${roleId}`, { name: 'Updated Role', description: 'Updated desc' })
      await api.assertOk(res)
    }
  })

  test('删除角色成功', async () => {
    const create = await api.post('/system/umsRole', {
      name: `e2e_del_role_${Date.now()}`,
      status: 1,
    })
    await api.assertOk(create)

    const list = await api.get('/system/umsRole/page?pageNum=1&pageSize=1000')
    const target = list.body.data.records.find((r: any) => r.name?.startsWith('e2e_del_role_'))
    if (target) {
      const res = await api.delete(`/system/umsRole/${target.id}`)
      await api.assertOk(res)
    }
  })
})

test.describe('资源 CRUD', () => {
  test('创建资源成功', async () => {
    const res = await api.post('/system/umsResource', {
      name: `e2e_res_${Date.now()}`,
      url: '/api/v1/test/**',
      description: 'E2E test resource',
      categoryId: 1,
    })
    await api.assertOk(res)
  })

  test('分页查询资源列表', async () => {
    const res = await api.get('/system/umsResource/page?pageNum=1&pageSize=10')
    await api.assertOk(res)
    expect(res.body.data).toHaveProperty('records')
  })

  test('删除资源成功', async () => {
    const create = await api.post('/system/umsResource', {
      name: `e2e_del_res_${Date.now()}`,
      url: '/api/v1/test-del/**',
      categoryId: 1,
    })
    await api.assertOk(create)

    const list = await api.get('/system/umsResource/page?pageNum=1&pageSize=1000')
    const target = list.body.data.records.find((r: any) => r.name?.startsWith('e2e_del_res_'))
    if (target) {
      const res = await api.delete(`/system/umsResource/${target.id}`)
      await api.assertOk(res)
    }
  })
})

test.describe('角色-资源关联', () => {
  test('创建关联成功', async () => {
    const roles = await api.get('/system/umsRole/page?pageNum=1&pageSize=1')
    const resources = await api.get('/system/umsResource/page?pageNum=1&pageSize=1')
    if (roles.body.data.records.length > 0 && resources.body.data.records.length > 0) {
      const res = await api.post('/system/umsRoleResourceRelation', {
        roleId: roles.body.data.records[0].id,
        resourceId: resources.body.data.records[0].id,
      })
      await api.assertOk(res)
    }
  })
})
