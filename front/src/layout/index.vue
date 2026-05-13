<template>
  <div class="layout-container" :class="{ 'finance-light': true }">
    <div class="layout-sidebar">
      <Sidebar />
    </div>
    <div class="layout-main">
      <Navbar />
      <div class="layout-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import Sidebar from './Sidebar.vue'
import Navbar from './Navbar.vue'
import '@/styles/light-theme.css'
</script>

<style scoped>
.layout-container {
  display: flex;
  height: 100vh;
  background: #f0f2f5;
}

.layout-sidebar {
  width: 220px;
  overflow-y: auto;
  flex-shrink: 0;
}

.layout-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
}

.layout-content {
  flex: 1;
  padding: 18px 22px;
  background: #f0f2f5;
  overflow-y: auto;
}
</style>
