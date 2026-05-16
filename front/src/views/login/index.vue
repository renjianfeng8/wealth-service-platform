<template>
  <div class="login-container" @mousemove="handleMouseMove">
    <!-- Background bubbles decoration -->
    <div class="bubble" v-for="n in 6" :key="n" :style="bubbleStyle(n)"></div>

    <div class="login-card">
      <!-- Character -->
      <div class="character-wrapper">
        <div class="character" ref="characterRef" :style="characterPulseStyle">
          <!-- Ears -->
          <div class="ear left"></div>
          <div class="ear right"></div>
          <!-- Face -->
          <div class="face" :class="eyeState">
            <!-- Hair -->
            <div class="hair-main"></div>
            <div class="hair-tuft"></div>
            <!-- Eyes -->
            <div class="eyes-row">
              <div class="eye left" ref="leftEyeRef" :style="{ transform: `scaleY(${eyeScale})` }">
                <div class="pupil" :style="leftPupilStyle"></div>
                <div class="eye-highlight"></div>
              </div>
              <div class="eye right" ref="rightEyeRef" :style="{ transform: `scaleY(${eyeScale})` }">
                <div class="pupil" :style="rightPupilStyle"></div>
                <div class="eye-highlight"></div>
              </div>
            </div>
            <!-- Nose -->
            <div class="nose"></div>
            <!-- Mouth -->
            <div class="mouth" :class="eyeState"></div>
            <!-- Cheeks -->
            <div class="cheek left" :class="{ visible: eyeState === 'closed' || eyeState === 'peeking' }"></div>
            <div class="cheek right" :class="{ visible: eyeState === 'closed' || eyeState === 'peeking' }"></div>
          </div>
        </div>
      </div>

      <!-- Title -->
      <h2 class="login-title">理财服务管理后台</h2>
      <p class="login-subtitle">Wealth Service Platform</p>

      <!-- Login Form -->
      <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名"
            :prefix-icon="User"
            @focus="onUserFocus"
            @blur="onUserBlur"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            :type="pwdType"
            placeholder="密码"
            :prefix-icon="Lock"
            @focus="onPwdFocus"
            @blur="onPwdBlur"
          >
            <template #suffix>
              <el-icon class="pwd-toggle" @click="togglePwdType">
                <View v-if="pwdType === 'password'" />
                <Hide v-else />
              </el-icon>
            </template>
          </el-input>
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
import { reactive, ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { User, Lock, View, Hide } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/store/index'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

// ---- Eye tracking state ----
const mouseX = ref(0)
const mouseY = ref(0)
const eyeCenters = reactive({ lx: 0, ly: 0, rx: 0, ry: 0 })
const MAX_PUPIL_OFFSET = 7

const leftEyeRef = ref<HTMLElement>()
const rightEyeRef = ref<HTMLElement>()
const characterRef = ref<HTMLElement>()

// ---- Character expression state ----
// 'normal' | 'curious' | 'peeking' | 'closed'
const eyeState = ref<'normal' | 'curious' | 'peeking' | 'closed'>('normal')
const eyeScale = ref(1)
const pwdType = ref<'password' | 'text'>('password')

let blinkTimer: ReturnType<typeof setTimeout> | null = null

// ---- Mouse tracking ----
function handleMouseMove(e: MouseEvent) {
  mouseX.value = e.clientX
  mouseY.value = e.clientY
}

function updateEyeCenters() {
  if (leftEyeRef.value && rightEyeRef.value) {
    const l = leftEyeRef.value.getBoundingClientRect()
    const r = rightEyeRef.value.getBoundingClientRect()
    eyeCenters.lx = l.left + l.width / 2
    eyeCenters.ly = l.top + l.height / 2
    eyeCenters.rx = r.left + r.width / 2
    eyeCenters.ry = r.top + r.height / 2
  }
}

function calcPupilOffset(cx: number, cy: number) {
  const dx = mouseX.value - cx
  const dy = mouseY.value - cy
  const dist = Math.sqrt(dx * dx + dy * dy)
  if (dist < 1) return { x: 0, y: 0 }
  const clamp = Math.min(dist, MAX_PUPIL_OFFSET)
  return { x: (dx / dist) * clamp, y: (dy / dist) * clamp }
}

function getPupilOverride() {
  switch (eyeState.value) {
    case 'curious': return { x: 3, y: 3 }
    case 'peeking': return { x: 6, y: 2 }
    case 'closed': return { x: 0, y: 0 }
    default: return null
  }
}

const leftPupilStyle = computed(() => {
  const override = getPupilOverride()
  let x: number, y: number
  if (override) {
    x = override.x
    y = override.y
  } else {
    const o = calcPupilOffset(eyeCenters.lx, eyeCenters.ly)
    x = o.x
    y = o.y
  }
  return { transform: `translate(calc(-50% + ${x}px), calc(-50% + ${y}px))` }
})

const rightPupilStyle = computed(() => {
  const override = getPupilOverride()
  let x: number, y: number
  if (override) {
    x = override.x
    y = override.y
  } else {
    const o = calcPupilOffset(eyeCenters.rx, eyeCenters.ry)
    x = o.x
    y = o.y
  }
  return { transform: `translate(calc(-50% + ${x}px), calc(-50% + ${y}px))` }
})

// ---- Blinking ----
function doBlink() {
  if (eyeState.value === 'closed') {
    scheduleNextBlink()
    return
  }
  eyeScale.value = 0.06
  setTimeout(() => {
    eyeScale.value = 1
    scheduleNextBlink()
  }, 160)
}

function scheduleNextBlink() {
  const delay = 2500 + Math.random() * 4000
  blinkTimer = setTimeout(doBlink, delay)
}

// ---- Input interaction ----
function onUserFocus() {
  eyeState.value = 'curious'
}

function onUserBlur() {
  eyeState.value = form.username ? 'curious' : 'normal'
}

function onPwdFocus() {
  eyeState.value = pwdType.value === 'password' ? 'peeking' : 'closed'
}

function onPwdBlur() {
  if (!form.password) {
    eyeState.value = 'normal'
  } else if (pwdType.value === 'text') {
    eyeState.value = 'closed'
  } else {
    eyeState.value = 'peeking'
  }
}

function togglePwdType() {
  pwdType.value = pwdType.value === 'password' ? 'text' : 'password'
  eyeState.value = pwdType.value === 'text' ? 'closed' : 'peeking'
}

// ---- Breathing animation ----
const characterPulseStyle = computed(() => ({
  animation: eyeState.value === 'closed' ? 'none' : undefined,
}))

// ---- Bubble decoration ----
function bubbleStyle(n: number) {
  const size = 20 + n * 15
  const left = 5 + (n * 17) % 90
  const delay = n * 3
  const duration = 8 + n * 2
  return {
    width: `${size}px`,
    height: `${size}px`,
    left: `${left}%`,
    animationDelay: `${delay}s`,
    animationDuration: `${duration}s`,
  }
}

// ---- Login ----
async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userStore.login(form.username, form.password)
    ElMessage.success('登录成功')
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch {
    // error already handled by interceptor
  } finally {
    loading.value = false
  }
}

