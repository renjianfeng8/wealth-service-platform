import request from './index'

export function getFavoritePage(params: { pageNum: number; pageSize: number; userId?: number }) {
  return request.get('/product/wea-user-favorite/page', { params })
}

export function getFavoriteList() {
  return request.get('/product/wea-user-favorite')
}

export function createFavorite(data: { userId: number; productCode: string }) {
  return request.post('/product/wea-user-favorite', data)
}

export function deleteFavorite(id: number) {
  return request.delete(`/product/wea-user-favorite/${id}`)
}
