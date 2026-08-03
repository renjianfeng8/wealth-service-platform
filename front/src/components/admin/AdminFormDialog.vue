<template>
  <el-dialog
    :model-value="modelValue"
    :title="title"
    :width="width"
    :before-close="beforeClose"
    @update:model-value="emit('update:modelValue', $event)"
    @closed="emit('close')"
  >
    <el-form ref="formRef" :model="model" :rules="rules" :label-width="labelWidth">
      <slot />
    </el-form>

    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSubmit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'

const props = withDefaults(defineProps<{
  modelValue: boolean
  title: string
  model: Record<string, any>
  rules?: FormRules
  saving?: boolean
  labelWidth?: string
  width?: string
  beforeClose?: (done: () => void) => void
}>(), {
  rules: () => ({}),
  saving: false,
  labelWidth: '96px',
  width: '560px',
})

const emit = defineEmits<{
  (event: 'update:modelValue', value: boolean): void
  (event: 'submit'): void
  (event: 'close'): void
}>()

const formRef = ref<FormInstance>()

async function handleSubmit() {
  if (props.saving) return
  const valid = await formRef.value?.validate().catch(() => false)
  if (valid) {
    emit('submit')
  }
}
</script>
