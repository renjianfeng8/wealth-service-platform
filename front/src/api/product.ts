import request from './index'

// 产品
export function getProductPage(params: { pageNum: number; pageSize: number }) {
  return request.get('/product/finProduct/page', { params })
}

export function getProductList() {
  return request.get('/product/finProduct')
}

export function getProductById(id: number) {
  return request.get(`/product/finProduct/${id}`)
}

export function createProduct(data: any) {
  return request.post('/product/finProduct', data)
}

export function updateProduct(id: number, data: any) {
  return request.put(`/product/finProduct/${id}`, data)
}

export function deleteProduct(id: number) {
  return request.delete(`/product/finProduct/${id}`)
}

// 行情数据
export function getMarketDataPage(params: { pageNum: number; pageSize: number }) {
  return request.get('/product/finMarketData/page', { params })
}

export function getMarketDataList() {
  return request.get('/product/finMarketData')
}

export function getMarketDataById(id: number) {
  return request.get(`/product/finMarketData/${id}`)
}

export function createMarketData(data: any) {
  return request.post('/product/finMarketData', data)
}

export function updateMarketData(id: number, data: any) {
  return request.put(`/product/finMarketData/${id}`, data)
}

export function deleteMarketData(id: number) {
  return request.delete(`/product/finMarketData/${id}`)
}
