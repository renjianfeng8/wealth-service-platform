import request from './index'

export function getUserPage(params: { pageNum: number; pageSize: number; username?: string; status?: number }) {
  return request.get('/user/page', { params })
}

export function getUserList() {
  return request.get('/user')
}

export function getUserById(id: number) {
  return request.get(`/user/${id}`)
}

/** getUserInfo 是 getUserById 的别名，兼容用户前台 profile 视图 */
export { getUserById as getUserInfo }

export function createUser(data: any) {
  return request.post('/user', data)
}

export function updateUser(id: number, data: any) {
  return request.put(`/user/${id}`, data)
}

export function deleteUser(id: number) {
  return request.delete(`/user/${id}`)
}

export function deleteUserBatch(ids: number[]) {
  return request.delete('/user/batch', { data: ids })
}

export function registerUser(data: any) {
  return request.post('/user/register', data)
}

export function resetPassword(data: any) {
  return request.post('/user/resetPassword', data)
}

export function userLogin(data: { username: string; password: string }) {
  return request.post('/user/login', data)
}
