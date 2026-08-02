<template>
  <div class="dashboard">
    <el-result v-if="hasError" icon="error" title="加载失败" sub-title="数据获取异常，请重试">
      <template #extra>
        <el-button type="primary" @click="initFetch">重试</el-button>
      </template>
    </el-result>

    <template v-else>
      <!-- 欢迎横幅 -->
      <div class="welcome-banner">
        <div class="welcome-text">
          <h2>欢迎回来，{{ displayName }}</h2>
          <p v-if="unreadCount > 0">你有 <strong>{{ unreadCount }}</strong> 条未读消息</p>
          <p v-else>今日市场稳健运行，祝你投资顺利</p>
        </div>
      </div>

      <!-- 我的概览 -->
      <el-row :gutter="20" class="section-overview">
        <el-col :xs="24" :sm="8" v-for="(item, idx) in overviewItems" :key="idx">
          <el-card shadow="never" class="overview-card" @click="router.push(item.path)">
            <div class="overview-inner">
              <div class="overview-icon" :style="{ background: item.bg }">
                <el-icon :size="22" color="#fff">
                  <component :is="item.icon" />
                </el-icon>
              </div>
              <div class="overview-info">
                <el-skeleton :loading="overviewLoading" :rows="1" animated>
                  <template #template>
                    <div class="overview-value-skeleton">
                      <el-skeleton-item variant="text" style="width: 40px; height: 28px;" />
                    </div>
                  </template>
                  <template #default>
                    <span class="overview-value">{{ loading ? '—' : item.value }}</span>
                  </template>
                </el-skeleton>
                <span class="overview-label">{{ item.label }}</span>
              </div>
              <el-icon class="overview-arrow"><ArrowRight /></el-icon>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 我的自选 (仅普通用户) -->
      <template v-if="!userStore.isAdmin">
        <el-card shadow="never" class="section-card" v-if="favoriteItems.length > 0">
          <template #header>
            <div class="card-header">
              <span><el-icon :size="16"><Star /></el-icon> 我的自选</span>
              <router-link to="/user/favorite" class="section-more">管理自选 →</router-link>
            </div>
          </template>
          <el-skeleton :loading="favMarketLoading" animated>
            <template #template>
              <el-row :gutter="16">
                <el-col :span="12" v-for="i in 2" :key="i">
                  <el-card shadow="never">
                    <div style="display:flex;justify-content:space-between;margin-bottom:8px;">
                      <el-skeleton-item variant="text" style="width:80px;height:15px;" />
                      <el-skeleton-item variant="text" style="width:50px;height:15px;" />
                    </div>
                    <el-skeleton-item variant="text" style="width:60px;height:24px;margin-bottom:8px;" />
                    <el-skeleton-item variant="text" style="width:100%;height:6px;border-radius:3px;" />
                  </el-card>
                </el-col>
              </el-row>
            </template>
          </el-skeleton>
          <template v-if="!favMarketLoading">
            <el-row :gutter="16">
              <el-col :xs="24" :sm="12" v-for="item in favoriteItems" :key="item.productCode">
                <div
                  class="fav-card"
                  :class="(item.riseFallRate || 0) >= 0 ? 'up' : 'down'"
                  @click="router.push('/products')"
                >
                  <div class="fav-card-top">
                    <div class="fav-card-left">
                      <span class="fav-card-name">{{ item.productName || item.productCode }}</span>
                      <span class="fav-card-code">{{ item.productCode }}</span>
                    </div>
                    <div class="fav-card-rate" :class="(item.riseFallRate || 0) >= 0 ? 'rise' : 'fall'">
                      <el-icon :size="14">
                        <CaretTop v-if="(item.riseFallRate || 0) >= 0" />
                        <CaretBottom v-else />
                      </el-icon>
                      {{ formatRate(item.riseFallRate) }}
                    </div>
                  </div>
                  <div class="fav-card-price">{{ formatPrice(item.currentPrice) }}</div>
                  <div class="fav-bar-wrap">
                    <div
                      class="fav-bar"
                      :class="(item.riseFallRate || 0) >= 0 ? 'up' : 'down'"
                      :style="{ width: maxFavPrice > 0 ? (item.currentPrice / maxFavPrice * 100) + '%' : '0%' }"
                    />
                  </div>
                </div>
              </el-col>
            </el-row>
          </template>
        </el-card>

        <el-card v-else-if="!overviewLoading" shadow="never" class="section-card">
          <template #header>
            <div class="card-header">
              <span><el-icon :size="16"><Star /></el-icon> 我的自选</span>
            </div>
          </template>
          <el-empty description="还没有添加自选产品" :image-size="64">
            <el-button type="primary" size="small" @click="router.push('/products')">去产品中心添加</el-button>
          </el-empty>
        </el-card>
      </template>

      <!-- 热门产品推荐 -->
      <el-card shadow="never" class="section-card">
        <template #header>
          <div class="card-header">
            <span><el-icon :size="16"><Coin /></el-icon> 热门产品推荐</span>
            <router-link to="/products" class="section-more">查看更多 →</router-link>
          </div>
        </template>

        <template v-if="prodLoading">
          <el-skeleton animated :rows="2">
            <template #template>
              <el-row :gutter="16">
                <el-col :span="8" v-for="i in 3" :key="i">
                  <el-card shadow="never">
                    <el-skeleton-item variant="text" style="width: 60%; height: 14px; margin-bottom: 8px;" />
                    <el-skeleton-item variant="text" style="width: 40%; height: 22px; margin-bottom: 4px;" />
                    <el-skeleton-item variant="text" style="width: 30%; height: 12px;" />
                  </el-card>
                </el-col>
              </el-row>
            </template>
          </el-skeleton>
        </template>

        <el-empty v-else-if="productList.length === 0" description="暂无产品" :image-size="64" />

        <el-row :gutter="16" v-else>
          <el-col :xs="24" :sm="12" :md="8" v-for="item in productList" :key="item.id">
            <el-card shadow="never" class="prod-card" @click="showProductDetail(item)">
              <div class="prod-tag" :class="'t-' + (item.productType || 1)">{{ productTypeLabel(item.productType) }}</div>
              <div class="prod-name">{{ item.productName }}</div>
              <div class="prod-code">{{ item.productCode }}</div>
              <div class="prod-price-row">
                <span class="prod-price">{{ formatPrice(item.price) }}</span>
                <span class="prod-change" :class="(item.riseFallRate || 0) >= 0 ? 'rise' : 'fall'">
                  {{ formatRate(item.riseFallRate) }}
                </span>
              </div>
              <el-tag :type="item.status === 1 ? 'success' : 'info'" size="small" effect="plain">
                {{ item.status === 1 ? '在售' : '已下架' }}
              </el-tag>
            </el-card>
          </el-col>
        </el-row>
      </el-card>

      <!-- 最新资讯 -->
      <el-card shadow="never" class="section-card">
        <template #header>
          <div class="card-header">
            <span><el-icon :size="16"><Reading /></el-icon> 最新资讯</span>
            <router-link to="/news" class="section-more">查看更多 →</router-link>
          </div>
        </template>

        <template v-if="newsLoading">
          <el-skeleton animated :rows="4">
            <template #template>
              <div v-for="i in 4" :key="i" class="news-skeleton-row">
                <el-skeleton-item variant="text" style="width: 20%; height: 14px; margin-right: 8px;" />
                <el-skeleton-item variant="text" style="width: 60%; height: 14px;" />
              </div>
            </template>
          </el-skeleton>
        </template>

        <el-empty v-else-if="newsList.length === 0" description="暂无资讯" :image-size="64" />

        <div v-else class="news-list">
          <div class="news-item" v-for="item in newsList" :key="item.id" @click="showNewsDetail(item)">
            <span class="news-tag" :class="'nt-' + (item.newsType || 1)">{{ newsTypeLabel(item.newsType) }}</span>
            <span class="news-title">{{ item.title }}</span>
            <span class="news-date">{{ formatTime(item.publishTime) }}</span>
          </div>
        </div>
      </el-card>
    </template>

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
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store'
import { useMarketSSEStore } from '@/store/marketSSE'
import { Star, Money, Message, ArrowRight, Coin, Reading, CaretTop, CaretBottom } from '@element-plus/icons-vue'
import { getProductPage } from '@/api/product'
import { getFavoritePage } from '@/api/favorite'
import { getTradeOrderPage } from '@/api/trade'
import { getMessagePage } from '@/api/message'
import { getNewsPage } from '@/api/message'
import { formatPrice, formatRate } from '@/utils/format'
import type { WeaProduct, WeaNews, WeaMarketData } from '@/types'
import ProductDetailDialog from '@/components/ProductDetailDialog.vue'
import NewsDetailDialog from '@/components/NewsDetailDialog.vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const hasError = ref(false)

