# 前端重构 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 `front/` 项目 UI 重构为深蓝色侧边栏 + 标签页切换的 Stitch 设计风格

**Architecture:** 保留 `api/` `types/` `utils/` `store/` `router/` 不动，重写 `layout/` `styles/`，每个视图页只改配色样式，新增 2 个关联页面和 TagsView 组件

**Tech Stack:** Vue 3 + Element Plus + TypeScript + Pinia + Vue Router

---

### Task 1: 新建深色主题 CSS 文件

**Files:**
- Create: `front/src/styles/theme.css`
- Delete: `front/src/styles/light-theme.css`

- [ ] **Step 1: Create new theme.css**

```css
:root {
  --fl-sidebar-bg: #1a365d;
  --fl-sidebar-text: #ffffffb3;
  --fl-sidebar-active: #ffffff;
  --fl-sidebar-active-bg: #2563eb;
  --fl-sidebar-hover-bg: rgba(255,255,255,0.08);
  --fl-sidebar-width: 220px;
  --fl-sidebar-collapsed-width: 64px;

  --fl-header-bg: #ffffff;
  --fl-header-height: 56px;
  --fl-header-border: #e4e7ed;

  --fl-content-bg: #f0f2f5;
  --fl-card-bg: #ffffff;
  --fl-border: #e4e7ed;
  --fl-border-light: #dcdfe6;

  --fl-text: #303133;
  --fl-text-secondary: #606266;
  --fl-text-dim: #909399;
  --fl-text-placeholder: #c0c4cc;

  --fl-primary: #1a6dff;
  --fl-primary-hover: #4a8aff;
  --fl-primary-light: rgba(26, 109, 255, 0.08);

  --fl-rise: #e74c3c;
  --fl-fall: #27ae60;
  --fl-rise-bg: rgba(231, 76, 60, 0.08);
  --fl-fall-bg: rgba(39, 174, 96, 0.08);

  --fl-radius: 10px;
  --fl-radius-sm: 6px;
  --fl-shadow: 0 2px 12px rgba(0,0,0,0.06);
  --fl-shadow-hover: 0 4px 20px rgba(0,0,0,0.10);
}

/* ===== 卡片 ===== */
.fl-card {
  background: var(--fl-card-bg);
  border: 1px solid var(--fl-border);
  border-radius: var(--fl-radius);
  padding: 18px 20px;
  transition: box-shadow 0.25s ease;
}
.fl-card:hover {
  box-shadow: var(--fl-shadow-hover);
}
.fl-card-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--fl-text);
}

/* ===== 统计卡片 ===== */
.fl-stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.fl-stat-card {
  background: var(--fl-card-bg);
  border: 1px solid var(--fl-border);
  border-radius: var(--fl-radius);
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.25s ease;
}
.fl-stat-card:hover {
  box-shadow: var(--fl-shadow-hover);
  transform: translateY(-2px);
}
.fl-stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.fl-stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--fl-text);
  line-height: 1.2;
}
.fl-stat-label {
  font-size: 13px;
  color: var(--fl-text-dim);
  margin-bottom: 2px;
}

/* ===== 涨跌颜色 ===== */
.fl-rise { color: var(--fl-rise); }
.fl-fall { color: var(--fl-fall); }
.fl-rise-bg { background: var(--fl-rise-bg); }
.fl-fall-bg { background: var(--fl-fall-bg); }
.fl-text-primary { color: var(--fl-primary); }

/* ===== 分页 ===== */
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

/* ===== 页面标题 ===== */
.page-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--fl-text);
  margin: 0 0 16px 0;
}

/* ===== 搜索卡片 ===== */
.search-card {
  margin-bottom: 0;
}

/* ===== 滚动条 ===== */
::-webkit-scrollbar { width: 6px; height: 6px; }
::-webkit-scrollbar-track { background: transparent; }
::-webkit-scrollbar-thumb { background: #dcdfe6; border-radius: 3px; }
::-webkit-scrollbar-thumb:hover { background: #c0c4cc; }

/* ===== TagsView ===== */
.tags-view-container {
  background: #fff;
  border-bottom: 1px solid var(--fl-border);
  padding: 4px 8px 0;
}
.tags-view-container .el-tabs__header {
  margin: 0;
}
.tags-view-container .el-tabs__nav-wrap::after {
  height: 1px;
}
.tags-view-container .el-tabs__item {
  height: 32px;
  line-height: 32px;
  font-size: 12px;
  border: 1px solid var(--fl-border);
  border-bottom: none;
  border-radius: 4px 4px 0 0;
  margin-right: 4px;
  padding: 0 14px;
}
.tags-view-container .el-tabs__item.is-active {
  background: var(--fl-content-bg);
  border-bottom-color: var(--fl-content-bg);
  color: var(--fl-primary);
}
```

