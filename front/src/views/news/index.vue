<template>
  <div class="news-page">
    <div class="page-title">财经资讯</div>

    <!-- 分类 -->
    <el-card class="filter-card" shadow="never">
      <el-radio-group v-model="filterType" @change="handleFilter">
        <el-radio-button :value="0">全部</el-radio-button>
        <el-radio-button v-for="opt in NEWS_TYPE_OPTIONS" :key="opt.value" :value="opt.value">
          {{ opt.label }}
        </el-radio-button>
      </el-radio-group>
    </el-card>

    <!-- 列表 -->
    <div v-if="loading" class="loading-wrap">
      <el-skeleton :rows="5" animated />
    </div>
    <div v-else-if="hasError" class="empty-wrap">
      <el-result icon="error" title="加载失败" sub-title="数据获取异常，请重试">
        <template #extra>
          <el-button type="primary" @click="fetchNews">重试</el-button>
        </template>
      </el-result>
    </div>
    <div v-else-if="newsList.length === 0" class="empty-wrap">
      <el-empty description="暂无资讯" />
    </div>
    <div v-else class="news-list">
      <div
        v-for="item in newsList"
        :key="item.id"
        class="news-item"
        @click="showDetail(item)"
      >
        <el-card class="news-card" shadow="never">
          <div class="news-content">
            <div class="news-header">
              <el-tag size="small" effect="plain">
                {{ newsTypeText(item.newsType) }}
              </el-tag>
              <el-tag v-if="item.source" size="small" type="info" effect="plain">
                {{ item.source }}
              </el-tag>
            </div>
            <h3 class="news-title">{{ item.title }}</h3>
            <p class="news-summary">{{ truncate(item.content, 150) }}</p>
            <div class="news-footer">
              <span class="news-time">{{ formatDateTime(item.publishTime) }}</span>
              <span class="news-read-more">阅读全文 →</span>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrap">
      <el-pagination
        v-if="total > 0"
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="fetchNews"
        @size-change="handlePageSizeChange"
      />
    </div>

    <!-- 详情弹窗（实时拉取） -->
    <NewsDetailDialog
      v-model="detailVisible"
      :news-id="selectedNewsId"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getNewsPage } from '@/api/message'
import { NEWS_TYPE_OPTIONS } from '@/types'
import { formatDateTime, newsTypeText } from '@/utils/format'
import NewsDetailDialog from '@/components/NewsDetailDialog.vue'
import type { WeaNews } from '@/types'

const newsList = ref<WeaNews[]>([])
const loading = ref(false)
const hasError = ref(false)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const filterType = ref(0)
const detailVisible = ref(false)
const selectedNewsId = ref<number | null>(null)

function truncate(text: string | undefined, len: number): string {
  if (!text) return ''
  return text.length > len ? text.substring(0, len) + '...' : text
}

async function fetchNews() {
  hasError.value = false
  loading.value = true
  try {
    const params: { pageNum: number; pageSize: number; newsType?: number } = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (filterType.value) params.newsType = filterType.value
    const res = await getNewsPage(params)
    newsList.value = (res.data?.records || []) as WeaNews[]
    total.value = res.data?.total || 0
  } catch {
    hasError.value = true
    newsList.value = []
  } finally {
    loading.value = false
  }
}

function handleFilter() {
  pageNum.value = 1
  fetchNews()
}

function handlePageSizeChange() {
  pageNum.value = 1
  fetchNews()
}

function showDetail(item: WeaNews) {
  if (!item.id) return
  selectedNewsId.value = item.id
  detailVisible.value = true
}

onMounted(fetchNews)
</script>

<style scoped>
.news-page { max-width: 1200px; }
.filter-card { margin-bottom: 20px; }
.loading-wrap, .empty-wrap { padding: 60px 0; }

.news-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
}

.news-card {
  cursor: pointer;
  transition: var(--transition);
}

.news-card:hover {
  transform: translateX(4px);
  box-shadow: var(--shadow-md) !important;
}

.news-content {
  padding: 4px 0;
}

.news-header {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.news-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
  line-height: 1.4;
}

.news-summary {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 12px;
}

.news-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.news-time {
  font-size: 13px;
  color: var(--text-placeholder);
}

.news-read-more {
  font-size: 13px;
  color: var(--primary);
  font-weight: 500;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}
</style>
