import { getToken } from './auth'
import type { WeaMarketData } from '@/types'

/**
 * 创建行情 SSE 连接。EventSource 不支持自定义请求头，
 * JWT token 通过查询参数传递。
 */
export function createMarketSSE(): EventSource {
  const token = getToken()
  const url = token
    ? `/api/v1/product/wea-market-data/sse?token=${encodeURIComponent(token)}`
    : '/api/v1/product/wea-market-data/sse'
  return new EventSource(url)
}

/**
 * 注册 market-update 事件监听。
 */
export function onMarketUpdate(
  es: EventSource,
  handler: (data: WeaMarketData[]) => void
) {
  es.addEventListener('market-update', (e: MessageEvent) => {
    try {
      const data = JSON.parse(e.data) as WeaMarketData[]
      handler(data)
    } catch {
      console.warn('[SSE] 解析行情数据失败:', e.data)
    }
  })
  es.onerror = () => {
    console.warn('[SSE] 连接异常，浏览器将自动重连')
  }
}
