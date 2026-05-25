<template>
  <div class="login-page">
    <div class="login-card">
      <h2 class="title">登录</h2>
      <p class="subtitle">理财服务平台</p>
      <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" class="login-btn" @click="handleLogin">
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { identifyLogin } from '@/api/user'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await identifyLogin({ username: form.username, password: form.password })
    const { token, userType, nickname } = res.data
    userStore.setLoginInfo({
      token,
      username: form.username,
      userType,
      nickname: nickname || undefined,
    })
    ElMessage.success('登录成功')
    // 整页跳转到对应 SPA
    const redirectUrl = userType === 'admin' ? '/admin/#/' : '/user/#/'
    window.location.href = redirectUrl
  } catch {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0f1b2d, #1a365d 40%, #2d5a8e 100%);
}
.login-card {
  width: 400px;
  padding: 36px 40px 32px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 12px 48px rgba(0,0,0,0.25);
}
.title { text-align: center; font-size: 24px; font-weight: 700; margin-bottom: 4px; }
.subtitle { text-align: center; font-size: 13px; color: #909399; margin-bottom: 24px; }
.login-btn { width: 100%; height: 42px; font-size: 15px; letter-spacing: 4px; }
</style>