- [ ] **Step 2: Delete old theme file**

Delete `front/src/styles/light-theme.css`.

- [ ] **Step 3: Commit**

```bash
git add front/src/styles/theme.css front/src/styles/light-theme.css
git commit -m "feat: 创建深色主题 CSS 文件，替换旧 light-theme"
```

---

### Task 2: TagsView 组件 + Store

**Files:**
- Create: `front/src/store/app.ts`
- Create: `front/src/components/TagsView.vue`

- [ ] **Step 1: Create app store for tags**

```typescript
// front/src/store/app.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { RouteLocationNormalized } from 'vue-router'

export const useAppStore = defineStore('app', () => {
  const visitedViews = ref<RouteLocationNormalized[]>([])
  const cachedViews = ref<string[]>([])

  const visitedViewTitles = computed(() =>
    visitedViews.value.map(v => v.meta?.title as string || v.name as string || '')
  )

  function addView(view: RouteLocationNormalized) {
    if (visitedViews.value.some(v => v.path === view.path)) return
    visitedViews.value.push(view)
    if (view.name) cachedViews.value.push(view.name as string)
  }

  function removeView(view: RouteLocationNormalized) {
    const i = visitedViews.value.findIndex(v => v.path === view.path)
    if (i > -1) visitedViews.value.splice(i, 1)
    if (view.name) {
      const j = cachedViews.value.indexOf(view.name as string)
      if (j > -1) cachedViews.value.splice(j, 1)
    }
  }

  function closeOtherViews(view: RouteLocationNormalized) {
    visitedViews.value = visitedViews.value.filter(v => v.path === view.path)
    if (view.name) {
      cachedViews.value = [view.name as string]
    }
  }

  function closeAllViews() {
    visitedViews.value = []
    cachedViews.value = []
  }

  return { visitedViews, cachedViews, visitedViewTitles, addView, removeView, closeOtherViews, closeAllViews }
})
```

- [ ] **Step 2: Create TagsView.vue**

