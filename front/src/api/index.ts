import axios from 'axios'
import { ElMessage } from 'element-plus'
import { removeToken } from '@/utils/auth'
import router from '@/router'

const redirectLogin = () => {
  const loginPath = '/auth/login'
  router.replace(loginPath)
}

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 30000,
})

request.interceptors.request.use((config) => {
  // JWT 由 httpOnly Cookie 自动携带，前端无需注入 Authorization 头
  // 参见 UmsAdminController.login() 设置的 wealth_token Cookie
  // POST/PUT/DELETE 添加防重放头（后端 @AntiReplay 校验兼容旧客户端，不传头时自动放行）
  if (config.method && ['post', 'put', 'delete'].includes(config.method)) {
    config.headers['X-Timestamp'] = Date.now().toString()
    config.headers['X-Nonce'] = crypto.randomUUID()
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 401) {
      removeToken()
      redirectLogin()
      return Promise.reject(new Error(res.message || '未登录'))
    }
    if (res.code !== 200 && res.code !== 0 && res.code !== undefined) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  (error) => {
    const status = error.response?.status
    const data = error.response?.data
    const msg = data?.message || error.message || '网络错误'
    if (status === 401) {
      removeToken()
      redirectLogin()
    }
    ElMessage.error(msg)
    return Promise.reject(error)
  },
)

export default request
