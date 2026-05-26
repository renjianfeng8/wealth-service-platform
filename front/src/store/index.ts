import { defineStore } from 'pinia'
import { getToken, setToken, removeToken, setStoredUser, getStoredUser } from '@/utils/auth'
import { loginApi } from '@/api/system'
import { userLogin } from '@/api/user'

const ROLE_KEY = 'wealth_role'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    username: getStoredUser()?.username || '',
    userId: getStoredUser()?.userId || 0,
    nickname: getStoredUser()?.nickname || '',
    avatar: getStoredUser()?.avatar || '',
    role: (sessionStorage.getItem(ROLE_KEY) as 'admin' | 'user' | null) || null,
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    isAdmin: (state) => state.role === 'admin',
  },
  actions: {
    async login(username: string, password: string) {
      const res = await loginApi({ username, password })
      setToken('true')
      sessionStorage.setItem(ROLE_KEY, 'admin')
      setStoredUser({ username })
      this.token = 'true'
      this.username = username
      this.role = 'admin'
    },
    async userLogin(username: string, password: string) {
      const res = await userLogin({ username, password })
      const { userId } = res.data
      const nickname = res.data.nickname || ''
      const avatar = res.data.avatar || ''
      setToken('true')
      sessionStorage.setItem(ROLE_KEY, 'user')
      setStoredUser({ username, userId, nickname, avatar })
      this.token = 'true'
      this.username = username
      this.userId = userId
      this.nickname = nickname
      this.avatar = avatar
      this.role = 'user'
    },
    setUserInfo(info: { userId: number; nickname: string; avatar: string }) {
      this.userId = info.userId
      this.nickname = info.nickname
      this.avatar = info.avatar
      setStoredUser({ username: this.username, ...info })
    },
    logout() {
      this.token = ''
      this.username = ''
      this.userId = 0
      this.nickname = ''
      this.avatar = ''
      this.role = null
      sessionStorage.removeItem(ROLE_KEY)
      removeToken()
    },
  },
})
