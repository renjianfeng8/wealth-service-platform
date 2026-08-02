import request from './index'
import type { UserInfo } from '@/types'

/**
 * 分页查询用户列表
 * @param params - 查询参数（pageNum、pageSize 必填，可选 username、status）
 * @returns 分页结果包含用户列表
 */
export function getUserPage(params: { pageNum: number; pageSize: number; username?: string; status?: number }) {
  return request.get('/user/page', { params })
}

/**
 * 根据 ID 查询用户
 * @param id - 用户 ID
 * @returns 用户信息
 */
export function getUserById(id: number) {
  return request.get(`/user/${id}`)
}

/** getUserInfo 是 getUserById 的别名，兼容用户前台 profile 视图 */
export { getUserById as getUserInfo }

/**
 * 创建用户
 * @param data - 用户信息
 * @returns 创建结果
 */
export function createUser(data: { username: string; password?: string; nickname?: string; phone?: string; status?: number }) {
  return request.post('/user', data)
}

/**
 * 更新用户
 * @param id - 用户 ID
 * @param data - 待更新的用户信息
 * @returns 更新结果
 */
export function updateUser(id: number, data: Partial<UserInfo>) {
  return request.put(`/user/${id}`, data)
}

/**
 * 删除用户
 * @param id - 用户 ID
 * @returns 删除结果
 */
export function deleteUser(id: number) {
  return request.delete(`/user/${id}`)
}

/**
 * 批量删除用户
 * @param ids - 用户 ID 数组
 * @returns 删除结果
 */
export function deleteUserBatch(ids: number[]) {
  // D1: 包裹为 { ids } 格式，避免 DELETE 请求裸数组 body 不被后端解析
  return request.delete('/user/batch', { data: { ids } })
}

/**
 * 用户注册
 * @param data - 注册信息
 * @returns 注册结果
 */
export function registerUser(data: { username: string; password: string; captchaKey?: string; captchaCode?: string }) {
  return request.post('/user/register', data)
}

/**
 * 重置密码
 * @param data - 重置密码参数
 * @returns 重置结果
 */
export function resetPassword(data: { id: number; oldPassword: string; password: string }) {
  return request.post('/user/resetPassword', data)
}

/**
 * 用户登录
 * @param data - 登录参数（username、password）
 * @returns 登录结果包含用户信息
 */
export function userLogin(data: { username: string; password: string }) {
  return request.post('/user/login', data)
}

/**
 * 统一登录（自动识别管理员/普通用户）
 * @param data - 登录参数（username、password）
 * @returns 登录结果包含 token、userId、nickname 及角色
 */
export function identifyLogin(data: { username: string; password: string; captchaKey?: string; captchaCode?: string }) {
  return request.post('/user/identify-login', data)
}
