<template>
  <div class="profile-page">
    <el-row :gutter="24">
      <!-- ==================== 左侧：头像卡片 ==================== -->
      <el-col :xs="24" :lg="7">
        <el-card shadow="never" class="profile-avatar-card">
          <el-skeleton :loading="loading" animated>
            <template #default>
              <div class="avatar-section">
                <div
                  class="avatar-wrap"
                  @mouseenter="avatarHover = true"
                  @mouseleave="avatarHover = false"
                >
                  <el-avatar :size="96" :src="userStore.avatar" class="profile-avatar" />
                  <div v-show="avatarHover" class="avatar-overlay" @click="triggerUpload">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z" />
                      <circle cx="12" cy="13" r="4" />
                    </svg>
                    <span>更换头像</span>
                  </div>
                  <input
                    ref="fileInputRef"
                    type="file"
                    accept="image/*"
                    class="file-input-hidden"
                    @change="handleFileSelected"
                  />
                </div>

                <h2 class="profile-name">{{ adminInfo.nickName || adminInfo.username }}</h2>
                <p class="profile-username">@{{ adminInfo.username }}</p>

                <div class="profile-tags">
                  <el-tag type="primary" effect="plain" size="small">管理员</el-tag>
                  <el-tag
                    :type="adminStatus === 1 ? 'success' : 'danger'"
                    effect="plain"
                    size="small"
                  >
                    {{ adminStatus === 1 ? '正常' : '禁用' }}
                  </el-tag>
                </div>
              </div>

              <el-divider />

              <div class="profile-meta">
                <div class="meta-row">
                  <div class="meta-row-label">注册时间</div>
                  <div class="meta-row-value">{{ createTime || '-' }}</div>
                </div>
                <div class="meta-row">
                  <div class="meta-row-label">上次更新</div>
                  <div class="meta-row-value">{{ lastUpdateTime || '-' }}</div>
                </div>
              </div>
            </template>
          </el-skeleton>
        </el-card>
      </el-col>

      <!-- ==================== 右侧：Tab 面板 ==================== -->
      <el-col :xs="24" :lg="17">
        <el-card shadow="never" class="profile-form-card">
          <el-tabs v-model="activeTab" class="profile-tabs">
            <!-- ---- Tab 1: 基本信息 ---- -->
            <el-tab-pane label="基本信息" name="basic">
              <el-skeleton :loading="loading" animated>
                <template #default>
                  <el-form
                    ref="basicFormRef"
                    :model="adminInfo"
                    label-position="top"
                    class="profile-form"
                  >
                    <el-form-item label="用户名" prop="username">
                      <el-input v-model="adminInfo.username" disabled>
                        <template #prefix>
                          <el-icon><User /></el-icon>
                        </template>
                      </el-input>
                    </el-form-item>
                    <el-form-item label="昵称" prop="nickName">
                      <el-input v-model="adminInfo.nickName" placeholder="设置显示名称" maxlength="20" show-word-limit>
                        <template #prefix>
                          <el-icon><EditPen /></el-icon>
                        </template>
                      </el-input>
                    </el-form-item>
                    <el-form-item label="邮箱" prop="email">
                      <el-input v-model="adminInfo.email" placeholder="设置联系邮箱" maxlength="50">
                        <template #prefix>
                          <el-icon><Message /></el-icon>
                        </template>
                      </el-input>
                    </el-form-item>
                    <el-form-item>
                      <el-button type="primary" :loading="saving" @click="handleSave">
                        {{ saving ? '保存中...' : '保存修改' }}
                      </el-button>
                    </el-form-item>
                  </el-form>
                </template>
              </el-skeleton>
            </el-tab-pane>

            <!-- ---- Tab 2: 安全设置 ---- -->
            <el-tab-pane label="安全设置" name="security">
              <el-skeleton :loading="loading" animated>
                <template #default>
                  <div class="security-section">
                    <div class="section-title">
                      <el-icon><Lock /></el-icon>
                      <span>修改密码</span>
                    </div>
                    <p class="section-desc">建议定期更换密码以保障账户安全</p>

                    <el-form
                      ref="pwdFormRef"
                      :model="passwordForm"
                      :rules="passwordRules"
                      label-position="top"
                      class="pwd-form"
                    >
                      <el-form-item label="当前密码" prop="oldPassword">
                        <el-input
                          v-model="passwordForm.oldPassword"
                          type="password"
                          show-password
                          placeholder="请输入当前密码"
                        />
                      </el-form-item>
                      <el-form-item label="新密码" prop="password">
                        <el-input
                          v-model="passwordForm.password"
                          type="password"
                          show-password
                          placeholder="至少 6 位字符"
                          @input="onPwdInput"
                        />
                        <!-- 密码强度 -->
                        <div class="pwd-strength" v-if="passwordForm.password">
                          <div class="strength-bar">
                            <div
                              :class="['strength-fill', strengthClass]"
                              :style="{ width: strengthPercent + '%' }"
                            />
                          </div>
                          <span class="strength-text" :class="strengthClass">
                            {{ strengthLabel }}
                          </span>
                        </div>
                      </el-form-item>
                      <el-form-item label="确认密码" prop="confirmPassword">
                        <el-input
                          v-model="passwordForm.confirmPassword"
                          type="password"
                          show-password
                          placeholder="请再次输入新密码"
                        />
                      </el-form-item>
                      <el-form-item>
                        <el-button
                          type="primary"
                          :loading="resetting"
                          @click="handleResetPassword"
                        >
                          {{ resetting ? '修改中...' : '确认修改' }}
                        </el-button>
                      </el-form-item>
                    </el-form>
                  </div>
                </template>
              </el-skeleton>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useUserStore, DEFAULT_AVATAR } from '@/store/index'
