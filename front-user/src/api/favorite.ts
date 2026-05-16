import request from './index'

export function getFavoritePage(params: { pageNum: number; pageSize: number; userId?: number }) {
  return request.get('/account/weaUserFavorite/page', { params })
}

export function getFavoriteList() {
  return request.get('/account/weaUserFavorite')
}

export function createFavorite(data: { userId: number; productCode: string }) {
  return request.post('/account/weaUserFavorite', data)
}

export function deleteFavorite(id: number) {
  return request.delete(`/account/weaUserFavorite/${id}`)
}
