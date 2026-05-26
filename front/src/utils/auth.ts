const LOGIN_KEY = 'wealth_logged_in'
const USER_KEY = 'wealth_user'

/**
 * 返回登录状态标记（true/false）。JWT 已由后端写入 httpOnly Cookie，
 * 前端无法读取，仅用此标记判断是否已登录。
 */
export function getToken(): string | null {
  return sessionStorage.getItem(LOGIN_KEY)
}

/**
 * 记录登录状态。JWT 已由后端写入 httpOnly Cookie，此处不存储明文 token。
 */
export function setToken(_token: string) {
  sessionStorage.setItem(LOGIN_KEY, 'true')
}

export function removeToken() {
  sessionStorage.removeItem(LOGIN_KEY)
  sessionStorage.removeItem(USER_KEY)
}

export function getStoredUser(): any {
  const raw = sessionStorage.getItem(USER_KEY)
  return raw ? JSON.parse(raw) : null
}

export function setStoredUser(user: any) {
  sessionStorage.setItem(USER_KEY, JSON.stringify(user))
}
