import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    icon?: string
    requiresAuth?: boolean
    requiresAdmin?: boolean
  }
}

const routes: RouteRecordRaw[] = [
  // ==================== 公共路由（UnifiedLayout，无需登录） ====================
  {
    path: '/',
    component: () => import('@/layouts/UserLayout.vue'),
    redirect: '/home',
    children: [
      { path: 'home',     component: () => import('@/views/home/index.vue'),  meta: { title: '首页' } },
      { path: 'products', component: () => import('@/views/products/index.vue'), meta: { title: '产品中心' } },
      { path: 'market',   component: () => import('@/views/market/index.vue'),  meta: { title: '实时行情' } },
      { path: 'news',     component: () => import('@/views/news/index.vue'),    meta: { title: '财经资讯' } },
      { path: 'auth/login', component: () => import('@/views/auth/login/index.vue'), meta: { title: '登录' } },
      { path: 'register', component: () => import('@/views/register/index.vue'),   meta: { title: '注册' } },
    ],
  },

  // ==================== 用户路由（UnifiedLayout，需登录） ====================
  {
    path: '/user',
    component: () => import('@/layouts/UserLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: 'dashboard', component: () => import('@/views/dashboard/index.vue'), meta: { title: '个人中心' } },
      { path: 'profile',   component: () => import('@/views/profile/index.vue'),   meta: { title: '个人资料' } },
      { path: 'favorite',  component: () => import('@/views/favorite/index.vue'),  meta: { title: '我的自选' } },
      { path: 'trade',     component: () => import('@/views/trade/index.vue'),     meta: { title: '交易委托' } },
      { path: 'message',   component: () => import('@/views/message/index.vue'),   meta: { title: '消息中心' } },
    ],
  },

  // ==================== 管理后台（AdminLayout，需管理员权限） ====================
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    meta: { requiresAuth: true, requiresAdmin: true },
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

  // ==================== 独立页面 ====================
  { path: '/admin/login', component: () => import('@/views/admin/login/index.vue'), meta: { title: '管理员登录' } },

  // 404
  { path: '/:pathMatch(.*)*', redirect: '/home' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 导航守卫 — 认证 + 角色校验 + 动态标题
router.beforeEach((to, _from, next) => {
  // 动态更新页面标题
  const title = to.meta.title as string
  if (title) {
    document.title = `${title} | 理财服务平台`
  }

  // 不需要认证的路由直接放行
  if (!to.meta.requiresAuth) {
    next()
    return
  }

  // 检查登录状态
  const loggedIn = sessionStorage.getItem('wealth_logged_in') === 'true'
  if (!loggedIn) {
    const loginPath = to.path.startsWith('/admin/') ? '/admin/login' : '/auth/login'
    next({ path: loginPath, query: { redirect: to.fullPath } })
    return
  }

  // 已登录访问登录页 → 跳转对应首页
  if (to.path === '/auth/login' || to.path === '/admin/login') {
    const role = sessionStorage.getItem('wealth_role')
    next(role === 'admin' ? '/admin/dashboard' : '/home')
    return
  }

  // 检查管理员权限
  if (to.meta.requiresAdmin) {
    const role = sessionStorage.getItem('wealth_role')
    if (role !== 'admin') {
      next('/home')
      return
    }
  }

  next()
})

export default router
