import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken } from '@/utils/auth'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
})

request.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 401) {
      removeToken()
      window.location.hash = '#/login'
      return Promise.reject(new Error(res.message || '未登录'))
    }
    if (res.code !== 200 && res.code !== 0 && res.code !== undefined) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  (error) => {
    if (error.response?.status === 401) {
      removeToken()
      window.location.hash = '#/login'
    }
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  },
)

export default request
