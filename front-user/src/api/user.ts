import request from './index'

export function userLogin(data: { username: string; password: string }) {
  return request.post('/user/login', data)
}

export function getUserList() {
  return request.get('/user')
}

export function getUserInfo(id: number) {
  return request.get(`/user/${id}`)
}

export function getUserPage(params: { pageNum: number; pageSize: number }) {
  return request.get('/user/page', { params })
}

export function updateUser(id: number, data: any) {
  return request.put(`/user/${id}`, data)
}

export function registerUser(data: any) {
  return request.post('/user/register', data)
}

export function resetPassword(data: any) {
  return request.post('/user/resetPassword', data)
}
