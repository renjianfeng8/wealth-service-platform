import request from './index'

// 产品
export function getProductPage(params: { pageNum: number; pageSize: number; productName?: string; productCode?: string; productType?: number }) {
  return request.get('/product/wea-product/page', { params })
}

export function getProductList() {
  return request.get('/product/wea-product')
}

export function getProductById(id: number) {
  return request.get(`/product/wea-product/${id}`)
}

export function createProduct(data: any) {
  return request.post('/product/wea-product', data)
}

export function updateProduct(id: number, data: any) {
  return request.put(`/product/wea-product/${id}`, data)
}

export function deleteProduct(id: number) {
  return request.delete(`/product/wea-product/${id}`)
}

// 行情数据
export function getMarketDataPage(params: { pageNum: number; pageSize: number }) {
  return request.get('/product/wea-market-data/page', { params })
}

export function getMarketDataList() {
  return request.get('/product/wea-market-data')
}

export function getMarketDataById(id: number) {
  return request.get(`/product/wea-market-data/${id}`)
}

export function createMarketData(data: any) {
  return request.post('/product/wea-market-data', data)
}

export function updateMarketData(id: number, data: any) {
  return request.put(`/product/wea-market-data/${id}`, data)
}

export function deleteMarketData(id: number) {
  return request.delete(`/product/wea-market-data/${id}`)
}
