const LOGIN_KEY = 'wealth_logged_in'
const USER_KEY = 'wealth_user'
const ROLE_KEY = 'wealth_role'
const LOGIN_TIME_KEY = 'wealth_login_time' // S2: 记录登录时间用于过期检测
const REFRESH_KEY = 'wealth_refresh' // refresh_token 存储（静默续期 / 登出黑名单用）

// 令牌过期时间（30分钟），与后端 jwt.access-expire=1800000 对齐
const TOKEN_EXPIRY_MS = 30 * 60 * 1000

/**
 * 返回登录状态标记（true/false）。JWT 已由后端写入 httpOnly Cookie，
 * 前端无法读取，仅用此标记判断是否已登录。
 * S2: 增加过期检测，超时时自动清除标记。
 * @returns 登录状态标记，未登录或已过期时返回 null
 */
export function getToken(): string | null {
  // S2: 检查登录时间是否过期
  const loginTime = sessionStorage.getItem(LOGIN_TIME_KEY)
  if (loginTime && Date.now() - Number(loginTime) > TOKEN_EXPIRY_MS) {
    removeToken()
    return null
  }
  return sessionStorage.getItem(LOGIN_KEY)
}

/**
 * 记录登录状态。JWT 已由后端写入 httpOnly Cookie，此处不存储明文 token。
 * S2: 同时记录登录时间戳。
 * @param _token - 登录 token（实际未使用，仅标记登录状态为 true）
 */
export function setToken() {
  sessionStorage.setItem(LOGIN_KEY, crypto.randomUUID())
  sessionStorage.setItem(LOGIN_TIME_KEY, String(Date.now())) // S2: 记录登录时间
}

/**
 * 清除登录状态和用户信息
 */
export function removeToken() {
  sessionStorage.removeItem(LOGIN_KEY)
  sessionStorage.removeItem(USER_KEY)
  sessionStorage.removeItem(ROLE_KEY)
  sessionStorage.removeItem(LOGIN_TIME_KEY) // S2: 同时清除登录时间
  sessionStorage.removeItem(REFRESH_KEY) // 同时清除 refresh_token
}

/**
 * 存储 refresh_token（用于静默续期与登出黑名单）
 */
export function setRefreshToken(token: string) {
  sessionStorage.setItem(REFRESH_KEY, token)
}

/**
 * 读取 refresh_token，无则返回 null
 */
export function getRefreshToken(): string | null {
  return sessionStorage.getItem(REFRESH_KEY)
}

/**
 * 续期成功后刷新登录时间戳，避免被本地 30 分钟过期标记强制登出
 */
export function bumpLoginTime() {
  sessionStorage.setItem(LOGIN_TIME_KEY, String(Date.now()))
}

export interface StoredUser {
  username: string
  userId: number
  nickname?: string
  avatar?: string
}

/**
 * 获取存储的用户信息
 * @returns 用户信息对象，无数据时返回 null
 */
export function getStoredUser(): StoredUser | null {
  const raw = sessionStorage.getItem(USER_KEY)
  return raw ? JSON.parse(raw) : null
}

/**
 * 存储用户信息
 * @param user - 用户信息对象
 */
export function setStoredUser(user: StoredUser) {
  sessionStorage.setItem(USER_KEY, JSON.stringify(user))
}
