const TOKEN_KEY = 'wealth_token'
const USER_KEY = 'wealth_user'

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export function setStoredUser(user: { username: string; userType: string; nickname?: string }) {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function getStoredUser(): { username: string; userType: string; nickname?: string } | null {
  const raw = localStorage.getItem(USER_KEY)
  return raw ? JSON.parse(raw) : null
}
