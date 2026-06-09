<template>
  <div class="fl-card latest-orders">
    <div class="fl-card-header">
      <div>
        <div class="fl-card-title">近期订单</div>
        <div class="fl-card-subtitle">最新委托与成交状态</div>
      </div>
      <el-button link type="primary" @click="router.push('/admin/trade')">进入交易管理</el-button>
    </div>

    <el-table :data="orders" size="small" stripe empty-text="暂无订单" class="orders-table">
      <el-table-column prop="orderNo" label="订单号" min-width="150" show-overflow-tooltip />
      <el-table-column prop="productCode" label="产品" min-width="100" show-overflow-tooltip />
      <el-table-column prop="tradeType" label="类型" width="78">
        <template #default="{ row }">
          <el-tag :type="row.tradeType === 1 ? 'danger' : 'success'" effect="plain">
            {{ tradeTypeText(row.tradeType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="entrustPrice" label="委托价" width="98" align="right">
        <template #default="{ row }">{{ formatPrice(row.entrustPrice) }}</template>
      </el-table-column>
      <el-table-column prop="entrustNum" label="数量" width="78" align="right" />
      <el-table-column prop="orderStatus" label="状态" width="86">
        <template #default="{ row }">
          <el-tag :type="orderStatusTag(row.orderStatus)" effect="light">
            {{ orderStatusText(row.orderStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="时间" width="150">
        <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import type { WeaTradeOrder } from '@/types'
import { formatDateTime, formatPrice, orderStatusTag, orderStatusText, tradeTypeText } from '@/utils/format'

defineProps<{
  orders: WeaTradeOrder[]
}>()

const router = useRouter()
</script>

<style scoped>
.latest-orders {
  min-width: 0;
}

.orders-table {
  width: 100%;
}

:deep(.el-table__cell) {
  padding: 8px 0;
}

:deep(.el-table .cell) {
  line-height: 1.4;
}
</style>