/* ---- 概览数据 ---- */
const overviewLoading = ref(true)
const favoriteCount = ref(0)
const orderCount = ref(0)
const unreadCount = ref(0)

/* ---- 自选行情 ---- */
const favMarketLoading = ref(false)
const favoriteItems = ref<(WeaMarketData & { productName?: string })[]>([])
const marketSSE = useMarketSSEStore()

/* ---- 产品 ---- */
const prodLoading = ref(false)
const productList = ref<WeaProduct[]>([])

/* ---- 资讯 ---- */
const newsLoading = ref(false)
const newsList = ref<WeaNews[]>([])

/* ---- 详情弹窗 ---- */
const productDetailVisible = ref(false)
const selectedProductId = ref<number | null>(null)
const selectedProductName = ref('')
const newsDetailVisible = ref(false)
const selectedNewsId = ref<number | null>(null)

function showProductDetail(item: WeaProduct) {
  selectedProductName.value = item.productName
  selectedProductId.value = item.id ?? null
  productDetailVisible.value = true
}

function showNewsDetail(item: WeaNews) {
  selectedNewsId.value = item.id ?? null
  newsDetailVisible.value = true
}

const displayName = computed(() => userStore.nickname || userStore.username || '用户')

const overviewItems = computed(() => [
  { label: '我的自选', value: favoriteCount.value, icon: Star, path: '/user/favorite', bg: 'linear-gradient(135deg, #1a6dff, #0a4dcc)' },
  { label: '委托中', value: orderCount.value, icon: Money, path: '/user/trade', bg: 'linear-gradient(135deg, #ff9500, #e68a00)' },
  { label: '未读消息', value: unreadCount.value, icon: Message, path: '/user/message', bg: 'linear-gradient(135deg, #34c759, #28a745)' },
])

