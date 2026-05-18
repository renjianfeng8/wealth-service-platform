import request from './index'

// 产品
export function getProductPage(params: { pageNum: number; pageSize: number; productName?: string; productCode?: string; productType?: number }) {
  return request.get('/product/WeaProduct/page', { params })
}

export function getProductList() {
  return request.get('/product/WeaProduct')
}

export function getProductById(id: number) {
  return request.get(`/product/WeaProduct/${id}`)
}

export function createProduct(data: any) {
  return request.post('/product/WeaProduct', data)
}

export function updateProduct(id: number, data: any) {
  return request.put(`/product/WeaProduct/${id}`, data)
}

export function deleteProduct(id: number) {
  return request.delete(`/product/WeaProduct/${id}`)
}

// 行情数据
export function getMarketDataPage(params: { pageNum: number; pageSize: number }) {
  return request.get('/product/WeaMarketData/page', { params })
}

export function getMarketDataList() {
  return request.get('/product/WeaMarketData')
}

export function getMarketDataById(id: number) {
  return request.get(`/product/WeaMarketData/${id}`)
}

export function createMarketData(data: any) {
  return request.post('/product/WeaMarketData', data)
}

export function updateMarketData(id: number, data: any) {
  return request.put(`/product/WeaMarketData/${id}`, data)
}

export function deleteMarketData(id: number) {
  return request.delete(`/product/WeaMarketData/${id}`)
}
