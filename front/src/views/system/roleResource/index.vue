<template>
  <div class="page">
    <div class="page-header"><h3>角色资源关联</h3></div>
    <el-card shadow="never" class="search-card">
      <el-form :model="query" inline>
        <el-form-item label="角色"><el-select v-model="query.roleId" clearable filterable style="width:200px" placeholder="选择角色"><el-option v-for="r in roleList" :key="r.id" :label="r.name" :value="r.id" /></el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="handleSearch">查询</el-button><el-button @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card shadow="never" style="margin-top:16px;">
      <div style="margin-bottom:16px;"><el-button type="primary" @click="handleAdd">添加关联</el-button></div>
      <el-table :data="tableData" stripe v-loading="loading" border empty-text="暂无数据">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="roleId" label="角色ID" width="70" />
        <el-table-column label="角色名称" min-width="130">
          <template #default="{ row }">{{ getRoleName(row.roleId) }}</template>
        </el-table-column>
        <el-table-column prop="resourceId" label="资源ID" width="70" />
        <el-table-column label="资源名称" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ getResourceName(row.resourceId) }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" :formatter="(_r:any,_c:any,v:any)=>formatDateTime(v)" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-popconfirm title="确定解除关联？" @confirm="handleDelete(row.id)"><template #reference><el-button type="danger" link>解除</el-button></template></el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" :page-sizes="[10,20,50]" layout="total, sizes, prev, pager, next" @size-change="handleSizeChange" @current-change="fetchData" />
      </div>
    </el-card>
    <el-dialog v-model="dialogVisible" title="添加关联" width="450px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="角色" prop="roleId"><el-select v-model="form.roleId" filterable style="width:100%" placeholder="选择角色"><el-option v-for="r in roleList" :key="r.id" :label="r.name" :value="r.id" /></el-select></el-form-item>
        <el-form-item label="资源" prop="resourceId"><el-select v-model="form.resourceId" filterable style="width:100%" placeholder="选择资源"><el-option v-for="r in resourceList" :key="r.id" :label="r.name" :value="r.id" /></el-select></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" :loading="saving" @click="handleSave">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getRoleResourceRelationPage, createRoleResourceRelation, deleteRoleResourceRelation, getRoleList, getResourceList } from '@/api/system'
import { formatDateTime } from '@/utils/format'

const loading = ref(false); const saving = ref(false)
const tableData = ref<any[]>([]); const total = ref(0)
const roleList = ref<any[]>([]); const resourceList = ref<any[]>([])
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const query = reactive({ pageNum: 1, pageSize: 10, roleId: '' })
const form = reactive({ roleId: undefined, resourceId: undefined })
const rules: FormRules = {
  roleId: [{ required: true, message: '请选择角色' }],
  resourceId: [{ required: true, message: '请选择资源' }],
}

const roleMap = new Map<number, string>()
const resourceMap = new Map<number, string>()
function getRoleName(id: number) { return roleMap.get(id) || `ID:${id}` }
function getResourceName(id: number) { return resourceMap.get(id) || `ID:${id}` }

async function loadSelectData() {
  const [roleRes, resourceRes] = await Promise.all([getRoleList(), getResourceList()])
  roleList.value = roleRes.data || []
  resourceList.value = resourceRes.data || []
  roleList.value.forEach((r: any) => roleMap.set(r.id, r.name))
  resourceList.value.forEach((r: any) => resourceMap.set(r.id, r.name))
}

async function fetchData() {
  loading.value = true
  try {
    const params: any = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.roleId !== '') params.roleId = query.roleId
    const res = await getRoleResourceRelationPage(params)
    tableData.value = res.data.records || []; total.value = res.data.total || 0
  } finally { loading.value = false }
}
function handleSearch() { query.pageNum = 1; fetchData() }
function handleReset() { query.roleId = ''; handleSearch() }
function handleSizeChange() { query.pageNum = 1; fetchData() }
function handleAdd() { form.roleId = undefined; form.resourceId = undefined; dialogVisible.value = true }
async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return; saving.value = true
  try {
    await createRoleResourceRelation(form)
    ElMessage.success('关联成功'); dialogVisible.value = false; fetchData()
  } finally { saving.value = false }
}
async function handleDelete(id: number) { try { await deleteRoleResourceRelation(id); ElMessage.success('已解除关联'); fetchData() } catch { /* handled by interceptor */ } }

onMounted(async () => { await loadSelectData(); fetchData() })
</script>
<style scoped>
.page-header h3 { margin-bottom: 16px; }
</style>
