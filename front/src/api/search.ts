import request from './index'

/**
 * ES 搜索产品
 * @param params - 搜索参数（keyword 必填，可选 page、size）
 * @returns 搜索结果
 */
export function searchProduct(params: { keyword: string; page?: number; size?: number }) {
  return request.get('/search/product/search', { params })
}

/**
 * 保存产品文档到 ES
 * @param data - 产品文档数据
 * @returns 保存结果
 */
export function saveProductDocument(data: any) {
  return request.post('/search/product', data)
}

/**
 * 根据 ID 查询 ES 产品文档
 * @param id - 文档 ID
 * @returns 产品文档信息
 */
export function getProductDocumentById(id: string) {
  return request.get(`/search/product/${id}`)
}

/**
 * 删除 ES 产品文档
 * @param id - 文档 ID
 * @returns 删除结果
 */
export function deleteProductDocument(id: string) {
  return request.delete(`/search/product/${id}`)
}
