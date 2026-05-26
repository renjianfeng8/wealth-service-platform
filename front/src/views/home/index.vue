<template>
  <div class="home">
    <!-- Hero -->
    <section class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">智慧投资 · 稳健增值</h1>
        <p class="hero-desc">专业理财服务平台，为您提供全方位的投资理财解决方案</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="router.push('/products')">了解产品</el-button>
          <el-button size="large" @click="router.push('/auth/login')" v-if="!userStore.isLoggedIn">立即登录</el-button>
          <el-button size="large" @click="router.push('/user/dashboard')" v-else>进入个人中心</el-button>
        </div>
      </div>
      <div class="hero-visual">
        <div class="hero-chart">
          <svg viewBox="0 0 400 240" width="400" height="240">
            <path d="M0 200 Q50 180 100 160 Q150 140 200 100 Q250 60 300 80 Q350 100 400 60" stroke="#1a6dff" stroke-width="3" fill="none" class="chart-line" />
            <path d="M0 200 Q50 180 100 160 Q150 140 200 100 Q250 60 300 80 Q350 100 400 60 L400 240 L0 240 Z" fill="url(#grad)" opacity="0.15" class="chart-area" />
            <defs>
              <linearGradient id="grad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#1a6dff" />
                <stop offset="100%" stop-color="#1a6dff" stop-opacity="0" />
              </linearGradient>
            </defs>
          </svg>
        </div>
      </div>
    </section>

    <!-- 平台特色 -->
    <section class="features-section">
      <h2 class="section-title">为什么选择我们</h2>
      <div class="features-grid">
        <div class="feature-card" v-for="f in features" :key="f.title">
          <div class="feature-icon" :style="{ background: f.bg }">
            <el-icon :size="32" color="#fff"><component :is="f.icon" /></el-icon>
          </div>
          <h3 class="feature-title">{{ f.title }}</h3>
          <p class="feature-desc">{{ f.desc }}</p>
        </div>
      </div>
    </section>

    <!-- 行情简报 -->
    <section class="market-section">
      <div class="section-header">
        <h2 class="section-title">实时行情</h2>
        <el-button text type="primary" @click="router.push('/market')">查看更多</el-button>
      </div>
      <el-row :gutter="16">
        <el-col :xs="12" :sm="6" v-for="item in marketItems" :key="item.name">
          <el-card shadow="never" class="market-card">
            <div class="market-name">{{ item.name }}</div>
            <div class="market-price">{{ item.price }}</div>
            <div class="market-change" :class="item.change >= 0 ? 'rise' : 'fall'">
              {{ item.change >= 0 ? '+' : '' }}{{ item.change }}%
            </div>
          </el-card>
        </el-col>
      </el-row>
    </section>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/index'
import { TrendCharts, Coin, DataLine, Star } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const features = [
  {
    title: '丰富产品',
    desc: '覆盖基金、理财、债券等多种投资产品，满足不同风险偏好',
    icon: Coin,
    bg: 'linear-gradient(135deg, #1a6dff, #0a4dcc)',
  },
  {
    title: '实时行情',
    desc: '毫秒级行情推送，掌握市场动态，把握投资时机',
    icon: TrendCharts,
    bg: 'linear-gradient(135deg, #34c759, #28a745)',
  },
  {
    title: '数据分析',
    desc: '专业的走势图表和数据分析工具，辅助投资决策',
    icon: DataLine,
    bg: 'linear-gradient(135deg, #ff9500, #e68a00)',
  },
  {
    title: '智能推荐',
    desc: '基于您的风险偏好和投资习惯，智能推荐合适产品',
    icon: Star,
    bg: 'linear-gradient(135deg, #8e44ad, #6c3483)',
  },
]

const marketItems = [
  { name: '沪深300', price: '3,892.45', change: 1.28 },
  { name: '上证指数', price: '3,156.78', change: 0.86 },
  { name: '创业板指', price: '2,234.56', change: -0.42 },
  { name: '科创50', price: '1,567.89', change: 2.15 },
]
</script>

<style scoped>
.home {
  max-width: 1200px;
}

/* Hero */
.hero-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 48px 0;
  gap: 40px;
}

.hero-content {
  flex: 1;
}

.hero-title {
  font-size: 36px;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 16px;
  line-height: 1.3;
}

.hero-desc {
  font-size: 16px;
  color: var(--text-secondary);
  margin-bottom: 32px;
  line-height: 1.6;
}

.hero-actions {
  display: flex;
  gap: 12px;
}

.hero-visual {
  flex-shrink: 0;
}

.hero-chart svg {
  display: block;
}

.chart-line {
  stroke-dasharray: 600;
  stroke-dashoffset: 600;
  animation: drawLine 2s ease forwards;
}

.chart-area {
  animation: fadeIn 1.5s ease 0.5s forwards;
  opacity: 0;
}

@keyframes drawLine {
  to { stroke-dashoffset: 0; }
}

@keyframes fadeIn {
  to { opacity: 1; }
}

/* Features */
.features-section {
  padding: 48px 0;
}

.section-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 32px;
  text-align: center;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}

.feature-card {
  padding: 32px 24px;
  border-radius: var(--radius);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  transition: var(--transition);
  text-align: center;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}

.feature-icon {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  margin: 0 auto 16px;
}

.feature-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.feature-desc {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
}

/* Market */
.market-section {
  padding: 48px 0;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.section-header .section-title {
  margin-bottom: 0;
}

.market-card {
  text-align: center;
  cursor: default;
  margin-bottom: 16px;
}

.market-card:hover {
  transform: translateY(-2px);
}

.market-name {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.market-price {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 6px;
  font-family: 'DIN Pro', monospace;
}

.market-change {
  font-size: 14px;
  font-weight: 600;
}

.market-change.rise { color: var(--rise-color); }
.market-change.fall { color: var(--fall-color); }

@media (max-width: 768px) {
  .hero-visual {
    display: none;
  }
  .hero-title {
    font-size: 28px;
  }
  .features-grid {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