```vue
<!-- front/src/components/TagsView.vue -->
<template>
  <div class="tags-view-container">
    <el-tabs
      v-model="activeTab"
      type="card"
      closable
      @tab-remove="removeTab"
      @tab-click="switchTab"
      @contextmenu.prevent="handleContextMenu"
    >
      <el-tab-pane
        v-for="view in visitedViews"
        :key="view.path"
        :label="view.meta?.title as string || '未命名'"
        :name="view.path"
      />
    </el-tabs>
    <ul v-if="menuVisible" class="tags-view-menu" :style="menuStyle">
      <li @click="closeCurrent">关闭当前</li>
      <li @click="closeOthers">关闭其他</li>
      <li @click="closeAll">关闭全部</li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/store/app'
import type { RouteLocationNormalized } from 'vue-router'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const visitedViews = computed(() => appStore.visitedViews)
const activeTab = ref('')
const menuVisible = ref(false)
const menuStyle = ref({ left: '0px', top: '0px' })

watch(route, (to) => {
  appStore.addView(to)
  activeTab.value = to.path
}, { immediate: true })

function switchTab(tab: any) {
  const path = tab.props.name
  router.push(path)
}

function removeTab(path: string) {
  const view = visitedViews.value.find(v => v.path === path)
  if (!view) return
  appStore.removeView(view)
  if (activeTab.value === path) {
    const last = visitedViews.value[visitedViews.value.length - 1]
    if (last) router.push(last.path)
  }
}

function handleContextMenu(e: MouseEvent) {
  const tabEl = (e.target as HTMLElement).closest('.el-tabs__item')
  if (!tabEl) return
  menuStyle.value = { left: `${e.clientX}px`, top: `${e.clientY}px` }
  menuVisible.value = true
  const hide = () => { menuVisible.value = false; document.removeEventListener('click', hide) }
  document.addEventListener('click', hide)
}

function closeCurrent() {
  removeTab(activeTab.value)
}

function closeOthers() {
  const current = visitedViews.value.find(v => v.path === activeTab.value)
  if (current) appStore.closeOtherViews(current)
}

function closeAll() {
  appStore.closeAllViews()
  router.push('/dashboard')
  menuVisible.value = false
}
</script>

<style scoped>
.tags-view-container {
  background: #fff;
  border-bottom: 1px solid var(--fl-border);
  padding: 4px 8px 0;
  position: relative;
}
.tags-view-container :deep(.el-tabs__header) {
  margin: 0;
}
.tags-view-container :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
}
.tags-view-container :deep(.el-tabs__item) {
  height: 32px;
  line-height: 32px;
  font-size: 12px;
  border: 1px solid var(--fl-border);
  border-bottom: none;
  border-radius: 4px 4px 0 0;
  margin-right: 4px;
  padding: 0 14px;
}
.tags-view-container :deep(.el-tabs__item.is-active) {
  background: var(--fl-content-bg);
  border-bottom-color: var(--fl-content-bg);
  color: var(--fl-primary);
}
.tags-view-menu {
  position: fixed;
  background: #fff;
  border: 1px solid var(--fl-border);
  border-radius: 6px;
  box-shadow: var(--fl-shadow-hover);
  list-style: none;
  margin: 0;
  padding: 4px 0;
  z-index: 3000;
  min-width: 120px;
}
.tags-view-menu li {
  padding: 8px 16px;
  font-size: 13px;
  cursor: pointer;
  color: var(--fl-text-secondary);
}
.tags-view-menu li:hover {
  background: var(--fl-primary-light);
  color: var(--fl-primary);
}
</style>
```

- [ ] **Step 3: Commit**

```bash
git add front/src/store/app.ts front/src/components/TagsView.vue
git commit -m "feat: 添加 TagsView 标签页组件和应用状态 store"
```

---

### Task 3: 重写 Sidebar 深色导航

**File:**
- Modify: `front/src/layout/Sidebar.vue` (全量重写)

- [ ] **Step 1: Rewrite Sidebar.vue**

