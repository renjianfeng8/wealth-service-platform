# 管理端前台同源品牌化改造 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 把后台管理控制台改造成与前台同源的蓝白品牌风格，复用前台视觉规范，替换上一版青绿配色方案。

**Architecture:** 将 `theme.css` 的 `--fl-*` 令牌值改为引用前台 `global.css` 的令牌，使前台成为唯一色彩真源；令牌别名化后，壳层、13 个 CRUD 页与仪表盘自动跟随。视觉不一致的残余（后台自定义 class 的硬编码灰、图形化渐变、六色图标、深蓝侧栏）逐点收敛到前台规范。仪表盘移除全部 ECharts 图表，改为纯卡片 + 列表结构。

**Tech Stack:** Vue 3 `<script setup>`、TypeScript、Element Plus 2.9、Vite 6、`vue-tsc`。

**Spec:** `docs/superpowers/specs/2026-09-14-admin-brand-redesign-design.md`

## Global Constraints

- 前端工程根目录为 `D:\demo\wealth-service-platform\front`，所有路径相对该目录。
- 不修改后端代码、数据库、`init.sql`、YAML、`pom.xml`、路由定义、权限拦截器或任何 API 契约。
- 不新增依赖。`echarts` 因 `KlinePanel.vue` 仍被前台 `views/market/MarketDetailDialog.vue` 引用而保留。
- 不修改业务逻辑、数据加载语义与权限判断。
- **本项目无前端测试框架**（`package.json` 仅 `dev` / `build` / `preview`），**且本机无 playwright**（`playwright-cli` 不在 PATH，`front/node_modules` 内亦无）。因此本计划**不做浏览器自动化验收**，子代理执行时不要尝试启动浏览器或截图。
- 每个任务的回归闸门是 `npm run build`（`vue-tsc --noEmit && vite build`）。类型检查能捕获删除组件后的失效引入。
- 需要视觉确认的地方改用**构建产物检查**：在 `front/dist/assets/*.css` 中 grep 关键令牌与类名，证明令牌别名已解析、死类已消失。这是产物级证据，**不等于**视觉验收，报告中不得表述为"视觉已验证"。
- 真正的视觉验收由用户在浏览器完成，核对清单见 Task 8 Step 6。
- **禁止执行 `git commit` / `git push`**（项目规约 `.claude/CLAUDE.md` §2.1 第 5 条要求用户单独审批）。本计划所有任务都不含提交步骤，改动全部留在工作区由用户决定何时提交。
- 不修复 `KlinePanel.vue` 的"前台引用后台组件"跨层耦合（超出本次范围）。

---

## File Structure

| 文件 | 动作 | 职责 |
|------|------|------|
| `src/styles/theme.css` | 修改 | 令牌别名化、死令牌与死样式清理、全局卡片头 class |
| `src/layouts/AdminLayout.vue` | 修改 | 1280px 居中内容容器 |
| `src/layouts/Sidebar.vue` | 修改 | 白底侧栏、品牌渐变 Logo、浅蓝激活态 |
| `src/layouts/Navbar.vue` | 修改 | 搜索框与下拉项对齐前台令牌 |
| `src/components/admin/AdminPageShell.vue` | 修改 | 页面标题加品牌蓝竖条、令牌化文字色 |
| `src/components/admin/AdminFilterBar.vue` | 修改 | 卡片令牌化 |
| `src/components/admin/AdminDataTable.vue` | 修改 | 卡片令牌化、空态改 `el-empty` |
| `src/views/admin/dashboard/index.vue` | 修改 | 移除趋势图、调整区块顺序、内容宽度 |
| `src/views/admin/dashboard/components/OperationsConsoleHeader.vue` | 修改 | 欢迎区改纯蓝渐变、告警卡统一品牌蓝 |
| `src/views/admin/dashboard/components/DashboardMetricGrid.vue` | 修改 | 六色图标统一品牌蓝 |
| `src/views/admin/dashboard/components/DashboardQuickEntries.vue` | 修改 | 复活并改为前台快捷入口样式 |
| `src/views/admin/dashboard/components/MarketSnapshot.vue` | 修改 | 全宽多列网格、类名去图表化 |
| `src/views/admin/dashboard/components/LatestActivities.vue` | 修改 | 移除重复的 scoped 卡片头定义、灰底令牌化 |
| `src/views/admin/search/index.vue` | 修改 | 修复 `--fl-text-primary` 未定义变量 |
| `src/views/admin/profile/index.vue` | 修改 | 3 处硬编码色令牌化 |
| `src/composables/useAdminDashboard.ts` | 修改 | 移除趋势数据加载 |
| `src/views/admin/dashboard/components/TrendPanel.vue` | 删除 | 趋势图组件，无人引用 |
| `src/views/admin/dashboard/components/ActionQueuePanel.vue` | 删除 | 孤儿组件 |
| `src/views/admin/dashboard/components/MiniProductStrip.vue` | 删除 | 孤儿组件 |
| `docs/superpowers/specs/2026-09-14-admin-operations-redesign-design.md` | 删除 | 被上一版青绿方案取代 |
| `docs/superpowers/plans/2026-09-14-admin-operations-redesign.md` | 删除 | 同上 |

**不修改**：`src/components/admin/AdminFormDialog.vue`（无自有样式，已继承 `global.css` 的 `.el-dialog` 覆盖）、`src/views/admin/dashboard/components/KlinePanel.vue`（前台在用）。

**任务依赖顺序**：Task 1（令牌）是地基，必须先做。Task 4 必须在 Task 6（删除）之前，否则删除会导致失效引入。

---

## Task 1: 令牌层改造

**Files:**
- Modify: `front/src/styles/theme.css`

