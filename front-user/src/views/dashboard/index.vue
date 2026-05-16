<template>
  <div class="dashboard">
    <div class="page-title">首页概览</div>

    <!-- 市场概况 -->
    <el-row :gutter="20" class="dashboard-section">
      <el-col :xs="24" :sm="12" :lg="6" v-for="stat in marketStats" :key="stat.label">
        <el-card class="stat-card" shadow="never">
          <div class="stat-header">
            <span class="stat-label">{{ stat.label }}</span>
            <el-icon :size="24" :style="{ color: stat.color }">
              <component :is="stat.icon" />
            </el-icon>
          </div>
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-change" :class="stat.change >= 0 ? 'rise' : 'fall'">
            <el-icon :size="14">
              <Top v-if="stat.change >= 0" />
              <Bottom v-else />
            </el-icon>
            {{ Math.abs(stat.change).toFixed(2) }}%
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <el-card class="quick-actions" shadow="never">
      <template #header>
        <span>快捷操作</span>
      </template>
      <div class="action-grid">
        <div
          v-for="action in quickActions"
          :key="action.label"
          class="action-item"
          @click="router.push(action.path)"
        >
          <div class="action-icon" :style="{ background: action.bg }">
            <el-icon :size="28" color="#fff">
              <component :is="action.icon" />
            </el-icon>
          </div>
          <span class="action-label">{{ action.label }}</span>
        </div>
      </div>
    </el-card>

    <!-- 最新行情 -->
    <el-card class="market-preview" shadow="never">
      <template #header>
        <div class="card-header">
          <span>最新行情</span>
          <el-button text type="primary" @click="router.push('/market')">查看更多</el-button>
        </div>
      </template>
      <el-table :data="hotProducts" stripe v-loading="loading" empty-text="暂无数据">
        <el-table-column prop="productName" label="产品名称" min-width="140" />
        <el-table-column prop="productCode" label="代码" width="120" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ productTypeText(row.productType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最新价" width="120" align="right">
          <template #default="{ row }">
            <span class="price-text">{{ formatPrice(row.price) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="涨跌幅" width="120" align="right">
          <template #default="{ row }">
            <span :class="(row.riseFallRate || 0) >= 0 ? 'rise-text' : 'fall-text'">
              {{ formatRate(row.riseFallRate) }}
            </span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getProductList } from '@/api/product'
import {
  TrendCharts, Coin, DataLine, Star,
  Goods, Notebook, Money, Message,
  Top, Bottom,
} from '@element-plus/icons-vue'
import { formatPrice, formatRate, productTypeText } from '@/utils/format'
import type { WeaProduct } from '@/types'

const router = useRouter()
const loading = ref(false)
const products = ref<WeaProduct[]>([])

const hotProducts = computed(() => products.value.slice(0, 8))

const marketStats = computed(() => [
  { label: '产品总数', value: products.value.length, change: 12.5, icon: Coin, color: '#1a6dff' },
  { label: '上涨产品', value: products.value.filter(p => (p.riseFallRate || 0) > 0).length, change: 5.8, icon: TrendCharts, color: '#34c759' },
  { label: '下跌产品', value: products.value.filter(p => (p.riseFallRate || 0) < 0).length, change: -3.2, icon: DataLine, color: '#ff3b30' },
  { label: '在售产品', value: products.value.filter(p => p.status === 1).length, change: 8.1, icon: Star, color: '#ff9500' },
])

const quickActions = [
  { label: '产品中心', path: '/product', icon: Goods, bg: 'linear-gradient(135deg, #1a6dff, #0a4dcc)' },
  { label: '实时行情', path: '/market', icon: TrendCharts, bg: 'linear-gradient(135deg, #34c759, #28a745)' },
  { label: '我的自选', path: '/favorite', icon: Star, bg: 'linear-gradient(135deg, #ff9500, #e68a00)' },
  { label: '交易委托', path: '/trade', icon: Money, bg: 'linear-gradient(135deg, #ff3b30, #d63031)' },
  { label: '财经资讯', path: '/news', icon: Notebook, bg: 'linear-gradient(135deg, #8e44ad, #6c3483)' },
  { label: '消息中心', path: '/message', icon: Message, bg: 'linear-gradient(135deg, #00b894, #00a381)' },
]

async function fetchProducts() {
  loading.value = true
  try {
    const res = await getProductList()
    products.value = (res.data || []) as WeaProduct[]
  } catch {
    products.value = []
  } finally {
    loading.value = false
  }
}

onMounted(fetchProducts)
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
}

.dashboard-section {
  margin-bottom: 20px;
}

.stat-card {
  margin-bottom: 20px;
  cursor: default;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
  margin-bottom: 8px;
}

.stat-change {
  font-size: 13px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 2px;
}

.stat-change.rise { color: var(--rise-color); }
.stat-change.fall { color: var(--fall-color); }

.quick-actions {
  margin-bottom: 20px;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 16px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 20px 12px;
  border-radius: var(--radius);
  cursor: pointer;
  transition: var(--transition);
}

.action-item:hover {
  background: var(--border-light);
  transform: translateY(-2px);
}

.action-icon {
  width: 52px;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
}

.action-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-regular);
}

.market-preview {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.price-text {
  font-weight: 600;
  font-family: 'DIN Pro', monospace;
}
</style>
