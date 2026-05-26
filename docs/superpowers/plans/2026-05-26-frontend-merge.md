# 前端合并实施方案

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 `front-user/`（用户前台）合并到 `front/`（管理后台），构建单一 SPA，通过路由前缀 `/admin/` 和 `/user/` 区分。

**Architecture:** 保留 `front/` 目录，在 `views/` 下分 `admin/` 和 `user/` 两个子目录容纳各自页面，共享 `api/`、`utils/`、`types/`、`stores/`。路由统一由 `vue-router` 管理，`vite.config.ts` base 改为 `/`。

**Tech Stack:** Vue 3 + Vite 6 + Element Plus + Pinia + TypeScript

---

### Task 1: 整理共享代码层（types、api、utils）

**Files:**
- Modify: `front/src/types/index.ts`
- Modify: `front/src/api/index.ts`
- Modify: `front/src/utils/auth.ts`
- Modify: `front/src/utils/format.ts`
- Modify: `front/src/api/user.ts`
- Modify: `front/src/api/product.ts`
- Modify: `front/src/api/trade.ts`
- Modify: `front/src/api/message.ts`
- Modify: `front/src/api/favorite.ts`

- [ ] **Step 1: 合并 types/index.ts — 补充 front-user 独有类型**

front-user 有 `NEWS_TYPE_OPTIONS`，front 没有。将 `NEWS_TYPE_OPTIONS` 追加到 `front/src/types/index.ts` 末尾：

```typescript
// types/index.ts (追加在 PRODUCT_TYPE_OPTIONS 之后)
export const NEWS_TYPE_OPTIONS: DictItem[] = [
  { label: '行业动态', value: 1 },
  { label: '市场分析', value: 2 },
  { label: '政策解读', value: 3 },
  { label: '公司公告', value: 4 },
]
```

- [ ] **Step 2: 合并 api/index.ts — 处理 401 重定向兼容**

front-api/index.ts 的 401 重定向写死了 `#/login`，需要区分 admin/user。改为根据当前 hash 前缀决定重定向目标：

```typescript
// api/index.ts — 修改两处 401 处理
// 第 26 行: window.location.hash = '#/login'
// 改为:
const redirectLogin = () => {
  const hash = window.location.hash
  if (hash.startsWith('#/user')) {
    window.location.hash = '#/user/login'
  } else {
    window.location.hash = '#/admin/login'
  }
}

// response 拦截器中: redirectLogin() 替换 window.location.hash = '#/login'
// error 拦截器中: redirectLogin() 替换 window.location.hash = '#/login'
```

- [ ] **Step 3: 合并 utils/auth.ts — 支持两套 token key**

将 admin/user 两套 localStorage key 合并到一个文件，用路由前缀区分：

```typescript
const ADMIN_LOGIN_KEY = 'wealth_admin_logged_in'
const ADMIN_USER_KEY = 'wealth_admin_user'
const USER_LOGIN_KEY = 'wealth_user_logged_in'
const USER_USER_KEY = 'wealth_user_info'

/** 根据路径前缀返回对应的 key 名 */
function getKeys(path?: string) {
  const isUser = path && path.startsWith('/user')
  return {
    LOGIN_KEY: isUser ? USER_LOGIN_KEY : ADMIN_LOGIN_KEY,
    USER_KEY: isUser ? USER_USER_KEY : ADMIN_USER_KEY,
  }
}

export function getToken(path?: string): string | null {
  const { LOGIN_KEY } = getKeys(path)
  return localStorage.getItem(LOGIN_KEY)
}

export function setToken(_token: string, path?: string) {
  const { LOGIN_KEY } = getKeys(path)
  localStorage.setItem(LOGIN_KEY, 'true')
}

export function removeToken(path?: string) {
  const keys = path ? [getKeys(path)] : [getKeys('/admin'), getKeys('/user')]
  keys.forEach(({ LOGIN_KEY, USER_KEY }) => {
    localStorage.removeItem(LOGIN_KEY)
    localStorage.removeItem(USER_KEY)
  })
}

export function getStoredUser(path?: string): any {
  const { USER_KEY } = getKeys(path)
  const raw = localStorage.getItem(USER_KEY)
  return raw ? JSON.parse(raw) : null
}

export function setStoredUser(user: any, path?: string) {
  const { USER_KEY } = getKeys(path)
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}
```

