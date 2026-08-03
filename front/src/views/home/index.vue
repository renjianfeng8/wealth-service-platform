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
          <el-button size="large" @click="goProfile" v-else>进入个人中心</el-button>
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

    <!-- 信任数据 -->
    <section class="trust-section">
      <div class="trust-item" v-for="s in trustStats" :key="s.label">
        <span class="trust-value">{{ s.value }}<span class="trust-suffix">{{ s.suffix }}</span></span>
        <span class="trust-label">{{ s.label }}</span>
      </div>
    </section>

    <!-- 快捷入口 -->
    <section class="quick-entry-section">
      <router-link
        v-for="entry in quickEntries"
        :key="entry.label"
        :to="entry.path"
        class="quick-entry-item"
      >
        <div class="quick-entry-icon" :style="{ background: entry.bg }">
          <el-icon :size="24" color="#fff"><component :is="entry.icon" /></el-icon>
        </div>
        <span class="quick-entry-label">{{ entry.label }}</span>
      </router-link>
    </section>

    <!-- 热门产品 -->
    <section class="products-section">
      <div class="section-header">
        <h2 class="section-title">热门产品</h2>
        <router-link to="/products" class="section-more">查看更多 →</router-link>
      </div>

      <el-skeleton animated :loading="prodLoading" v-if="prodLoading">
        <el-row :gutter="16">
          <el-col :span="8" v-for="i in 3" :key="i">
            <el-card shadow="never">
              <el-skeleton-item variant="text" style="width: 60%; height: 16px; margin-bottom: 12px;" />
              <el-skeleton-item variant="text" style="width: 40%; height: 24px; margin-bottom: 8px;" />
              <el-skeleton-item variant="text" style="width: 30%; height: 14px;" />
            </el-card>
          </el-col>
        </el-row>
      </el-skeleton>

      <el-empty v-else-if="productList.length === 0" description="暂无产品数据" />

      <el-row :gutter="16" v-else>
        <el-col :xs="24" :sm="12" :md="8" v-for="item in productList" :key="item.id" class="product-col">
          <el-card shadow="never" class="product-card" @click="showProductDetail(item)">
            <div class="product-tag" :class="'type-' + (item.productType || 1)">{{ productTypeLabel(item.productType) }}</div>
            <div class="product-name">{{ item.productName }}</div>
            <div class="product-code">{{ item.productCode }}</div>
            <div class="product-price-row">
              <span class="product-price">{{ formatPrice(item.price) }}</span>
              <span class="product-change" :class="(item.riseFallRate || 0) >= 0 ? 'rise' : 'fall'">
                {{ formatRate(item.riseFallRate) }}
              </span>
            </div>
            <el-tag :type="item.status === 1 ? 'success' : 'info'" size="small" effect="plain">
              {{ item.status === 1 ? '在售' : '已下架' }}
            </el-tag>
          </el-card>
        </el-col>
      </el-row>
    </section>

    <!-- 行情简报 -->
    <section class="market-section">
      <div class="section-header">
        <h2 class="section-title">实时行情</h2>
        <router-link to="/market" class="section-more">查看更多 →</router-link>
      </div>

      <el-row :gutter="16" v-if="marketLoading">
        <el-col :xs="12" :sm="6" v-for="i in 4" :key="i">
          <el-card shadow="never" class="market-card">
            <el-skeleton animated :loading="true">
              <template #template>
                <div class="skeleton-market">
                  <el-skeleton-item variant="text" style="width: 40%; height: 14px; margin: 0 auto 12px;" />
                  <el-skeleton-item variant="text" style="width: 65%; height: 24px; margin: 0 auto 8px;" />
                  <el-skeleton-item variant="text" style="width: 45%; height: 16px; margin: 0 auto;" />
                </div>
              </template>
            </el-skeleton>
          </el-card>
        </el-col>
      </el-row>

      <el-empty v-else-if="marketList.length === 0" description="暂无行情数据" />

      <el-row :gutter="16" v-else>
        <el-col :xs="12" :sm="6" v-for="item in marketList" :key="item.productCode">
          <el-card shadow="never" class="market-card">
            <div class="market-name">{{ item.productCode }}</div>
            <div class="market-price">{{ formatPrice(item.currentPrice) }}</div>
            <div class="market-change" :class="(item.riseFallRate || 0) >= 0 ? 'rise' : 'fall'">
              {{ formatRate(item.riseFallRate) }}
            </div>
          </el-card>
        </el-col>
      </el-row>
    </section>

    <!-- 最新资讯 -->
    <section class="news-section">
      <div class="section-header">
        <h2 class="section-title">最新资讯</h2>
        <router-link to="/news" class="section-more">查看更多 →</router-link>
      </div>

      <el-skeleton animated :loading="newsLoading" v-if="newsLoading">
        <div v-for="i in 4" :key="i" style="padding: 12px 0; border-bottom: 1px solid #f0f0f0;">
          <el-skeleton-item variant="text" style="width: 70%; height: 16px; margin-bottom: 8px;" />
          <el-skeleton-item variant="text" style="width: 30%; height: 12px;" />
        </div>
      </el-skeleton>

      <el-empty v-else-if="newsList.length === 0" description="暂无资讯" />

      <div v-else class="news-list">
        <div class="news-item" v-for="item in newsList" :key="item.id" @click="showNewsDetail(item)">
          <div class="news-left">
            <span class="news-type-tag" :class="'nt-' + (item.newsType || 1)">{{ newsTypeLabel(item.newsType) }}</span>
          </div>
          <div class="news-center">
            <div class="news-title">{{ item.title }}</div>
            <div class="news-meta">
              <span v-if="item.source">{{ item.source }}</span>
              <span v-if="item.source && item.publishTime"> · </span>
              <span v-if="item.publishTime">{{ formatTime(item.publishTime) }}</span>
            </div>
          </div>
          <el-icon class="news-arrow"><ArrowRight /></el-icon>
        </div>
      </div>
    </section>

    <!-- 登录引导 -->
    <section class="cta-section" v-if="!userStore.isLoggedIn">
      <div class="cta-content">
        <h3>开启您的投资之旅</h3>
        <p>注册即享个性化产品推荐、实时行情提醒、资产分析等专业服务</p>
        <el-button type="primary" size="large" @click="router.push('/auth/login')">立即登录 / 注册</el-button>
      </div>
    </section>

    <ProductDetailDialog
      v-model="productDetailVisible"
      :product-id="selectedProductId"
      :fallback-name="selectedProductName"
    />
    <NewsDetailDialog
      v-model="newsDetailVisible"
      :news-id="selectedNewsId"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/index'
