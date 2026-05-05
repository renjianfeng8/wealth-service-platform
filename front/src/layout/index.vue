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
.layout-container { display: flex; height: 100vh; }
.layout-sidebar { width: var(--sidebar-width); background: #304156; overflow-y: auto; }
.layout-main { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.layout-content { flex: 1; padding: 20px; background: #f0f2f5; overflow-y: auto; }
</style>
