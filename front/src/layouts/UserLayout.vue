<template>
  <div class="layout-container">
    <header class="layout-header">
      <div class="header-inner">
        <div class="header-left">
          <div class="logo" @click="router.push(homePath)">
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
          <template v-if="!userStore.isLoggedIn">
            <el-button text @click="router.push('/auth/login')">登录</el-button>
            <el-button @click="router.push('/register')">注册</el-button>
          </template>

          <template v-else>
            <MessageNoticePopover target-path="/user/message" />

            <el-dropdown trigger="click" @command="handleCommand">
              <span class="user-info">
                <el-avatar :size="32" :src="userStore.avatar" class="user-avatar" />
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

        <button class="mobile-menu-btn" @click="mobileMenuOpen = !mobileMenuOpen">
          <el-icon :size="24"><MoreFilled /></el-icon>
        </button>
      </div>
    </header>

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
      <el-breadcrumb v-if="route.meta?.title && route.path !== homePath" class="layout-breadcrumb" separator="/">
        <el-breadcrumb-item :to="{ path: homePath }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>{{ route.meta.title as string }}</el-breadcrumb-item>
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
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/index'
import MessageNoticePopover from '@/components/MessageNoticePopover.vue'
import {
  HomeFilled, GoodsFilled, DataAnalysis, StarFilled,
  Money, Notebook, Message, UserFilled,
  ArrowDown, SwitchButton, MoreFilled, Close,
  Setting, Odometer, Avatar, Connection,
  User, Goods, List, Reading,
  Search, Edit, TrendCharts,
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const currentPath = computed(() => route.path)
const mobileMenuOpen = ref(false)
const isAdmin = computed(() => userStore.isAdmin)

const homePath = computed(() => userStore.isLoggedIn ? '/user/dashboard' : '/home')

const publicNavItems = computed(() => [
  { path: homePath.value, title: '首页', icon: HomeFilled },
  { path: '/products', title: '产品中心', icon: GoodsFilled },
  { path: '/market', title: '实时行情', icon: DataAnalysis },
  { path: '/news', title: '财经资讯', icon: Notebook },
])

const authNavItems = [
  { path: '/user/trade', title: '交易委托', icon: Money },
  { path: '/user/favorite', title: '我的自选', icon: StarFilled },
  { path: '/user/message', title: '消息中心', icon: Message },
]

const visibleNavItems = computed(() => {
  if (userStore.isLoggedIn) {
    return [...publicNavItems.value, ...authNavItems]
  }
  return publicNavItems.value
})

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
