<template>
  <div class="market-page">
    <div class="page-title">
      实时行情
      <span class="sse-status" :class="sseConnected ? 'connected' : 'disconnected'">
        <span class="sse-dot"></span>
        {{ sseConnected ? '实时已连接' : '实时已断开' }}
      </span>
    </div>

    <el-card class="market-table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>行情数据</span>
          <el-button size="small" :icon="Refresh" @click="refreshData" :loading="refreshing">
            {{ refreshing ? '刷新中...' : '刷新' }}
          </el-button>
        </div>
      </template>

      <el-result v-if="hasError" icon="error" title="加载失败" sub-title="数据获取异常，请重试">
        <template #extra>
          <el-button type="primary" @click="fetchData">重试</el-button>
        </template>
      </el-result>
      <template v-else>
        <el-table :data="marketList" stripe v-loading="loading" empty-text="暂无行情数据">
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="productCode" label="产品代码" width="120">
          <template #default="{ row }">
            <span class="code-text">{{ row.productCode }}</span>
          </template>
        </el-table-column>
        <el-table-column label="当前价" width="130" align="right">
          <template #default="{ row }">
            <span class="price-value">{{ formatPrice(row.currentPrice) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="开盘价" width="120" align="right">
          <template #default="{ row }">
            {{ formatPrice(row.openPrice) }}
          </template>
        </el-table-column>
        <el-table-column label="最高价" width="120" align="right">
          <template #default="{ row }">
            <span class="high-text">{{ formatPrice(row.highestPrice) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="最低价" width="120" align="right">
          <template #default="{ row }">
            <span class="low-text">{{ formatPrice(row.lowestPrice) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="涨跌额" width="120" align="right">
          <template #default="{ row }">
            <span :class="(row.riseFall || 0) >= 0 ? 'rise-text' : 'fall-text'">
              {{ formatPrice(row.riseFall) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="涨跌幅" width="110" align="right">
          <template #default="{ row }">
            <el-tag
              :type="(row.riseFallRate || 0) >= 0 ? 'success' : 'danger'"
              effect="dark"
              size="small"
            >
              {{ formatRate(row.riseFallRate) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="行情时间" width="170" align="center">
          <template #default="{ row }">
            <span class="time-text">{{ formatDateTime(row.marketTime) }}</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-if="total > 0"
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="fetchData"
          @size-change="fetchData"
        />
      </div>
        </template>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { getMarketDataPage } from '@/api/product'
import { formatPrice, formatRate, formatDateTime } from '@/utils/format'
import { Refresh } from '@element-plus/icons-vue'
import type { WeaMarketData } from '@/types'
import { createMarketSSE, onMarketUpdate } from '@/utils/sse'

const marketList = ref<WeaMarketData[]>([])
const loading = ref(false)
const refreshing = ref(false)
const hasError = ref(false)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const sseConnected = ref(false)
let eventSource: EventSource | null = null

async function fetchData() {
  hasError.value = false
  loading.value = true
  try {
    const res = await getMarketDataPage({ pageNum: pageNum.value, pageSize: pageSize.value })
    marketList.value = (res.data?.records || []) as WeaMarketData[]
    total.value = res.data?.total || 0
  } catch {
    hasError.value = true
    marketList.value = []
  } finally {
    loading.value = false
  }
}

async function refreshData() {
  refreshing.value = true
  await fetchData()
  setTimeout(() => { refreshing.value = false }, 300)
}

function handleMarketUpdate(data: WeaMarketData[]) {
  const dataMap = new Map(data.map((d) => [d.productCode, d]))
  marketList.value = marketList.value.map((item) => {
    const update = dataMap.get(item.productCode)
    return update ? { ...item, ...update } : item
  })
}

onMounted(() => {
  fetchData()
  eventSource = createMarketSSE((connected) => sseConnected.value = connected)
  onMarketUpdate(eventSource, handleMarketUpdate)
})

onUnmounted(() => {
  eventSource?.close()
  eventSource = null
})
</script>

<style scoped>
.market-page {
  max-width: 1200px;
}

.market-table-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.code-text {
  font-family: 'DIN Pro', monospace;
  font-weight: 500;
  color: var(--primary);
}

.price-value {
  font-size: 15px;
  font-weight: 700;
  font-family: 'DIN Pro', monospace;
  color: var(--text-primary);
}

.high-text {
  color: var(--rise-color);
  font-weight: 500;
}

.low-text {
  color: var(--fall-color);
  font-weight: 500;
}

.time-text {
  font-size: 13px;
  color: var(--text-secondary);
}

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

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
