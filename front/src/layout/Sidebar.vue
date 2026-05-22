<template>
  <div class="sidebar">
    <div class="sidebar-logo">
      <span class="sidebar-logo-text">理财服务管理</span>
    </div>
    <el-menu
        :default-active="activeMenu"
        router
        :collapse-transition="false"
        class="sidebar-menu"
    >
      <el-menu-item index="/dashboard">
        <el-icon><Monitor /></el-icon>
        <span>控制台</span>
      </el-menu-item>

      <el-sub-menu index="user-mgmt">
        <template #title>
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </template>
        <el-menu-item index="/user">用户列表</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="product-mgmt">
        <template #title>
          <el-icon><Goods /></el-icon>
          <span>产品管理</span>
        </template>
        <el-menu-item index="/product">产品列表</el-menu-item>
        <el-menu-item index="/market">行情数据</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="trade-mgmt">
        <template #title>
          <el-icon><List /></el-icon>
          <span>交易管理</span>
        </template>
        <el-menu-item index="/trade">委托单管理</el-menu-item>
      </el-sub-menu>

      <el-menu-item index="/favorite">
        <el-icon><Star /></el-icon>
        <span>自选管理</span>
      </el-menu-item>

      <el-sub-menu index="msg-mgmt">
        <template #title>
          <el-icon><Message /></el-icon>
          <span>消息管理</span>
        </template>
        <el-menu-item index="/message">站内消息</el-menu-item>
        <el-menu-item index="/news">资讯管理</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="system-mgmt">
        <template #title>
          <el-icon><Setting /></el-icon>
          <span>系统管理</span>
        </template>
        <el-menu-item index="/system/admin">管理员</el-menu-item>
        <el-menu-item index="/system/role">角色</el-menu-item>
        <el-menu-item index="/system/resource">资源</el-menu-item>
      </el-sub-menu>

      <el-menu-item index="/search">
        <el-icon><Search /></el-icon>
        <span>全文搜索</span>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style scoped>
.sidebar {
  height: 100%;
  display: flex;
  flex-direction: column;
  /* 浅色背景（图片样式） */
  background: #ffffff;
  border-right: 1px solid #e5e6eb;
  user-select: none;
}

.sidebar-logo {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #e5e6eb;
  flex-shrink: 0;
}

.sidebar-logo-text {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

/* 菜单整体 */
:deep(.el-menu) {
  border-right: none !important;
  background: #ffffff !important;
}

/* 一级菜单项 */
:deep(.el-menu-item) {
  color: #606266 !important;
  height: 44px;
  line-height: 44px;
  margin: 2px 8px;
  border-radius: 4px !important;
  transition: all 0.2s ease;
}

:deep(.el-menu-item .el-icon) {
  font-size: 16px;
  color: #606266 !important;
}

/* hover 效果 */
:deep(.el-menu-item:hover) {
  background: #f5f7fa !important;
  color: #303133 !important;
}

/* 选中状态（蓝色高亮，和你图片一致） */
:deep(.el-menu-item.is-active) {
  background: #e8f3ff !important;
  color: #1a6dff !important;
  font-weight: 500;
}

:deep(.el-menu-item.is-active .el-icon) {
  color: #1a6dff !important;
}

/* 子菜单标题 */
:deep(.el-sub-menu__title) {
  color: #606266 !important;
  height: 44px;
  line-height: 44px;
  margin: 2px 8px;
  border-radius: 4px !important;
  transition: all 0.2s ease;
}

:deep(.el-sub-menu__title .el-icon) {
  font-size: 16px;
  color: #606266 !important;
}

:deep(.el-sub-menu__title:hover) {
  background: #f5f7fa !important;
  color: #303133 !important;
}

/* 子菜单展开箭头 */
:deep(.el-sub-menu__icon-arrow) {
  color: #c0c4cc !important;
}

/* 二级菜单 */
:deep(.el-menu--inline) {
  background: #fafafa !important;
}

:deep(.el-menu--inline .el-menu-item) {
  padding-left: 48px !important;
  height: 38px;
  line-height: 38px;
  font-size: 14px;
}

:deep(.el-menu--inline .el-menu-item.is-active) {
  background: #e8f3ff !important;
  color: #1a6dff !important;
}
</style>