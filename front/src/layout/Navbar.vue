<template>
  <div class="navbar" :class="{ 'navbar-dark': dark }">
    <div class="navbar-left">
      <el-breadcrumb>
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>{{ route.meta?.title }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <div class="navbar-right">
      <span class="navbar-user">{{ userStore.username }}</span>
      <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/index'

defineProps<{ dark?: boolean }>()

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.navbar {
  height: var(--navbar-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: #fff;
  border-bottom: 1px solid var(--border-color);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  position: relative;
  z-index: 10;
}
.navbar-dark {
  background: #161b22 !important;
  border-bottom-color: #21262d !important;
}
.navbar-dark .navbar-user { color: #e6edf3; }
:deep(.navbar-dark .el-breadcrumb__inner) { color: #8b949e; }
:deep(.navbar-dark .el-breadcrumb__inner.is-link) { color: #e6edf3; }
:deep(.navbar-dark .el-breadcrumb__separator) { color: #484f58; }

.navbar-left {
  display: flex;
  align-items: center;
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 14px;
}

.navbar-user {
  font-size: 14px;
  color: var(--text-regular);
  font-weight: 500;
}

.navbar-user::before {
  content: '';
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--success);
  margin-right: 8px;
  vertical-align: middle;
}
</style>
