# 管理端前台同源品牌化改造

## 目标

将后台管理控制台改造成与前台同源的蓝白品牌风格，替换上一版「浅色高级运营台」的青绿配色方案。所有页面复用前台视觉规范：白底侧栏、品牌蓝渐变 Logo、浅蓝激活态、白底卡片、蓝色渐变主按钮、1280px 内容宽度。

接口、权限、路由、业务数据加载逻辑保持不变。本轮只做前端 UI 重构。

## 背景与根因

项目里存在**两套并行色板**，这是"后台看起来不像前台"的根本原因：

- 前台页面（`home`、`products`、`market`、`UserLayout`）消费 `global.css` 的 `--primary`、`--bg-page`、`--bg-card`、`--text-*`、`--radius`、`--shadow-*`。
- 后台页面消费 `theme.css` 的 `--fl-*` 系列。

两套色值接近但不同源（正文 `#303133` vs `#1c1c1e`、边框 `#e4e7ed` vs `#e5e5ea`、卡片阴影 `0 2px 12px/.06` vs `0 1px 3px/.06`），且后台还有深蓝色侧栏、蓝绿混渐变欢迎区、六色图标块等自成一体的装饰，导致视觉割裂。

`theme.css` 由 `main.ts` 全局引入，但 `--fl-*` 令牌的使用者**几乎全在后台**，只有两个例外：`views/profile/index.vue`（前台个人中心）与 `components/FloatingActions.vue`（全局悬浮操作组件）。

Element Plus 组件（按钮渐变、弹窗圆角、表格、分页、输入框）已由 `global.css` 全局覆盖，**本来就是前台同款**。真正需要改造的只有后台自定义 class 与 `--fl-*` 色板。

## 视觉系统（令牌层）

### 令牌别名化

将 `theme.css` 中 `--fl-*` 的**值**改为引用前台令牌，使前台成为唯一真源。

| 令牌 | 现值 | 新值 |
|------|------|------|
| `--fl-primary` | `#1a6dff` | `var(--primary)` |
| `--fl-content-bg` | `#f0f2f5` | `var(--bg-page)` |
| `--fl-card-bg` | `#ffffff` | `var(--bg-card)` |
| `--fl-text` | `#303133` | `var(--text-primary)` |
| `--fl-text-secondary` | `#606266` | `var(--text-regular)` |
| `--fl-text-dim` | `#909399` | `var(--text-secondary)` |
| `--fl-text-placeholder` | `#c0c4cc` | `var(--text-placeholder)` |
| `--fl-border` | `#e4e7ed` | `var(--border-color)` |
| `--fl-border-light` | `#dcdfe6` | `var(--border-light)` |
| `--fl-radius` | `10px` | `var(--radius)` |
| `--fl-shadow-hover` | `0 4px 20px rgba(0,0,0,0.10)` | `var(--shadow-md)` |
| `--fl-rise` | `#34c759` | `var(--rise-color)` |
| `--fl-fall` | `#ff3b30` | `var(--fall-color)` |
| `--fl-header-height` | `56px` | `var(--navbar-height)` |
| `--fl-header-bg` | `#ffffff` | `var(--bg-navbar)` |
| `--fl-header-border` | `#e4e7ed` | `var(--border-color)` |
| `--fl-sidebar-bg` | `#1a365d` | `var(--bg-card)` |
| `--fl-sidebar-text` | `#ffffffb3` | `var(--text-regular)` |
| `--fl-sidebar-active` | `#ffffff` | `var(--primary)` |
| `--fl-sidebar-active-bg` | `#2563eb` | `var(--primary-light)` |
| `--fl-sidebar-hover-bg` | `rgba(255,255,255,0.08)` | `var(--el-fill-color-lighter)` |

`--fl-sidebar-width`（220px）与 `--fl-sidebar-collapsed-width`（64px）保持不变。

`--fl-rise` 与 `--fl-fall` 与前台 `--rise-color` / `--fall-color` 现值本来就完全相同，别名化属于纯去重，无视觉变化。

### 死令牌与死样式清理

经全量引用扫描，以下令牌**零引用**，直接删除：

`--fl-primary-hover`、`--fl-primary-light`、`--fl-radius-sm`、`--fl-shadow`、`--fl-rise-bg`、`--fl-fall-bg`

