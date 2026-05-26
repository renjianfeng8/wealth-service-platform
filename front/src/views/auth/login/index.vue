<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="bg-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
      </div>
    </div>

    <div class="login-container">
      <div class="login-brand">
        <div class="brand-icon">
          <el-icon :size="56" color="rgba(255,255,255,0.9)"><TrendCharts /></el-icon>
        </div>
        <h1 class="brand-title">理财服务平台</h1>
        <p class="brand-desc">智慧投资 · 稳健增值</p>
      </div>

      <el-card class="login-card" :shadow="'never'">
        <h2 class="login-title">用户登录</h2>
        <p class="login-subtitle">欢迎回来，请登录您的账户</p>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          class="login-form"
          @keyup.enter="handleLogin"
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

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              class="login-btn"
              @click="handleLogin"
            >
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-footer">
          <span class="login-hint">还没有账户？</span>
          <router-link to="/register" class="register-link">立即注册</router-link>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/index'
import { ElMessage } from 'element-plus'
import { User, Lock, TrendCharts } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { setToken, setStoredUser } from '@/utils/auth'
import { userLogin as userLoginApi } from '@/api/user'
import { loginApi } from '@/api/system'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    // 先尝试用户登录
    try {
      const res = await userLoginApi({ username: form.username, password: form.password })
      if (res.data) {
        // 用户登录成功
        const { userId } = res.data
        const nickname = res.data.nickname || ''
        const avatar = res.data.avatar || ''
        sessionStorage.setItem('wealth_logged_in', 'true')
        sessionStorage.setItem('wealth_role', 'user')
        setStoredUser({ username: form.username, userId, nickname, avatar })
        userStore.setUserInfo({ userId, nickname, avatar })
        userStore.token = 'true'
        userStore.username = form.username
        userStore.role = 'user'
        ElMessage.success('登录成功')
        const redirect = (route.query.redirect as string) || '/user/dashboard'
        router.push(redirect)
        return
      }
    } catch {
      // 用户登录失败，继续尝试管理员登录
    }

    // 再尝试管理员登录
    try {
      await loginApi({ username: form.username, password: form.password })
      sessionStorage.setItem('wealth_logged_in', 'true')
      sessionStorage.setItem('wealth_role', 'admin')
      setStoredUser({ username: form.username })
      userStore.token = 'true'
      userStore.username = form.username
      userStore.userId = 0
      userStore.nickname = ''
      userStore.avatar = ''
      userStore.role = 'admin'
      ElMessage.success('登录成功')
      const adminRedirect = (route.query.redirect as string) || '/admin/dashboard'
      router.push(adminRedirect)
    } catch {
      // 两种登录都失败，错误已由拦截器展示
    }
  } finally {
    loading.value = false
  }
}

// ---- URL token auto-login (来自统一登录跳转，仅处理一次) ----
onMounted(() => {
  const params = new URLSearchParams(window.location.search)
  const token = params.get('token')
  if (token && !userStore.isLoggedIn) {
    setToken(token)
    sessionStorage.setItem('wealth_role', 'user')
    setStoredUser({ username: '用户' })
    window.history.replaceState({}, '', window.location.pathname)
    ElMessage.success('登录成功')
    router.push('/user/dashboard')
    return
  }

  // 无 token 直接访问 → 展示登录页面等待用户操作
})
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  background: linear-gradient(135deg, #0f0c29 0%, #1a1a4e 50%, #24243e 100%);
  overflow: hidden;
}

.login-bg {
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

.login-container {
  display: flex;
  align-items: center;
  gap: 60px;
  z-index: 1;
  padding: 24px;
}

.login-brand {
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

.login-card {
  width: 400px;
  padding: 8px 0;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: var(--radius-lg) !important;
}

.login-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  text-align: center;
  margin-bottom: 4px;
}

.login-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  text-align: center;
  margin-bottom: 28px;
}

.login-form {
  padding: 0 8px;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 4px;
  margin-top: 4px;
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
}

.login-hint {
  color: var(--text-secondary);
}

.register-link {
  color: var(--primary);
  font-weight: 500;
  margin-left: 4px;
}

@media (max-width: 800px) {
  .login-container {
    flex-direction: column;
    gap: 30px;
  }
  .login-brand {
    display: none;
  }
  .login-card {
    width: 100%;
    max-width: 400px;
  }
}
</style>
