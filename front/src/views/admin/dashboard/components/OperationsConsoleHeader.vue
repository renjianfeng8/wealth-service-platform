<template>
  <section class="welcome-banner">
    <div class="welcome-main">
      <div class="welcome-greeting">
        <h1>{{ greeting }}{{ adminName }}</h1>
        <p class="welcome-sub">欢迎回来，以下是平台运行概况</p>
      </div>
      <div class="welcome-meta">
        <span class="meta-item">
          <span class="health-dot" />
          系统运行正常
        </span>
        <span class="meta-item meta-time">数据更新于 {{ lastRefreshTime }}</span>
        <button class="refresh-btn" type="button" @click="$emit('refresh')">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="23 4 23 10 17 10" />
            <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10" />
          </svg>
          刷新
        </button>
      </div>
    </div>

    <div class="alert-row">
      <div
        :class="['alert-card', { 'alert-warning': pendingOrders > 0, 'alert-done': pendingOrders === 0 }]"
        @click="$router.push('/admin/trade')"
      >
        <div class="alert-icon-wrap">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
            <polyline points="14 2 14 8 20 8" />
            <line x1="16" y1="13" x2="8" y2="13" />
            <line x1="16" y1="17" x2="8" y2="17" />
            <polyline points="10 9 9 9 8 9" />
          </svg>
        </div>
        <div class="alert-body">
          <span class="alert-label">待处理订单</span>
          <strong class="alert-count">{{ pendingOrders }}</strong>
        </div>
      </div>

      <div
        :class="['alert-card', { 'alert-danger': unreadMessages > 0, 'alert-done': unreadMessages === 0 }]"
        @click="$router.push('/admin/message')"
      >
        <div class="alert-icon-wrap">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
          </svg>
        </div>
        <div class="alert-body">
          <span class="alert-label">未读消息</span>
          <strong class="alert-count">{{ unreadMessages }}</strong>
        </div>
      </div>

      <div
        :class="['alert-card', { 'alert-warning': disabledProducts > 0, 'alert-done': disabledProducts === 0 }]"
        @click="$router.push('/admin/product')"
      >
        <div class="alert-icon-wrap">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="8" x2="12" y2="12" />
            <line x1="12" y1="16" x2="12.01" y2="16" />
          </svg>
        </div>
        <div class="alert-body">
          <span class="alert-label">已下架产品</span>
          <strong class="alert-count">{{ disabledProducts }}</strong>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps<{
  adminName: string
  lastRefreshTime: string
  pendingOrders: number
  unreadMessages: number
  disabledProducts: number
}>()

defineEmits<{ refresh: [] }>()

const router = useRouter()

const hour = new Date().getHours()
const greeting = computed(() => {
  if (hour < 6) return '凌晨好，'
  if (hour < 12) return '上午好，'
  if (hour < 18) return '下午好，'
  return '晚上好，'
})
</script>

<style scoped>
.welcome-banner {
  background:
    linear-gradient(135deg, rgba(26, 109, 255, 0.07) 0%, rgba(25, 190, 107, 0.05) 100%),
    var(--fl-card-bg);
  border: 1px solid rgba(26, 109, 255, 0.12);
  border-radius: var(--fl-radius);
  padding: 20px 24px;
}

.welcome-main {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.welcome-greeting h1 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--fl-text);
}

.welcome-sub {
  margin: 4px 0 0;
  font-size: 13px;
  color: var(--fl-text-dim);
}

.welcome-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--fl-text-secondary);
}

.health-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--fl-rise);
  box-shadow: 0 0 0 3px rgba(52, 199, 89, 0.15);
}

.meta-time {
  color: var(--fl-text-placeholder);
}

.refresh-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 6px 14px;
  border: 1px solid var(--fl-border);
  border-radius: 6px;
  background: var(--fl-card-bg);
  color: var(--fl-text-secondary);
  font-size: 12px;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s;
}
.refresh-btn:hover {
  border-color: var(--fl-primary);
  color: var(--fl-primary);
}

/* ---- Alert Cards ---- */
.alert-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
  margin-top: 18px;
}

.alert-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 18px;
  border-radius: 10px;
  border: 1px solid var(--fl-border);
  background: var(--fl-card-bg);
  cursor: pointer;
  transition: all 0.2s ease;
}
.alert-card:hover {
  box-shadow: var(--fl-shadow-hover);
  transform: translateY(-1px);
}

.alert-icon-wrap {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.alert-warning .alert-icon-wrap {
  background: rgba(245, 166, 35, 0.1);
  color: #f5a623;
}
.alert-danger .alert-icon-wrap {
  background: rgba(255, 59, 48, 0.08);
  color: var(--fl-fall);
}
.alert-done .alert-icon-wrap {
  background: rgba(52, 199, 89, 0.08);
  color: var(--fl-rise);
}

.alert-body {
  display: flex;
  align-items: baseline;
  gap: 10px;
  min-width: 0;
}

.alert-label {
  font-size: 13px;
  color: var(--fl-text-secondary);
  white-space: nowrap;
}

.alert-count {
  font-size: 22px;
  font-weight: 700;
  font-family: 'Courier New', monospace;
  line-height: 1;
}

.alert-warning .alert-count { color: #f5a623; }
.alert-danger .alert-count { color: var(--fl-fall); }
.alert-done .alert-count { color: var(--fl-rise); }

@media (max-width: 768px) {
  .alert-row {
    grid-template-columns: 1fr;
  }
  .welcome-main {
    flex-direction: column;
  }
}
</style>
