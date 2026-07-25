<template>
  <div class="message-page">
    <div class="page-title">消息中心</div>

    <el-card class="message-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>站内消息</span>
          <div class="header-stats">
            <span class="unread-badge">未读: {{ unreadCount }}</span>
            <el-button v-if="unreadCount > 0" text type="primary" size="small" @click="handleMarkAllRead">全部已读</el-button>
          </div>
        </div>
      </template>

      <div v-if="loading" class="loading-wrap">
        <el-skeleton :rows="5" animated />
      </div>
      <div v-else-if="messages.length === 0" class="empty-wrap">
        <el-empty description="暂无消息" />
      </div>
      <div v-else class="message-list">
        <div
          v-for="item in messages"
          :key="item.id"
          class="message-item"
          :class="{ unread: item.readFlag !== 1 }"
          @click="handleRead(item)"
        >
          <div class="message-indicator">
            <span v-if="item.readFlag !== 1" class="unread-dot" />
          </div>
          <div class="message-body">
            <div class="message-header">
              <el-tag size="small" :type="msgTagType(item.msgType)" effect="plain">
                {{ msgTypeText(item.msgType) }}
              </el-tag>
              <span class="message-time">{{ formatRelativeTime(item.createTime) }}</span>
            </div>
            <h4 class="message-title">{{ item.msgTitle }}</h4>
            <p class="message-content">{{ truncate(item.msgContent, 100) }}</p>
          </div>
        </div>
      </div>

      <div class="pagination-wrap">
        <el-pagination
          v-if="total > 0"
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="fetchMessages"
          @size-change="fetchMessages"
        />
      </div>
    </el-card>

    <!-- 详情 -->
    <el-dialog v-model="detailVisible" :title="detailItem?.msgTitle" width="560" destroy-on-close>
      <div class="detail-meta">
        <el-tag size="small" :type="msgTagType(detailItem?.msgType)" effect="plain">
          {{ msgTypeText(detailItem?.msgType) }}
        </el-tag>
        <span class="detail-time">{{ formatDateTime(detailItem?.createTime) }}</span>
      </div>
      <div class="detail-content">{{ detailItem?.msgContent }}</div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/index'
import { getMessagePage, readMessage, batchReadMessage } from '@/api/message'
import { formatDateTime, formatRelativeTime, msgTypeText } from '@/utils/format'
import type { WeaMessage } from '@/types'

const userStore = useUserStore()

const messages = ref<WeaMessage[]>([])
const loading = ref(false)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(15)
const detailVisible = ref(false)
const detailItem = ref<WeaMessage | null>(null)

const unreadCount = computed(() => messages.value.filter((m) => m.readFlag !== 1).length)

function truncate(text: string | undefined, len: number): string {
  if (!text) return ''
  return text.length > len ? text.substring(0, len) + '...' : text
}

function msgTagType(msgType: number | undefined): string {
  const map: Record<number, string> = { 1: 'info', 2: 'warning', 3: 'danger', 4: 'success' }
  return msgType ? map[msgType] || 'info' : 'info'
}

async function fetchMessages() {
  loading.value = true
  try {
    const params: { pageNum: number; pageSize: number; userId?: number } = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (userStore.userId) params.userId = userStore.userId
    const res = await getMessagePage(params)
    messages.value = (res.data?.records || []) as WeaMessage[]
    total.value = res.data?.total || 0
  } catch {
    messages.value = []
  } finally {
    loading.value = false
  }
}

async function handleRead(item: WeaMessage) {
  detailItem.value = item
  detailVisible.value = true
  if (item.readFlag !== 1 && item.id) {
    try {
      await readMessage(item.id)
      item.readFlag = 1
    } catch { /* ignore */ }
  }
}

async function handleMarkAllRead() {
  const unreadIds = messages.value.filter(m => m.readFlag !== 1 && m.id).map(m => m.id as number)
  if (unreadIds.length === 0) return
  try {
    await batchReadMessage(unreadIds)
    messages.value.forEach(m => { m.readFlag = 1 })
    ElMessage.success('已全部标为已读')
  } catch {
    // handled globally
  }
}

onMounted(() => {
  if (userStore.userId) fetchMessages()
})
</script>

<style scoped>
.message-page { max-width: 960px; }

.message-card { margin-bottom: 20px; }

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-stats {
  display: flex;
  align-items: center;
  gap: 12px;
}

.unread-badge {
  font-size: 13px;
  color: var(--danger);
  font-weight: 500;
}

.loading-wrap, .empty-wrap { padding: 60px 0; }

.message-list {
  display: flex;
  flex-direction: column;
}

.message-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid var(--border-color);
  cursor: pointer;
  transition: var(--transition-fast);
}

.message-item:last-child {
  border-bottom: none;
}

.message-item:hover {
  background: var(--primary-light);
  margin: 0 -16px;
  padding: 16px;
  border-radius: 8px;
}

.message-item.unread {
  background: #f0f7ff;
  margin: 0 -16px;
  padding: 16px;
  border-radius: 8px;
}

.message-indicator {
  width: 12px;
  flex-shrink: 0;
  padding-top: 4px;
}

.unread-dot {
  display: block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--primary);
}

.message-body { flex: 1; min-width: 0; }

.message-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.message-time {
  font-size: 12px;
  color: var(--text-placeholder);
  margin-left: auto;
}

.message-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.message-item.unread .message-title {
  font-weight: 700;
}

.message-content {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
}

/* 详情 */
.detail-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-color);
}

.detail-time {
  font-size: 13px;
  color: var(--text-placeholder);
  margin-left: auto;
}

.detail-content {
  font-size: 15px;
  color: var(--text-regular);
  line-height: 1.8;
  white-space: pre-wrap;
}

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}
</style>