**Interfaces:**
- Consumes: `global.css` 中已声明的 `--primary`、`--primary-dark`、`--primary-light`、`--bg-page`、`--bg-card`、`--bg-navbar`、`--text-primary`、`--text-regular`、`--text-secondary`、`--text-placeholder`、`--border-color`、`--border-light`、`--radius`、`--shadow-md`、`--navbar-height`、`--rise-color`、`--fall-color`、`--warning`；Element Plus 的 `--el-fill-color-lighter`。
- Produces: 后台通用令牌 `--fl-*` 全部指向前台真源；新增全局 class `.fl-card-header` / `.fl-card-subtitle`。

- [ ] **Step 1: 记录改造前基线**

```bash
cd D:/demo/wealth-service-platform/front
npm run build
```

预期：退出码 0。若基线就失败，先停下来报告，不要在失败的基线上继续。

- [ ] **Step 2: 用前台真源重写 `:root` 令牌块**

将 `theme.css` 顶部 `:root { ... }` 整块（当前第 1–37 行）替换为：

```css
:root {
  /* 侧栏 — 前台同源白底，激活态用浅蓝底 + 品牌蓝字 */
  --fl-sidebar-bg: var(--bg-card);
  --fl-sidebar-text: var(--text-regular);
  --fl-sidebar-active: var(--primary);
  --fl-sidebar-active-bg: var(--primary-light);
  --fl-sidebar-hover-bg: var(--el-fill-color-lighter);
  --fl-sidebar-width: 220px;
  --fl-sidebar-collapsed-width: 64px;

  --fl-header-bg: var(--bg-navbar);
  --fl-header-height: var(--navbar-height);
  --fl-header-border: var(--border-color);

  --fl-content-bg: var(--bg-page);
  --fl-card-bg: var(--bg-card);
  --fl-border: var(--border-color);
  --fl-border-light: var(--border-light);

  --fl-text: var(--text-primary);
  --fl-text-secondary: var(--text-regular);
  --fl-text-dim: var(--text-secondary);
  --fl-text-placeholder: var(--text-placeholder);

  --fl-primary: var(--primary);

  --fl-rise: var(--rise-color);
  --fl-fall: var(--fall-color);

  --fl-radius: var(--radius);
  --fl-shadow-hover: var(--shadow-md);
}
```

被删除的 6 个零引用令牌：`--fl-primary-hover`、`--fl-primary-light`、`--fl-radius-sm`、`--fl-shadow`、`--fl-rise-bg`、`--fl-fall-bg`。

`--fl-sidebar-active` 与 `--fl-sidebar-text` 当前零引用，但 Task 2 移除 `el-menu` 硬编码颜色属性后会被启用，必须保留。

- [ ] **Step 3: 删除死样式类，新增全局卡片头 class**

删除 `theme.css` 中以下零引用规则：`.fl-stat-grid`、`.fl-stat-card`、`.fl-stat-card:hover`、`.fl-stat-icon`、`.fl-stat-value`、`.fl-stat-label`、`.fl-rise-bg`、`.fl-fall-bg`、`.fl-text-primary`、`.pagination-wrap`、`.page-header h3`、`.search-card`、`.search-card .el-form-item`。

保留 `.fl-card`、`.fl-card:hover`、`.fl-card-title`、`.fl-rise`、`.fl-fall`（均有真实引用），并在 `.fl-card-title` 之后新增两条全局规则——这两个 class 目前只在 `LatestActivities.vue` 和待删的 `TrendPanel.vue` 里以 scoped 定义，导致 `LatestOrdersPanel.vue`、`MarketSnapshot.vue`、`DashboardQuickEntries.vue` 的卡片头部排版失效：

```css
.fl-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 14px;
}
.fl-card-subtitle {
  font-size: 12px;
  color: var(--fl-text-dim);
  margin-top: 2px;
}
```

改造后 `theme.css` 应只剩四个区块：卡片、涨跌颜色、滚动条，以及新增的卡片头。

- [ ] **Step 4: 构建验证**

```bash
cd D:/demo/wealth-service-platform/front
npm run build
```

预期：退出码 0。CSS 改动不影响类型检查，此步确认没有误删被 `@import` 或其他规则引用的内容。

- [ ] **Step 5: 确认死令牌确已无引用**

```bash
cd D:/demo/wealth-service-platform/front/src
grep -rn "fl-primary-hover\|fl-primary-light\|fl-radius-sm\|var(--fl-shadow)\|fl-rise-bg\|fl-fall-bg" . --include=*.vue --include=*.css
```

预期：无输出。若有输出，说明该令牌仍有引用，需把它加回 `:root`。

---

## Task 2: 壳层改造

**Files:**
- Modify: `front/src/layouts/AdminLayout.vue:1-101`
- Modify: `front/src/layouts/Sidebar.vue:1-227`
- Modify: `front/src/layouts/Navbar.vue:1-344`

**Interfaces:**
- Consumes: Task 1 产出的 `--fl-*` 令牌。
- Produces: 无新增运行时接口。菜单 `index` 值、折叠状态、移动端抽屉、搜索与通知逻辑全部不变。

- [ ] **Step 1: `AdminLayout.vue` 加 1280px 居中容器**

模板中把 `<router-view />` 包一层：

```vue
      <div class="layout-content">
        <div class="layout-content-inner">
          <router-view />
        </div>
      </div>
```

样式把 `.layout-content` 的内边距改为 24px，并新增 `.layout-content-inner`：

```css
.layout-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  scroll-behavior: smooth;
}
.layout-content-inner {
  max-width: 1280px;
  width: 100%;
  margin: 0 auto;
}
```

移动端区块内的 `.layout-content { padding: 12px; }` 保持不变（覆盖桌面 24px）。

- [ ] **Step 2: `Sidebar.vue` 改白底侧栏**

模板两处改动。

其一，Logo 文案改为与前台一致：

