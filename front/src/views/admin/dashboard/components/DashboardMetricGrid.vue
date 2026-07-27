<template>
  <div class="metric-grid">
    <div class="metric-card" @click="$router.push('/admin/user')">
      <div class="metric-icon metric-icon-blue">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
          <circle cx="9" cy="7" r="4" />
          <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
          <path d="M16 3.13a4 4 0 0 1 0 7.75" />
        </svg>
      </div>
      <div class="metric-body">
        <span class="metric-label">总用户数</span>
        <span class="metric-value">{{ totalUsers.toLocaleString() }}</span>
      </div>
    </div>

    <div class="metric-card" @click="$router.push('/admin/product')">
      <div class="metric-icon metric-icon-green">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z" />
          <polyline points="3.27 6.96 12 12.01 20.73 6.96" />
          <line x1="12" y1="22.08" x2="12" y2="12" />
        </svg>
      </div>
      <div class="metric-body">
        <span class="metric-label">产品总量</span>
        <span class="metric-value">{{ totalProducts.toLocaleString() }}</span>
      </div>
    </div>

    <div class="metric-card" @click="$router.push('/admin/trade')">
      <div class="metric-icon metric-icon-orange">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <line x1="12" y1="1" x2="12" y2="23" />
          <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6" />
        </svg>
      </div>
      <div class="metric-body">
        <span class="metric-label">委托订单</span>
        <span class="metric-value">{{ totalOrders.toLocaleString() }}</span>
      </div>
    </div>

    <div class="metric-card" @click="$router.push('/admin/message')">
      <div class="metric-icon metric-icon-red">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
        </svg>
      </div>
      <div class="metric-body">
        <span class="metric-label">未读消息</span>
        <span class="metric-value">{{ unreadMessages.toLocaleString() }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  totalUsers: number
  totalProducts: number
  totalOrders: number
  unreadMessages: number
}>()
</script>

<style scoped>
.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}

.metric-card {
  background: var(--fl-card-bg);
  border: 1px solid var(--fl-border);
  border-radius: var(--fl-radius);
  padding: 20px 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.25s ease;
}
.metric-card:hover {
  box-shadow: var(--fl-shadow-hover);
  transform: translateY(-2px);
}

.metric-icon {
  width: 46px;
  height: 46px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.metric-icon-blue {
  background: rgba(26, 109, 255, 0.08);
  color: var(--fl-primary);
}
.metric-icon-green {
  background: rgba(52, 199, 89, 0.08);
  color: var(--fl-rise);
}
.metric-icon-orange {
  background: rgba(245, 166, 35, 0.1);
  color: #f5a623;
}
.metric-icon-red {
  background: rgba(255, 59, 48, 0.08);
  color: var(--fl-fall);
}

.metric-body {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.metric-label {
  font-size: 13px;
  color: var(--fl-text-dim);
}

.metric-value {
  font-size: 26px;
  font-weight: 700;
  color: var(--fl-text);
  line-height: 1.2;
  font-family: 'Courier New', monospace;
}

@media (max-width: 1024px) {
  .metric-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .metric-grid {
    grid-template-columns: 1fr;
  }
}
</style>