const maxFavPrice = computed(() => Math.max(...favoriteItems.value.map(i => i.currentPrice || 0), 0))

const PRODUCT_TYPE_LABELS: Record<number, string> = { 1: '黄金', 2: '白银', 3: '理财' }
function productTypeLabel(v?: number) { return v ? PRODUCT_TYPE_LABELS[v] || '-' : '-' }

const NEWS_TYPE_LABELS: Record<number, string> = { 1: '行情快讯', 2: '行业公告', 3: '理财资讯' }
function newsTypeLabel(v?: number) { return v ? NEWS_TYPE_LABELS[v] || '资讯' : '资讯' }

function formatTime(t?: string) {
  if (!t) return ''
  const d = new Date(t)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

async function fetchOverview() {
  if (userStore.isAdmin) {
    favoriteCount.value = 0
    orderCount.value = 0
    unreadCount.value = 0
    overviewLoading.value = false
    return
  }
  try {
    const [fr, tr, mr] = await Promise.all([
      getFavoritePage({ pageNum: 1, pageSize: 1, userId: userStore.userId }),
      getTradeOrderPage({ pageNum: 1, pageSize: 1, userId: userStore.userId }),
      getMessagePage({ pageNum: 1, pageSize: 1, userId: userStore.userId, readFlag: 0 }),
    ])
    favoriteCount.value = fr.data?.total || 0
    orderCount.value = tr.data?.total || 0
    unreadCount.value = mr.data?.total || 0
  } catch {
    // 静默失败，保持 0
  } finally {
    overviewLoading.value = false
  }
}

async function fetchFavoriteMarket() {
  if (userStore.isAdmin) return
  favMarketLoading.value = true
  try {
    const favRes = await getFavoritePage({ pageNum: 1, pageSize: 4, userId: userStore.userId })
    const favList = (favRes.data?.records || []) as { productCode: string }[]
    if (favList.length === 0) {
      favoriteItems.value = []
      return
    }

    const codes = [...new Set(favList.map(f => f.productCode))]
    const productMap = new Map<string, { name: string; price: number; riseFallRate: number }>()

    await Promise.all(codes.map(async (code) => {
      try {
        const res = await getProductPage({ pageNum: 1, pageSize: 1, productCode: code })
        const p = (res.data?.records || [])[0] as WeaProduct | undefined
        if (p) {
          productMap.set(code, {
            name: p.productName || code,
            price: p.price,
            riseFallRate: p.riseFallRate || 0,
          })
        }
      } catch { /* 单个失败不影响其他 */ }
    }))

    favoriteItems.value = codes.map(code => {
      const prod = productMap.get(code)
      return {
        productCode: code,
        productName: prod?.name || code,
        currentPrice: prod?.price ?? 0,
        riseFallRate: prod?.riseFallRate ?? 0,
      } as WeaMarketData & { productName: string }
    })
  } catch {
    favoriteItems.value = []
  } finally {
    favMarketLoading.value = false
  }
}

function handleMarketUpdate(data: WeaMarketData[]) {
  const dataMap = new Map(data.map(d => [d.productCode, d]))
  favoriteItems.value = favoriteItems.value.map(item => {
    const mkt = dataMap.get(item.productCode)
    return mkt
      ? { ...item, currentPrice: mkt.currentPrice, riseFallRate: mkt.riseFallRate }
      : item
  })
}

async function fetchProducts() {
  prodLoading.value = true
  try {
    const res = await getProductPage({ pageNum: 1, pageSize: 6, orderBy: 'sort', orderDir: 'asc' })
    productList.value = res.data?.records || []
  } catch {
    productList.value = []
  } finally {
    prodLoading.value = false
  }
}

async function fetchNews() {
  newsLoading.value = true
  try {
    const res = await getNewsPage({ pageNum: 1, pageSize: 4 })
    newsList.value = res.data?.records || []
  } catch {
    newsList.value = []
  } finally {
    newsLoading.value = false
  }
}

async function initFetch() {
  hasError.value = false
  loading.value = true
  try {
    await Promise.all([
      fetchOverview(),
      fetchFavoriteMarket(),
      fetchProducts(),
      fetchNews(),
    ])
  } catch {
    hasError.value = true
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  initFetch()
  if (userStore.userId && !userStore.isAdmin) {
    marketSSE.subscribe(handleMarketUpdate)
  }
})

onUnmounted(() => {
  marketSSE.unsubscribe(handleMarketUpdate)
})
</script>

<style scoped>
.dashboard { max-width: 1200px; }

/* ============ 欢迎横幅 ============ */
.welcome-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28px 0 24px;
}