```vue
        <span v-show="!isCollapsed" class="logo-text">理财服务平台</span>
```

其二，移除 `el-menu` 上硬编码的深色三兄弟属性，保留其余属性不变：

```vue
    <el-menu
      :default-active="activeMenu"
      router
      :collapse="isCollapsed"
      :collapse-transition="false"
    >
```

样式替换为：

```css
.sidebar {
  width: var(--fl-sidebar-width);
  height: 100vh;
  background: var(--fl-sidebar-bg);
  border-right: 1px solid var(--fl-border);
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  overflow: hidden;
  flex-shrink: 0;
}
.sidebar.collapsed {
  width: var(--fl-sidebar-collapsed-width);
}
.sidebar.collapsed :deep(.el-menu-item),
.sidebar.collapsed :deep(.el-sub-menu__title) {
  margin: 2px 0;
  padding: 0 !important;
  justify-content: center;
}
.sidebar.collapsed .logo-icon {
  width: 100%;
  justify-content: center;
}
.sidebar-logo {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  border-bottom: 1px solid var(--fl-border);
  flex-shrink: 0;
}
.logo-icon {
  color: var(--fl-primary);
  display: flex;
  align-items: center;
}
.logo-text {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.5px;
  white-space: nowrap;
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}
.sidebar :deep(.el-menu) {
  border-right: none !important;
  flex: 1;
  overflow-y: auto;
  background: transparent !important;
}
.sidebar :deep(.el-menu-item),
.sidebar :deep(.el-sub-menu__title) {
  height: 44px;
  line-height: 44px;
  margin: 2px 8px;
  border-radius: 8px;
  width: auto !important;
  color: var(--fl-sidebar-text);
}
.sidebar :deep(.el-menu-item:hover),
.sidebar :deep(.el-sub-menu__title:hover) {
  background: var(--fl-sidebar-hover-bg) !important;
  color: var(--fl-primary);
}
.sidebar :deep(.el-menu-item.is-active) {
  background: var(--fl-sidebar-active-bg) !important;
  color: var(--fl-sidebar-active) !important;
  font-weight: 600;
}
.sidebar :deep(.el-menu-item.is-active .el-icon) {
  color: var(--fl-sidebar-active) !important;
}
.sidebar :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  color: var(--fl-primary);
}
.sidebar :deep(.el-sub-menu .el-menu) {
  background: transparent !important;
}
.sidebar :deep(.el-sub-menu .el-menu .el-menu-item) {
  padding-left: 48px !important;
  height: 38px;
  line-height: 38px;
}
.sidebar-footer {
  padding: 12px;
  border-top: 1px solid var(--fl-border);
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
  color: var(--fl-text);
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.user-role {
  color: var(--fl-text-dim);
  font-size: 11px;
}
.logout-btn {
  color: var(--fl-text-dim) !important;
}
.logout-btn:hover {
  color: var(--fl-primary) !important;
}
```

要点：白底侧栏必须靠 `border-right` 与内容区区分，否则在 `--fl-content-bg`（#f5f7fa）上会糊成一片。底部用户块原先是白字，白底上不可见，必须改为深色。

- [ ] **Step 3: `Navbar.vue` 搜索框与下拉项令牌化**

样式改动（功能一行不动）：

```css
.search-box .el-input__wrapper {
  background: var(--el-fill-color-lighter);
  border-radius: 8px;
  box-shadow: none;
}

.search-box .el-input__wrapper:hover {
  background: var(--el-fill-color);
}

.search-box .el-input__wrapper.is-focus {
  background: var(--fl-card-bg);
  box-shadow: 0 0 0 1px var(--fl-primary) inset;
}

.search-shortcut-hint kbd {
  font-size: 11px;
  padding: 1px 6px;
  background: var(--fl-border);
  border-radius: 4px;
  color: var(--fl-text-placeholder);
  font-family: inherit;
}

.user-dropdown:hover {
  background: var(--el-fill-color-lighter);
}

.username {
  font-size: 13px;
  color: var(--fl-text);
  font-weight: 500;
}
```

同时删除从未被模板使用的死规则 `.search-shortcut { ... }`（模板里只有 `.search-shortcut-hint`），并从图标 import 中移除未使用的 `UserFilled`。

- [ ] **Step 4: 构建验证**

```bash
cd D:/demo/wealth-service-platform/front
npm run build
```

预期：退出码 0。

- [ ] **Step 5: 产物检查壳层改动已生效**

```bash
cd D:/demo/wealth-service-platform/front
npm run build
grep -o "\-\-fl-sidebar-bg:[^;]*" dist/assets/*.css | head -3
grep -c "理财服务平台" dist/assets/*.js
```

预期：`--fl-sidebar-bg` 的值为 `var(--bg-card)`；`理财服务平台` 在 JS 产物中命中 ≥1 次（侧栏 logo 文案）。

---

## Task 3: 共享组件层改造

**Files:**
- Modify: `front/src/components/admin/AdminPageShell.vue:24-66`
- Modify: `front/src/components/admin/AdminFilterBar.vue:70-81`
- Modify: `front/src/components/admin/AdminDataTable.vue:1-93`

**Interfaces:**
- Consumes: Task 1 的 `--fl-*` 令牌。
- Produces: `AdminDataTable` 的 props 与 emits **完全不变**（`data`、`loading`、`total`、`pagination`、`pageSizes`、`emptyText`、`selectable`、`page-change`、`selection-change`），13 个调用方无需改动。

- [ ] **Step 1: `AdminPageShell.vue` 加品牌蓝标题竖条**

