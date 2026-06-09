<template>
  <AdminPageShell title="资讯管理" description="维护财经资讯内容、来源、分类与发布状态。">
    <AdminFilterBar :model="query" :fields="filterFields" @search="handleSearch" @reset="handleReset" />

    <AdminDataTable :data="tableData" :loading="loading" :total="total" :pagination="query" @page-change="fetchData">
      <template #toolbar>
        <el-button type="primary" @click="handleAdd">新增资讯</el-button>
      </template>

      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
      <el-table-column prop="newsType" label="类型" width="120">
        <template #default="{ row }">
          <el-tag>{{ newsTypeText(row.newsType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="source" label="来源" min-width="120" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '发布' : '草稿' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="publishTime" label="发布时间" width="170" :formatter="formatDateColumn" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-popconfirm title="确定删除该资讯？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button type="danger" link>删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </AdminDataTable>

    <AdminFormDialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑资讯' : '新增资讯'"
      :model="form"
      :rules="rules"
      :saving="saving"
      :before-close="handleDialogClose"
      width="620px"
      @submit="handleSave"
    >
      <el-row :gutter="20">
        <el-col :span="16">
          <el-form-item label="标题" prop="title">
            <el-input v-model="form.title" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="类型">
            <el-select v-model="form.newsType" style="width: 100%">
              <el-option v-for="item in NEWS_TYPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="来源">
            <el-input v-model="form.source" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态">
            <el-radio-group v-model="form.status">
              <el-radio :value="1">发布</el-radio>
              <el-radio :value="0">草稿</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="内容" prop="content">
        <el-input v-model="form.content" type="textarea" :rows="4" />
      </el-form-item>
      <el-form-item label="发布时间">
        <el-date-picker v-model="form.publishTime" type="datetime" style="width: 100%" value-format="YYYY-MM-DDTHH:mm:ss" />
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
import { getNewsPage, createNews, updateNews, deleteNews } from '@/api/message'
import { formatDateTime, newsTypeText } from '@/utils/format'
import { NEWS_TYPE_OPTIONS } from '@/types'
import type { WeaNews } from '@/types'
import type { AdminFilterField } from '@/components/admin/AdminFilterBar.vue'

type NewsQuery = {
  pageNum: number
  pageSize: number
  title: string
  source: string
}

const filterFields: AdminFilterField[] = [
  { prop: 'title', label: '标题', placeholder: '搜索资讯标题' },
  { prop: 'source', label: '来源', placeholder: '搜索来源' },
]

const loading = ref(false)
const saving = ref(false)
const tableData = ref<WeaNews[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)

const query = reactive<NewsQuery>({ pageNum: 1, pageSize: 10, title: '', source: '' })
const form = reactive<WeaNews>({ id: undefined, title: '', content: '', newsType: 1, source: '', status: 1, publishTime: '' })
const { isDirty, reset } = useFormGuard(form)

const rules: FormRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
}

async function fetchData() {
  loading.value = true
  try {
    const params: { pageNum: number; pageSize: number; title?: string; source?: string } = {
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    }
    if (query.title) params.title = query.title
    if (query.source) params.source = query.source
    const res = await getNewsPage(params)
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
  query.title = ''
  query.source = ''
  handleSearch()
}

function resetForm() {
  Object.assign(form, { id: undefined, title: '', content: '', newsType: 1, source: '', status: 1, publishTime: '' })
}

function handleAdd() {
  isEdit.value = false
  resetForm()
  reset()
  dialogVisible.value = true
}

function handleEdit(row: WeaNews) {
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
      await updateNews(form.id, form)
    } else {
      await createNews(form)
    }
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
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
    await deleteNews(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    // 统一拦截器处理错误提示
  }
}

function formatDateColumn(_row: WeaNews, _column: unknown, value: string) {
  return formatDateTime(value)
}

onMounted(fetchData)
</script>
