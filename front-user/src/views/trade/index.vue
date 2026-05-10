<template>
  <div class="trade-page">
    <div class="page-title">交易委托</div>

    <el-row :gutter="20">
      <!-- 下单区 -->
      <el-col :xs="24" :lg="8">
        <el-card class="order-card" shadow="never">
          <template #header>新委托单</template>
          <el-form :model="orderForm" :rules="orderRules" ref="orderFormRef" label-width="80px">
            <el-form-item label="产品代码" prop="productCode">
              <el-input v-model="orderForm.productCode" placeholder="如 GOLD001" />
            </el-form-item>
            <el-form-item label="方向" prop="tradeType">
              <el-radio-group v-model="orderForm.tradeType">
                <el-radio-button :value="1">买入</el-radio-button>
                <el-radio-button :value="2">卖出</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="委托价" prop="entrustPrice">
              <el-input-number
                v-model="orderForm.entrustPrice"
                :min="0.01"
                :precision="2"
                :step="0.1"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="数量" prop="entrustNum">
              <el-input-number
                v-model="orderForm.entrustNum"
                :min="1"
                :step="1"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" :loading="submitting" @click="handleSubmit" style="width:100%">
                {{ submitting ? '提交中...' : '提交委托' }}
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 委托单列表 -->
      <el-col :xs="24" :lg="16">
        <el-card class="order-list-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>我的委托单</span>
              <div class="header-filters">
                <el-select v-model="statusFilter" placeholder="状态" clearable size="small" style="width:110px" @change="fetchOrders">
                  <el-option label="全部" :value="undefined" />
                  <el-option v-for="opt in ORDER_STATUS_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
                </el-select>
                <el-button size="small" :icon="Refresh" @click="fetchOrders" :loading="refreshing" />
              </div>
            </div>
          </template>

          <el-table :data="orders" stripe v-loading="loading" empty-text="暂无委托单">
            <el-table-column type="index" label="#" width="50" />
            <el-table-column prop="orderNo" label="单号" width="160">
              <template #default="{ row }">
                <span class="order-no">{{ row.orderNo }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="productCode" label="产品" width="100" />
            <el-table-column label="方向" width="70">
              <template #default="{ row }">
                <el-tag :type="row.tradeType === 1 ? 'danger' : 'success'" size="small" effect="plain">
                  {{ tradeTypeText(row.tradeType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="委托价" width="100" align="right">
              <template #default="{ row }">{{ formatPrice(row.entrustPrice) }}</template>
            </el-table-column>
            <el-table-column label="数量" width="70" align="right">
              <template #default="{ row }">{{ row.entrustNum }}</template>
            </el-table-column>
            <el-table-column label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="orderStatusTag(row.orderStatus)" size="small">
                  {{ orderStatusText(row.orderStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="时间" width="160">
              <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="80" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="row.orderStatus === 0"
                  text
                  type="danger"
                  size="small"
                  @click="handleCancel(row)"
                >
                  撤销
                </el-button>
                <span v-else class="text-muted">-</span>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrap">
            <el-pagination
              v-if="orderTotal > 0"
              v-model:current-page="orderPageNum"
              v-model:page-size="orderPageSize"
              :total="orderTotal"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              @current-change="fetchOrders"
              @size-change="fetchOrders"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/store/index'
import { getTradeOrderPage, createTradeOrder, cancelTradeOrder } from '@/api/trade'
import { ORDER_STATUS_OPTIONS } from '@/types'
import { formatPrice, formatDateTime, tradeTypeText, orderStatusText, orderStatusTag } from '@/utils/format'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import type { FinTradeOrder } from '@/types'

const route = useRoute()
const userStore = useUserStore()

// 订单表单
const orderFormRef = ref<FormInstance>()
const submitting = ref(false)
const orderForm = reactive({
  productCode: '',
  tradeType: 1,
  entrustPrice: 100,
  entrustNum: 1,
})

const orderRules: FormRules = {
  productCode: [{ required: true, message: '请输入产品代码', trigger: 'blur' }],
  tradeType: [{ required: true, message: '请选择方向', trigger: 'change' }],
  entrustPrice: [{ required: true, message: '请输入委托价', trigger: 'blur' }],
  entrustNum: [{ required: true, message: '请输入数量', trigger: 'blur' }],
}

// 订单列表
const orders = ref<FinTradeOrder[]>([])
const loading = ref(false)
const refreshing = ref(false)
const orderTotal = ref(0)
const orderPageNum = ref(1)
const orderPageSize = ref(10)
const statusFilter = ref<number | undefined>(undefined)

async function handleSubmit() {
  const valid = await orderFormRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!userStore.userId) {
    ElMessage.error('用户信息异常，请重新登录')
    return
  }
  submitting.value = true
  try {
    await createTradeOrder({
      userId: userStore.userId,
      productCode: orderForm.productCode,
      tradeType: orderForm.tradeType,
      entrustPrice: orderForm.entrustPrice,
      entrustNum: orderForm.entrustNum,
    })
    ElMessage.success('委托提交成功')
    orderForm.productCode = ''
    orderForm.tradeType = 1
    orderForm.entrustPrice = 100
    orderForm.entrustNum = 1
    fetchOrders()
  } catch {
    // handled
  } finally {
    submitting.value = false
  }
}

async function fetchOrders() {
  loading.value = true
  try {
    const params: any = {
      pageNum: orderPageNum.value,
      pageSize: orderPageSize.value,
      userId: userStore.userId || undefined,
    }
    if (statusFilter.value !== undefined) params.orderStatus = statusFilter.value
    const res = await getTradeOrderPage(params)
    orders.value = (res.data?.records || []) as FinTradeOrder[]
    orderTotal.value = res.data?.total || 0
  } catch {
    orders.value = []
  } finally {
    loading.value = false
  }
}

async function handleCancel(order: FinTradeOrder) {
  try {
    await ElMessageBox.confirm('确定要撤销该委托单吗？', '确认', { type: 'warning' })
    await cancelTradeOrder(order.id!)
    ElMessage.success('已撤销')
    fetchOrders()
  } catch { /* cancelled */ }
}

onMounted(() => {
  // 从产品中心带过来的产品代码
  if (route.query.productCode) {
    orderForm.productCode = route.query.productCode as string
  }
  if (userStore.userId) fetchOrders()
})
</script>

<style scoped>
.trade-page {
  max-width: 1200px;
}

.order-card {
  margin-bottom: 20px;
}

.order-list-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-filters {
  display: flex;
  align-items: center;
  gap: 8px;
}

.order-no {
  font-family: 'DIN Pro', monospace;
  font-size: 13px;
  color: var(--text-secondary);
}

.text-muted {
  color: var(--text-placeholder);
  font-size: 13px;
}

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
