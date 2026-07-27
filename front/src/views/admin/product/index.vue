<template>
  <AdminPageShell title="产品管理" description="维护产品基础信息、价格、涨跌幅与上下架状态。">
    <AdminFilterBar :model="query" :fields="filterFields" @search="handleSearch(query)" @reset="handleReset" />

    <AdminDataTable
      :data="tableData"
      :loading="loading"
      :total="total"
      :pagination="query"
      @page-change="fetchData"
    >
      <template #toolbar>
        <el-button type="primary" @click="handleAdd(resetForm, reset)">新增产品</el-button>
      </template>

      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="productName" label="产品名称" min-width="160" show-overflow-tooltip />
      <el-table-column prop="productCode" label="产品编码" min-width="130" show-overflow-tooltip />
      <el-table-column prop="productType" label="类型" width="110">
        <template #default="{ row }">
          <el-tag>{{ productTypeText(row.productType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="price" label="价格" width="110" :formatter="formatPriceColumn" />
      <el-table-column prop="riseFallRate" label="涨跌幅" width="110">
        <template #default="{ row }">
          <span :class="(row.riseFallRate || 0) >= 0 ? 'fl-rise' : 'fl-fall'">
            {{ formatRate(row.riseFallRate) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" :formatter="formatDateColumn" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(form, row, reset)">编辑</el-button>
          <el-popconfirm title="确定删除该产品？" @confirm="handleDelete(row.id, deleteProduct)">
            <template #reference>
              <el-button type="danger" link>删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </AdminDataTable>

    <AdminFormDialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑产品' : '新增产品'"
      :model="form"
      :rules="rules"
      :saving="saving"
      :before-close="handleDialogClose(isDirty)"
      @submit="handleSave"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="产品名称" prop="productName">
            <el-input v-model="form.productName" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="产品编码" prop="productCode">
            <el-input v-model="form.productCode" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="产品类型" prop="productType">
            <el-select v-model="form.productType" style="width: 100%">
              <el-option v-for="item in productTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="价格" prop="price">
            <el-input-number v-model="form.price" :min="0" :precision="2" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="涨跌额">
            <el-input-number v-model="form.riseFall" :precision="2" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="涨跌幅">
            <el-input-number v-model="form.riseFallRate" :precision="4" :step="0.001" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>
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
import { onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormRules } from 'element-plus'
import AdminPageShell from '@/components/admin/AdminPageShell.vue'
import AdminFilterBar from '@/components/admin/AdminFilterBar.vue'
import AdminDataTable from '@/components/admin/AdminDataTable.vue'
import AdminFormDialog from '@/components/admin/AdminFormDialog.vue'
import { useFormGuard } from '@/composables/useFormGuard'
import { useCrudPage } from '@/composables/useCrudPage'
import { getProductPage, createProduct, updateProduct, deleteProduct } from '@/api/product'
import { formatRate } from '@/utils/format'
import type { DictItem, WeaProduct } from '@/types'
import type { AdminFilterField } from '@/components/admin/AdminFilterBar.vue'

type ProductQuery = {
  pageNum: number
  pageSize: number
  productName: string
  productCode: string
  productType: number | ''
}

const productTypeOptions: DictItem[] = [
  { label: '黄金', value: 1 },
  { label: '白银', value: 2 },
  { label: '理财', value: 3 },
]

const filterFields: AdminFilterField[] = [
  { prop: 'productName', label: '产品名称', placeholder: '搜索产品名称' },
  { prop: 'productCode', label: '产品编码', placeholder: '搜索产品编码' },
  { prop: 'productType', label: '类型', type: 'select', options: productTypeOptions, width: '132px' },
]

const query = reactive<ProductQuery>({
  pageNum: 1,
  pageSize: 10,
  productName: '',
  productCode: '',
  productType: '',
})

const form = reactive<WeaProduct>({
  id: undefined,
  productName: '',
  productCode: '',
  productType: 1,
  price: 0,
  riseFall: 0,
  riseFallRate: 0,
  status: 1,
  sort: 0,
})

const { isDirty, reset } = useFormGuard(form)
const { loading, saving, tableData, total, dialogVisible, isEdit, handleSearch, handleAdd, handleEdit, handleDialogClose, handleDelete, formatDateColumn, formatPriceColumn } = useCrudPage<WeaProduct>(fetchData)

const rules: FormRules = {
  productName: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
  productCode: [{ required: true, message: '请输入产品编码', trigger: 'blur' }],
  productType: [{ required: true, message: '请选择产品类型', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
}

async function fetchData() {
  loading.value = true
  try {
    const params: {
      pageNum: number
      pageSize: number
      productName?: string
      productCode?: string
      productType?: number
    } = {
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    }
    if (query.productName) params.productName = query.productName
    if (query.productCode) params.productCode = query.productCode
    if (query.productType !== '') params.productType = query.productType

    const res = await getProductPage(params)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function handleReset() {
  query.productName = ''
  query.productCode = ''
  query.productType = ''
  handleSearch(query)
}

function resetForm() {
  Object.assign(form, {
    id: undefined,
    productName: '',
    productCode: '',
    productType: 1,
    price: 0,
    riseFall: 0,
    riseFallRate: 0,
    status: 1,
    sort: 0,
  })
}

async function handleSave() {
  saving.value = true
  try {
    if (isEdit.value && form.id) {
      await updateProduct(form.id, form)
    } else {
      await createProduct(form)
    }
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    reset()
    dialogVisible.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

function productTypeText(value?: number) {
  return productTypeOptions.find((item) => item.value === value)?.label || '-'
}

function statusText(value?: number) {
  return value === 1 ? '正常' : '禁用'
}

function statusTag(value?: number) {
  return value === 1 ? 'success' : 'info'
}

onMounted(fetchData)
</script>
