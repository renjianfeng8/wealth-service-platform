<template>
  <div class="product-page">
    <div class="page-title">产品中心</div>

    <!-- 筛选栏 -->
    <el-card class="filter-card" shadow="never">
      <div class="filter-bar">
        <el-radio-group v-model="filterType" @change="handleFilter">
          <el-radio-button :value="0">全部</el-radio-button>
          <el-radio-button
            v-for="opt in PRODUCT_TYPE_OPTIONS"
            :key="opt.value"
            :value="opt.value"
          >
            {{ opt.label }}
          </el-radio-button>
        </el-radio-group>
        <el-input
          v-model="keyword"
          placeholder="搜索产品名称/编码"
          clearable
          style="width: 220px"
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        />
        <el-select v-model="sortBy" style="width: 140px" @change="handleSearch">
          <el-option label="默认排序" :value="''" />
          <el-option label="价格 ↑" :value="'price_asc'" />
          <el-option label="价格 ↓" :value="'price_desc'" />
          <el-option label="涨幅 ↑" :value="'riseFallRate_asc'" />
          <el-option label="涨幅 ↓" :value="'riseFallRate_desc'" />
        </el-select>
      </div>
    </el-card>

    <!-- 产品网格 -->
    <div v-if="loading" class="loading-wrap">
      <el-skeleton :rows="3" animated />
    </div>
    <div v-else-if="hasError" class="empty-wrap">
      <el-result icon="error" title="加载失败" sub-title="数据获取异常，请重试">
        <template #extra>
          <el-button type="primary" @click="fetchProducts">重试</el-button>
        </template>
      </el-result>
    </div>
    <div v-else-if="products.length === 0" class="empty-wrap">
      <el-empty description="暂无产品数据" />
    </div>
    <div v-else class="product-grid">
      <el-row :gutter="20">
        <el-col
          v-for="item in products"
          :key="item.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
          class="product-col"
        >
          <el-card class="product-card" shadow="never" @click="showDetail(item)">
            <div class="product-type">
              <el-tag size="small" effect="plain">
                {{ productTypeText(item.productType) }}
              </el-tag>
              <el-tag v-if="item.status === 1" size="small" type="success" effect="plain">在售</el-tag>
              <el-tag v-else size="small" type="danger" effect="plain">停售</el-tag>
            </div>
            <h3 class="product-name">{{ item.productName }}</h3>
            <div class="product-code">{{ item.productCode }}</div>
            <div class="product-price-section">
              <div class="price-row">
                <span class="price-label">现价</span>
                <span class="product-price">{{ formatPrice(item.price) }}</span>
              </div>
              <div class="change-row" :class="(item.riseFallRate || 0) >= 0 ? 'rise' : 'fall'">
                <el-icon :size="12">
                  <CaretTop v-if="(item.riseFallRate || 0) >= 0" />
                  <CaretBottom v-else />
                </el-icon>
                <span>{{ formatRate(item.riseFallRate) }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrap">
      <el-pagination
        v-if="total > 0"
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[12, 24, 48]"
        layout="total, sizes, prev, pager, next"
        @current-change="fetchProducts"
        @size-change="handlePageSizeChange"
      />
    </div>

    <!-- 详情弹窗（实时拉取） -->
    <ProductDetailDialog
      v-model="detailVisible"
      :product-id="selectedProductId"
      :fallback-name="detailFallbackName"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getProductPage } from '@/api/product'
import { PRODUCT_TYPE_OPTIONS } from '@/types'
import { formatPrice, formatRate, productTypeText } from '@/utils/format'
import { CaretTop, CaretBottom } from '@element-plus/icons-vue'
import ProductDetailDialog from '@/components/ProductDetailDialog.vue'
import type { WeaProduct } from '@/types'

const products = ref<WeaProduct[]>([])
const loading = ref(false)
const hasError = ref(false)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(12)
const filterType = ref(0)
const keyword = ref('')
const sortBy = ref('')
const detailVisible = ref(false)
const selectedProductId = ref<number | null>(null)
const detailFallbackName = ref('')

async function fetchProducts() {
  hasError.value = false
  loading.value = true
  try {
    const params: {
      pageNum: number
      pageSize: number
      productType?: number
      productName?: string
      productCode?: string
      orderBy?: string
      orderDir?: string
    } = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (filterType.value) params.productType = filterType.value
    if (keyword.value) {
      params.productName = keyword.value
      params.productCode = keyword.value
    }
    if (sortBy.value) {
      const parts = sortBy.value.split('_')
      params.orderBy = parts[0]
      params.orderDir = parts[1]
    }
    const res = await getProductPage(params)
    products.value = (res.data?.records || []) as WeaProduct[]
    total.value = res.data?.total || 0
  } catch {
    hasError.value = true
    products.value = []
  } finally {
    loading.value = false
  }
}

function handleFilter() {
  pageNum.value = 1
  fetchProducts()
}

function handlePageSizeChange() {
  pageNum.value = 1
  fetchProducts()
}

function handleSearch() {
  pageNum.value = 1
  fetchProducts()
}

function showDetail(item: WeaProduct) {
  detailFallbackName.value = item.productName
  selectedProductId.value = item.id ?? null
  detailVisible.value = true
}

onMounted(() => {
  fetchProducts()
})
</script>

<style scoped>
.product-page {
  max-width: 1200px;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.loading-wrap,
.empty-wrap {
  padding: 60px 0;
}

.product-grid {
  margin-bottom: 20px;
}

.product-col {
  margin-bottom: 20px;
}

.product-card {
  cursor: pointer;
  transition: var(--transition);
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg) !important;
}

.product-type {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.product-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-code {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 16px;
  font-family: 'DIN Pro', monospace;
}

.product-price-section {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  padding-top: 16px;
  border-top: 1px solid var(--border-color);
}

.price-row {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.price-label {
  font-size: 12px;
  color: var(--text-secondary);
}

.product-price {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  font-family: 'DIN Pro', monospace;
}

.change-row {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 14px;
  font-weight: 600;
}

.change-row.rise { color: var(--rise-color); }
.change-row.fall { color: var(--fall-color); }

.pagination-wrap {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}
</style>
