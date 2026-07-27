<template>
  <AdminPageShell title="产品搜索" description="按产品名称或编码快速检索产品数据。">
    <div class="search-header" v-if="routeKeyword">
      <div class="search-header-info">
        搜索"<strong>{{ routeKeyword }}</strong>"，共找到 <strong>{{ total }}</strong> 条结果
      </div>
      <el-input
        v-model="query.keyword"
        placeholder="输入关键词搜索..."
        :prefix-icon="Search"
        size="default"
        clearable
        class="search-inline-input"
        @keyup.enter="handleSearch"
        @clear="handleReset"
      />
    </div>

    <AdminDataTable
      :data="tableData"
      :loading="loading"
      :total="total"
      :pagination="query"
      empty-text="请输入关键词搜索"
      @page-change="handleSearch"
    >
      <template #toolbar>
        <div class="search-result-info" v-if="total > 0">共找到 {{ total }} 条结果</div>
        <div class="search-result-info" v-else-if="searched">未找到相关结果</div>
      </template>

      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="产品名称" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">
          <router-link to="/admin/product" class="result-link">{{ row.productName }}</router-link>
        </template>
      </el-table-column>
      <el-table-column prop="productCode" label="产品编码" min-width="130" show-overflow-tooltip />
      <el-table-column prop="productType" label="类型" width="110">
        <template #default="{ row }">
          <el-tag>{{ productTypeText(row.productType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="price" label="价格" width="110">
        <template #default="{ row }">{{ formatPrice(row.price) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '在售' : '已下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <router-link to="/admin/product" class="result-link">前往管理</router-link>
        </template>
      </el-table-column>
    </AdminDataTable>
  </AdminPageShell>
</template>

<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import AdminPageShell from '@/components/admin/AdminPageShell.vue'
import AdminDataTable from '@/components/admin/AdminDataTable.vue'
import { getProductPage } from '@/api/product'
import { formatPrice, productTypeText } from '@/utils/format'
import type { WeaProduct } from '@/types'

type SearchQuery = {
  pageNum: number
  pageSize: number
  keyword: string
}

const loading = ref(false)
const searched = ref(false)
const tableData = ref<WeaProduct[]>([])
const total = ref(0)
const query = reactive<SearchQuery>({ keyword: '', pageNum: 1, pageSize: 10 })

const route = useRoute()
const routeKeyword = computed(() => (route.query.keyword as string) || '')

async function handleSearch() {
  if (!query.keyword.trim()) {
    ElMessage.warning('请输入关键词')
    return
  }
  query.pageNum = 1
  loading.value = true
  searched.value = true
  try {
    const res = await getProductPage({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      productName: query.keyword,
      productCode: query.keyword,
    })
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleReset() {
  query.keyword = ''
  query.pageNum = 1
  tableData.value = []
  total.value = 0
  searched.value = false
}

onMounted(() => {
  const kw = route.query.keyword
  if (kw && typeof kw === 'string' && kw.trim()) {
    query.keyword = kw.trim()
    handleSearch()
  }
})
</script>

<style scoped>
.search-result-info {
  color: var(--fl-text-secondary);
  font-size: 14px;
}

.search-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: var(--el-fill-color-lighter);
  border-radius: 8px;
}

.search-header-info {
  font-size: 14px;
  color: var(--fl-text-secondary);
}

.search-header-info strong {
  color: var(--fl-text-primary);
}

.search-inline-input {
  width: 280px;
}

.result-link {
  color: var(--el-color-primary);
  text-decoration: none;
  font-weight: 500;
}

.result-link:hover {
  text-decoration: underline;
}
</style>
