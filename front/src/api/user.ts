import request from './index'

export function getUserPage(params: { pageNum: number; pageSize: number }) {
  return request.get('/user/user/page', { params })
}

export function getUserList() {
  return request.get('/user/user')
}

export function getUserById(id: number) {
  return request.get(`/user/user/${id}`)
}

export function createUser(data: any) {
  return request.post('/user/user', data)
}

export function updateUser(id: number, data: any) {
  return request.put(`/user/user/${id}`, data)
}

export function deleteUser(id: number) {
  return request.delete(`/user/user/${id}`)
}

export function deleteUserBatch(ids: number[]) {
  return request.delete('/user/user/batch', { data: ids })
}

export function registerUser(data: any) {
  return request.post('/user/user/register', data)
}

export function resetPassword(data: any) {
  return request.post('/user/user/resetPassword', data)
}

export function userLogin(data: { username: string; password: string }) {
  return request.post('/user/user/login', data)
}