```css
.admin-page-shell__header h2 {
  margin: 0;
  color: var(--fl-text);
  font-size: 22px;
  font-weight: 700;
  line-height: 1.3;
  display: flex;
  align-items: center;
  gap: 8px;
}

.admin-page-shell__header h2::before {
  content: '';
  width: 4px;
  height: 20px;
  background: var(--fl-primary);
  border-radius: 2px;
  flex-shrink: 0;
}

.admin-page-shell__header p {
  margin: 6px 0 0;
  color: var(--fl-text-secondary);
  font-size: 13px;
  line-height: 1.5;
}
```

竖条写法对齐前台 `global.css` 的 `.page-title::before`。其余规则（`.admin-page-shell`、`__toolbar`、移动端断点）不变。

- [ ] **Step 2: `AdminFilterBar.vue` 卡片令牌化**

```css
.admin-filter-bar {
  padding: 20px 24px 6px;
  background: var(--fl-card-bg);
  border: 1px solid var(--fl-border);
  border-radius: var(--fl-radius);
}

.admin-filter-bar :deep(.el-form-item) {
  margin-bottom: 14px;
}
```

下内边距用 6px 而非 20px，是因为末行 `.el-form-item` 自带 14px 下边距，合计 20px 视觉齐平。

- [ ] **Step 3: `AdminDataTable.vue` 卡片令牌化 + 空态改 `el-empty`**

模板中删除 `el-table` 的 `:empty-text="emptyText"`，改用 `#empty` 插槽：

```vue
    <el-table
      :data="data"
      stripe
      border
      v-loading="loading"
      @selection-change="handleSelectionChange"
    >
      <el-table-column v-if="selectable" type="selection" width="55" />
      <slot />
      <template #empty>
        <el-empty :description="emptyText" :image-size="50" />
      </template>
    </el-table>
```

`emptyText` 的 prop 与默认值 `'暂无数据'` 保持不变，各页面无需改动。`#empty` 插槽优先于 `empty-text` 属性，因此移除该属性不会丢失文案。

样式：

```css
.admin-data-table {
  padding: 20px 24px;
  background: var(--fl-card-bg);
  border: 1px solid var(--fl-border);
  border-radius: var(--fl-radius);
}

.admin-data-table__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.admin-data-table__pagination {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
}
```

- [ ] **Step 4: 构建验证**

```bash
cd D:/demo/wealth-service-platform/front
npm run build
```

预期：退出码 0。

- [ ] **Step 5: 产物检查共享组件改动已生效**

```bash
cd D:/demo/wealth-service-platform/front
npm run build
grep -c "el-empty" dist/assets/*.js
grep -rn "border: 1px solid var(--fl-border)" src/components/admin/AdminDataTable.vue
```

预期：`AdminDataTable.vue` 已引用 `--fl-border` 令牌（不再有 `#e5e7eb`）；构建产物中 `el-empty` 命中 ≥1 次（表格空态已接入）。

---

## Task 4: 仪表盘结构重构

**Files:**
- Modify: `front/src/views/admin/dashboard/index.vue:1-145`
- Modify: `front/src/composables/useAdminDashboard.ts:1-124`
- Modify: `front/src/views/admin/dashboard/components/DashboardQuickEntries.vue:1-98`

**Interfaces:**
- Consumes: `useAdminDashboard()` 的 `loading`、`lastRefreshTime`、`pendingOrders`、`unreadMessages`、`disabledProducts`、`totalUsers`、`totalProducts`、`totalOrders`、`overview`、`marketProducts`、`latestOrders`、`recentMessages`、`fetchData`。
- Produces: `useAdminDashboard()` 不再返回 `trendData` 与 `loadTrend`；仪表盘新布局 `Row1 欢迎区 / Row2 汇总数据 / Row3 快捷入口 / Row4 订单+动态 / Row5 行情快照`。

- [ ] **Step 1: `useAdminDashboard.ts` 移除趋势加载**

三处改动。

引入行改为（去掉 `getDashboardTrend`）：

```ts
import { getDashboardOverview, type DashboardOverview } from '@/api/dashboard'
```

删除 `trendData` 声明并把该段落注释去图表化，结果为：

```ts
  /* ---- Row 3: Market ---- */
  const marketProducts = ref<WeaProduct[]>([])
```

删除 `loadTrend` 函数：

```ts
  async function loadTrend(period = '7D') {
    const res = await getDashboardTrend(period)
    if (res) trendData.value = res
  }
```

`fetchData` 的 `Promise.allSettled` 数组去掉 `loadTrend(),` 一项，`return` 对象去掉 `trendData` 与 `loadTrend`。其余全部保留（`overview` 继续供资产总值与今日收益）。

后端 `getDashboardTrend` 接口与 `src/api/dashboard.ts` 中的封装**保持不动**。

- [ ] **Step 2: `dashboard/index.vue` 重排区块**

模板整体替换为（移除 `TrendPanel`、移除死 class `wealth-light`、插入 `DashboardQuickEntries`、`MarketSnapshot` 移到最后一行）：

```vue
<template>
  <div class="fl-dashboard-page">
    <div v-if="loading" class="fl-loading">
      <div class="fl-loading-spinner" />
      <span>加载中...</span>
    </div>

    <template v-else>
      <div class="fl-dashboard">
        <OperationsConsoleHeader
          :admin-name="displayName"
          :last-refresh-time="lastRefreshTime"
          :pending-orders="pendingOrders"
          :unread-messages="unreadMessages"
          :disabled-products="disabledProducts"
          @refresh="fetchData"
        />

        <DashboardMetricGrid
          :total-users="totalUsers"
          :total-products="totalProducts"
          :total-orders="totalOrders"
          :unread-messages="unreadMessages"
          :total-asset="overview?.totalAsset ?? null"
          :daily-income="overview?.dailyIncome ?? null"
        />

        <DashboardQuickEntries />

        <div class="fl-ops-row">
          <LatestOrdersPanel :orders="latestOrders" />
          <LatestActivities :messages="recentMessages" />
        </div>

        <MarketSnapshot
          :products="marketProducts"
          :format-price="formatPrice"
          :format-rate="formatRate"
        />
      </div>
    </template>
  </div>
</template>
```

