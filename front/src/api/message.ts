import request from './index'

// 站内消息
export function getMessagePage(params: any) {
  return request.get('/message/weaMessage/page', { params })
}

export function getMessageList() {
  return request.get('/message/weaMessage')
}

export function getMessageById(id: number) {
  return request.get(`/message/weaMessage/${id}`)
}

export function createMessage(data: any) {
  return request.post('/message/weaMessage', data)
}

export function updateMessage(id: number, data: any) {
  return request.put(`/message/weaMessage/${id}`, data)
}

export function deleteMessage(id: number) {
  return request.delete(`/message/weaMessage/${id}`)
}

// 资讯
export function getNewsPage(params: any) {
  return request.get('/message/weaNews/page', { params })
}

export function getNewsList() {
  return request.get('/message/weaNews')
}

export function getNewsById(id: number) {
  return request.get(`/message/weaNews/${id}`)
}

export function createNews(data: any) {
  return request.post('/message/weaNews', data)
}

export function updateNews(id: number, data: any) {
  return request.put(`/message/weaNews/${id}`, data)
}

export function deleteNews(id: number) {
  return request.delete(`/message/weaNews/${id}`)
}
