# 第二批用户端体验补全 — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 前端消费 6 个闲置 GET 接口，补全用户端详情/交互（产品/资讯/消息详情实时拉取、行情 K 线、委托单详情、自选与首页深链），后端零改动。

**Architecture:** 抽 2 个共享弹窗组件（`ProductDetailDialog`、`NewsDetailDialog`）承担详情拉取与展示；行情 K 线新建 `MarketDetailDialog` 复用现有 `KlinePanel` 组件；交易页、首页、个人中心在页面内联接入弹窗。全部改动位于 `front/src`，不触碰 `wealth-service` / `wealth-common`。

**Tech Stack:** Vue 3 `<script setup>` + TypeScript + Element Plus + ECharts + axios（复用现有 `request` 实例与 API 封装）。

**提交约定：** 项目 CLAUDE.md 规定禁止自动 `git commit`。本计划中「提交」步骤为可选项，需用户明确指示后执行；默认每完成一个 Task 仅保留工作区改动。

**验证约定：** 每个 Task 完成执行 `cd front && npm run build`（= `vue-tsc --noEmit && vite build`，含类型检查）；交互验收用 `cd front && npx vite` 手动点验。逐项验收（用户已确认）。

---

### Task 0: 基线确认

- [ ] **Step 1: 确认前端可构建**

```bash
cd front
npm install
npm run build
```
Expected: `vue-tsc` 无类型错误，`vite build` 产出 `dist/`。若基线失败，先解决既有问题再继续。

- [ ] **Step 2: 记录基线状态**
  确认 `git status` 干净（除本批次前已有的 docs/superpowers 目录外无改动）。

---

### Task 1: 新建共享组件 `ProductDetailDialog.vue`

**Files:**
- Create: `front/src/components/ProductDetailDialog.vue`

功能：按 `productId` 实时拉取产品（`GET /product/wea-product/{id}`）、展示详情、内联收藏/取消收藏、跳转交易。被 products / favorite / home / dashboard 四页复用。

- [ ] **Step 1: 写入组件文件**

```vue
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
```

- [ ] **Step 2: 构建验证**

```bash
cd front && npm run build
```
Expected: 通过（vue-tsc 无报错）。

---

### Task 2: 新建共享组件 `NewsDetailDialog.vue`

**Files:**
- Create: `front/src/components/NewsDetailDialog.vue`

功能：按 `newsId` 实时拉取资讯全文（`GET /message/wea-news/{id}`）。被 news / home / dashboard 三页复用。

- [ ] **Step 1: 写入组件文件**

```vue
<template>
  <el-dialog
    :model-value="modelValue"
    :title="item?.title || '资讯详情'"
    width="700"
    destroy-on-close
    @update:model-value="emit('update:modelValue', $event)"
  >
    <div v-loading="loading" class="nd-body">
      <template v-if="item">
        <div class="nd-meta">
          <el-tag size="small" effect="plain">{{ newsTypeText(item.newsType) }}</el-tag>
          <span v-if="item.source" class="nd-source">来源：{{ item.source }}</span>
          <span class="nd-time">{{ formatDateTime(item.publishTime) }}</span>
        </div>
        <div class="nd-content">{{ item.content }}</div>
      </template>
      <el-empty v-else-if="!loading" description="资讯不存在" :image-size="64" />
    </div>
    <template #footer>
      <el-button @click="emit('update:modelValue', false)">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { getNewsById } from '@/api/message'
import { formatDateTime, newsTypeText } from '@/utils/format'
import type { WeaNews } from '@/types'

const props = defineProps<{
  modelValue: boolean
  newsId: number | null
}>()
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const item = ref<WeaNews | null>(null)
const loading = ref(false)

async function load() {
  if (!props.newsId) return
  loading.value = true
  try {
    const res = await getNewsById(props.newsId)
    item.value = (res.data || null) as WeaNews | null
  } catch {
    item.value = null
  } finally {
    loading.value = false
  }
}

watch(
  () => [props.modelValue, props.newsId] as const,
  ([visible, id]) => {
    if (visible && id) load()
  },
  { immediate: true },
)
</script>

<style scoped>
.nd-body { min-height: 80px; }
.nd-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-color);
}
.nd-source { font-size: 14px; color: var(--text-secondary); }
.nd-time { font-size: 13px; color: var(--text-placeholder); margin-left: auto; }
.nd-content {
  font-size: 15px;
  color: var(--text-regular);
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
```