`wealth-light` 是一个从未被任何样式表定义的 class，属上一轮遗留的死标记，一并移除。

- [ ] **Step 3: `dashboard/index.vue` 清理脚本**

import 块去掉 `TrendPanel`，加上 `DashboardQuickEntries`：

```ts
import OperationsConsoleHeader from './components/OperationsConsoleHeader.vue'
import DashboardMetricGrid from './components/DashboardMetricGrid.vue'
import DashboardQuickEntries from './components/DashboardQuickEntries.vue'
import MarketSnapshot from './components/MarketSnapshot.vue'
import LatestOrdersPanel from './components/LatestOrdersPanel.vue'
import LatestActivities from './components/LatestActivities.vue'
```

`useAdminDashboard()` 解构去掉 `trendData` 与 `loadTrend`。删除仅服务于 `TrendPanel` 的本地函数 `formatNumber`（`DashboardMetricGrid` 自带 `@/utils/format` 的 `formatNumber`，不依赖此本地函数）。`formatPrice`、`formatRate` 的引入保留（`MarketSnapshot` 仍在用）。

- [ ] **Step 4: `dashboard/index.vue` 更新布局样式**

```css
.fl-dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.fl-dashboard {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* Loading */
.fl-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120px 0;
  gap: 16px;
  color: var(--fl-text-dim);
  font-size: 14px;
}
.fl-loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--fl-border);
  border-top-color: var(--fl-primary);
  border-radius: 50%;
  animation: fl-spin 0.8s linear infinite;
}
@keyframes fl-spin { to { transform: rotate(360deg); } }

.fl-ops-row {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(300px, 0.9fr);
  gap: 14px;
  align-items: stretch;
}

@media (max-width: 1024px) {
  .fl-ops-row { grid-template-columns: 1fr; }
}
```

原 `.fl-chart-row-kline` 规则随图表一并删除。内容宽度由 Task 2 的 `AdminLayout` 1280px 容器统一负责，此处不再设 `max-width`。

- [ ] **Step 5: `DashboardQuickEntries.vue` 对齐前台快捷入口**

图标尺寸与圆角对齐前台首页 `quick-entry`（48px / 12px），每项保留独立渐变：

```ts
const entries: QuickEntry[] = [
  { label: '用户管理', path: '/admin/user', icon: User, bg: 'linear-gradient(135deg, #1a6dff, #0a4dcc)' },
  { label: '产品管理', path: '/admin/product', icon: Goods, bg: 'linear-gradient(135deg, #34c759, #28a745)' },
  { label: '行情数据', path: '/admin/market', icon: DataLine, bg: 'linear-gradient(135deg, #ff9500, #e68a00)' },
  { label: '交易管理', path: '/admin/trade', icon: List, bg: 'linear-gradient(135deg, #8e44ad, #6c3483)' },
  { label: '自选管理', path: '/admin/favorite', icon: Star, bg: 'linear-gradient(135deg, #1a6dff, #0a4dcc)' },
  { label: '消息管理', path: '/admin/message', icon: Message, bg: 'linear-gradient(135deg, #34c759, #28a745)' },
  { label: '管理员', path: '/admin/system/admin', icon: Setting, bg: 'linear-gradient(135deg, #ff9500, #e68a00)' },
]
```

样式改为：

```css
.fl-entry-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(110px, 1fr));
  gap: 12px;
}

.fl-entry-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 8px;
  border-radius: var(--fl-radius);
  cursor: pointer;
  transition: background 0.2s;
}

.fl-entry-item:hover {
  background: var(--el-fill-color-lighter);
}

.fl-entry-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  transition: transform 0.2s;
}

.fl-entry-item:hover .fl-entry-icon {
  transform: scale(1.08);
}

.fl-entry-label {
  font-size: 13px;
  color: var(--fl-text-secondary);
  white-space: nowrap;
}
```

模板无需改动——它已使用 `.fl-card` 与 `.fl-card-header`，后者在 Task 1 已提升为全局 class。

同时从图标 import 中移除未使用的 `Search`（该组件的条目只用到 `User`、`Goods`、`DataLine`、`List`、`Star`、`Message`、`Setting`）。

- [ ] **Step 6: 构建验证**

```bash
cd D:/demo/wealth-service-platform/front
npm run build
```

预期：退出码 0。`TrendPanel.vue` 目前仍存在于磁盘但已无人 import，`vue-tsc` 不会报错。

- [ ] **Step 7: 产物检查仪表盘结构**

```bash
cd D:/demo/wealth-service-platform/front
npm run build
grep -n "OperationsConsoleHeader\|DashboardMetricGrid\|DashboardQuickEntries\|LatestOrdersPanel\|LatestActivities\|MarketSnapshot" src/views/admin/dashboard/index.vue
grep -c "TrendPanel" src/views/admin/dashboard/index.vue || true
grep -rn "getDashboardTrend\|loadTrend\|trendData" src/composables/useAdminDashboard.ts || echo "趋势引用已清空"
```

预期：六个组件在 `index.vue` 中的出现顺序为 `OperationsConsoleHeader` → `DashboardMetricGrid` → `DashboardQuickEntries` → `LatestOrdersPanel` → `LatestActivities` → `MarketSnapshot`；`TrendPanel` 在 `index.vue` 中出现 0 次；`useAdminDashboard.ts` 输出"趋势引用已清空"。

---

## Task 5: 仪表盘组件样式收敛

