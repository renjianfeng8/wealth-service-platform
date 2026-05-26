<template>
  <div class="page">
    <div class="page-header"><h3>交易委托管理</h3></div>
    <el-card shadow="never" class="search-card">
      <el-form :model="query" inline>
        <el-form-item label="订单号"><el-input v-model="query.orderNo" placeholder="搜索" clearable /></el-form-item>
        <el-form-item label="产品编码"><el-input v-model="query.productCode" placeholder="搜索" clearable /></el-form-item>
        <el-form-item label="状态"><el-select v-model="query.orderStatus" clearable style="width:110px"><el-option v-for="d in ORDER_STATUS_OPTIONS" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="handleSearch">查询</el-button><el-button @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card shadow="never" style="margin-top:16px;">
      <div style="margin-bottom:16px;"><el-button type="primary" @click="handleAdd">新增委托</el-button></div>
      <el-table :data="tableData" stripe v-loading="loading" border empty-text="暂无数据">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column prop="userId" label="用户ID" min-width="80" />
        <el-table-column prop="productCode" label="产品编码" min-width="120" />
        <el-table-column prop="tradeType" label="类型" min-width="80">
          <template #default="{ row }"><el-tag :type="row.tradeType===1?'danger':'success'">{{ tradeTypeText(row.tradeType) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="entrustPrice" label="委托价" width="100" :formatter="(_r:any,_c:any,v:any)=>formatPrice(v)" />
        <el-table-column prop="entrustNum" label="数量" width="70" />
        <el-table-column prop="orderStatus" label="状态" width="80">
          <template #default="{ row }"><el-tag :type="orderStatusTag(row.orderStatus)">{{ orderStatusText(row.orderStatus) }}</el-tag></template>
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
    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑委托':'新增委托'" width="550px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="用户ID" prop="userId"><el-input-number v-model="form.userId" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="产品编码" prop="productCode"><el-input v-model="form.productCode" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="交易类型" prop="tradeType"><el-select v-model="form.tradeType" style="width:100%"><el-option label="买入" :value="1" /><el-option label="卖出" :value="2" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="委托价" prop="entrustPrice"><el-input-number v-model="form.entrustPrice" :precision="2" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="委托数量" prop="entrustNum"><el-input-number v-model="form.entrustNum" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" :loading="saving" @click="handleSave">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getTradeOrderPage, createTradeOrder, updateTradeOrder, deleteTradeOrder } from '@/api/trade'
import { formatDateTime, formatPrice, tradeTypeText, orderStatusTag, orderStatusText } from '@/utils/format'
import { ORDER_STATUS_OPTIONS } from '@/types'

const loading = ref(false); const saving = ref(false)
const tableData = ref<any[]>([]); const total = ref(0)
const dialogVisible = ref(false); const isEdit = ref(false)
const formRef = ref<FormInstance>()
const query = reactive({ pageNum: 1, pageSize: 10, orderNo: '', productCode: '', orderStatus: '' })
const form = reactive({ id: undefined, userId: undefined, productCode: '', tradeType: 1, entrustPrice: 0, entrustNum: 0 })
const rules: FormRules = { userId: [{ required: true, message: '必填' }], productCode: [{ required: true, message: '必填' }], entrustPrice: [{ required: true, message: '必填' }], entrustNum: [{ required: true, message: '必填' }] }

async function fetchData() {
  loading.value = true
  try {
    const params: any = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.orderNo) params.orderNo = query.orderNo
    if (query.productCode) params.productCode = query.productCode
    if (query.orderStatus !== '') params.orderStatus = query.orderStatus
    const res = await getTradeOrderPage(params)
    tableData.value = res.data.records || []; total.value = res.data.total || 0
  } finally { loading.value = false }
}
function handleSearch() { query.pageNum = 1; fetchData() }
function handleReset() { query.orderNo = ''; query.productCode = ''; query.orderStatus = ''; handleSearch() }
function handleSizeChange() { query.pageNum = 1; fetchData() }
function handleAdd() { isEdit.value = false; Object.assign(form, { id: undefined, userId: undefined, productCode: '', tradeType: 1, entrustPrice: 0, entrustNum: 0 }); dialogVisible.value = true }
function handleEdit(row: any) { isEdit.value = true; Object.assign(form, row); dialogVisible.value = true }
async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return; saving.value = true
  try {
    isEdit.value ? await updateTradeOrder(form.id!, form) : await createTradeOrder({ ...form, idempotentKey: crypto.randomUUID() })
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功'); dialogVisible.value = false; fetchData()
  } finally { saving.value = false }
}
async function handleDelete(id: number) { try { await deleteTradeOrder(id); ElMessage.success('删除成功'); fetchData() } catch { /* handled by interceptor */ } }
onMounted(fetchData)
</script>
<style scoped>
/* Global styles handle pagination-wrap and page-header */
</style>