// ---- Lifecycle ----
onMounted(() => {
  updateEyeCenters()
  window.addEventListener('resize', updateEyeCenters)
  scheduleNextBlink()
})

onUnmounted(() => {
  window.removeEventListener('resize', updateEyeCenters)
  if (blinkTimer) clearTimeout(blinkTimer)
})
</script>

<style scoped>
/* =========================================
   Container & Background
   ========================================= */
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0f1b2d 0%, #1a365d 40%, #2d5a8e 100%);
  position: relative;
  overflow: hidden;
}

/* Floating bubbles */
.bubble {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(circle at 30% 30%, rgba(255, 255, 255, 0.08), rgba(255, 255, 255, 0.02));
  bottom: -10%;
  animation: bubbleFloat linear infinite;
  pointer-events: none;
}

@keyframes bubbleFloat {
  0% { transform: translateY(0) scale(1); opacity: 0.6; }
  50% { opacity: 0.3; }
  100% { transform: translateY(-110vh) scale(0.6); opacity: 0; }
}

/* =========================================
   Login Card
   ========================================= */
.login-card {
  width: 420px;
  padding: 36px 40px 32px;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.25);
  position: relative;
  z-index: 1;
  transition: box-shadow 0.4s ease;
}

.login-card:hover {
  box-shadow: 0 16px 56px rgba(0, 0, 0, 0.3);
}

.login-title {
  text-align: center;
  margin-bottom: 0;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 2px;
  background: linear-gradient(135deg, #409eff, #2d7ad9);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.login-subtitle {
  text-align: center;
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
  margin-bottom: 20px;
  letter-spacing: 1px;
  text-transform: uppercase;
}

/* =========================================
   Character
   ========================================= */
.character-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 8px;
}

.character {
  width: 140px;
  height: 152px;
  position: relative;
  animation: characterBreath 4s ease-in-out infinite;
}

@keyframes characterBreath {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-4px); }
}

/* Ears */
.ear {
  position: absolute;
  top: 18px;
  width: 20px;
  height: 30px;
  background: #f5cba0;
  border-radius: 50%;
  z-index: 0;
}
.ear.left { left: -6px; }
.ear.right { right: -6px; }

