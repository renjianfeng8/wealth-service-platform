import request from './index'

// 管理员登录
export function loginApi(data: { username: string; password: string }) {
  return request.post('/system/umsAdmin/login', data)
}

// 管理员 CRUD
export function getAdminPage(params: { pageNum: number; pageSize: number }) {
  return request.get('/system/umsAdmin/page', { params })
}

export function getAdminList() {
  return request.get('/system/umsAdmin')
}

export function getAdminById(id: number) {
  return request.get(`/system/umsAdmin/${id}`)
}

export function createAdmin(data: any) {
  return request.post('/system/umsAdmin', data)
}

export function updateAdmin(id: number, data: any) {
  return request.put(`/system/umsAdmin/${id}`, data)
}

export function deleteAdmin(id: number) {
  return request.delete(`/system/umsAdmin/${id}`)
}

// 角色 CRUD
export function getRolePage(params: { pageNum: number; pageSize: number }) {
  return request.get('/system/umsRole/page', { params })
}

export function getRoleList() {
  return request.get('/system/umsRole')
}

export function getRoleById(id: number) {
  return request.get(`/system/umsRole/${id}`)
}

export function createRole(data: any) {
  return request.post('/system/umsRole', data)
}

export function updateRole(id: number, data: any) {
  return request.put(`/system/umsRole/${id}`, data)
}

export function deleteRole(id: number) {
  return request.delete(`/system/umsRole/${id}`)
}

// 资源 CRUD
export function getResourcePage(params: { pageNum: number; pageSize: number }) {
  return request.get('/system/umsResource/page', { params })
}

export function getResourceList() {
  return request.get('/system/umsResource')
}

export function getResourceById(id: number) {
  return request.get(`/system/umsResource/${id}`)
}

export function createResource(data: any) {
  return request.post('/system/umsResource', data)
}

export function updateResource(id: number, data: any) {
  return request.put(`/system/umsResource/${id}`, data)
}

export function deleteResource(id: number) {
  return request.delete(`/system/umsResource/${id}`)
}

// 管理员-角色关联
export function getAdminRoleRelationPage(params: any) {
  return request.get('/system/umsAdminRoleRelation/page', { params })
}

export function createAdminRoleRelation(data: any) {
  return request.post('/system/umsAdminRoleRelation', data)
}

export function deleteAdminRoleRelation(id: number) {
  return request.delete(`/system/umsAdminRoleRelation/${id}`)
}

// 角色-资源关联
export function getRoleResourceRelationPage(params: any) {
  return request.get('/system/umsRoleResourceRelation/page', { params })
}

export function createRoleResourceRelation(data: any) {
  return request.post('/system/umsRoleResourceRelation', data)
}

export function deleteRoleResourceRelation(id: number) {
  return request.delete(`/system/umsRoleResourceRelation/${id}`)
}