**Files:**
- Modify: `front/src/views/admin/dashboard/components/OperationsConsoleHeader.vue:104-260`
- Modify: `front/src/views/admin/dashboard/components/DashboardMetricGrid.vue:98-198`
- Modify: `front/src/views/admin/dashboard/components/MarketSnapshot.vue:1-115`
- Modify: `front/src/views/admin/dashboard/components/LatestActivities.vue:57-143`

**Interfaces:**
- Consumes: 各组件既有 props，**全部不变**。
- Produces: 无接口变更，纯样式。

- [ ] **Step 1: `OperationsConsoleHeader.vue` 欢迎区改纯蓝渐变**

```css
.welcome-banner {
  background: linear-gradient(135deg, #f0f5ff 0%, #e6f7ff 100%);
  border: 1px solid rgba(26, 109, 255, 0.12);
  border-radius: var(--fl-radius);
  padding: 20px 24px;
}

.health-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--fl-primary);
  box-shadow: 0 0 0 3px var(--primary-light);
}
```

替换原来的蓝绿混渐变与绿色圆点。

- [ ] **Step 2: `OperationsConsoleHeader.vue` 告警卡统一品牌蓝**

把三条分色规则

```css
.alert-warning .alert-icon-wrap { background: rgba(245, 166, 35, 0.1); color: #f5a623; }
.alert-danger  .alert-icon-wrap { background: rgba(255, 59, 48, 0.08);  color: var(--fl-fall); }
.alert-done    .alert-icon-wrap { background: rgba(52, 199, 89, 0.08);  color: var(--fl-rise); }
```

合并为一条：

```css
.alert-warning .alert-icon-wrap,
.alert-danger .alert-icon-wrap,
.alert-done .alert-icon-wrap {
  background: var(--primary-light);
  color: var(--fl-primary);
}
```

数值颜色规则改为（去掉绿色，保留"需注意"的橙/红信号，归零态用中性灰）：

```css
.alert-warning .alert-count { color: var(--warning); }
.alert-danger .alert-count { color: var(--fl-fall); }
.alert-done .alert-count { color: var(--fl-text-dim); }
```

同时把 `.alert-count` 的 `font-family: 'Courier New', monospace` 改为 `'DIN Pro', monospace`，与前台首页数值字体一致。

- [ ] **Step 3: `DashboardMetricGrid.vue` 六色图标统一品牌蓝**

删除 `.metric-icon-blue`、`.metric-icon-green`、`.metric-icon-orange`、`.metric-icon-red`、`.metric-icon-purple`、`.metric-icon-cyan` 六条规则，改为 `.metric-icon` 自带品牌蓝：

```css
.metric-icon {
  width: 46px;
  height: 46px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: var(--primary-light);
  color: var(--fl-primary);
}
```

`.metric-value` 的字体同样改为 `'DIN Pro', monospace`。涨跌语义色（`.fl-rise` / `.fl-fall`）本组件未使用，无需处理。

- [ ] **Step 4: `MarketSnapshot.vue` 全宽多列网格 + 类名去图表化**

`MarketSnapshot` 现在独占 Row5 全宽，原先为图表侧列设计的单列 340px 宽布局会显得稀疏，改为自适应多列网格。

模板把外层 class 由 `fl-chart-col-list` 改为 `fl-market-panel`（保留内部 `fl-card` 与 `fl-card-list`）：

```vue
  <div class="fl-market-panel">
    <div class="fl-card fl-card-list">
```

样式相应替换：

```css
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
```

滚动条规则 `.fl-list-wrap::-webkit-scrollbar*` 保留不变。

- [ ] **Step 5: `LatestActivities.vue` 去重复定义、灰底令牌化**

删除组件内 scoped 的 `.fl-card-header`、`.fl-card-title`、`.fl-card-subtitle` 三条规则——它们已在 Task 1 提升为 `theme.css` 的全局 class，留着会与新定义重复且难以维护。

`.activity-item:hover` 的 `background: #f5f7fa` 改为 `var(--el-fill-color-lighter)`。

`.activity-item` 的 `border-radius: 6px` 改为 `8px`，与侧栏菜单项、前台导航项的圆角规范一致。（`--fl-radius-sm` 已在 Task 1 作为死令牌删除，此处不得引用。）

- [ ] **Step 6: 构建验证**

```bash
cd D:/demo/wealth-service-platform/front
npm run build
```

预期：退出码 0。

- [ ] **Step 7: 产物检查视觉收敛已生效**

```bash
cd D:/demo/wealth-service-platform/front
npm run build
grep -rn "19be6b\|rgba(25, 190, 107\|#f5a623\|Courier New" src/views/admin/dashboard/components/ || echo "无残留旧色值/旧字体"
grep -c "grid-template-columns: repeat(auto-fill, minmax(240px" dist/assets/*.css
```

预期：第一个 grep 无输出（仪表盘组件内不再有绿色、`#f5a623`、Courier New）；第二个 grep 命中 ≥1，证明行情快照的多列网格已进入构建产物。

---

## Task 6: 删除孤儿组件与旧设计文档

**Files:**
- Delete: `front/src/views/admin/dashboard/components/TrendPanel.vue`
- Delete: `front/src/views/admin/dashboard/components/ActionQueuePanel.vue`
- Delete: `front/src/views/admin/dashboard/components/MiniProductStrip.vue`
- Delete: `docs/superpowers/specs/2026-09-14-admin-operations-redesign-design.md`
- Delete: `docs/superpowers/plans/2026-09-14-admin-operations-redesign.md`

**Interfaces:**
- Consumes: Task 4 已完成——`TrendPanel` 的引用已从 `dashboard/index.vue` 移除。
- Produces: 无。这是一个纯删除任务。

> 用户已明确批准这 5 个文件的删除，并知悉 `docs/superpowers/` 目录全部处于 git 未跟踪状态，删除后**无法通过 git 恢复**。

