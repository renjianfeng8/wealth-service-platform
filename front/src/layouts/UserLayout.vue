<template>
  <div class="layout-container">
    <header class="layout-header">
      <div class="header-inner">
        <div class="header-left">
          <div class="logo" @click="router.push('/home')">
            <div class="logo-icon">
              <el-icon :size="28"><TrendCharts /></el-icon>
            </div>
            <span class="logo-text">理财服务平台</span>
          </div>
        </div>

        <nav class="header-nav">
          <router-link
            v-for="item in visibleNavItems"
            :key="item.path"
            :to="item.path"
            class="nav-item"
            :class="{ active: currentPath.startsWith(item.path) }"
          >
            <el-icon :size="18">
              <component :is="item.icon" />
            </el-icon>
            <span>{{ item.title }}</span>
          </router-link>

          <!-- 管理后台入口（仅管理员） -->
          <el-dropdown v-if="isAdmin" trigger="hover" class="admin-dropdown">
            <span class="nav-item admin-entry" :class="{ active: currentPath.startsWith('/admin/') }">
              <el-icon :size="18"><Setting /></el-icon>
              <span>系统管理</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/admin/dashboard')">
                  <el-icon><Odometer /></el-icon>控制台
                </el-dropdown-item>
                <el-dropdown-item @click="router.push('/admin/system/admin')">
                  <el-icon><UserFilled /></el-icon>管理员管理
                </el-dropdown-item>
                <el-dropdown-item @click="router.push('/admin/system/role')">
                  <el-icon><Avatar /></el-icon>角色管理
                </el-dropdown-item>
                <el-dropdown-item @click="router.push('/admin/system/resource')">
                  <el-icon><Connection /></el-icon>资源管理
                </el-dropdown-item>
                <el-dropdown-item @click="router.push('/admin/product')">
                  <el-icon><Goods /></el-icon>产品管理
                </el-dropdown-item>
                <el-dropdown-item @click="router.push('/admin/user')">
                  <el-icon><User /></el-icon>用户管理
                </el-dropdown-item>
                <el-dropdown-item @click="router.push('/admin/trade')">
                  <el-icon><List /></el-icon>交易管理
                </el-dropdown-item>
                <el-dropdown-item @click="router.push('/admin/news')">
                  <el-icon><Reading /></el-icon>资讯管理
                </el-dropdown-item>
                <el-dropdown-item @click="router.push('/admin/message')">
                  <el-icon><Message /></el-icon>消息管理
                </el-dropdown-item>
                <el-dropdown-item @click="router.push('/admin/search')">
                  <el-icon><Search /></el-icon>搜索管理
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </nav>

        <div class="header-right">
          <!-- 未登录 → 登录/注册按钮 -->
          <template v-if="!userStore.isLoggedIn">
            <el-button text @click="router.push('/auth/login')">登录</el-button>
            <el-button @click="router.push('/register')">注册</el-button>
          </template>

          <!-- 已登录 → 用户信息 -->
          <template v-else>
            <el-popover placement="bottom-end" :width="380" trigger="click" popper-class="notice-popover">
              <template #reference>
                <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notice-badge">
                  <el-button text @click="fetchUnread">
                    <el-icon :size="20"><Bell /></el-icon>
                  </el-button>
                </el-badge>
              </template>
              <div class="notice-panel">
                <div class="notice-header">
                  <span class="notice-header-title">消息通知</span>
                  <el-button v-if="unreadList.length > 0" text size="small" @click="markAllRead">全部已读</el-button>
                </div>
                <div v-if="loadingNotice" class="notice-loading">
                  <el-skeleton :rows="3" animated />
                </div>
                <div v-else-if="unreadList.length === 0" class="notice-empty">
                  <el-empty description="暂无未读消息" :image-size="60" />
                </div>
                <div v-else class="notice-list">
                  <div v-for="msg in unreadList" :key="msg.id" class="notice-item" @click="handleRead(msg)">
                    <div class="notice-item-header">
                      <span class="notice-item-type">{{ msgTypeText(msg.msgType) }}</span>
                      <span class="notice-item-time">{{ formatTime(msg.createTime) }}</span>
                    </div>
                    <div class="notice-item-title">{{ msg.msgTitle }}</div>
                    <div class="notice-item-content">{{ msg.msgContent }}</div>
                  </div>
                </div>
                <div class="notice-footer">
                  <el-button text size="small" @click="router.push('/user/message')">查看全部消息</el-button>
                </div>
              </div>
            </el-popover>

            <el-dropdown trigger="click" @command="handleCommand">
              <span class="user-info">
                <el-avatar :size="32" :icon="UserFilled" class="user-avatar" />
                <span class="username">{{ userStore.nickname || userStore.username }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><UserFilled /></el-icon>个人中心
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><SwitchButton /></el-icon>退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </div>

        <!-- 消息详情弹窗 -->
        <el-dialog v-model="detailVisible" :title="detailMsg?.msgTitle" width="520" destroy-on-close>
          <div v-if="detailMsg" class="msg-detail-body">
            <div class="msg-detail-row">
              <span class="msg-detail-label">类型</span>
              <span class="msg-detail-type">{{ msgTypeText(detailMsg.msgType) }}</span>
            </div>
            <div class="msg-detail-row">
              <span class="msg-detail-label">时间</span>
              <span class="msg-detail-value">{{ formatDateTime(detailMsg.createTime) }}</span>
            </div>
            <div class="msg-detail-divider"></div>
            <div class="msg-detail-content">{{ detailMsg.msgContent }}</div>
          </div>
          <template #footer>
            <el-button @click="detailVisible = false">关闭</el-button>
          </template>
        </el-dialog>

        <!-- 移动端菜单按钮 -->
        <button class="mobile-menu-btn" @click="mobileMenuOpen = !mobileMenuOpen">
          <el-icon :size="24"><MoreFilled /></el-icon>
        </button>
      </div>
    </header>

    <!-- 移动端菜单 -->
    <div v-if="mobileMenuOpen" class="mobile-menu-overlay" @click="mobileMenuOpen = false">
      <div class="mobile-menu" @click.stop>
        <div class="mobile-menu-header">
          <span class="mobile-menu-title">导航菜单</span>
          <button class="mobile-close" @click="mobileMenuOpen = false">
            <el-icon><Close /></el-icon>
          </button>
        </div>
        <div class="mobile-menu-items">
          <router-link
            v-for="item in visibleNavItems"
            :key="item.path"
            :to="item.path"
            class="mobile-nav-item"
            :class="{ active: currentPath.startsWith(item.path) }"
            @click="mobileMenuOpen = false"
          >
            <el-icon :size="18">
              <component :is="item.icon" />
            </el-icon>
            <span>{{ item.title }}</span>
          </router-link>
          <template v-if="!userStore.isLoggedIn">
            <router-link to="/auth/login" class="mobile-nav-item" @click="mobileMenuOpen = false">
              <el-icon><User /></el-icon><span>登录</span>
            </router-link>
            <router-link to="/register" class="mobile-nav-item" @click="mobileMenuOpen = false">
              <el-icon><Edit /></el-icon><span>注册</span>
            </router-link>
          </template>
        </div>
      </div>
    </div>

    <main class="layout-main">
      <el-breadcrumb v-if="route.meta?.title" class="layout-breadcrumb" separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item v-if="route.meta?.title">{{ route.meta.title as string }}</el-breadcrumb-item>
      </el-breadcrumb>
      <router-view />
    </main>

    <footer class="layout-footer">
      <div class="footer-inner">
        <span>&copy; {{ new Date().getFullYear() }} 理财服务平台. All rights reserved.</span>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/index'
import { getMessagePage, readMessage } from '@/api/message'
import {
  HomeFilled, GoodsFilled, DataAnalysis, StarFilled,
  Money, Notebook, Message, UserFilled,
  ArrowDown, SwitchButton, MoreFilled, Close,
  Setting, Odometer, Avatar, Connection,
  User, Goods, List, Reading,
  Search, Edit, TrendCharts, Bell,
} from '@element-plus/icons-vue'
import { formatDateTime } from '@/utils/format'
import type { WeaMessage } from '@/types'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const currentPath = computed(() => route.path)
const mobileMenuOpen = ref(false)
const isAdmin = computed(() => userStore.isAdmin)

const unreadCount = ref(0)
const unreadList = ref<WeaMessage[]>([])
const loadingNotice = ref(false)
const detailVisible = ref(false)
const detailMsg = ref<WeaMessage | null>(null)

// 公开导航项（始终显示）
const publicNavItems = [
  { path: '/home', title: '首页', icon: HomeFilled },
  { path: '/products', title: '产品中心', icon: GoodsFilled },
  { path: '/market', title: '实时行情', icon: DataAnalysis },
  { path: '/news', title: '财经资讯', icon: Notebook },
]

// 登录后导航项
const authNavItems = [
  { path: '/user/trade', title: '交易委托', icon: Money },
  { path: '/user/favorite', title: '我的自选', icon: StarFilled },
  { path: '/user/message', title: '消息中心', icon: Message },
]

const visibleNavItems = computed(() => {
  if (userStore.isLoggedIn) {
    return [...publicNavItems, ...authNavItems]
  }
  return publicNavItems
})

function msgTypeText(type?: number) {
  const map: Record<number, string> = { 1: '系统通知', 2: '交易提醒', 3: '风控通知', 4: '活动通知' }
  return type ? map[type] || '系统通知' : '系统通知'
}

function formatTime(time?: string) {
  if (!time) return ''
  const d = new Date(time)
  const pad = (n: number) => n.toString().padStart(2, '0')
  return `${d.getMonth() + 1}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

async function fetchUnread() {
  if (!userStore.userId) {
    unreadCount.value = 0
    unreadList.value = []
    return
  }
  loadingNotice.value = true
  try {
    const res = await getMessagePage({
      pageNum: 1,
      pageSize: 5,
      userId: userStore.userId,
      readFlag: 0,
    })
    unreadList.value = (res.data?.records || []) as WeaMessage[]
    unreadCount.value = res.data?.total || 0
  } catch {
    // ignore
  } finally {
    loadingNotice.value = false
  }
}

async function handleRead(msg: WeaMessage) {
  if (!msg.id) return
  detailMsg.value = msg
  detailVisible.value = true
  // 打开弹窗即标记已读
  try {
    await readMessage(msg.id)
    msg.readFlag = 1
    unreadList.value = unreadList.value.filter(m => m.id !== msg.id)
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  } catch {
    // handled globally
  }
}

async function markAllRead() {
  const ids = unreadList.value.map(m => m.id).filter(Boolean) as number[]
  if (ids.length === 0) return
  try {
    await Promise.all(ids.map(id => readMessage(id)))
    unreadList.value = []
    unreadCount.value = 0
  } catch {
    // handled globally
  }
}

watch(() => userStore.userId, () => {
  fetchUnread()
})

onMounted(fetchUnread)

function handleCommand(command: string) {
  if (command === 'logout') {
    userStore.logout()
    router.push('/home')
  } else if (command === 'profile') {
    router.push('/user/profile')
  }
}
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-page);
}

.layout-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: var(--bg-navbar);
  border-bottom: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
}

.header-inner {
  max-width: 1280px;
  margin: 0 auto;
  height: var(--navbar-height);
  display: flex;
  align-items: center;
  padding: 0 24px;
  gap: 8px;
}

.header-left {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.logo-icon {
  color: var(--primary);
  display: flex;
  align-items: center;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  white-space: nowrap;
}

.header-nav {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 0 24px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-regular);
  transition: var(--transition-fast);
  white-space: nowrap;
  text-decoration: none;
  cursor: pointer;
}

.nav-item:hover {
  background: var(--primary-light);
  color: var(--primary);
}

.nav-item.active {
  background: var(--primary-light);
  color: var(--primary);
  font-weight: 600;
}

.admin-dropdown .admin-entry {
  border-left: 1px solid var(--border-color);
  margin-left: 8px;
  padding-left: 16px;
}

.header-right {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  gap: 4px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 8px;
  transition: var(--transition-fast);
}

.user-info:hover {
  background: var(--border-light);
}

.user-avatar {
  background: var(--primary-light);
  color: var(--primary);
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.notice-badge {
  line-height: 1;
}

.mobile-menu-btn {
  display: none;
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  color: var(--text-regular);
}

.layout-main {
  flex: 1;
  max-width: 1280px;
  width: 100%;
  margin: 0 auto;
  padding: 24px;
}

.layout-breadcrumb {
  margin-bottom: 16px;
}

.layout-footer {
  background: var(--bg-footer);
  color: rgba(255, 255, 255, 0.5);
  text-align: center;
  padding: 16px 24px;
  font-size: 13px;
}

.footer-inner {
  max-width: 1280px;
  margin: 0 auto;
}

/* 移动端菜单 */
.mobile-menu-overlay {
  display: none;
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 200;
}

.mobile-menu {
  position: absolute;
  right: 0;
  top: 0;
  bottom: 0;
  width: 280px;
  background: #fff;
  box-shadow: var(--shadow-lg);
  overflow-y: auto;
}

.mobile-menu-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-color);
}

.mobile-menu-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.mobile-close {
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  color: var(--text-regular);
}

.mobile-menu-items {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.mobile-nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 500;
  color: var(--text-regular);
  text-decoration: none;
  transition: var(--transition-fast);
}

.mobile-nav-item:hover,
.mobile-nav-item.active {
  background: var(--primary-light);
  color: var(--primary);
}

@media (max-width: 900px) {
  .header-nav {
    display: none;
  }
  .header-right {
    display: none;
  }
  .mobile-menu-btn {
    display: flex;
    margin-left: auto;
  }
  .mobile-menu-overlay {
    display: block;
  }
  .layout-main {
    padding: 16px;
  }
}
</style>

<style>
/* 通知面板全局样式（popover 不支持 scoped） */
.notice-popover {
  padding: 0 !important;
}
.notice-panel {
  display: flex;
  flex-direction: column;
  max-height: 400px;
}
.notice-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid var(--el-border-color-light);
}
.notice-header-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}
.notice-loading {
  padding: 16px;
}
.notice-empty {
  padding: 20px 0;
}
.notice-list {
  overflow-y: auto;
  max-height: 300px;
}
.notice-item {
  padding: 12px 16px;
  cursor: pointer;
  border-bottom: 1px solid var(--el-border-color-lighter);
  transition: background 0.2s;
}
.notice-item:hover {
  background: #f5f7fa;
}
.notice-item:last-child {
  border-bottom: none;
}
.notice-item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}
.notice-item-type {
  font-size: 11px;
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  padding: 1px 6px;
  border-radius: 4px;
}
.notice-item-time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.notice-item-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  margin-bottom: 2px;
}
.notice-item-content {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.notice-footer {
  padding: 8px 16px;
  border-top: 1px solid var(--el-border-color-light);
  text-align: center;
}
/* 消息详情弹窗 */
.msg-detail-body {
  padding: 8px 0;
}
.msg-detail-row {
  display: flex;
  align-items: center;
  padding: 6px 0;
}
.msg-detail-label {
  width: 60px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  flex-shrink: 0;
}
.msg-detail-type {
  font-size: 12px;
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  padding: 1px 8px;
  border-radius: 4px;
}
.msg-detail-value {
  font-size: 13px;
  color: var(--el-text-color-primary);
}
.msg-detail-divider {
  height: 1px;
  background: var(--el-border-color-light);
  margin: 12px 0;
}
.msg-detail-content {
  font-size: 14px;
  line-height: 1.8;
  color: var(--el-text-color-primary);
  white-space: pre-wrap;
}
</style>