```vue
<!-- front/src/layout/Sidebar.vue -->
<template>
  <div class="sidebar" :class="{ collapsed: isCollapsed }">
    <div class="sidebar-logo" @click="router.push('/dashboard')">
      <div class="logo-icon">
        <el-icon :size="24"><TrendCharts /></el-icon>
      </div>
      <span v-show="!isCollapsed" class="logo-text">财富管理平台</span>
    </div>

    <el-menu
      :default-active="activeMenu"
      router
      :collapse="isCollapsed"
      :collapse-transition="false"
      background-color="#1a365d"
      text-color="#ffffffb3"
      active-text-color="#ffffff"
    >
      <el-menu-item index="/dashboard">
        <el-icon><Odometer /></el-icon>
        <span>控制面板</span>
      </el-menu-item>

      <el-sub-menu index="system">
        <template #title>
          <el-icon><Setting /></el-icon>
          <span>系统管理</span>
        </template>
        <el-menu-item index="/system/admin">管理员管理</el-menu-item>
        <el-menu-item index="/system/role">角色管理</el-menu-item>
        <el-menu-item index="/system/resource">资源管理</el-menu-item>
        <el-menu-item index="/system/admin-role">管理员角色关联</el-menu-item>
        <el-menu-item index="/system/role-resource">角色资源关联</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="user-mgmt">
        <template #title>
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </template>
        <el-menu-item index="/user">用户列表</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="product-mgmt">
        <template #title>
          <el-icon><Goods /></el-icon>
          <span>产品管理</span>
        </template>
        <el-menu-item index="/product">产品列表</el-menu-item>
        <el-menu-item index="/market">行情数据</el-menu-item>
        <el-menu-item index="/favorite">用户自选</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="trade-mgmt">
        <template #title>
          <el-icon><List /></el-icon>
          <span>交易管理</span>
        </template>
        <el-menu-item index="/trade">交易委托</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="msg-mgmt">
        <template #title>
          <el-icon><Message /></el-icon>
          <span>消息管理</span>
        </template>
        <el-menu-item index="/message">站内消息</el-menu-item>
        <el-menu-item index="/news">财经资讯</el-menu-item>
      </el-sub-menu>

      <el-menu-item index="/search">
        <el-icon><Search /></el-icon>
        <span>产品搜索</span>
      </el-menu-item>
    </el-menu>

    <div class="sidebar-footer" v-show="!isCollapsed">
      <div class="user-info">
        <el-avatar :size="32" icon="UserFilled" />
        <div class="user-detail">
          <div class="user-name">{{ userStore.username || '管理员' }}</div>
          <div class="user-role">超级管理员</div>
        </div>
      </div>
      <el-button text size="small" class="logout-btn" @click="handleLogout">
        <el-icon><SwitchButton /></el-icon>
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store'
import {
  TrendCharts, Odometer, Setting, User, Goods, List,
  Message, Search, SwitchButton, UserFilled,
} from '@element-plus/icons-vue'

defineProps<{ isCollapsed: boolean }>()
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const activeMenu = computed(() => route.path)

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.sidebar {
  width: var(--fl-sidebar-width);
  height: 100vh;
  background: var(--fl-sidebar-bg);
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  overflow: hidden;
  flex-shrink: 0;
}
.sidebar.collapsed {
  width: var(--fl-sidebar-collapsed-width);
}
.sidebar-logo {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  border-bottom: 1px solid rgba(255,255,255,0.1);
  flex-shrink: 0;
}
.logo-icon {
  color: #fff;
  display: flex;
  align-items: center;
}
.logo-text {
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 1px;
  white-space: nowrap;
}
.sidebar :deep(.el-menu) {
  border-right: none !important;
  flex: 1;
  overflow-y: auto;
}
.sidebar :deep(.el-menu-item),
.sidebar :deep(.el-sub-menu__title) {
  height: 44px;
  line-height: 44px;
  margin: 2px 8px;
  border-radius: 6px;
  width: auto !important;
}
.sidebar :deep(.el-menu-item:hover),
.sidebar :deep(.el-sub-menu__title:hover) {
  background: var(--fl-sidebar-hover-bg) !important;
}
.sidebar :deep(.el-menu-item.is-active) {
  background: var(--fl-sidebar-active-bg) !important;
  color: #fff !important;
}
.sidebar :deep(.el-menu-item.is-active .el-icon) {
  color: #fff !important;
}
.sidebar :deep(.el-sub-menu .el-menu) {
  background: rgba(0,0,0,0.15) !important;
}
.sidebar :deep(.el-sub-menu .el-menu .el-menu-item) {
  padding-left: 48px !important;
  height: 38px;
  line-height: 38px;
}
.sidebar-footer {
  padding: 12px;
  border-top: 1px solid rgba(255,255,255,0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}
.user-detail {
  min-width: 0;
}
.user-name {
  color: #fff;
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.user-role {
  color: #ffffff80;
  font-size: 11px;
}
.logout-btn {
  color: #ffffff80 !important;
}
.logout-btn:hover {
  color: #fff !important;
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add front/src/layout/Sidebar.vue
git commit -m "feat: 重写 Sidebar 为深蓝主题配色，支持折叠"
```

---

### Task 4: 重写 Navbar

**File:**
- Modify: `front/src/layout/Navbar.vue` (全量重写)

- [ ] **Step 1: Rewrite Navbar.vue**

