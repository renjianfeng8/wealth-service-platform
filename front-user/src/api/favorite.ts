import request from './index'

export function getFavoritePage(params: { pageNum: number; pageSize: number; userId?: number }) {
  return request.get('/account/WeaUserFavorite/page', { params })
}

export function getFavoriteList() {
  return request.get('/account/WeaUserFavorite')
}

export function createFavorite(data: { userId: number; productCode: string }) {
  return request.post('/account/WeaUserFavorite', data)
}

export function deleteFavorite(id: number) {
  return request.delete(`/account/WeaUserFavorite/${id}`)
}
