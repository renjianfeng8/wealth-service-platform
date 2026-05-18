<template>
  <div class="page">
    <div class="page-header"><h3>产品管理</h3></div>
    <el-card shadow="never" class="search-card">
      <el-form :model="query" inline>
        <el-form-item label="产品名称"><el-input v-model="query.productName" placeholder="搜索" clearable /></el-form-item>
        <el-form-item label="产品编码"><el-input v-model="query.productCode" placeholder="搜索" clearable /></el-form-item>
        <el-form-item label="类型"><el-select v-model="query.productType" clearable style="width:120px"><el-option v-for="d in PRODUCT_TYPE_OPTIONS" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="handleSearch">查询</el-button><el-button @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card shadow="never" style="margin-top:16px;">
      <div style="margin-bottom:16px;"><el-button type="primary" @click="handleAdd">新增产品</el-button></div>
      <el-table :data="tableData" stripe v-loading="loading" border empty-text="暂无数据">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="productName" label="产品名称" min-width="140" />
        <el-table-column prop="productCode" label="编码" width="120" />
        <el-table-column prop="productType" label="类型" width="80">
          <template #default="{ row }"><el-tag>{{ productTypeText(row.productType) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="100" :formatter="(_r:any,_c:any,v:any)=>formatPrice(v)" />
        <el-table-column prop="riseFallRate" label="涨跌幅" width="100">
          <template #default="{ row }"><span :style="{color: (row.riseFallRate||0)>=0?'#f56c6c':'#67c23a'}">{{ formatRate(row.riseFallRate) }}</span></template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="70">
          <template #default="{ row }"><el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" :formatter="(_r:any,_c:any,v:any)=>formatDateTime(v)" />
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
    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑产品':'新增产品'" width="550px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="产品名称" prop="productName"><el-input v-model="form.productName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="产品编码" prop="productCode"><el-input v-model="form.productCode" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="产品类型" prop="productType"><el-select v-model="form.productType" style="width:100%"><el-option v-for="d in PRODUCT_TYPE_OPTIONS" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="价格" prop="price"><el-input-number v-model="form.price" :precision="2" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="涨跌额"><el-input-number v-model="form.riseFall" :precision="2" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="涨跌幅"><el-input-number v-model="form.riseFallRate" :precision="4" :step="0.001" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="排序"><el-input-number v-model="form.sort" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态"><el-radio-group v-model="form.status"><el-radio :value="1">正常</el-radio><el-radio :value="0">禁用</el-radio></el-radio-group></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" :loading="saving" @click="handleSave">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getProductPage, createProduct, updateProduct, deleteProduct } from '@/api/product'
import { formatDateTime, formatPrice, formatRate, statusTag, statusText, productTypeText } from '@/utils/format'
import { PRODUCT_TYPE_OPTIONS } from '@/types'

const loading = ref(false); const saving = ref(false)
const tableData = ref<any[]>([]); const total = ref(0)
const dialogVisible = ref(false); const isEdit = ref(false)
const formRef = ref<FormInstance>()
const query = reactive({ pageNum: 1, pageSize: 10, productName: '', productCode: '', productType: '' })
const form = reactive({ id: undefined, productName: '', productCode: '', productType: 1, price: 0, riseFall: 0, riseFallRate: 0, status: 1, sort: 0 })
const rules: FormRules = { productName: [{ required: true, message: '必填' }], productCode: [{ required: true, message: '必填' }], price: [{ required: true, message: '请输入价格' }] }

async function fetchData() {
  loading.value = true
  try {
    const params: any = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.productName) params.productName = query.productName
    if (query.productCode) params.productCode = query.productCode
    if (query.productType !== '') params.productType = query.productType
    const res = await getProductPage(params)
    tableData.value = res.data.records || []; total.value = res.data.total || 0
  } finally { loading.value = false }
}
function handleSearch() { query.pageNum = 1; fetchData() }
function handleReset() { query.productName = ''; query.productCode = ''; query.productType = ''; handleSearch() }
function handleSizeChange() { query.pageNum = 1; fetchData() }
function handleAdd() { isEdit.value = false; Object.assign(form, { id: undefined, productName: '', productCode: '', productType: 1, price: 0, riseFall: 0, riseFallRate: 0, status: 1, sort: 0 }); dialogVisible.value = true }
function handleEdit(row: any) { isEdit.value = true; Object.assign(form, row); dialogVisible.value = true }
async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return; saving.value = true
  try {
    isEdit.value ? await updateProduct(form.id!, form) : await createProduct(form)
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功'); dialogVisible.value = false; fetchData()
  } finally { saving.value = false }
}
async function handleDelete(id: number) { try { await deleteProduct(id); ElMessage.success('删除成功'); fetchData() } catch { /* handled by interceptor */ } }
onMounted(fetchData)
</script>
<style scoped>
.page-header h3 { margin-bottom: 16px; }
</style>