```vue
<!-- front/src/layout/Navbar.vue -->
<template>
  <header class="navbar">
    <div class="navbar-left">
      <el-button text class="collapse-btn" @click="$emit('toggle')">
        <el-icon :size="20"><Fold v-if="!collapsed" /><Expand v-else /></el-icon>
      </el-button>
      <el-breadcrumb>
        <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>{{ route.meta?.title as string || '' }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="navbar-right">
      <div class="search-box" @click="goSearch">
        <el-icon><Search /></el-icon>
        <span class="search-placeholder">搜索产品名称/编码...</span>
        <span class="search-shortcut">⌘K</span>
      </div>

      <el-badge :value="3" :hidden="false" class="notice-badge">
        <el-button text @click="showNotices = !showNotices">
          <el-icon :size="20"><Bell /></el-icon>
        </el-button>
      </el-badge>

      <el-dropdown trigger="click">
        <span class="user-dropdown">
          <el-avatar :size="28" icon="UserFilled" />
          <span class="username">{{ userStore.username || '管理员' }}</span>
          <el-icon><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="router.push('/dashboard')">
              <el-icon><User /></el-icon>个人信息
            </el-dropdown-item>
            <el-dropdown-item divided @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store'
import {
  Fold, Expand, Search, Bell, UserFilled,
  ArrowDown, User, SwitchButton,
} from '@element-plus/icons-vue'

defineProps<{ collapsed: boolean }>()
defineEmits<{ toggle: [] }>()

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const showNotices = ref(false)

function goSearch() {
  router.push('/search')
}

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.navbar {
  height: var(--fl-header-height);
  background: var(--fl-header-bg);
  border-bottom: 1px solid var(--fl-header-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  flex-shrink: 0;
}
.navbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.collapse-btn {
  font-size: 16px;
  color: var(--fl-text-secondary);
}
.navbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  background: #f5f7fa;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--fl-text-dim);
}
.search-box:hover {
  background: #eef1f6;
}
.search-placeholder {
  font-size: 13px;
  white-space: nowrap;
}
.search-shortcut {
  font-size: 11px;
  padding: 1px 6px;
  background: #e4e7ed;
  border-radius: 4px;
  color: var(--fl-text-placeholder);
}
.notice-badge {
  line-height: 1;
}
.user-dropdown {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background 0.2s;
}
.user-dropdown:hover {
  background: #f5f7fa;
}
.username {
  font-size: 13px;
  color: var(--fl-text-secondary);
  font-weight: 500;
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add front/src/layout/Navbar.vue
git commit -m "feat: 重写 Navbar 顶栏，添加搜索框/通知铃铛/用户下拉"
```

---

### Task 5: 重写布局容器 MainLayout

**File:**
- Modify: `front/src/layout/index.vue` (全量重写)

- [ ] **Step 1: Rewrite layout/index.vue**

```vue
<!-- front/src/layout/index.vue -->
<template>
  <div class="layout-container">
    <Sidebar :is-collapsed="isCollapsed" />
    <div class="layout-main">
      <Navbar :collapsed="isCollapsed" @toggle="isCollapsed = !isCollapsed" />
      <TagsView />
      <div class="layout-content">
        <router-view />
      </div>
      <div class="layout-footer">
        <span>财富管理平台 v1.0.0</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import Sidebar from './Sidebar.vue'
import Navbar from './Navbar.vue'
import TagsView from '@/components/TagsView.vue'
import '@/styles/theme.css'

const isCollapsed = ref(false)
</script>

<style scoped>
.layout-container {
  display: flex;
  height: 100vh;
  overflow: hidden;
}
.layout-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: var(--fl-content-bg);
}
.layout-content {
  flex: 1;
  padding: 16px 20px;
  overflow-y: auto;
}
.layout-footer {
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: var(--fl-text-dim);
  border-top: 1px solid var(--fl-border);
  background: #fff;
  flex-shrink: 0;
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add front/src/layout/index.vue
git commit -m "feat: 重写布局容器，集成深色侧边栏、TagsView、页脚"
```

---

### Task 6: 更新入口文件

**Files:**
- Modify: `front/src/App.vue`
- Modify: `front/src/main.ts`

- [ ] **Step 1: Update App.vue**

```vue
<!-- front/src/App.vue -->
<template>
  <router-view />
</template>
```

(内容不变，确认即可)

- [ ] **Step 2: Update main.ts** — 全局样式不再需要 `wealth-light` class，改为直接引入 theme.css

```typescript
// front/src/main.ts
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import App from './App.vue'
import router from './router'
import './styles/theme.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
app.mount('#app')
```

- [ ] **Step 3: Commit**

```bash
git add front/src/App.vue front/src/main.ts
git commit -m "chore: 更新入口文件，全局引入新主题 CSS"
```

---

### Task 7: 更新路由，新增 2 条路由

**File:**
- Modify: `front/src/router/index.ts`

- [ ] **Step 1: Add admin-role and role-resource routes**

在 `children` 数组中添加：

```typescript
{
  path: 'system/admin-role',
  component: () => import('@/views/system/adminRole/index.vue'),
  meta: { title: '管理员角色关联', icon: 'Link' },
},
{
  path: 'system/role-resource',
  component: () => import('@/views/system/roleResource/index.vue'),
  meta: { title: '角色资源关联', icon: 'Connection' },
},
```

