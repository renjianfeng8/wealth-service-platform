<template>
  <div class="page-loading" role="status" aria-live="polite">
    <el-skeleton animated>
      <template #template>
        <div class="loading-header">
          <el-skeleton-item variant="text" class="loading-title" />
          <el-skeleton-item variant="button" class="loading-action" />
        </div>
        <div class="loading-grid">
          <el-skeleton-item v-for="i in 3" :key="i" variant="rect" class="loading-card" />
        </div>
        <el-skeleton-item variant="rect" class="loading-table" />
      </template>
    </el-skeleton>
  </div>
</template>

<style scoped>
.page-loading {
  width: 100%;
  min-height: 360px;
  padding: 8px 0;
}

.loading-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.loading-title {
  width: 180px;
  height: 26px;
}

.loading-action {
  width: 96px;
  height: 32px;
}

.loading-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 16px;
}

.loading-card {
  height: 108px;
  border-radius: 8px;
}

.loading-table {
  height: 260px;
  border-radius: 8px;
}

@media (max-width: 768px) {
  .loading-grid {
    grid-template-columns: 1fr;
  }
}
</style>
