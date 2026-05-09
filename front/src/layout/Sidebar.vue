<template>
  <div class="sidebar">
    <div class="sidebar-logo">
      <span class="sidebar-logo-text">金融中台管理</span>
    </div>
    <el-menu
      :default-active="activeMenu"
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409eff"
      router
    >
      <el-menu-item index="/dashboard">
        <el-icon><Monitor /></el-icon>
        <span>控制台</span>
      </el-menu-item>

      <el-sub-menu index="user-mgmt">
        <template #title>
          <el-icon><User /></el-icon><span>用户管理</span>
        </template>
        <el-menu-item index="/user">用户列表</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="product-mgmt">
        <template #title>
          <el-icon><Goods /></el-icon><span>产品管理</span>
        </template>
        <el-menu-item index="/product">产品列表</el-menu-item>
        <el-menu-item index="/market">行情数据</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="trade-mgmt">
        <template #title>
          <el-icon><List /></el-icon><span>交易管理</span>
        </template>
        <el-menu-item index="/trade">委托单管理</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="account-mgmt">
        <template #title>
          <el-icon><Star /></el-icon><span>账户管理</span>
        </template>
        <el-menu-item index="/favorite">自选管理</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="msg-mgmt">
        <template #title>
          <el-icon><Message /></el-icon><span>消息管理</span>
        </template>
        <el-menu-item index="/message">站内消息</el-menu-item>
        <el-menu-item index="/news">资讯管理</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="system-mgmt">
        <template #title>
          <el-icon><Setting /></el-icon><span>系统管理</span>
        </template>
        <el-menu-item index="/system/admin">管理员</el-menu-item>
        <el-menu-item index="/system/role">角色</el-menu-item>
        <el-menu-item index="/system/resource">资源</el-menu-item>
      </el-sub-menu>

      <el-menu-item index="/search">
        <el-icon><Search /></el-icon>
        <span>ES 搜索</span>
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
  background: var(--bg-sidebar);
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.08);
}

.sidebar-logo {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(0, 0, 0, 0.15);
}

.sidebar-logo-text {
  color: #fff;
  font-size: 17px;
  font-weight: 700;
  letter-spacing: 1.5px;
  background: linear-gradient(135deg, #409eff, #6ab0ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.el-menu {
  border-right: none !important;
}
</style>
