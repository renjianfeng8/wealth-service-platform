<template>
  <div class="user-page">
    <div class="page-header">
      <h3>用户管理</h3>
    </div>

    <el-card shadow="never" class="search-card">
      <el-form :model="query" inline>
        <el-form-item label="用户名">
          <el-input v-model="query.username" placeholder="用户名" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="状态" clearable style="width:120px">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top:16px;">
      <div style="margin-bottom:16px;">
        <el-button type="primary" @click="handleAdd">新增用户</el-button>
      </div>
      <el-table :data="tableData" stripe v-loading="loading" border empty-text="暂无数据">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" :formatter="fmtDT" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top:16px; display:flex; justify-content:flex-end;">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10,20,50]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="500px" :before-close="handleDialogClose">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useFormGuard } from '@/composables/useFormGuard'
import { getUserPage, createUser, updateUser, deleteUser } from '@/api/user'
import { formatDateTime, statusTag, statusText } from '@/utils/format'

const fmtDT = (_r: any, _c: any, val: string) => formatDateTime(val)
const loading = ref(false)
const saving = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const query = reactive({ pageNum: 1, pageSize: 10, username: '', status: '' })
const form = reactive({ id: undefined, username: '', password: '', nickname: '', phone: '', status: 1 })
const { isDirty, reset } = useFormGuard(form)
const validatePhone = (_rule: any, value: string, callback: any) => {
  if (value && !/^1\d{10}$/.test(value)) { callback(new Error('手机号格式不正确')) } else { callback() }
}
const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{
    validator: (_rule: any, value: string, callback: any) => {
      if (!isEdit.value && !value) { callback(new Error('请输入密码')) } else { callback() }
    }, trigger: 'blur'
  }],
  phone: [{ validator: validatePhone, trigger: 'blur' }],
}

async function fetchData() {
  loading.value = true
  try {
    const params: any = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.username) params.username = query.username
    if (query.status !== '') params.status = query.status
    const res = await getUserPage(params)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

function handleSearch() { query.pageNum = 1; fetchData() }
function handleReset() { query.username = ''; query.status = ''; handleSearch() }
function handleSizeChange() { query.pageNum = 1; fetchData() }
function handleAdd() { isEdit.value = false; form.id = undefined; form.username = ''; form.password = ''; form.nickname = ''; form.phone = ''; form.status = 1; reset(); dialogVisible.value = true }
function handleEdit(row: any) { isEdit.value = true; Object.assign(form, row); form.password = ''; reset(); dialogVisible.value = true }

async function handleDialogClose(done: () => void) {
  if (!isDirty()) return done()
  try {
    await ElMessageBox.confirm('有未保存的更改，确定关闭吗？', '离开确认', { type: 'warning' })
    done()
  } catch { /* 取消关闭 */ }
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (isEdit.value) {
      await updateUser(form.id!, form)
      ElMessage.success('更新成功')
      reset()
    } else {
      await createUser(form)
      ElMessage.success('创建成功')
      reset()
    }
    dialogVisible.value = false
    fetchData()
  } finally { saving.value = false }
}

async function handleDelete(id: number) {
  try {
    await deleteUser(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch { /* handled by interceptor */ }
}

onMounted(fetchData)
</script>

<style scoped>
.user-page { padding: 0; }
</style>
