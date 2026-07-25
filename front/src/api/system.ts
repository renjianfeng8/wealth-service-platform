import request from './index'
import type { PageParam, UmsAdmin, UmsRole, UmsResource } from '@/types'

/**
 * 管理员登录
 * @param data - 登录参数（username、password）
 * @returns 登录结果包含 JWT token
 */
export function loginApi(data: { username: string; password: string }) {
  return request.post('/system/umsAdmin/login', data)
}

/**
 * 分页查询管理员列表
 * @param params - 查询参数（pageNum、pageSize）
 * @returns 分页结果包含管理员列表
 */
export function getAdminPage(params: { pageNum: number; pageSize: number; username?: string; status?: number }) {
  return request.get('/system/umsAdmin/page', { params })
}

/**
 * 查询所有管理员列表
 * @returns 管理员列表
 */
export function getAdminList() {
  return request.get('/system/umsAdmin')
}

/**
 * 根据 ID 查询管理员
 * @param id - 管理员 ID
 * @returns 管理员信息
 */
export function getAdminById(id: number) {
  return request.get(`/system/umsAdmin/${id}`)
}

/**
 * 创建管理员
 * @param data - 管理员信息
 * @returns 创建结果
 */
export function createAdmin(data: UmsAdmin) {
  return request.post('/system/umsAdmin', data)
}

/**
 * 更新管理员
 * @param id - 管理员 ID
 * @param data - 待更新的管理员信息
 * @returns 更新结果
 */
export function updateAdmin(id: number, data: Partial<UmsAdmin>) {
  return request.put(`/system/umsAdmin/${id}`, data)
}

/**
 * 删除管理员
 * @param id - 管理员 ID
 * @returns 删除结果
 */
export function deleteAdmin(id: number) {
  return request.delete(`/system/umsAdmin/${id}`)
}

/**
 * 重置管理员密码
 * @param data - { id, oldPassword, password }
 * @returns 重置结果
 */
export function resetAdminPassword(data: { id: number; oldPassword: string; password: string }) {
  return request.post('/system/umsAdmin/resetPassword', data)
}

/**
 * 分页查询角色列表
 * @param params - 查询参数（pageNum、pageSize）
 * @returns 分页结果包含角色列表
 */
export function getRolePage(params: { pageNum: number; pageSize: number; name?: string; status?: number }) {
  return request.get('/system/umsRole/page', { params })
}

/**
 * 查询所有角色列表
 * @returns 角色列表
 */
export function getRoleList() {
  return request.get('/system/umsRole')
}

/**
 * 根据 ID 查询角色
 * @param id - 角色 ID
 * @returns 角色信息
 */
export function getRoleById(id: number) {
  return request.get(`/system/umsRole/${id}`)
}

/**
 * 创建角色
 * @param data - 角色信息
 * @returns 创建结果
 */
export function createRole(data: UmsRole) {
  return request.post('/system/umsRole', data)
}

/**
 * 更新角色
 * @param id - 角色 ID
 * @param data - 待更新的角色信息
 * @returns 更新结果
 */
export function updateRole(id: number, data: Partial<UmsRole>) {
  return request.put(`/system/umsRole/${id}`, data)
}

/**
 * 删除角色
 * @param id - 角色 ID
 * @returns 删除结果
 */
export function deleteRole(id: number) {
  return request.delete(`/system/umsRole/${id}`)
}

/**
 * 分页查询资源列表
 * @param params - 查询参数（pageNum、pageSize）
 * @returns 分页结果包含资源列表
 */
export function getResourcePage(params: { pageNum: number; pageSize: number; name?: string; url?: string }) {
  return request.get('/system/umsResource/page', { params })
}

/**
 * 查询所有资源列表
 * @returns 资源列表
 */
export function getResourceList() {
  return request.get('/system/umsResource')
}

/**
 * 根据 ID 查询资源
 * @param id - 资源 ID
 * @returns 资源信息
 */
export function getResourceById(id: number) {
  return request.get(`/system/umsResource/${id}`)
}

/**
 * 创建资源
 * @param data - 资源信息
 * @returns 创建结果
 */
export function createResource(data: UmsResource) {
  return request.post('/system/umsResource', data)
}

/**
 * 更新资源
 * @param id - 资源 ID
 * @param data - 待更新的资源信息
 * @returns 更新结果
 */
export function updateResource(id: number, data: Partial<UmsResource>) {
  return request.put(`/system/umsResource/${id}`, data)
}

/**
 * 删除资源
 * @param id - 资源 ID
 * @returns 删除结果
 */
export function deleteResource(id: number) {
  return request.delete(`/system/umsResource/${id}`)
}

/**
 * 分页查询管理员-角色关联列表
 * @param params - 查询参数（pageNum、pageSize，可选 adminId）
 * @returns 分页结果包含管理员-角色关联列表
 */
export function getAdminRoleRelationPage(params: PageParam & { adminId?: number | string }) {
  return request.get('/system/umsAdminRoleRelation/page', { params })
}

/**
 * 创建管理员-角色关联
 * @param data - 关联信息
 * @returns 创建结果
 */
export function createAdminRoleRelation(data: { adminId?: number; roleId?: number }) {
  return request.post('/system/umsAdminRoleRelation', data)
}

/**
 * 删除管理员-角色关联
 * @param id - 关联 ID
 * @returns 删除结果
 */
export function deleteAdminRoleRelation(id: number) {
  return request.delete(`/system/umsAdminRoleRelation/${id}`)
}

/**
 * 分页查询角色-资源关联列表
 * @param params - 查询参数（pageNum、pageSize，可选 roleId）
 * @returns 分页结果包含角色-资源关联列表
 */
export function getRoleResourceRelationPage(params: PageParam & { roleId?: number | string }) {
  return request.get('/system/umsRoleResourceRelation/page', { params })
}

/**
 * 创建角色-资源关联
 * @param data - 关联信息
 * @returns 创建结果
 */
export function createRoleResourceRelation(data: { roleId?: number; resourceId?: number }) {
  return request.post('/system/umsRoleResourceRelation', data)
}

/**
 * 删除角色-资源关联
 * @param id - 关联 ID
 * @returns 删除结果
 */
export function deleteRoleResourceRelation(id: number) {
  return request.delete(`/system/umsRoleResourceRelation/${id}`)
}
