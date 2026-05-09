<template>
  <div class="page">
    <div class="page-header"><h3>行情数据</h3></div>
    <el-card shadow="never" class="search-card">
      <el-form :model="query" inline>
        <el-form-item label="产品编码"><el-input v-model="query.productCode" placeholder="搜索" clearable /></el-form-item>
        <el-form-item><el-button type="primary" @click="handleSearch">查询</el-button><el-button @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card shadow="never" style="margin-top:16px;">
      <div style="margin-bottom:16px;"><el-button type="primary" @click="handleAdd">新增行情</el-button></div>
      <el-table :data="tableData" stripe v-loading="loading" border empty-text="暂无数据">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="productCode" label="产品编码" width="110" />
        <el-table-column prop="currentPrice" label="当前价" width="100" :formatter="(_r:any,_c:any,v:any)=>formatPrice(v)" />
        <el-table-column prop="openPrice" label="开盘价" width="100" :formatter="(_r:any,_c:any,v:any)=>formatPrice(v)" />
        <el-table-column prop="closePrice" label="收盘价" width="100" :formatter="(_r:any,_c:any,v:any)=>formatPrice(v)" />
        <el-table-column prop="highestPrice" label="最高价" width="100" :formatter="(_r:any,_c:any,v:any)=>formatPrice(v)" />
        <el-table-column prop="lowestPrice" label="最低价" width="100" :formatter="(_r:any,_c:any,v:any)=>formatPrice(v)" />
        <el-table-column prop="riseFallRate" label="涨跌幅" width="100">
          <template #default="{ row }"><span :style="{color: (row.riseFallRate||0)>=0?'#f56c6c':'#67c23a'}">{{ formatRate(row.riseFallRate) }}</span></template>
        </el-table-column>
        <el-table-column prop="marketTime" label="行情时间" width="160" :formatter="(_r:any,_c:any,v:any)=>formatDateTime(v)" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)"><template #reference><el-button type="danger" link>删除</el-button></template></el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" :page-sizes="[10,20,50]" layout="total, sizes, prev, pager, next" @size-change="handleSizeChange" @current-change="fetchData" />
      </div>
    </el-card>
    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑行情':'新增行情'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="产品编码" prop="productCode"><el-input v-model="form.productCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="当前价"><el-input-number v-model="form.currentPrice" :precision="2" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="开盘价"><el-input-number v-model="form.openPrice" :precision="2" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="收盘价"><el-input-number v-model="form.closePrice" :precision="2" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="最高价"><el-input-number v-model="form.highestPrice" :precision="2" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="最低价"><el-input-number v-model="form.lowestPrice" :precision="2" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="涨跌额"><el-input-number v-model="form.riseFall" :precision="2" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="涨跌幅"><el-input-number v-model="form.riseFallRate" :precision="4" :step="0.001" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="行情时间"><el-date-picker v-model="form.marketTime" type="datetime" style="width:100%" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" :loading="saving" @click="handleSave">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getMarketDataPage, createMarketData, updateMarketData, deleteMarketData } from '@/api/product'
import { formatDateTime, formatPrice, formatRate } from '@/utils/format'

const loading = ref(false); const saving = ref(false)
const tableData = ref<any[]>([]); const total = ref(0)
const dialogVisible = ref(false); const isEdit = ref(false)
const formRef = ref<FormInstance>()
const query = reactive({ pageNum: 1, pageSize: 10, productCode: '' })
const form = reactive({ id: undefined, productCode: '', currentPrice: 0, openPrice: 0, closePrice: 0, highestPrice: 0, lowestPrice: 0, riseFall: 0, riseFallRate: 0, marketTime: '' })
const rules: FormRules = { productCode: [{ required: true, message: '必填' }] }

async function fetchData() {
  loading.value = true
  try {
    const params: any = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.productCode) params.productCode = query.productCode
    const res = await getMarketDataPage(params)
    tableData.value = res.data.records || []; total.value = res.data.total || 0
  } finally { loading.value = false }
}
function handleSearch() { query.pageNum = 1; fetchData() }
function handleReset() { query.productCode = ''; handleSearch() }
function handleSizeChange() { query.pageNum = 1; fetchData() }
function handleAdd() { isEdit.value = false; Object.assign(form, { id: undefined, productCode: '', currentPrice: 0, openPrice: 0, closePrice: 0, highestPrice: 0, lowestPrice: 0, riseFall: 0, riseFallRate: 0, marketTime: '' }); dialogVisible.value = true }
function handleEdit(row: any) { isEdit.value = true; Object.assign(form, row); dialogVisible.value = true }
async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return; saving.value = true
  try {
    isEdit.value ? await updateMarketData(form.id!, form) : await createMarketData(form)
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功'); dialogVisible.value = false; fetchData()
  } finally { saving.value = false }
}
async function handleDelete(id: number) { try { await deleteMarketData(id); ElMessage.success('删除成功'); fetchData() } catch { /* handled by interceptor */ } }
onMounted(fetchData)
</script>
<style scoped>
/* Global styles handle pagination-wrap and page-header */
</style>
