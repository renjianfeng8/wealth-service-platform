import { defineStore } from 'pinia'
import { getToken, setToken, removeToken, setStoredUser, getStoredUser } from '@/utils/auth'
import { loginApi } from '@/api/system'

interface UserInfo {
  username: string
  token: string
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    username: getStoredUser()?.username || '',
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
  },
  actions: {
    async login(username: string, password: string) {
      const res = await loginApi({ username, password })
      // JWT 已由后端写入 httpOnly Cookie，前端仅记录登录状态
      setToken(res.data.accessToken as string)
      setStoredUser({ username })
      this.token = getToken() || ''
      this.username = username
    },
    logout() {
      this.token = ''
      this.username = ''
      removeToken()
    },
  },
})
