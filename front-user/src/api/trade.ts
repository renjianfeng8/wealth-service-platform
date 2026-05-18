import request from './index'

export function getTradeOrderPage(params: {
  pageNum: number
  pageSize: number
  userId?: number
  orderStatus?: number
}) {
  return request.get('/trade/weaTradeOrder/page', { params })
}

export function getTradeOrderList() {
  return request.get('/trade/weaTradeOrder')
}

export function getTradeOrderById(id: number) {
  return request.get(`/trade/weaTradeOrder/${id}`)
}

export function createTradeOrder(data: {
  userId: number
  productCode: string
  tradeType: number
  entrustPrice: number
  entrustNum: number
  idempotentKey?: string
}) {
  return request.post('/trade/weaTradeOrder', data)
}

export function cancelTradeOrder(id: number) {
  return request.put(`/trade/weaTradeOrder/${id}`, { orderStatus: 2 })
}
