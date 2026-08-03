import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { assignEditable } from '@/utils/object'
import { formatDateTime, formatPrice } from '@/utils/format'

export function useCrudPage<T extends Record<string, any>>(fetchData: () => Promise<void>) {
  const loading = ref(false)
  const saving = ref(false)
  const tableData = ref<T[]>([])
  const total = ref(0)
  const dialogVisible = ref(false)
  const isEdit = ref(false)

  function handleSearch(query: { pageNum: number }) {
    query.pageNum = 1
    fetchData()
  }

  function handleAdd(resetForm: () => void, resetDirty: () => void) {
    isEdit.value = false
    resetForm()
    resetDirty()
    dialogVisible.value = true
  }

  function handleEdit(form: Record<string, any>, row: Record<string, any>, resetDirty: () => void) {
    isEdit.value = true
    assignEditable(form, row)
    resetDirty()
    dialogVisible.value = true
  }

  function handleDialogClose(dirty: () => boolean) {
    return (done: () => void) => {
      if (!dirty()) {
        done()
        return
      }
      ElMessageBox.confirm('有未保存的修改，确定关闭吗？', '离开确认', { type: 'warning' })
        .then(() => done())
        .catch(() => {})
    }
  }

  async function handleDelete(id: number | string | undefined, api: (id: number | string) => Promise<unknown>) {
    if (!id) return
    try {
      await api(id)
      ElMessage.success('删除成功')
      await fetchData()
    } catch {
      // 统一拦截器处理错误提示
    }
  }

  function formatDateColumn(_row: unknown, _column: unknown, value: string) {
    return formatDateTime(value)
  }

  function formatPriceColumn(_row: unknown, _column: unknown, value: number) {
    return formatPrice(value)
  }

  return {
    loading,
    saving,
    tableData,
    total,
    dialogVisible,
    isEdit,
    handleSearch,
    handleAdd,
    handleEdit,
    handleDialogClose,
    handleDelete,
    formatDateColumn,
    formatPriceColumn,
  }
}