- [ ] **Step 4: 合并 utils/format.ts — 补充 front-user 独有的格式化函数**

将 `formatRelativeTime`、`newsTypeText`、`msgTypeText` 追加到 `front/src/utils/format.ts`：

```typescript
// format.ts — 追加以下函数
export function formatRelativeTime(time: string | undefined | null): string {
  if (!time) return '-'
  const now = dayjs()
  const t = dayjs(time)
  const diffMin = now.diff(t, 'minute')
  if (diffMin < 1) return '刚刚'
  if (diffMin < 60) return `${diffMin}分钟前`
  const diffHour = now.diff(t, 'hour')
  if (diffHour < 24) return `${diffHour}小时前`
  const diffDay = now.diff(t, 'day')
  if (diffDay < 7) return `${diffDay}天前`
  return dayjs(time).format('MM-DD HH:mm')
}

export function newsTypeText(type: number | undefined | null): string {
  const map: Record<number, string> = { 1: '行业动态', 2: '市场分析', 3: '政策解读', 4: '公司公告' }
  return type !== null && type !== undefined ? map[type] || '-' : '-'
}

export function msgTypeText(type: number | undefined | null): string {
  const map: Record<number, string> = { 1: '系统通知', 2: '交易提醒', 3: '风控通知', 4: '活动通知' }
  return type !== null && type !== undefined ? map[type] || '-' : '-'
}
```

- [ ] **Step 5: 合并 api/user.ts — 取两者并集**

front（管理端）有 `getUserById`、`createUser`、`deleteUser`、`deleteUserBatch`、`userLogin`；
front-user（用户端）没有 `deleteUserBatch` 但结构类似。
合并后保留所有函数（取并集），方法签名统一用宽松的 `any`。

```typescript
// api/user.ts — 保留所有导出函数，不做删减
// 已同时包含：
// getUserPage, getUserList, getUserById, createUser, updateUser, deleteUser,
// deleteUserBatch, registerUser, resetPassword, userLogin
// 不需要改动
```

> front-user 比 front 少 `getUserById`, `createUser`, `deleteUserBatch` —— front 已有这些，无需处理。

- [ ] **Step 6: 合并 api/product.ts — 取并集**

front 有完整的 CRUD（create/update/delete），front-user 只有查询（page/list/byId）。front 已覆盖全部，无需修改。

- [ ] **Step 7: 合并 api/trade.ts — 取并集**

front 有 `updateTradeOrder`、`deleteTradeOrder`，front-user 有 `cancelTradeOrder`、`createTradeOrder` 带类型签名。
合并后保留所有函数，front-user 的类型签名可保留但放宽为 `any`。

```typescript
// api/trade.ts — 补充 front-user 独有函数
export function cancelTradeOrder(id: number) {
  return request.put(`/trade/wea-trade-order/${id}`, { orderStatus: 2 })
}
```

- [ ] **Step 8: 合并 api/message.ts — 取两者并集**

front 有完整的 CRUD（含 create/update/delete for 资讯和消息），front-user 只有查询 + `readMessage`。
合并后保留所有函数：

```typescript
// api/message.ts — 补充 front-user 独有函数
export function readMessage(id: number) {
  return request.put(`/message/wea-message/${id}`, { readFlag: 1 })
}
```

- [ ] **Step 9: 合并 api/favorite.ts — 保留所有函数**

front 有完整 CRUD，front-user 只有 page/list/create/delete。front 已覆盖全部，无需修改。

---

### Task 2: 创建 AdminLayout 和 UserLayout

