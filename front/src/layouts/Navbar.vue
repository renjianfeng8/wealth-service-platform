<template>
  <header class="navbar">
    <div class="navbar-left">
      <el-button text class="collapse-btn" @click="$emit('toggle')">
        <el-icon :size="20"><Fold v-if="!collapsed" /><Expand v-else /></el-icon>
      </el-button>
      <el-breadcrumb>
        <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>{{ route.meta?.title as string || '' }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="navbar-right">
      <div class="search-box" @click="goSearch">
        <el-icon><Search /></el-icon>
        <span class="search-placeholder">搜索产品...</span>
      </div>

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
            <div
              v-for="msg in unreadList"
              :key="msg.id"
              class="notice-item"
              @click="handleRead(msg)"
            >
              <div class="notice-item-header">
                <span class="notice-item-type">{{ msgTypeText(msg.msgType) }}</span>
                <span class="notice-item-time">{{ formatTime(msg.createTime) }}</span>
              </div>
              <div class="notice-item-title">{{ msg.msgTitle }}</div>
              <div class="notice-item-content">{{ msg.msgContent }}</div>
            </div>
          </div>
          <div class="notice-footer">
            <el-button text size="small" @click="router.push('/admin/message')">查看全部消息</el-button>
          </div>
        </div>
      </el-popover>

      <el-dropdown trigger="click">
        <span class="user-dropdown">
          <el-avatar :size="28" icon="UserFilled" />
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
  </header>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store'
import { getMessagePage, readMessage } from '@/api/message'
import {
  Fold, Expand, Search, Bell,
  ArrowDown, User, SwitchButton,
} from '@element-plus/icons-vue'
import { formatDateTime } from '@/utils/format'
import type { WeaMessage } from '@/types'

defineProps<{ collapsed: boolean }>()
defineEmits<{ toggle: [] }>()

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const unreadCount = ref(0)
const unreadList = ref<WeaMessage[]>([])
const loadingNotice = ref(false)
const detailVisible = ref(false)
const detailMsg = ref<WeaMessage | null>(null)

function goSearch() {
  router.push('/admin/search')
}

function handleLogout() {
  userStore.logout()
  router.push('/home')
}

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
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  background: #f5f7fa;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--fl-text-dim);
}
.search-box:hover {
  background: #eef1f6;
}
.search-placeholder {
  font-size: 13px;
  white-space: nowrap;
}
.search-shortcut {
  font-size: 11px;
  padding: 1px 6px;
  background: #e4e7ed;
  border-radius: 4px;
  color: var(--fl-text-placeholder);
}
.notice-badge {
  line-height: 1;
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
