# 骨架屏实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 首页行情数据改为 API 动态加载 + 骨架屏，个人资料页补充骨架屏

**Architecture:** 使用 Element Plus `<el-skeleton animated>` 组件（已在产品中心页使用），保持项目风格一致。首页行情区从硬编码改为调用 `getMarketDataList()` API；个人资料页在 API 加载期间展示骨架屏代替空表单。

**Tech Stack:** Vue 3 + Element Plus + TypeScript

---

### Task 1: 首页行情数据 API 化 + 骨架屏

**Files:**
- Modify: `front/src/views/home/index.vue`

**改动说明：**
- 移除硬编码的 `marketItems` 数组
- 增加 `loading` 状态和 `marketList` 响应式数据
- `onMounted` 中调用 `getMarketDataList()` 获取行情数据
- 添加三种状态渲染：loading（骨架屏）、empty（空状态）、data（行情卡片）
- 骨架屏使用 `<el-skeleton>` 自定义 template，模拟 4 张行情卡片布局

- [ ] **Step 1: 修改 `<script>` 部分——替换硬编码数据为 API 调用**

删除旧代码（`marketItems` 常量 + 生成的静态数据），替换为：

```vue
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/index'
import { TrendCharts, Coin, DataLine, Star } from '@element-plus/icons-vue'
import { getMarketDataList } from '@/api/product'
import { formatPrice, formatRate } from '@/utils/format'
import type { WeaMarketData } from '@/types'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const marketList = ref<WeaMarketData[]>([])

const features = [
  { title: '丰富产品', desc: '覆盖基金、理财、债券等多种投资产品，满足不同风险偏好', icon: Coin, bg: 'linear-gradient(135deg, #1a6dff, #0a4dcc)' },
  { title: '实时行情', desc: '毫秒级行情推送，掌握市场动态，把握投资时机', icon: TrendCharts, bg: 'linear-gradient(135deg, #34c759, #28a745)' },
  { title: '数据分析', desc: '专业的走势图表和数据分析工具，辅助投资决策', icon: DataLine, bg: 'linear-gradient(135deg, #ff9500, #e68a00)' },
  { title: '智能推荐', desc: '基于您的风险偏好和投资习惯，智能推荐合适产品', icon: Star, bg: 'linear-gradient(135deg, #8e44ad, #6c3483)' },
]

async function fetchMarketData() {
  loading.value = true
  try {
    const res = await getMarketDataList()
    marketList.value = (res.data || []) as WeaMarketData[]
  } catch {
    marketList.value = []
  } finally {
    loading.value = false
  }
}

onMounted(fetchMarketData)
</script>
```

注意：`<script>` 中删除 `import { ref } from 'vue'` 的改动——原来的代码没有 `ref` 导入（静态数据不需要），现在需要加上。当前文件没有 `import { ref }`，所以新增导入。

- [ ] **Step 2: 修改 `<template>`——替换行情区块为三态渲染**

原行情区块（`.market-section` 的 `el-row` 部分）替换为：

```vue
    <!-- 行情简报 -->
    <section class="market-section">
      <div class="section-header">
        <h2 class="section-title">实时行情</h2>
        <el-button text type="primary" @click="router.push('/market')">查看更多</el-button>
      </div>

      <!-- 骨架屏 -->
      <el-row :gutter="16" v-if="loading">
        <el-col :xs="12" :sm="6" v-for="i in 4" :key="i">
          <el-card shadow="never" class="market-card">
            <el-skeleton animated :loading="true">
              <template #template>
                <div class="skeleton-market">
                  <el-skeleton-item variant="text" style="width: 40%; height: 14px; margin: 0 auto 12px;" />
                  <el-skeleton-item variant="text" style="width: 65%; height: 24px; margin: 0 auto 8px;" />
                  <el-skeleton-item variant="text" style="width: 45%; height: 16px; margin: 0 auto;" />
                </div>
              </template>
            </el-skeleton>
          </el-card>
        </el-col>
      </el-row>

      <!-- 空状态 -->
      <el-row :gutter="16" v-else-if="marketList.length === 0">
        <el-col :span="24">
          <el-empty description="暂无行情数据" />
        </el-col>
      </el-row>

      <!-- 行情卡片 -->
      <el-row :gutter="16" v-else>
        <el-col :xs="12" :sm="6" v-for="item in marketList" :key="item.productCode">
          <el-card shadow="never" class="market-card">
            <div class="market-name">{{ item.productCode }}</div>
            <div class="market-price">{{ formatPrice(item.currentPrice) }}</div>
            <div class="market-change" :class="(item.riseFallRate || 0) >= 0 ? 'rise' : 'fall'">
              {{ formatRate(item.riseFallRate) }}
            </div>
          </el-card>
        </el-col>
      </el-row>
    </section>
```

