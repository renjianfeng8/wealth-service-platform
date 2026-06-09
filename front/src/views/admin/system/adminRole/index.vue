<template>
  <AdminPageShell title="管理员角色关联" description="维护管理员与角色之间的授权关系。">
    <AdminFilterBar :model="query" :fields="filterFields" @search="handleSearch" @reset="handleReset" />

    <AdminDataTable :data="tableData" :loading="loading" :total="total" :pagination="query" @page-change="fetchData">
      <template #toolbar>
        <el-button type="primary" @click="handleAdd">添加关联</el-button>
      </template>

      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="adminId" label="管理员ID" width="110" />
      <el-table-column label="管理员" min-width="150" show-overflow-tooltip>
        <template #default="{ row }">{{ getAdminName(row.adminId) }}</template>
      </el-table-column>
      <el-table-column prop="roleId" label="角色ID" width="100" />
      <el-table-column label="角色名称" min-width="150" show-overflow-tooltip>
        <template #default="{ row }">{{ getRoleName(row.roleId) }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" :formatter="formatDateColumn" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-popconfirm title="确定解除该关联？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button type="danger" link>解除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </AdminDataTable>

    <AdminFormDialog
      v-model="dialogVisible"
      title="添加关联"
      :model="form"
      :rules="rules"
      :saving="saving"
      :before-close="handleDialogClose"
      width="460px"
      @submit="handleSave"
    >
      <el-form-item label="管理员" prop="adminId">
        <el-select v-model="form.adminId" filterable style="width: 100%" placeholder="选择管理员">
          <el-option v-for="admin in adminList" :key="admin.id" :label="admin.nickName || admin.username" :value="admin.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="角色" prop="roleId">
        <el-select v-model="form.roleId" filterable style="width: 100%" placeholder="选择角色">
          <el-option v-for="role in roleList" :key="role.id" :label="role.name" :value="role.id" />
        </el-select>
      </el-form-item>
    </AdminFormDialog>
  </AdminPageShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormRules } from 'element-plus'
import AdminPageShell from '@/components/admin/AdminPageShell.vue'
import AdminFilterBar from '@/components/admin/AdminFilterBar.vue'
import AdminDataTable from '@/components/admin/AdminDataTable.vue'
import AdminFormDialog from '@/components/admin/AdminFormDialog.vue'
import { useFormGuard } from '@/composables/useFormGuard'
import { getAdminRoleRelationPage, createAdminRoleRelation, deleteAdminRoleRelation, getAdminList, getRoleList } from '@/api/system'
import { formatDateTime } from '@/utils/format'
import type { DictItem, UmsAdmin, UmsRole } from '@/types'
import type { AdminFilterField } from '@/components/admin/AdminFilterBar.vue'

type RelationQuery = {
  pageNum: number
  pageSize: number
  adminId: number | ''
}

type AdminRoleRelation = {
  id?: number
  adminId?: number
  roleId?: number
  createTime?: string
}

const loading = ref(false)
const saving = ref(false)
const tableData = ref<AdminRoleRelation[]>([])
const total = ref(0)
const adminList = ref<UmsAdmin[]>([])
const roleList = ref<UmsRole[]>([])
const dialogVisible = ref(false)

const query = reactive<RelationQuery>({ pageNum: 1, pageSize: 10, adminId: '' })
const form = reactive<AdminRoleRelation>({ adminId: undefined, roleId: undefined })
const { isDirty, reset } = useFormGuard(form)

const adminMap = new Map<number, string>()
const roleMap = new Map<number, string>()

const filterFields = computed<AdminFilterField[]>(() => [
  { prop: 'adminId', label: '管理员', type: 'select', options: adminOptions.value, width: '200px', placeholder: '选择管理员' },
])

const adminOptions = computed<DictItem[]>(() => adminList.value.map((admin) => ({
  label: admin.nickName || admin.username,
  value: admin.id,
})))

const rules: FormRules = {
  adminId: [{ required: true, message: '请选择管理员', trigger: 'change' }],
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }],
}

function getAdminName(id?: number) {
  return id ? adminMap.get(id) || `ID:${id}` : '-'
}

function getRoleName(id?: number) {
  return id ? roleMap.get(id) || `ID:${id}` : '-'
}

async function loadSelectData() {
  try {
    const [adminRes, roleRes] = await Promise.all([getAdminList(), getRoleList()])
    adminList.value = adminRes.data || []
    roleList.value = roleRes.data || []
    adminMap.clear()
    roleMap.clear()
    adminList.value.forEach((admin) => {
      if (admin.id) adminMap.set(admin.id, admin.nickName || admin.username)
    })
    roleList.value.forEach((role) => {
      if (role.id) roleMap.set(role.id, role.name)
    })
  } catch {
    // 统一拦截器处理错误提示
  }
}

async function fetchData() {
  loading.value = true
  try {
    const params: { pageNum: number; pageSize: number; adminId?: number } = {
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    }
    if (query.adminId !== '') params.adminId = query.adminId
    const res = await getAdminRoleRelationPage(params)
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
  query.adminId = ''
  handleSearch()
}

function handleAdd() {
  Object.assign(form, { adminId: undefined, roleId: undefined })
  reset()
  dialogVisible.value = true
}

async function handleSave() {
  saving.value = true
  try {
    await createAdminRoleRelation(form)
    ElMessage.success('关联成功')
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
    await deleteAdminRoleRelation(id)
    ElMessage.success('已解除关联')
    fetchData()
  } catch {
    // 统一拦截器处理错误提示
  }
}

async function handleDialogClose(done: () => void) {
  if (!isDirty()) {
    done()
    return
  }
  try {
    await ElMessageBox.confirm('有关联信息未保存，确定关闭吗？', '离开确认', { type: 'warning' })
    done()
  } catch {
    // 用户取消关闭
  }
}

function formatDateColumn(_row: AdminRoleRelation, _column: unknown, value: string) {
  return formatDateTime(value)
}

onMounted(async () => {
  await loadSelectData()
  fetchData()
})
</script>
