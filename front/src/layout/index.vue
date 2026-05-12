<template>
  <div class="layout-container" :class="{ 'layout-dark': isDark }">
    <div class="layout-sidebar">
      <Sidebar />
    </div>
    <div class="layout-main">
      <Navbar :dark="isDark" />
      <div class="layout-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import Sidebar from './Sidebar.vue'
import Navbar from './Navbar.vue'

const route = useRoute()
const isDark = computed(() => route.path === '/dashboard')
</script>

<style scoped>
.layout-container { display: flex; height: 100vh; background: var(--bg-page); }
.layout-container.layout-dark { background: #0d1117; }
.layout-sidebar { width: var(--sidebar-width); overflow-y: auto; flex-shrink: 0; }
.layout-main { flex: 1; display: flex; flex-direction: column; overflow: hidden; min-width: 0; }
.layout-content { flex: 1; padding: 20px 24px; background: var(--bg-page); overflow-y: auto; }
.layout-dark .layout-content {
  background: #0d1117;
  padding: 10px 16px;
}
</style>