- [ ] **Step 3: 添加骨架屏样式**

在 `<style scoped>` 末尾添加：

```css
/* Skeleton */
.skeleton-market {
  text-align: center;
  padding: 12px 0;
}
```

- [ ] **Step 4: 验证编译通过**

Run: `cd front && npx vue-tsc --noEmit 2>&1 || echo "Type check done (may need full build)"`

---

### Task 2: 个人资料页骨架屏

**Files:**
- Modify: `front/src/views/profile/index.vue`

**改动说明：**
- 增加 `loading` 状态，初始为 `true`
- `fetchProfile` 和 `fetchStats` 两个异步函数完成后统一关闭 loading
- 左侧头像卡和右侧表单卡分别添加 skeleton 模板
- 安全设置卡保持静态不变

- [ ] **Step 1: 修改 `<script>` 部分——增加 loading 状态和完成回调**

在 `const saving = ref(false)` 之后添加：

```vue
const loading = ref(true)
```

将 `onMounted` 改为：

```vue
onMounted(async () => {
  await Promise.all([fetchProfile(), fetchStats()])
  loading.value = false
})
```

将 `fetchProfile` 和 `fetchStats` 改为返回 Promise（加 return），以便 `Promise.all` 等待：

```vue
async function fetchProfile() {
  userInfo.username = userStore.username
  userInfo.nickname = userStore.nickname
  if (!userStore.userId) return
  try {
    const res = await getUserInfo(userStore.userId)
    const data = res.data
    if (data) {
      userInfo.username = data.username || userStore.username
      userInfo.nickname = data.nickname || userStore.nickname
      userInfo.phone = data.phone || ''
    }
  } catch { /* use store defaults */ }
}

async function fetchStats() {
  if (!userStore.userId) return
  try {
    const [fr, tr, mr] = await Promise.all([
      getFavoritePage({ pageNum: 1, pageSize: 1, userId: userStore.userId }),
      getTradeOrderPage({ pageNum: 1, pageSize: 1, userId: userStore.userId }),
      getMessagePage({ pageNum: 1, pageSize: 1, userId: userStore.userId }),
    ])
    statFavorites.value = fr.data?.total || 0
    statOrders.value = tr.data?.total || 0
    statMessages.value = mr.data?.total || 0
  } catch { /* ignore */ }
}
```

- [ ] **Step 2: 修改 `<template>`——左侧头像卡添加骨架屏**

原 `.profile-card` 区域：

```diff
         <el-card class="profile-card" shadow="never">
-          <div class="profile-header">
-            <el-avatar :size="72" :icon="UserFilled" class="profile-avatar" />
-            <h2 class="profile-name">{{ userInfo.nickname || userInfo.username }}</h2>
-            <p class="profile-username">@{{ userInfo.username }}</p>
-          </div>
-          <el-divider />
-          <div class="profile-stats">
-            <div class="stat-item">
-              <span class="stat-num">{{ statFavorites }}</span>
-              <span class="stat-lbl">自选</span>
-            </div>
-            <div class="stat-item">
-              <span class="stat-num">{{ statOrders }}</span>
-              <span class="stat-lbl">委托单</span>
-            </div>
-            <div class="stat-item">
-              <span class="stat-num">{{ statMessages }}</span>
-              <span class="stat-lbl">消息</span>
-            </div>
-          </div>
+          <el-skeleton animated :loading="loading">
+            <template #template>
+              <div class="profile-skeleton">
+                <div class="skeleton-avatar-wrap">
+                  <el-skeleton-item variant="circle" style="width: 72px; height: 72px; margin: 0 auto;" />
+                </div>
+                <el-skeleton-item variant="text" style="width: 50%; height: 20px; margin: 16px auto 4px;" />
+                <el-skeleton-item variant="text" style="width: 35%; height: 14px; margin: 0 auto;" />
+                <el-skeleton-item variant="text" style="width: 100%; height: 1px; margin: 16px 0;" />
+                <div class="skeleton-stats">
+                  <div v-for="i in 3" :key="i" class="skeleton-stat-item">
+                    <el-skeleton-item variant="text" style="width: 28px; height: 22px; margin: 0 auto;" />
+                    <el-skeleton-item variant="text" style="width: 28px; height: 13px; margin: 4px auto 0;" />
+                  </div>
+                </div>
+              </div>
+            </template>
+            <template #default>
+              <div class="profile-header">
+                <el-avatar :size="72" :icon="UserFilled" class="profile-avatar" />
+                <h2 class="profile-name">{{ userInfo.nickname || userInfo.username }}</h2>
+                <p class="profile-username">@{{ userInfo.username }}</p>
+              </div>
+              <el-divider />
+              <div class="profile-stats">
+                <div class="stat-item">
+                  <span class="stat-num">{{ statFavorites }}</span>
+                  <span class="stat-lbl">自选</span>
+                </div>
+                <div class="stat-item">
+                  <span class="stat-num">{{ statOrders }}</span>
+                  <span class="stat-lbl">委托单</span>
+                </div>
+                <div class="stat-item">
+                  <span class="stat-num">{{ statMessages }}</span>
+                  <span class="stat-lbl">消息</span>
+                </div>
+              </div>
+            </template>
+          </el-skeleton>
         </el-card>
```

