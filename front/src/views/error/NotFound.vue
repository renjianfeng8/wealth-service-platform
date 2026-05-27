<template>
  <div class="not-found">
    <div class="not-found-content">
      <!-- 装饰 SVG：断环 -->
      <div class="not-found-ring">
        <svg viewBox="0 0 120 120" width="120" height="120">
          <circle cx="60" cy="60" r="48" fill="none" stroke="#e5e5ea" stroke-width="6" />
          <path d="M 60 12 A 48 48 0 1 1 30 105" fill="none" stroke="#1a6dff" stroke-width="6" stroke-linecap="round" />
        </svg>
      </div>

      <!-- 404 渐变文字 -->
      <h1 class="not-found-code">404</h1>

      <!-- 标题 -->
      <h2 class="not-found-title">页面未找到</h2>

      <!-- 描述 -->
      <p class="not-found-desc">抱歉，您访问的页面不存在或已被移除</p>

      <!-- 按钮组 -->
      <div class="not-found-actions">
        <el-button type="primary" size="large" @click="goHome">返回首页</el-button>
        <el-button size="large" @click="goBack">返回上一页</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/home')
}

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.not-found {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
}

.not-found-content {
  text-align: center;
  padding: 40px 24px;
}

/* 装饰环 */
.not-found-ring {
  margin-bottom: 24px;
  animation: ring-rotate 3s ease-in-out infinite;
}

@keyframes ring-rotate {
  0%, 100% { transform: rotate(0deg); }
  50% { transform: rotate(15deg); }
}

/* 404 数字 */
.not-found-code {
  font-size: 120px;
  font-weight: 800;
  line-height: 1;
  margin: 0 0 16px;
  background: linear-gradient(135deg, #1a6dff 0%, #0a4dcc 100%);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* 标题 */
.not-found-title {
  font-size: 24px;
  font-weight: 600;
  color: #1c1c1e;
  margin: 0 0 12px;
}

/* 描述 */
.not-found-desc {
  font-size: 14px;
  color: #8e8e93;
  margin: 0 0 32px;
}

/* 按钮组 */
.not-found-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .not-found-code {
    font-size: 72px;
  }
  .not-found-title {
    font-size: 20px;
  }
  .not-found-actions {
    flex-direction: column;
    align-items: center;
  }
}
</style>
