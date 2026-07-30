# 全量查询分页优化 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 4 处前端全量列表查询替换为已有分页接口，消除数据增长后的性能风险

**Architecture:** 纯前端改造，后端不动。4 个独立文件各自将 `getXxxList()` 替换为 `getXxxPage()`，仅修改 API 调用方式，不改变模板、样式、功能逻辑

**Tech Stack:** Vue 3, TypeScript, Axios

---

### Task 1: 首页行情 — `home/index.vue`

**Files:**
- Modify: `front/src/views/home/index.vue:95,132-136`

- [ ] **Step 1: 替换 import**

  将 `getMarketDataList` 替换为 `getMarketDataPage`：

  ```typescript
  import { getMarketDataPage } from '@/api/product'
  ```

- [ ] **Step 2: 替换调用**

  将 `fetchMarketData` 中的全量查询改为分页查询 pageSize=4：

  ```typescript
  async function fetchMarketData() {
    loading.value = true
    try {
      const res = await getMarketDataPage({ pageNum: 1, pageSize: 4 })
      marketList.value = (res.data?.records || []) as WeaMarketData[]
    } catch (err) {
      console.warn('[home] fetchMarketData 失败:', err)
      marketList.value = []
    } finally {
      loading.value = false
    }
  }
  ```

- [ ] **Step 3: 验证**

  Run: `cd front && npx vue-tsc --noEmit`
  Expected: 0 errors

  Run: `cd front && npx vite build`
  Expected: build 成功

- [ ] **Step 4: Commit**

  ```bash
  git add front/src/views/home/index.vue
  git commit -m "fix(front): 首页行情改用分页查询 pageSize=4"
  ```

---

### Task 2: 用户仪表盘 — `dashboard/index.vue`

**Files:**
- Modify: `front/src/views/dashboard/index.vue:92,106,124-135`

- [ ] **Step 1: 替换 import**

  将 `getProductList` 替换为 `getProductPage`：

  ```typescript
  import { getProductPage } from '@/api/product'
  ```

- [ ] **Step 2: 移除 `hotProducts` computed**

  `hotProducts` 原是 `products.value.slice(0, 8)`，改为分页后 products 直接就是 8 条，不再需要切片，模板直接引用 `products` 即可。

  改 `hotProducts` 为 `products`：

  ```diff
  - const hotProducts = computed(() => products.value.slice(0, 8))
  ```

  模板中 `:data="hotProducts"` → `:data="products"`

- [ ] **Step 3: 调整 `marketStats`**

  统计指标不再依赖全量数据，改为基于当前页计算。文案调整：

  ```typescript
  const marketStats = computed(() => [
    { label: '展示产品', value: products.value.length, change: 0, icon: Coin, color: '#1a6dff' },
    { label: '上涨产品', value: products.value.filter(p => (p.riseFallRate || 0) > 0).length, change: 0, icon: TrendCharts, color: '#34c759' },
    { label: '下跌产品', value: products.value.filter(p => (p.riseFallRate || 0) < 0).length, change: 0, icon: DataLine, color: '#ff3b30' },
    { label: '在售产品', value: products.value.filter(p => p.status === 1).length, change: 0, icon: Star, color: '#ff9500' },
  ])
  ```

- [ ] **Step 4: 替换调用**

  将 `fetchProducts` 中的全量查询改为分页查询 pageSize=8：

  ```typescript
  async function fetchProducts() {
    hasError.value = false
    loading.value = true
    try {
      const res = await getProductPage({ pageNum: 1, pageSize: 8 })
      products.value = (res.data?.records || []) as WeaProduct[]
    } catch {
      hasError.value = true
      products.value = []
    } finally {
      loading.value = false
    }
  }
  ```

- [ ] **Step 5: 移除不再使用的 import**

  `computed` 如果只在 `marketStats` 中使用则仍需要，否则清理。
  模板如果不再使用 `computed` 相关的 `change` 字段，检查是否可移除不影响样式。
  
  注意 `computed` 仍用于 `marketStats`，保留。

