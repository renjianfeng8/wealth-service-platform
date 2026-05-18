import request from './index'

export function getTradeOrderPage(params: {
  pageNum: number
  pageSize: number
  userId?: number
  orderStatus?: number
}) {
  return request.get('/trade/WeaTradeOrder/page', { params })
}

export function getTradeOrderList() {
  return request.get('/trade/WeaTradeOrder')
}

export function getTradeOrderById(id: number) {
  return request.get(`/trade/WeaTradeOrder/${id}`)
}

export function createTradeOrder(data: {
  userId: number
  productCode: string
  tradeType: number
  entrustPrice: number
  entrustNum: number
  idempotentKey?: string
}) {
  return request.post('/trade/WeaTradeOrder', data)
}

export function cancelTradeOrder(id: number) {
  return request.put(`/trade/WeaTradeOrder/${id}`, { orderStatus: 2 })
}
