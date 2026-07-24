<template>
  <AdminPageShell title="资源管理" description="维护后台接口资源、URL、分类和说明。">
    <AdminFilterBar :model="query" :fields="filterFields" @search="handleSearch" @reset="handleReset" />

    <AdminDataTable :data="tableData" :loading="loading" :total="total" :pagination="query" @page-change="fetchData">
      <template #toolbar>
        <el-button type="primary" @click="handleAdd">新增资源</el-button>
      </template>

      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="资源名称" min-width="140" show-overflow-tooltip />
      <el-table-column prop="url" label="URL" min-width="280" show-overflow-tooltip />
      <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
      <el-table-column prop="categoryId" label="分类ID" width="90" />
      <el-table-column prop="createTime" label="创建时间" width="170" :formatter="formatDateColumn" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-popconfirm title="确定删除该资源？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button type="danger" link>删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </AdminDataTable>

    <AdminFormDialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑资源' : '新增资源'"
      :model="form"
      :rules="rules"
      :saving="saving"
      :before-close="handleDialogClose"
      width="540px"
      @submit="handleSave"
    >
      <el-form-item label="资源名称" prop="name">
        <el-input v-model="form.name" />
      </el-form-item>
      <el-form-item label="URL" prop="url">
        <el-input v-model="form.url" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="form.description" type="textarea" :rows="2" />
      </el-form-item>
      <el-form-item label="分类ID">
        <el-input-number v-model="form.categoryId" style="width: 100%" />
      </el-form-item>
    </AdminFormDialog>
  </AdminPageShell>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormRules } from 'element-plus'
import AdminPageShell from '@/components/admin/AdminPageShell.vue'
import AdminFilterBar from '@/components/admin/AdminFilterBar.vue'
import AdminDataTable from '@/components/admin/AdminDataTable.vue'
import AdminFormDialog from '@/components/admin/AdminFormDialog.vue'
import { useFormGuard } from '@/composables/useFormGuard'
import { assignEditable } from '@/utils/object'
import { getResourcePage, createResource, updateResource, deleteResource } from '@/api/system'
import { formatDateTime } from '@/utils/format'
import type { UmsResource } from '@/types'
import type { AdminFilterField } from '@/components/admin/AdminFilterBar.vue'

type ResourceQuery = {
  pageNum: number
  pageSize: number
  name: string
  url: string
}

type ResourceRow = UmsResource & {
  createTime?: string
}

const filterFields: AdminFilterField[] = [
  { prop: 'name', label: '资源名称', placeholder: '搜索资源名称' },
  { prop: 'url', label: 'URL', placeholder: '搜索 URL', width: '220px' },
]

const loading = ref(false)
const saving = ref(false)
const tableData = ref<ResourceRow[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)

const query = reactive<ResourceQuery>({ pageNum: 1, pageSize: 10, name: '', url: '' })
const form = reactive<ResourceRow>({ id: undefined, name: '', url: '', description: '', categoryId: undefined })
const { isDirty, reset } = useFormGuard(form)

const rules: FormRules = {
  name: [{ required: true, message: '请输入资源名称', trigger: 'blur' }],
  url: [{ required: true, message: '请输入 URL', trigger: 'blur' }],
}

async function fetchData() {
  loading.value = true
  try {
    const params: any = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.name) params.name = query.name
    if (query.url) params.url = query.url
    const res = await getResourcePage(params)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.pageNum = 1
  fetchData()
}

function handleReset() {
  query.name = ''
  query.url = ''
  handleSearch()
}

function resetForm() {
  Object.assign(form, { id: undefined, name: '', url: '', description: '', categoryId: undefined })
}

function handleAdd() {
  isEdit.value = false
  resetForm()
  reset()
  dialogVisible.value = true
}

function handleEdit(row: ResourceRow) {
  isEdit.value = true
  assignEditable(form, row)
  reset()
  dialogVisible.value = true
}

async function handleDialogClose(done: () => void) {
  if (!isDirty()) {
    done()
    return
  }
  try {
    await ElMessageBox.confirm('有未保存的修改，确定关闭吗？', '离开确认', { type: 'warning' })
    done()
  } catch {
    // 用户取消关闭
  }
}

async function handleSave() {
  saving.value = true
  try {
    if (isEdit.value && form.id) {
      await updateResource(form.id, form)
    } else {
      await createResource(form)
    }
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    reset()
    dialogVisible.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function handleDelete(id?: number) {
  if (!id) return
  try {
    await deleteResource(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    // 统一拦截器处理错误提示
  }
}

function formatDateColumn(_row: ResourceRow, _column: unknown, value: string) {
  return formatDateTime(value)
}

onMounted(fetchData)
</script>