- [ ] **Step 1: 删除前确认三个组件已无任何引用**

```bash
cd D:/demo/wealth-service-platform/front
grep -rn "TrendPanel\|ActionQueuePanel\|MiniProductStrip" src
```

预期：无输出。若有输出，说明还有文件在引用，必须先处理该引用，**不要删除**。

- [ ] **Step 2: 确认 `KlinePanel` 必须保留**

```bash
cd D:/demo/wealth-service-platform/front
grep -rn "KlinePanel" src
```

预期：能看到 `src/views/market/MarketDetailDialog.vue` 的 import 与使用。该文件**不在删除清单内**。

- [ ] **Step 3: 删除三个组件文件**

```bash
cd D:/demo/wealth-service-platform/front
rm src/views/admin/dashboard/components/TrendPanel.vue
rm src/views/admin/dashboard/components/ActionQueuePanel.vue
rm src/views/admin/dashboard/components/MiniProductStrip.vue
```

- [ ] **Step 4: 删除两份旧设计文档**

```bash
cd D:/demo/wealth-service-platform
rm docs/superpowers/specs/2026-09-14-admin-operations-redesign-design.md
rm docs/superpowers/plans/2026-09-14-admin-operations-redesign.md
```

- [ ] **Step 5: 构建验证**

```bash
cd D:/demo/wealth-service-platform/front
npm run build
```

预期：退出码 0。若报 "Cannot find module './components/TrendPanel.vue'"，说明 Task 4 的 import 清理未完成，回到 Task 4 Step 3 处理。

- [ ] **Step 6: 确认删除结果**

```bash
cd D:/demo/wealth-service-platform
ls docs/superpowers/specs docs/superpowers/plans
```

预期：`specs` 只剩 `2026-09-14-admin-brand-redesign-design.md`，`plans` 只剩 `2026-09-14-admin-brand-redesign.md`。

---

## Task 7: 残余页面修复

**Files:**
- Modify: `front/src/views/admin/search/index.vue:167`
- Modify: `front/src/views/admin/profile/index.vue:560,576,591`

**Interfaces:**
- Consumes: Task 1 的令牌。
- Produces: 无接口变更。

- [ ] **Step 1: 修复 `admin/search` 的未定义变量**

`views/admin/search/index.vue` 第 167 行使用了从未声明的 `--fl-text-primary`，该声明在计算值阶段失效，导致搜索结果里的关键字无法高亮。

```css
.search-header-info strong {
  color: var(--fl-primary);
}
```

- [ ] **Step 2: `admin/profile` 密码强度指示器令牌化**

三处替换：

```css
.strength-bar {
  flex: 1;
  height: 4px;
  background: var(--fl-border);
  border-radius: 2px;
  overflow: hidden;
}
```

```css
.strength-fill.medium {
  background: var(--warning);
}
```

```css
.strength-text.medium { color: var(--warning); }
```

该文件另一处 `color: #fff`（`.avatar-overlay`）是 `rgba(0,0,0,0.55)` 深色遮罩上的白字，语义正确，**保持不变**。

- [ ] **Step 3: 构建验证**

```bash
cd D:/demo/wealth-service-platform/front
npm run build
```

预期：退出码 0。

- [ ] **Step 4: 检查是否还有残留的未定义 `--fl-*` 变量**

```bash
cd D:/demo/wealth-service-platform/front/src
grep -rhoE 'var\(--fl-[a-z-]+' . --include=*.vue --include=*.css | sed 's/var(//' | sort -u > /tmp/used.txt
grep -oE '^\s+--fl-[a-z-]+' styles/theme.css | tr -d ' ' | sort -u > /tmp/decl.txt
comm -23 /tmp/used.txt /tmp/decl.txt
```

预期：无输出（无"被引用但未声明"的令牌）。若仍有输出，逐一按语义修正为已声明的令牌。

---

## Task 8: 全量验证与交付

**Files:**
- 仅在验证发现缺陷时修改前面任务涉及的文件。不得为了让验证通过而修改无关代码。

**Interfaces:**
- Consumes: Task 1–7 的全部产出。
- Produces: 构建通过证据 + 产物级检查结果 + 交付给用户的人工视觉核对清单。

> **本机无 playwright，本任务不做浏览器自动化验收。** 视觉确认由用户按 Step 4 的清单在浏览器完成。执行者不得把产物级检查表述为"视觉已验证"。

- [ ] **Step 1: 全量构建校验**

```bash
cd D:/demo/wealth-service-platform/front
npm run build
```

预期：`vue-tsc --noEmit && vite build` 退出码 0，`dist/` 生成成功。

- [ ] **Step 2: 产物级检查——令牌已解析、死类已消失**

```bash
cd D:/demo/wealth-service-platform/front
echo "=== 令牌别名（应为 var(--bg-card) / var(--bg-page) 等前台令牌） ==="
grep -o "\-\-fl-sidebar-bg:[^;]*" dist/assets/*.css
grep -o "\-\-fl-content-bg:[^;]*" dist/assets/*.css
grep -o "\-\-fl-primary:[^;]*" dist/assets/*.css
echo "=== 死令牌应无输出 ==="
grep -o "\-\-fl-primary-hover\|\-\-fl-radius-sm\|\-\-fl-fall-bg" dist/assets/*.css || echo "死令牌已清除"
echo "=== 死类应无输出 ==="
grep -o "\.fl-stat-card\|\.search-card\|\.fl-rise-bg" dist/assets/*.css || echo "死类已清除"
echo "=== 旧深蓝侧栏色应无输出 ==="
grep -o "#1a365d\|#2563eb" dist/assets/*.css || echo "深蓝侧栏色已清除"
```

预期：令牌值已是前台令牌引用；三段"应无输出"的检查全部输出"已清除"。

