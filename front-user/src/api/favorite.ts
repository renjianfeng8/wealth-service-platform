import request from './index'

export function getFavoritePage(params: { pageNum: number; pageSize: number; userId?: number }) {
  return request.get('/account/finUserFavorite/page', { params })
}

export function getFavoriteList() {
  return request.get('/account/finUserFavorite')
}

export function createFavorite(data: { userId: number; productCode: string }) {
  return request.post('/account/finUserFavorite', data)
}

export function deleteFavorite(id: number) {
  return request.delete(`/account/finUserFavorite/${id}`)
}
