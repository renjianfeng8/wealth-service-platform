import { defineStore } from 'pinia'
import { getToken, setToken, removeToken, setStoredUser, getStoredUser } from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    username: getStoredUser()?.username || '',
    userType: getStoredUser()?.userType || '',
    nickname: getStoredUser()?.nickname || '',
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
  },
  actions: {
    setLoginInfo(info: { token: string; username: string; userType: string; nickname?: string }) {
      setToken(info.token)
      setStoredUser({ username: info.username, userType: info.userType, nickname: info.nickname })
      this.token = info.token
      this.username = info.username
      this.userType = info.userType
      this.nickname = info.nickname || ''
    },
    logout() {
      this.token = ''
      this.username = ''
      this.userType = ''
      this.nickname = ''
      removeToken()
    },
  },
})
