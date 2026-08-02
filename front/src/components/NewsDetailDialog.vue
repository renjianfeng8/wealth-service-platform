<template>
  <el-dialog
    :model-value="modelValue"
    :title="item?.title || '资讯详情'"
    width="700"
    destroy-on-close
    @update:model-value="emit('update:modelValue', $event)"
  >
    <div v-loading="loading" class="nd-body">
      <template v-if="item">
        <div class="nd-meta">
          <el-tag size="small" effect="plain">{{ newsTypeText(item.newsType) }}</el-tag>
          <span v-if="item.source" class="nd-source">来源：{{ item.source }}</span>
          <span class="nd-time">{{ formatDateTime(item.publishTime) }}</span>
        </div>
        <div class="nd-content">{{ item.content }}</div>
      </template>
      <el-empty v-else-if="!loading" description="资讯不存在" :image-size="64" />
    </div>
    <template #footer>
      <el-button @click="emit('update:modelValue', false)">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { getNewsById } from '@/api/message'
import { formatDateTime, newsTypeText } from '@/utils/format'
import type { WeaNews } from '@/types'

const props = defineProps<{
  modelValue: boolean
  newsId: number | null
}>()
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const item = ref<WeaNews | null>(null)
const loading = ref(false)

async function load() {
  if (!props.newsId) return
  item.value = null
  loading.value = true
  try {
    const res = await getNewsById(props.newsId)
    item.value = (res.data || null) as WeaNews | null
  } catch {
    item.value = null
  } finally {
    loading.value = false
  }
}

watch(
  () => [props.modelValue, props.newsId] as const,
  ([visible, id]) => {
    if (visible && id) load()
  },
  { immediate: true },
)
</script>

<style scoped>
.nd-body { min-height: 80px; }
.nd-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-color);
}
.nd-source { font-size: 14px; color: var(--text-secondary); }
.nd-time { font-size: 13px; color: var(--text-placeholder); margin-left: auto; }
.nd-content {
  font-size: 15px;
  color: var(--text-regular);
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
