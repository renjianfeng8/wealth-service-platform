<template>
  <div class="fl-market-panel">
    <div class="fl-card fl-card-list">
      <div class="fl-card-header">
        <span class="fl-card-title">实时行情</span>
        <el-select v-model="mktFilter" size="small" placeholder="筛选" style="width:85px">
          <el-option label="全部" value="" />
          <el-option label="上涨" value="rise" />
          <el-option label="下跌" value="fall" />
        </el-select>
      </div>
      <div class="fl-list-wrap">
        <div v-for="p in filteredList" :key="p.productCode" class="fl-list-row">
          <div class="fl-list-l">
            <div class="fl-list-name">{{ p.productName }}</div>
            <div class="fl-list-code">{{ p.productCode }}</div>
          </div>
          <div class="fl-list-r">
            <div class="fl-list-price">¥{{ formatPrice(p.price) }}</div>
            <div class="fl-list-chg" :class="(p.riseFallRate || 0) >= 0 ? 'fl-rise' : 'fl-fall'">
              {{ formatRate(p.riseFallRate) }}
            </div>
          </div>
        </div>
        <el-empty v-if="!filteredList.length" class="fl-list-empty" description="暂无数据" :image-size="50" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import type { WeaProduct } from '@/types'

const props = defineProps<{
  products: WeaProduct[]
  formatPrice: (value?: number) => string
  formatRate: (value?: number) => string
}>()

const mktFilter = ref('')
const filteredList = computed(() => {
  if (!mktFilter.value) return props.products
  return props.products.filter(product => {
    const rate = product.riseFallRate || 0
    return mktFilter.value === 'rise' ? rate > 0 : rate < 0
  })
})
</script>

<style scoped>
.fl-market-panel {
  display: flex;
  flex-direction: column;
}

.fl-card-list {
  height: 100%;
}

.fl-list-wrap {
  max-height: 420px;
  overflow-y: auto;
  padding-right: 4px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 0 24px;
  align-content: start;
}

.fl-list-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid var(--fl-border-light);
}

.fl-list-row:last-child {
  border-bottom: none;
}

/* 多列网格下空态需跨满整行，否则会被压进单列 */
.fl-list-empty {
  grid-column: 1 / -1;
}

.fl-list-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--fl-text);
}

.fl-list-code {
  font-size: 11px;
  color: var(--fl-text-dim);
}

.fl-list-r {
  text-align: right;
}

.fl-list-price {
  font-size: 14px;
  font-weight: 600;
  color: var(--fl-text);
  font-family: 'DIN Pro', monospace;
}

.fl-list-chg {
  font-size: 12px;
  font-weight: 600;
  margin-top: 1px;
}

.fl-list-wrap::-webkit-scrollbar {
  width: 4px;
}

.fl-list-wrap::-webkit-scrollbar-thumb {
  background: var(--fl-border-light);
  border-radius: 2px;
}
</style>