- [ ] **Step 2: 构建验证**

```bash
cd front && npm run build
```
Expected: 通过。

---

### Task 3: A3 — 产品中心接入 `ProductDetailDialog`

**Files:**
- Modify: `front/src/views/products/index.vue`

将内联详情弹窗替换为共享组件；移除页内收藏逻辑（已下沉到弹窗）。

- [ ] **Step 1: 修改 `<template>` 详情弹窗**

将现有 `<el-dialog ...>...` 整块（`front/src/views/products/index.vue:104-142`）替换为：

```html
    <!-- 详情弹窗（实时拉取） -->
    <ProductDetailDialog
      v-model="detailVisible"
      :product-id="selectedProductId"
      :fallback-name="detailFallbackName"
    />
```

- [ ] **Step 2: 修改 `<script setup>`**

将 `import { ref, computed, onMounted } from 'vue'` 改为 `import { ref, onMounted } from 'vue'`；替换 import 段：

```ts
import { ref, onMounted } from 'vue'
import { getProductPage } from '@/api/product'
import { PRODUCT_TYPE_OPTIONS } from '@/types'
import { formatPrice, formatRate, productTypeText } from '@/utils/format'
import ProductDetailDialog from '@/components/ProductDetailDialog.vue'
import type { WeaProduct } from '@/types'
```

删除 `useRouter` / `useUserStore` / `createFavorite` / `getFavoritePage` / `deleteFavorite` / `Star` / `StarFilled` / `ElMessage` / `WeaUserFavorite` 相关 import 与逻辑（`router`、`userStore`、`favoritedMap`、`isFavorited`、`fetchFavorites`、`handleFavorite`、`goTrade` 全部移除）。保留 `fetchProducts` / `handleFilter` / `handleSearch` / 分页逻辑。

替换 `showDetail` 与弹窗状态声明：

```ts
const detailVisible = ref(false)
const selectedProductId = ref<number | null>(null)
const detailFallbackName = ref('')

function showDetail(item: WeaProduct) {
  detailFallbackName.value = item.productName
  selectedProductId.value = item.id ?? null
  detailVisible.value = true
}
```

`onMounted` 改为仅 `fetchProducts()`：

```ts
onMounted(() => {
  fetchProducts()
})
```

- [ ] **Step 3: 删除不再使用的 style 块**
  删除 `.detail-body` / `.detail-row` / `.detail-label` / `.detail-value` 相关样式（若保留无引用无碍，可一并清理）。

- [ ] **Step 4: 构建验证**

```bash
cd front && npm run build
```
Expected: 通过（确认无未使用 import 报错——vue-tsc 默认不报未使用 import，但保持整洁）。

- [ ] **Step 5: 手动验收**
  `npx vite` → 产品中心 → 点击产品卡 → 弹窗显示实时数据；收藏/去交易可用；无报错。

---

### Task 4: A4 — 自选页新增「详情」入口

**Files:**
- Modify: `front/src/views/favorite/index.vue`

- [ ] **Step 1: 模板卡片 footer 加「详情」按钮**

在 `front/src/views/favorite/index.vue:73` 的 footer 内，「交易」按钮前插入：

```html
              <el-button size="small" @click="showDetail(item)">详情</el-button>
```

在 `</el-row>` 网格结束后、分页前（`front/src/views/favorite/index.vue:79` 附近）追加：

