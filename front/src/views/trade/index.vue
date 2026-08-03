<template>
  <div class="trade-page">
    <div class="page-title">交易委托</div>

    <!-- 下单确认弹窗 -->
    <el-dialog v-model="confirmVisible" title="确认下单" width="420" destroy-on-close>
      <div class="confirm-body">
        <div class="confirm-row">
          <span class="confirm-label">交易类型</span>
          <el-tag :type="confirmData.tradeType === 1 ? 'danger' : 'success'" size="small" effect="plain">
            {{ confirmData.tradeType === 1 ? '买入' : '卖出' }}
          </el-tag>
        </div>
        <div class="confirm-row">
          <span class="confirm-label">产品代码</span>
          <span class="confirm-value">{{ confirmData.productCode }}</span>
        </div>
        <div class="confirm-row">
          <span class="confirm-label">委托价格</span>
          <span class="confirm-value price">{{ formatPrice(confirmData.entrustPrice) }}</span>
        </div>
        <div class="confirm-row">
          <span class="confirm-label">委托数量</span>
          <span class="confirm-value">{{ confirmData.entrustNum }}</span>
        </div>
        <el-divider />
        <div class="confirm-row total-row">
          <span class="confirm-label">委托金额</span>
          <span class="confirm-value total">{{ (confirmData.entrustPrice * confirmData.entrustNum).toFixed(2) }}</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="confirmVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="doSubmit">确定下单</el-button>
      </template>
    </el-dialog>

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
                :max="999999.99"
                :precision="2"
                :step="0.1"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="数量" prop="entrustNum">
              <el-input-number
                v-model="orderForm.entrustNum"
                :min="1"
                :max="1000000"
                :step="1"
                controls-position="right"
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
            <el-table-column label="操作" width="130" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="row.orderStatus === 1"
                  text
                  type="danger"
                  size="small"
                  @click="handleCancel(row)"
                >
                  撤销
                </el-button>
                <el-button text type="primary" size="small" @click="handleDetail(row)">详情</el-button>
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

    <!-- 委托单详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="`委托单 ${detailItem?.orderNo || ''}`" width="480" destroy-on-close>
      <div v-loading="detailLoading" class="order-detail-body">
        <template v-if="detailItem">
          <div class="od-row">
            <span class="od-label">订单号</span>
            <span class="od-value code">{{ detailItem.orderNo }}</span>
          </div>
          <div class="od-row">
            <span class="od-label">产品代码</span>
            <span class="od-value code">{{ detailItem.productCode }}</span>
          </div>
          <div class="od-row">
            <span class="od-label">交易方向</span>
            <el-tag :type="detailItem.tradeType === 1 ? 'danger' : 'success'" size="small" effect="plain">
              {{ tradeTypeText(detailItem.tradeType) }}
            </el-tag>
          </div>
          <div class="od-row">
            <span class="od-label">委托价格</span>
            <span class="od-value price">{{ formatPrice(detailItem.entrustPrice) }}</span>
          </div>
          <div class="od-row">
            <span class="od-label">委托数量</span>
            <span class="od-value">{{ detailItem.entrustNum }}</span>
          </div>
          <div class="od-row">
            <span class="od-label">委托金额</span>
            <span class="od-value price">
              {{ detailItem.entrustPrice != null && detailItem.entrustNum != null
                ? formatPrice(detailItem.entrustPrice * detailItem.entrustNum)
                : '-' }}
            </span>
          </div>
          <div class="od-row">
            <span class="od-label">订单状态</span>
            <el-tag :type="orderStatusTag(detailItem.orderStatus)" size="small">
              {{ orderStatusText(detailItem.orderStatus) }}
            </el-tag>
          </div>
          <div class="od-row">
            <span class="od-label">下单时间</span>
            <span class="od-value">{{ formatDateTime(detailItem.createTime) }}</span>
          </div>
        </template>
        <el-empty v-else-if="!detailLoading" description="订单不存在" :image-size="64" />
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/store/index'
import { getTradeOrderPage, createTradeOrder, getTradeOrderById, cancelTradeOrder } from '@/api/trade'
import { ORDER_STATUS_OPTIONS } from '@/types'
import { formatPrice, formatDateTime, tradeTypeText, orderStatusText, orderStatusTag } from '@/utils/format'
import { randomUUID } from '@/utils/uuid'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import type { WeaTradeOrder } from '@/types'

const route = useRoute()
const userStore = useUserStore()

// 订单表单
const orderFormRef = ref<FormInstance>()
const submitting = ref(false)
const confirmVisible = ref(false)
// B6: 基于业务参数的幂等键缓存 — key 由最终提交参数确定，参数变更自动重算
let lastOrderHash = ''
let currentIdempotentKey = ''