**Files:**
- Rename: `front/src/layout/` → AdminLayout 视图保持不变（它是管理后台 layout）
- Copy: `front-user/src/layout/index.vue` → `front/src/layouts/UserLayout.vue`

- [ ] **Step 10: 创建 layouts 目录，移动 AdminLayout**

```bash
mkdir -p front/src/layouts
mv front/src/layout/index.vue front/src/layouts/AdminLayout.vue
mv front/src/layout/Navbar.vue front/src/layouts/
mv front/src/layout/Sidebar.vue front/src/layouts/
mv front/src/layout/TagsView.vue front/src/layouts/  # 确认此文件是否存在
```

> 注意：如果 layout 子组件引用路径带 `@/layout/`，需要同步更新 import 为 `@/layouts/`。

- [ ] **Step 11: 复制 front-user 的 layout 为 UserLayout.vue**

```bash
cp front-user/src/layout/index.vue front/src/layouts/UserLayout.vue
```

修改 UserLayout.vue 中的 navItems 路径前缀（加上 `/user`）：

```typescript
// UserLayout.vue — 修改路由路径
const navItems = [
  { path: '/user/dashboard', title: '首页', icon: HomeFilled },
  { path: '/user/product', title: '产品中心', icon: GoodsFilled },
  { path: '/user/market', title: '实时行情', icon: DataAnalysis },
  { path: '/user/favorite', title: '我的自选', icon: StarFilled },
  { path: '/user/trade', title: '交易委托', icon: Money },
  { path: '/user/news', title: '财经资讯', icon: Notebook },
  { path: '/user/message', title: '消息中心', icon: Message },
]
```

---

### Task 3: 整合视图文件

**Files:**
- Move: 所有 `front-user/src/views/` 下的文件移入 `front/src/views/user/`

