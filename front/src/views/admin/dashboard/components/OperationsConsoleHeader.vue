<template>
  <section class="ops-header">
    <div class="ops-main">
      <div class="ops-eyebrow">全局运营状态</div>
      <h1>运营控制台</h1>
      <p>聚合资产、订单、消息与市场波动，优先暴露需要管理员处理的事项。</p>
    </div>

    <div class="ops-status">
      <div class="ops-pill">
        <span class="ops-dot" />
        <span>实时监控</span>
      </div>
      <div class="ops-stat">
        <span class="ops-stat-label">待处理订单</span>
        <strong>{{ pendingOrders }}</strong>
      </div>
      <div class="ops-stat">
        <span class="ops-stat-label">未读消息</span>
        <strong>{{ unreadMessages }}</strong>
      </div>
      <button class="ops-refresh" type="button" @click="emit('refresh')">刷新</button>
    </div>
  </section>
</template>

<script setup lang="ts">
defineProps<{
  pendingOrders: number
  unreadMessages: number
}>()

const emit = defineEmits<{
  refresh: []
}>()
</script>

<style scoped>
.ops-header {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 24px;
  align-items: center;
  padding: 22px 24px;
  border: 1px solid rgba(26, 109, 255, 0.14);
  border-radius: 8px;
  background:
    linear-gradient(135deg, rgba(26, 109, 255, 0.08), rgba(25, 190, 107, 0.06)),
    var(--fl-card-bg);
  box-shadow: var(--fl-shadow);
}

.ops-eyebrow {
  color: var(--fl-primary);
  font-size: 12px;
  font-weight: 700;
  margin-bottom: 8px;
}

.ops-main h1 {
  margin: 0;
  color: var(--fl-text);
  font-size: 26px;
  line-height: 1.2;
  font-weight: 700;
}

.ops-main p {
  margin: 8px 0 0;
  color: var(--fl-text-secondary);
  font-size: 13px;
  line-height: 1.7;
}

.ops-status {
  display: grid;
  grid-auto-flow: column;
  grid-auto-columns: max-content;
  gap: 12px;
  align-items: center;
}

.ops-pill,
.ops-stat,
.ops-refresh {
  height: 38px;
  border-radius: 8px;
  border: 1px solid var(--fl-border);
  background: #fff;
}

.ops-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 0 12px;
  color: var(--fl-text-secondary);
  font-size: 12px;
}

.ops-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--fl-success);
  box-shadow: 0 0 0 4px rgba(25, 190, 107, 0.12);
}

.ops-stat {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 12px;
}

.ops-stat-label {
  color: var(--fl-text-dim);
  font-size: 12px;
}

.ops-stat strong {
  color: var(--fl-text);
  font-size: 18px;
}

.ops-refresh {
  padding: 0 16px;
  color: #fff;
  background: var(--fl-primary);
  border-color: var(--fl-primary);
  cursor: pointer;
  font-weight: 600;
  font-family: inherit;
}

.ops-refresh:hover {
  filter: brightness(0.96);
}

@media (max-width: 900px) {
  .ops-header {
    grid-template-columns: 1fr;
  }

  .ops-status {
    grid-auto-flow: row;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .ops-pill,
  .ops-stat,
  .ops-refresh {
    width: 100%;
    justify-content: center;
  }
}
</style>
