import request from './index'

/** 资讯 */
export function getNewsPage(params: { pageNum: number; pageSize: number; newsType?: number }) {
  return request.get('/message/finNews/page', { params })
}

export function getNewsList() {
  return request.get('/message/finNews')
}

export function getNewsById(id: number) {
  return request.get(`/message/finNews/${id}`)
}

/** 站内消息 */
export function getMessagePage(params: { pageNum: number; pageSize: number; userId?: number }) {
  return request.get('/message/finMessage/page', { params })
}

export function getMessageById(id: number) {
  return request.get(`/message/finMessage/${id}`)
}

export function readMessage(id: number) {
  return request.put(`/message/finMessage/${id}`, { readFlag: 1 })
}
