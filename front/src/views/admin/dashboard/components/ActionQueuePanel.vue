<template>
  <div class="fl-card action-queue">
    <div class="fl-card-header">
      <div>
        <div class="fl-card-title">运营任务</div>
        <div class="fl-card-subtitle">按优先级处理异常与待办</div>
      </div>
    </div>

    <div class="action-list">
      <button
        v-for="action in actions"
        :key="action.label"
        type="button"
        :class="['action-item', { done: action.done }]"
        @click="go(action.path)"
      >
        <span class="action-marker" />
        <span class="action-copy">
          <strong>{{ action.label }}</strong>
          <em>{{ action.description }}</em>
        </span>
        <span class="action-count">{{ action.count }}</span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

export interface ActionQueueItem {
  label: string
  description: string
  count: number
  path: string
  done: boolean
}

defineProps<{
  actions: ActionQueueItem[]
}>()

const router = useRouter()

function go(path: string) {
  router.push(path)
}
</script>

<style scoped>
.action-queue {
  min-width: 0;
}

.action-list {
  display: grid;
  gap: 10px;
}

.action-item {
  display: grid;
  grid-template-columns: 10px minmax(0, 1fr) auto;
  gap: 10px;
  align-items: center;
  width: 100%;
  min-height: 58px;
  padding: 10px 12px;
  border: 1px solid var(--fl-border);
  border-radius: 8px;
  background: #fff;
  text-align: left;
  cursor: pointer;
  font-family: inherit;
}

.action-item:hover {
  border-color: rgba(26, 109, 255, 0.36);
  box-shadow: var(--fl-shadow-hover);
}

.action-item.done {
  background: #fbfcfe;
}

.action-marker {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--fl-warning);
}

.action-item.done .action-marker {
  background: var(--fl-success);
}

.action-copy {
  display: grid;
  gap: 3px;
  min-width: 0;
}

.action-copy strong {
  color: var(--fl-text);
  font-size: 13px;
  font-weight: 700;
}

.action-copy em {
  color: var(--fl-text-dim);
  font-size: 12px;
  font-style: normal;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.action-count {
  min-width: 32px;
  height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: rgba(245, 166, 35, 0.12);
  color: var(--fl-warning);
  font-size: 14px;
  font-weight: 700;
}

.action-item.done .action-count {
  background: rgba(25, 190, 107, 0.12);
  color: var(--fl-success);
}
</style>