/** B6: 根据订单参数生成稳定哈希，相同参数返回相同 key，参数变更自动生成新 key */
function resolveIdempotentKey(): string {
  const hash = [
    userStore.userId,
    orderForm.productCode,
    orderForm.tradeType,
    orderForm.entrustPrice,
    orderForm.entrustNum,
  ].join('|')
  if (hash !== lastOrderHash || !currentIdempotentKey) {
    currentIdempotentKey = randomUUID()
    lastOrderHash = hash
  }
  return currentIdempotentKey
}
const confirmData = reactive({
  productCode: '',
  tradeType: 1,
  entrustPrice: 0,
  entrustNum: 0,
})
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
const orders = ref<WeaTradeOrder[]>([])
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
  // B6: resolveIdempotentKey() 在弹窗打开前预计算一次，确保 key 基于当前参数
  // 用户修改参数再提交时，hash 变化自动触发新 key 生成
  resolveIdempotentKey()
  // 打开下单确认弹窗
  confirmData.productCode = orderForm.productCode
  confirmData.tradeType = orderForm.tradeType
  confirmData.entrustPrice = orderForm.entrustPrice
  confirmData.entrustNum = orderForm.entrustNum
  confirmVisible.value = true
}

async function doSubmit() {
  confirmVisible.value = false
  submitting.value = true
  try {
    await createTradeOrder({
      userId: userStore.userId,
      productCode: orderForm.productCode,
      tradeType: orderForm.tradeType,
      entrustPrice: orderForm.entrustPrice,
      entrustNum: orderForm.entrustNum,
      idempotentKey: resolveIdempotentKey(), // B6: 实时计算 key，参数变更自动重算
    })
    ElMessage.success('委托提交成功')
    lastOrderHash = '' // B6: 清除哈希缓存，下次全新提交
    currentIdempotentKey = ''
    orderForm.productCode = ''
    orderForm.tradeType = 1
    orderForm.entrustPrice = 100
    orderForm.entrustNum = 1
    fetchOrders()
  } catch {
    // handled — currentIdempotentKey 保留，允许重试复用
  } finally {
    submitting.value = false
  }
}

async function fetchOrders() {
  loading.value = true
  try {
    const params: { pageNum: number; pageSize: number; userId?: number; orderStatus?: number } = {
      pageNum: orderPageNum.value,
      pageSize: orderPageSize.value,
      userId: userStore.userId || undefined,
    }
    if (statusFilter.value !== undefined) params.orderStatus = statusFilter.value
    const res = await getTradeOrderPage(params)
    orders.value = (res.data?.records || []) as WeaTradeOrder[]
    orderTotal.value = res.data?.total || 0
  } catch (err) {
    console.warn('[trade] fetchOrders 失败:', err)
    orders.value = []
  } finally {
    loading.value = false
  }
}

async function handleCancel(order: WeaTradeOrder) {
  try {
    await ElMessageBox.confirm('确定要撤销该委托单吗？', '确认', { type: 'warning' })
  } catch {
    return // 用户取消确认框
  }
  try {
    await cancelTradeOrder(order.id!)
    ElMessage.success('已撤销')
  } catch {
    ElMessage.error('撤销失败，请稍后重试')
  } finally {
    fetchOrders()
  }
}

const detailVisible = ref(false)
const detailLoading = ref(false)
const detailItem = ref<WeaTradeOrder | null>(null)

async function handleDetail(order: WeaTradeOrder) {
  if (!order.id) return
  detailVisible.value = true
  detailLoading.value = true
  detailItem.value = null
  try {
    const res = await getTradeOrderById(order.id)
    detailItem.value = (res.data || null) as WeaTradeOrder | null
  } catch {
    detailItem.value = null
  } finally {
    detailLoading.value = false
  }
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

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

/* 确认弹窗 */
.confirm-body {
  padding: 8px 0;
}
.confirm-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0;
}
.confirm-label {
  font-size: 14px;
  color: var(--el-text-color-secondary);
}
.confirm-value {
  font-size: 14px;
  color: var(--el-text-color-primary);
  font-weight: 500;
}
.confirm-value.price {
  font-family: 'DIN Pro', monospace;
  font-size: 15px;
}
.total-row {
  padding: 4px 0;
}
.total-row .confirm-label {
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}
.confirm-value.total {
  font-size: 18px;
  font-weight: 700;
  color: var(--el-color-danger);
}

/* 委托单详情弹窗 */
.order-detail-body { min-height: 60px; padding: 4px 0; }
.od-row {
  display: flex;
  align-items: center;
  padding: 11px 0;
  border-bottom: 1px solid var(--border-color);
}
.od-row:last-child { border-bottom: none; }
.od-label { width: 90px; font-size: 14px; color: var(--text-secondary); flex-shrink: 0; }
.od-value { font-size: 14px; color: var(--text-primary); font-weight: 500; }
.od-value.code { font-family: 'DIN Pro', monospace; }
.od-value.price { font-weight: 700; font-family: 'DIN Pro', monospace; }

@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
