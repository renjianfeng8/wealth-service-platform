<template>
  <div class="page">
    <div class="page-header"><h3>产品搜索</h3></div>
    <el-card shadow="never">
      <el-form :model="query" inline @submit.prevent="handleSearch">
        <el-form-item label="关键词" style="width:400px">
          <el-input v-model="query.keyword" placeholder="输入产品名称/编码搜索" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :loading="loading">搜索</el-button>
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
      <el-table :data="tableData" stripe v-loading="loading" border empty-text="请输入关键词搜索">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="productName" label="产品名称" min-width="160" />
        <el-table-column prop="productCode" label="编码" min-width="120" />
        <el-table-column prop="productType" label="类型" width="80">
          <template #default="{ row }"><el-tag>{{ row.productType===1?'股票':row.productType===2?'基金':'其他' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">{{ formatPrice(row.price) }}</template>
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
import { getProductPage } from '@/api/product'
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
    const res = await getProductPage({
      pageNum: query.page,
      pageSize: query.size,
      productName: query.keyword,
      productCode: query.keyword,
    })
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    tableData.value = []
    total.value = 0
  } finally { loading.value = false }
}
</script>
<style scoped>
.page-header h3 { margin-bottom: 16px; }

.search-result-info {
  font-size: 14px;
  color: var(--fl-text-secondary);
  margin-bottom: 16px;
  padding: 8px 12px;
  background: var(--fl-content-bg);
  border-radius: var(--fl-radius-sm);
  border-left: 3px solid var(--fl-primary);
}
</style>