import { getAdminById, updateAdmin, resetAdminPassword } from '@/api/system'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useFormGuard } from '@/composables/useFormGuard'
import { onBeforeRouteLeave } from 'vue-router'
import { User, EditPen, Message, Lock } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const userStore = useUserStore()

/* ---- State ---- */
const loading = ref(true)
const saving = ref(false)
const resetting = ref(false)
const activeTab = ref('basic')
const avatarHover = ref(false)
const fileInputRef = ref<HTMLInputElement>()

const adminInfo = reactive({
  username: '',
  nickName: '',
  email: '',
})
const adminStatus = ref(1)
const createTime = ref('')
const lastUpdateTime = ref('')

let fetchProfilePromise: Promise<void> | null = null

/* ---- Password form ---- */
const pwdFormRef = ref<FormInstance>()
const passwordForm = reactive({
  oldPassword: '',
  password: '',
  confirmPassword: '',
})

const passwordRules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (value !== passwordForm.password) callback(new Error('两次密码不一致'))
        else callback()
      },
      trigger: 'blur',
    },
  ],
}

/* ---- Password strength ---- */
const strengthPercent = computed(() => {
  const v = passwordForm.password
  if (!v) return 0
  if (v.length < 6) return 25
  if (v.length < 10) return 55
  const hasMixed = /[a-z]/.test(v) && /[A-Z]/.test(v)
  const hasNumber = /\d/.test(v)
  const hasSpecial = /[^a-zA-Z\d]/.test(v)
  const score = (hasMixed ? 1 : 0) + (hasNumber ? 1 : 0) + (hasSpecial ? 1 : 0)
  if (score >= 2) return 100
  return 70
})

const strengthClass = computed(() => {
  const pct = strengthPercent.value
  if (pct >= 100) return 'strong'
  if (pct >= 55) return 'medium'
  return 'weak'
})

const strengthLabel = computed(() => {
  const map: Record<string, string> = { weak: '弱', medium: '中', strong: '强' }
  return map[strengthClass.value] || ''
})

function onPwdInput() {
  // reactivity only
}

/* ---- B7: form guard ---- */
const { isDirty, reset: resetGuard } = useFormGuard(adminInfo)

/* ---- Fetch profile ---- */
async function fetchProfile() {
  if (!userStore.userId) return
  if (fetchProfilePromise) return fetchProfilePromise
  adminInfo.username = userStore.username
  fetchProfilePromise = (async () => {
    try {
      const res = await getAdminById(userStore.userId)
      const data = res.data
      if (data) {
        adminInfo.username = data.username || userStore.username
        adminInfo.nickName = data.nickName || ''
        adminInfo.email = data.email || ''
        adminStatus.value = data.status ?? 1
        createTime.value = data.createTime ? dayjs(data.createTime).format('YYYY-MM-DD HH:mm') : ''
        lastUpdateTime.value = data.updateTime ? dayjs(data.updateTime).format('YYYY-MM-DD HH:mm') : ''
        if (data.avatar) {
          userStore.setUserInfo({ userId: userStore.userId, nickname: adminInfo.nickName, avatar: data.avatar })
        }
      }
      resetGuard()
    } catch {
      // use store defaults
    } finally {
      loading.value = false
      fetchProfilePromise = null
    }
  })()
  return fetchProfilePromise
}

/* ---- Save basic info ---- */
async function handleSave() {
  if (!userStore.userId) return
  saving.value = true
  try {
    await updateAdmin(userStore.userId, {
      nickName: adminInfo.nickName,
      email: adminInfo.email,
    })
    userStore.setUserInfo({
      userId: userStore.userId,
      nickname: adminInfo.nickName || '',
      avatar: userStore.avatar,
    })
    resetGuard()
    ElMessage.success('保存成功')
  } catch {
    // handled
  } finally {
    saving.value = false
  }
}

