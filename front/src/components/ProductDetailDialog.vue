<template>
  <el-dialog
    :model-value="modelValue"
    :title="item?.productName || fallbackName || '产品详情'"
    width="520"
    destroy-on-close
    @update:model-value="emit('update:modelValue', $event)"
  >
    <div v-loading="loading" class="pd-body">
      <template v-if="item">
        <div class="pd-row">
          <span class="pd-label">产品代码</span>
          <span class="pd-value code">{{ item.productCode }}</span>
        </div>
        <div class="pd-row">
          <span class="pd-label">产品类型</span>
          <span class="pd-value">{{ productTypeText(item.productType) }}</span>
        </div>
        <div class="pd-row">
          <span class="pd-label">当前价格</span>
          <span class="pd-value price">{{ formatPrice(item.price) }}</span>
        </div>
        <div class="pd-row">
          <span class="pd-label">涨跌额</span>
          <span class="pd-value" :class="(item.riseFall || 0) >= 0 ? 'rise-text' : 'fall-text'">
            {{ item.riseFall != null ? formatPrice(item.riseFall) : '-' }}
          </span>
        </div>
        <div class="pd-row">
          <span class="pd-label">涨跌幅</span>
          <span class="pd-value" :class="(item.riseFallRate || 0) >= 0 ? 'rise-text' : 'fall-text'">
            {{ formatRate(item.riseFallRate) }}
          </span>
        </div>
        <div class="pd-row">
          <span class="pd-label">状态</span>
          <el-tag :type="item.status === 1 ? 'success' : 'danger'" size="small">
            {{ item.status === 1 ? '在售' : '停售' }}
          </el-tag>
        </div>
      </template>
      <el-empty v-else-if="!loading" description="产品不存在或已下架" :image-size="64" />
    </div>
    <template #footer>
      <el-button @click="emit('update:modelValue', false)">关闭</el-button>
      <el-button
        :icon="isFavorited ? StarFilled : Star"
        :disabled="item?.status !== 1"
        :type="isFavorited ? 'warning' : ''"
        :loading="favLoading"
        @click="toggleFavorite"
      >
        {{ isFavorited ? '已收藏' : '收藏' }}
      </el-button>
      <el-button type="primary" :disabled="item?.status !== 1" @click="goTrade">去交易</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/index'
import { getProductById } from '@/api/product'
import { getFavoritePage, createFavorite, deleteFavorite } from '@/api/favorite'
import { formatPrice, formatRate, productTypeText } from '@/utils/format'
import { Star, StarFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { WeaProduct } from '@/types'

const props = defineProps<{
  modelValue: boolean
  productId: number | null
  fallbackName?: string
}>()
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const router = useRouter()
const userStore = useUserStore()

const item = ref<WeaProduct | null>(null)
const loading = ref(false)
const favLoading = ref(false)
const favoriteId = ref<number | null>(null)

const isFavorited = computed(() => favoriteId.value != null)

async function load() {
  if (!props.productId) return
  item.value = null
  favoriteId.value = null
  loading.value = true
  try {
    const res = await getProductById(props.productId)
    item.value = (res.data || null) as WeaProduct | null
    await loadFavoriteState()
  } catch {
    item.value = null
  } finally {
    loading.value = false
  }
}

async function loadFavoriteState() {
  favoriteId.value = null
  if (!userStore.userId || !item.value?.productCode) return
  try {
    const res = await getFavoritePage({ pageNum: 1, pageSize: 1, userId: userStore.userId, productCode: item.value.productCode })
    const first = (res.data?.records || [])[0] as { id: number } | undefined
    favoriteId.value = first?.id ?? null
  } catch { /* 单个查询失败静默 */ }
}

watch(
  () => [props.modelValue, props.productId] as const,
  ([visible, id]) => {
    if (visible && id) load()
  },
  { immediate: true },
)

async function toggleFavorite() {
  if (!userStore.userId) {
    ElMessage.warning('请先登录')
    return
  }
  if (!item.value?.productCode) return
  favLoading.value = true
  try {
    if (favoriteId.value) {
      await deleteFavorite(favoriteId.value)
      favoriteId.value = null
      ElMessage.success('已取消收藏')
    } else {
      await createFavorite({ userId: userStore.userId, productCode: item.value.productCode })
      await loadFavoriteState()
      ElMessage.success('已添加自选')
    }
  } catch {
    // handled globally
  } finally {
    favLoading.value = false
  }
}

function goTrade() {
  if (!item.value?.productCode) return
  emit('update:modelValue', false)
  router.push({ path: '/user/trade', query: { productCode: item.value.productCode } })
}
</script>

<style scoped>
.pd-body { min-height: 80px; padding: 4px 0; }
.pd-row {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--border-color);
}
.pd-row:last-child { border-bottom: none; }
.pd-label {
  width: 100px;
  font-size: 14px;
  color: var(--text-secondary);
  flex-shrink: 0;
}
.pd-value { font-size: 14px; color: var(--text-primary); font-weight: 500; }
.pd-value.code { font-family: 'DIN Pro', monospace; }
.pd-value.price { font-size: 20px; font-weight: 700; font-family: 'DIN Pro', monospace; }
.rise-text { color: var(--rise-color, #34c759); }
.fall-text { color: var(--fall-color, #ff3b30); }
</style>
