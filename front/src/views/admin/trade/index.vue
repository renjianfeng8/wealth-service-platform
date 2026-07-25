<template>
  <AdminPageShell title="交易委托管理" description="跟踪委托订单、成交状态与交易方向。">
    <AdminFilterBar :model="query" :fields="filterFields" @search="handleSearch" @reset="handleReset" />

    <AdminDataTable
      :data="tableData"
      :loading="loading"
      :total="total"
      :pagination="query"
      @page-change="fetchData"
    >
      <template #toolbar>
        <el-button type="primary" @click="handleAdd">新增委托</el-button>
      </template>

      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="orderNo" label="订单号" min-width="180" show-overflow-tooltip />
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column prop="productCode" label="产品编码" min-width="130" show-overflow-tooltip />
      <el-table-column prop="tradeType" label="方向" width="90">
        <template #default="{ row }">
          <el-tag :type="row.tradeType === 1 ? 'danger' : 'success'">{{ tradeTypeText(row.tradeType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="entrustPrice" label="委托价" width="110" :formatter="formatPriceColumn" />
      <el-table-column prop="entrustNum" label="数量" width="90" />
      <el-table-column prop="orderStatus" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="orderStatusTag(row.orderStatus)">{{ orderStatusText(row.orderStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" :formatter="formatDateColumn" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-popconfirm title="确定删除该委托？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button type="danger" link>删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </AdminDataTable>

    <AdminFormDialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑委托' : '新增委托'"
      :model="form"
      :rules="rules"
      :saving="saving"
      :before-close="handleDialogClose"
      @submit="handleSave"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="用户ID" prop="userId">
            <el-input-number v-model="form.userId" :min="1" controls-position="right" style="width: 100%" />
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
          <el-form-item label="交易方向" prop="tradeType">
            <el-select v-model="form.tradeType" style="width: 100%">
              <el-option v-for="item in tradeTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="委托价" prop="entrustPrice">
            <el-input-number v-model="form.entrustPrice" :min="0.01" :precision="2" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="委托数量" prop="entrustNum">
        <el-input-number v-model="form.entrustNum" :min="1" controls-position="right" style="width: 100%" />
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
import { getTradeOrderPage, createTradeOrder, updateTradeOrder, deleteTradeOrder } from '@/api/trade'
import { formatDateTime, formatPrice } from '@/utils/format'
import type { DictItem, WeaTradeOrder } from '@/types'
import type { AdminFilterField } from '@/components/admin/AdminFilterBar.vue'

type TradeQuery = {
  pageNum: number
  pageSize: number
  orderNo: string
  productCode: string
  orderStatus: number | ''
}

type TradeForm = Omit<WeaTradeOrder, 'userId'> & {
  userId?: number
}

const orderStatusOptions: DictItem[] = [
  { label: '待成交', value: 0 },
  { label: '已成交', value: 1 },
  { label: '已撤销', value: 2 },
]

const tradeTypeOptions: DictItem[] = [
  { label: '买入', value: 1 },
  { label: '卖出', value: 2 },
]

const filterFields: AdminFilterField[] = [
  { prop: 'orderNo', label: '订单号', placeholder: '搜索订单号' },
  { prop: 'productCode', label: '产品编码', placeholder: '搜索产品编码' },
  { prop: 'orderStatus', label: '状态', type: 'select', options: orderStatusOptions, width: '132px' },
]

const loading = ref(false)
const saving = ref(false)
const tableData = ref<WeaTradeOrder[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)

const query = reactive<TradeQuery>({
  pageNum: 1,
  pageSize: 10,
  orderNo: '',
  productCode: '',
  orderStatus: '',
})

const form = reactive<TradeForm>({
  id: undefined,
  userId: undefined,
  productCode: '',
  tradeType: 1,
  entrustPrice: 0,
  entrustNum: 0,
})

const { isDirty, reset } = useFormGuard(form)

const rules: FormRules = {
  userId: [{ required: true, message: '请输入用户ID', trigger: 'blur' }],
  productCode: [{ required: true, message: '请输入产品编码', trigger: 'blur' }],
  tradeType: [{ required: true, message: '请选择交易方向', trigger: 'change' }],
  entrustPrice: [{ required: true, message: '请输入委托价', trigger: 'blur' }],
  entrustNum: [{ required: true, message: '请输入委托数量', trigger: 'blur' }],
}

async function fetchData() {
  loading.value = true
  try {
    const params: {
      pageNum: number
      pageSize: number
      orderNo?: string
      productCode?: string
      orderStatus?: number
    } = {
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    }
    if (query.orderNo) params.orderNo = query.orderNo
    if (query.productCode) params.productCode = query.productCode
    if (query.orderStatus !== '') params.orderStatus = query.orderStatus

    const res = await getTradeOrderPage(params)
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
  query.orderNo = ''
  query.productCode = ''
  query.orderStatus = ''
  handleSearch()
}

function resetForm() {
  Object.assign(form, {
    id: undefined,
    userId: undefined,
    productCode: '',
    tradeType: 1,
    entrustPrice: 0,
    entrustNum: 0,
  })
}

function handleAdd() {
  isEdit.value = false
  resetForm()
  reset()
  dialogVisible.value = true
}

function handleEdit(row: WeaTradeOrder) {
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
  if (!form.userId) return

  saving.value = true
  try {
    if (isEdit.value && form.id) {
      await updateTradeOrder(form.id, form)
    } else {
      await createTradeOrder({
        ...form,
        userId: form.userId,
        idempotentKey: crypto.randomUUID(),
      })
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
    await deleteTradeOrder(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    // 统一拦截器处理错误提示
  }
}

function tradeTypeText(value?: number) {
  return tradeTypeOptions.find((item) => item.value === value)?.label || '-'
}

function orderStatusText(value?: number) {
  return orderStatusOptions.find((item) => item.value === value)?.label || '-'
}

function orderStatusTag(value?: number) {
  if (value === 1) return 'success'
  if (value === 2) return 'info'
  return 'warning'
}

function formatPriceColumn(_row: WeaTradeOrder, _column: unknown, value: number) {
  return formatPrice(value)
}

function formatDateColumn(_row: WeaTradeOrder, _column: unknown, value: string) {
  return formatDateTime(value)
}

onMounted(fetchData)
</script>
