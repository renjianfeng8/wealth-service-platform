import request from './index'

/** 资讯 */
export function getNewsPage(params: { pageNum: number; pageSize: number; newsType?: number }) {
  return request.get('/message/WeaNews/page', { params })
}

export function getNewsList() {
  return request.get('/message/WeaNews')
}

export function getNewsById(id: number) {
  return request.get(`/message/WeaNews/${id}`)
}

/** 站内消息 */
export function getMessagePage(params: { pageNum: number; pageSize: number; userId?: number }) {
  return request.get('/message/WeaMessage/page', { params })
}

export function getMessageById(id: number) {
  return request.get(`/message/WeaMessage/${id}`)
}

export function readMessage(id: number) {
  return request.put(`/message/WeaMessage/${id}`, { readFlag: 1 })
}
