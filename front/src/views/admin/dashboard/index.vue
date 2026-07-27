<template>
  <div class="wealth-light">
    <div v-if="loading" class="fl-loading">
      <div class="fl-loading-spinner" />
      <span>加载中...</span>
    </div>

    <template v-else>
      <div class="fl-dashboard">
        <!-- Row 1: Welcome + Alerts -->
        <OperationsConsoleHeader
          :admin-name="displayName"
          :last-refresh-time="lastRefreshTime"
          :pending-orders="pendingOrders"
          :unread-messages="unreadMessages"
          :disabled-products="disabledProducts"
          @refresh="fetchData"
        />

        <!-- Row 2: Core Metrics -->
        <DashboardMetricGrid
          :total-users="totalUsers"
          :total-products="totalProducts"
          :total-orders="totalOrders"
          :unread-messages="unreadMessages"
        />

        <!-- Row 3: Trend Charts + Market -->
        <div class="fl-chart-row-kline">
          <TrendPanel
            :trend-data="trendData"
            :load-trend="loadTrend"
            :format-number="formatNumber"
          />
          <MarketSnapshot
            :products="marketProducts"
            :format-price="formatPrice"
            :format-rate="formatRate"
          />
        </div>

        <!-- Row 4: Latest Orders + Activities -->
        <div class="fl-ops-row">
          <LatestOrdersPanel :orders="latestOrders" />
          <LatestActivities :messages="recentMessages" />
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useUserStore } from '@/store'
import { useAdminDashboard } from '@/composables/useAdminDashboard'
import OperationsConsoleHeader from './components/OperationsConsoleHeader.vue'
import DashboardMetricGrid from './components/DashboardMetricGrid.vue'
import TrendPanel from './components/TrendPanel.vue'
import MarketSnapshot from './components/MarketSnapshot.vue'
import LatestOrdersPanel from './components/LatestOrdersPanel.vue'
import LatestActivities from './components/LatestActivities.vue'
import { formatPrice, formatRate } from '@/utils/format'

const userStore = useUserStore()

const displayName = computed(() => userStore.nickname || userStore.username || '管理员')

const {
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
} = useAdminDashboard()

function formatNumber(value: number): string {
  if (value >= 1e8) return (value / 1e8).toFixed(2) + '亿'
  if (value >= 1e4) return (value / 1e4).toFixed(2) + '万'
  return value.toFixed(2)
}

onMounted(fetchData)
</script>

<style scoped>
.fl-dashboard {
  max-width: 1320px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* Loading */
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

/* Layout rows */
.fl-chart-row-kline {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 14px;
}

.fl-ops-row {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(300px, 0.9fr);
  gap: 14px;
  align-items: stretch;
}

@media (max-width: 1024px) {
  .fl-chart-row-kline { grid-template-columns: 1fr; }
  .fl-ops-row { grid-template-columns: 1fr; }
}
</style>
