<template>
  <section class="admin-data-table">
    <div v-if="$slots.toolbar" class="admin-data-table__toolbar">
      <slot name="toolbar" />
    </div>

    <el-table
      :data="data"
      stripe
      border
      v-loading="loading"
      @selection-change="handleSelectionChange"
    >
      <el-table-column v-if="selectable" type="selection" width="55" />
      <slot />
      <template #empty>
        <el-empty :description="emptyText" :image-size="50" />
      </template>
    </el-table>

    <div class="admin-data-table__pagination">
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="total"
        :page-sizes="pageSizes"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </section>
</template>

<script setup lang="ts">
const props = withDefaults(defineProps<{
  data: any[]
  loading?: boolean
  total: number
  pagination: {
    pageNum: number
    pageSize: number
  }
  pageSizes?: number[]
  emptyText?: string
  /** 开启后渲染首列勾选框并透传 selection-change */
  selectable?: boolean
}>(), {
  loading: false,
  pageSizes: () => [10, 20, 50],
  emptyText: '暂无数据',
  selectable: false,
})

const emit = defineEmits<{
  (event: 'page-change'): void
  (event: 'selection-change', rows: any[]): void
}>()

function handleSelectionChange(rows: any[]) {
  emit('selection-change', rows)
}

function handleSizeChange() {
  props.pagination.pageNum = 1
  emit('page-change')
}

function handleCurrentChange() {
  emit('page-change')
}
</script>

<style scoped>
.admin-data-table {
  padding: 20px 24px;
  background: var(--fl-card-bg);
  border: 1px solid var(--fl-border);
  border-radius: var(--fl-radius);
}

.admin-data-table__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.admin-data-table__pagination {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
}

/* el-table 的 .el-table__empty-text 带 line-height:60px，会被空态子树继承 */
.admin-data-table :deep(.el-table__empty-text) {
  line-height: normal;
}

.admin-data-table :deep(.el-empty) {
  --el-empty-padding: 16px 0;
  --el-empty-description-margin-top: 8px;
}
</style>
