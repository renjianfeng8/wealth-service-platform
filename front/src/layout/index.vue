<template>
  <div class="layout-container">
    <Sidebar :is-collapsed="isCollapsed" />
    <div class="layout-main">
      <Navbar :collapsed="isCollapsed" @toggle="isCollapsed = !isCollapsed" />
      <div class="layout-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import Sidebar from './Sidebar.vue'
import Navbar from './Navbar.vue'

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
</style>
