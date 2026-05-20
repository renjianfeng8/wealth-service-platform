import request from './index'

/** 产品 */
export function getProductPage(params: { pageNum: number; pageSize: number; productType?: number }) {
  return request.get('/product/wea-product/page', { params })
}

export function getProductList() {
  return request.get('/product/wea-product')
}

export function getProductById(id: number) {
  return request.get(`/product/wea-product/${id}`)
}

/** 行情 */
export function getMarketDataPage(params: { pageNum: number; pageSize: number }) {
  return request.get('/product/wea-market-data/page', { params })
}

export function getMarketDataList() {
  return request.get('/product/wea-market-data')
}



