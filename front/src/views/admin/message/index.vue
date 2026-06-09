<template>
  <AdminPageShell title="站内消息" description="发送和维护用户站内消息、阅读状态与消息类型。">
    <AdminFilterBar :model="query" :fields="filterFields" @search="handleSearch" @reset="handleReset" />

    <AdminDataTable :data="tableData" :loading="loading" :total="total" :pagination="query" @page-change="fetchData">
      <template #toolbar>
        <el-button type="primary" @click="handleAdd">发送消息</el-button>
      </template>

      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="msgTitle" label="标题" min-width="160" show-overflow-tooltip />
      <el-table-column prop="msgType" label="类型" width="120">
        <template #default="{ row }">
          <el-tag>{{ msgTypeText(row.msgType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column prop="readFlag" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.readFlag === 1 ? 'success' : 'info'">{{ row.readFlag === 1 ? '已读' : '未读' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="发送时间" width="170" :formatter="formatDateColumn" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-popconfirm title="确定删除该消息？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button type="danger" link>删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </AdminDataTable>

    <AdminFormDialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑消息' : '发送消息'"
      :model="form"
      :rules="rules"
      :saving="saving"
      :before-close="handleDialogClose"
      width="560px"
      @submit="handleSave"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="用户ID">
            <el-input-number v-model="form.userId" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="类型" prop="msgType">
            <el-select v-model="form.msgType" style="width: 100%">
              <el-option v-for="item in MSG_TYPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="标题" prop="msgTitle">
        <el-input v-model="form.msgTitle" />
      </el-form-item>
      <el-form-item label="内容" prop="msgContent">
        <el-input v-model="form.msgContent" type="textarea" :rows="3" />
      </el-form-item>
    </AdminFormDialog>
  </AdminPageShell>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormRules } from 'element-plus'
import AdminPageShell from '@/components/admin/AdminPageShell.vue'
import AdminFilterBar from '@/components/admin/AdminFilterBar.vue'
import AdminDataTable from '@/components/admin/AdminDataTable.vue'
import AdminFormDialog from '@/components/admin/AdminFormDialog.vue'
import { useFormGuard } from '@/composables/useFormGuard'
import { getMessagePage, createMessage, updateMessage, deleteMessage } from '@/api/message'
import { formatDateTime, msgTypeText } from '@/utils/format'
import { MSG_TYPE_OPTIONS } from '@/types'
import type { WeaMessage } from '@/types'
import type { AdminFilterField } from '@/components/admin/AdminFilterBar.vue'

type MessageQuery = {
  pageNum: number
  pageSize: number
  msgTitle: string
  msgType: number | ''
}

type MessageForm = Omit<WeaMessage, 'userId'> & {
  userId?: number
}

const filterFields: AdminFilterField[] = [
  { prop: 'msgTitle', label: '标题', placeholder: '搜索消息标题' },
  { prop: 'msgType', label: '类型', type: 'select', options: MSG_TYPE_OPTIONS, width: '148px' },
]

const loading = ref(false)
const saving = ref(false)
const tableData = ref<WeaMessage[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)

const query = reactive<MessageQuery>({ pageNum: 1, pageSize: 10, msgTitle: '', msgType: '' })
const form = reactive<MessageForm>({ id: undefined, userId: undefined, msgType: 1, msgTitle: '', msgContent: '' })
const { isDirty, reset } = useFormGuard(form)

const rules: FormRules = {
  msgTitle: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  msgContent: [{ required: true, message: '请输入内容', trigger: 'blur' }],
}

async function fetchData() {
  loading.value = true
  try {
    const params: { pageNum: number; pageSize: number; msgTitle?: string; msgType?: number } = {
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    }
    if (query.msgTitle) params.msgTitle = query.msgTitle
    if (query.msgType !== '') params.msgType = query.msgType
    const res = await getMessagePage(params)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.pageNum = 1
  fetchData()
}

function handleReset() {
  query.msgTitle = ''
  query.msgType = ''
  handleSearch()
}

function resetForm() {
  Object.assign(form, { id: undefined, userId: undefined, msgType: 1, msgTitle: '', msgContent: '' })
}

function handleAdd() {
  isEdit.value = false
  resetForm()
  reset()
  dialogVisible.value = true
}

function handleEdit(row: WeaMessage) {
  isEdit.value = true
  Object.assign(form, row)
  reset()
  dialogVisible.value = true
}

async function handleDialogClose(done: () => void) {
  if (!isDirty()) {
    done()
    return
  }
  try {
    await ElMessageBox.confirm('有未保存的修改，确定关闭吗？', '离开确认', { type: 'warning' })
    done()
  } catch {
    // 用户取消关闭
  }
}

async function handleSave() {
  saving.value = true
  try {
    if (isEdit.value && form.id) {
      await updateMessage(form.id, form)
    } else {
      await createMessage(form)
    }
    ElMessage.success(isEdit.value ? '更新成功' : '发送成功')
    reset()
    dialogVisible.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function handleDelete(id?: number) {
  if (!id) return
  try {
    await deleteMessage(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    // 统一拦截器处理错误提示
  }
}

function formatDateColumn(_row: WeaMessage, _column: unknown, value: string) {
  return formatDateTime(value)
}

onMounted(fetchData)
</script>