完整 children 数组成为：

```typescript
children: [
  { path: 'dashboard', component: () => import('@/views/dashboard/index.vue'), meta: { title: '控制台', icon: 'Monitor' } },
  { path: 'user', component: () => import('@/views/user/index.vue'), meta: { title: '用户管理', icon: 'User' } },
  { path: 'system/admin', component: () => import('@/views/system/admin/index.vue'), meta: { title: '管理员管理', icon: 'Setting' } },
  { path: 'system/role', component: () => import('@/views/system/role/index.vue'), meta: { title: '角色管理', icon: 'Avatar' } },
  { path: 'system/resource', component: () => import('@/views/system/resource/index.vue'), meta: { title: '资源管理', icon: 'Connection' } },
  { path: 'system/admin-role', component: () => import('@/views/system/adminRole/index.vue'), meta: { title: '管理员角色关联', icon: 'Link' } },
  { path: 'system/role-resource', component: () => import('@/views/system/roleResource/index.vue'), meta: { title: '角色资源关联', icon: 'Connection' } },
  { path: 'product', component: () => import('@/views/product/index.vue'), meta: { title: '产品管理', icon: 'Goods' } },
  { path: 'market', component: () => import('@/views/market/index.vue'), meta: { title: '行情数据', icon: 'DataLine' } },
  { path: 'trade', component: () => import('@/views/trade/index.vue'), meta: { title: '交易管理', icon: 'List' } },
  { path: 'favorite', component: () => import('@/views/favorite/index.vue'), meta: { title: '自选管理', icon: 'Star' } },
  { path: 'message', component: () => import('@/views/message/index.vue'), meta: { title: '站内消息', icon: 'Message' } },
  { path: 'news', component: () => import('@/views/news/index.vue'), meta: { title: '资讯管理', icon: 'Reading' } },
  { path: 'search', component: () => import('@/views/search/index.vue'), meta: { title: '搜索管理', icon: 'Search' } },
],
```

- [ ] **Step 2: Commit**

```bash
git add front/src/router/index.ts
git commit -m "feat: 添加管理员角色关联和角色资源关联路由"
```

---

### Task 8: 新建管理员-角色关联页面

**Files:**
- Create: `front/src/views/system/adminRole/index.vue`

- [ ] **Step 1: Create adminRole/index.vue**

```vue
<!-- front/src/views/system/adminRole/index.vue -->
<template>
  <div class="page">
    <div class="page-header"><h3>管理员角色关联</h3></div>
    <el-card shadow="never" class="search-card">
      <el-form :model="query" inline>
        <el-form-item label="管理员ID">
          <el-input-number v-model="query.adminId" :min="0" clearable />
        </el-form-item>
        <el-form-item label="角色ID">
          <el-input-number v-model="query.roleId" :min="0" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top:16px;">
      <div style="margin-bottom:16px;">
        <el-button type="primary" @click="handleAdd">添加关联</el-button>
      </div>
      <el-table :data="tableData" stripe v-loading="loading" border empty-text="暂无数据">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="adminId" label="管理员ID" width="100" />
        <el-table-column prop="roleId" label="角色ID" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="170" :formatter="(_r:any,_c:any,v:any)=>formatDateTime(v)" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-popconfirm title="确定删除此关联？" @confirm="handleDelete(row.id)">
              <template #reference><el-button type="danger" link>删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
          :total="total" :page-sizes="[10,20,50]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange" @current-change="fetchData"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" title="添加关联" width="420px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="管理员ID" prop="adminId">
          <el-input-number v-model="form.adminId" style="width:100%" />
        </el-form-item>
        <el-form-item label="角色ID" prop="roleId">
          <el-input-number v-model="form.roleId" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getAdminRoleRelationPage, createAdminRoleRelation, deleteAdminRoleRelation } from '@/api/system'
import { formatDateTime } from '@/utils/format'

const loading = ref(false); const saving = ref(false)
const tableData = ref<any[]>([]); const total = ref(0)
const dialogVisible = ref(false); const formRef = ref<FormInstance>()
const query = reactive({ pageNum: 1, pageSize: 10, adminId: 0, roleId: 0 })
const form = reactive({ adminId: undefined, roleId: undefined })
const rules: FormRules = {
  adminId: [{ required: true, message: '请输入管理员ID' }],
  roleId: [{ required: true, message: '请输入角色ID' }],
}

async function fetchData() {
  loading.value = true
  try {
    const params: any = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.adminId > 0) params.adminId = query.adminId
    if (query.roleId > 0) params.roleId = query.roleId
    const res = await getAdminRoleRelationPage(params)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}
function handleSearch() { query.pageNum = 1; fetchData() }
function handleReset() { query.adminId = 0; query.roleId = 0; handleSearch() }
function handleSizeChange() { query.pageNum = 1; fetchData() }
function handleAdd() { form.adminId = undefined; form.roleId = undefined; dialogVisible.value = true }
async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return; saving.value = true
  try {
    await createAdminRoleRelation(form)
    ElMessage.success('添加成功'); dialogVisible.value = false; fetchData()
  } finally { saving.value = false }
}
async function handleDelete(id: number) {
  try { await deleteAdminRoleRelation(id); ElMessage.success('删除成功'); fetchData() } catch { /* handled */ }
}
onMounted(fetchData)
</script>
```

