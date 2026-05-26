const LOGIN_KEY = 'wealth_logged_in'
const USER_KEY = 'wealth_user'

/**
 * 返回登录状态标记（true/false）。JWT 已由后端写入 httpOnly Cookie，
 * 前端无法读取，仅用此标记判断是否已登录。
 * @returns 登录状态标记，未登录时返回 null
 */
export function getToken(): string | null {
  return sessionStorage.getItem(LOGIN_KEY)
}

/**
 * 记录登录状态。JWT 已由后端写入 httpOnly Cookie，此处不存储明文 token。
 * @param _token - 登录 token（实际未使用，仅标记登录状态为 true）
 */
export function setToken(_token: string) {
  sessionStorage.setItem(LOGIN_KEY, 'true')
}

/**
 * 清除登录状态和用户信息
 */
export function removeToken() {
  sessionStorage.removeItem(LOGIN_KEY)
  sessionStorage.removeItem(USER_KEY)
}

/**
 * 获取存储的用户信息
 * @returns 用户信息对象，无数据时返回 null
 */
export function getStoredUser(): any {
  const raw = sessionStorage.getItem(USER_KEY)
  return raw ? JSON.parse(raw) : null
}

/**
 * 存储用户信息
 * @param user - 用户信息对象
 */
export function setStoredUser(user: any) {
  sessionStorage.setItem(USER_KEY, JSON.stringify(user))
}
