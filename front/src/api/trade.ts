import request from './index'

export function getTradeOrderPage(params: any) {
  return request.get('/trade/WeaTradeOrder/page', { params })
}

export function getTradeOrderList() {
  return request.get('/trade/WeaTradeOrder')
}

export function getTradeOrderById(id: number) {
  return request.get(`/trade/WeaTradeOrder/${id}`)
}

export function createTradeOrder(data: any) {
  return request.post('/trade/WeaTradeOrder', data)
}

export function updateTradeOrder(id: number, data: any) {
  return request.put(`/trade/WeaTradeOrder/${id}`, data)
}

export function deleteTradeOrder(id: number) {
  return request.delete(`/trade/WeaTradeOrder/${id}`)
}
