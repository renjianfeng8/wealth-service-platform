import request from './index'

export function getTradeOrderPage(params: any) {
  return request.get('/trade/weaTradeOrder/page', { params })
}

export function getTradeOrderList() {
  return request.get('/trade/weaTradeOrder')
}

export function getTradeOrderById(id: number) {
  return request.get(`/trade/weaTradeOrder/${id}`)
}

export function createTradeOrder(data: any) {
  return request.post('/trade/weaTradeOrder', data)
}

export function updateTradeOrder(id: number, data: any) {
  return request.put(`/trade/weaTradeOrder/${id}`, data)
}

export function deleteTradeOrder(id: number) {
  return request.delete(`/trade/weaTradeOrder/${id}`)
}
