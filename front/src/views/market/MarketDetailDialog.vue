<template>
  <el-dialog
    :model-value="modelValue"
    :title="productCode || '行情详情'"
    width="720"
    destroy-on-close
    @update:model-value="emit('update:modelValue', $event)"
  >
    <div v-if="market" class="md-snapshot">
      <div class="md-row">
        <span class="md-label">当前价</span>
        <span class="md-price" :class="(market.riseFallRate || 0) >= 0 ? 'rise-text' : 'fall-text'">
          {{ formatPrice(market.currentPrice) }}
        </span>
        <span class="md-change" :class="(market.riseFallRate || 0) >= 0 ? 'rise-text' : 'fall-text'">
          {{ formatRate(market.riseFallRate) }}
        </span>
      </div>
      <div class="md-grid">
        <div class="md-cell">
          <span class="md-cell-label">开盘</span>
          <span class="md-cell-value">{{ formatPrice(market.openPrice) }}</span>
        </div>
        <div class="md-cell">
          <span class="md-cell-label">最高</span>
          <span class="md-cell-value">{{ formatPrice(market.highestPrice) }}</span>
        </div>
        <div class="md-cell">
          <span class="md-cell-label">最低</span>
          <span class="md-cell-value">{{ formatPrice(market.lowestPrice) }}</span>
        </div>
        <div class="md-cell">
          <span class="md-cell-label">行情时间</span>
          <span class="md-cell-value">{{ formatDateTime(market.marketTime) }}</span>
        </div>
      </div>
    </div>

    <KlinePanel
      v-if="products.length > 0"
      :products="products"
      :kline-data="klineData"
      :load-kline="loadKline"
    />
    <el-empty v-else description="暂无行情数据" :image-size="64" />
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import KlinePanel from '@/views/admin/dashboard/components/KlinePanel.vue'
import { getDashboardKline } from '@/api/dashboard'
import { formatPrice, formatRate, formatDateTime } from '@/utils/format'
import type { Candle } from '@/api/dashboard'
import type { WeaProduct, WeaMarketData } from '@/types'

const props = defineProps<{
  modelValue: boolean
  market: WeaMarketData | null
}>()
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const klineData = ref<Candle[]>([])
const productCode = computed(() => props.market?.productCode || '')

const products = computed<WeaProduct[]>(() =>
  productCode.value
    ? [{ productCode: productCode.value, productName: productCode.value } as WeaProduct]
    : [],
)

async function loadKline(code: string, period = '1M') {
  try {
    const res = await getDashboardKline(code, period)
    klineData.value = (res?.candles || []) as Candle[]
  } catch {
    klineData.value = []
  }
}

watch(
  () => props.market,
  (m) => {
    if (m?.productCode) {
      klineData.value = []
    }
  },
  { immediate: true },
)
</script>

<style scoped>
.md-snapshot { padding: 8px 0 4px; }
.md-row { display: flex; align-items: baseline; gap: 12px; margin-bottom: 12px; }
.md-label { font-size: 13px; color: var(--text-secondary); }
.md-price { font-size: 24px; font-weight: 700; font-family: 'DIN Pro', monospace; }
.md-change { font-size: 14px; font-weight: 600; }
.rise-text { color: var(--rise-color, #34c759); }
.fall-text { color: var(--fall-color, #ff3b30); }
.md-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  padding: 10px 0;
  border-top: 1px solid var(--border-color);
  border-bottom: 1px solid var(--border-color);
  margin-bottom: 14px;
}
.md-cell { display: flex; flex-direction: column; gap: 4px; }
.md-cell-label { font-size: 12px; color: var(--text-placeholder); }
.md-cell-value { font-size: 13px; color: var(--text-primary); font-weight: 500; }
@media (max-width: 480px) {
  .md-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
