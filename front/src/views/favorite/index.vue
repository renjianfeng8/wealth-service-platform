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
              <el-button type="primary" size="small" @click="goTrade(item)">交易</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

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
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/index'
import { getFavoritePage, createFavorite, deleteFavorite } from '@/api/favorite'
import { getProductList } from '@/api/product'
import { formatPrice, formatRate, formatDate } from '@/utils/format'
import { Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { WeaUserFavorite, WeaProduct, WeaMarketData } from '@/types'
import { createMarketSSE, onMarketUpdate } from '@/utils/sse'

interface FavoriteItem extends WeaUserFavorite {
  productName?: string
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
const sseConnected = ref(false)
let eventSource: EventSource | null = null

async function enrichFavorites(records: WeaUserFavorite[]): Promise<FavoriteItem[]> {
  let allProducts: WeaProduct[] = []
  try {
    const pr = await getProductList()
    allProducts = (pr.data || []) as WeaProduct[]
  } catch { /* ignore */ }

  return records.map((fav) => {
    const product = allProducts.find((p) => p.productCode === fav.productCode)
    return {
      ...fav,
      productName: product?.productName || fav.productCode,
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
  } catch {
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

onMounted(() => {
  if (userStore.userId) {
    fetchFavorites()
    eventSource = createMarketSSE((connected) => sseConnected.value = connected)
    onMarketUpdate(eventSource, handleMarketUpdate)
  }
})

onUnmounted(() => {
  eventSource?.close()
  eventSource = null
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
.fav-time { font-size: 12px; color: var(--text-placeholder); }
.pagination-wrap { display: flex; justify-content: center; padding: 20px 0; }

.sse-status {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  padding: 2px 10px;
  border-radius: 12px;
  margin-left: 12px;
  vertical-align: middle;
}
.sse-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
}
.sse-status.connected {
  color: #67c23a;
  background: rgba(103, 194, 58, 0.1);
}
.sse-status.connected .sse-dot {
  background: #67c23a;
}
.sse-status.disconnected {
  color: #f56c6c;
  background: rgba(245, 108, 108, 0.1);
}
.sse-status.disconnected .sse-dot {
  background: #f56c6c;
}
</style>
