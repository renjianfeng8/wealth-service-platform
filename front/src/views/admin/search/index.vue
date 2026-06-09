<template>
  <AdminPageShell title="产品搜索" description="按产品名称或编码快速检索产品数据。">
    <AdminFilterBar :model="query" :fields="filterFields" @search="handleSearch" @reset="handleReset" />

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
      <el-table-column prop="productName" label="产品名称" min-width="160" show-overflow-tooltip />
      <el-table-column prop="productCode" label="产品编码" min-width="130" show-overflow-tooltip />
      <el-table-column prop="productType" label="类型" width="110">
        <template #default="{ row }">
          <el-tag>{{ productTypeText(row.productType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="price" label="价格" width="110">
        <template #default="{ row }">{{ formatPrice(row.price) }}</template>
      </el-table-column>
    </AdminDataTable>
  </AdminPageShell>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import AdminPageShell from '@/components/admin/AdminPageShell.vue'
import AdminFilterBar from '@/components/admin/AdminFilterBar.vue'
import AdminDataTable from '@/components/admin/AdminDataTable.vue'
import { getProductPage } from '@/api/product'
import { formatPrice, productTypeText } from '@/utils/format'
import type { WeaProduct } from '@/types'
import type { AdminFilterField } from '@/components/admin/AdminFilterBar.vue'

type SearchQuery = {
  pageNum: number
  pageSize: number
  keyword: string
}

const filterFields: AdminFilterField[] = [
  { prop: 'keyword', label: '关键词', placeholder: '输入产品名称或编码搜索', width: '320px' },
]

const loading = ref(false)
const searched = ref(false)
const tableData = ref<WeaProduct[]>([])
const total = ref(0)
const query = reactive<SearchQuery>({ keyword: '', pageNum: 1, pageSize: 10 })

async function handleSearch() {
  if (!query.keyword.trim()) {
    ElMessage.warning('请输入关键词')
    return
  }
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
</script>

<style scoped>
.search-result-info {
  color: var(--fl-text-secondary);
  font-size: 14px;
}
</style>
