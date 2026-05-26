import request from './index'

export function getTradeOrderPage(params: any) {
  return request.get('/trade/wea-trade-order/page', { params })
}

export function getTradeOrderList() {
  return request.get('/trade/wea-trade-order')
}

export function getTradeOrderById(id: number) {
  return request.get(`/trade/wea-trade-order/${id}`)
}

export function createTradeOrder(data: any) {
  return request.post('/trade/wea-trade-order', data)
}

export function updateTradeOrder(id: number, data: any) {
  return request.put(`/trade/wea-trade-order/${id}`, data)
}

export function deleteTradeOrder(id: number) {
  return request.delete(`/trade/wea-trade-order/${id}`)
}

export function cancelTradeOrder(id: number) {
  return request.put(`/trade/wea-trade-order/${id}`, { orderStatus: 2 })
}
