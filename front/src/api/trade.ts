import request from './index'
import type { PageParam, WeaTradeOrder } from '@/types'

/**
 * 分页查询交易委托列表
 * @param params - 查询参数（pageNum、pageSize，可选 userId、orderStatus）
 * @returns 分页结果包含交易委托列表
 */
export function getTradeOrderPage(params: PageParam & { userId?: number; productCode?: string; orderStatus?: number }) {
  return request.get('/trade/wea-trade-order/page', { params })
}

/**
 * 根据 ID 查询交易委托
 * @param id - 交易委托 ID
 * @returns 交易委托信息
 */
export function getTradeOrderById(id: number) {
  return request.get(`/trade/wea-trade-order/${id}`)
}

/**
 * 创建交易委托
 * @param data - 交易委托信息
 * @returns 创建结果
 */
export function createTradeOrder(data: Omit<WeaTradeOrder, 'id' | 'orderNo' | 'orderStatus' | 'createTime'> & { idempotentKey?: string }) {
  return request.post('/trade/wea-trade-order', data)
}

/**
 * 更新交易委托
 * @param id - 交易委托 ID
 * @param data - 待更新的交易委托信息
 * @returns 更新结果
 */
export function updateTradeOrder(id: number, data: Partial<WeaTradeOrder>) {
  return request.put(`/trade/wea-trade-order/${id}`, data)
}

/**
 * 删除交易委托
 * @param id - 交易委托 ID
 * @returns 删除结果
 */
export function deleteTradeOrder(id: number) {
  return request.delete(`/trade/wea-trade-order/${id}`)
}

/**
 * 取消交易委托
 * @param id - 交易委托 ID
 * @returns 取消结果
 */
export function cancelTradeOrder(id: number) {
  return request.put(`/trade/wea-trade-order/${id}`, { orderStatus: 2 })
}