```html
    <ProductDetailDialog
      v-model="detailVisible"
      :product-id="selectedProductId"
      :fallback-name="detailFallbackName"
    />
```

- [ ] **Step 2: 扩展 `FavoriteItem` 携带 productId**

将接口声明（`front/src/views/favorite/index.vue:107-111`）改为：

```ts
interface FavoriteItem extends WeaUserFavorite {
  productName?: string
  productId?: number
  currentPrice?: number
  riseFallRate?: number
}
```

- [ ] **Step 3: `enrichFavorites` 记录 productId**

将 `productMap` 相关逻辑（`front/src/views/favorite/index.vue:126-143`）改为同时记录 id：

```ts
async function enrichFavorites(records: WeaUserFavorite[]): Promise<FavoriteItem[]> {
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
```

- [ ] **Step 4: 脚本加弹窗状态与 import**

在 `front/src/views/favorite/index.vue` import 段追加：

```ts
import ProductDetailDialog from '@/components/ProductDetailDialog.vue'
```

在 `router` 声明附近追加状态与函数：

```ts
const detailVisible = ref(false)
const selectedProductId = ref<number | null>(null)
const detailFallbackName = ref('')

function showDetail(item: FavoriteItem) {
  if (!item.productId) {
    ElMessage.warning('该产品信息缺失，请前往产品中心查看')
    return
  }
  detailFallbackName.value = item.productName || item.productCode
  selectedProductId.value = item.productId
  detailVisible.value = true
}
```

（`ElMessage` 已由本页 import，无需新增。）

- [ ] **Step 5: 构建验证**

```bash
cd front && npm run build
```

- [ ] **Step 6: 手动验收**
  自选页 → 卡片「详情」→ 弹窗显示产品实时信息；未添加产品 id 时给出提示。

---

### Task 5: A5 — 首页/个人中心卡片深链

**Files:**
- Modify: `front/src/views/home/index.vue`
- Modify: `front/src/views/dashboard/index.vue`

#### 5a. 首页 `home/index.vue`

- [ ] **Step 1: 产品卡点击改开弹窗**

将 `front/src/views/home/index.vue:76` 的 `@click="router.push('/products')"` 改为 `@click="showProductDetail(item)"`。

- [ ] **Step 2: 资讯条目点击改开弹窗**

将 `front/src/views/home/index.vue:149` 的 `@click="router.push('/news')"` 改为 `@click="showNewsDetail(item)"`。

- [ ] **Step 3: 模板尾部追加两个弹窗**

在 `</template>` 根节点结束前（`home/index.vue:160` 附近）追加：

```html
    <ProductDetailDialog
      v-model="productDetailVisible"
      :product-id="selectedProductId"
      :fallback-name="selectedProductName"
    />
    <NewsDetailDialog
      v-model="newsDetailVisible"
      :news-id="selectedNewsId"
    />
```

- [ ] **Step 4: 脚本加 import 与状态**

```ts
import ProductDetailDialog from '@/components/ProductDetailDialog.vue'
import NewsDetailDialog from '@/components/NewsDetailDialog.vue'
```

```ts
const productDetailVisible = ref(false)
const selectedProductId = ref<number | null>(null)
const selectedProductName = ref('')
const newsDetailVisible = ref(false)
const selectedNewsId = ref<number | null>(null)

function showProductDetail(item: WeaProduct) {
  selectedProductName.value = item.productName
  selectedProductId.value = item.id ?? null
  productDetailVisible.value = true
}

function showNewsDetail(item: WeaNews) {
  selectedNewsId.value = item.id ?? null
  newsDetailVisible.value = true
}
```

#### 5b. 个人中心 `dashboard/index.vue`

- [ ] **Step 5: 产品卡点击改开弹窗**

将 `front/src/views/dashboard/index.vue:149` 的 `@click="router.push('/products')"` 改为 `@click="showProductDetail(item)"`。

