import request from './index'

export function getTradeOrderPage(params: any) {
  return request.get('/trade/finTradeOrder/page', { params })
}

export function getTradeOrderList() {
  return request.get('/trade/finTradeOrder')
}

export function getTradeOrderById(id: number) {
  return request.get(`/trade/finTradeOrder/${id}`)
}

export function createTradeOrder(data: any) {
  return request.post('/trade/finTradeOrder', data)
}

export function updateTradeOrder(id: number, data: any) {
  return request.put(`/trade/finTradeOrder/${id}`, data)
}

export function deleteTradeOrder(id: number) {
  return request.delete(`/trade/finTradeOrder/${id}`)
}
