<template>
  <div class="layout-container">
    <header class="layout-header">
      <div class="header-inner">
        <div class="header-left">
          <div class="logo" @click="router.push('/')">
            <div class="logo-icon">
              <svg viewBox="0 0 32 32" width="28" height="28" fill="none">
                <circle cx="16" cy="16" r="14" stroke="currentColor" stroke-width="2" />
                <path d="M10 20 L16 10 L22 20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
              </svg>
            </div>
            <span class="logo-text">金融投资平台</span>
          </div>
        </div>

        <nav class="header-nav">
          <router-link
            v-for="item in navItems"
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
        </nav>

        <div class="header-right">
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
        </div>

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
            v-for="item in navItems"
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
        </div>
      </div>
    </div>

    <main class="layout-main">
      <router-view />
    </main>

    <footer class="layout-footer">
      <div class="footer-inner">
        <span>&copy; {{ new Date().getFullYear() }} 金融投资平台. All rights reserved.</span>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/index'
import {
  HomeFilled, GoodsFilled, DataAnalysis, StarFilled,
  Money, Notebook, Message, UserFilled,
  ArrowDown, SwitchButton, MoreFilled, Close,
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const currentPath = computed(() => route.path)
const mobileMenuOpen = ref(false)

const navItems = [
  { path: '/dashboard', title: '首页', icon: HomeFilled },
  { path: '/product', title: '产品中心', icon: GoodsFilled },
  { path: '/market', title: '实时行情', icon: DataAnalysis },
  { path: '/favorite', title: '我的自选', icon: StarFilled },
  { path: '/trade', title: '交易委托', icon: Money },
  { path: '/news', title: '财经资讯', icon: Notebook },
  { path: '/message', title: '消息中心', icon: Message },
]

function handleCommand(command: string) {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/profile')
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

.header-right {
  display: flex;
  align-items: center;
  flex-shrink: 0;
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