其中 `--fl-rise-bg` / `--fl-fall-bg` 仅被 `theme.css` 自身的 `.fl-rise-bg` / `.fl-fall-bg` 工具类消费，而这两个类在业务代码中同样零引用，一并删除。

`--fl-sidebar-active` 与 `--fl-sidebar-text` 当前也是零引用，但侧栏改造（移除 `el-menu` 硬编码颜色属性）后将被启用，**保留**。

同时清理 `theme.css` 中零引用的死样式类：`.fl-stat-grid`、`.fl-stat-card`、`.fl-stat-icon`、`.fl-stat-value`、`.fl-stat-label`、`.page-header`、`.search-card`。

`theme.css` 的全局 `.pagination-wrap` 声明也可删除：该 class 的实际使用者全在前台页面（`favorite`、`message`、`market`、`news`、`products`、`trade`），且各自用 `<style scoped>` 定义了同名规则并因属性选择器而优先，全局声明从未生效。

以下工具类**有真实引用，必须保留**：`.fl-card`（44 处）、`.fl-card-title`（10 处）、`.fl-rise` / `.fl-fall`（涨跌语义色）。

需要品牌蓝浅底的地方直接使用前台的 `var(--primary-light)`。

### 卡片头部 class 修复

`.fl-card-header` 与 `.fl-card-subtitle` 目前只在 `LatestActivities.vue` 和待删除的 `TrendPanel.vue` 中以 `<style scoped>` 定义，但 `LatestOrdersPanel.vue`、`MarketSnapshot.vue`、`DashboardQuickEntries.vue` 也在使用这两个 class。scoped 样式不跨组件，**这几个卡片头部当前的排版是失效的**。

将这两个 class 提升到 `theme.css` 全局定义，消除该缺陷。

### 未定义变量修复

`views/admin/search/index.vue:167` 使用了 `color: var(--fl-text-primary)`，但 `--fl-text-primary` **从未在任何地方声明**（`theme.css` 里只有同名的 CSS 类 `.fl-text-primary`，没有同名令牌）。该声明在计算值阶段失效，导致搜索结果里的关键字 `strong` 无法按预期高亮，只能继承上一条规则的灰色。

按 `.fl-text-primary` 类的语义（`color: var(--fl-primary)`），修复为 `color: var(--fl-primary)`。

（另有 `--fl-success` / `--fl-warning` 两处未定义变量，位于待删除的 `ActionQueuePanel.vue`，随文件删除自动解决。）

### 共享令牌的影响面

`--fl-text`、`--fl-text-secondary`、`--fl-text-dim`、`--fl-text-placeholder`、`--fl-border`、`--fl-border-light`、`--fl-primary`、`--fl-rise`、`--fl-fall` 共 9 个令牌同时被前台 `views/profile/index.vue` 与全局 `components/FloatingActions.vue` 消费。别名化后这些位置会出现轻微色值变化（如正文 `#303133` → `#3a3a3c`）。方向是向品牌色收敛，肉眼几乎不可见，视为修正而非回归。

## 页面框架

### AdminLayout

`.layout-content` 内部增加居中内容容器：`max-width: 1280px; margin: 0 auto; width: 100%`，桌面端内边距统一为 24px，对齐前台 `UserLayout` 的 `layout-main`。宽度低于 768px 时内边距回退为当前的 12px。此改动使全部后台页面（含 13 个 CRUD 页）自动获得统一的 1280px 内容宽度。

移动端抽屉逻辑与断点行为保持不变。

### Sidebar

- 底色改为白色，**增加右侧 1px `var(--fl-border)` 分隔线**（白底侧栏必须靠边框与内容区区分，否则视觉上会糊在一起）。
- Logo 改为前台同款：品牌蓝 `TrendCharts` 图标 + 渐变文字，使用 `linear-gradient(135deg, var(--primary), var(--primary-dark))` 配合 `background-clip: text`。
- Logo 文案由「财富管理平台」改为**「理财服务平台」**，与前台 `UserLayout` 完全一致。
- 移除 `el-menu` 上硬编码的 `background-color="#1a365d"`、`text-color="#ffffffb3"`、`active-text-color="#ffffff"` 三个属性，改由 CSS 变量驱动。
- 激活菜单项：浅蓝底 `var(--fl-sidebar-active-bg)` + 品牌蓝文字 `var(--fl-sidebar-active)`，圆角由 6px 改为 **8px**（对齐前台导航规范）。
- 子菜单背景由 `rgba(0,0,0,0.15)` 改为透明。
- 底部用户信息块需重绘为深色文字（当前全为白字，白底上不可见）。
- 多级菜单结构、折叠能力、移动端抽屉一律保留，`index` 路由值不变。