/* Face */
.face {
  width: 128px;
  height: 130px;
  background: linear-gradient(180deg, #ffe4c4, #ffd5a8);
  border-radius: 50%;
  position: relative;
  z-index: 1;
  margin: 0 auto;
  box-shadow:
    inset 0 -4px 12px rgba(0, 0, 0, 0.06),
    0 4px 16px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

/* Hair */
.hair-main {
  position: absolute;
  top: -8px;
  left: -10px;
  width: 148px;
  height: 58px;
  background: #4a3728;
  border-radius: 70px 70px 20px 20px;
  z-index: 2;
}

.hair-tuft {
  position: absolute;
  top: -2px;
  left: 50%;
  transform: translateX(-50%);
  width: 36px;
  height: 22px;
  background: #4a3728;
  border-radius: 0 0 18px 18px;
  z-index: 3;
}

/* Eyes */
.eyes-row {
  position: absolute;
  top: 44px;
  left: 0;
  width: 100%;
  display: flex;
  justify-content: center;
  gap: 24px;
  z-index: 4;
}

.eye {
  width: 32px;
  height: 36px;
  background: #fff;
  border-radius: 50%;
  position: relative;
  box-shadow:
    inset 0 2px 4px rgba(0, 0, 0, 0.06),
    0 1px 3px rgba(0, 0, 0, 0.1);
  transition: transform 0.12s ease;
  /* Prevent scale from affecting child pupils */
  transform-origin: center 18px;
}

.eye .pupil {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 12px;
  height: 12px;
  background: #2c3e50;
  border-radius: 50%;
  transition: transform 0.08s ease-out;
  z-index: 5;
}

.eye .pupil::after {
  content: '';
  position: absolute;
  top: 2px;
  right: 2px;
  width: 4px;
  height: 4px;
  background: #fff;
  border-radius: 50%;
}

.eye-highlight {
  position: absolute;
  top: 8px;
  right: 6px;
  width: 6px;
  height: 6px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  z-index: 6;
  pointer-events: none;
}

/* Nose */
.nose {
  position: absolute;
  top: 64px;
  left: 50%;
  transform: translateX(-50%);
  width: 8px;
  height: 6px;
  background: #f0b88a;
  border-radius: 50%;
  z-index: 4;
}

/* Mouth */
.mouth {
  position: absolute;
  bottom: 22px;
  left: 50%;
  transform: translateX(-50%);
  width: 24px;
  height: 10px;
  border-bottom: 3px solid #d4737a;
  border-radius: 0 0 16px 16px;
  z-index: 4;
  transition: all 0.3s ease;
}

.mouth.curious {
  width: 16px;
  height: 14px;
  border: 2.5px solid #d4737a;
  border-radius: 50%;
  border-bottom-width: 2.5px;
  background: transparent;
}

.mouth.peeking {
  width: 26px;
  height: 8px;
  border-bottom: 3px solid #d4737a;
  border-radius: 0 0 16px 0;
  border-right: 3px solid #d4737a;
  transform: translateX(-40%) rotate(-4deg);
}

.mouth.closed {
  width: 18px;
  height: 3px;
  border-bottom: none;
  background: #d4737a;
  border-radius: 0;
  bottom: 26px;
}

/* Cheeks */
.cheek {
  position: absolute;
  width: 18px;
  height: 12px;
  background: rgba(255, 150, 150, 0.25);
  border-radius: 50%;
  top: 68px;
  z-index: 3;
  opacity: 0;
  transition: opacity 0.4s ease;
}
.cheek.left { left: 12px; }
.cheek.right { right: 12px; }
.cheek.visible { opacity: 1; }

/* =========================================
   Form Overrides
   ========================================= */
.login-card :deep(.el-form-item) {
  margin-bottom: 20px;
}

.login-card :deep(.el-input__wrapper) {
  border-radius: 10px;
  padding: 2px 14px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  transition: box-shadow 0.25s ease;
}

.login-card :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #409eff inset;
}

.login-card :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.3) inset;
}

.login-card :deep(.el-input__inner) {
  height: 40px;
  font-size: 14px;
}

/* Password toggle icon */
.pwd-toggle {
  font-size: 16px;
  color: #909399;
  cursor: pointer;
  transition: color 0.2s ease;
}

.pwd-toggle:hover {
  color: #409eff;
}

/* Login button */
.login-btn {
  width: 100%;
  height: 42px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 4px;
  border-radius: 10px;
  background: linear-gradient(135deg, #409eff, #2d7ad9);
  border: none;
  transition: all 0.3s ease;
}

.login-btn:hover {
  background: linear-gradient(135deg, #6ab0ff, #409eff);
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.4);
  transform: translateY(-1px);
}

.login-btn:active {
  transform: translateY(0);
}
</style>
