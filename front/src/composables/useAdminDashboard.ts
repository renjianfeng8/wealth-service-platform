import { ref } from 'vue'
import dayjs from 'dayjs'
import { getProductPage } from '@/api/product'
import { getUserPage } from '@/api/user'
import { getTradeOrderPage } from '@/api/trade'
import { getMessagePage } from '@/api/message'
import { getDashboardTrend } from '@/api/dashboard'
import { useUserStore } from '@/store'
import type { WeaProduct, WeaTradeOrder, WeaMessage } from '@/types'

export function useAdminDashboard() {
  const loading = ref(true)

  /* ---- Row 1: Welcome + Alerts ---- */
  const lastRefreshTime = ref('')
  const pendingOrders = ref(0)
  const unreadMessages = ref(0)
  const disabledProducts = ref(0)

  /* ---- Row 2: Core metrics ---- */
  const totalUsers = ref(0)
  const totalProducts = ref(0)
  const totalOrders = ref(0)

  /* ---- Row 3: Charts ---- */
  const marketProducts = ref<WeaProduct[]>([])
  const trendData = ref<{ series: { date: string; assetValue: number; balanceValue: number; income: number }[] } | null>(null)

  /* ---- Row 4: Tables ---- */
  const latestOrders = ref<WeaTradeOrder[]>([])
  const recentMessages = ref<WeaMessage[]>([])

  async function loadAdminCounts() {
    const [userRes, productRes, orderRes] = await Promise.allSettled([
      getUserPage({ pageNum: 1, pageSize: 1 }),
      getProductPage({ pageNum: 1, pageSize: 1 }),
      getTradeOrderPage({ pageNum: 1, pageSize: 1 }),
    ])
    if (userRes.status === 'fulfilled') totalUsers.value = (userRes.value as any).data?.total || 0
    if (productRes.status === 'fulfilled') totalProducts.value = (productRes.value as any).data?.total || 0
    if (orderRes.status === 'fulfilled') totalOrders.value = (orderRes.value as any).data?.total || 0
  }

  async function loadProducts() {
    const res = await getProductPage({ pageNum: 1, pageSize: 100 })
    const list = (res.data?.records || []) as WeaProduct[]
    marketProducts.value = list
    disabledProducts.value = list.filter(p => p.status === 0).length
  }

  async function loadLatestOrders() {
    const res = await getTradeOrderPage({ pageNum: 1, pageSize: 5 })
    latestOrders.value = (res.data?.records || []) as WeaTradeOrder[]
    pendingOrders.value = latestOrders.value.filter(o => o.orderStatus === 0).length
  }

  async function loadUnreadMessages() {
    const params: Record<string, any> = { pageNum: 1, pageSize: 1, readFlag: 0 }
    const uid = useUserStore().userId
    if (uid) params.userId = uid
    const res = await getMessagePage(params)
    unreadMessages.value = Number(res.data?.total || 0)
  }

  async function loadRecentMessages() {
    const params: Record<string, any> = { pageNum: 1, pageSize: 5 }
    const uid = useUserStore().userId
    if (uid) params.userId = uid
    const res = await getMessagePage(params)
    recentMessages.value = (res.data?.records || []) as WeaMessage[]
  }

  async function loadTrend(period = '7D') {
    const res = await getDashboardTrend(period)
    if (res.data) trendData.value = res.data
  }

  function updateRefreshTime() {
    lastRefreshTime.value = dayjs().format('HH:mm:ss')
  }

  async function fetchData() {
    loading.value = true
    await Promise.allSettled([
      loadAdminCounts(),
      loadProducts(),
      loadTrend(),
      loadLatestOrders(),
      loadUnreadMessages(),
      loadRecentMessages(),
    ])
    updateRefreshTime()
    loading.value = false
  }

  return {
    loading,
    lastRefreshTime,
    pendingOrders,
    unreadMessages,
    disabledProducts,
    totalUsers,
    totalProducts,
    totalOrders,
    trendData,
    marketProducts,
    latestOrders,
    recentMessages,
    loadTrend,
    fetchData,
  }
}
