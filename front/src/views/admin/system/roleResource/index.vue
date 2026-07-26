<template>
  <AdminPageShell title="角色资源关联" description="维护角色可访问的后端资源权限。">
    <AdminFilterBar :model="query" :fields="filterFields" @search="handleSearch(query)" @reset="handleReset" />

    <AdminDataTable :data="tableData" :loading="loading" :total="total" :pagination="query" @page-change="fetchData">
      <template #toolbar>
        <el-button type="primary" @click="handleAdd">添加关联</el-button>
      </template>

      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="roleId" label="角色ID" width="100" />
      <el-table-column label="角色名称" min-width="150" show-overflow-tooltip>
        <template #default="{ row }">{{ getRoleName(row.roleId) }}</template>
      </el-table-column>
      <el-table-column prop="resourceId" label="资源ID" width="100" />
      <el-table-column label="资源名称" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">{{ getResourceName(row.resourceId) }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" :formatter="formatDateColumn" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-popconfirm title="确定解除该关联？" @confirm="handleDelete(row.id, deleteRoleResourceRelation)">
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
      <el-form-item label="角色" prop="roleId">
        <el-select v-model="form.roleId" filterable style="width: 100%" placeholder="选择角色">
          <el-option v-for="role in roleList" :key="role.id" :label="role.name" :value="role.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="资源" prop="resourceId">
        <el-select v-model="form.resourceId" filterable style="width: 100%" placeholder="选择资源">
          <el-option v-for="resource in resourceList" :key="resource.id" :label="resource.name" :value="resource.id" />
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
import { useCrudPage } from '@/composables/useCrudPage'
import { getRoleResourceRelationPage, createRoleResourceRelation, deleteRoleResourceRelation, getRoleList, getResourceList } from '@/api/system'
import type { DictItem, UmsResource, UmsRole } from '@/types'
import type { AdminFilterField } from '@/components/admin/AdminFilterBar.vue'

type RelationQuery = {
  pageNum: number
  pageSize: number
  roleId: number | ''
}

type RoleResourceRelation = {
  id?: number
  roleId?: number
  resourceId?: number
  createTime?: string
}

const roleList = ref<UmsRole[]>([])
const resourceList = ref<UmsResource[]>([])
const { loading, saving, tableData, total, dialogVisible, handleSearch, handleDelete, formatDateColumn } = useCrudPage<RoleResourceRelation>(fetchData)

const query = reactive<RelationQuery>({ pageNum: 1, pageSize: 10, roleId: '' })
const form = reactive<RoleResourceRelation>({ roleId: undefined, resourceId: undefined })
const { isDirty, reset } = useFormGuard(form)

const roleMap = new Map<number, string>()
const resourceMap = new Map<number, string>()

const filterFields = computed<AdminFilterField[]>(() => [
  { prop: 'roleId', label: '角色', type: 'select', options: roleOptions.value, width: '200px', placeholder: '选择角色' },
])

const roleOptions = computed<DictItem[]>(() => roleList.value.map((role) => ({
  label: role.name,
  value: role.id,
})))

const rules: FormRules = {
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }],
  resourceId: [{ required: true, message: '请选择资源', trigger: 'change' }],
}

function getRoleName(id?: number) {
  return id ? roleMap.get(id) || `ID:${id}` : '-'
}

function getResourceName(id?: number) {
  return id ? resourceMap.get(id) || `ID:${id}` : '-'
}

async function loadSelectData() {
  try {
    const [roleRes, resourceRes] = await Promise.all([getRoleList(), getResourceList()])
    roleList.value = roleRes.data || []
    resourceList.value = resourceRes.data || []
    roleMap.clear()
    resourceMap.clear()
    roleList.value.forEach((role) => {
      if (role.id) roleMap.set(role.id, role.name)
    })
    resourceList.value.forEach((resource) => {
      if (resource.id) resourceMap.set(resource.id, resource.name)
    })
  } catch {
    // 统一拦截器处理错误提示
  }
}

async function fetchData() {
  loading.value = true
  try {
    const params: { pageNum: number; pageSize: number; roleId?: number } = {
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    }
    if (query.roleId !== '') params.roleId = query.roleId
    const res = await getRoleResourceRelationPage(params)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function handleReset() {
  query.roleId = ''
  handleSearch(query)
}

function handleAdd() {
  Object.assign(form, { roleId: undefined, resourceId: undefined })
  reset()
  dialogVisible.value = true
}

async function handleSave() {
  saving.value = true
  try {
    await createRoleResourceRelation(form)
    ElMessage.success('关联成功')
    reset()
    dialogVisible.value = false
    fetchData()
  } finally {
    saving.value = false
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

onMounted(async () => {
  await loadSelectData()
  fetchData()
})
</script>
