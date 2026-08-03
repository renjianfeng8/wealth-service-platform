import request from './index'
import type { PageResult, WeaProduct, WeaMarketData } from '@/types'

/**
 * 分页查询产品列表
 * @param params - 查询参数（pageNum、pageSize 必填，可选 productName、productCode、productType）
 * @returns 分页结果包含产品列表
 */
export function getProductPage(params: { pageNum: number; pageSize: number; productName?: string; productCode?: string; productType?: number; orderBy?: string; orderDir?: string }) {
  return request.get<PageResult<WeaProduct>>('/product/wea-product/page', { params })
}

/**
 * 根据 ID 查询产品
 * @param id - 产品 ID
 * @returns 产品信息
 */
export function getProductById(id: number | string) {
  return request.get<WeaProduct>(`/product/wea-product/${id}`)
}

/**
 * 创建产品
 * @param data - 产品信息
 * @returns 创建结果
 */
export function createProduct(data: Partial<WeaProduct>) {
  return request.post<boolean>('/product/wea-product', data)
}

/**
 * 更新产品
 * @param id - 产品 ID
 * @param data - 待更新的产品信息
 * @returns 更新结果
 */
export function updateProduct(id: number | string, data: Partial<WeaProduct>) {
  return request.put<boolean>(`/product/wea-product/${id}`, data)
}

/**
 * 删除产品
 * @param id - 产品 ID
 * @returns 删除结果
 */
export function deleteProduct(id: number | string) {
  return request.delete<boolean>(`/product/wea-product/${id}`)
}

/**
 * 分页查询行情数据列表
 * @param params - 查询参数（pageNum、pageSize）
 * @returns 分页结果包含行情数据列表
 */
export function getMarketDataPage(params: { pageNum: number; pageSize: number }) {
  return request.get<PageResult<WeaMarketData>>('/product/wea-market-data/page', { params })
}

/**
 * 创建行情数据
 * @param data - 行情数据信息
 * @returns 创建结果
 */
export function createMarketData(data: Partial<WeaMarketData>) {
  return request.post<boolean>('/product/wea-market-data', data)
}

/**
 * 更新行情数据
 * @param id - 行情数据 ID
 * @param data - 待更新的行情数据信息
 * @returns 更新结果
 */
export function updateMarketData(id: number | string, data: Partial<WeaMarketData>) {
  return request.put<boolean>(`/product/wea-market-data/${id}`, data)
}

/**
 * 删除行情数据
 * @param id - 行情数据 ID
 * @returns 删除结果
 */
export function deleteMarketData(id: number | string) {
  return request.delete<boolean>(`/product/wea-market-data/${id}`)
}
