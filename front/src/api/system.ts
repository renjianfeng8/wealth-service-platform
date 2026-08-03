import axios from 'axios'
import request from './index'
import type { PageParam, PageResult, UmsAdmin, UmsRole, UmsResource } from '@/types'

/**
 * 获取图形验证码（登录/注册使用）
 * @returns 验证码 KEY 与 Base64 图片
 */
export function getCaptcha() {
  return request.get<{ captchaKey: string; captchaImage: string }>('/system/captcha')
}

/**
 * 登出（fire-and-forget）：用裸 axios 绕过 request 拦截器，避免 refresh token 失效时
 * 401 → 拦截器再次登出/跳转的递归循环；调用方无需关心结果
 * @param refreshToken - refresh_token
 */
export function logoutApi(refreshToken: string) {
  return axios.post('/api/v1/system/umsAdmin/logout', null, {
    headers: { Authorization: `Bearer ${refreshToken}` },
  }).catch(() => undefined)
}

/**
 * 分页查询管理员列表
 * @param params - 查询参数（pageNum、pageSize）
 * @returns 分页结果包含管理员列表
 */
export function getAdminPage(params: { pageNum: number; pageSize: number; username?: string; status?: number }) {
  return request.get<PageResult<UmsAdmin>>('/system/umsAdmin/page', { params })
}

/**
 * 查询所有管理员列表（用于下拉选择器）
 * @returns 管理员列表
 */
export function getAdminList() {
  return request.get<UmsAdmin[]>('/system/umsAdmin')
}

/**
 * 根据 ID 查询管理员
 * @param id - 管理员 ID
 * @returns 管理员信息
 */
export function getAdminById(id: number | string) {
  return request.get<UmsAdmin>(`/system/umsAdmin/${id}`)
}

/**
 * 创建管理员
 * @param data - 管理员信息
 * @returns 创建结果
 */
export function createAdmin(data: UmsAdmin) {
  return request.post<boolean>('/system/umsAdmin', data)
}

/**
 * 更新管理员
 * @param id - 管理员 ID
 * @param data - 待更新的管理员信息
 * @returns 更新结果
 */
export function updateAdmin(id: number | string, data: Partial<UmsAdmin>) {
  return request.put<boolean>(`/system/umsAdmin/${id}`, data)
}

/**
 * 删除管理员
 * @param id - 管理员 ID
 * @returns 删除结果
 */
export function deleteAdmin(id: number | string) {
  return request.delete<boolean>(`/system/umsAdmin/${id}`)
}

/**
 * 重置管理员密码
 * @param data - { id, oldPassword, password }
 * @returns 重置结果
 */
export function resetAdminPassword(data: { id: number | string; oldPassword: string; password: string }) {
  return request.post<boolean>('/system/umsAdmin/resetPassword', data)
}

/**
 * 分页查询角色列表
 * @param params - 查询参数（pageNum、pageSize）
 * @returns 分页结果包含角色列表
 */
export function getRolePage(params: { pageNum: number; pageSize: number; name?: string; status?: number }) {
  return request.get<PageResult<UmsRole>>('/system/umsRole/page', { params })
}

/**
 * 查询所有角色列表（用于下拉选择器）
 * @returns 角色列表
 */
export function getRoleList() {
  return request.get<UmsRole[]>('/system/umsRole')
}

/**
 * 创建角色
 * @param data - 角色信息
 * @returns 创建结果
 */
export function createRole(data: UmsRole) {
  return request.post<boolean>('/system/umsRole', data)
}

/**
 * 更新角色
 * @param id - 角色 ID
 * @param data - 待更新的角色信息
 * @returns 更新结果
 */
export function updateRole(id: number | string, data: Partial<UmsRole>) {
  return request.put<boolean>(`/system/umsRole/${id}`, data)
}

/**
 * 删除角色
 * @param id - 角色 ID
 * @returns 删除结果
 */
export function deleteRole(id: number | string) {
  return request.delete<boolean>(`/system/umsRole/${id}`)
}

/**
 * 分页查询资源列表
 * @param params - 查询参数（pageNum、pageSize）
 * @returns 分页结果包含资源列表
 */
export function getResourcePage(params: { pageNum: number; pageSize: number; name?: string; url?: string }) {
  return request.get<PageResult<UmsResource>>('/system/umsResource/page', { params })
}

/**
 * 查询所有资源列表
 * @returns 资源列表
 */
export function getResourceList() {
  return request.get<UmsResource[]>('/system/umsResource')
}

/**
 * 创建资源
 * @param data - 资源信息
 * @returns 创建结果
 */
export function createResource(data: UmsResource) {
  return request.post<boolean>('/system/umsResource', data)
}

/**
 * 更新资源
 * @param id - 资源 ID
 * @param data - 待更新的资源信息
 * @returns 更新结果
 */
export function updateResource(id: number | string, data: Partial<UmsResource>) {
  return request.put<boolean>(`/system/umsResource/${id}`, data)
}

/**
 * 删除资源
 * @param id - 资源 ID
 * @returns 删除结果
 */
export function deleteResource(id: number | string) {
  return request.delete<boolean>(`/system/umsResource/${id}`)
}

/**
 * 分页查询管理员-角色关联列表
 * @param params - 查询参数（pageNum、pageSize，可选 adminId）
 * @returns 分页结果包含管理员-角色关联列表
 */
export function getAdminRoleRelationPage(params: PageParam & { adminId?: number | string }) {
  return request.get<PageResult<Record<string, any>>>('/system/umsAdminRoleRelation/page', { params })
}

/**
 * 创建管理员-角色关联
 * @param data - 关联信息
 * @returns 创建结果
 */
export function createAdminRoleRelation(data: { adminId?: number; roleId?: number }) {
  return request.post<boolean>('/system/umsAdminRoleRelation', data)
}

/**
 * 删除管理员-角色关联
 * @param id - 关联 ID
 * @returns 删除结果
 */
export function deleteAdminRoleRelation(id: number | string) {
  return request.delete<boolean>(`/system/umsAdminRoleRelation/${id}`)
}

/**
 * 分页查询角色-资源关联列表
 * @param params - 查询参数（pageNum、pageSize，可选 roleId）
 * @returns 分页结果包含角色-资源关联列表
 */
export function getRoleResourceRelationPage(params: PageParam & { roleId?: number | string }) {
  return request.get<PageResult<Record<string, any>>>('/system/umsRoleResourceRelation/page', { params })
}

/**
 * 创建角色-资源关联
 * @param data - 关联信息
 * @returns 创建结果
 */
export function createRoleResourceRelation(data: { roleId?: number; resourceId?: number }) {
  return request.post<boolean>('/system/umsRoleResourceRelation', data)
}

/**
 * 删除角色-资源关联
 * @param id - 关联 ID
 * @returns 删除结果
 */
export function deleteRoleResourceRelation(id: number | string) {
  return request.delete<boolean>(`/system/umsRoleResourceRelation/${id}`)
}
