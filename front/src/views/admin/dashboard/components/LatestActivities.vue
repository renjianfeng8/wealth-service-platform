<template>
  <div class="fl-card">
    <div class="fl-card-header">
      <div>
        <div class="fl-card-title">最新动态</div>
        <div class="fl-card-subtitle">最近的站内消息与通知</div>
      </div>
      <el-button link type="primary" @click="router.push('/admin/message')">查看全部</el-button>
    </div>

    <el-empty v-if="!messages.length" description="暂无动态" :image-size="50" />

    <div v-else class="activity-list">
      <div
        v-for="msg in messages"
        :key="msg.id"
        class="activity-item"
        @click="router.push('/admin/message')"
      >
        <div :class="['activity-dot', dotColor(msg.msgType)]" />
        <div class="activity-content">
          <div class="activity-title">{{ msg.msgTitle || '站内通知' }}</div>
          <div class="activity-meta">
            <el-tag :type="tagType(msg.msgType)" size="small" effect="plain">
              {{ msgTypeText(msg.msgType) }}
            </el-tag>
            <span class="activity-time">{{ formatRelativeTime(msg.createTime) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import type { WeaMessage } from '@/types'
import { formatRelativeTime, msgTypeText } from '@/utils/format'

defineProps<{
  messages: WeaMessage[]
}>()

const router = useRouter()

function dotColor(type?: number): string {
  const map: Record<number, string> = { 1: 'dot-blue', 2: 'dot-green', 3: 'dot-orange', 4: 'dot-purple' }
  return map[type ?? 0] || 'dot-gray'
}

function tagType(type?: number): string {
  const map: Record<number, string> = { 1: 'primary', 2: 'success', 3: 'warning', 4: 'info' }
  return map[type ?? 0] || 'info'
}
</script>

<style scoped>
.fl-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 14px;
}

.fl-card-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--fl-text);
}

.fl-card-subtitle {
  font-size: 11px;
  color: var(--fl-text-dim);
  margin-top: 2px;
}

.activity-list {
  display: flex;
  flex-direction: column;
}

.activity-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 8px;
  border-bottom: 1px solid var(--fl-border-light);
  cursor: pointer;
  transition: background 0.15s;
  border-radius: 6px;
  margin: 0 -8px;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-item:hover {
  background: #f5f7fa;
}

.activity-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-top: 4px;
  flex-shrink: 0;
}

.dot-blue { background: var(--fl-primary); }
.dot-green { background: var(--fl-rise); }
.dot-orange { background: #f5a623; }
.dot-purple { background: #8b5cf6; }
.dot-gray { background: var(--fl-text-placeholder); }

.activity-content {
  flex: 1;
  min-width: 0;
}

.activity-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--fl-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
}

.activity-time {
  font-size: 11px;
  color: var(--fl-text-placeholder);
}
</style>
