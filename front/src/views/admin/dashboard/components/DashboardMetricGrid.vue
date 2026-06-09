<template>
  <div class="fl-stats-row">
    <div class="fl-stat-card">
      <div class="fl-stat-icon fl-icon-blue">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="22 7 13.5 15.5 8.5 10.5 2 17" />
          <polyline points="16 7 22 7 22 13" />
        </svg>
      </div>
      <div class="fl-stat-body">
        <div class="fl-stat-label">资产总值 (估算)</div>
        <div class="fl-stat-value">¥{{ formatNumber(overview.totalAsset) }}</div>
        <div class="fl-stat-change">
          <span :class="overview.assetChange >= 0 ? 'fl-rise' : 'fl-fall'">
            <svg v-if="overview.assetChange >= 0" width="12" height="12" viewBox="0 0 24 24" fill="currentColor">
              <path d="M7 14l5-5 5 5z" />
            </svg>
            <svg v-else width="12" height="12" viewBox="0 0 24 24" fill="currentColor">
              <path d="M7 10l5 5 5-5z" />
            </svg>
            {{ Math.abs(overview.assetChange).toFixed(2) }}%
          </span>
          <span class="fl-stat-sub">过去24小时</span>
        </div>
      </div>
    </div>

    <div class="fl-stat-card">
      <div class="fl-stat-icon fl-icon-yellow">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="3" />
          <path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42" />
        </svg>
      </div>
      <div class="fl-stat-body">
        <div class="fl-stat-label">账户余额</div>
        <div class="fl-stat-value fl-text-yellow">¥{{ formatNumber(overview.balanceValue) }}</div>
        <div class="fl-stat-change">
          <span :class="overview.balanceChange >= 0 ? 'fl-rise' : 'fl-fall'">
            <svg v-if="overview.balanceChange >= 0" width="12" height="12" viewBox="0 0 24 24" fill="currentColor">
              <path d="M7 14l5-5 5 5z" />
            </svg>
            <svg v-else width="12" height="12" viewBox="0 0 24 24" fill="currentColor">
              <path d="M7 10l5 5 5-5z" />
            </svg>
            {{ Math.abs(overview.balanceChange).toFixed(2) }}%
          </span>
          <span class="fl-stat-sub">本期变化</span>
        </div>
      </div>
    </div>

    <div class="fl-stat-card">
      <div class="fl-stat-icon fl-icon-green">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="12" y1="1" x2="12" y2="23" />
          <path d="M17 5H9.5a3.5 3.5 0 000 7h5a3.5 3.5 0 010 7H6" />
        </svg>
      </div>
      <div class="fl-stat-body">
        <div class="fl-stat-label">今日收益</div>
        <div class="fl-stat-value" :class="overview.dailyIncome >= 0 ? 'fl-text-green' : 'fl-text-red'">
          ¥{{ formatNumber(overview.dailyIncome) }}
        </div>
        <div class="fl-stat-change">
          <span :class="overview.dailyIncome >= 0 ? 'fl-rise' : 'fl-fall'">
            <svg v-if="overview.dailyIncome >= 0" width="12" height="12" viewBox="0 0 24 24" fill="currentColor">
              <path d="M7 14l5-5 5 5z" />
            </svg>
            <svg v-else width="12" height="12" viewBox="0 0 24 24" fill="currentColor">
              <path d="M7 10l5 5 5-5z" />
            </svg>
            {{ Math.abs(overview.dailyIncomeRate).toFixed(2) }}%
          </span>
          <span class="fl-stat-sub">日收益率</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { DashboardOverview } from '@/api/dashboard'

defineProps<{
  overview: DashboardOverview
  formatNumber: (value: number) => string
}>()
</script>

<style scoped>
.fl-stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}

.fl-stat-body {
  flex: 1;
  min-width: 0;
}

.fl-icon-blue {
  background: var(--fl-primary-light);
  color: var(--fl-primary);
}

.fl-icon-yellow {
  background: rgba(245, 166, 35, 0.08);
  color: #f5a623;
}

.fl-icon-green {
  background: rgba(25, 190, 107, 0.08);
  color: #19be6b;
}

.fl-stat-change {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 6px;
  font-size: 13px;
}

.fl-stat-sub {
  color: var(--fl-text-placeholder);
}

.fl-text-yellow {
  color: #f5a623;
}

.fl-stat-value {
  font-family: 'Courier New', monospace;
}

@media (max-width: 900px) {
  .fl-stats-row {
    gap: 10px;
  }
}

@media (max-width: 768px) {
  .fl-stats-row {
    grid-template-columns: 1fr;
  }
}
</style>
