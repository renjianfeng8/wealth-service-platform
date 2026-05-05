import request from './index'

// 站内消息
export function getMessagePage(params: any) {
  return request.get('/message/finMessage', { params })
}

export function getMessageList() {
  return request.get('/message/finMessage')
}

export function getMessageById(id: number) {
  return request.get(`/message/finMessage/${id}`)
}

export function createMessage(data: any) {
  return request.post('/message/finMessage', data)
}

export function updateMessage(id: number, data: any) {
  return request.put(`/message/finMessage/${id}`, data)
}

export function deleteMessage(id: number) {
  return request.delete(`/message/finMessage/${id}`)
}

// 资讯
export function getNewsPage(params: any) {
  return request.get('/message/finNews', { params })
}

export function getNewsList() {
  return request.get('/message/finNews')
}

export function getNewsById(id: number) {
  return request.get(`/message/finNews/${id}`)
}

export function createNews(data: any) {
  return request.post('/message/finNews', data)
}

export function updateNews(id: number, data: any) {
  return request.put(`/message/finNews/${id}`, data)
}

export function deleteNews(id: number) {
  return request.delete(`/message/finNews/${id}`)
}
