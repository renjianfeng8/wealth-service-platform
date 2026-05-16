import request from './index'

/** 产品 */
export function getProductPage(params: { pageNum: number; pageSize: number; productType?: number }) {
  return request.get('/product/weaProduct/page', { params })
}

export function getProductList() {
  return request.get('/product/weaProduct')
}

export function getProductById(id: number) {
  return request.get(`/product/weaProduct/${id}`)
}

/** 行情 */
export function getMarketDataPage(params: { pageNum: number; pageSize: number }) {
  return request.get('/product/weaMarketData/page', { params })
}

export function getMarketDataList() {
  return request.get('/product/weaMarketData')
}