### Navbar

面包屑、全局产品搜索（含 Ctrl+K 快捷键与下拉结果）、消息通知、管理员下拉菜单、前台评测入口**功能一律不变**，仅调整样式：

- 搜索框圆角 6px 改为 8px，底色对齐 `var(--el-fill-color-lighter)`。
- 前台评测按钮 hover 色对齐 `var(--fl-primary)`。
- 高度维持 56px。

## 共享组件层

`AdminFormDialog.vue` **无需改动**：它没有自有样式，完全继承 `global.css` 的 `.el-dialog` 覆盖。

| 组件 | 改动 |
|------|------|
| `AdminPageShell.vue` | 标题色 `#1f2937` 改为 `var(--fl-text)`；增加前台 `.page-title` 同款的 4px 品牌蓝竖条（`::before`）；描述色 `#64748b` 改为 `var(--fl-text-secondary)` |
| `AdminFilterBar.vue` | `background/border/radius` 由硬编码 `#fff` / `#e5e7eb` / `8px` 改为 `var(--fl-card-bg)` / `var(--fl-border)` / `var(--fl-radius)`；内边距由 `16px 16px 2px` 改为 `20px 24px`，与 `.fl-card` 一致 |
| `AdminDataTable.vue` | 卡片化处理同上；内边距由 16px 改为 `20px 24px`；**空态由 `el-table` 的纯文字 `empty-text` 改为 `<el-empty :image-size="50">`**，与前台用法一致；分页保持右对齐，上边距统一为 16px |

受益页面共 13 个，均只消费上述共享组件与 `--fl-*` 令牌，经核查无硬编码颜色：

`admin/product`、`admin/market`、`admin/favorite`、`admin/user`、`admin/trade`、`admin/message`、`admin/news`、`admin/search`、`admin/system/admin`、`admin/system/role`、`admin/system/resource`、`admin/system/adminRole`、`admin/system/roleResource`

`admin/search/index.vue` 除共享组件外还使用 `--fl-text-primary` 未定义变量，按上述修复。

另有 `admin/profile/index.vue` 需单独处理 3 处硬编码颜色：

| 位置 | 现值 | 新值 |
|------|------|------|
| `.strength-bar` background | `#e4e7ed` | `var(--fl-border)` |
| `.strength-fill.medium` background | `#f5a623` | `var(--warning)` |
| `.strength-text.medium` color | `#f5a623` | `var(--warning)` |

该文件另有一处 `color: #fff`（`.avatar-overlay`），是深色遮罩 `rgba(0,0,0,0.55)` 上的白字，语义正确，**保持不变**。

## 仪表盘

### 保留区块

| 区块 | 组件 | 处理 |
|------|------|------|
| 品牌欢迎区 | `OperationsConsoleHeader` | 当前为蓝绿混渐变 `rgba(26,109,255,0.07) → rgba(25,190,107,0.05)` 配绿色「系统运行正常」圆点。改为前台 `.cta-section` 同款纯蓝渐变底 `linear-gradient(135deg, #f0f5ff, #e6f7ff)`，状态圆点改为品牌蓝。三张告警卡（待处理订单 / 未读消息 / 已下架产品）的图标底色与图标色统一为品牌蓝浅底 + 品牌蓝；数值颜色改为：待处理用 `var(--warning)`、未读用 `var(--fl-fall)`、"已下架产品" 归零态用 `var(--fl-text-dim)` 灰 —— 以彻底移除绿色，同时保留"需注意"的橙/红信号 |
| 平台汇总数据 | `DashboardMetricGrid` | 保留全部 6 项指标与跳转路径（`/admin/user`、`/admin/product`、`/admin/trade`、`/admin/message`，资产总值与今日收益为静态卡）。装饰性图标底色由当前蓝/绿/橙/红/紫/青六色统一为 `var(--primary-light)` + 品牌蓝图标；**涨跌语义色保持不变** |
| 管理快捷入口 | `DashboardQuickEntries` | 该组件当前无人引用，本轮复活复用。图标由 42px 改为 48px、圆角 12px，保留每项独立渐变（对齐前台首页 `quick-entry` 的蓝/绿/橙/紫），7 个入口路径不变 |
| 近期订单列表 | `LatestOrdersPanel` | 保留，样式对齐前台卡片规范。注意与欢迎区里的「待处理订单」告警卡**并非同一模块**：告警卡只显示待处理数量，本面板是最近 5 条委托明细 |
| 行情快照列表 | `MarketSnapshot` | 保留，含上涨/下跌筛选器 |
| 消息资讯列表 | `LatestActivities` | 保留站内消息动态列表 |

