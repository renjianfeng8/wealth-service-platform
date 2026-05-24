import { test, expect, request as pwRequest } from '@playwright/test'
import { ApiClient } from '../utils/api-client'

let api: ApiClient

test.beforeAll(async () => {
  const ctx = await pwRequest.newContext()
  api = new ApiClient(ctx, 'http://localhost:8080')
  await api.loginAsAdmin()
})

test.describe('资讯 CRUD', () => {
  const testNews = {
    title: `E2E新闻标题_${Date.now()}`,
    content: '这是E2E测试的新闻内容正文',
    newsType: 1,
    status: 1,
  }

  test('分页查询资讯列表', async () => {
    const res = await api.get('/message/wea-news/page?pageNum=1&pageSize=10')
    await api.assertOk(res)
    expect(res.body.data).toHaveProperty('records')
    expect(res.body.data).toHaveProperty('total')
  })

  test('创建资讯成功', async () => {
    const res = await api.post('/message/wea-news', testNews)
    await api.assertOk(res, '创建资讯')
    expect(res.body.data).toBe(true)
  })

  test('获取资讯详情', async () => {
    const list = await api.get('/message/wea-news/page?pageNum=1&pageSize=10')
    if (list.body.data.records.length > 0) {
      const id = list.body.data.records[0].id
      const res = await api.get(`/message/wea-news/${id}`)
      await api.assertOk(res)
      expect(res.body.data).toHaveProperty('title')
    }
  })

  test('获取不存在资讯返回404', async () => {
    const res = await api.get('/message/wea-news/99999')
    expect(res.body.code).toBe(404)
  })

  test('更新资讯成功', async () => {
    const list = await api.get('/message/wea-news/page?pageNum=1&pageSize=10')
    if (list.body.data.records.length > 0) {
      const id = list.body.data.records[0].id
      const res = await api.put(`/message/wea-news/${id}`, { title: 'Updated新闻标题', content: 'Updated内容正文', newsType: 1 })
      await api.assertOk(res)
    }
  })

  test('删除资讯成功', async () => {
    const create = await api.post('/message/wea-news', {
      title: `E2E待删除新闻_${Date.now()}`,
      content: '即将被删除',
      newsType: 2,
    })
    await api.assertOk(create)

    const list = await api.get('/message/wea-news/page?pageNum=1&pageSize=1000')
    const target = list.body.data.records.find((r: any) => r.title?.startsWith('E2E待删除新闻_'))
    if (target) {
      const res = await api.delete(`/message/wea-news/${target.id}`)
      await api.assertOk(res)
    }
  })

  test('创建资讯-标题为空', async () => {
    const res = await api.post('/message/wea-news', { content: '无标题内容', newsType: 1 })
    expect(res.body.code).toBe(400)
  })
})

test.describe('消息推送 CRUD', () => {
  const testMessage = {
    userId: 1,
    msgTitle: `E2E消息标题_${Date.now()}`,
    msgContent: '这是E2E测试的消息内容',
    msgType: 1,
  }

  test('分页查询消息列表', async () => {
    const res = await api.get('/message/wea-message/page?pageNum=1&pageSize=10')
    await api.assertOk(res)
    expect(res.body.data).toHaveProperty('records')
  })

  test('创建消息成功', async () => {
    const res = await api.post('/message/wea-message', testMessage)
    await api.assertOk(res, '创建消息')
    expect(res.body.data).toBe(true)
  })

  test('获取消息详情', async () => {
    const list = await api.get('/message/wea-message/page?pageNum=1&pageSize=10')
    if (list.body.data.records.length > 0) {
      const id = list.body.data.records[0].id
      const res = await api.get(`/message/wea-message/${id}`)
      await api.assertOk(res)
      expect(res.body.data).toHaveProperty('msgTitle')
    }
  })

  test('获取不存在消息返回404', async () => {
    const res = await api.get('/message/wea-message/99999')
    expect(res.body.code).toBe(404)
  })

  test('更新消息成功', async () => {
    const list = await api.get('/message/wea-message/page?pageNum=1&pageSize=10')
    if (list.body.data.records.length > 0) {
      const id = list.body.data.records[0].id
      const res = await api.put(`/message/wea-message/${id}`, { msgTitle: 'Updated消息标题', userId: 1, msgContent: 'Updated消息内容' })
      await api.assertOk(res)
    }
  })

  test('按用户筛选消息', async () => {
    const res = await api.get('/message/wea-message/page?pageNum=1&pageSize=10&userId=1')
    await api.assertOk(res)
  })

  test('删除消息成功', async () => {
    const create = await api.post('/message/wea-message', {
      userId: 1,
      msgTitle: `E2E待删除消息_${Date.now()}`,
      msgContent: '即将被删除',
      msgType: 1,
    })
    await api.assertOk(create)

    const list = await api.get('/message/wea-message/page?pageNum=1&pageSize=1000')
    const target = list.body.data.records.find((r: any) => r.msgTitle?.startsWith('E2E待删除消息_'))
    if (target) {
      const res = await api.delete(`/message/wea-message/${target.id}`)
      await api.assertOk(res)
    }
  })
})
