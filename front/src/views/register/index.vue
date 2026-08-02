<template>
  <div class="register-page">
    <div class="register-bg">
      <div class="bg-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
      </div>
    </div>

    <div class="register-container">
      <div class="register-brand">
        <div class="brand-icon">
          <el-icon :size="56" color="rgba(255,255,255,0.9)"><TrendCharts /></el-icon>
        </div>
        <h1 class="brand-title">理财服务平台</h1>
        <p class="brand-desc">智慧投资 · 稳健增值</p>
      </div>

      <el-card class="register-card" :shadow="'never'">
        <h2 class="register-title">用户注册</h2>
        <p class="register-subtitle">创建您的账户，开启投资之旅</p>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          class="register-form"
          @keyup.enter="handleRegister"
        >
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              :prefix-icon="User"
              size="large"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              size="large"
              show-password
            />
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请确认密码"
              :prefix-icon="Lock"
              size="large"
              show-password
            />
          </el-form-item>

          <el-form-item prop="captchaCode">
            <CaptchaField ref="captchaRef" v-model="form.captchaCode" />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              class="register-btn"
              @click="handleRegister"
            >
              {{ loading ? '注册中...' : '注 册' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="register-footer">
          <span class="register-hint">已有账户？</span>
          <router-link to="/auth/login" class="login-link">立即登录</router-link>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter, onBeforeRouteLeave } from 'vue-router'
import { registerUser } from '@/api/user'
import { User, Lock, TrendCharts } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useFormGuard } from '@/composables/useFormGuard'
import CaptchaField from '@/components/CaptchaField.vue'

interface CaptchaFieldExpose {
  reload: () => void
  getCaptchaKey: () => string
}

// B1: 为 KeepAlive exclude 提供组件名
defineOptions({ name: 'RegisterPage' })

const router = useRouter()

const formRef = ref<FormInstance>()
const captchaRef = ref<CaptchaFieldExpose>()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  captchaCode: '',
})

const { isDirty, reset } = useFormGuard(form)

const handleBeforeUnload = (e: BeforeUnloadEvent) => {
  if (isDirty()) {
    e.preventDefault()
    e.returnValue = ''
  }
}

onBeforeRouteLeave((to, from, next) => {
  if (!isDirty()) return next()
  ElMessageBox.confirm('有未保存的更改，确定离开吗？', '离开确认', {
    confirmButtonText: '离开',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => next()).catch(() => next(false))
})

onMounted(() => {
  window.addEventListener('beforeunload', handleBeforeUnload)
})

onUnmounted(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload)
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (value !== form.password) callback(new Error('两次密码不一致'))
        else callback()
      },
      trigger: 'blur',
    },
  ],
}

async function handleRegister() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await registerUser({
      username: form.username,
      password: form.password,
      captchaKey: captchaRef.value?.getCaptchaKey() || '',
      captchaCode: form.captchaCode,
    })
    ElMessage.success('注册成功，请登录')
    reset()
    router.push('/auth/login')
  } catch {
    // error already handled by interceptor；注册失败刷新验证码
    captchaRef.value?.reload()
    form.captchaCode = ''
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  background: linear-gradient(135deg, #0f0c29 0%, #1a1a4e 50%, #24243e 100%);
  overflow: hidden;
}

.register-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.bg-shapes {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.08;
}

.shape-1 {
  width: 400px;
  height: 400px;
  background: var(--primary);
  top: -100px;
  right: -100px;
  animation: float 8s ease-in-out infinite;
}

.shape-2 {
  width: 300px;
  height: 300px;
  background: #34c759;
  bottom: -80px;
  left: -80px;
  animation: float 10s ease-in-out infinite reverse;
}

.shape-3 {
  width: 200px;
  height: 200px;
  background: #ff9500;
  top: 40%;
  left: 10%;
  animation: float 12s ease-in-out infinite 2s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(30px, -30px) scale(1.05); }
}

.register-container {
  display: flex;
  align-items: center;
  gap: 60px;
  z-index: 1;
  padding: 24px;
}

.register-brand {
  color: #fff;
  text-align: center;
}

.brand-icon {
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 16px;
}

.brand-title {
  font-size: 36px;
  font-weight: 800;
  margin-bottom: 8px;
  letter-spacing: 2px;
}

.brand-desc {
  font-size: 16px;
  opacity: 0.6;
  letter-spacing: 4px;
}

.register-card {
  width: 400px;
  padding: 8px 0;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: var(--radius-lg) !important;
}

.register-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  text-align: center;
  margin-bottom: 4px;
}

.register-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  text-align: center;
  margin-bottom: 28px;
}

.register-form {
  padding: 0 8px;
}

.register-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 4px;
  margin-top: 4px;
}

.register-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
}

.register-hint {
  color: var(--text-secondary);
}

.login-link {
  color: var(--primary);
  font-weight: 500;
  margin-left: 4px;
}

@media (max-width: 800px) {
  .register-container {
    flex-direction: column;
    gap: 30px;
  }
  .register-brand {
    display: none;
  }
  .register-card {
    width: 100%;
    max-width: 400px;
  }
}
</style>
