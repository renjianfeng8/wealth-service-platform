<template>
  <div class="captcha-field">
    <el-input
      :model-value="modelValue"
      placeholder="请输入验证码"
      size="large"
      :prefix-icon="Key"
      maxlength="4"
      @update:model-value="$emit('update:modelValue', $event)"
    />
    <div
      class="captcha-image"
      title="点击刷新验证码"
      @click="reload"
    >
      <img v-if="captchaImage && !loading" :src="captchaImage" alt="验证码" />
      <el-icon v-else class="captcha-loading" :class="{ 'is-spinning': loading }">
        <Refresh />
      </el-icon>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Key, Refresh } from '@element-plus/icons-vue'
import { getCaptcha } from '@/api/system'

defineOptions({ name: 'CaptchaField' })

const props = defineProps<{ modelValue: string }>()
const emit = defineEmits<{ 'update:modelValue': [value: string] }>()

const captchaKey = ref('')
const captchaImage = ref('')
const loading = ref(false)

/** 拉取验证码：更新 key 与图片，失败时清空图片（后端降级跳过校验，登录不阻塞） */
async function reload() {
  loading.value = true
  try {
    const res = await getCaptcha()
    const data = res || {}
    captchaKey.value = data.captchaKey
    captchaImage.value = data.captchaImage
  } catch {
    captchaKey.value = ''
    captchaImage.value = ''
  } finally {
    loading.value = false
  }
}

/** 供父组件提交时读取当前验证码 KEY */
function getCaptchaKey(): string {
  return captchaKey.value
}

// 挂载即拉取验证码
onMounted(() => {
  reload()
})

defineExpose({ reload, getCaptchaKey })
</script>

<style scoped>
.captcha-field {
  display: flex;
  align-items: center;
  gap: 12px;
}

.captcha-field .el-input {
  flex: 1;
}

.captcha-image {
  flex-shrink: 0;
  width: 130px;
  height: 40px;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid var(--el-border-color);
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.captcha-image img {
  width: 100%;
  height: 100%;
  object-fit: fill;
}

.captcha-loading {
  color: var(--el-text-color-placeholder);
  font-size: 18px;
}

.captcha-loading.is-spinning {
  animation: captcha-spin 0.8s linear infinite;
}

@keyframes captcha-spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
