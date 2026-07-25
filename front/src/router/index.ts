import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { nextTick } from 'vue'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store'

declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    icon?: string
    requiresAuth?: boolean
    requiresAdmin?: boolean
    group?: string
    keepAlive?: boolean // B1: 是否启用 KeepAlive 缓存，默认 true
  }
}

const routes: RouteRecordRaw[] = [
  // ==================== 公共路由（UserLayout，无需登录） ====================
  {
    path: '/',
    component: () => import('@/layouts/UserLayout.vue'),
    redirect: '/home',
    children: [
      { path: 'home',     component: () => import('@/views/home/index.vue'),  meta: { title: '首页' } },
      { path: 'products', component: () => import('@/views/products/index.vue'), meta: { title: '产品中心' } },
      { path: 'market',   component: () => import('@/views/market/index.vue'),  meta: { title: '实时行情' } },
      { path: 'news',     component: () => import('@/views/news/index.vue'),    meta: { title: '财经资讯' } },
      { path: 'auth/login', component: () => import('@/views/auth/login/index.vue'), meta: { title: '登录', keepAlive: false } },
      { path: 'register', component: () => import('@/views/register/index.vue'),   meta: { title: '注册', keepAlive: false } },
    ],
  },

  // ==================== 用户路由（UserLayout，需登录） ====================
  {
    path: '/user',
    component: () => import('@/layouts/UserLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: 'dashboard', component: () => import('@/views/dashboard/index.vue'), meta: { title: '个人中心' } },
      { path: 'profile',   component: () => import('@/views/profile/index.vue'),   meta: { title: '个人资料' } },
      { path: 'favorite',  component: () => import('@/views/favorite/index.vue'),  meta: { title: '我的自选' } },
      { path: 'trade',     component: () => import('@/views/trade/index.vue'),     meta: { title: '交易委托', keepAlive: false } },
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
      { path: 'dashboard',      component: () => import('@/views/admin/dashboard/index.vue'),   meta: { title: '控制台', icon: 'Monitor', group: 'dashboard' } },
      { path: 'profile',        component: () => import('@/views/admin/profile/index.vue'),     meta: { title: '个人信息', icon: 'User', group: 'profile' } },
      { path: 'user',           component: () => import('@/views/admin/user/index.vue'),         meta: { title: '用户管理', icon: 'User', group: 'user' } },
      { path: 'system/admin',   component: () => import('@/views/admin/system/admin/index.vue'), meta: { title: '管理员管理', icon: 'Setting', group: 'system' } },
      { path: 'system/role',    component: () => import('@/views/admin/system/role/index.vue'),  meta: { title: '角色管理', icon: 'Avatar', group: 'system' } },
      { path: 'system/resource',component: () => import('@/views/admin/system/resource/index.vue'), meta: { title: '资源管理', icon: 'Connection', group: 'system' } },
      { path: 'product',        component: () => import('@/views/admin/product/index.vue'),      meta: { title: '产品管理', icon: 'Goods', group: 'product' } },
      { path: 'market',         component: () => import('@/views/admin/market/index.vue'),       meta: { title: '行情数据', icon: 'DataLine', group: 'product' } },
      { path: 'trade',          component: () => import('@/views/admin/trade/index.vue'),        meta: { title: '交易管理', icon: 'List', group: 'trade' } },
      { path: 'favorite',       component: () => import('@/views/admin/favorite/index.vue'),     meta: { title: '自选管理', icon: 'Star', group: 'product' } },
      { path: 'message',        component: () => import('@/views/admin/message/index.vue'),      meta: { title: '站内消息', icon: 'Message', group: 'message' } },
      { path: 'news',           component: () => import('@/views/admin/news/index.vue'),         meta: { title: '资讯管理', icon: 'Reading', group: 'message' } },
      { path: 'system/admin-role',   component: () => import('@/views/admin/system/adminRole/index.vue'),   meta: { title: '管理员角色关联', icon: 'Link', group: 'system' } },
      { path: 'system/role-resource',component: () => import('@/views/admin/system/roleResource/index.vue'), meta: { title: '角色资源关联', icon: 'Connection', group: 'system' } },
      { path: 'search',         component: () => import('@/views/admin/search/index.vue'),       meta: { title: '搜索管理', icon: 'Search', group: 'search' } },
    ],
  },

  // ==================== 错误页面 ====================
  { path: '/403', name: 'Forbidden', component: () => import('@/views/error/Forbidden.vue'), meta: { title: '权限不足' } },
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('@/views/error/NotFound.vue') },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// NProgress 配置：关闭转圈，只保留顶部进度条
NProgress.configure({ showSpinner: false })

// 导航守卫 — 认证 + 角色校验 + 动态标题
let isInitialNavigation = true

router.beforeEach((to, _from) => {
  if (isInitialNavigation) {
    isInitialNavigation = false
  } else {
    NProgress.start()
  }
  // 动态更新页面标题
  const title = to.meta.title as string
  if (title) {
    document.title = `${title} | 理财服务平台`
  }

  const userStore = useUserStore()

  // S2: 导航时重新校验令牌过期状态
  if (userStore.isLoggedIn) {
    const expired = userStore.checkTokenExpired()
    if (expired) {
      ElMessage.warning('登录已过期，请重新登录')
      return { path: '/auth/login', replace: true }
    }
  }

  // 已登录用户访问登录页 → 跳转对应首页
  if (to.path === '/auth/login' && userStore.isLoggedIn) {
    return userStore.isAdmin ? '/admin/dashboard' : '/home'
  }

  // 不需要认证的路由直接放行
  if (!to.meta.requiresAuth) {
    return true
  }

  // 检查登录状态
  if (!userStore.isLoggedIn) {
    const loginPath = '/auth/login'
    ElMessage.warning('请先登录后再访问')
    // 使用 replace 避免回退时陷入登录页死循环
    return { path: loginPath, query: { redirect: to.fullPath }, replace: true }
  }

  // 检查管理员权限
  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    ElMessage.warning('当前账号暂无管理员权限')
    return { path: '/403', query: { redirect: to.fullPath }, replace: true }
  }

  return true
})

router.afterEach(() => {
  // L5：延迟到 DOM 更新后结束进度条，避免懒加载组件未渲染时进度条已消失
  nextTick(() => NProgress.done())
})

router.onError(() => {
  NProgress.done()
  ElMessage.error('页面加载失败，请稍后重试')
})

export default router
