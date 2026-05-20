import request from './index'

export function getFavoritePage(params: { pageNum: number; pageSize: number; userId?: number }) {
  return request.get('/account/wea-user-favorite/page', { params })
}

export function getFavoriteList() {
  return request.get('/account/wea-user-favorite')
}

export function createFavorite(data: { userId: number; productCode: string }) {
  return request.post('/account/wea-user-favorite', data)
}

export function deleteFavorite(id: number) {
  return request.delete(`/account/wea-user-favorite/${id}`)
}
