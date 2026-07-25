<template>
  <AdminPageShell title="角色管理" description="维护后台角色、排序、描述和启用状态。">
    <AdminFilterBar :model="query" :fields="filterFields" @search="handleSearch" @reset="handleReset" />

    <AdminDataTable :data="tableData" :loading="loading" :total="total" :pagination="query" @page-change="fetchData">
      <template #toolbar>
        <el-button type="primary" @click="handleAdd">新增角色</el-button>
      </template>

      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="角色名称" min-width="140" show-overflow-tooltip />
      <el-table-column prop="description" label="描述" min-width="220" show-overflow-tooltip />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" :formatter="formatDateColumn" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-popconfirm title="确定删除该角色？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button type="danger" link>删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </AdminDataTable>

    <AdminFormDialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑角色' : '新增角色'"
      :model="form"
      :rules="rules"
      :saving="saving"
      :before-close="handleDialogClose"
      width="520px"
      @submit="handleSave"
    >
      <el-form-item label="角色名称" prop="name">
        <el-input v-model="form.name" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="form.description" type="textarea" :rows="2" />
      </el-form-item>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="排序">
            <el-input-number v-model="form.sort" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态">
            <el-radio-group v-model="form.status">
              <el-radio :value="1">正常</el-radio>
              <el-radio :value="0">禁用</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>
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
import { getRolePage, createRole, updateRole, deleteRole } from '@/api/system'
import { formatDateTime, statusTag, statusText } from '@/utils/format'
import { STATUS_OPTIONS } from '@/types'
import type { UmsRole } from '@/types'
import type { AdminFilterField } from '@/components/admin/AdminFilterBar.vue'

type RoleQuery = {
  pageNum: number
  pageSize: number
  name: string
  status: number | ''
}

type RoleRow = UmsRole & {
  createTime?: string
}

const filterFields: AdminFilterField[] = [
  { prop: 'name', label: '角色名称', placeholder: '搜索角色名称' },
  { prop: 'status', label: '状态', type: 'select', options: STATUS_OPTIONS, width: '132px' },
]

const loading = ref(false)
const saving = ref(false)
const tableData = ref<RoleRow[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)

const query = reactive<RoleQuery>({ pageNum: 1, pageSize: 10, name: '', status: '' })
const form = reactive<RoleRow>({ id: undefined, name: '', description: '', sort: 0, status: 1 })
const { isDirty, reset } = useFormGuard(form)

const rules: FormRules = {
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
}

async function fetchData() {
  loading.value = true
  try {
    const params: { pageNum: number; pageSize: number; name?: string; status?: number } = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.name) params.name = query.name
    if (query.status !== '') params.status = query.status
    const res = await getRolePage(params)
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
  query.status = ''
  handleSearch()
}

function resetForm() {
  Object.assign(form, { id: undefined, name: '', description: '', sort: 0, status: 1 })
}

function handleAdd() {
  isEdit.value = false
  resetForm()
  reset()
  dialogVisible.value = true
}

function handleEdit(row: RoleRow) {
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
      await updateRole(form.id, form)
    } else {
      await createRole(form)
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
    await deleteRole(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    // 统一拦截器处理错误提示
  }
}

function formatDateColumn(_row: RoleRow, _column: unknown, value: string) {
  return formatDateTime(value)
}

onMounted(fetchData)
</script>
