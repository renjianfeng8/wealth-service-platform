<template>
  <div class="profile-page">
    <div class="page-title">个人中心</div>

    <el-row :gutter="20">
      <el-col :xs="24" :lg="8">
        <el-card class="profile-card" shadow="never">
          <div class="profile-header">
            <el-avatar :size="72" :icon="UserFilled" class="profile-avatar" />
            <h2 class="profile-name">{{ userInfo.nickname || userInfo.username }}</h2>
            <p class="profile-username">@{{ userInfo.username }}</p>
          </div>
          <el-divider />
          <div class="profile-stats">
            <div class="stat-item">
              <span class="stat-num">{{ statFavorites }}</span>
              <span class="stat-lbl">自选</span>
            </div>
            <div class="stat-item">
              <span class="stat-num">{{ statOrders }}</span>
              <span class="stat-lbl">委托单</span>
            </div>
            <div class="stat-item">
              <span class="stat-num">{{ statMessages }}</span>
              <span class="stat-lbl">消息</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="16">
        <el-card class="info-card" shadow="never">
          <template #header>个人信息</template>
          <el-form :model="userInfo" label-width="80px" size="large">
            <el-form-item label="用户名">
              <el-input v-model="userInfo.username" disabled />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="userInfo.nickname" placeholder="设置昵称" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="userInfo.phone" placeholder="绑定手机号" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="handleSave">
                {{ saving ? '保存中...' : '保存修改' }}
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="security-card" shadow="never" style="margin-top: 20px">
          <template #header>安全设置</template>
          <div class="security-item">
            <div class="security-info">
              <span class="security-label">登录密码</span>
              <span class="security-desc">建议定期更换密码以保障账户安全</span>
            </div>
            <el-button text type="primary" @click="showPasswordDialog = true">修改密码</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 修改密码 -->
    <el-dialog v-model="showPasswordDialog" title="修改密码" width="420" destroy-on-close>
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="80px">
        <el-form-item label="新密码" prop="password">
          <el-input v-model="passwordForm.password" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请确认新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" :loading="resetting" @click="handleResetPassword">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/store/index'
import { getUserInfo, updateUser } from '@/api/user'
import { resetPassword } from '@/api/user'
import { getFavoritePage } from '@/api/favorite'
import { getTradeOrderPage } from '@/api/trade'
import { getMessagePage } from '@/api/message'
import { UserFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

const userStore = useUserStore()

const saving = ref(false)
const resetting = ref(false)
const showPasswordDialog = ref(false)

const userInfo = reactive({
  username: '',
  nickname: '',
  phone: '',
})

// 统计
const statFavorites = ref(0)
const statOrders = ref(0)
const statMessages = ref(0)

// 密码表单
const passwordFormRef = ref<FormInstance>()
const passwordForm = reactive({
  password: '',
  confirmPassword: '',
})

const passwordRules: FormRules = {
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' },
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

async function fetchProfile() {
  // 使用 store 数据作为兜底
  userInfo.username = userStore.username
  userInfo.nickname = userStore.nickname

  if (!userStore.userId) return
  try {
    const res = await getUserInfo(userStore.userId)
    const data = res.data
    if (data) {
      userInfo.username = data.username || userStore.username
      userInfo.nickname = data.nickname || userStore.nickname
      userInfo.phone = data.phone || ''
    }
  } catch { /* use store defaults */ }
}

async function fetchStats() {
  if (!userStore.userId) return
  try {
    const [fr, tr, mr] = await Promise.all([
      getFavoritePage({ pageNum: 1, pageSize: 1, userId: userStore.userId }),
      getTradeOrderPage({ pageNum: 1, pageSize: 1, userId: userStore.userId }),
      getMessagePage({ pageNum: 1, pageSize: 1, userId: userStore.userId }),
    ])
    statFavorites.value = fr.data?.total || 0
    statOrders.value = tr.data?.total || 0
    statMessages.value = mr.data?.total || 0
  } catch { /* ignore */ }
}

async function handleSave() {
  if (!userStore.userId) return
  saving.value = true
  try {
    await updateUser(userStore.userId, {
      nickname: userInfo.nickname,
      phone: userInfo.phone,
    })
    userStore.setUserInfo({
      userId: userStore.userId,
      nickname: userInfo.nickname || '',
      avatar: '',
    })
    ElMessage.success('保存成功')
  } catch {
    // handled
  } finally {
    saving.value = false
  }
}

async function handleResetPassword() {
  const valid = await passwordFormRef.value?.validate().catch(() => false)
  if (!valid) return
  resetting.value = true
  try {
    await resetPassword({ username: userInfo.username, password: passwordForm.password })
    ElMessage.success('密码修改成功')
    showPasswordDialog.value = false
    passwordForm.password = ''
    passwordForm.confirmPassword = ''
  } catch {
    // handled
  } finally {
    resetting.value = false
  }
}

onMounted(() => {
  fetchProfile()
  fetchStats()
})
</script>

<style scoped>
.profile-page { max-width: 1200px; }

.profile-card {
  margin-bottom: 20px;
}

.profile-header {
  text-align: center;
  padding: 20px 0 8px;
}

.profile-avatar {
  background: var(--primary-light);
  color: var(--primary);
  margin-bottom: 12px;
}

.profile-name {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.profile-username {
  font-size: 14px;
  color: var(--text-secondary);
}

.profile-stats {
  display: flex;
  justify-content: space-around;
  padding: 8px 0;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-num {
  font-size: 22px;
  font-weight: 700;
  color: var(--primary);
}

.stat-lbl {
  font-size: 13px;
  color: var(--text-secondary);
}

.security-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
}

.security-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.security-label {
  font-size: 15px;
  font-weight: 500;
  color: var(--text-primary);
}

.security-desc {
  font-size: 13px;
  color: var(--text-secondary);
}

/* 密码弹窗 */
:deep(.el-dialog__body) {
  padding: 24px;
}
</style>
