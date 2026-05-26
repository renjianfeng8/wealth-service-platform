<template>
  <div class="page">
    <div class="page-header"><h3>站内消息</h3></div>
    <el-card shadow="never" class="search-card">
      <el-form :model="query" inline>
        <el-form-item label="标题"><el-input v-model="query.msgTitle" placeholder="搜索" clearable /></el-form-item>
        <el-form-item label="类型"><el-select v-model="query.msgType" clearable style="width:120px"><el-option v-for="d in MSG_TYPE_OPTIONS" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="handleSearch">查询</el-button><el-button @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card shadow="never" style="margin-top:16px;">
      <div style="margin-bottom:16px;"><el-button type="primary" @click="handleAdd">发送消息</el-button></div>
      <el-table :data="tableData" stripe v-loading="loading" border empty-text="暂无数据">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="msgTitle" label="标题" min-width="140" show-overflow-tooltip />
        <el-table-column prop="msgType" label="类型" width="100">
          <template #default="{ row }"><el-tag>{{ row.msgType===1?'系统通知':row.msgType===2?'交易提醒':row.msgType===3?'风控通知':'活动通知' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="userId" label="用户ID" width="70" />
        <el-table-column prop="readFlag" label="状态" width="70">
          <template #default="{ row }"><el-tag :type="row.readFlag===1?'success':'info'">{{ row.readFlag===1?'已读':'未读' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="createTime" label="发送时间" width="160" :formatter="(_r:any,_c:any,v:any)=>formatDateTime(v)" />
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
    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑消息':'发送消息'" width="550px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="用户ID"><el-input-number v-model="form.userId" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="类型" prop="msgType"><el-select v-model="form.msgType" style="width:100%"><el-option v-for="d in MSG_TYPE_OPTIONS" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
        </el-row>
        <el-form-item label="标题" prop="msgTitle"><el-input v-model="form.msgTitle" /></el-form-item>
        <el-form-item label="内容" prop="msgContent"><el-input v-model="form.msgContent" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" :loading="saving" @click="handleSave">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getMessagePage, createMessage, updateMessage, deleteMessage } from '@/api/message'
import { formatDateTime } from '@/utils/format'
import { MSG_TYPE_OPTIONS } from '@/types'

const loading = ref(false); const saving = ref(false)
const tableData = ref<any[]>([]); const total = ref(0)
const dialogVisible = ref(false); const isEdit = ref(false)
const formRef = ref<FormInstance>()
const query = reactive({ pageNum: 1, pageSize: 10, msgTitle: '', msgType: '' })
const form = reactive({ id: undefined, userId: undefined, msgType: 1, msgTitle: '', msgContent: '' })
const rules: FormRules = { msgTitle: [{ required: true, message: '必填' }], msgContent: [{ required: true, message: '必填' }] }

async function fetchData() {
  loading.value = true
  try {
    const params: any = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.msgTitle) params.msgTitle = query.msgTitle
    if (query.msgType !== '') params.msgType = query.msgType
    const res = await getMessagePage(params)
    tableData.value = res.data.records || []; total.value = res.data.total || 0
  } finally { loading.value = false }
}
function handleSearch() { query.pageNum = 1; fetchData() }
function handleReset() { query.msgTitle = ''; query.msgType = ''; handleSearch() }
function handleSizeChange() { query.pageNum = 1; fetchData() }
function handleAdd() { isEdit.value = false; Object.assign(form, { id: undefined, userId: undefined, msgType: 1, msgTitle: '', msgContent: '' }); dialogVisible.value = true }
function handleEdit(row: any) { isEdit.value = true; Object.assign(form, row); dialogVisible.value = true }
async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return; saving.value = true
  try {
    isEdit.value ? await updateMessage(form.id!, form) : await createMessage(form)
    ElMessage.success(isEdit.value ? '更新成功' : '发送成功'); dialogVisible.value = false; fetchData()
  } finally { saving.value = false }
}
async function handleDelete(id: number) { try { await deleteMessage(id); ElMessage.success('删除成功'); fetchData() } catch { /* handled by interceptor */ } }
onMounted(fetchData)
</script>
<style scoped>
/* Global styles handle pagination-wrap and page-header */
</style>
