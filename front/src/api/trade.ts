import request from './index'
import type { PageParam, PageResult, WeaTradeOrder } from '@/types'

/**
 * 分页查询交易委托列表
 * @param params - 查询参数（pageNum、pageSize，可选 userId、orderStatus）
 * @returns 分页结果包含交易委托列表
 */
export function getTradeOrderPage(params: PageParam & { userId?: number | string; productCode?: string; orderStatus?: number }) {
  return request.get<PageResult<WeaTradeOrder>>('/trade/wea-trade-order/page', { params })
}

/**
 * 根据 ID 查询交易委托
 * @param id - 交易委托 ID
 * @returns 交易委托信息
 */
export function getTradeOrderById(id: number | string) {
  return request.get<WeaTradeOrder>(`/trade/wea-trade-order/${id}`)
}

/**
 * 创建交易委托
 * @param data - 交易委托信息
 * @returns 创建结果
 */
export function createTradeOrder(data: Omit<WeaTradeOrder, 'id' | 'orderNo' | 'orderStatus' | 'createTime'> & { idempotentKey?: string }) {
  return request.post<boolean>('/trade/wea-trade-order', data)
}

/**
 * 更新交易委托
 * @param id - 交易委托 ID
 * @param data - 待更新的交易委托信息
 * @returns 更新结果
 */
export function updateTradeOrder(id: number | string, data: Partial<WeaTradeOrder>) {
  return request.put<boolean>(`/trade/wea-trade-order/${id}`, data)
}

/**
 * 删除交易委托
 * @param id - 交易委托 ID
 * @returns 删除结果
 */
export function deleteTradeOrder(id: number | string) {
  return request.delete<boolean>(`/trade/wea-trade-order/${id}`)
}

/**
 * 取消交易委托
 * @param id - 交易委托 ID
 * @returns 取消结果
 */
export function cancelTradeOrder(id: number | string) {
  return request.put<boolean>(`/trade/wea-trade-order/${id}/status`, { orderStatus: 3 })
}
