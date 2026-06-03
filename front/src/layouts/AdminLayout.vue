<template>
  <div class="layout-container" :class="{ 'mobile-sidebar-open': mobileSidebarOpen }">
    <div v-if="mobileSidebarOpen" class="sidebar-mask" @click="mobileSidebarOpen = false"></div>
    <Sidebar :is-collapsed="isCollapsed" />
    <div class="layout-main">
      <Navbar :collapsed="isCollapsed" @toggle="handleToggle" />
      <TagsView />
      <div class="layout-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch } from 'vue'
import Sidebar from './Sidebar.vue'
import Navbar from './Navbar.vue'
import TagsView from '@/components/TagsView.vue'

const COLLAPSED_KEY = 'wealth_admin_sidebar_collapsed'
const isCollapsed = ref(localStorage.getItem(COLLAPSED_KEY) === '1')
const mobileSidebarOpen = ref(false)

function handleToggle() {
  if (window.innerWidth <= 768) {
    mobileSidebarOpen.value = !mobileSidebarOpen.value
    isCollapsed.value = false
    return
  }
  isCollapsed.value = !isCollapsed.value
}

function handleResize() {
  if (window.innerWidth > 768) {
    mobileSidebarOpen.value = false
  }
}

watch(isCollapsed, (value) => {
  localStorage.setItem(COLLAPSED_KEY, value ? '1' : '0')
})

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
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
  scroll-behavior: smooth;
}
.sidebar-mask {
  display: none;
}

@media (max-width: 768px) {
  .layout-container {
    position: relative;
  }
  .layout-container :deep(.sidebar) {
    position: fixed;
    top: 0;
    bottom: 0;
    left: 0;
    z-index: 1001;
    transform: translateX(-100%);
    transition: transform 0.25s ease;
  }
  .layout-container.mobile-sidebar-open :deep(.sidebar) {
    transform: translateX(0);
  }
  .sidebar-mask {
    display: block;
    position: fixed;
    inset: 0;
    z-index: 1000;
    background: rgba(15, 23, 42, 0.38);
  }
  .layout-content {
    padding: 12px;
  }
}
</style>
