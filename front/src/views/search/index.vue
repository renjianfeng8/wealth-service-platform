<template>
  <div class="page">
    <div class="page-header"><h3>ES 产品搜索</h3></div>
    <el-card shadow="never">
      <el-form :model="query" inline @keyup.enter="handleSearch">
        <el-form-item label="关键词" style="width:400px">
          <el-input v-model="query.keyword" placeholder="输入产品名称、编码等关键词搜索" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <el-card shadow="never" style="margin-top:16px;">
      <div class="search-result-info" v-if="total > 0">
        共找到 {{ total }} 条结果
      </div>
      <div class="search-result-info" v-else-if="searched">
        未找到相关结果
      </div>
      <el-table :data="tableData" stripe v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="200" />
        <el-table-column prop="productName" label="产品名称" min-width="160" />
        <el-table-column prop="productCode" label="编码" width="120" />
        <el-table-column prop="productType" label="类型" width="80">
          <template #default="{ row }"><el-tag>{{ row.productType===1?'股票':row.productType===2?'基金':'其他' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">{{ formatPrice(row.price) }}</template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-popconfirm title="确定从ES删除？" @confirm="handleDelete(row.id)"><template #reference><el-button type="danger" link>删除</el-button></template></el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total" :page-sizes="[10,20,50]" layout="total, sizes, prev, pager, next" @size-change="handleSearch" @current-change="handleSearch" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { searchProduct, deleteProductDocument } from '@/api/search'
import { formatPrice } from '@/utils/format'

const loading = ref(false)
const searched = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const query = reactive({ keyword: '', page: 1, size: 10 })

async function handleSearch() {
  if (!query.keyword.trim()) { ElMessage.warning('请输入关键词'); return }
  loading.value = true; searched.value = true
  try {
    const res = await searchProduct({ keyword: query.keyword, page: query.page, size: query.size })
    tableData.value = res.data.records || res.data.content || []
    total.value = res.data.total || res.data.totalElements || 0
  } finally { loading.value = false }
}

async function handleDelete(id: string) {
  try {
    await deleteProductDocument(id)
    ElMessage.success('删除成功')
    handleSearch()
  } catch { /* handled by interceptor */ }
}
</script>
<style scoped>
.page-header h3 { margin-bottom: 16px; }

.search-result-info {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 16px;
  padding: 8px 12px;
  background: var(--bg-table-stripe);
  border-radius: var(--radius-sm);
  border-left: 3px solid var(--primary);
}
</style>
