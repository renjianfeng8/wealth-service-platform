import { defineStore } from 'pinia'
import { getToken, setToken, removeToken, setStoredUser, getStoredUser } from '@/utils/auth'
import { userLogin } from '@/api/user'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    username: getStoredUser()?.username || '',
    userId: getStoredUser()?.userId || 0,
    nickname: getStoredUser()?.nickname || '',
    avatar: getStoredUser()?.avatar || '',
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
  },
  actions: {
    async login(username: string, password: string) {
      const res = await userLogin({ username, password })
      // JWT 已由后端写入 httpOnly Cookie，前端仅记录登录状态
      const { userId } = res.data
      const nickname = res.data.nickname || ''
      const avatar = res.data.avatar || ''
      setToken(res.data.token as string)
      setStoredUser({ username, userId, nickname, avatar })
      this.token = getToken() || ''
      this.username = username
      this.userId = userId
      this.nickname = nickname
      this.avatar = avatar
    },
    setUserInfo(info: { userId: number; nickname: string; avatar: string }) {
      this.userId = info.userId
      this.nickname = info.nickname
      this.avatar = info.avatar
      setStoredUser({
        username: this.username,
        userId: info.userId,
        nickname: info.nickname,
        avatar: info.avatar,
      })
    },
    logout() {
      this.token = ''
      this.username = ''
      this.userId = 0
      this.nickname = ''
      this.avatar = ''
      removeToken()
    },
  },
})