- [ ] **Step 6: 资讯条目点击改开弹窗**

将 `front/src/views/dashboard/index.vue:190` 的 `@click="router.push('/news')"` 改为 `@click="showNewsDetail(item)"`。

- [ ] **Step 7: 模板尾部追加两个弹窗**

在 `dashboard/index.vue` 根节点结束前（`:197` `</template>` 前）追加：

```html
    <ProductDetailDialog
      v-model="productDetailVisible"
      :product-id="selectedProductId"
      :fallback-name="selectedProductName"
    />
    <NewsDetailDialog
      v-model="newsDetailVisible"
      :news-id="selectedNewsId"
    />
```

- [ ] **Step 8: 脚本加 import 与状态**

```ts
import ProductDetailDialog from '@/components/ProductDetailDialog.vue'
import NewsDetailDialog from '@/components/NewsDetailDialog.vue'
```

```ts
const productDetailVisible = ref(false)
const selectedProductId = ref<number | null>(null)
const selectedProductName = ref('')
const newsDetailVisible = ref(false)
const selectedNewsId = ref<number | null>(null)

function showProductDetail(item: WeaProduct) {
  selectedProductName.value = item.productName
  selectedProductId.value = item.id ?? null
  productDetailVisible.value = true
}

function showNewsDetail(item: WeaNews) {
  selectedNewsId.value = item.id ?? null
  newsDetailVisible.value = true
}
```

- [ ] **Step 9: 构建验证**

```bash
cd front && npm run build
```

- [ ] **Step 10: 手动验收**
  首页产品卡点击 → 产品详情弹窗；资讯条目点击 → 资讯全文弹窗；个人中心（用户与管理员账号各验一次）同样生效；原有「查看更多 →」跳列表页不受影响。

---

### Task 6: A2 — 交易页委托单详情弹窗

**Files:**
- Modify: `front/src/views/trade/index.vue`

- [ ] **Step 1: 操作列加「详情」按钮**

将 `front/src/views/trade/index.vue:129-142` 的操作列改为（撤销按钮前加详情按钮）：

```html
            <el-table-column label="操作" width="130" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="row.orderStatus === 0"
                  text
                  type="danger"
                  size="small"
                  @click="handleCancel(row)"
                >
                  撤销
                </el-button>
                <el-button text type="primary" size="small" @click="handleDetail(row)">详情</el-button>
              </template>
            </el-table-column>
```

- [ ] **Step 2: 表格后追加详情弹窗**

在 `front/src/views/trade/index.vue:157` 分页容器后、根节点结束前追加：

```html
    <!-- 委托单详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="`委托单 ${detailItem?.orderNo || ''}`" width="480" destroy-on-close>
      <div v-loading="detailLoading" class="order-detail-body">
        <template v-if="detailItem">
          <div class="od-row">
            <span class="od-label">订单号</span>
            <span class="od-value code">{{ detailItem.orderNo }}</span>
          </div>
          <div class="od-row">
            <span class="od-label">产品代码</span>
            <span class="od-value code">{{ detailItem.productCode }}</span>
          </div>
          <div class="od-row">
            <span class="od-label">交易方向</span>
            <el-tag :type="detailItem.tradeType === 1 ? 'danger' : 'success'" size="small" effect="plain">
              {{ tradeTypeText(detailItem.tradeType) }}
            </el-tag>
          </div>
          <div class="od-row">
            <span class="od-label">委托价格</span>
            <span class="od-value price">{{ formatPrice(detailItem.entrustPrice) }}</span>
          </div>
          <div class="od-row">
            <span class="od-label">委托数量</span>
            <span class="od-value">{{ detailItem.entrustNum }}</span>
          </div>
          <div class="od-row">
            <span class="od-label">委托金额</span>
            <span class="od-value price">
              {{ detailItem.entrustPrice != null && detailItem.entrustNum != null
                ? formatPrice(detailItem.entrustPrice * detailItem.entrustNum)
                : '-' }}
            </span>
          </div>
          <div class="od-row">
            <span class="od-label">订单状态</span>
            <el-tag :type="orderStatusTag(detailItem.orderStatus)" size="small">
              {{ orderStatusText(detailItem.orderStatus) }}
            </el-tag>
          </div>
          <div class="od-row">
            <span class="od-label">下单时间</span>
            <span class="od-value">{{ formatDateTime(detailItem.createTime) }}</span>
          </div>
        </template>
        <el-empty v-else-if="!detailLoading" description="订单不存在" :image-size="64" />
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
```

