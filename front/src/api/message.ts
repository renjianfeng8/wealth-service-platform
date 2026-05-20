import request from './index'

// 站内消息
export function getMessagePage(params: any) {
  return request.get('/message/wea-message/page', { params })
}

export function getMessageList() {
  return request.get('/message/wea-message')
}

export function getMessageById(id: number) {
  return request.get(`/message/wea-message/${id}`)
}

export function createMessage(data: any) {
  return request.post('/message/wea-message', data)
}

export function updateMessage(id: number, data: any) {
  return request.put(`/message/wea-message/${id}`, data)
}

export function deleteMessage(id: number) {
  return request.delete(`/message/wea-message/${id}`)
}

// 资讯
export function getNewsPage(params: any) {
  return request.get('/message/wea-news/page', { params })
}

export function getNewsList() {
  return request.get('/message/wea-news')
}

export function getNewsById(id: number) {
  return request.get(`/message/wea-news/${id}`)
}

export function createNews(data: any) {
  return request.post('/message/wea-news', data)
}

export function updateNews(id: number, data: any) {
  return request.put(`/message/wea-news/${id}`, data)
}

export function deleteNews(id: number) {
  return request.delete(`/message/wea-news/${id}`)
}