import { Coin, TrendCharts, Reading, Star, ArrowRight } from '@element-plus/icons-vue'
import { getProductPage, getMarketDataPage } from '@/api/product'
import { getNewsPage } from '@/api/message'
import { formatPrice, formatRate } from '@/utils/format'
import type { WeaProduct, WeaMarketData, WeaNews } from '@/types'
import ProductDetailDialog from '@/components/ProductDetailDialog.vue'
import NewsDetailDialog from '@/components/NewsDetailDialog.vue'

const router = useRouter()
const userStore = useUserStore()

/* ---- 信任数据 ---- */
const productTotalCount = ref(0)
const trustStats = [
  { label: '产品数量', value: productTotalCount, suffix: '+' },
  { label: '服务用户', value: '10', suffix: '万+' },
  { label: '覆盖市场', value: '2', suffix: '个' },
]

/* ---- 快捷入口 ---- */
const quickEntries = [
  { label: '产品中心', path: '/products', icon: Coin, bg: 'linear-gradient(135deg, #1a6dff, #0a4dcc)' },
  { label: '实时行情', path: '/market', icon: TrendCharts, bg: 'linear-gradient(135deg, #34c759, #28a745)' },
  { label: '财经资讯', path: '/news', icon: Reading, bg: 'linear-gradient(135deg, #ff9500, #e68a00)' },
  { label: '我的自选', path: userStore.isLoggedIn ? '/user/favorite' : '/auth/login', icon: Star, bg: 'linear-gradient(135deg, #8e44ad, #6c3483)' },
]

