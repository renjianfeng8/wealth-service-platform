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
  Search,
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
  { label: '用户管理', path: '/admin/user', icon: User, bg: '#1a6dff' },
  { label: '产品管理', path: '/admin/product', icon: Goods, bg: '#19be6b' },
  { label: '行情数据', path: '/admin/market', icon: DataLine, bg: '#f5a623' },
  { label: '交易管理', path: '/admin/trade', icon: List, bg: '#ed4014' },
  { label: '自选管理', path: '/admin/favorite', icon: Star, bg: '#d29922' },
  { label: '消息管理', path: '/admin/message', icon: Message, bg: '#00b894' },
  { label: '管理员', path: '/admin/system/admin', icon: Setting, bg: '#8b5cf6' },
]
</script>

<style scoped>
.fl-entry-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 10px;
}

.fl-entry-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 8px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s;
}

.fl-entry-item:hover {
  background: #f5f7fa;
}

.fl-entry-icon {
  width: 42px;
  height: 42px;
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
  font-size: 12px;
  color: var(--fl-text-secondary);
  white-space: nowrap;
}
</style>