.welcome-text h2 {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 4px;
}

.welcome-text p {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}

.welcome-text p strong { color: var(--el-color-primary); }




/* ============ Section 通用 ============ */
.section-card { margin-bottom: 20px; }

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-header span {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  font-weight: 600;
}

.section-more {
  font-size: 13px;
  color: var(--el-color-primary);
  text-decoration: none;
}

.section-more:hover { text-decoration: underline; }

/* ============ 概览卡片 ============ */
.section-overview { margin-bottom: 20px; }

.overview-card {
  margin-bottom: 16px;
  cursor: pointer;
  transition: all 0.2s;
}

.overview-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.overview-inner {
  display: flex;
  align-items: center;
  gap: 16px;
}

.overview-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  flex-shrink: 0;
}

.overview-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.overview-value {
  font-size: 26px;
  font-weight: 700;
  color: var(--text-primary);
  font-family: 'DIN Pro', monospace;
  line-height: 1.2;
}

.overview-value-skeleton { padding: 2px 0; }

.overview-label {
  font-size: 13px;
  color: var(--text-secondary);
}

.overview-arrow {
  color: #c9cdd4;
  font-size: 16px;
}

/* ============ 自选行情 ============ */
.fav-card {
  background: #fff;
  border: 1px solid var(--border-color);
  border-radius: 10px;
  padding: 16px;
  margin-bottom: 16px;
  cursor: pointer;
  transition: all 0.2s;
  border-left: 4px solid transparent;
}