/* ---- 产品 ---- */
const prodLoading = ref(false)
const productList = ref<WeaProduct[]>([])

const PRODUCT_TYPE_LABELS: Record<number, string> = { 1: '黄金', 2: '白银', 3: '理财' }
function productTypeLabel(v?: number) { return v ? PRODUCT_TYPE_LABELS[v] || '-' : '-' }

/* ---- 行情 ---- */
const marketLoading = ref(false)
const marketList = ref<WeaMarketData[]>([])

/* ---- 资讯 ---- */
const newsLoading = ref(false)
const newsList = ref<WeaNews[]>([])

const NEWS_TYPE_LABELS: Record<number, string> = { 1: '行情快讯', 2: '行业公告', 3: '理财资讯' }
function newsTypeLabel(v?: number) { return v ? NEWS_TYPE_LABELS[v] || '资讯' : '资讯' }

/* ---- 详情弹窗 ---- */
const productDetailVisible = ref(false)
const selectedProductId = ref<number | string | null>(null)
const selectedProductName = ref('')
const newsDetailVisible = ref(false)
const selectedNewsId = ref<number | string | null>(null)

function showProductDetail(item: WeaProduct) {
  selectedProductName.value = item.productName
  selectedProductId.value = item.id ?? null
  productDetailVisible.value = true
}

function showNewsDetail(item: WeaNews) {
  selectedNewsId.value = item.id ?? null
  newsDetailVisible.value = true
}

function formatTime(t?: string) {
  if (!t) return ''
  const d = new Date(t)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

async function fetchProducts() {
  prodLoading.value = true
  try {
    const res = await getProductPage({ pageNum: 1, pageSize: 6, orderBy: 'sort', orderDir: 'asc' })
    productList.value = res?.records || []
    productTotalCount.value = res?.total || 0
    trustStats[0].value = productTotalCount
  } catch {
    productList.value = []
  } finally {
    prodLoading.value = false
  }
}

async function fetchMarketData() {
  marketLoading.value = true
  try {
    const res = await getMarketDataPage({ pageNum: 1, pageSize: 4 })
    marketList.value = (res?.records || []) as WeaMarketData[]
  } catch {
    marketList.value = []
  } finally {
    marketLoading.value = false
  }
}

async function fetchNews() {
  newsLoading.value = true
  try {
    const res = await getNewsPage({ pageNum: 1, pageSize: 5 })
    newsList.value = res?.records || []
  } catch {
    newsList.value = []
  } finally {
    newsLoading.value = false
  }
}

function goProfile() {
  router.push('/user/profile')
}

onMounted(() => {
  Promise.all([fetchProducts(), fetchMarketData(), fetchNews()])
})
</script>

<style scoped>
.home { max-width: 1200px; }

/* ============ Hero ============ */
.hero-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 48px 0 32px;
  gap: 40px;
}

.hero-content { flex: 1; }

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

.hero-actions { display: flex; gap: 12px; }

.hero-visual { flex-shrink: 0; }

.hero-chart svg { display: block; }

.chart-line {
  stroke-dasharray: 600;
  stroke-dashoffset: 600;
  animation: drawLine 2s ease forwards;
}

.chart-area {
  animation: fadeIn 1.5s ease 0.5s forwards;
  opacity: 0;
}

@keyframes drawLine { to { stroke-dashoffset: 0; } }
@keyframes fadeIn { to { opacity: 1; } }

/* ============ 信任数据 ============ */
.trust-section {
  display: flex;
  justify-content: center;
  gap: 64px;
  padding: 28px 0;
  background: var(--el-fill-color-lighter);
  border-radius: 12px;
  margin-bottom: 32px;
}

