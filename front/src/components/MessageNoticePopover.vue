<template>
  <div class="message-notice">
    <el-popover placement="bottom-end" :width="380" trigger="click" popper-class="message-notice-popper">
      <template #reference>
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="message-notice-badge">
          <el-button text @click="fetchUnread">
            <el-icon :size="20"><Bell /></el-icon>
          </el-button>
        </el-badge>
      </template>

      <div class="message-notice-panel">
        <div class="message-notice-header">
          <span class="message-notice-title">消息通知</span>
          <el-button v-if="unreadList.length > 0" text size="small" @click="markAllRead">全部已读</el-button>
        </div>

        <div v-if="loadingNotice" class="message-notice-loading">
          <el-skeleton :rows="3" animated />
        </div>
        <div v-else-if="unreadList.length === 0" class="message-notice-empty">
          <el-empty description="暂无未读消息" :image-size="60" />
        </div>
        <div v-else class="message-notice-list">
          <div
            v-for="msg in unreadList"
            :key="msg.id"
            class="message-notice-item"
            @click="handleRead(msg)"
          >
            <div class="message-notice-item-header">
              <span class="message-notice-item-type">{{ msgTypeText(msg.msgType) }}</span>
              <span class="message-notice-item-time">{{ formatTime(msg.createTime) }}</span>
            </div>
            <div class="message-notice-item-title">{{ msg.msgTitle }}</div>
            <div class="message-notice-item-content">{{ msg.msgContent }}</div>
          </div>
        </div>

        <div class="message-notice-footer">
          <el-button text size="small" @click="goTarget">查看全部消息</el-button>
        </div>
      </div>
    </el-popover>

    <el-dialog v-model="detailVisible" :title="detailMsg?.msgTitle" width="520" destroy-on-close>
      <div v-if="detailMsg" class="message-detail-body">
        <div class="message-detail-row">
          <span class="message-detail-label">类型</span>
          <span class="message-detail-type">{{ msgTypeText(detailMsg.msgType) }}</span>
        </div>
        <div class="message-detail-row">
          <span class="message-detail-label">时间</span>
          <span class="message-detail-value">{{ formatDateTime(detailMsg.createTime) }}</span>
        </div>
        <div class="message-detail-divider"></div>
        <div class="message-detail-content">{{ detailMsg.msgContent }}</div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Bell } from '@element-plus/icons-vue'
import { getMessagePage, readMessage, batchReadMessage } from '@/api/message'
import { useUserStore } from '@/store'
import { formatDateTime } from '@/utils/format'
import type { WeaMessage } from '@/types'

const props = defineProps<{
  targetPath: string
}>()

const router = useRouter()
const userStore = useUserStore()

const unreadCount = ref(0)
const unreadList = ref<WeaMessage[]>([])
const loadingNotice = ref(false)
const detailVisible = ref(false)
const detailMsg = ref<WeaMessage | null>(null)

function msgTypeText(type?: number) {
  const map: Record<number, string> = {
    1: '行情提醒',
    2: '资讯推送',
    3: '委托通知',
    4: '活动通知',
  }
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
    unreadList.value = (res?.records || []) as WeaMessage[]
    unreadCount.value = res?.total || 0
  } catch {
    // Global request handler owns user-facing errors.
  } finally {
    loadingNotice.value = false
  }
}

async function handleRead(msg: WeaMessage) {
  if (!msg.id) return
  detailMsg.value = msg
  detailVisible.value = true

  try {
    await readMessage(msg.id)
    msg.readFlag = 1
    unreadList.value = unreadList.value.filter(item => item.id !== msg.id)
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  } catch {
    // Global request handler owns user-facing errors.
  }
}

async function markAllRead() {
  const ids = unreadList.value.map(item => item.id).filter(Boolean) as (number | string)[]
  if (ids.length === 0) return

  try {
    await batchReadMessage(ids)
    unreadList.value = []
    unreadCount.value = 0
  } catch {
    // Global request handler owns user-facing errors.
  }
}

function goTarget() {
  router.push(props.targetPath)
}

watch(() => userStore.userId, () => {
  fetchUnread()
})

onMounted(fetchUnread)
</script>

<style>
.message-notice {
  line-height: 1;
}

.message-notice-popper {
  padding: 0 !important;
}

.message-notice-badge {
  line-height: 1;
}

.message-notice-panel {
  display: flex;
  flex-direction: column;
  max-height: 400px;
}

.message-notice-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.message-notice-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.message-notice-loading {
  padding: 16px;
}

.message-notice-empty {
  padding: 20px 0;
}

.message-notice-list {
  overflow-y: auto;
  max-height: 300px;
}

.message-notice-item {
  padding: 12px 16px;
  cursor: pointer;
  border-bottom: 1px solid var(--el-border-color-lighter);
  transition: background 0.2s;
}

.message-notice-item:hover {
  background: #f5f7fa;
}

.message-notice-item:last-child {
  border-bottom: none;
}

.message-notice-item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

.message-notice-item-type {
  font-size: 11px;
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  padding: 1px 6px;
  border-radius: 4px;
}

.message-notice-item-time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.message-notice-item-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  margin-bottom: 2px;
}

.message-notice-item-content {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-notice-footer {
  padding: 8px 16px;
  border-top: 1px solid var(--el-border-color-light);
  text-align: center;
}

.message-detail-body {
  padding: 8px 0;
}

.message-detail-row {
  display: flex;
  align-items: center;
  padding: 6px 0;
}

.message-detail-label {
  width: 60px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  flex-shrink: 0;
}

.message-detail-type {
  font-size: 12px;
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  padding: 1px 8px;
  border-radius: 4px;
}

.message-detail-value {
  font-size: 13px;
  color: var(--el-text-color-primary);
}

.message-detail-divider {
  height: 1px;
  background: var(--el-border-color-light);
  margin: 12px 0;
}

.message-detail-content {
  font-size: 14px;
  line-height: 1.8;
  color: var(--el-text-color-primary);
  white-space: pre-wrap;
}
</style>
