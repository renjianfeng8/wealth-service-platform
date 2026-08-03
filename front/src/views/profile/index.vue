<template>
  <div class="profile-page">
    <el-row :gutter="24">
      <!-- ==================== 左侧：头像卡片 ==================== -->
      <el-col :xs="24" :lg="7">
        <el-card shadow="never" class="profile-avatar-card">
          <el-skeleton :loading="loading" animated>
            <template #default>
              <div class="avatar-section">
                <template v-if="!userStore.isAdmin">
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
                    <input ref="fileInputRef" type="file" accept="image/*" class="file-input-hidden" @change="handleFileSelected" />
                  </div>
                </template>
                <template v-else>
                  <el-avatar :size="96" :src="userStore.avatar" class="profile-avatar" />
                </template>

                <h2 class="profile-name">{{ displayName }}</h2>
                <p class="profile-username">@{{ userInfo.username }}</p>

                <div class="profile-tags">
                  <el-tag v-if="userStore.isAdmin" type="warning" effect="plain" size="small">管理员</el-tag>
                  <el-tag v-else type="success" effect="plain" size="small">用户</el-tag>
                </div>
              </div>

              <el-divider />

              <!-- 普通用户：统计 + 时间 -->
              <template v-if="!userStore.isAdmin">
                <div class="profile-stats">
                  <div class="stat-item" @click="router.push('/user/favorite')">
                    <span class="stat-num">{{ statFavorites }}</span>
                    <span class="stat-lbl">自选</span>
                  </div>
                  <div class="stat-item" @click="router.push('/user/trade')">
                    <span class="stat-num">{{ statOrders }}</span>
                    <span class="stat-lbl">委托单</span>
                  </div>
                  <div class="stat-item" @click="router.push('/user/message')">
                    <span class="stat-num">{{ statMessages }}</span>
                    <span class="stat-lbl">消息</span>
                  </div>
                </div>
                <el-divider />
                <div class="profile-meta">
                  <div class="meta-row">
                    <span class="meta-row-label">注册时间</span>
                    <span class="meta-row-value">{{ createTime || '-' }}</span>
                  </div>
                  <div class="meta-row">
                    <span class="meta-row-label">上次更新</span>
                    <span class="meta-row-value">{{ lastUpdateTime || '-' }}</span>
                  </div>
                </div>
              </template>
            </template>
          </el-skeleton>
        </el-card>
      </el-col>

      <!-- ==================== 右侧：Tab 面板 ==================== -->
      <el-col :xs="24" :lg="17">
        <el-card shadow="never" class="profile-form-card">
          <el-skeleton :loading="loading" animated>
            <template #default>
              <!-- 管理员预览模式 -->
              <template v-if="userStore.isAdmin">
                <div class="admin-preview-wrap">
                  <div class="preview-icon">
                    <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                      <circle cx="12" cy="12" r="3" />
                    </svg>
                  </div>
                  <h3>管理员预览模式</h3>
                  <p>管理员后台信息请前往 <router-link to="/admin/profile">个人信息</router-link> 查看与编辑。</p>
                  <el-descriptions :column="1" border size="small">
                    <el-descriptions-item label="用户名">{{ userInfo.username }}</el-descriptions-item>
                    <el-descriptions-item label="角色">管理员</el-descriptions-item>
                  </el-descriptions>
                </div>
              </template>

              <!-- 普通用户：Tab 面板 -->
              <template v-else>
                <el-tabs v-model="activeTab" class="profile-tabs">
                  <el-tab-pane label="基本信息" name="basic">
                    <el-form ref="basicFormRef" :model="userInfo" :rules="basicRules" label-position="top" class="profile-form">
                      <el-form-item label="用户名" prop="username">
                        <el-input v-model="userInfo.username" disabled>
                          <template #prefix><el-icon><User /></el-icon></template>
                        </el-input>
                      </el-form-item>
                      <el-form-item label="昵称" prop="nickname">
                        <el-input v-model="userInfo.nickname" placeholder="设置显示名称" maxlength="20" show-word-limit>
                          <template #prefix><el-icon><EditPen /></el-icon></template>
                        </el-input>
                      </el-form-item>
                      <el-form-item label="手机号" prop="phone">
                        <el-input v-model="userInfo.phone" placeholder="绑定手机号" maxlength="11">
                          <template #prefix><el-icon><Iphone /></el-icon></template>
                        </el-input>
                      </el-form-item>
                      <el-form-item>
                        <el-button type="primary" :loading="saving" @click="handleSave">
                          {{ saving ? '保存中...' : '保存修改' }}
                        </el-button>
                      </el-form-item>
                    </el-form>
                  </el-tab-pane>

                  <el-tab-pane label="安全设置" name="security">
                    <div class="security-section">
                      <div class="section-title">
                        <el-icon><Lock /></el-icon>
                        <span>修改密码</span>
                      </div>
                      <p class="section-desc">建议定期更换密码以保障账户安全</p>

                      <el-form ref="pwdFormRef" :model="passwordForm" :rules="passwordRules" label-position="top" class="pwd-form">
                        <el-form-item label="当前密码" prop="oldPassword">
                          <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入当前密码" />
                        </el-form-item>
                        <el-form-item label="新密码" prop="password">
                          <el-input v-model="passwordForm.password" type="password" show-password placeholder="至少 6 位字符" @input="onPwdInput" />
                          <div v-if="passwordForm.password" class="pwd-strength">
                            <div class="strength-bar">
                              <div :class="['strength-fill', strengthClass]" :style="{ width: strengthPercent + '%' }" />
                            </div>
                            <span class="strength-text" :class="strengthClass">{{ strengthLabel }}</span>
                          </div>
                        </el-form-item>
                        <el-form-item label="确认密码" prop="confirmPassword">
                          <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
                        </el-form-item>
                        <el-form-item>
                          <el-button type="primary" :loading="resetting" @click="handleResetPassword">
                            {{ resetting ? '修改中...' : '确认修改' }}
                          </el-button>
                        </el-form-item>
                      </el-form>
                    </div>
                  </el-tab-pane>
                </el-tabs>
              </template>
            </template>
          </el-skeleton>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/index'