- [ ] **Step 2: Commit**

```bash
git add front/src/views/system/adminRole/index.vue
git commit -m "feat: 添加管理员角色关联页面"
```

---

### Task 9: 新建角色-资源关联页面

**Files:**
- Create: `front/src/views/system/roleResource/index.vue`

- [ ] **Step 1: Create roleResource/index.vue**

```vue
<!-- front/src/views/system/roleResource/index.vue -->
<template>
  <div class="page">
    <div class="page-header"><h3>角色资源关联</h3></div>
    <el-card shadow="never" class="search-card">
      <el-form :model="query" inline>
        <el-form-item label="角色ID">
          <el-input-number v-model="query.roleId" :min="0" clearable />
        </el-form-item>
        <el-form-item label="资源ID">
          <el-input-number v-model="query.resourceId" :min="0" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top:16px;">
      <div style="margin-bottom:16px;">
        <el-button type="primary" @click="handleAdd">添加关联</el-button>
      </div>
      <el-table :data="tableData" stripe v-loading="loading" border empty-text="暂无数据">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="roleId" label="角色ID" width="100" />
        <el-table-column prop="resourceId" label="资源ID" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="170" :formatter="(_r:any,_c:any,v:any)=>formatDateTime(v)" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-popconfirm title="确定删除此关联？" @confirm="handleDelete(row.id)">
              <template #reference><el-button type="danger" link>删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
          :total="total" :page-sizes="[10,20,50]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange" @current-change="fetchData"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" title="添加关联" width="420px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="角色ID" prop="roleId">
          <el-input-number v-model="form.roleId" style="width:100%" />
        </el-form-item>
        <el-form-item label="资源ID" prop="resourceId">
          <el-input-number v-model="form.resourceId" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getRoleResourceRelationPage, createRoleResourceRelation, deleteRoleResourceRelation } from '@/api/system'
import { formatDateTime } from '@/utils/format'

const loading = ref(false); const saving = ref(false)
const tableData = ref<any[]>([]); const total = ref(0)
const dialogVisible = ref(false); const formRef = ref<FormInstance>()
const query = reactive({ pageNum: 1, pageSize: 10, roleId: 0, resourceId: 0 })
const form = reactive({ roleId: undefined, resourceId: undefined })
const rules: FormRules = {
  roleId: [{ required: true, message: '请输入角色ID' }],
  resourceId: [{ required: true, message: '请输入资源ID' }],
}

async function fetchData() {
  loading.value = true
  try {
    const params: any = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.roleId > 0) params.roleId = query.roleId
    if (query.resourceId > 0) params.resourceId = query.resourceId
    const res = await getRoleResourceRelationPage(params)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}
function handleSearch() { query.pageNum = 1; fetchData() }
function handleReset() { query.roleId = 0; query.resourceId = 0; handleSearch() }
function handleSizeChange() { query.pageNum = 1; fetchData() }
function handleAdd() { form.roleId = undefined; form.resourceId = undefined; dialogVisible.value = true }
async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return; saving.value = true
  try {
    await createRoleResourceRelation(form)
    ElMessage.success('添加成功'); dialogVisible.value = false; fetchData()
  } finally { saving.value = false }
}
async function handleDelete(id: number) {
  try { await deleteRoleResourceRelation(id); ElMessage.success('删除成功'); fetchData() } catch { /* handled */ }
}
onMounted(fetchData)
</script>
```

