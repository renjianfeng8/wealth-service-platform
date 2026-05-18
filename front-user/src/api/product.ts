import request from './index'

/** 产品 */
export function getProductPage(params: { pageNum: number; pageSize: number; productType?: number }) {
  return request.get('/product/WeaProduct/page', { params })
}

export function getProductList() {
  return request.get('/product/WeaProduct')
}

export function getProductById(id: number) {
  return request.get(`/product/WeaProduct/${id}`)
}

/** 行情 */
export function getMarketDataPage(params: { pageNum: number; pageSize: number }) {
  return request.get('/product/WeaMarketData/page', { params })
}

export function getMarketDataList() {
  return request.get('/product/WeaMarketData')
}



