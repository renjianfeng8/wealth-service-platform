// 内存存储，不持久化到 localStorage。
// landing 页仅作为登录跳板，token 用完即弃，刷新页面后自动清零。
let _token = ''
let _user: { username: string; userType: string; nickname?: string } | null = null

export function getToken(): string | null {
  return _token || null
}

export function setToken(token: string) {
  _token = token
}

export function removeToken() {
  _token = ''
  _user = null
}

export function setStoredUser(user: { username: string; userType: string; nickname?: string }) {
  _user = user
}

export function getStoredUser(): { username: string; userType: string; nickname?: string } | null {
  return _user
}