.trust-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.trust-value {
  font-size: 28px;
  font-weight: 800;
  color: var(--el-color-primary);
  font-family: 'DIN Pro', monospace;
}

.trust-suffix {
  font-size: 18px;
  font-weight: 600;
}

.trust-label {
  font-size: 14px;
  color: var(--text-secondary);
}

/* ============ 快捷入口 ============ */
.quick-entry-section {
  display: flex;
  gap: 16px;
  margin-bottom: 48px;
}

.quick-entry-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px;
  border-radius: 12px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  cursor: pointer;
  transition: all 0.2s;
  text-decoration: none;
}

.quick-entry-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.quick-entry-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  flex-shrink: 0;
}

.quick-entry-label {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

/* ============ 通用 Section ============ */
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.section-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.section-more {
  font-size: 14px;
  color: var(--el-color-primary);
  text-decoration: none;
}

.section-more:hover { text-decoration: underline; }

.products-section,
.market-section,
.news-section {
  padding: 40px 0;
  border-top: 1px solid var(--border-color);
}

/* ============ 热门产品 ============ */
.product-col { margin-bottom: 16px; }

.product-card {
  cursor: pointer;
  transition: all 0.2s;
}

.product-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.product-tag {
  display: inline-block;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 4px;
  margin-bottom: 10px;
}

.product-tag.type-1 { background: #fff7e6; color: #d48806; }
.product-tag.type-2 { background: #f0f5ff; color: #597ef7; }
.product-tag.type-3 { background: #f6ffed; color: #52c41a; }

.product-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.product-code {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 12px;
}

.product-price-row {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 8px;
}

.product-price {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  font-family: 'DIN Pro', monospace;
}

.product-change { font-size: 14px; font-weight: 600; }
.product-change.rise { color: var(--rise-color); }
.product-change.fall { color: var(--fall-color); }

/* ============ 行情简报 ============ */
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

.market-change { font-size: 14px; font-weight: 600; }
.market-change.rise { color: var(--rise-color); }
.market-change.fall { color: var(--fall-color); }

/* Skeleton 行情 */
.skeleton-market { text-align: center; padding: 12px 0; }

/* ============ 最新资讯 ============ */
.news-list {
  border: 1px solid var(--border-color);
  border-radius: 8px;
  overflow: hidden;
}

.news-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  cursor: pointer;
  transition: background 0.15s;
}

.news-item:hover { background: var(--el-fill-color-lighter); }
.news-item + .news-item { border-top: 1px solid #f5f5f5; }

.news-left { flex-shrink: 0; }

.news-type-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 600;
  white-space: nowrap;
}

.news-type-tag.nt-1 { background: #fff7e6; color: #d48806; }
.news-type-tag.nt-2 { background: #f0f5ff; color: #597ef7; }
.news-type-tag.nt-3 { background: #f6ffed; color: #52c41a; }

.news-center {
  flex: 1;
  min-width: 0;
}

.news-title {
  font-size: 14px;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 2px;
}

.news-meta {
  font-size: 12px;
  color: var(--text-secondary);
}

.news-arrow {
  flex-shrink: 0;
  color: #c9cdd4;
  font-size: 14px;
}

/* ============ 登录引导 ============ */
.cta-section {
  margin: 48px 0;
  padding: 48px;
  text-align: center;
  background: linear-gradient(135deg, #f0f5ff, #e6f7ff);
  border-radius: 12px;
}

.cta-content h3 {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 8px;
}

.cta-content p {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0 0 24px;
}

/* ============ 响应式 ============ */
@media (max-width: 768px) {
  .hero-visual { display: none; }
  .hero-title { font-size: 28px; }
  .trust-section { gap: 32px; flex-wrap: wrap; }
  .quick-entry-section { flex-wrap: wrap; }
  .quick-entry-item { min-width: calc(50% - 8px); }
  .news-item { flex-wrap: wrap; }
}
</style>
