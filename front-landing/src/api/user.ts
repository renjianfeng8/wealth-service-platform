import request from './index'

export function identifyLogin(data: { username: string; password: string }) {
  return request.post('/user/identify-login', data)
}
