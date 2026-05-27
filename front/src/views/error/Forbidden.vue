<template>
  <div class="forbidden">
    <!-- 浮动感叹号装饰 -->
    <div class="forbidden-shapes">
      <div class="ex ex-1">!</div>
      <div class="ex ex-2">!</div>
      <div class="ex ex-3">!</div>
      <div class="ex ex-4">!</div>
    </div>

    <div class="forbidden-content">
      <!-- Logo -->
      <div class="forbidden-brand">
        <el-icon :size="48" class="brand-icon"><TrendCharts /></el-icon>
      </div>
      <div class="brand-text">理财服务平台</div>

      <!-- 403 渐变文字 -->
      <h1 class="forbidden-code">403</h1>

      <!-- 标题 -->
      <h2 class="forbidden-title">权限不足</h2>

      <!-- 描述 -->
      <p class="forbidden-desc">抱歉，您没有访问该页面的权限，请联系管理员</p>

      <!-- 按钮组 -->
      <div class="forbidden-actions">
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
.forbidden {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  position: relative;
  overflow: hidden;
}

/* ===== 浮动感叹号 ===== */
.forbidden-shapes {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.ex {
  position: absolute;
  font-weight: 800;
  opacity: 0.06;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.ex-1 {
  font-size: 120px;
  color: #ff9500;
  top: 10%;
  left: 8%;
  animation: float 8s ease-in-out infinite;
}

.ex-2 {
  font-size: 80px;
  color: #ff3b30;
  bottom: 15%;
  right: 10%;
  animation: float 10s ease-in-out infinite reverse;
}

.ex-3 {
  font-size: 60px;
  color: #1a6dff;
  top: 50%;
  right: 5%;
  animation: float 12s ease-in-out infinite 2s;
}

.ex-4 {
  font-size: 100px;
  color: #34c759;
  top: 20%;
  right: 25%;
  animation: float 9s ease-in-out infinite 1s reverse;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(30px, -30px) scale(1.05); }
}

/* ===== 品牌 Logo ===== */
.forbidden-brand {
  margin-bottom: 8px;
}

.brand-icon {
  color: #ff9500;
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
.forbidden-content {
  text-align: center;
  padding: 40px 24px;
  position: relative;
  z-index: 1;
}

/* 403 数字 */
.forbidden-code {
  font-size: 120px;
  font-weight: 800;
  line-height: 1;
  margin: 0 0 16px;
  background: linear-gradient(135deg, #ff9500 0%, #ff6b00 100%);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* 标题 */
.forbidden-title {
  font-size: 24px;
  font-weight: 600;
  color: #1c1c1e;
  margin: 0 0 12px;
}

/* 描述 */
.forbidden-desc {
  font-size: 14px;
  color: #8e8e93;
  margin: 0 0 32px;
}

/* 按钮组 */
.forbidden-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .forbidden-code {
    font-size: 72px;
  }
  .forbidden-title {
    font-size: 20px;
  }
  .forbidden-actions {
    flex-direction: column;
    align-items: center;
  }

  .ex-1 { font-size: 80px; }
  .ex-2 { font-size: 50px; }
  .ex-3 { font-size: 40px; }
  .ex-4 { font-size: 60px; }
}
</style>
