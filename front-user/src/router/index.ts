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
        meta: { title: '首页', icon: 'HomeFilled' },
      },
      {
        path: 'product',
        component: () => import('@/views/product/index.vue'),
        meta: { title: '产品中心', icon: 'GoodsFilled' },
      },
      {
        path: 'market',
        component: () => import('@/views/market/index.vue'),
        meta: { title: '实时行情', icon: 'DataAnalysis' },
      },
      {
        path: 'favorite',
        component: () => import('@/views/favorite/index.vue'),
        meta: { title: '我的自选', icon: 'StarFilled' },
      },
      {
        path: 'trade',
        component: () => import('@/views/trade/index.vue'),
        meta: { title: '交易委托', icon: 'Money' },
      },
      {
        path: 'news',
        component: () => import('@/views/news/index.vue'),
        meta: { title: '财经资讯', icon: 'Notebook' },
      },
      {
        path: 'message',
        component: () => import('@/views/message/index.vue'),
        meta: { title: '消息中心', icon: 'Message' },
      },
      {
        path: 'profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人中心', icon: 'UserFilled' },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard',
  },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

router.beforeEach((to, _from, next) => {
  if (to.meta.requiresAuth && !getToken()) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else if (to.path === '/login' && getToken()) {
    next('/')
  } else {
    next()
  }
})

export default router
