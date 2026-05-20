import request from './index'

/** 资讯 */
export function getNewsPage(params: { pageNum: number; pageSize: number; newsType?: number }) {
  return request.get('/message/wea-news/page', { params })
}

export function getNewsList() {
  return request.get('/message/wea-news')
}

export function getNewsById(id: number) {
  return request.get(`/message/wea-news/${id}`)
}

/** 站内消息 */
export function getMessagePage(params: { pageNum: number; pageSize: number; userId?: number }) {
  return request.get('/message/wea-message/page', { params })
}

export function getMessageById(id: number) {
  return request.get(`/message/wea-message/${id}`)
}

export function readMessage(id: number) {
  return request.put(`/message/wea-message/${id}`, { readFlag: 1 })
}