- [ ] **Step 3: 修改 `<template>`——右侧表单卡添加骨架屏**

原 `.info-card` 区域：

```diff
         <el-card class="info-card" shadow="never">
           <template #header>个人信息</template>
-          <el-form :model="userInfo" label-width="80px" size="large">
-            <el-form-item label="用户名">
-              <el-input v-model="userInfo.username" disabled />
-            </el-form-item>
-            <el-form-item label="昵称">
-              <el-input v-model="userInfo.nickname" placeholder="设置昵称" />
-            </el-form-item>
-            <el-form-item label="手机号">
-              <el-input v-model="userInfo.phone" placeholder="绑定手机号" />
-            </el-form-item>
-            <el-form-item>
-              <el-button type="primary" :loading="saving" @click="handleSave">
-                {{ saving ? '保存中...' : '保存修改' }}
-              </el-button>
-            </el-form-item>
-          </el-form>
+          <el-skeleton animated :loading="loading">
+            <template #template>
+              <div class="form-skeleton">
+                <div v-for="i in 3" :key="i" class="form-skeleton-row">
+                  <el-skeleton-item variant="text" style="width: 60px; height: 14px;" />
+                  <el-skeleton-item variant="text" style="width: 100%; height: 32px;" />
+                </div>
+                <el-skeleton-item variant="button" style="width: 100px; height: 36px; margin-top: 8px;" />
+              </div>
+            </template>
+            <template #default>
+              <el-form :model="userInfo" label-width="80px" size="large">
+                <el-form-item label="用户名">
+                  <el-input v-model="userInfo.username" disabled />
+                </el-form-item>
+                <el-form-item label="昵称">
+                  <el-input v-model="userInfo.nickname" placeholder="设置昵称" />
+                </el-form-item>
+                <el-form-item label="手机号">
+                  <el-input v-model="userInfo.phone" placeholder="绑定手机号" />
+                </el-form-item>
+                <el-form-item>
+                  <el-button type="primary" :loading="saving" @click="handleSave">
+                    {{ saving ? '保存中...' : '保存修改' }}
+                  </el-button>
+                </el-form-item>
+              </el-form>
+            </template>
+          </el-skeleton>
         </el-card>
```

- [ ] **Step 4: 添加骨架屏样式**

在 `<style scoped>` 末尾添加：

```css
/* Skeleton */
.profile-skeleton {
  text-align: center;
  padding: 20px 0 8px;
}
.skeleton-avatar-wrap {
  display: flex;
  justify-content: center;
}
.skeleton-stats {
  display: flex;
  justify-content: space-around;
  padding: 8px 0;
}
.skeleton-stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.form-skeleton {
  padding: 8px 0;
}
.form-skeleton-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 18px;
}
```

- [ ] **Step 5: 验证编译通过**

Run: `cd front && npx vue-tsc --noEmit 2>&1 || echo "check done"`

---

### 验证清单

- [ ] 首页 `onMounted` 时显示 4 张骨架卡片（扫光动画）
- [ ] API 返回后骨架屏消失，显示真实行情卡片
- [ ] API 返回空时显示「暂无行情数据」空状态
- [ ] API 异常时显示空状态（静默降级）
- [ ] 个人资料页 `onMounted` 时显示左侧头像骨架 + 右侧表单骨架
- [ ] 两个 API 都完成后骨架统一消失，显示完整内容
- [ ] 安全设置卡始终可见，不受 loading 影响
- [ ] 编译无错误
