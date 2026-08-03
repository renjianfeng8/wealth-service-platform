<template>
  <AdminPageShell title="行情数据" description="维护产品实时价格、开收盘价与涨跌幅。">
    <template #toolbar>
      <span class="sse-status" :class="sseConnected ? 'connected' : 'disconnected'">
        <span class="sse-dot"></span>
        {{ sseConnected ? '实时已连接' : '实时已断开' }}
      </span>
    </template>

    <AdminFilterBar :model="query" :fields="filterFields" @search="handleSearch(query)" @reset="handleReset" />

    <AdminDataTable :data="tableData" :loading="loading" :total="total" :pagination="query" @page-change="fetchData">
      <template #toolbar>
        <el-button type="primary" @click="handleAdd(resetForm, reset)">新增行情</el-button>
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
          <el-button type="primary" link @click="handleEdit(form, row, reset)">编辑</el-button>
          <el-popconfirm title="确定删除该行情？" @confirm="handleDelete(row.id, deleteMarketData)">
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
      :before-close="handleDialogClose(isDirty)"
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
            <el-input-number v-model="form.currentPrice" :min="0" :precision="2" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="开盘价">
            <el-input-number v-model="form.openPrice" :min="0" :precision="2" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="收盘价">
            <el-input-number v-model="form.closePrice" :min="0" :precision="2" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="最高价" prop="highestPrice">
            <el-input-number v-model="form.highestPrice" :min="0" :precision="2" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="最低价" prop="lowestPrice">
            <el-input-number v-model="form.lowestPrice" :min="0" :precision="2" controls-position="right" style="width: 100%" />
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
      <el-form-item label="行情时间">
        <el-date-picker v-model="form.marketTime" type="datetime" style="width: 100%" value-format="YYYY-MM-DDTHH:mm:ss" />
      </el-form-item>
    </AdminFormDialog>
  </AdminPageShell>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormRules } from 'element-plus'
import AdminPageShell from '@/components/admin/AdminPageShell.vue'
import AdminFilterBar from '@/components/admin/AdminFilterBar.vue'
import AdminDataTable from '@/components/admin/AdminDataTable.vue'
import AdminFormDialog from '@/components/admin/AdminFormDialog.vue'
import { useFormGuard } from '@/composables/useFormGuard'
import { useCrudPage } from '@/composables/useCrudPage'
import { getMarketDataPage, createMarketData, updateMarketData, deleteMarketData } from '@/api/product'
import { formatRate } from '@/utils/format'
import type { WeaMarketData } from '@/types'
import type { AdminFilterField } from '@/components/admin/AdminFilterBar.vue'
import { useMarketSSEStore } from '@/store/marketSSE'

type MarketQuery = {
  pageNum: number
  pageSize: number
  productCode: string
}

const filterFields: AdminFilterField[] = [
  { prop: 'productCode', label: '产品编码', placeholder: '搜索产品编码' },
]

const marketSSE = useMarketSSEStore()
const sseConnected = computed(() => marketSSE.connected)

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
const { loading, saving, tableData, total, dialogVisible, isEdit, handleSearch, handleAdd, handleEdit, handleDialogClose, handleDelete, formatDateColumn, formatPriceColumn } = useCrudPage<WeaMarketData>(fetchData)

const rules: FormRules = {
  productCode: [{ required: true, message: '请输入产品编码', trigger: 'blur' }],
  currentPrice: [{ required: true, message: '请输入当前价', trigger: 'blur' }],
  highestPrice: [{
    validator: (_rule: unknown, value: number, callback: (error?: Error) => void) => {
      const lowest = form.lowestPrice ?? 0
      if (value > 0 && lowest > 0 && value < lowest) callback(new Error('最高价不能低于最低价'))
      else callback()
    },
    trigger: 'blur',
  }],
  lowestPrice: [{
    validator: (_rule: unknown, value: number, callback: (error?: Error) => void) => {
      const highest = form.highestPrice ?? 0
      if (value > 0 && highest > 0 && value > highest) callback(new Error('最低价不能高于最高价'))
      else callback()
    },
    trigger: 'blur',
  }],
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

function handleReset() {
  query.productCode = ''
  handleSearch(query)
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

async function handleSave() {
  saving.value = true
  try {
    if (isEdit.value && form.id) {
      await updateMarketData(form.id, { ...form })
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

function handleMarketUpdate(data: WeaMarketData[]) {
  const dataMap = new Map(data.map((item) => [item.productCode, item]))
  tableData.value = tableData.value.map((item) => {
    const update = dataMap.get(item.productCode)
    return update ? { ...item, ...update } : item
  })
}

onMounted(() => {
  fetchData()
  marketSSE.subscribe(handleMarketUpdate)
})

onUnmounted(() => {
  marketSSE.unsubscribe(handleMarketUpdate)
})
</script>

<style scoped>
</style>
