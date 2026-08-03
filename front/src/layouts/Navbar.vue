<template>
  <header class="navbar">
    <div class="navbar-left">
      <el-button text class="collapse-btn" @click="$emit('toggle')">
        <el-icon :size="20"><Fold v-if="!collapsed" /><Expand v-else /></el-icon>
      </el-button>
      <el-breadcrumb>
        <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item v-for="(item, idx) in breadcrumbItems" :key="idx" :to="item.path">{{ item.title }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="navbar-right">
      <div class="search-wrap" ref="searchWrapRef">
        <div class="search-box" :class="{ 'is-focused': searchFocused }">
          <el-input
            ref="searchInputRef"
            v-model="searchKeyword"
            placeholder="搜索产品..."
            :prefix-icon="Search"
            size="small"
            clearable
            @focus="onSearchFocus"
            @input="onSearchInput"
            @keydown.enter.prevent="onSearchEnter"
            @keydown.esc="closeSearchDropdown"
            @clear="onSearchClear"
          />
          <div class="search-shortcut-hint" v-if="!searchFocused && !searchKeyword">
            <kbd>Ctrl+K</kbd>
          </div>
        </div>

        <SearchDropdown
          :visible="showSearchDropdown"
          :keyword="searchKeyword"
          :results="searchResults"
          :total="searchTotal"
          :loading="searchLoading"
          @select="handleSelectResult"
          @view-all="doSearch"
        />
      </div>

      <el-tooltip content="前台评测" placement="bottom">
        <el-button text class="preview-btn" @click="router.push('/home')">
          <el-icon :size="18"><View /></el-icon>
        </el-button>
      </el-tooltip>

      <MessageNoticePopover target-path="/admin/message" />

      <el-dropdown trigger="click">
        <span class="user-dropdown">
          <el-avatar :size="28" :src="userStore.avatar" />
          <span class="username">{{ userStore.username || '管理员' }}</span>
          <el-icon><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="router.push('/admin/profile')">
              <el-icon><User /></el-icon>个人信息
            </el-dropdown-item>
            <el-dropdown-item divided @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store'
import MessageNoticePopover from '@/components/MessageNoticePopover.vue'
import SearchDropdown from '@/components/admin/SearchDropdown.vue'
import { getProductPage } from '@/api/product'
import type { WeaProduct } from '@/types'
import {
  Fold, Expand, Search,
  ArrowDown, User, SwitchButton, UserFilled, View,
} from '@element-plus/icons-vue'

defineProps<{ collapsed: boolean }>()
defineEmits<{ toggle: [] }>()

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

/* ---- 搜索 ---- */
const searchKeyword = ref('')
const searchInputRef = ref<{ focus: () => void } | null>(null)
const searchWrapRef = ref<HTMLElement>()
const searchFocused = ref(false)
const showSearchDropdown = ref(false)
const searchResults = ref<WeaProduct[]>([])
const searchLoading = ref(false)
const searchTotal = ref(0)

let searchTimer: ReturnType<typeof setTimeout> | null = null

function onSearchFocus() {
  searchFocused.value = true
  if (searchKeyword.value.trim()) {
    showSearchDropdown.value = true
  }
}

function onSearchInput(val: string | number) {
  const kw = String(val)
  if (searchTimer) clearTimeout(searchTimer)
  if (!kw.trim()) {
    searchResults.value = []
    searchTotal.value = 0
    showSearchDropdown.value = false
    return
  }
  searchTimer = setTimeout(() => fetchSearchResults(kw.trim()), 300)
}

function onSearchClear() {
  searchResults.value = []
  searchTotal.value = 0
  showSearchDropdown.value = false
}

function onSearchEnter() {
  if (!searchKeyword.value.trim()) return
  showSearchDropdown.value = false
  doSearch()
}

function closeSearchDropdown() {
  showSearchDropdown.value = false
  searchFocused.value = false
}

async function fetchSearchResults(keyword: string) {
  searchLoading.value = true
  showSearchDropdown.value = true
  try {
    const res = await getProductPage({
      pageNum: 1,
      pageSize: 6,
      productName: keyword,
      productCode: keyword,
    })
    searchResults.value = res?.records || []
    searchTotal.value = res?.total || 0
  } catch {
    searchResults.value = []
    searchTotal.value = 0
  } finally {
    searchLoading.value = false
  }
}

function handleSelectResult(item: WeaProduct) {
  showSearchDropdown.value = false
  searchFocused.value = false
  router.push({ path: '/admin/product' })
}

function doSearch() {
  const keyword = searchKeyword.value.trim()
  if (!keyword) return
  showSearchDropdown.value = false
  searchFocused.value = false
  router.push({ path: '/admin/search', query: { keyword } })
}

/* ---- 键盘快捷键 Ctrl+K ---- */
function onGlobalKeydown(e: KeyboardEvent) {
  if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
    e.preventDefault()
    searchInputRef.value?.focus()
    return
  }
}

