<template>
  <AdminPageShell title="行情数据" description="维护产品实时价格、开收盘价与涨跌幅。">
    <template #toolbar>
      <span class="sse-status" :class="sseConnected ? 'connected' : 'disconnected'">
        <span class="sse-dot"></span>
        {{ sseConnected ? '实时已连接' : '实时已断开' }}
      </span>
    </template>

    <AdminFilterBar :model="query" :fields="filterFields" @search="handleSearch" @reset="handleReset" />

    <AdminDataTable :data="tableData" :loading="loading" :total="total" :pagination="query" @page-change="fetchData">
      <template #toolbar>
        <el-button type="primary" @click="handleAdd">新增行情</el-button>
      </template>

      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="productCode" label="产品编码" min-width="130" show-overflow-tooltip />
      <el-table-column prop="currentPrice" label="当前价" width="110" :formatter="formatPriceColumn" />
      <el-table-column prop="openPrice" label="开盘价" width="110" :formatter="formatPriceColumn" />
      <el-table-column prop="closePrice" label="收盘价" width="110" :formatter="formatPriceColumn" />
      <el-table-column prop="highestPrice" label="最高价" width="110" :formatter="formatPriceColumn" />
      <el-table-column prop="lowestPrice" label="最低价" width="110" :formatter="formatPriceColumn" />
      <el-table-column prop="riseFallRate" label="涨跌幅" width="110">
        <template #default="{ row }">
          <span :class="(row.riseFallRate || 0) >= 0 ? 'fl-rise' : 'fl-fall'">
            {{ formatRate(row.riseFallRate) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="marketTime" label="行情时间" width="170" :formatter="formatDateColumn" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-popconfirm title="确定删除该行情？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button type="danger" link>删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </AdminDataTable>

    <AdminFormDialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑行情' : '新增行情'"
      :model="form"
      :rules="rules"
      :saving="saving"
      :before-close="handleDialogClose"
      width="620px"
      @submit="handleSave"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="产品编码" prop="productCode">
            <el-input v-model="form.productCode" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="当前价" prop="currentPrice">
            <el-input-number v-model="form.currentPrice" :precision="2" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="开盘价">
            <el-input-number v-model="form.openPrice" :precision="2" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="收盘价">
            <el-input-number v-model="form.closePrice" :precision="2" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="最高价">
            <el-input-number v-model="form.highestPrice" :precision="2" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="最低价">
            <el-input-number v-model="form.lowestPrice" :precision="2" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="涨跌额">
            <el-input-number v-model="form.riseFall" :precision="2" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="涨跌幅">
            <el-input-number v-model="form.riseFallRate" :precision="4" :step="0.001" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="行情时间">
        <el-date-picker v-model="form.marketTime" type="datetime" style="width: 100%" value-format="YYYY-MM-DDTHH:mm:ss" />
      </el-form-item>
    </AdminFormDialog>
  </AdminPageShell>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormRules } from 'element-plus'
import AdminPageShell from '@/components/admin/AdminPageShell.vue'
import AdminFilterBar from '@/components/admin/AdminFilterBar.vue'
import AdminDataTable from '@/components/admin/AdminDataTable.vue'
import AdminFormDialog from '@/components/admin/AdminFormDialog.vue'
import { useFormGuard } from '@/composables/useFormGuard'
import { getMarketDataPage, createMarketData, updateMarketData, deleteMarketData } from '@/api/product'
import { formatDateTime, formatPrice, formatRate } from '@/utils/format'
import type { WeaMarketData } from '@/types'
import type { AdminFilterField } from '@/components/admin/AdminFilterBar.vue'
import { createMarketSSE, onMarketUpdate } from '@/utils/sse'

type MarketQuery = {
  pageNum: number
  pageSize: number
  productCode: string
}

const filterFields: AdminFilterField[] = [
  { prop: 'productCode', label: '产品编码', placeholder: '搜索产品编码' },
]

const loading = ref(false)
const saving = ref(false)
const tableData = ref<WeaMarketData[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const sseConnected = ref(false)
let eventSource: EventSource | null = null

const query = reactive<MarketQuery>({ pageNum: 1, pageSize: 10, productCode: '' })
const form = reactive<WeaMarketData>({
  id: undefined,
  productCode: '',
  currentPrice: 0,
  openPrice: 0,
  closePrice: 0,
  highestPrice: 0,
  lowestPrice: 0,
  riseFall: 0,
  riseFallRate: 0,
  marketTime: '',
})
const { isDirty, reset } = useFormGuard(form)

const rules: FormRules = {
  productCode: [{ required: true, message: '请输入产品编码', trigger: 'blur' }],
  currentPrice: [{ required: true, message: '请输入当前价', trigger: 'blur' }],
}

async function fetchData() {
  loading.value = true
  try {
    const params: { pageNum: number; pageSize: number; productCode?: string } = {
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    }
    if (query.productCode) params.productCode = query.productCode
    const res = await getMarketDataPage(params)
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
  query.productCode = ''
  handleSearch()
}

function resetForm() {
  Object.assign(form, {
    id: undefined,
    productCode: '',
    currentPrice: 0,
    openPrice: 0,
    closePrice: 0,
    highestPrice: 0,
    lowestPrice: 0,
    riseFall: 0,
    riseFallRate: 0,
    marketTime: '',
  })
}

function handleAdd() {
  isEdit.value = false
  resetForm()
  reset()
  dialogVisible.value = true
}

function handleEdit(row: WeaMarketData) {
  isEdit.value = true
  Object.assign(form, row)
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
      await updateMarketData(form.id, form)
    } else {
      await createMarketData(form)
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
    await deleteMarketData(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    // 统一拦截器处理错误提示
  }
}

function handleMarketUpdate(data: WeaMarketData[]) {
  const dataMap = new Map(data.map((item) => [item.productCode, item]))
  tableData.value = tableData.value.map((item) => {
    const update = dataMap.get(item.productCode)
    return update ? { ...item, ...update } : item
  })
}

function formatPriceColumn(_row: WeaMarketData, _column: unknown, value: number) {
  return formatPrice(value)
}

function formatDateColumn(_row: WeaMarketData, _column: unknown, value: string) {
  return formatDateTime(value)
}

onMounted(() => {
  fetchData()
  eventSource = createMarketSSE((connected) => {
    sseConnected.value = connected
  })
  onMarketUpdate(eventSource, handleMarketUpdate)
})

onUnmounted(() => {
  eventSource?.close()
  eventSource = null
})
</script>

<style scoped>
.sse-status {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
}

.sse-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.sse-status.connected {
  color: #16a34a;
  background: #dcfce7;
}

.sse-status.connected .sse-dot {
  background: #16a34a;
}

.sse-status.disconnected {
  color: #dc2626;
  background: #fee2e2;
}

.sse-status.disconnected .sse-dot {
  background: #dc2626;
}
</style>
