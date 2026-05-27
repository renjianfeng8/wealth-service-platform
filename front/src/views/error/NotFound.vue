<template>
  <div class="not-found">
    <!-- 浮动问号装饰 -->
    <div class="not-found-shapes">
      <div class="q q-1">?</div>
      <div class="q q-2">?</div>
      <div class="q q-3">?</div>
      <div class="q q-4">?</div>
    </div>

    <div class="not-found-content">
      <!-- Logo -->
      <div class="not-found-brand">
        <el-icon :size="48" class="brand-icon"><TrendCharts /></el-icon>
      </div>
      <div class="brand-text">理财服务平台</div>

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
import { TrendCharts } from '@element-plus/icons-vue'

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
  position: relative;
  overflow: hidden;
}

/* ===== 浮动问号 ===== */
.not-found-shapes {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.q {
  position: absolute;
  font-weight: 800;
  opacity: 0.06;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.q-1 {
  font-size: 120px;
  color: #1a6dff;
  top: 10%;
  left: 8%;
  animation: float 8s ease-in-out infinite;
}

.q-2 {
  font-size: 80px;
  color: #34c759;
  bottom: 15%;
  right: 10%;
  animation: float 10s ease-in-out infinite reverse;
}

.q-3 {
  font-size: 60px;
  color: #ff9500;
  top: 50%;
  right: 5%;
  animation: float 12s ease-in-out infinite 2s;
}

.q-4 {
  font-size: 100px;
  color: #ff3b30;
  top: 20%;
  right: 25%;
  animation: float 9s ease-in-out infinite 1s reverse;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(30px, -30px) scale(1.05); }
}

/* ===== 品牌 Logo ===== */
.not-found-brand {
  margin-bottom: 8px;
}

.brand-icon {
  color: #1a6dff;
}

.brand-text {
  font-size: 18px;
  font-weight: 700;
  background: linear-gradient(135deg, #1a6dff 0%, #0a4dcc 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 32px;
  display: block;
}

/* ===== 内容区域 ===== */
.not-found-content {
  text-align: center;
  padding: 40px 24px;
  position: relative;
  z-index: 1;
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

  .q-1 { font-size: 80px; }
  .q-2 { font-size: 50px; }
  .q-3 { font-size: 40px; }
  .q-4 { font-size: 60px; }
}
</style>