/* ---- 点击外部关闭 ---- */
function onDocumentMousedown(e: MouseEvent) {
  if (searchWrapRef.value && !searchWrapRef.value.contains(e.target as Node)) {
    showSearchDropdown.value = false
    searchFocused.value = false
  }
}

onMounted(() => {
  document.addEventListener('keydown', onGlobalKeydown)
  document.addEventListener('mousedown', onDocumentMousedown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', onGlobalKeydown)
  document.removeEventListener('mousedown', onDocumentMousedown)
  if (searchTimer) clearTimeout(searchTimer)
})

const GROUP_LABELS: Record<string, string> = {
  dashboard: '仪表盘',
  profile: '个人中心',
  user: '用户管理',
  system: '系统管理',
  product: '产品管理',
  trade: '交易管理',
  message: '消息管理',
  search: '搜索',
}

const breadcrumbItems = computed(() => {
  const items: { title: string; path?: string }[] = []
  const group = route.meta?.group as string | undefined
  const pathSegments = route.path.replace('/admin/', '').split('/')
  if (group && GROUP_LABELS[group] && pathSegments.length > 1) {
    items.push({ title: GROUP_LABELS[group] })
  }
  if (route.meta?.title) {
    items.push({ title: route.meta.title as string })
  }
  return items
})

function handleLogout() {
  userStore.logout()
  router.push('/home')
}
</script>

<style scoped>
.navbar {
  height: var(--fl-header-height);
  background: var(--fl-header-bg);
  border-bottom: 1px solid var(--fl-header-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  flex-shrink: 0;
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.collapse-btn {
  font-size: 16px;
  color: var(--fl-text-secondary);
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.search-box {
  width: 180px;
  transition: width 0.25s ease;
  position: relative;
}

.search-box.is-focused {
  width: 300px;
}

.search-shortcut-hint {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  pointer-events: none;
}

.search-shortcut-hint kbd {
  font-size: 11px;
  padding: 1px 6px;
  background: #e4e7ed;
  border-radius: 4px;
  color: var(--fl-text-placeholder);
  font-family: inherit;
}

.search-wrap {
  position: relative;
}

.preview-btn {
  color: var(--fl-text-secondary);
  font-size: 13px;
}
.preview-btn:hover {
  color: var(--fl-primary);
}

.search-box .el-input__wrapper {
  background: #f5f7fa;
  border-radius: 6px;
  box-shadow: none;
}

.search-box .el-input__wrapper:hover {
  background: #eef1f6;
}

.search-box .el-input__wrapper.is-focus {
  background: #fff;
  box-shadow: 0 0 0 1px var(--el-color-primary) inset;
}

.search-shortcut {
  font-size: 11px;
  padding: 1px 6px;
  background: #e4e7ed;
  border-radius: 4px;
  color: var(--fl-text-placeholder);
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background 0.2s;
}

.user-dropdown:hover {
  background: #f5f7fa;
}

.username {
  font-size: 13px;
  color: var(--fl-text-secondary);
  font-weight: 500;
}
</style>
