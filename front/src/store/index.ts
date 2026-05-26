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
    /**
     * 管理员登录，设置登录状态、角色为 admin 并存储用户信息
     * @param username - 管理员用户名
     * @param password - 管理员密码
     */
    async login(username: string, password: string) {
      const res = await loginApi({ username, password })
      setToken('true')
      sessionStorage.setItem(ROLE_KEY, 'admin')
      setStoredUser({ username })
      this.token = 'true'
      this.username = username
      this.role = 'admin'
    },
    /**
     * 普通用户登录，设置登录状态、角色为 user 并存储用户信息
     * @param username - 用户名
     * @param password - 密码
     */
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
    /**
     * 更新当前登录用户的个人信息
     * @param info - 用户信息（userId、nickname、avatar）
     */
    setUserInfo(info: { userId: number; nickname: string; avatar: string }) {
      this.userId = info.userId
      this.nickname = info.nickname
      this.avatar = info.avatar
      setStoredUser({ username: this.username, ...info })
    },
    /**
     * 登出，清除所有登录状态、用户信息和角色
     */
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
