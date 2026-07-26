import { ref } from 'vue'
import { getProductPage } from '@/api/product'
import { getTradeOrderPage } from '@/api/trade'
import { getMessagePage } from '@/api/message'
import { getDashboardOverview, getDashboardTrend, getDashboardKline } from '@/api/dashboard'
import { useUserStore } from '@/store'
import type { DashboardOverview, Candle } from '@/api/dashboard'
import type { WeaProduct, WeaTradeOrder } from '@/types'

const defaultOverview: DashboardOverview = {
  totalAsset: 0,
  assetChange: 0,
  balanceValue: 0,
  balanceChange: 0,
  dailyIncome: 0,
  dailyIncomeRate: 0,
}

export function useAdminDashboard() {
  const loading = ref(true)
  const products = ref<WeaProduct[]>([])
  const overview = ref<DashboardOverview>({ ...defaultOverview })
  const trendData = ref<{ series: { date: string; assetValue: number; balanceValue: number; income: number }[] } | null>(null)
  const klineData = ref<Candle[]>([])
  const latestOrders = ref<WeaTradeOrder[]>([])
  const unreadMessages = ref(0)

  async function loadOverview() {
    const res = await getDashboardOverview()
    if (res.data) {
      overview.value = res.data
    }
  }

  async function loadTrend(period: string = '7D') {
    const res = await getDashboardTrend(period)
    if (res.data) {
      trendData.value = res.data
    }
  }

  async function loadKline(code: string, period: string = '1M') {
    const res = await getDashboardKline(code, period)
    if (res.data?.candles) {
      klineData.value = res.data.candles
    }
  }

  async function loadProducts() {
    const res = await getProductPage({ pageNum: 1, pageSize: 200 })
    products.value = (res.data?.records || []) as WeaProduct[]
  }

  async function loadLatestOrders() {
    const res = await getTradeOrderPage({ pageNum: 1, pageSize: 6 })
    latestOrders.value = (res.data?.records || []) as WeaTradeOrder[]
  }

  async function loadUnreadMessages() {
    const params: Record<string, any> = { pageNum: 1, pageSize: 1, readFlag: 0 }
    const uid = useUserStore().userId
    if (uid) params.userId = uid
    const res = await getMessagePage(params)
    unreadMessages.value = Number(res.data?.total || 0)
  }

  return {
    loading,
    products,
    overview,
    trendData,
    klineData,
    latestOrders,
    unreadMessages,
    loadOverview,
    loadTrend,
    loadKline,
    loadProducts,
    loadLatestOrders,
    loadUnreadMessages,
  }
}
