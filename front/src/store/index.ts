import { defineStore } from 'pinia'
import { getToken, setToken, removeToken, setStoredUser, getStoredUser } from '@/utils/auth'

export interface LoginInfo {
  username: string
  userId: number
  nickname?: string
  avatar?: string
  role: 'admin' | 'user'
}

export const DEFAULT_AVATAR = '/default-avatar.svg'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    username: getStoredUser()?.username || '',
    userId: getStoredUser()?.userId || 0,
    nickname: getStoredUser()?.nickname || '',
    avatar: getStoredUser()?.avatar || DEFAULT_AVATAR,
    role: (sessionStorage.getItem('wealth_role') as 'admin' | 'user' | null) || null,
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    isAdmin: (state) => state.role === 'admin',
  },
  actions: {
    /**
     * 设置登录状态（由登录页面调用 identifyLogin 后传入结果）
     * @param info - 用户信息及角色
     */
    setLoginInfo(info: LoginInfo) {
      const { username, userId, nickname, avatar, role } = info
      setToken()
      sessionStorage.setItem('wealth_role', role)
      setStoredUser({ username, userId, nickname, avatar })
      this.token = getToken() || ''
      this.username = username
      this.userId = userId
      this.nickname = nickname || ''
      this.avatar = avatar || DEFAULT_AVATAR
      this.role = role
    },
    /**
     * 更新当前登录用户的个人信息
     * @param info - 用户信息（userId、nickname、avatar）
     */
    setUserInfo(info: { userId: number; nickname: string; avatar: string }) {
      this.userId = info.userId
      this.nickname = info.nickname
      this.avatar = info.avatar || DEFAULT_AVATAR
      setStoredUser({ username: this.username, ...info })
    },
    /**
     * S2: 检查令牌是否过期，过期则自动登出。
     * @returns true 表示已过期并登出
     */
    checkTokenExpired(): boolean {
      if (!this.token) return false
      // 从 storage 重新获取，getToken() 内部会做过期检查
      const stored = getToken()
      if (!stored) {
        this.logout()
        return true
      }
      return false
    },
    /**
     * 登出，清除所有登录状态、用户信息和角色
     */
    logout() {
      this.token = ''
      this.username = ''
      this.userId = 0
      this.nickname = ''
      this.avatar = DEFAULT_AVATAR
      this.role = null
      removeToken()
    },
  },
})
