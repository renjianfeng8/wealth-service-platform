import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { WeaMarketData } from '@/types'

export const useMarketSSEStore = defineStore('marketSSE', () => {
  const connected = ref(false)
  let eventSource: EventSource | null = null
  const handlers = new Set<(data: WeaMarketData[]) => void>()

  function onMessage(e: MessageEvent) {
    try {
      const data = JSON.parse(e.data) as WeaMarketData[]
      handlers.forEach(fn => fn(data))
    } catch {
      console.warn('[SSE] 解析行情数据失败:', e.data)
    }
  }

  function connect() {
    if (eventSource) return
    eventSource = new EventSource('/api/v1/product/wea-market-data/sse')
    eventSource.onopen = () => { connected.value = true }
    eventSource.onerror = () => {
      console.warn('[SSE] 连接异常，浏览器将自动重连')
      connected.value = false
    }
    eventSource.addEventListener('market-update', onMessage)
  }

  function disconnect() {
    eventSource?.close()
    eventSource = null
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
