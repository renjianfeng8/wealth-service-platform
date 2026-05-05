import { createRouter, createWebHashHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { getToken } from '@/utils/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '控制台', icon: 'Monitor' },
      },
      {
        path: 'user',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '用户管理', icon: 'User' },
      },
      {
        path: 'system/admin',
        component: () => import('@/views/system/admin/index.vue'),
        meta: { title: '管理员管理', icon: 'Setting' },
      },
      {
        path: 'system/role',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色管理', icon: 'Avatar' },
      },
      {
        path: 'system/resource',
        component: () => import('@/views/system/resource/index.vue'),
        meta: { title: '资源管理', icon: 'Connection' },
      },
      {
        path: 'product',
        component: () => import('@/views/product/index.vue'),
        meta: { title: '产品管理', icon: 'Goods' },
      },
      {
        path: 'market',
        component: () => import('@/views/market/index.vue'),
        meta: { title: '行情数据', icon: 'DataLine' },
      },
      {
        path: 'trade',
        component: () => import('@/views/trade/index.vue'),
        meta: { title: '交易管理', icon: 'List' },
      },
      {
        path: 'favorite',
        component: () => import('@/views/favorite/index.vue'),
        meta: { title: '自选管理', icon: 'Star' },
      },
      {
        path: 'message',
        component: () => import('@/views/message/index.vue'),
        meta: { title: '站内消息', icon: 'Message' },
      },
      {
        path: 'news',
        component: () => import('@/views/news/index.vue'),
        meta: { title: '资讯管理', icon: 'Reading' },
      },
      {
        path: 'search',
        component: () => import('@/views/search/index.vue'),
        meta: { title: '搜索管理', icon: 'Search' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

router.beforeEach((to, _from, next) => {
  if (to.meta.requiresAuth && !getToken()) {
    next('/login')
  } else if (to.path === '/login' && getToken()) {
    next('/')
  } else {
    next()
  }
})

export default router