- [ ] **Step 6: 验证**

  Run: `cd front && npx vue-tsc --noEmit`
  Expected: 0 errors

  Run: `cd front && npx vite build`
  Expected: build 成功

- [ ] **Step 7: Commit**

  ```bash
  git add front/src/views/dashboard/index.vue
  git commit -m "fix(front): 用户仪表盘产品列表改用分页查询 pageSize=8"
  ```

---

### Task 3: 管理端仪表盘 — `useAdminDashboard.ts`

**Files:**
- Modify: `front/src/composables/useAdminDashboard.ts:5,49-52`
- 调用方: `front/src/views/admin/dashboard/index.vue`（无改动，仅消费返回值）

- [ ] **Step 1: 替换 import**

  将 `getProductList` 替换为 `getProductPage`：

  ```typescript
  import { getProductPage } from '@/api/product'
  ```

- [ ] **Step 2: 替换调用**

  将 `loadProducts` 中的全量查询改为分页查询 pageSize=200：

  ```typescript
  async function loadProducts() {
    const res = await getProductPage({ pageNum: 1, pageSize: 200 })
    products.value = (res.data?.records || []) as WeaProduct[]
  }
  ```

- [ ] **Step 3: 验证**

  Run: `cd front && npx vue-tsc --noEmit`
  Expected: 0 errors

  Run: `cd front && npx vite build`
  Expected: build 成功

- [ ] **Step 4: Commit**

  ```bash
  git add front/src/composables/useAdminDashboard.ts
  git commit -m "fix(front): 管理端仪表盘产品列表改用分页查询 pageSize=200"
  ```

---

### Task 4: 产品页收藏 — `products/index.vue`

**Files:**
- Modify: `front/src/views/products/index.vue:151,232-247`

- [ ] **Step 1: 替换 import**

  将 `getFavoriteList` 替换为 `getFavoritePage`：

  ```typescript
  import { createFavorite, getFavoritePage, deleteFavorite } from '@/api/favorite'
  ```

- [ ] **Step 2: 替换调用**

  将 `fetchFavorites` 中的全量查询改为分页查询 pageSize=200：

  ```typescript
  async function fetchFavorites() {
    if (!userStore.userId) {
      favoritedMap.value = {}
      return
    }
    try {
      const res = await getFavoritePage({ pageNum: 1, pageSize: 200, userId: userStore.userId })
      const map: Record<string, number> = {}
      for (const fav of (res.data?.records || []) as WeaUserFavorite[]) {
        if (fav.productCode) map[fav.productCode] = fav.id!
      }
      favoritedMap.value = map
    } catch (err) {
      console.warn('[products] fetchFavorites 失败:', err)
    }
  }
  ```

- [ ] **Step 3: 验证**

  Run: `cd front && npx vue-tsc --noEmit`
  Expected: 0 errors

  Run: `cd front && npx vite build`
  Expected: build 成功

- [ ] **Step 4: Commit**

  ```bash
  git add front/src/views/products/index.vue
  git commit -m "fix(front): 产品页收藏改用分页查询 pageSize=200"
  ```

---

### Task 5: 全量验证

**Files:** 无修改

- [ ] **Step 1: 全量编译 + 类型检查**

  Run: `cd front && npx vue-tsc --noEmit && npx vite build`
  Expected: 类型检查 0 errors，build 成功

- [ ] **Step 2: 验收清单**

  - [ ] 首页展示 4 张行情卡片
  - [ ] 用户仪表盘展示 8 条热销产品
  - [ ] 管理端仪表盘正常加载产品列表
  - [ ] 产品页收藏按钮状态正常
  - [ ] 无 `sse.ts`、`createMarketSSE`、`onMarketUpdate` 残留引用
  - [ ] 无 `nextTick` 残留 import