- [ ] **Step 3: 开发服务器 HTTP 冒烟**

```bash
cd D:/demo/wealth-service-platform/front
npm run dev
```

另开一个终端：

```bash
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:3000/admin/dashboard
```

预期：返回 `200`，且 dev server 终端无编译错误。验证完用 `Ctrl+C` 停掉 dev server。

这一步只证明应用能编译并响应请求，**不证明视觉正确**。

- [ ] **Step 4: 交付人工视觉核对清单**

向用户逐条输出下列清单，请其在浏览器自查。不要在未获确认时声称视觉已验收。

**管理端（视口 1440px）**

| 路径 | 重点确认 |
|------|---------|
| `/admin/dashboard` | 五区块顺序（欢迎区→汇总数据→快捷入口→订单+动态→行情快照）、无 ECharts、欢迎区为纯蓝渐变无绿调、六张指标卡图标统一品牌蓝、快捷入口彩色渐变可跳转、行情快照多列网格 |
| `/admin/product` | 标题左侧 4px 蓝竖条、筛选与表格为白底 10px 圆角卡片、主按钮蓝色渐变、新增/编辑弹窗、分页 |
| `/admin/market` | 同上，含行情表单 |
| `/admin/favorite` | 同上 |
| `/admin/user` | 同上，含勾选框与批量操作 |
| `/admin/trade` | 同上 |
| `/admin/message` | 同上 |
| `/admin/news` | 同上 |
| `/admin/search` | 关键字高亮为品牌蓝、空态为 `el-empty` 图形 |
| `/admin/system/admin` | 同 CRUD 页 |
| `/admin/system/role` | 同 CRUD 页 |
| `/admin/system/resource` | 同 CRUD 页 |
| `/admin/system/admin-role` | 同 CRUD 页 |
| `/admin/system/role-resource` | 同 CRUD 页 |
| `/admin/profile` | 密码强度条弱/中/强三档颜色正确（红/橙/灰） |
| 侧栏 | 白底 + 右侧分隔线、蓝图标 + 蓝色渐变「理财服务平台」、激活项浅蓝底蓝字、折叠可用、底部用户名可见 |
| 顶栏 | 搜索框可聚焦、Ctrl+K 生效、消息通知与用户下拉可用 |

**窄屏（视口 768px）**：侧栏收起为抽屉、内容单列堆叠、仪表盘订单行降为单列、汇总卡 2 列、表格可横向滚动、无横向溢出。

**前台回归（共享令牌别名化影响 9 处色值）**

| 路径 | 重点确认 |
|------|---------|
| `/home` | 主色仍为品牌蓝，无异常 |
| `/products` | 产品卡片、涨跌色正确 |
| `/market` | 行情页与 K 线弹窗正常（`KlinePanel` 未改动） |
| `/news` | 资讯列表正常 |
| `/user/profile` | 个人中心文字层级与边框正常（消费 `--fl-text*` / `--fl-border*`） |

- [ ] **Step 5: 汇总验证结论**

向用户报告：构建退出码、产物级检查的实际输出、HTTP 冒烟结果，以及**尚未由我验证的视觉部分**。**不要执行 `git commit` 或 `git push`**——由用户决定提交时机。

---

## Self-Review 记录

**Spec 覆盖核对**

| Spec 章节 | 对应任务 |
|-----------|---------|
| 令牌别名化（21 项映射） | Task 1 Step 2 |
| 死令牌与死样式清理 | Task 1 Step 2–3、Step 5 |
| 卡片头部 class 修复 | Task 1 Step 3 |
| 未定义变量修复（`--fl-text-primary`） | Task 7 Step 1 |
| 共享令牌影响面 | Task 8 Step 5 |
| AdminLayout 1280px 容器 | Task 2 Step 1 |
| Sidebar 白底 / 渐变 Logo / 浅蓝激活 / 改名 | Task 2 Step 2 |
| Navbar 样式微调（功能不动） | Task 2 Step 3 |
| AdminPageShell / AdminFilterBar / AdminDataTable | Task 3 Step 1–3 |
| AdminFormDialog 不改 | 未列入任务（Spec 明确无需改动） |
| `admin/profile` 3 处硬编码色 | Task 7 Step 2 |
| 欢迎区纯蓝渐变 + 告警卡统一 | Task 5 Step 1–2 |
| 汇总数据六色统一品牌蓝 | Task 5 Step 3 |
| 快捷入口复活并对齐前台 | Task 4 Step 5 |
| 行情快照列表保留 | Task 5 Step 4 |
| 消息资讯列表保留 | Task 5 Step 5 |
| 删除 TrendPanel 与图表 | Task 4 Step 2–3、Task 6 Step 3 |
| 新布局五区块 | Task 4 Step 2、Step 4 |
| 响应式断点 1024 / 480 | Task 4 Step 4、Task 8 Step 4 |
| `useAdminDashboard` 去趋势 | Task 4 Step 1 |
| 删除 3 组件 + 2 旧文档 | Task 6 |
| `KlinePanel` 原位保留 | Task 6 Step 2 |
| 构建 + 浏览器验证 | Task 8 |

**类型一致性**：Task 3 声明的 `AdminDataTable` props/emits 与 Task 4、Task 5 中各组件的 props 均取自现有代码，未做重命名。`useAdminDashboard()` 的返回字段在 Task 4 Step 1 移除两项后，Task 4 Step 2–3 的消费端同步去掉了对应字段，无悬空引用。

**已知偏差**：Spec 第 4 节写响应式"低于 480px 指标卡单列"，Task 4 Step 4 只显式写了 `.fl-ops-row` 的 1024px 断点；`DashboardMetricGrid` 的 1024px / 480px 断点在其自身 scoped 样式中已存在且 Task 5 Step 3 未触碰，故沿用现状，不重复声明。
