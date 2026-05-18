import request from './index'

// 站内消息
export function getMessagePage(params: any) {
  return request.get('/message/WeaMessage/page', { params })
}

export function getMessageList() {
  return request.get('/message/WeaMessage')
}

export function getMessageById(id: number) {
  return request.get(`/message/WeaMessage/${id}`)
}

export function createMessage(data: any) {
  return request.post('/message/WeaMessage', data)
}

export function updateMessage(id: number, data: any) {
  return request.put(`/message/WeaMessage/${id}`, data)
}

export function deleteMessage(id: number) {
  return request.delete(`/message/WeaMessage/${id}`)
}

// 资讯
export function getNewsPage(params: any) {
  return request.get('/message/WeaNews/page', { params })
}

export function getNewsList() {
  return request.get('/message/WeaNews')
}

export function getNewsById(id: number) {
  return request.get(`/message/WeaNews/${id}`)
}

export function createNews(data: any) {
  return request.post('/message/WeaNews', data)
}

export function updateNews(id: number, data: any) {
  return request.put(`/message/WeaNews/${id}`, data)
}

export function deleteNews(id: number) {
  return request.delete(`/message/WeaNews/${id}`)
}
