import request from './index'

/**
 * 分页查询用户自选列表
 * @param params - 查询参数
 * @returns 分页结果包含用户自选列表
 */
export function getFavoritePage(params: any) {
  return request.get('/product/wea-user-favorite/page', { params })
}

/**
 * 查询所有用户自选列表
 * @param params - 查询参数（可选 userId）
 * @returns 用户自选列表
 */
export function getFavoriteList(params?: { userId?: number }) {
  return request.get('/product/wea-user-favorite', { params })
}

/**
 * 根据 ID 查询用户自选
 * @param id - 自选 ID
 * @returns 用户自选信息
 */
export function getFavoriteById(id: number) {
  return request.get(`/product/wea-user-favorite/${id}`)
}

/**
 * 创建用户自选
 * @param data - 自选信息
 * @returns 创建结果
 */
export function createFavorite(data: any) {
  return request.post('/product/wea-user-favorite', data)
}

/**
 * 更新用户自选
 * @param id - 自选 ID
 * @param data - 待更新的自选信息
 * @returns 更新结果
 */
export function updateFavorite(id: number, data: any) {
  return request.put(`/product/wea-user-favorite/${id}`, data)
}

/**
 * 删除用户自选
 * @param id - 自选 ID
 * @returns 删除结果
 */
export function deleteFavorite(id: number) {
  return request.delete(`/product/wea-user-favorite/${id}`)
}