- [ ] **Step 2: Commit**

```bash
git add front/src/views/system/roleResource/index.vue
git commit -m "feat: 添加角色资源关联页面"
```

---

### Task 10: 重构 Dashboard 仪表盘

**File:**
- Modify: `front/src/views/dashboard/index.vue`

- [ ] **Step 1: Restructure Dashboard**

Dashboard 需要改为设计文档的 4 统计卡片 + SSE 行情网格 + 最近资讯。保留现有的 ECharts 图表和快捷入口在下方。

关键改动：
1. 移除外层的 `wealth-light` class 依赖
2. 统计卡片改为：产品总数 / 今日委托单数 / 待处理消息数 / 在线用户数
3. 添加 SSE 实时行情卡片网格区域（调用 `createMarketSSE`）
4. 添加最近 5 条资讯区域（调用 `getNewsList`）
5. 保留 ECharts 图表（位置移到底部）+ 快捷入口

文件较大，具体实施方案：复制现有 `dashboard/index.vue`，替换统计卡片内容，新增 SSE 行情网格 + 资讯列表。由于改动较大，完整代码见附件（约 350 行）。

- [ ] **Step 2: 清除 scoped style 中所有 `wealth-light` 引用**

将 scoped style 中的 `.wealth-light` class 选择器前缀全部移除，改为直接使用 CSS 变量。

- [ ] **Step 3: Commit**

```bash
git add front/src/views/dashboard/index.vue
git commit -m "feat: 重构 Dashboard 为 4 统计卡片 + SSE 行情 + 最近资讯"
```

---

### Task 11: 更新所有视图页面样式

**Files:**
- Modify: `front/src/views/login/index.vue`
- Modify: `front/src/views/user/index.vue`
- Modify: `front/src/views/product/index.vue`
- Modify: `front/src/views/market/index.vue`
- Modify: `front/src/views/favorite/index.vue`
- Modify: `front/src/views/trade/index.vue`
- Modify: `front/src/views/message/index.vue`
- Modify: `front/src/views/news/index.vue`
- Modify: `front/src/views/search/index.vue`
- Modify: `front/src/views/system/admin/index.vue`
- Modify: `front/src/views/system/role/index.vue`
- Modify: `front/src/views/system/resource/index.vue`

- [ ] **Step 1: 统一移除 `wealth-light` 依赖**

每个页面的 scoped style 中如果引用了 `.wealth-light` class，均改为使用 `:root` CSS 变量。现有页面都没有 `wealth-light` 引用（只在 layout 层），所以这一步主要是确认。

每个页面的 `<template>` 根元素如果使用了 `class="wealth-light"`（如 dashboard），将其移除。如果没有则跳过。

- [ ] **Step 2: 统一 scoped style 使用 CSS 变量**

确保每个页面的 scoped style 使用 `var(--fl-*)` CSS 变量而非硬编码颜色。例如：
- 涨/跌颜色使用 `var(--fl-rise)` / `var(--fl-fall)`
- 文字颜色使用 `var(--fl-text)` / `var(--fl-text-secondary)` / `var(--fl-text-dim)`

- [ ] **Step 3: Commit**

```bash
git add front/src/views/
git commit -m "style: 统一所有视图页面使用 CSS 变量主题色"
```

---

### Task 12: 编译验证

- [ ] **Step 1: 安装依赖（如之前未安装）**

Run: `cd front && npm install`

- [ ] **Step 2: 尝试编译**

Run: `cd front && npx vue-tsc --noEmit`
Expected: No type errors

- [ ] **Step 3: 构建验证**

Run: `cd front && npx vite build`
Expected: Build successful

- [ ] **Step 4: 启动开发服务器**

Run: `cd front && npx vite`
Expected: Dev server running at localhost:3000

- [ ] **Step 5: 如有错误，修复后重复 Step 2-4**

- [ ] **Step 6: 提交最终修复**

```bash
git add -A
git commit -m "fix: 编译修复"
```
