import { defineStore } from 'pinia'
import { getToken, setToken, removeToken, setStoredUser, getStoredUser } from '@/utils/auth'
import { userLogin, getUserList } from '@/api/user'
import type { UserInfo } from '@/types'

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
      const token = res.data as string
      this.token = token
      this.username = username
      setToken(token)
      // 尝试获取用户详情
      try {
        const userRes = await getUserList()
        const users = (userRes.data || []) as UserInfo[]
        const matched = users.find((u) => u.username === username)
        if (matched && matched.id) {
          this.userId = matched.id
          this.nickname = matched.nickname || ''
          this.avatar = matched.avatar || ''
        }
      } catch {
        // 静默失败，后续可手动调用 setUserInfo
      }
      setStoredUser({
        username: this.username,
        userId: this.userId,
        nickname: this.nickname,
        avatar: this.avatar,
      })
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
