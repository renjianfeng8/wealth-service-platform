<template>
  <div class="fl-mini-row">
    <div
      v-for="product in products"
      :key="product.productCode"
      :class="['fl-mini-card', isRise(product.riseFallRate) ? 'fl-bdr-green' : 'fl-bdr-red']"
    >
      <div class="fl-mc-name">{{ product.productName }}</div>
      <div class="fl-mc-price" :class="isRise(product.riseFallRate) ? 'fl-rise' : 'fl-fall'">
        {{ formatPrice(product.price) }}
      </div>
      <div class="fl-mc-chg" :class="isRise(product.riseFallRate) ? 'fl-rise' : 'fl-fall'">
        <svg v-if="isRise(product.riseFallRate)" width="10" height="10" viewBox="0 0 24 24" fill="currentColor">
          <path d="M7 14l5-5 5 5z" />
        </svg>
        <svg v-else width="10" height="10" viewBox="0 0 24 24" fill="currentColor">
          <path d="M7 10l5 5 5-5z" />
        </svg>
        {{ formatRate(product.riseFallRate) }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface MiniProduct {
  productCode: string
  productName: string
  price?: number
  riseFallRate?: number
}

defineProps<{
  products: MiniProduct[]
  formatPrice: (value?: number) => string
  formatRate: (value?: number) => string
}>()

function isRise(value?: number) {
  return (value || 0) >= 0
}
</script>

<style scoped>
.fl-mini-row {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(105px, 1fr));
  gap: 10px;
}

.fl-mini-card {
  background: var(--fl-card-bg);
  border: 1px solid var(--fl-border);
  border-radius: 8px;
  padding: 12px 10px;
  transition: all 0.2s ease;
  cursor: pointer;
}

.fl-mini-card:hover {
  box-shadow: var(--fl-shadow-hover);
  transform: translateY(-1px);
}

.fl-mc-name {
  font-size: 12px;
  font-weight: 600;
  color: var(--fl-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.fl-mc-price {
  font-size: 16px;
  font-weight: 700;
  margin-top: 6px;
  font-family: 'Courier New', monospace;
}

.fl-mc-chg {
  font-size: 12px;
  font-weight: 600;
  margin-top: 3px;
  display: flex;
  align-items: center;
  gap: 2px;
}
</style>
