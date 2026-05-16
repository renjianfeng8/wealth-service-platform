import request from './index'

export function getFavoritePage(params: any) {
  return request.get('/account/weaUserFavorite/page', { params })
}

export function getFavoriteList() {
  return request.get('/account/weaUserFavorite')
}

export function getFavoriteById(id: number) {
  return request.get(`/account/weaUserFavorite/${id}`)
}

export function createFavorite(data: any) {
  return request.post('/account/weaUserFavorite', data)
}

export function updateFavorite(id: number, data: any) {
  return request.put(`/account/weaUserFavorite/${id}`, data)
}

export function deleteFavorite(id: number) {
  return request.delete(`/account/weaUserFavorite/${id}`)
}
