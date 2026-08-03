import { defineStore } from 'pinia'
import { getToken, setToken, removeToken, setStoredUser, getStoredUser, setRefreshToken, getRefreshToken, bumpLoginTime } from '@/utils/auth'
import { logoutApi } from '@/api/system'

export interface LoginInfo {
  username: string
  userId: number | string
  nickname?: string
  avatar?: string
  role: 'admin' | 'user'
  refreshToken?: string
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
    refreshToken: getRefreshToken() || '',
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
      const { username, userId, nickname, avatar, role, refreshToken } = info
      setToken()
      sessionStorage.setItem('wealth_role', role)
      setStoredUser({ username, userId, nickname, avatar })
      this.token = getToken() || ''
      this.username = username
      this.userId = userId
      this.nickname = nickname || ''
      this.avatar = avatar || DEFAULT_AVATAR
      this.role = role
      if (refreshToken) {
        setRefreshToken(refreshToken)
        this.refreshToken = refreshToken
      }
    },
    /**
     * 更新当前登录用户的个人信息
     * @param info - 用户信息（userId、nickname、avatar）
     */
    setUserInfo(info: { userId: number | string; nickname: string; avatar: string }) {
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
     * 用户主动登出：先通知后端将 refresh_token 加入黑名单（fire-and-forget，失败静默），再清除本地状态
     */
    logout() {
      const refreshToken = this.refreshToken || getRefreshToken()
      if (refreshToken) {
        logoutApi(refreshToken)
      }
      this.clearLocal()
    },
    /**
     * 强制登出（仅清本地，不调后端）：供响应拦截器续期失败路径复用，避免 401 → 登出 → 401 递归
     */
    forceLogout() {
      this.clearLocal()
    },
    /**
     * 静默续期成功后更新 refresh_token 并刷新登录时间戳（access token 由后端 Set-Cookie 写入，前端无需存储）
     * @param pair - 后端返回的 token 对（accessToken 由 httpOnly Cookie 承载）
     */
    applyRefreshedPair(pair: { refreshToken: string }) {
      setRefreshToken(pair.refreshToken)
      this.refreshToken = pair.refreshToken
      bumpLoginTime()
    },
    clearLocal() {
      this.token = ''
      this.username = ''
      this.userId = 0
      this.nickname = ''
      this.avatar = DEFAULT_AVATAR
      this.role = null
      this.refreshToken = ''
      removeToken()
    },
  },
})
