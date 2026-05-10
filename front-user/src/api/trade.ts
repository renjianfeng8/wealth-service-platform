import request from './index'

export function getTradeOrderPage(params: {
  pageNum: number
  pageSize: number
  userId?: number
  orderStatus?: number
}) {
  return request.get('/trade/finTradeOrder/page', { params })
}

export function getTradeOrderList() {
  return request.get('/trade/finTradeOrder')
}

export function getTradeOrderById(id: number) {
  return request.get(`/trade/finTradeOrder/${id}`)
}

export function createTradeOrder(data: {
  userId: number
  productCode: string
  tradeType: number
  entrustPrice: number
  entrustNum: number
}) {
  return request.post('/trade/finTradeOrder', data)
}

export function cancelTradeOrder(id: number) {
  return request.put(`/trade/finTradeOrder/${id}`, { orderStatus: 2 })
}