import { getUserInfo, updateUser, resetPassword } from '@/api/user'
import { getFavoritePage } from '@/api/favorite'
import { getTradeOrderPage } from '@/api/trade'
import { getMessagePage } from '@/api/message'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useFormGuard } from '@/composables/useFormGuard'
import { onBeforeRouteLeave } from 'vue-router'
import { User, EditPen, Iphone, Lock } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const saving = ref(false)
const resetting = ref(false)
const activeTab = ref('basic')
const avatarHover = ref(false)
const fileInputRef = ref<HTMLInputElement>()

const userInfo = reactive({
  username: '',
  nickname: '',
  phone: '',
})
const createTime = ref('')
const lastUpdateTime = ref('')

/* ---- Stats ---- */
const statFavorites = ref(0)
const statOrders = ref(0)
const statMessages = ref(0)

/* ---- Basic form ---- */
const basicFormRef = ref<FormInstance>()
const basicRules: FormRules = {
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }],
}

/* ---- Password form ---- */
const pwdFormRef = ref<FormInstance>()
const passwordForm = reactive({
  oldPassword: '',
  password: '',
  confirmPassword: '',
})

const displayName = computed(() => userInfo.nickname || userInfo.username || '用户')

/* ---- Password rules ---- */
const passwordRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
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
  return score >= 2 ? 100 : 70
})
const strengthClass = computed(() => {
  const pct = strengthPercent.value
  if (pct >= 100) return 'strong'
  if (pct >= 55) return 'medium'
  return 'weak'
})
const strengthLabel = computed(() => ({ weak: '弱', medium: '中', strong: '强' })[strengthClass.value] || '')

function onPwdInput() { /* reactivity */ }

/* ---- Form guard ---- */
const { isDirty, reset: resetGuard } = useFormGuard(userInfo)

/* ---- Fetch profile ---- */
let fetchProfilePromise: Promise<void> | null = null
let fetchStatsPromise: Promise<void> | null = null

async function fetchProfile() {
  if (!userStore.userId) return
  if (fetchProfilePromise) return fetchProfilePromise
  userInfo.username = userStore.username
  userInfo.nickname = userStore.nickname

  if (userStore.isAdmin) { loading.value = false; return }

  fetchProfilePromise = (async () => {
    try {
      const res = await getUserInfo(userStore.userId)
      const data = res.data
      if (data) {
        userInfo.username = data.username || userStore.username
        userInfo.nickname = data.nickname || userStore.nickname
        userInfo.phone = data.phone || ''
        createTime.value = (data as any).createTime ? dayjs((data as any).createTime).format('YYYY-MM-DD HH:mm') : ''
        lastUpdateTime.value = (data as any).updateTime ? dayjs((data as any).updateTime).format('YYYY-MM-DD HH:mm') : ''
        if (data.avatar) userStore.setUserInfo({ userId: userStore.userId, nickname: userInfo.nickname, avatar: data.avatar })
      }
      resetGuard()
    } catch (err) { console.warn('[profile] fetchProfile 失败:', err)
    } finally { fetchProfilePromise = null }
  })()
  return fetchProfilePromise
}

async function fetchStats() {
  if (!userStore.userId || userStore.isAdmin) return
  if (fetchStatsPromise) return fetchStatsPromise
  fetchStatsPromise = (async () => {
    try {
      const [fr, tr, mr] = await Promise.all([
        getFavoritePage({ pageNum: 1, pageSize: 1, userId: userStore.userId }),
        getTradeOrderPage({ pageNum: 1, pageSize: 1, userId: userStore.userId }),
        getMessagePage({ pageNum: 1, pageSize: 1, userId: userStore.userId }),
      ])
      statFavorites.value = fr.data?.total || 0
      statOrders.value = tr.data?.total || 0
      statMessages.value = mr.data?.total || 0
    } catch (err) { console.warn('[profile] fetchStats 失败:', err)
    } finally { fetchStatsPromise = null }
  })()
  return fetchStatsPromise
}

