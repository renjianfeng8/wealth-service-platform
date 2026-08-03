import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store'
import router from '@/router'
import { getRefreshToken } from '@/utils/auth'
import { randomUUID } from '@/utils/uuid'

const redirectLogin = () => {
  const loginPath = '/auth/login'
  router.replace(loginPath)
}

// 公开认证路径：业务 401（如密码错误）只提示，不触发续期/登出
const PUBLIC_AUTH_PATHS = [
  '/user/register',
  '/user/identify-login',
  '/system/captcha',
  '/system/umsAdmin/refresh',
  '/system/umsAdmin/logout',
]

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 30000,
})

// 并发 401 去重：多个失败请求共享一次续期
let refreshing: Promise<boolean> | null = null

/**
 * 静默续期：用 refresh_token 换取新 token 对（后端 Set-Cookie 写新 access token）。
 * 使用裸 axios 调用，不经过本拦截器，避免续期请求自身的 401 触发递归。
 */
async function tryRefresh(): Promise<boolean> {
  const refreshToken = getRefreshToken()
  if (!refreshToken) return false
  if (!refreshing) {
    refreshing = axios.post('/api/v1/system/umsAdmin/refresh', null, {
      headers: { Authorization: `Bearer ${refreshToken}` },
    })
      .then(({ data }) => {
        useUserStore().applyRefreshedPair(data.data)
        return true
      })
      .catch(() => false)
      .finally(() => { refreshing = null })
  }
  return refreshing
}

function isPublicAuthPath(url?: string): boolean {
  return !!url && PUBLIC_AUTH_PATHS.some((p) => url.includes(p))
}

function failToLogin(message: string) {
  useUserStore().forceLogout()
  redirectLogin()
  ElMessage.error(message || '登录已过期，请重新登录')
}

request.interceptors.request.use((config) => {
  // JWT 由 httpOnly Cookie 自动携带，前端无需注入 Authorization 头
  // 参见 UmsAdminController.login() 设置的 wealth_token Cookie
  // POST/PUT/DELETE 添加防重放头（后端 @AntiReplay 校验兼容旧客户端，不传头时自动放行）
  if (config.method && ['post', 'put', 'delete'].includes(config.method)) {
    config.headers['X-Timestamp'] = Date.now().toString()
    config.headers['X-Nonce'] = randomUUID()
  }
  return config
})

request.interceptors.response.use(
  async (response) => {
    const res = response.data
    if (res.code === 401) {
      const config = response.config as { url?: string; _refreshed?: boolean }
      // 非公开路径：先尝试静默续期一次，成功后重放原请求
      if (!isPublicAuthPath(config.url) && !config._refreshed) {
        const ok = await tryRefresh()
        if (ok) {
          config._refreshed = true
          return request(response.config)
        }
      }
      // 公开路径（登录/注册/验证码）：仅提示，不续期不登出
      if (isPublicAuthPath(config.url)) {
        ElMessage.error(res.message || '请求失败')
        return Promise.reject(new Error(res.message || '请求失败'))
      }
      failToLogin(res.message || '未登录')
      return Promise.reject(new Error(res.message || '未登录'))
    }
    if (res.code !== 200 && res.code !== 0 && res.code !== undefined) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  async (error) => {
    const status = error.response?.status
    const config = error.config as { url?: string; _refreshed?: boolean } | undefined
    if (status === 401) {
      // 非公开路径且未续期过：尝试静默续期并重放；续期失败或无 refresh_token → 强制登出
      if (config && !isPublicAuthPath(config.url) && !config._refreshed) {
        const ok = await tryRefresh()
        if (ok) {
          config._refreshed = true
          return request(config)
        }
      }
      if (config && isPublicAuthPath(config.url)) {
        const data = error.response?.data
        ElMessage.error(data?.message || '请求失败')
      } else {
        failToLogin('登录已过期，请重新登录')
      }
    } else {
      const data = error.response?.data
      const msg = data?.message || error.message || '网络错误'
      ElMessage.error(msg)
    }
    return Promise.reject(error)
  },
)

export default request
