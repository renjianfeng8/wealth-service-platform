<template>
  <AdminPageShell title="管理员管理" description="维护后台管理员账号、邮箱和启用状态。">
    <AdminFilterBar :model="query" :fields="filterFields" @search="handleSearch" @reset="handleReset" />

    <AdminDataTable :data="tableData" :loading="loading" :total="total" :pagination="query" @page-change="fetchData">
      <template #toolbar>
        <el-button type="primary" @click="handleAdd">新增管理员</el-button>
      </template>

      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="用户名" min-width="130" show-overflow-tooltip />
      <el-table-column prop="nickName" label="昵称" min-width="130" show-overflow-tooltip />
      <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" :formatter="formatDateColumn" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-popconfirm title="确定删除该管理员？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button type="danger" link>删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </AdminDataTable>

    <AdminFormDialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑管理员' : '新增管理员'"
      :model="form"
      :rules="rules"
      :saving="saving"
      :before-close="handleDialogClose"
      width="520px"
      @submit="handleSave"
    >
      <el-form-item label="用户名" prop="username">
        <el-input v-model="form.username" />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input v-model="form.password" type="password" show-password />
      </el-form-item>
      <el-form-item label="昵称" prop="nickName">
        <el-input v-model="form.nickName" />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" />
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="form.status">
          <el-radio :value="1">正常</el-radio>
          <el-radio :value="0">禁用</el-radio>
        </el-radio-group>
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
import { getAdminPage, createAdmin, updateAdmin, deleteAdmin } from '@/api/system'
import { formatDateTime, statusTag, statusText } from '@/utils/format'
import { STATUS_OPTIONS } from '@/types'
import type { UmsAdmin } from '@/types'
import type { AdminFilterField } from '@/components/admin/AdminFilterBar.vue'

type AdminQuery = {
  pageNum: number
  pageSize: number
  username: string
  status: number | ''
}

type AdminForm = UmsAdmin & {
  password?: string
  createTime?: string
}

const filterFields: AdminFilterField[] = [
  { prop: 'username', label: '用户名', placeholder: '搜索用户名' },
  { prop: 'status', label: '状态', type: 'select', options: STATUS_OPTIONS, width: '132px' },
]

const loading = ref(false)
const saving = ref(false)
const tableData = ref<AdminForm[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)

const query = reactive<AdminQuery>({ pageNum: 1, pageSize: 10, username: '', status: '' })
const form = reactive<AdminForm>({ id: undefined, username: '', password: '', nickName: '', email: '', status: 1 })
const { isDirty, reset } = useFormGuard(form)

const validateEmail = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (value && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
    callback(new Error('邮箱格式不正确'))
    return
  }
  callback()
}

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{
    validator: (_rule: unknown, value: string, callback: (error?: Error) => void) => {
      if (!isEdit.value && !value) {
        callback(new Error('请输入密码'))
        return
      }
      callback()
    },
    trigger: 'blur',
  }],
  email: [{ validator: validateEmail, trigger: 'blur' }],
}

async function fetchData() {
  loading.value = true
  try {
    const params: { pageNum: number; pageSize: number; username?: string; status?: number } = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.username) params.username = query.username
    if (query.status !== '') params.status = query.status
    const res = await getAdminPage(params)
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
  query.username = ''
  query.status = ''
  handleSearch()
}

function resetForm() {
  Object.assign(form, { id: undefined, username: '', password: '', nickName: '', email: '', status: 1 })
}

function handleAdd() {
  isEdit.value = false
  resetForm()
  reset()
  dialogVisible.value = true
}

function handleEdit(row: AdminForm) {
  isEdit.value = true
  Object.assign(form, row, { password: '' })
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
    const payload: AdminForm = { ...form }
    if (isEdit.value && !payload.password) delete payload.password
    if (isEdit.value && form.id) {
      await updateAdmin(form.id, payload)
    } else {
      await createAdmin(payload)
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
    await deleteAdmin(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    // 统一拦截器处理错误提示
  }
}

function formatDateColumn(_row: AdminForm, _column: unknown, value: string) {
  return formatDateTime(value)
}

onMounted(fetchData)
</script>