- [ ] **Step 12: 复制用户视图到 front/src/views/user/**

```bash
mkdir -p front/src/views/user
cp -r front-user/src/views/dashboard front/src/views/user/dashboard
cp -r front-user/src/views/product front/src/views/user/product
cp -r front-user/src/views/market front/src/views/user/market
cp -r front-user/src/views/trade front/src/views/user/trade
cp -r front-user/src/views/favorite front/src/views/user/favorite
cp -r front-user/src/views/news front/src/views/user/news
cp -r front-user/src/views/message front/src/views/user/message
cp -r front-user/src/views/profile front/src/views/user/profile
cp -r front-user/src/views/login front/src/views/user/login
cp -r front-user/src/views/register front/src/views/register
```

- [ ] **Step 13: 重命名 admin 登录视图路径**

```bash
# front 原有 login 视图 → views/admin/login
mkdir -p front/src/views/admin
mv front/src/views/login front/src/views/admin/login
mv front/src/views/dashboard front/src/views/admin/dashboard
mv front/src/views/user front/src/views/admin/user         # 用户管理（管理员视角）
mv front/src/views/system front/src/views/admin/system
mv front/src/views/product front/src/views/admin/product
mv front/src/views/market front/src/views/admin/market
mv front/src/views/trade front/src/views/admin/trade
mv front/src/views/favorite front/src/views/admin/favorite
mv front/src/views/news front/src/views/admin/news
mv front/src/views/message front/src/views/admin/message
mv front/src/views/search front/src/views/admin/search
```

---

### Task 4: 整合路由

**Files:**
- Modify: `front/src/router/index.ts`
- Delete: `front-user/src/router/index.ts`（合并不再需要）

- [ ] **Step 14: 重写路由文件**

```typescript
// front/src/router/index.ts — 合并后的路由
import { createRouter, createWebHashHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { getToken } from '@/utils/auth'

declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    icon?: string
    requiresAuth?: boolean
  }
}

const routes: RouteRecordRaw[] = [
  // ==================== 公共 ====================
  { path: '/', redirect: '/admin/dashboard' },

  // ==================== 管理后台 ====================
  {
    path: '/admin/login',
    component: () => import('@/views/admin/login/index.vue'),
    meta: { title: '管理员登录' },
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    meta: { requiresAuth: true },
    children: [
      { path: 'dashboard',      component: () => import('@/views/admin/dashboard/index.vue'),   meta: { title: '控制台', icon: 'Monitor' } },
      { path: 'user',           component: () => import('@/views/admin/user/index.vue'),         meta: { title: '用户管理', icon: 'User' } },
      { path: 'system/admin',   component: () => import('@/views/admin/system/admin/index.vue'), meta: { title: '管理员管理', icon: 'Setting' } },
      { path: 'system/role',    component: () => import('@/views/admin/system/role/index.vue'),  meta: { title: '角色管理', icon: 'Avatar' } },
      { path: 'system/resource',component: () => import('@/views/admin/system/resource/index.vue'), meta: { title: '资源管理', icon: 'Connection' } },
      { path: 'product',        component: () => import('@/views/admin/product/index.vue'),      meta: { title: '产品管理', icon: 'Goods' } },
      { path: 'market',         component: () => import('@/views/admin/market/index.vue'),       meta: { title: '行情数据', icon: 'DataLine' } },
      { path: 'trade',          component: () => import('@/views/admin/trade/index.vue'),        meta: { title: '交易管理', icon: 'List' } },
      { path: 'favorite',       component: () => import('@/views/admin/favorite/index.vue'),     meta: { title: '自选管理', icon: 'Star' } },
      { path: 'message',        component: () => import('@/views/admin/message/index.vue'),      meta: { title: '站内消息', icon: 'Message' } },
      { path: 'news',           component: () => import('@/views/admin/news/index.vue'),         meta: { title: '资讯管理', icon: 'Reading' } },
      { path: 'system/admin-role',   component: () => import('@/views/admin/system/adminRole/index.vue'),   meta: { title: '管理员角色关联', icon: 'Link' } },
      { path: 'system/role-resource',component: () => import('@/views/admin/system/roleResource/index.vue'), meta: { title: '角色资源关联', icon: 'Connection' } },
      { path: 'search',         component: () => import('@/views/admin/search/index.vue'),       meta: { title: '搜索管理', icon: 'Search' } },
    ],
  },

  // ==================== 用户前台 ====================
  {
    path: '/user/login',
    component: () => import('@/views/user/login/index.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/user/register',
    component: () => import('@/views/register/index.vue'),
    meta: { title: '注册' },
  },
  {
    path: '/user',
    component: () => import('@/layouts/UserLayout.vue'),
    redirect: '/user/dashboard',
    meta: { requiresAuth: true },
    children: [
      { path: 'dashboard', component: () => import('@/views/user/dashboard/index.vue'), meta: { title: '首页', icon: 'HomeFilled' } },
      { path: 'product',   component: () => import('@/views/user/product/index.vue'),   meta: { title: '产品中心', icon: 'GoodsFilled' } },
      { path: 'market',    component: () => import('@/views/user/market/index.vue'),    meta: { title: '实时行情', icon: 'DataAnalysis' } },
      { path: 'favorite',  component: () => import('@/views/user/favorite/index.vue'),  meta: { title: '我的自选', icon: 'StarFilled' } },
      { path: 'trade',     component: () => import('@/views/user/trade/index.vue'),     meta: { title: '交易委托', icon: 'Money' } },
      { path: 'news',      component: () => import('@/views/user/news/index.vue'),      meta: { title: '财经资讯', icon: 'Notebook' } },
      { path: 'message',   component: () => import('@/views/user/message/index.vue'),   meta: { title: '消息中心', icon: 'Message' } },
      { path: 'profile',   component: () => import('@/views/user/profile/index.vue'),   meta: { title: '个人中心', icon: 'UserFilled' } },
    ],
  },

  // 404
  { path: '/:pathMatch(.*)*', redirect: '/admin/dashboard' },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

// 导航守卫 — 根据路径前缀校验对应 token
router.beforeEach((to, _from, next) => {
  if (!to.meta.requiresAuth) {
    next()
    return
  }

  const isUser = to.path.startsWith('/user')
  const token = getToken(isUser ? '/user' : '/admin')

  if (!token) {
    next({ path: isUser ? '/user/login' : '/admin/login', query: { redirect: to.fullPath } })
  } else if ((to.path === '/admin/login' || to.path === '/user/login') && token) {
    next(isUser ? '/user/dashboard' : '/admin/dashboard')
  } else {
    next()
  }
})

export default router
```

---

### Task 5: 更新 vite.config.ts 和 store

**Files:**
- Modify: `front/vite.config.ts`
- Modify: `front/src/main.ts`
- Modify: `front/src/store/index.ts`（如果 store name 冲突）

- [ ] **Step 15: 修改 vite.config.ts — base 改为 `/`**

```typescript
// front/vite.config.ts
export default defineConfig({
  base: '/',             // 改为 / 而不是 /admin/
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 3000,
    proxy: {
      '/api/v1': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api\/v1/, ''),
      },
    },
  },
})
```

- [ ] **Step 16: 更新 main.ts — 引入 global.css（front-user 的样式）**

```typescript
// front/src/main.ts — 引入 global.css 确保用户端样式生效
import './styles/theme.css'
import './styles/global.css'    // 新增：复制自 front-user
```

需要将 `front-user/src/styles/global.css` 复制到 `front/src/styles/global.css`。

- [ ] **Step 17: 确认 store 无冲突**

front/src/store/index.ts 使用 `defineStore('user', ...)`，front-user 也使用 `defineStore('user', ...)`。两者 pinia store name 相同但 state 结构不同。
解决方案：将 user store 统一为一个，包含所有字段（取并集）。

```typescript
// front/src/store/index.ts — 合并后的 user store
export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    username: getStoredUser()?.username || '',
    userId: getStoredUser()?.userId || 0,
    nickname: getStoredUser()?.nickname || '',
    avatar: getStoredUser()?.avatar || '',
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
  },
  actions: {
    async login(username: string, password: string) {
      const res = await loginApi({ username, password })
      setToken(res.data.accessToken as string)
      setStoredUser({ username })
      this.token = getToken() || ''
      this.username = username
    },
    async userLogin(username: string, password: string) {
      const res = await userLogin({ username, password })
      const { userId } = res.data
      const nickname = res.data.nickname || ''
      const avatar = res.data.avatar || ''
      setToken(res.data.token as string, '/user')
      setStoredUser({ username, userId, nickname, avatar }, '/user')
      this.token = getToken('/user') || ''
      this.username = username
      this.userId = userId
      this.nickname = nickname
      this.avatar = avatar
    },
    setUserInfo(info: { userId: number; nickname: string; avatar: string }) {
      this.userId = info.userId
      this.nickname = info.nickname
      this.avatar = info.avatar
      setStoredUser({ username: this.username, ...info }, '/user')
    },
    logout(path?: string) {
      this.token = ''
      this.username = ''
      this.userId = 0
      this.nickname = ''
      this.avatar = ''
      removeToken(path)
    },
  },
})
```

---

### Task 6: 验证编译

- [ ] **Step 18: 安装依赖并编译**

```bash
cd front
npm install
npx vue-tsc --noEmit
npx vite build
```

确保无 TS 错误和构建错误。

---

### Task 7: 启动 dev server 验证

- [ ] **Step 19: 启动开发服务器验证两个端**

```bash
npx vite
```

验证：
- `http://localhost:3000/#/admin/login` → 显示管理后台登录页
- `http://localhost:3000/#/user/login` → 显示用户前台登录页
- `http://localhost:3000/#/admin/dashboard` → 重定向到登录页（未登录）

---

### Task 8: 清理 front-user 目录

- [ ] **Step 20: 备份并删除 front-user**

```bash
# 确认一切正常后
# git rm -r front-user
# 或保留目录但标记为已废弃
```
