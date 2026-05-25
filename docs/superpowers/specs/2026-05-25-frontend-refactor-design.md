# 前端重构设计文档 — 财富管理服务平台

> 基于设计提示词（已归档），对现有 `front/` 项目进行 UI 层重构
> 生成日期：2026-05-25

---

## 一、项目范围

在现有 `front/` 项目基础上重构，保留已有基础设施，只改 UI 层：

**保留（不改动）：**
- `src/api/` — 全部 API 封装（system/user/product/trade/message/favorite/search）
- `src/types/` — TypeScript 类型定义
- `src/utils/` — 格式化工具、认证工具、SSE 工具
- `src/store/` — Pinia 状态管理
- `src/router/` — 路由配置（新增 2 条子路由）
- `vite.config.ts` — Vite 配置
- `package.json` — 依赖声明

**重写：**
- `src/layout/` — Sidebar、Navbar、MainLayout 完全重写
- `src/styles/` — 新深色主题 CSS
- `src/views/` — 各页面按新配色调整（保留现有 CRUD 逻辑）
- `src/App.vue` — 根组件
- `src/main.ts` — 入口注册

**新增：**
- `src/views/system/adminRole/index.vue` — 管理员-角色关联
- `src/views/system/roleResource/index.vue` — 角色-资源关联
- `src/components/TagsView.vue` — 标签页切换组件
- `src/components/PageHeader.vue` — 页面标题组件

---

## 二、配色方案

| Token | 值 | 用途 |
|-------|-----|------|
| `--fl-primary` | `#1a6dff` | 主色 |
| `--fl-sidebar-bg` | `#1a365d` | 侧边栏背景 |
| `--fl-sidebar-text` | `#ffffff` | 侧边栏文字 |
| `--fl-sidebar-active` | `#2563eb` | 侧边栏激活 |
| `--fl-header-bg` | `#ffffff` | 顶栏背景 |
| `--fl-content-bg` | `#f0f2f5` | 内容区背景 |
| `--fl-card-bg` | `#ffffff` | 卡片背景 |
| `--fl-border` | `#e4e7ed` | 边框色 |
| `--fl-rise` | `#e74c3c` | 涨（红色） |
| `--fl-fall` | `#27ae60` | 跌（绿色） |
| `--fl-text` | `#303133` | 主文字 |
| `--fl-text-secondary` | `#606266` | 次要文字 |
| `--fl-text-dim` | `#909399` | 辅助文字 |

---

## 三、布局结构

### MainLayout
```
┌─────────────────────┬──────────────────────────────────────┐
│ Sidebar (220px)     │ Navbar (面包屑/搜索/通知/用户)       │
│ ↓ 可折叠 64px       ├──────────────────────────────────────┤
│ Logo + 菜单         │ TagsView (el-tabs, 可关闭标签页)     │
│ 分组导航            ├──────────────────────────────────────┤
│ 深蓝底 #1a365d     │ Content (<router-view />)           │
│                     │                                      │
│                     │ Footer (版本号)                       │
└─────────────────────┴──────────────────────────────────────┘
```

### Sidebar
- 宽度 220px，折叠至 64px（仅显示图标）
- Element Plus `el-menu` 配合 `dark` 背景
- Logo 区域显示「财富管理平台」+ 金融图标
- 分组菜单：系统管理、用户管理、产品管理、交易管理、消息管理
- 单页菜单：控制面板、产品搜索
- 底部显示当前管理员信息 + 退出按钮

### Navbar
- 面包屑导航（左侧）
- 全局产品搜索输入框（点击跳转 /search）
- 消息通知图标（未读数量徽标）
- 用户头像 + 下拉菜单（个人信息/退出登录）

### TagsView
- 基于 Element Plus `el-tabs` 组件
- 页面切换时自动添加标签
- 标签可关闭（至少保留一个）
- 支持右键菜单（关闭其他/关闭所有）
- 刷新当前标签页

---

## 四、视图页面清单

### Login 登录页
- 保留现有角色表情动画、眼球追踪效果
- 配色微调适配新主题
- 表单字段：用户名 + 密码
- 接口：`POST /system/umsAdmin/login`

### Dashboard 仪表盘
- 4 个统计卡片行：产品总数、今日委托单数、待处理消息数、在线用户数
- SSE 实时行情卡片网格（每个产品一个卡片，涨/跌颜色）
- 最近 5 条资讯列表（调用资讯接口）
- 快捷入口行（复用现有）

### 系统管理
- **管理员管理** `/system/admin` — 现有关键不改造型
- **角色管理** `/system/role` — 现有关键不改造型
- **资源管理** `/system/resource` — 现有关键不改造型
- **管理员-角色关联** `/system/admin-role` — 新增，分页表格 + 添加关联弹窗
- **角色-资源关联** `/system/role-resource` — 新增，分页表格 + 添加关联弹窗

### 用户管理
- **用户列表** `/user` — 现有关键不改造型

### 产品管理
- **产品列表** `/product` — 现有关键不改造型
- **行情数据** `/market` — 现有关键不改造型
- **用户自选** `/favorite` — 现有关键不改造型

### 交易管理
- **交易委托** `/trade` — 现有关键不改造型

### 消息管理
- **站内消息** `/message` — 现有关键不改造型
- **财经资讯** `/news` — 现有关键不改造型

### 搜索
- **产品搜索** `/search` — 现有关键不改造型

---

## 五、路由配置（调整后）

新增 2 条子路由，挂载到已有布局下：

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

---

## 六、通用组件

### TagsView
- 使用 `localStorage` 持久化已打开的标签页列表
- 使用 Pinia store 管理标签页状态
- 支持点击切换、双击关闭、右键菜单

### PageHeader
- 页面标题 + 描述（可选）
- 面包屑路径（可选）

### 错误处理（全局）
- 401 → 跳转登录页
- 网络错误 → ElMessage 提示
- 后端业务异常 → 由 axios 拦截器统一处理

---

## 七、改动范围总结

| 维度 | 统计 |
|------|------|
| 新文件 | ~5 个（TagsView, PageHeader, adminRole, roleResource, 新 theme CSS） |
| 修改文件 | ~15 个（Layout/Sidebar/Navbar + 所有 View 的 `<style>` 部分） |
| 删除文件 | `styles/light-theme.css`（替换为新主题） |
| 保留不动 | ~20 个（API/Types/Utils/Store/Router/Vite/package.json） |

无依赖变更，npm install 无需重新执行。