/* ---- Save ---- */
async function handleSave() {
  if (!userStore.userId) return
  const valid = await basicFormRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await updateUser(userStore.userId, { nickname: userInfo.nickname, phone: userInfo.phone })
    userStore.setUserInfo({ userId: userStore.userId, nickname: userInfo.nickname || '', avatar: '' })
    ElMessage.success('保存成功')
    resetGuard()
  } catch { /* handled */
  } finally { saving.value = false }
}

/* ---- Reset password ---- */
async function handleResetPassword() {
  const valid = await pwdFormRef.value?.validate().catch(() => false)
  if (!valid) return
  resetting.value = true
  try {
    await resetPassword({ id: userStore.userId, oldPassword: passwordForm.oldPassword, password: passwordForm.password })
    ElMessage.success('密码修改成功')
    passwordForm.oldPassword = ''
    passwordForm.password = ''
    passwordForm.confirmPassword = ''
  } catch { /* handled */
  } finally { resetting.value = false }
}

/* ---- Avatar upload ---- */
function triggerUpload() { fileInputRef.value?.click() }

function handleFileSelected(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  // 头像上传接口未接入：不写入 localStorage（DataURL 有配额超限风险），不谎报已更新
  ElMessage.warning('头像上传功能暂未开放，无法保存')
  input.value = ''
}

/* ---- Route guard ---- */
onBeforeRouteLeave((_to, _from, next) => {
  if (!isDirty()) return next()
  ElMessageBox.confirm('有未保存的更改，确定离开吗？', '离开确认', { type: 'warning' })
    .then(() => next()).catch(() => next(false))
})

function handleBeforeUnload(e: BeforeUnloadEvent) {
  if (isDirty()) { e.preventDefault(); (e as any).returnValue = '' }
}

/* ---- Lifecycle ---- */
onMounted(async () => {
  window.addEventListener('beforeunload', handleBeforeUnload)
  await Promise.all([fetchProfile(), fetchStats()])
  loading.value = false
})

onUnmounted(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload)
})
</script>

<style scoped>
/* 页面垂直居中 + 水平居中：撑满 UserLayout .layout-main 剩余高度，超出可滚动 */
.profile-page {
  max-width: 1100px;
  margin: 0 auto;
  min-height: calc(100vh - var(--navbar-height, 56px) - 120px);
  display: flex;
  flex-direction: column;
  justify-content: center;
}

/* 左右列等高 */
.profile-page :deep(.el-row) {
  display: flex;
  flex-wrap: wrap;
}
.profile-page :deep(.el-col) {
  display: flex;
}

/* 左右卡片宽高撑满列容器 */
.profile-avatar-card {
  width: 100%;
  height: 100%;
}
.profile-form-card {
  width: 100%;
  height: 100%;
}

/* ========== 左侧头像卡片 ========== */
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

.file-input-hidden { display: none; }

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

/* ---- Stats ---- */
.profile-stats {
  display: flex;
  justify-content: space-around;
  padding: 4px 0;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 8px;
  transition: background 0.2s;
}
.stat-item:hover {
  background: #f5f7fa;
}

.stat-num {
  font-size: 20px;
  font-weight: 700;
  color: var(--fl-primary);
  font-family: 'Courier New', monospace;
}

.stat-lbl {
  font-size: 12px;
  color: var(--fl-text-dim);
}

/* ---- Meta rows ---- */
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

.pwd-form { margin-top: 4px; }

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
.strength-fill.weak { background: var(--fl-fall); }
.strength-fill.medium { background: #f5a623; }
.strength-fill.strong { background: var(--fl-rise); }
.strength-text { font-size: 11px; font-weight: 500; min-width: 20px; text-align: right; }
.strength-text.weak { color: var(--fl-fall); }
.strength-text.medium { color: #f5a623; }
.strength-text.strong { color: var(--fl-rise); }

/* ========== 管理员预览模式 ========== */
.admin-preview-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 20px;
  text-align: center;
}
.preview-icon {
  color: var(--fl-text-placeholder);
  margin-bottom: 12px;
}
.admin-preview-wrap h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--fl-text);
  margin: 0 0 8px;
}
.admin-preview-wrap p {
  font-size: 14px;
  color: var(--fl-text-secondary);
  margin: 0 0 20px;
}
.admin-preview-wrap a {
  color: var(--fl-primary);
  text-decoration: none;
  font-weight: 500;
}
.admin-preview-wrap a:hover {
  text-decoration: underline;
}
.admin-preview-wrap .el-descriptions {
  width: 100%;
  max-width: 320px;
}
</style>