/* ---- Reset password ---- */
async function handleResetPassword() {
  const valid = await pwdFormRef.value?.validate().catch(() => false)
  if (!valid) return
  resetting.value = true
  try {
    await resetAdminPassword({
      id: userStore.userId,
      oldPassword: passwordForm.oldPassword,
      password: passwordForm.password,
    })
    ElMessage.success('密码修改成功')
    passwordForm.oldPassword = ''
    passwordForm.password = ''
    passwordForm.confirmPassword = ''
  } catch {
    // handled
  } finally {
    resetting.value = false
  }
}

/* ---- Avatar upload ---- */
function triggerUpload() {
  fileInputRef.value?.click()
}

function handleFileSelected(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  // Preview locally only – no upload API
  const reader = new FileReader()
  reader.onload = () => {
    const url = reader.result as string
    userStore.setUserInfo({
      userId: userStore.userId,
      nickname: adminInfo.nickName || '',
      avatar: url,
    })
    ElMessage.success('头像已更新（仅本地预览）')
  }
  reader.readAsDataURL(file)
  input.value = ''
}

/* ---- Route guard ---- */
onBeforeRouteLeave((_to, _from, next) => {
  if (!isDirty()) {
    next()
    return
  }
  ElMessageBox.confirm('有未保存的修改，确定离开吗？', '离开确认', { type: 'warning' })
    .then(() => next())
    .catch(() => next(false))
})

function handleBeforeUnload(e: BeforeUnloadEvent) {
  if (isDirty()) e.preventDefault()
}

/* ---- Lifecycle ---- */
onMounted(() => {
  window.addEventListener('beforeunload', handleBeforeUnload)
  fetchProfile()
})

onUnmounted(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload)
})
</script>

<style scoped>
/* 页面垂直居中 + 水平居中：撑满 AdminLayout .layout-content 剩余高度，超出可滚动 */
.profile-page {
  max-width: 1100px;
  margin: 0 auto;
  min-height: calc(100vh - var(--fl-header-height) - 80px);
  display: flex;
  flex-direction: column;
  justify-content: center;
}

/* 左右列等高：使两列在同一行自然等高 */
.profile-page :deep(.el-row) {
  display: flex;
  flex-wrap: wrap;
}
.profile-page :deep(.el-col) {
  display: flex;
}

/* 左右卡片宽高撑满列容器，配合 el-row flex 实现等高 */
.profile-avatar-card {
  width: 100%;
  height: 100%;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 0 4px;
}

.avatar-wrap {
  position: relative;
  width: 96px;
  height: 96px;
  margin-bottom: 16px;
  border-radius: 50%;
  cursor: pointer;
}

.profile-avatar {
  width: 96px;
  height: 96px;
  display: block;
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.55);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #fff;
  font-size: 11px;
  transition: opacity 0.2s;
  cursor: pointer;
}

.file-input-hidden {
  display: none;
}

.profile-name {
  font-size: 18px;
  font-weight: 700;
  color: var(--fl-text);
  margin: 0 0 4px;
  text-align: center;
}

.profile-username {
  font-size: 13px;
  color: var(--fl-text-dim);
  margin: 0 0 12px;
  text-align: center;
}

.profile-tags {
  display: flex;
  gap: 6px;
  justify-content: center;
}

.profile-meta {
  padding: 0 4px;
}

.meta-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.meta-row + .meta-row {
  border-top: 1px solid var(--fl-border-light);
}

.meta-row-label {
  font-size: 12px;
  color: var(--fl-text-dim);
}

.meta-row-value {
  font-size: 12px;
  color: var(--fl-text-secondary);
  text-align: right;
}

/* ========== 右侧表单卡片 ========== */
.profile-form-card {
  width: 100%;
  height: 100%;
}

.profile-tabs {
  margin-top: -8px;
}

.profile-form {
  max-width: 480px;
  margin-top: 8px;
}

/* ========== 安全设置 ========== */
.security-section {
  max-width: 480px;
  margin-top: 8px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: var(--fl-text);
}

.section-desc {
  margin: 6px 0 20px;
  font-size: 13px;
  color: var(--fl-text-dim);
}

.pwd-form {
  margin-top: 4px;
}

/* 密码强度 */
.pwd-strength {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 6px;
}

.strength-bar {
  flex: 1;
  height: 4px;
  background: #e4e7ed;
  border-radius: 2px;
  overflow: hidden;
}

.strength-fill {
  height: 100%;
  border-radius: 2px;
  transition: all 0.3s ease;
}

.strength-fill.weak {
  background: var(--fl-fall);
}

.strength-fill.medium {
  background: #f5a623;
}

.strength-fill.strong {
  background: var(--fl-rise);
}

.strength-text {
  font-size: 11px;
  font-weight: 500;
  min-width: 20px;
  text-align: right;
}

.strength-text.weak { color: var(--fl-fall); }
.strength-text.medium { color: #f5a623; }
.strength-text.strong { color: var(--fl-rise); }
</style>
