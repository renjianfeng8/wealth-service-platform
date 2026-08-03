import request from './index'
import type { PageParam, WeaUserFavorite } from '@/types'

/**
 * 分页查询用户自选列表
 * @param params - 查询参数
 * @returns 分页结果包含用户自选列表
 */
export function getFavoritePage(params: PageParam & { userId?: number; productCode?: string }) {
  return request.get('/product/wea-user-favorite/page', { params })
}

/**
 * 创建用户自选
 * @param data - 自选信息
 * @returns 创建结果
 */
export function createFavorite(data: Partial<WeaUserFavorite>) {
  return request.post('/product/wea-user-favorite', data)
}

/**
 * 更新用户自选
 * @param id - 自选 ID
 * @param data - 待更新的自选信息
 * @returns 更新结果
 */
export function updateFavorite(id: number, data: Partial<WeaUserFavorite>) {
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
