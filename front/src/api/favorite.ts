import request from './index'

export function getFavoritePage(params: any) {
  return request.get('/account/finUserFavorite/page', { params })
}

export function getFavoriteList() {
  return request.get('/account/finUserFavorite')
}

export function getFavoriteById(id: number) {
  return request.get(`/account/finUserFavorite/${id}`)
}

export function createFavorite(data: any) {
  return request.post('/account/finUserFavorite', data)
}

export function updateFavorite(id: number, data: any) {
  return request.put(`/account/finUserFavorite/${id}`, data)
}

export function deleteFavorite(id: number) {
  return request.delete(`/account/finUserFavorite/${id}`)
}
