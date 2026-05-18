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
      const token = res.data.accessToken as string
      this.token = token
      this.username = username
      setToken(token)
      setStoredUser({ username })
    },
    logout() {
      this.token = ''
      this.username = ''
      removeToken()
    },
  },
})
