<template>
  <div class="floating-actions" aria-label="页面快捷操作">
    <el-tooltip content="返回上一页" placement="left">
      <el-button circle :icon="Back" @click="goBack" />
    </el-tooltip>
    <el-tooltip content="刷新当前页" placement="left">
      <el-button circle :icon="Refresh" @click="refreshPage" />
    </el-tooltip>
    <el-tooltip content="回到顶部" placement="left">
      <el-button circle :icon="ArrowUp" @click="scrollTop" />
    </el-tooltip>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ArrowUp, Back, Refresh } from '@element-plus/icons-vue'

const router = useRouter()

function goBack() {
  if (window.history.length > 1) {
    router.back()
    return
  }
  router.push('/home')
}

function refreshPage() {
  router.go(0)
}

function scrollTop() {
  const mainScroller = document.querySelector('.layout-content') || document.documentElement
  mainScroller.scrollTo({ top: 0, behavior: 'smooth' })
}
</script>

<style scoped>
.floating-actions {
  position: fixed;
  right: 22px;
  bottom: 28px;
  z-index: 1200;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.floating-actions :deep(.el-button) {
  width: 38px;
  height: 38px;
  margin: 0;
  color: var(--fl-text-secondary, var(--text-secondary));
  background: rgba(255, 255, 255, 0.94);
  border-color: var(--fl-border, var(--border-color));
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.12);
}

.floating-actions :deep(.el-button:hover) {
  color: var(--fl-primary, var(--primary));
  border-color: var(--fl-primary, var(--primary));
}

@media (max-width: 768px) {
  .floating-actions {
    right: 14px;
    bottom: 18px;
  }
}
</style>
