import request from './index'

// 产品
export function getProductPage(params: { pageNum: number; pageSize: number; productName?: string; productCode?: string; productType?: number }) {
  return request.get('/product/weaProduct/page', { params })
}

export function getProductList() {
  return request.get('/product/weaProduct')
}

export function getProductById(id: number) {
  return request.get(`/product/weaProduct/${id}`)
}

export function createProduct(data: any) {
  return request.post('/product/weaProduct', data)
}

export function updateProduct(id: number, data: any) {
  return request.put(`/product/weaProduct/${id}`, data)
}

export function deleteProduct(id: number) {
  return request.delete(`/product/weaProduct/${id}`)
}

// 行情数据
export function getMarketDataPage(params: { pageNum: number; pageSize: number }) {
  return request.get('/product/weaMarketData/page', { params })
}

export function getMarketDataList() {
  return request.get('/product/weaMarketData')
}

export function getMarketDataById(id: number) {
  return request.get(`/product/weaMarketData/${id}`)
}

export function createMarketData(data: any) {
  return request.post('/product/weaMarketData', data)
}

export function updateMarketData(id: number, data: any) {
  return request.put(`/product/weaMarketData/${id}`, data)
}

export function deleteMarketData(id: number) {
  return request.delete(`/product/weaMarketData/${id}`)
}