- [ ] **Step 3: 脚本加状态与处理函数**

import 段追加 `getTradeOrderById`：

```ts
import { getTradeOrderPage, createTradeOrder, getTradeOrderById, cancelTradeOrder } from '@/api/trade'
```

在 `cancelTradeOrder` 相关声明处附近追加：

```ts
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailItem = ref<WeaTradeOrder | null>(null)

async function handleDetail(order: WeaTradeOrder) {
  if (!order.id) return
  detailVisible.value = true
  detailLoading.value = true
  detailItem.value = null
  try {
    const res = await getTradeOrderById(order.id)
    detailItem.value = (res.data || null) as WeaTradeOrder | null
  } catch {
    detailItem.value = null
  } finally {
    detailLoading.value = false
  }
}
```

- [ ] **Step 4: 追加弹窗样式**

在 `<style scoped>` 内追加：

```css
.order-detail-body { min-height: 60px; padding: 4px 0; }
.od-row {
  display: flex;
  align-items: center;
  padding: 11px 0;
  border-bottom: 1px solid var(--border-color);
}
.od-row:last-child { border-bottom: none; }
.od-label { width: 90px; font-size: 14px; color: var(--text-secondary); flex-shrink: 0; }
.od-value { font-size: 14px; color: var(--text-primary); font-weight: 500; }
.od-value.code { font-family: 'DIN Pro', monospace; }
.od-value.price { font-weight: 700; font-family: 'DIN Pro', monospace; }
```

- [ ] **Step 5: 构建验证**

```bash
cd front && npm run build
```

- [ ] **Step 6: 手动验收**
  交易页 → 任一订单行「详情」→ 弹窗显示完整委托信息；撤销按钮仍可用。

---

### Task 7: A1 — 行情详情 + K 线弹窗

**Files:**
- Create: `front/src/views/market/MarketDetailDialog.vue`
- Modify: `front/src/views/market/index.vue`

- [ ] **Step 1: 新建 `MarketDetailDialog.vue`**

复用 `KlinePanel.vue`（其 `products`/`klineData`/`loadKline` props 与 `getDashboardKline` 数据形状一致）：

```vue
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
    klineData.value = (res.data?.candles || []) as Candle[]
  } catch {
    klineData.value = []
  }
}

watch(
  () => props.market,
  (m) => {
    if (m?.productCode) {
      klineData.value = []
      loadKline(m.productCode)
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
```

- [ ] **Step 2: 行情页表格行点击开弹窗**

`front/src/views/market/index.vue` 的 `<el-table>` 标签（`:27`）加 `@row-click`：

```html
        <el-table :data="marketList" stripe v-loading="loading" empty-text="暂无行情数据" @row-click="showDetail">
```

在模板根节点结束前追加：

```html
    <MarketDetailDialog
      v-model="detailVisible"
      :market="selectedMarket"
    />
```

- [ ] **Step 3: 脚本加状态与 import**

```ts
import MarketDetailDialog from './MarketDetailDialog.vue'
```

```ts
const detailVisible = ref(false)
const selectedMarket = ref<WeaMarketData | null>(null)

function showDetail(row: WeaMarketData) {
  selectedMarket.value = row
  detailVisible.value = true
}
```

