import request from './index'

export function getFavoritePage(params: any) {
  return request.get('/account/WeaUserFavorite/page', { params })
}

export function getFavoriteList() {
  return request.get('/account/WeaUserFavorite')
}

export function getFavoriteById(id: number) {
  return request.get(`/account/WeaUserFavorite/${id}`)
}

export function createFavorite(data: any) {
  return request.post('/account/WeaUserFavorite', data)
}

export function updateFavorite(id: number, data: any) {
  return request.put(`/account/WeaUserFavorite/${id}`, data)
}

export function deleteFavorite(id: number) {
  return request.delete(`/account/WeaUserFavorite/${id}`)
}
