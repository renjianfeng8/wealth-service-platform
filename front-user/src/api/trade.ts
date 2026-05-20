import request from './index'

export function getTradeOrderPage(params: {
  pageNum: number
  pageSize: number
  userId?: number
  orderStatus?: number
}) {
  return request.get('/trade/wea-trade-order/page', { params })
}

export function getTradeOrderList() {
  return request.get('/trade/wea-trade-order')
}

export function getTradeOrderById(id: number) {
  return request.get(`/trade/wea-trade-order/${id}`)
}

export function createTradeOrder(data: {
  userId: number
  productCode: string
  tradeType: number
  entrustPrice: number
  entrustNum: number
  idempotentKey?: string
}) {
  return request.post('/trade/wea-trade-order', data)
}

export function cancelTradeOrder(id: number) {
  return request.put(`/trade/wea-trade-order/${id}`, { orderStatus: 2 })
}
