import request from './index'

/**
 * 分页查询产品列表
 * @param params - 查询参数（pageNum、pageSize 必填，可选 productName、productCode、productType）
 * @returns 分页结果包含产品列表
 */
export function getProductPage(params: { pageNum: number; pageSize: number; productName?: string; productCode?: string; productType?: number; orderBy?: string; orderDir?: string }) {
  return request.get('/product/wea-product/page', { params })
}

/**
 * 查询所有产品列表
 * @returns 产品列表
 */
export function getProductList() {
  return request.get('/product/wea-product')
}

/**
 * 根据 ID 查询产品
 * @param id - 产品 ID
 * @returns 产品信息
 */
export function getProductById(id: number) {
  return request.get(`/product/wea-product/${id}`)
}

/**
 * 创建产品
 * @param data - 产品信息
 * @returns 创建结果
 */
export function createProduct(data: any) {
  return request.post('/product/wea-product', data)
}

/**
 * 更新产品
 * @param id - 产品 ID
 * @param data - 待更新的产品信息
 * @returns 更新结果
 */
export function updateProduct(id: number, data: any) {
  return request.put(`/product/wea-product/${id}`, data)
}

/**
 * 删除产品
 * @param id - 产品 ID
 * @returns 删除结果
 */
export function deleteProduct(id: number) {
  return request.delete(`/product/wea-product/${id}`)
}

/**
 * 分页查询行情数据列表
 * @param params - 查询参数（pageNum、pageSize）
 * @returns 分页结果包含行情数据列表
 */
export function getMarketDataPage(params: { pageNum: number; pageSize: number }) {
  return request.get('/product/wea-market-data/page', { params })
}

/**
 * 查询所有行情数据列表
 * @returns 行情数据列表
 */
export function getMarketDataList() {
  return request.get('/product/wea-market-data')
}

/**
 * 根据 ID 查询行情数据
 * @param id - 行情数据 ID
 * @returns 行情数据信息
 */
export function getMarketDataById(id: number) {
  return request.get(`/product/wea-market-data/${id}`)
}

/**
 * 创建行情数据
 * @param data - 行情数据信息
 * @returns 创建结果
 */
export function createMarketData(data: any) {
  return request.post('/product/wea-market-data', data)
}

/**
 * 更新行情数据
 * @param id - 行情数据 ID
 * @param data - 待更新的行情数据信息
 * @returns 更新结果
 */
export function updateMarketData(id: number, data: any) {
  return request.put(`/product/wea-market-data/${id}`, data)
}

/**
 * 删除行情数据
 * @param id - 行情数据 ID
 * @returns 删除结果
 */
export function deleteMarketData(id: number) {
  return request.delete(`/product/wea-market-data/${id}`)
}