- [ ] **Step 4: 构建验证**

```bash
cd front && npm run build
```
Expected: 通过。若 vue-tsc 对 `KlinePanel` props 类型（`load-kline` 为 `(code: string, period?: string) => Promise<void>`）报错，则调整 `loadKline` 签名与之对齐。

- [ ] **Step 5: 手动验收**
  实时行情页 → 点击任意行情行 → 弹窗显示快照 + K 线图；切换 1D/1W/1M 有数据；SSE 刷新不破坏弹窗。

---

### Task 8: A6 — 资讯中心接入 `NewsDetailDialog`

**Files:**
- Modify: `front/src/views/news/index.vue`

- [ ] **Step 1: 替换内联详情弹窗**

将 `front/src/views/news/index.vue:71-79` 的 `<el-dialog>` 替换为：

```html
    <!-- 详情弹窗（实时拉取） -->
    <NewsDetailDialog
      v-model="detailVisible"
      :news-id="selectedNewsId"
    />
```

- [ ] **Step 2: 脚本改造**

import 段改为：

```ts
import { ref, onMounted } from 'vue'
import { getNewsPage } from '@/api/message'
import { NEWS_TYPE_OPTIONS } from '@/types'
import { formatDateTime, newsTypeText } from '@/utils/format'
import NewsDetailDialog from '@/components/NewsDetailDialog.vue'
import type { WeaNews } from '@/types'
```

删除 `detailItem`，改为：

```ts
const detailVisible = ref(false)
const selectedNewsId = ref<number | null>(null)

function showDetail(item: WeaNews) {
  selectedNewsId.value = item.id ?? null
  detailVisible.value = true
}
```

模板中 `showDetail(item)` 调用保持不变。

- [ ] **Step 3: 清理不再使用的样式**
  删除 `.detail-meta` / `.detail-source` / `.detail-time` / `.detail-content` 样式（弹窗样式已移至共享组件；若保留无引用可一并清理）。

- [ ] **Step 4: 构建验证**

```bash
cd front && npm run build
```

- [ ] **Step 5: 手动验收**
  财经资讯页 → 点击资讯条目 → 弹窗实时拉取全文；分类筛选正常。

---

### Task 9: 全量回归验收

**Files:**
- None（仅验证）

- [ ] **Step 1: 全量构建**

```bash
cd front && npm run build
```
Expected: 通过。

- [ ] **Step 2: 手工回归清单**

`npx vite`，普通用户 + 管理员各走一遍：

- 公共路由（未登录）：`/products` 点产品卡 → 弹窗出现且详情接口 401 → 按全局拦截器跳登录（预期行为，不报未捕获错误）。
- `/news`、`/market` 未登录访问：列表正常，详情弹窗触发 401 跳登录。
- 登录后：产品/资讯/消息/行情/自选/交易 六页详情弹窗全部可用。
- 回归原功能：行情 SSE 实时刷新；产品收藏/取消、去交易跳转；消息已读/全部已读；交易下单/撤销；自选添加/删除；首页/个人中心「查看更多 →」跳转。

- [ ] **Step 3: 生成变更摘要**
  汇总本批新增 3 个组件、修改 6 个页面；确认无 `wealth-service` / `wealth-common` 改动。

---

## Self-Review 记录

- **Spec 覆盖**：A1→Task7、A2→Task6、A3→Task3、A4→Task4、A5→Task5、A6→Task8，共享组件→Task1/2，全量验证→Task9。无缺口。
- **占位符**：无 TBD/TODO；每个代码步骤含完整实现。
- **类型一致性**：`ProductDetailDialog` props `{ modelValue, productId, fallbackName? }`、`NewsDetailDialog` props `{ modelValue, newsId }`、`MarketDetailDialog` props `{ modelValue, market }` 在各接入页面一致使用；`KlinePanel` 复用保持其 props 契约。
