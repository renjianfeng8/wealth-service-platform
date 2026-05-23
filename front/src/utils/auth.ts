const LOGIN_KEY = 'wealth_admin_logged_in'
const USER_KEY = 'wealth_admin_user'

/**
 * 返回登录状态标记（true/false）。JWT 已由后端写入 httpOnly Cookie，
 * 前端无法读取，仅用此标记判断是否已登录。
 */
export function getToken(): string | null {
  return localStorage.getItem(LOGIN_KEY)
}

/**
 * 记录登录状态。JWT 已由后端写入 httpOnly Cookie，此处不存储明文 token。
 */
export function setToken(_token: string) {
  localStorage.setItem(LOGIN_KEY, 'true')
}

export function removeToken() {
  localStorage.removeItem(LOGIN_KEY)
  localStorage.removeItem(USER_KEY)
}

export function getStoredUser(): any {
  const raw = localStorage.getItem(USER_KEY)
  return raw ? JSON.parse(raw) : null
}

export function setStoredUser(user: any) {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}
