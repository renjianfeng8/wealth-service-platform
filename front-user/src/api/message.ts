import request from './index'

/** 资讯 */
export function getNewsPage(params: { pageNum: number; pageSize: number; newsType?: number }) {
  return request.get('/message/weaNews/page', { params })
}

export function getNewsList() {
  return request.get('/message/weaNews')
}

export function getNewsById(id: number) {
  return request.get(`/message/weaNews/${id}`)
}

/** 站内消息 */
export function getMessagePage(params: { pageNum: number; pageSize: number; userId?: number }) {
  return request.get('/message/weaMessage/page', { params })
}

export function getMessageById(id: number) {
  return request.get(`/message/weaMessage/${id}`)
}

export function readMessage(id: number) {
  return request.put(`/message/weaMessage/${id}`, { readFlag: 1 })
}
