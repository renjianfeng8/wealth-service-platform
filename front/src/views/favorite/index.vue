<template>
  <div class="favorite-page">
    <div class="page-title">
      我的自选
      <span class="sse-status" :class="sseConnected ? 'connected' : 'disconnected'">
        <span class="sse-dot"></span>
        {{ sseConnected ? '实时已连接' : '实时已断开' }}
      </span>
    </div>

    <el-card class="add-card" shadow="never">
      <div class="add-bar">
        <el-input
          v-model="newProductCode"
          placeholder="输入产品代码添加自选，如 GOLD001"
          size="large"
          class="add-input"
          clearable
        />
        <el-button type="primary" size="large" :loading="adding" @click="handleAdd">
          添加自选
        </el-button>
      </div>
    </el-card>

    <div v-if="loading" class="loading-wrap">
      <el-skeleton :rows="3" animated />
    </div>
    <div v-else-if="favorites.length === 0" class="empty-wrap">
      <el-empty description="暂无自选，请添加产品">
        <el-button type="primary" @click="router.push('/products')">去产品中心</el-button>
      </el-empty>
    </div>
    <div v-else class="fav-grid">
      <el-row :gutter="20">
        <el-col
          v-for="item in favorites"
          :key="item.id"
          :xs="24"
          :sm="12"
          :lg="8"
          class="fav-col"
        >
          <el-card class="fav-card" shadow="never">
            <div class="fav-header">
              <div class="fav-info">
                <h3 class="fav-name">{{ item.productName || item.productCode }}</h3>
                <span class="fav-code">{{ item.productCode }}</span>
              </div>
              <el-button
                type="danger"
                :icon="Delete"
                circle
                size="small"
                text
                @click="handleDelete(item)"
              />
            </div>
            <div class="fav-price-section">
              <div v-if="item.currentPrice != null" class="fav-price-row">
                <span class="fav-price">{{ formatPrice(item.currentPrice) }}</span>
                <span
                  class="fav-change"
                  :class="(item.riseFallRate || 0) >= 0 ? 'rise' : 'fall'"
                >
                  {{ formatRate(item.riseFallRate) }}
                </span>
              </div>
              <span v-else class="no-price">暂无行情数据</span>
            </div>
            <div class="fav-footer">
              <span class="fav-time">添加于 {{ formatDate(item.createTime) }}</span>
              <div class="fav-actions">
                <el-button size="small" @click="showDetail(item)">详情</el-button>
                <el-button type="primary" size="small" @click="goTrade(item)">交易</el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <ProductDetailDialog
      v-model="detailVisible"
      :product-id="selectedProductId"
      :fallback-name="detailFallbackName"
    />

    <div class="pagination-wrap">
      <el-pagination
        v-if="total > 0"
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[12, 24, 48]"
        layout="total, sizes, prev, pager, next"
        @current-change="fetchFavorites"
        @size-change="fetchFavorites"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/index'
import { getFavoritePage, createFavorite, deleteFavorite } from '@/api/favorite'
import { getProductPage } from '@/api/product'
import { formatPrice, formatRate, formatDate } from '@/utils/format'
import { Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import ProductDetailDialog from '@/components/ProductDetailDialog.vue'
import type { WeaUserFavorite, WeaProduct, WeaMarketData } from '@/types'
import { useMarketSSEStore } from '@/store/marketSSE'

interface FavoriteItem extends WeaUserFavorite {
  productName?: string
  productId?: number
  currentPrice?: number
  riseFallRate?: number
}

const router = useRouter()
const userStore = useUserStore()

const favorites = ref<FavoriteItem[]>([])
const loading = ref(false)
const adding = ref(false)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(12)
const newProductCode = ref('')
const detailVisible = ref(false)
const selectedProductId = ref<number | null>(null)
const detailFallbackName = ref('')
const marketSSE = useMarketSSEStore()
const sseConnected = computed(() => marketSSE.connected)

async function enrichFavorites(records: WeaUserFavorite[]): Promise<FavoriteItem[]> {
  // 仅查当前页自选对应的 productCode，避免全表扫描
  const codes = [...new Set(records.map((r) => r.productCode))]
  const productMap = new Map<string, { name: string; id?: number }>()

  await Promise.all(codes.map(async (code) => {
    try {
      const res = await getProductPage({ pageNum: 1, pageSize: 1, productCode: code })
      const p = (res.data?.records || [])[0] as WeaProduct | undefined
      if (p) productMap.set(code, { name: p.productName, id: p.id })
    } catch { /* 单个查询失败不影响其他 */ }
  }))

  return records.map((fav) => {
    const meta = productMap.get(fav.productCode)
    return {
      ...fav,
      productName: meta?.name || fav.productCode,
      productId: meta?.id,
    }
  })
}

function handleMarketUpdate(data: WeaMarketData[]) {
  const dataMap = new Map(data.map((d) => [d.productCode, d]))
  favorites.value = favorites.value.map((fav) => {
    const market = dataMap.get(fav.productCode)
    return market
      ? { ...fav, currentPrice: market.currentPrice, riseFallRate: market.riseFallRate }
      : fav
  })
}

async function fetchFavorites() {
  loading.value = true
  try {
    const res = await getFavoritePage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      userId: userStore.userId || undefined,
    })
    const records = (res.data?.records || []) as WeaUserFavorite[]
    total.value = res.data?.total || 0
    favorites.value = await enrichFavorites(records)
  } catch (err) {
    console.warn('[favorite] fetchFavorites 失败:', err)
    favorites.value = []
  } finally {
    loading.value = false
  }
}

