/**
 * ES 搜索服务已下线（模块合并时删除）。
 * 搜索功能请使用产品 API (@/api/product)：
 *   - getProductPage() — 分页查询产品
 *   - getProductList() — 产品列表
 *
 * 管理端搜索页 (/admin/search) 已改用 getProductPage。
 */

import request from './index'

/** @deprecated ES 搜索已下线 */
export function searchProduct(_params: { keyword: string; page?: number; size?: number }) {
  console.warn('[search.ts] ES 搜索已下线，请使用 product API 代替')
  return Promise.reject(new Error('ES 搜索已下线'))
}

/** @deprecated ES 索引已删除 */
export function saveProductDocument(_data: any) {
  console.warn('[search.ts] ES 索引已删除，请使用 product API 代替')
  return Promise.reject(new Error('ES 索引已删除'))
}

/** @deprecated ES 索引已删除 */
export function getProductDocumentById(_id: string) {
  console.warn('[search.ts] ES 索引已删除，请使用 product API 代替')
  return Promise.reject(new Error('ES 索引已删除'))
}

/** @deprecated ES 索引已删除 */
export function deleteProductDocument(_id: string) {
  console.warn('[search.ts] ES 索引已删除，请使用 product API 代替')
  return Promise.reject(new Error('ES 索引已删除'))
}
