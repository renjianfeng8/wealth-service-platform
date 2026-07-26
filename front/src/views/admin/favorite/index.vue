<template>
  <AdminPageShell title="自选管理" description="管理用户自选产品关系，便于排查关注与推荐数据。">
    <AdminFilterBar :model="query" :fields="filterFields" @search="handleSearch(query)" @reset="handleReset" />

    <AdminDataTable :data="tableData" :loading="loading" :total="total" :pagination="query" @page-change="fetchData">
      <template #toolbar>
        <el-button type="primary" @click="handleAdd(resetForm, reset)">新增自选</el-button>
      </template>

      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="userId" label="用户ID" width="110" />
      <el-table-column prop="productCode" label="产品编码" min-width="150" show-overflow-tooltip />
      <el-table-column prop="createTime" label="添加时间" width="170" :formatter="formatDateColumn" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(form, row, reset)">编辑</el-button>
          <el-popconfirm title="确定删除该自选？" @confirm="handleDelete(row.id, deleteFavorite)">
            <template #reference>
              <el-button type="danger" link>删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </AdminDataTable>

    <AdminFormDialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑自选' : '新增自选'"
      :model="form"
      :rules="rules"
      :saving="saving"
      :before-close="handleDialogClose(isDirty)"
      width="420px"
      @submit="handleSave"
    >
      <el-form-item label="用户ID" prop="userId">
        <el-input-number v-model="form.userId" :min="1" controls-position="right" style="width: 100%" />
      </el-form-item>
      <el-form-item label="产品编码" prop="productCode">
        <el-input v-model="form.productCode" />
      </el-form-item>
    </AdminFormDialog>
  </AdminPageShell>
</template>

<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormRules } from 'element-plus'
import AdminPageShell from '@/components/admin/AdminPageShell.vue'
import AdminFilterBar from '@/components/admin/AdminFilterBar.vue'
import AdminDataTable from '@/components/admin/AdminDataTable.vue'
import AdminFormDialog from '@/components/admin/AdminFormDialog.vue'
import { useFormGuard } from '@/composables/useFormGuard'
import { useCrudPage } from '@/composables/useCrudPage'
import { getFavoritePage, createFavorite, updateFavorite, deleteFavorite } from '@/api/favorite'
import type { WeaUserFavorite } from '@/types'
import type { AdminFilterField } from '@/components/admin/AdminFilterBar.vue'

type FavoriteQuery = {
  pageNum: number
  pageSize: number
  userId?: number
  productCode: string
}

type FavoriteForm = Omit<WeaUserFavorite, 'userId'> & {
  userId?: number
}

const filterFields: AdminFilterField[] = [
  { prop: 'userId', label: '用户ID', type: 'number', width: '148px' },
  { prop: 'productCode', label: '产品编码', placeholder: '搜索产品编码' },
]

const query = reactive<FavoriteQuery>({ pageNum: 1, pageSize: 10, userId: undefined, productCode: '' })
const form = reactive<FavoriteForm>({ id: undefined, userId: undefined, productCode: '' })
const { isDirty, reset } = useFormGuard(form)
const { loading, saving, tableData, total, dialogVisible, isEdit, handleSearch, handleAdd, handleEdit, handleDialogClose, handleDelete, formatDateColumn } = useCrudPage<WeaUserFavorite>(fetchData)

const rules: FormRules = {
  userId: [{ required: true, message: '请输入用户ID', trigger: 'blur' }],
  productCode: [{ required: true, message: '请输入产品编码', trigger: 'blur' }],
}

async function fetchData() {
  loading.value = true
  try {
    const params: { pageNum: number; pageSize: number; userId?: number; productCode?: string } = {
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    }
    if (query.userId != null) params.userId = query.userId // B4: != null 同时排除 undefined 和 null
    if (query.productCode) params.productCode = query.productCode
    const res = await getFavoritePage(params)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function handleReset() {
  query.userId = undefined
  query.productCode = ''
  handleSearch(query)
}

function resetForm() {
  Object.assign(form, { id: undefined, userId: undefined, productCode: '' })
}

async function handleSave() {
  if (form.userId === undefined) return
  saving.value = true
  try {
    if (isEdit.value && form.id) {
      await updateFavorite(form.id, form)
    } else {
      await createFavorite(form)
    }
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    reset()
    dialogVisible.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

onMounted(fetchData)
</script>
