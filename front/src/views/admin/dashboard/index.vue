<template>
  <div class="fl-dashboard-page">
    <div v-if="loading" class="fl-loading">
      <div class="fl-loading-spinner" />
      <span>加载中...</span>
    </div>

    <template v-else>
      <div class="fl-dashboard">
        <OperationsConsoleHeader
          :admin-name="displayName"
          :last-refresh-time="lastRefreshTime"
          :pending-orders="pendingOrders"
          :unread-messages="unreadMessages"
          :disabled-products="disabledProducts"
          @refresh="fetchData"
        />

        <DashboardMetricGrid
          :total-users="totalUsers"
          :total-products="totalProducts"
          :total-orders="totalOrders"
          :unread-messages="unreadMessages"
          :total-asset="overview?.totalAsset ?? null"
          :daily-income="overview?.dailyIncome ?? null"
        />

        <DashboardQuickEntries />

        <div class="fl-ops-row">
          <LatestOrdersPanel :orders="latestOrders" />
          <LatestActivities :messages="recentMessages" />
        </div>

        <MarketSnapshot
          :products="marketProducts"
          :format-price="formatPrice"
          :format-rate="formatRate"
        />
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
import DashboardQuickEntries from './components/DashboardQuickEntries.vue'
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
  overview,
  marketProducts,
  latestOrders,
  recentMessages,
  fetchData,
} = useAdminDashboard()

onMounted(fetchData)
</script>

<style scoped>
.fl-dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.fl-dashboard {
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

/* Layout row: orders + activities */
.fl-ops-row {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(300px, 0.9fr);
  gap: 14px;
  align-items: stretch;
}

@media (max-width: 1024px) {
  .fl-ops-row { grid-template-columns: 1fr; }
}
</style>