### 删除区块

`TrendPanel`（运营趋势与收益趋势两条 ECharts 折线）整体删除。仪表盘不再使用任何 ECharts 图表。

### 新布局

图表移除后重新编排为纯卡片 + 列表结构：

```
Row1  品牌欢迎区（全宽）
Row2  平台汇总数据（4 列网格）
Row3  管理快捷入口（全宽）
Row4  近期订单列表 2fr  +  消息资讯列表 1fr
Row5  行情快照列表（全宽）
```

响应式：宽度低于 1024px 时 Row4 降为单列、指标卡降为 2 列；低于 480px 时指标卡单列（沿用现有阈值，不新增断点）。

### 数据层

`useAdminDashboard` 移除 `loadTrend`、`trendData` 与 `getDashboardTrend` 的引入，`fetchData` 的 `Promise.allSettled` 去掉 `loadTrend()` 调用。

其余信号全部保留，`overview` 继续为资产总值与今日收益提供数据。后端 `getDashboardTrend` 接口本身保留不动。

## 清理清单

### 删除文件

- `front/src/views/admin/dashboard/components/TrendPanel.vue`
- `front/src/views/admin/dashboard/components/ActionQueuePanel.vue`
- `front/src/views/admin/dashboard/components/MiniProductStrip.vue`
- `docs/superpowers/specs/2026-09-14-admin-operations-redesign-design.md`
- `docs/superpowers/plans/2026-09-14-admin-operations-redesign.md`

### 保留原位

`front/src/views/admin/dashboard/components/KlinePanel.vue` —— 前台 `views/market/MarketDetailDialog.vue` 正在引用它。本轮不动，也不修复这处"前台引用后台组件"的越界耦合（超出本次范围）。

### 新增文件

- `docs/superpowers/specs/2026-09-14-admin-brand-redesign-design.md`（本文件）
- `docs/superpowers/plans/2026-09-14-admin-brand-redesign.md`（实施计划）

## 验证

前端目前无测试框架，`package.json` 仅有 `dev`、`build`、`preview` 三个脚本。本轮不引入测试框架。

1. **构建校验**：`cd front && npm run build`，须为 `vue-tsc --noEmit && vite build` 退出码 0。类型检查能捕获删除组件后遗留的失效引入。
2. **浏览器实测**：启动 dev server，在浏览器逐页实操核对：
   - 仪表盘 `/admin/dashboard`（六个区块顺序、快捷入口、空态）
   - 13 个 CRUD 页面（筛选、表格、空态、分页、弹窗增删改）
   - `admin/profile`
   - 登录页与错误页
   - 桌面 1440px 宽与 768px 窄屏各走一遍；核对侧栏折叠、移动端抽屉、表格横向滚动
3. **回归检查**：确认前台页面（首页、产品、行情、资讯、个人中心）未因共享令牌别名化出现异常色。

## 非目标

- 不修改后端代码、数据库、`init.sql`、YAML 配置、`pom.xml`、路由定义、权限拦截器或任何 API 契约。
- 不新增依赖。`echarts` 因 `KlinePanel` 仍被前台使用而保留。
- 不修改业务逻辑、数据加载语义与权限判断。
- 不重写各 CRUD 页的表单字段、表格列或校验规则。
- 不修复 `KlinePanel` 的跨层引用问题。

## 风险与回滚

纯前端样式与展示层改动，不触碰数据与权限。回滚方式为 `git revert`。

主要风险是共享令牌别名化会轻微改变前台个人中心与全局悬浮组件的 9 处色值，需在浏览器实测中一并核对。
