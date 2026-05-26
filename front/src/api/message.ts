import request from './index'

/**
 * 分页查询站内消息列表
 * @param params - 查询参数
 * @returns 分页结果包含站内消息列表
 */
export function getMessagePage(params: any) {
  return request.get('/message/wea-message/page', { params })
}

/**
 * 查询所有站内消息列表
 * @returns 站内消息列表
 */
export function getMessageList() {
  return request.get('/message/wea-message')
}

/**
 * 根据 ID 查询站内消息
 * @param id - 消息 ID
 * @returns 站内消息信息
 */
export function getMessageById(id: number) {
  return request.get(`/message/wea-message/${id}`)
}

/**
 * 创建站内消息
 * @param data - 消息信息
 * @returns 创建结果
 */
export function createMessage(data: any) {
  return request.post('/message/wea-message', data)
}

/**
 * 更新站内消息
 * @param id - 消息 ID
 * @param data - 待更新的消息信息
 * @returns 更新结果
 */
export function updateMessage(id: number, data: any) {
  return request.put(`/message/wea-message/${id}`, data)
}

/**
 * 删除站内消息
 * @param id - 消息 ID
 * @returns 删除结果
 */
export function deleteMessage(id: number) {
  return request.delete(`/message/wea-message/${id}`)
}

/**
 * 分页查询资讯列表
 * @param params - 查询参数
 * @returns 分页结果包含资讯列表
 */
export function getNewsPage(params: any) {
  return request.get('/message/wea-news/page', { params })
}

/**
 * 查询所有资讯列表
 * @returns 资讯列表
 */
export function getNewsList() {
  return request.get('/message/wea-news')
}

/**
 * 根据 ID 查询资讯
 * @param id - 资讯 ID
 * @returns 资讯信息
 */
export function getNewsById(id: number) {
  return request.get(`/message/wea-news/${id}`)
}

/**
 * 创建资讯
 * @param data - 资讯信息
 * @returns 创建结果
 */
export function createNews(data: any) {
  return request.post('/message/wea-news', data)
}

/**
 * 更新资讯
 * @param id - 资讯 ID
 * @param data - 待更新的资讯信息
 * @returns 更新结果
 */
export function updateNews(id: number, data: any) {
  return request.put(`/message/wea-news/${id}`, data)
}

/**
 * 删除资讯
 * @param id - 资讯 ID
 * @returns 删除结果
 */
export function deleteNews(id: number) {
  return request.delete(`/message/wea-news/${id}`)
}

/**
 * 标记站内消息为已读
 * @param id - 消息 ID
 * @returns 更新结果
 */
export function readMessage(id: number) {
  return request.put(`/message/wea-message/${id}`, { readFlag: 1 })
}
