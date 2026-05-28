<template>
  <div class="page">
    <div class="page-header"><h3>资讯管理</h3></div>
    <el-card shadow="never" class="search-card">
      <el-form :model="query" inline>
        <el-form-item label="标题"><el-input v-model="query.title" placeholder="搜索" clearable /></el-form-item>
        <el-form-item label="来源"><el-input v-model="query.source" placeholder="搜索" clearable /></el-form-item>
        <el-form-item><el-button type="primary" @click="handleSearch">查询</el-button><el-button @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card shadow="never" style="margin-top:16px;">
      <div style="margin-bottom:16px;"><el-button type="primary" @click="handleAdd">新增资讯</el-button></div>
      <el-table :data="tableData" stripe v-loading="loading" border empty-text="暂无数据">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
        <el-table-column prop="newsType" label="类型" width="80">
          <template #default="{ row }"><el-tag>{{ row.newsType===1?'财经':row.newsType===2?'公告':'其他' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="source" label="来源" min-width="120" />
        <el-table-column prop="status" label="状态" width="70">
          <template #default="{ row }"><el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="160" :formatter="(_r:any,_c:any,v:any)=>formatDateTime(v)" />
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
    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑资讯':'新增资讯'" width="600px" :before-close="handleDialogClose">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="20">
          <el-col :span="16"><el-form-item label="标题" prop="title"><el-input v-model="form.title" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="类型"><el-select v-model="form.newsType" style="width:100%"><el-option label="财经" :value="1" /><el-option label="公告" :value="2" /><el-option label="其他" :value="3" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="来源"><el-input v-model="form.source" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态"><el-radio-group v-model="form.status"><el-radio :value="1">发布</el-radio><el-radio :value="0">草稿</el-radio></el-radio-group></el-form-item></el-col>
        </el-row>
        <el-form-item label="内容" prop="content"><el-input v-model="form.content" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="发布时间"><el-date-picker v-model="form.publishTime" type="datetime" style="width:100%" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" :loading="saving" @click="handleSave">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useFormGuard } from '@/composables/useFormGuard'
import { getNewsPage, createNews, updateNews, deleteNews } from '@/api/message'
import { formatDateTime, statusTag, statusText } from '@/utils/format'

const loading = ref(false); const saving = ref(false)
const tableData = ref<any[]>([]); const total = ref(0)
const dialogVisible = ref(false); const isEdit = ref(false)
const formRef = ref<FormInstance>()
const query = reactive({ pageNum: 1, pageSize: 10, title: '', source: '' })
const form = reactive({ id: undefined, title: '', content: '', newsType: 1, source: '', status: 1, publishTime: '' })
const { isDirty, reset } = useFormGuard(form)
const rules: FormRules = { title: [{ required: true, message: '必填' }] }

async function fetchData() {
  loading.value = true
  try {
    const params: any = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.title) params.title = query.title; if (query.source) params.source = query.source
    const res = await getNewsPage(params)
    tableData.value = res.data.records || []; total.value = res.data.total || 0
  } finally { loading.value = false }
}
function handleSearch() { query.pageNum = 1; fetchData() }
function handleReset() { query.title = ''; query.source = ''; handleSearch() }
function handleSizeChange() { query.pageNum = 1; fetchData() }
function handleAdd() { isEdit.value = false; Object.assign(form, { id: undefined, title: '', content: '', newsType: 1, source: '', status: 1, publishTime: '' }); reset(); dialogVisible.value = true }
function handleEdit(row: any) { isEdit.value = true; Object.assign(form, row); reset(); dialogVisible.value = true }
async function handleDialogClose(done: () => void) {
  if (!isDirty()) return done()
  try {
    await ElMessageBox.confirm('有未保存的更改，确定关闭吗？', '离开确认', { type: 'warning' })
    done()
  } catch { /* 取消关闭 */ }
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return; saving.value = true
  try {
    isEdit.value ? await updateNews(form.id!, form) : await createNews(form)
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功'); reset(); dialogVisible.value = false; fetchData()
  } finally { saving.value = false }
}
async function handleDelete(id: number) { try { await deleteNews(id); ElMessage.success('删除成功'); fetchData() } catch { /* handled by interceptor */ } }
onMounted(fetchData)
</script>
<style scoped>
/* Global styles handle pagination-wrap and page-header */
</style>
