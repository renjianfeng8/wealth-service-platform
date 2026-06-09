<template>
  <div class="wealth-light">
    <div v-if="loading" class="fl-loading">
      <div class="fl-loading-spinner" />
      <span>加载中...</span>
    </div>

    <template v-else>
      <div class="fl-dashboard">
        <OperationsConsoleHeader
          :pending-orders="pendingOrders"
          :unread-messages="unreadMessages"
          @refresh="fetchData"
        />

        <DashboardMetricGrid :overview="overview" :format-number="formatNumber" />

        <DashboardQuickEntries />

        <div class="fl-ops-row">
          <LatestOrdersPanel :orders="latestOrders" />
          <ActionQueuePanel :actions="actionQueue" />
        </div>

        <TrendPanel :trend-data="trendData" :load-trend="loadTrend" :format-number="formatNumber" />

        <!-- Row 5: K线图 + 行情列表 -->
        <div class="fl-chart-row-kline">
          <KlinePanel :products="products" :kline-data="klineData" :load-kline="loadKline" />
          <MarketSnapshot :products="products" :format-price="formatPrice" :format-rate="formatRate" />
        </div>

      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useAdminDashboard } from '@/composables/useAdminDashboard'
import ActionQueuePanel from './components/ActionQueuePanel.vue'
import DashboardMetricGrid from './components/DashboardMetricGrid.vue'
import DashboardQuickEntries from './components/DashboardQuickEntries.vue'
import KlinePanel from './components/KlinePanel.vue'
import LatestOrdersPanel from './components/LatestOrdersPanel.vue'
import MarketSnapshot from './components/MarketSnapshot.vue'
import OperationsConsoleHeader from './components/OperationsConsoleHeader.vue'
import TrendPanel from './components/TrendPanel.vue'
import { formatPrice, formatRate } from '@/utils/format'

const {
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
} = useAdminDashboard()

const pendingOrders = computed(() => latestOrders.value.filter(order => order.orderStatus === 0).length)
const actionQueue = computed(() => {
  const negativeProducts = products.value.filter(product => (product.riseFallRate || 0) < 0).length
  const disabledProducts = products.value.filter(product => product.status === 0).length
  return [
    {
      label: '待处理委托',
      description: '仍有订单处于待成交状态',
      count: pendingOrders.value,
      path: '/admin/trade',
      done: pendingOrders.value === 0,
    },
    {
      label: '未读消息',
      description: '需要管理员查看的站内提醒',
      count: unreadMessages.value,
      path: '/admin/message',
      done: unreadMessages.value === 0,
    },
    {
      label: '下跌产品',
      description: '当前行情为负的产品数量',
      count: negativeProducts,
      path: '/admin/market',
      done: negativeProducts === 0,
    },
    {
      label: '禁用产品',
      description: '需要复核上下架状态的产品',
      count: disabledProducts,
      path: '/admin/product',
      done: disabledProducts === 0,
    },
  ]
})

function formatNumber(value: number): string {
  if (value >= 1e8) return (value / 1e8).toFixed(2) + '亿'
  if (value >= 1e4) return (value / 1e4).toFixed(2) + '万'
  return value.toFixed(2)
}

async function fetchData() {
  loading.value = true
  try {
    await Promise.all([
      loadProducts(),
      loadOverview(),
      loadTrend(),
      loadLatestOrders(),
      loadUnreadMessages(),
    ])
  } catch {
    // Request errors are handled by the shared interceptor.
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
/* ============================================
   仪表盘 — 仅布局和特有样式，通用样式见 theme.css
   ============================================ */

.fl-dashboard {
  max-width: 1320px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* ---------- Loading ---------- */
.fl-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120px 0;
  gap: 16px;
  color: var(--fl-text-dim);
  font-size: 14px;
}
.fl-loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--fl-border);
  border-top-color: var(--fl-primary);
  border-radius: 50%;
  animation: fl-spin 0.8s linear infinite;
}
@keyframes fl-spin { to { transform: rotate(360deg); } }

/* ---------- Card Header ---------- */
.fl-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 14px;
}
.fl-card-subtitle {
  font-size: 11px;
  color: var(--fl-text-dim);
  margin-top: 2px;
}

/* ---------- Charts Row ---------- */
.fl-chart-row {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 14px;
}

.fl-ops-row {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(300px, 0.9fr);
  gap: 14px;
  align-items: stretch;
}

/* K-line + Market list row — 固定右侧列表宽度 */
.fl-chart-row-kline {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 14px;
}
.fl-chart-col-main,
.fl-chart-col-side,
.fl-chart-col-kline,
.fl-chart-col-list {
  display: flex;
  flex-direction: column;
}
.fl-chart-col-main .fl-card,
.fl-chart-col-side .fl-card,
.fl-chart-col-kline .fl-card {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.fl-chart-box {
  flex: 1;
  min-height: 260px;
  width: 100%;
}
.fl-chart-kline { min-height: 400px; }

/* ---------- Time Filters ---------- */
.fl-time-filters {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}
.fl-tm-btn {
  background: transparent;
  border: 1px solid var(--fl-border-light);
  color: var(--fl-text-dim);
  padding: 3px 12px;
  border-radius: 5px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.15s ease;
  font-family: inherit;
}
.fl-tm-btn:hover {
  border-color: var(--fl-primary);
  color: var(--fl-primary);
}
.fl-tm-btn.active {
  background: var(--fl-primary);
  border-color: var(--fl-primary);
  color: #fff;
}

/* ---------- K-line controls ---------- */
.fl-kline-controls {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.fl-symbol-name {
  font-size: 16px;
  font-weight: 600;
  margin-right: 8px;
  color: var(--fl-text);
}
.fl-symbol-code {
  font-size: 12px;
  color: var(--fl-text-dim);
  font-weight: 400;
}

:deep(.fl-symb-group .el-radio-button__inner) {
  font-size: 11px;
  padding: 4px 10px;
}

/* ---------- K-line + List Row ---------- */
.fl-chart-col-kline .fl-card {
  flex: 1;
  display: flex;
  flex-direction: column;
}
/* ---------- Responsive ---------- */
@media (max-width: 1024px) {
  .fl-chart-row,
  .fl-ops-row,
  .fl-chart-row-kline { grid-template-columns: 1fr; }
  .fl-stats-row { gap: 10px; }
}
@media (max-width: 768px) {
  .fl-stats-row { grid-template-columns: 1fr; }
  .fl-card-header { flex-direction: column; align-items: flex-start; }
  .fl-kline-controls { flex-direction: column; align-items: flex-start; }
}
</style>
