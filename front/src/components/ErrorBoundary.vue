<template>
  <div v-if="hasError" class="error-boundary">
    <div class="error-content">
      <el-icon :size="56" color="#ff3b30"><WarningFilled /></el-icon>
      <h2 class="error-title">页面出错了</h2>
      <p class="error-desc">很抱歉，页面遇到了意外错误，请尝试刷新</p>
      <el-button type="primary" size="large" @click="handleRetry">
        重新加载
      </el-button>
    </div>
  </div>
  <slot v-else />
</template>

<script setup lang="ts">
import { ref, onErrorCaptured } from 'vue'
import { WarningFilled } from '@element-plus/icons-vue'

const hasError = ref(false)

onErrorCaptured((err) => {
  hasError.value = true
  console.error('[ErrorBoundary] 捕获渲染异常:', err)
  return false
})

function handleRetry() {
  hasError.value = false
  window.location.reload()
}
</script>

<style scoped>
.error-boundary {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-page, #f5f7fa);
}

.error-content {
  text-align: center;
  padding: 40px 24px;
}

.error-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary, #1c1c1e);
  margin: 16px 0 8px;
}

.error-desc {
  font-size: 14px;
  color: var(--text-secondary, #8e8e93);
  margin: 0 0 24px;
}
</style>
