<template>
  <AdminPageShell title="用户管理" description="维护普通用户账号、联系方式与启用状态。">
    <AdminFilterBar :model="query" :fields="filterFields" @search="handleSearch" @reset="handleReset" />

    <AdminDataTable :data="tableData" :loading="loading" :total="total" :pagination="query" @page-change="fetchData">
      <template #toolbar>
        <el-button type="primary" @click="handleAdd">新增用户</el-button>
      </template>

      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="用户名" min-width="130" show-overflow-tooltip />
      <el-table-column prop="nickname" label="昵称" min-width="130" show-overflow-tooltip />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" :formatter="formatDateColumn" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-popconfirm title="确定删除该用户？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button type="danger" link>删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </AdminDataTable>

    <AdminFormDialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑用户' : '新增用户'"
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
      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="form.nickname" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="form.phone" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
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
import { getUserPage, createUser, updateUser, deleteUser } from '@/api/user'
import { formatDateTime, statusTag, statusText } from '@/utils/format'
import { STATUS_OPTIONS } from '@/types'
import type { AdminFilterField } from '@/components/admin/AdminFilterBar.vue'

type UserQuery = {
  pageNum: number
  pageSize: number
  username: string
  status: number | ''
}

type UserForm = {
  id?: number
  username: string
  password?: string
  nickname?: string
  phone?: string
  status: number
}

const filterFields: AdminFilterField[] = [
  { prop: 'username', label: '用户名', placeholder: '搜索用户名' },
  { prop: 'status', label: '状态', type: 'select', options: STATUS_OPTIONS, width: '132px' },
]

const loading = ref(false)
const saving = ref(false)
const tableData = ref<UserForm[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)

const query = reactive<UserQuery>({ pageNum: 1, pageSize: 10, username: '', status: '' })
const form = reactive<UserForm>({ id: undefined, username: '', password: '', nickname: '', phone: '', status: 1 })
const { isDirty, reset } = useFormGuard(form)

const validatePhone = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (value && !/^1\d{10}$/.test(value)) {
    callback(new Error('手机号格式不正确'))
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
  phone: [{ validator: validatePhone, trigger: 'blur' }],
}

async function fetchData() {
  loading.value = true
  try {
    const params: { pageNum: number; pageSize: number; username?: string; status?: number } = {
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    }
    if (query.username) params.username = query.username
    if (query.status !== '') params.status = query.status
    const res = await getUserPage(params)
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
  Object.assign(form, { id: undefined, username: '', password: '', nickname: '', phone: '', status: 1 })
}

function handleAdd() {
  isEdit.value = false
  resetForm()
  reset()
  dialogVisible.value = true
}

function handleEdit(row: UserForm) {
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
    const payload: UserForm = { ...form }
    if (isEdit.value && !payload.password) delete payload.password
    if (isEdit.value && form.id) {
      await updateUser(form.id, payload)
    } else {
      await createUser(payload)
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
    await deleteUser(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    // 统一拦截器处理错误提示
  }
}

function formatDateColumn(_row: UserForm, _column: unknown, value: string) {
  return formatDateTime(value)
}

onMounted(fetchData)
</script>
