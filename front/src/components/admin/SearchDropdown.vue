<template>
  <transition name="el-zoom-in-top">
    <div v-if="visible" class="search-dropdown" @click.stop>
      <div class="sd-header">
        <span v-if="loading">搜索中...</span>
        <span v-else-if="results.length > 0">共 {{ total }} 条结果</span>
        <span v-else-if="keyword">未找到"{{ keyword }}"相关产品</span>
        <span v-else>输入产品名称或编码搜索</span>
      </div>

      <div v-if="results.length > 0" class="sd-body">
        <div
          v-for="item in results"
          :key="item.id"
          class="sd-item"
          @click="handleSelect(item)"
        >
          <div class="sd-item-info">
            <span class="sd-item-name">{{ item.productName }}</span>
            <span class="sd-item-code">{{ item.productCode }}</span>
          </div>
          <div class="sd-item-meta">
            <span class="sd-item-price">{{ formatPrice(item.price) }}</span>
            <el-tag :type="item.status === 1 ? 'success' : 'info'" size="small">
              {{ item.status === 1 ? '在售' : '已下架' }}
            </el-tag>
          </div>
        </div>
      </div>

      <div
        v-if="keyword && results.length > 0"
        class="sd-footer"
        @click="handleViewAll"
      >
        <span>查看更多 →</span>
        <span class="sd-footer-hint">Enter</span>
      </div>

      <div v-if="keyword && results.length === 0 && !loading" class="sd-empty-wrap">
        <el-empty :image-size="48" description="暂无匹配产品" />
      </div>
    </div>
  </transition>
</template>

<script setup lang="ts">
import { formatPrice } from '@/utils/format'
import type { WeaProduct } from '@/types'

defineProps<{
  visible: boolean
  keyword: string
  results: WeaProduct[]
  total: number
  loading: boolean
}>()

const emit = defineEmits<{
  select: [item: WeaProduct]
  viewAll: []
}>()

function handleSelect(item: WeaProduct) {
  emit('select', item)
}

function handleViewAll() {
  emit('viewAll')
}
</script>

<style scoped>
.search-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 6px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  z-index: 2000;
  max-height: 440px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  border: 1px solid #e8e8e8;
}

.sd-header {
  padding: 10px 16px;
  font-size: 12px;
  color: #999;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.sd-body {
  overflow-y: auto;
  flex: 1;
}

.sd-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  cursor: pointer;
  transition: background 0.15s;
}

.sd-item:hover {
  background: #f5f7fa;
}

.sd-item + .sd-item {
  border-top: 1px solid #f5f5f5;
}

.sd-item-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
  flex: 1;
}

.sd-item-name {
  font-size: 14px;
  font-weight: 500;
  color: #1d2129;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sd-item-code {
  font-size: 12px;
  color: #86909c;
}

.sd-item-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
  margin-left: 12px;
}

.sd-item-price {
  font-size: 14px;
  font-weight: 600;
  color: #1d2129;
  font-family: 'DIN Pro', 'Courier New', monospace;
}

.sd-footer {
  padding: 10px 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 13px;
  color: var(--el-color-primary);
  border-top: 1px solid #f0f0f0;
  cursor: pointer;
  flex-shrink: 0;
  transition: background 0.15s;
}

.sd-footer:hover {
  background: #f5f7fa;
}

.sd-footer-hint {
  font-size: 11px;
  color: #c9cdd4;
  background: #f5f7fa;
  padding: 0 6px;
  border-radius: 3px;
  border: 1px solid #e8e8e8;
  line-height: 18px;
}

.sd-empty-wrap {
  padding: 24px 0;
  flex-shrink: 0;
}
</style>
