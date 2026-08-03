import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { WeaMarketData } from '@/types'

export const useMarketSSEStore = defineStore('marketSSE', () => {
  const connected = ref(false)
  let eventSource: EventSource | null = null
  let retryDelay = 1000
  let retryTimer: ReturnType<typeof setTimeout> | null = null
  const handlers = new Set<(data: WeaMarketData[]) => void>()

  function onMessage(e: MessageEvent) {
    try {
      // 载荷为原始 MarketDataVO[] 数组（非 Result 信封），见 MarketDataController.subscribe
      const data = JSON.parse(e.data) as WeaMarketData[]
      handlers.forEach(fn => fn(data))
    } catch {
      console.warn('[SSE] 解析行情数据失败:', e.data)
    }
  }

  function connect() {
    if (eventSource) return
    eventSource = new EventSource('/api/v1/product/wea-market-data/sse')
    eventSource.onopen = () => {
      connected.value = true
      retryDelay = 1000
    }
    eventSource.onerror = () => {
      connected.value = false
      // 主动 close 接管重连，按指数退避（1s→30s），避免浏览器固定频率重连风暴
      eventSource?.close()
      eventSource = null
      if (retryTimer) clearTimeout(retryTimer)
      retryTimer = setTimeout(() => connect(), retryDelay)
      retryDelay = Math.min(retryDelay * 2, 30000)
    }
    eventSource.addEventListener('market-update', onMessage)
  }

  function disconnect() {
    eventSource?.close()
    eventSource = null
    if (retryTimer) {
      clearTimeout(retryTimer)
      retryTimer = null
    }
    connected.value = false
  }

  function subscribe(handler: (data: WeaMarketData[]) => void) {
    handlers.add(handler)
    if (!eventSource) connect()
  }

  function unsubscribe(handler: (data: WeaMarketData[]) => void) {
    handlers.delete(handler)
    if (handlers.size === 0) disconnect()
  }

  return { connected, subscribe, unsubscribe }
})
