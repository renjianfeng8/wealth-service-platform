<template>
  <div class="sidebar" :class="{ collapsed: isCollapsed }">
    <div class="sidebar-logo" @click="router.push('/admin/dashboard')">
      <div class="logo-icon">
        <el-icon :size="24"><TrendCharts /></el-icon>
      </div>
      <span v-show="!isCollapsed" class="logo-text">财富管理平台</span>
    </div>

    <el-menu
      :default-active="activeMenu"
      router
      :collapse="isCollapsed"
      :collapse-transition="false"
      background-color="#1a365d"
      text-color="#ffffffb3"
      active-text-color="#ffffff"
    >
      <el-menu-item index="/admin/dashboard">
        <el-icon><Odometer /></el-icon>
        <span>控制面板</span>
      </el-menu-item>

      <el-sub-menu index="system">
        <template #title>
          <el-icon><Setting /></el-icon>
          <span>系统管理</span>
        </template>
        <el-menu-item index="/admin/system/admin">管理员管理</el-menu-item>
        <el-menu-item index="/admin/system/role">角色管理</el-menu-item>
        <el-menu-item index="/admin/system/resource">资源管理</el-menu-item>
        <el-menu-item index="/admin/system/admin-role">管理员角色关联</el-menu-item>
        <el-menu-item index="/admin/system/role-resource">角色资源关联</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="user-mgmt">
        <template #title>
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </template>
        <el-menu-item index="/admin/user">用户列表</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="product-mgmt">
        <template #title>
          <el-icon><Goods /></el-icon>
          <span>产品管理</span>
        </template>
        <el-menu-item index="/admin/product">产品列表</el-menu-item>
        <el-menu-item index="/admin/market">行情数据</el-menu-item>
        <el-menu-item index="/admin/favorite">用户自选</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="trade-mgmt">
        <template #title>
          <el-icon><List /></el-icon>
          <span>交易管理</span>
        </template>
        <el-menu-item index="/admin/trade">交易委托</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="msg-mgmt">
        <template #title>
          <el-icon><Message /></el-icon>
          <span>消息管理</span>
        </template>
        <el-menu-item index="/admin/message">站内消息</el-menu-item>
        <el-menu-item index="/admin/news">财经资讯</el-menu-item>
      </el-sub-menu>

      <el-menu-item index="/admin/search">
        <el-icon><Search /></el-icon>
        <span>产品搜索</span>
      </el-menu-item>
    </el-menu>

    <div class="sidebar-footer" v-show="!isCollapsed">
      <div class="user-info">
        <el-avatar :size="32" icon="UserFilled" />
        <div class="user-detail">
          <div class="user-name">{{ userStore.username || '管理员' }}</div>
          <div class="user-role">超级管理员</div>
        </div>
      </div>
      <el-button text size="small" class="logout-btn" @click="handleLogout">
        <el-icon><SwitchButton /></el-icon>
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store'
import {
  TrendCharts, Odometer, Setting, User, Goods, List,
  Message, Search, SwitchButton,
} from '@element-plus/icons-vue'

defineProps<{ isCollapsed: boolean }>()
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const activeMenu = computed(() => route.path)

function handleLogout() {
  userStore.logout()
  router.push('/home')
}
</script>

<style scoped>
.sidebar {
  width: var(--fl-sidebar-width);
  height: 100vh;
  background: var(--fl-sidebar-bg);
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  overflow: hidden;
  flex-shrink: 0;
}
.sidebar.collapsed {
  width: var(--fl-sidebar-collapsed-width);
}
.sidebar.collapsed :deep(.el-menu-item),
.sidebar.collapsed :deep(.el-sub-menu__title) {
  margin: 2px 0;
  padding: 0 !important;
  justify-content: center;
}
.sidebar.collapsed .logo-icon {
  width: 100%;
  justify-content: center;
}
.sidebar-logo {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  border-bottom: 1px solid rgba(255,255,255,0.1);
  flex-shrink: 0;
}
.logo-icon {
  color: #fff;
  display: flex;
  align-items: center;
}
.logo-text {
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 1px;
  white-space: nowrap;
}
.sidebar :deep(.el-menu) {
  border-right: none !important;
  flex: 1;
  overflow-y: auto;
}
.sidebar :deep(.el-menu-item),
.sidebar :deep(.el-sub-menu__title) {
  height: 44px;
  line-height: 44px;
  margin: 2px 8px;
  border-radius: 6px;
  width: auto !important;
}
.sidebar :deep(.el-menu-item:hover),
.sidebar :deep(.el-sub-menu__title:hover) {
  background: var(--fl-sidebar-hover-bg) !important;
}
.sidebar :deep(.el-menu-item.is-active) {
  background: var(--fl-sidebar-active-bg) !important;
  color: #fff !important;
}
.sidebar :deep(.el-menu-item.is-active .el-icon) {
  color: #fff !important;
}
.sidebar :deep(.el-sub-menu .el-menu) {
  background: rgba(0,0,0,0.15) !important;
}
.sidebar :deep(.el-sub-menu .el-menu .el-menu-item) {
  padding-left: 48px !important;
  height: 38px;
  line-height: 38px;
}
.sidebar-footer {
  padding: 12px;
  border-top: 1px solid rgba(255,255,255,0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}
.user-detail {
  min-width: 0;
}
.user-name {
  color: #fff;
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.user-role {
  color: #ffffff80;
  font-size: 11px;
}
.logout-btn {
  color: #ffffff80 !important;
}
.logout-btn:hover {
  color: #fff !important;
}
</style>
