<template>
  <header class="navbar">
    <div class="navbar-left">
      <el-button text class="collapse-btn" @click="$emit('toggle')">
        <el-icon :size="20"><Fold v-if="!collapsed" /><Expand v-else /></el-icon>
      </el-button>
      <el-breadcrumb>
        <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>{{ route.meta?.title as string || '' }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="navbar-right">
      <div class="search-box" @click="goSearch">
        <el-icon><Search /></el-icon>
        <span class="search-placeholder">搜索产品名称/编码...</span>
        <span class="search-shortcut">⌘K</span>
      </div>

      <el-badge :value="3" :hidden="false" class="notice-badge">
        <el-button text @click="showNotices = !showNotices">
          <el-icon :size="20"><Bell /></el-icon>
        </el-button>
      </el-badge>

      <el-dropdown trigger="click">
        <span class="user-dropdown">
          <el-avatar :size="28" icon="UserFilled" />
          <span class="username">{{ userStore.username || '管理员' }}</span>
          <el-icon><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="router.push('/dashboard')">
              <el-icon><User /></el-icon>个人信息
            </el-dropdown-item>
            <el-dropdown-item divided @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store'
import {
  Fold, Expand, Search, Bell, UserFilled,
  ArrowDown, User, SwitchButton,
} from '@element-plus/icons-vue'

defineProps<{ collapsed: boolean }>()
defineEmits<{ toggle: [] }>()

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const showNotices = ref(false)

function goSearch() {
  router.push('/search')
}

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.navbar {
  height: var(--fl-header-height);
  background: var(--fl-header-bg);
  border-bottom: 1px solid var(--fl-header-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  flex-shrink: 0;
}
.navbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.collapse-btn {
  font-size: 16px;
  color: var(--fl-text-secondary);
}
.navbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  background: #f5f7fa;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--fl-text-dim);
}
.search-box:hover {
  background: #eef1f6;
}
.search-placeholder {
  font-size: 13px;
  white-space: nowrap;
}
.search-shortcut {
  font-size: 11px;
  padding: 1px 6px;
  background: #e4e7ed;
  border-radius: 4px;
  color: var(--fl-text-placeholder);
}
.notice-badge {
  line-height: 1;
}
.user-dropdown {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background 0.2s;
}
.user-dropdown:hover {
  background: #f5f7fa;
}
.username {
  font-size: 13px;
  color: var(--fl-text-secondary);
  font-weight: 500;
}
</style>
