<template>
  <div class="layout-container">
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
</script>

<style scoped>
.layout-container { display: flex; height: 100vh; background: var(--bg-page); }
.layout-sidebar { width: var(--sidebar-width); overflow-y: auto; flex-shrink: 0; }
.layout-main { flex: 1; display: flex; flex-direction: column; overflow: hidden; min-width: 0; }
.layout-content { flex: 1; padding: 20px 24px; background: var(--bg-page); overflow-y: auto; }
</style>
