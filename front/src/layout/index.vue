<template>
  <div class="layout-container">
    <Sidebar :is-collapsed="isCollapsed" />
    <div class="layout-main">
      <Navbar :collapsed="isCollapsed" @toggle="isCollapsed = !isCollapsed" />
      <TagsView />
      <div class="layout-content">
        <router-view />
      </div>
      <div class="layout-footer">
        <span>财富管理平台 v1.0.0</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import Sidebar from './Sidebar.vue'
import Navbar from './Navbar.vue'
import TagsView from '@/components/TagsView.vue'

const isCollapsed = ref(false)
</script>

<style scoped>
.layout-container {
  display: flex;
  height: 100vh;
  overflow: hidden;
}
.layout-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: var(--fl-content-bg);
}
.layout-content {
  flex: 1;
  padding: 16px 20px;
  overflow-y: auto;
}
.layout-footer {
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: var(--fl-text-dim);
  border-top: 1px solid var(--fl-border);
  background: #fff;
  flex-shrink: 0;
}
</style>
