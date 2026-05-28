import { defineStore } from 'pinia'
import { getToken, setToken, removeToken, setStoredUser, getStoredUser } from '@/utils/auth'
import { useAppStore } from '@/store/app'

export interface LoginInfo {
  username: string
  userId: number
  nickname?: string
  avatar?: string
  role: 'admin' | 'user'
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    username: getStoredUser()?.username || '',
    userId: getStoredUser()?.userId || 0,
    nickname: getStoredUser()?.nickname || '',
    avatar: getStoredUser()?.avatar || '',
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
      setToken('true')
      sessionStorage.setItem('wealth_role', role)
      setStoredUser({ username, userId, nickname, avatar })
      this.token = 'true'
      this.username = username
      this.userId = userId
      this.nickname = nickname || ''
      this.avatar = avatar || ''
      this.role = role
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
      removeToken()
      // 清理缓存的标签页状态
      try {
        const appStore = useAppStore()
        appStore.closeAllViews()
      } catch {
        // appStore 可能尚未初始化
      }
    },
  },
})
