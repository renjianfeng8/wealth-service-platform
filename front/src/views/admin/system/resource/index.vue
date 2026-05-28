<template>
  <div class="page">
    <div class="page-header"><h3>资源管理</h3></div>
    <el-card shadow="never" class="search-card">
      <el-form :model="query" inline @submit.prevent="handleSearch">
        <el-form-item label="资源名称"><el-input v-model="query.name" placeholder="搜索" clearable /></el-form-item>
        <el-form-item label="URL"><el-input v-model="query.url" placeholder="搜索" clearable /></el-form-item>
        <el-form-item><el-button type="primary" @click="handleSearch">查询</el-button><el-button @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card shadow="never" style="margin-top:16px;">
      <div style="margin-bottom:16px;"><el-button type="primary" @click="handleAdd">新增资源</el-button></div>
      <el-table :data="tableData" stripe v-loading="loading" border empty-text="暂无数据">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="资源名称" min-width="120" />
        <el-table-column prop="url" label="URL" min-width="280" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" min-width="160" show-overflow-tooltip />
        <el-table-column prop="categoryId" label="分类ID" width="70" />
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
    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑资源':'新增资源'" width="500px" :before-close="handleDialogClose">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="资源名" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="URL" prop="url"><el-input v-model="form.url" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="分类ID"><el-input-number v-model="form.categoryId" style="width:100%" /></el-form-item>
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
import { getResourcePage, createResource, updateResource, deleteResource } from '@/api/system'
import { formatDateTime } from '@/utils/format'

const loading = ref(false); const saving = ref(false)
const tableData = ref<any[]>([]); const total = ref(0)
const dialogVisible = ref(false); const isEdit = ref(false)
const formRef = ref<FormInstance>()
const query = reactive({ pageNum: 1, pageSize: 10, name: '', url: '' })
const form = reactive({ id: undefined, name: '', url: '', description: '', categoryId: undefined })
const { isDirty, reset } = useFormGuard(form)
const rules: FormRules = { name: [{ required: true, message: '必填' }], url: [{ required: true, message: '必填' }] }

async function fetchData() {
  loading.value = true
  try {
    const params: any = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.name) params.name = query.name; if (query.url) params.url = query.url
    const res = await getResourcePage(params)
    tableData.value = res.data.records || []; total.value = res.data.total || 0
  } finally { loading.value = false }
}
function handleSearch() { query.pageNum = 1; fetchData() }
function handleReset() { query.name = ''; query.url = ''; handleSearch() }
function handleSizeChange() { query.pageNum = 1; fetchData() }
function handleAdd() { isEdit.value = false; Object.assign(form, { id: undefined, name: '', url: '', description: '', categoryId: undefined }); reset(); dialogVisible.value = true }
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
    isEdit.value ? await updateResource(form.id!, form) : await createResource(form)
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功'); reset(); dialogVisible.value = false; fetchData()
  } finally { saving.value = false }
}
async function handleDelete(id: number) { try { await deleteResource(id); ElMessage.success('删除成功'); fetchData() } catch { /* handled by interceptor */ } }
onMounted(fetchData)
</script>
<style scoped>
.page-header h3 { margin-bottom: 16px; }
</style>