.fav-card.up { border-left-color: var(--rise-color, #34c759); }
.fav-card.down { border-left-color: var(--fall-color, #ff3b30); }

.fav-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.fav-card-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 8px;
}

.fav-card-left {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.fav-card-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.fav-card-code {
  font-size: 12px;
  color: var(--text-secondary);
}

.fav-card-rate {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 14px;
  font-weight: 600;
  padding: 2px 10px;
  border-radius: 20px;
  white-space: nowrap;
}

.fav-card-rate.rise {
  color: #fff;
  background: var(--rise-color, #34c759);
}

.fav-card-rate.fall {
  color: #fff;
  background: var(--fall-color, #ff3b30);
}

.fav-card-price {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  font-family: 'DIN Pro', 'Courier New', monospace;
  margin-bottom: 12px;
}

.fav-bar-wrap {
  height: 6px;
  background: var(--el-fill-color-lighter);
  border-radius: 3px;
  overflow: hidden;
}

.fav-bar {
  height: 100%;
  border-radius: 3px;
  transition: width 0.4s ease;
}

.fav-bar.up { background: linear-gradient(90deg, #34c759, #68d88b); }
.fav-bar.down { background: linear-gradient(90deg, #ff3b30, #ff6b62); }

/* ============ 产品卡片 ============ */
.prod-card {
  margin-bottom: 16px;
  cursor: pointer;
  transition: all 0.2s;
}

.prod-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.prod-tag {
  display: inline-block;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 4px;
  margin-bottom: 8px;
}

.prod-tag.t-1 { background: #fff7e6; color: #d48806; }
.prod-tag.t-2 { background: #f0f5ff; color: #597ef7; }
.prod-tag.t-3 { background: #f6ffed; color: #52c41a; }

.prod-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 2px;
}

.prod-code {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 10px;
}

.prod-price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 6px;
}

.prod-price {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  font-family: 'DIN Pro', monospace;
}

.prod-change { font-size: 13px; font-weight: 600; }
.prod-change.rise { color: var(--rise-color); }
.prod-change.fall { color: var(--fall-color); }

/* ============ 资讯 ============ */
.news-list {
  border: 1px solid var(--border-color);
  border-radius: 8px;
  overflow: hidden;
}

.news-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.15s;
}

.news-item:hover { background: var(--el-fill-color-lighter); }
.news-item + .news-item { border-top: 1px solid #f5f5f5; }

.news-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 600;
  white-space: nowrap;
  flex-shrink: 0;
}

.news-tag.nt-1 { background: #fff7e6; color: #d48806; }
.news-tag.nt-2 { background: #f0f5ff; color: #597ef7; }
.news-tag.nt-3 { background: #f6ffed; color: #52c41a; }

.news-title {
  flex: 1;
  font-size: 14px;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.news-date {
  font-size: 12px;
  color: var(--text-secondary);
  flex-shrink: 0;
}

.news-skeleton-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
}

@media (max-width: 768px) {
  .welcome-banner { flex-direction: column; align-items: flex-start; gap: 12px; }
}
</style>
