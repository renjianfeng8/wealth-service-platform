import request from './index'

export function getFavoritePage(params: any) {
  return request.get('/account/wea-user-favorite/page', { params })
}

export function getFavoriteList() {
  return request.get('/account/wea-user-favorite')
}

export function getFavoriteById(id: number) {
  return request.get(`/account/wea-user-favorite/${id}`)
}

export function createFavorite(data: any) {
  return request.post('/account/wea-user-favorite', data)
}

export function updateFavorite(id: number, data: any) {
  return request.put(`/account/wea-user-favorite/${id}`, data)
}

export function deleteFavorite(id: number) {
  return request.delete(`/account/wea-user-favorite/${id}`)
}
