import type { WeaMarketData } from '@/types'

/**
 * 创建行情 SSE 连接。JWT 由 httpOnly Cookie 自动携带，
 * 参见后端 MarketDataController.subscribe() 对 wealth_token Cookie 的支持。
 */
export function createMarketSSE(onStatusChange?: (connected: boolean) => void): EventSource {
  const es = new EventSource('/api/v1/product/wea-market-data/sse')
  es.onopen = () => onStatusChange?.(true)
  es.onerror = () => {
    console.warn('[SSE] 连接异常，浏览器将自动重连')
    onStatusChange?.(false)
  }
  return es
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
}
