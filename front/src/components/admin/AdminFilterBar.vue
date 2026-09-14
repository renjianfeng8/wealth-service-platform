<template>
  <section class="admin-filter-bar">
    <el-form :model="model" inline @submit.prevent="emit('search')">
      <el-form-item v-for="field in fields" :key="field.prop" :label="field.label">
        <el-select
          v-if="field.type === 'select'"
          v-model="model[field.prop]"
          :placeholder="field.placeholder || '请选择'"
          :style="{ width: field.width || '148px' }"
          clearable
        >
          <el-option
            v-for="option in field.options || []"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
        <el-input-number
          v-else-if="field.type === 'number'"
          v-model="model[field.prop]"
          :placeholder="field.placeholder || '请输入'"
          :style="{ width: field.width || '148px' }"
          :min="field.min"
          :precision="field.precision"
          controls-position="right"
        />
        <el-input
          v-else
          v-model="model[field.prop]"
          :placeholder="field.placeholder || '请输入'"
          :style="{ width: field.width || '180px' }"
          clearable
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="emit('search')">查询</el-button>
        <el-button @click="emit('reset')">重置</el-button>
      </el-form-item>
    </el-form>
  </section>
</template>

<script setup lang="ts">
import type { DictItem } from '@/types'

export interface AdminFilterField {
  prop: string
  label: string
  type?: 'input' | 'select' | 'number'
  placeholder?: string
  width?: string
  options?: DictItem[]
  min?: number
  precision?: number
}

defineProps<{
  model: Record<string, any>
  fields: AdminFilterField[]
}>()

const emit = defineEmits<{
  (event: 'search'): void
  (event: 'reset'): void
}>()
</script>

<style scoped>
.admin-filter-bar {
  /* 底部 6px + 末行 .el-form-item 的 14px = 20px，与其余三边留白齐平 */
  padding: 20px 24px 6px;
  background: var(--fl-card-bg);
  border: 1px solid var(--fl-border);
  border-radius: var(--fl-radius);
}

.admin-filter-bar :deep(.el-form-item) {
  margin-bottom: 14px;
}
</style>