async function handleAdd() {
  const code = newProductCode.value.trim()
  if (!code) {
    ElMessage.warning('请输入产品代码')
    return
  }
  if (!userStore.userId) {
    ElMessage.error('用户信息异常，请重新登录')
    return
  }
  adding.value = true
  try {
    await createFavorite({ userId: userStore.userId, productCode: code })
    ElMessage.success('添加成功')
    newProductCode.value = ''
    pageNum.value = 1
    fetchFavorites()
  } catch {
    // handled
  } finally {
    adding.value = false
  }
}

async function handleDelete(item: FavoriteItem) {
  try {
    await ElMessageBox.confirm('确定要删除该自选吗？', '确认', { type: 'warning' })
    await deleteFavorite(item.id!)
    ElMessage.success('已删除')
    fetchFavorites()
  } catch { /* cancelled */ }
}

function goTrade(item: FavoriteItem) {
  router.push({ path: '/user/trade', query: { productCode: item.productCode } })
}

function showDetail(item: FavoriteItem) {
  if (!item.productId) {
    ElMessage.warning('该产品信息缺失，请前往产品中心查看')
    return
  }
  detailFallbackName.value = item.productName || item.productCode
  selectedProductId.value = item.productId
  detailVisible.value = true
}

onMounted(() => {
  if (userStore.userId) {
    fetchFavorites()
    marketSSE.subscribe(handleMarketUpdate)
  }
})

onUnmounted(() => {
  marketSSE.unsubscribe(handleMarketUpdate)
})
</script>

<style scoped>
.favorite-page { max-width: 1200px; }
.add-card { margin-bottom: 20px; }
.add-bar { display: flex; gap: 12px; }
.add-input { flex: 1; max-width: 360px; }
.loading-wrap, .empty-wrap { padding: 60px 0; }
.fav-col { margin-bottom: 20px; }
.fav-card { transition: var(--transition); }
.fav-card:hover { transform: translateY(-2px); box-shadow: var(--shadow-md) !important; }
.fav-header { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 12px; }
.fav-info { flex: 1; min-width: 0; }
.fav-name { font-size: 16px; font-weight: 600; color: var(--text-primary); margin-bottom: 2px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.fav-code { font-size: 13px; color: var(--text-secondary); font-family: 'DIN Pro', monospace; }
.fav-price-section { padding: 12px 0; border-top: 1px solid var(--border-color); }
.fav-price-row { display: flex; align-items: baseline; gap: 12px; }
.fav-price { font-size: 24px; font-weight: 700; color: var(--text-primary); font-family: 'DIN Pro', monospace; }
.fav-change { font-size: 15px; font-weight: 600; }
.fav-change.rise { color: var(--rise-color); }
.fav-change.fall { color: var(--fall-color); }
.no-price { font-size: 14px; color: var(--text-secondary); }
.fav-footer { display: flex; align-items: center; justify-content: space-between; padding-top: 12px; border-top: 1px solid var(--border-color); }
.fav-actions { display: flex; align-items: center; gap: 8px; }
.fav-time { font-size: 12px; color: var(--text-placeholder); }
.pagination-wrap { display: flex; justify-content: center; padding: 20px 0; }

</style>
