<template>
  <div class="fl-card">
    <div class="fl-card-header">
      <span class="fl-card-title">快捷入口</span>
    </div>
    <div class="fl-entry-grid">
      <div
        v-for="entry in entries"
        :key="entry.label"
        class="fl-entry-item"
        @click="router.push(entry.path)"
      >
        <div class="fl-entry-icon" :style="{ background: entry.bg }">
          <el-icon :size="20"><component :is="entry.icon" /></el-icon>
        </div>
        <span class="fl-entry-label">{{ entry.label }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Component } from 'vue'
import { useRouter } from 'vue-router'
import {
  User,
  Goods,
  DataLine,
  List,
  Message,
  Star,
  Setting,
} from '@element-plus/icons-vue'

interface QuickEntry {
  label: string
  path: string
  icon: Component
  bg: string
}

const router = useRouter()

const entries: QuickEntry[] = [
  { label: '用户管理', path: '/admin/user', icon: User, bg: 'linear-gradient(135deg, #1a6dff, #0a4dcc)' },
  { label: '产品管理', path: '/admin/product', icon: Goods, bg: 'linear-gradient(135deg, #34c759, #28a745)' },
  { label: '行情数据', path: '/admin/market', icon: DataLine, bg: 'linear-gradient(135deg, #ff9500, #e68a00)' },
  { label: '交易管理', path: '/admin/trade', icon: List, bg: 'linear-gradient(135deg, #8e44ad, #6c3483)' },
  { label: '自选管理', path: '/admin/favorite', icon: Star, bg: 'linear-gradient(135deg, #1a6dff, #0a4dcc)' },
  { label: '消息管理', path: '/admin/message', icon: Message, bg: 'linear-gradient(135deg, #34c759, #28a745)' },
  { label: '管理员', path: '/admin/system/admin', icon: Setting, bg: 'linear-gradient(135deg, #ff9500, #e68a00)' },
]
</script>

<style scoped>
.fl-entry-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(110px, 1fr));
  gap: 12px;
}

.fl-entry-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 8px;
  border-radius: var(--fl-radius);
  cursor: pointer;
  transition: background 0.2s;
}

.fl-entry-item:hover {
  background: var(--el-fill-color-lighter);
}

.fl-entry-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  transition: transform 0.2s;
}

.fl-entry-item:hover .fl-entry-icon {
  transform: scale(1.08);
}

.fl-entry-label {
  font-size: 13px;
  color: var(--fl-text-secondary);
  white-space: nowrap;
}
</style>
