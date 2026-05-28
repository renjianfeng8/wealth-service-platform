<template>
  <div class="admin-profile-page">
    <div class="page-title">个人信息</div>

    <el-row :gutter="20">
      <el-col :xs="24" :lg="8">
        <el-card class="profile-card" shadow="never">
          <el-skeleton animated :loading="loading">
            <template #default>
              <div class="profile-header">
                <el-avatar :size="72" :icon="UserFilled" class="profile-avatar" />
                <h2 class="profile-name">{{ adminInfo.nickName || adminInfo.username }}</h2>
                <p class="profile-username">@{{ adminInfo.username }}</p>
              </div>
            </template>
          </el-skeleton>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="16">
        <el-card class="info-card" shadow="never">
          <template #header>基本信息</template>
          <el-form :model="adminInfo" label-width="80px" size="large">
            <el-form-item label="用户名">
              <el-input v-model="adminInfo.username" disabled />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="adminInfo.nickName" placeholder="设置昵称" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="adminInfo.email" placeholder="设置邮箱" />
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
    <el-dialog v-model="showPasswordDialog" title="修改密码" width="420" destroy-on-close :before-close="handlePasswordDialogClose">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="80px">
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入当前密码" />
        </el-form-item>
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
import { getAdminById, updateAdmin, resetAdminPassword } from '@/api/system'
import { UserFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

const userStore = useUserStore()

const loading = ref(true)
const saving = ref(false)
const resetting = ref(false)
const showPasswordDialog = ref(false)

const adminInfo = reactive({
  username: '',
  nickName: '',
  email: '',
})

const passwordFormRef = ref<FormInstance>()
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
  if (!userStore.userId) return
  adminInfo.username = userStore.username
  try {
    const res = await getAdminById(userStore.userId)
    const data = res.data
    if (data) {
      adminInfo.username = data.username || userStore.username
      adminInfo.nickName = data.nickName || ''
      adminInfo.email = data.email || ''
    }
  } catch {
    // use store defaults
  } finally {
    loading.value = false
  }
}

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
    await resetAdminPassword({
      id: userStore.userId,
      oldPassword: passwordForm.oldPassword,
      password: passwordForm.password,
    })
    ElMessage.success('密码修改成功')
    showPasswordDialog.value = false
    passwordForm.oldPassword = ''
    passwordForm.password = ''
    passwordForm.confirmPassword = ''
  } catch {
    // handled
  } finally {
    resetting.value = false
  }
}

async function handlePasswordDialogClose(done: () => void) {
  if (!passwordForm.oldPassword && !passwordForm.password && !passwordForm.confirmPassword) return done()
  try {
    await ElMessageBox.confirm('密码信息未保存，确定关闭吗？', '离开确认', { type: 'warning' })
    done()
  } catch { /* 取消关闭 */ }
}

onMounted(fetchProfile)
</script>

<style scoped>
.admin-profile-page {
  max-width: 1200px;
}
.profile-card {
  margin-bottom: 20px;
}
.profile-header {
  text-align: center;
  padding: 20px 0 8px;
}
.profile-avatar {
  background: var(--el-color-primary-light-8);
  color: var(--el-color-primary);
  margin-bottom: 12px;
}
.profile-name {
  font-size: 20px;
  font-weight: 700;
  color: var(--el-text-color-primary);
  margin-bottom: 4px;
}
.profile-username {
  font-size: 14px;
  color: var(--el-text-color-secondary);
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
  color: var(--el-text-color-primary);
}
.security-desc {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
</style>
