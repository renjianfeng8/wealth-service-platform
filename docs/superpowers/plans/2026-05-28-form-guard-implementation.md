# 表单离开确认实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为前端表单添加离开确认和编辑保存提示增强

**Architecture:** 创建 `useFormGuard` composable 统一管理表单 dirty 状态，分别在独立路由页面（profile/register）和弹窗表单页面（admin CRUD）中接入

**Tech Stack:** Vue 3 + TypeScript + Element Plus + Vue Router 4

---

## 文件清单

| 操作 | 路径 |
|------|------|
| Create | `front/src/composables/useFormGuard.ts` |
| Modify | `front/src/views/profile/index.vue` |
| Modify | `front/src/views/register/index.vue` |
| Modify | `front/src/views/admin/product/index.vue` |
| Modify | `front/src/views/admin/user/index.vue` |
| Modify | `front/src/views/admin/system/admin/index.vue` |
| Modify | `front/src/views/admin/system/role/index.vue` |
| Modify | `front/src/views/admin/system/resource/index.vue` |
| Modify | `front/src/views/admin/news/index.vue` |
| Modify | `front/src/views/admin/message/index.vue` |

---

### Task 1: 创建 composables 目录和 useFormGuard

**Files:**
- Create: `front/src/composables/useFormGuard.ts`

- [ ] **Step 1: 创建目录和文件**

```bash
mkdir -p front/src/composables
```

- [ ] **Step 2: 编写 useFormGuard**

```typescript
// front/src/composables/useFormGuard.ts
import { reactive, toRefs } from 'vue'

export function useFormGuard(form: Record<string, any>) {
  const state = reactive({
    snapshot: JSON.stringify(form),
    clean: true,
  })

  function isDirty(): boolean {
    if (!state.clean) return false
    return JSON.stringify(form) !== state.snapshot
  }

  function reset() {
    state.snapshot = JSON.stringify(form)
  }

  function markClean() {
    state.clean = false
  }

  return { isDirty, reset, markClean }
}
```

---

### Task 2: Profile 页面接入

**Files:**
- Modify: `front/src/views/profile/index.vue`

- [ ] **Step 1: 添加 import 和使用 useFormGuard**

在 `<script setup>` 中 `import { ElMessage } from 'element-plus'` 之后，先不要执行测试，只是添加如下改动：

在 125 行 `import { ElMessage } from 'element-plus'` 后新增：
```typescript
import { ElMessageBox } from 'element-plus'
import { useFormGuard } from '@/composables/useFormGuard'
import { onBeforeRouteLeave } from 'vue-router'
import { onUnmounted } from 'vue'
```

在 `const passwordForm = reactive({...})` 之后（148 行后）添加：
```typescript
const { isDirty, reset } = useFormGuard(userInfo)
```

在 `onMounted` 之前添加路由守卫和 beforeunload 监听：
```typescript
onBeforeRouteLeave((to, from, next) => {
  if (!isDirty()) return next()
  ElMessageBox.confirm('有未保存的更改，确定离开吗？', '离开确认', {
    confirmButtonText: '离开',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => next()).catch(() => next(false))
})

onMounted(() => {
  const handler = (e: BeforeUnloadEvent) => {
    if (isDirty()) { e.preventDefault(); e.returnValue = '' }
  }
  window.addEventListener('beforeunload', handler)
})
onUnmounted(() => window.removeEventListener('beforeunload', handler))
```

在 `handleSave` 中 `ElMessage.success('保存成功')` 之后（214 行后）调用 `reset()`：
```typescript
reset()
```

- [ ] **Step 2: 验证改动**

```bash
cd front && npx vue-tsc --noEmit --strict 2>&1 | head -30
```
Expected: No type errors, or only pre-existing errors unrelated to our changes.

---

### Task 3: Register 页面接入

**Files:**
- Modify: `front/src/views/register/index.vue`

- [ ] **Step 1: 添加 import 和使用 useFormGuard**

在 `import { ElMessage } from 'element-plus'` 后新增：
```typescript
import { ElMessageBox } from 'element-plus'
import { useFormGuard } from '@/composables/useFormGuard'
import { onBeforeRouteLeave } from 'vue-router'
import { onUnmounted } from 'vue'
```

在 `const form = reactive({...})` 之后（101 行后）添加：
```typescript
const { isDirty, reset } = useFormGuard(form)
```

在 `const rules: FormRules` 之前添加路由守卫：
```typescript
onBeforeRouteLeave((to, from, next) => {
  if (!isDirty()) return next()
  ElMessageBox.confirm('有未保存的更改，确定离开吗？', '离开确认', {
    confirmButtonText: '离开',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => next()).catch(() => next(false))
})

onMounted(() => {
  const handler = (e: BeforeUnloadEvent) => {
    if (isDirty()) { e.preventDefault(); e.returnValue = '' }
  }
  window.addEventListener('beforeunload', handler)
})
onUnmounted(() => window.removeEventListener('beforeunload', handler))
```

在 `handleRegister` 中 `router.push('/auth/login')` 之前调用 `reset()`：
```typescript
reset()
```

- [ ] **Step 2: 验证编译**

```bash
cd front && npx vue-tsc --noEmit --strict 2>&1 | head -30
```
Expected: No type errors from our changes.

---

### Task 4: Admin 产品管理弹窗接入

**Files:**
- Modify: `front/src/views/admin/product/index.vue`

- [ ] **Step 1: 添加 import 和 useFormGuard**

在 `import { ElMessage } from 'element-plus'` 后新增：
```typescript
import { ElMessageBox } from 'element-plus'
import { useFormGuard } from '@/composables/useFormGuard'
```

在 `const form = reactive({...})` 之后（77 行后）添加：
```typescript
const { isDirty, reset } = useFormGuard(form)
```

在 `handleAdd` 中 `dialogVisible.value = true` 之前调用 `reset()`：
```typescript
reset()
```

在 `handleEdit` 中 `dialogVisible.value = true` 之前调用 `reset()`：
```typescript
reset()
```

修改 el-dialog 添加 `:before-close="handleDialogClose"`：
```html
<el-dialog v-model="dialogVisible" :title="isEdit?'编辑产品':'新增产品'" width="550px" :before-close="handleDialogClose">
```

添加 `handleDialogClose` 函数：
```typescript
async function handleDialogClose(done: () => void) {
  if (!isDirty()) return done()
  try {
    await ElMessageBox.confirm('有未保存的更改，确定关闭吗？', '离开确认', { type: 'warning' })
    done()
  } catch { /* 取消关闭 */ }
}
```

在 `handleSave` 中 `ElMessage.success(...)` 之后调用 `reset()`：
```typescript
reset()
```

---

### Task 5: Admin 用户管理弹窗接入

**Files:**
- Modify: `front/src/views/admin/user/index.vue`

- [ ] **Step 1: 添加 import 和 useFormGuard**

在 `import { ElMessage } from 'element-plus'` 后新增：
```typescript
import { ElMessageBox } from 'element-plus'
import { useFormGuard } from '@/composables/useFormGuard'
```

在 `const form = reactive({...})` 之后（110 行后）添加：
```typescript
const { isDirty, reset } = useFormGuard(form)
```

在 `handleAdd` 中 `dialogVisible.value = true` 之前调用 `reset()`：
```typescript
reset()
```

在 `handleEdit` 中 `dialogVisible.value = true` 之前调用 `reset()`：
```typescript
reset()
```

修改 el-dialog 添加 `:before-close="handleDialogClose"`：
```html
<el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="500px" :before-close="handleDialogClose">
```

添加 `handleDialogClose` 函数：
```typescript
async function handleDialogClose(done: () => void) {
  if (!isDirty()) return done()
  try {
    await ElMessageBox.confirm('有未保存的更改，确定关闭吗？', '离开确认', { type: 'warning' })
    done()
  } catch { /* 取消关闭 */ }
}
```

在 `handleSave` 中 `ElMessage.success(...)` 之后调用 `reset()`：
```typescript
reset()
```

---

### Task 6: Admin 管理员管理弹窗接入

**Files:**
- Modify: `front/src/views/admin/system/admin/index.vue`

- [ ] **Step 1: 添加 import**

在 `import { ElMessage } from 'element-plus'` 后新增：
```typescript
import { ElMessageBox } from 'element-plus'
import { useFormGuard } from '@/composables/useFormGuard'
```

在 `const form = reactive({ id: undefined, username: '', password: '', nickName: '', email: '', status: 1 })` 之后添加：
```typescript
const { isDirty, reset } = useFormGuard(form)
```

修改 `handleAdd` 和 `handleEdit`，在设置 `dialogVisible.value = true` 之前调用 `reset()`：
```typescript
function handleAdd() { isEdit.value = false; Object.assign(form, { id: undefined, username: '', password: '', nickName: '', email: '', status: 1 }); reset(); dialogVisible.value = true }
function handleEdit(row: any) { isEdit.value = true; Object.assign(form, row); form.password = ''; reset(); dialogVisible.value = true }
```

修改 el-dialog 模板添加 `:before-close`：
```html
<el-dialog v-model="dialogVisible" :title="isEdit?'编辑管理员':'新增管理员'" width="500px" :before-close="handleDialogClose">
```

添加 `handleDialogClose` 函数（放在 `handleSave` 之前）：
```typescript
async function handleDialogClose(done: () => void) {
  if (!isDirty()) return done()
  try {
    await ElMessageBox.confirm('有未保存的更改，确定关闭吗？', '离开确认', { type: 'warning' })
    done()
  } catch { /* 取消关闭 */ }
}
```

在 `handleSave` 中 `ElMessage.success(...)` 之后调用 `reset()`：
```typescript
reset()
```

---

### Task 7: Admin 角色管理弹窗接入

**Files:**
- Modify: `front/src/views/admin/system/role/index.vue`

- [ ] **Step 1: 添加 import**

在 `import { ElMessage } from 'element-plus'` 后新增：
```typescript
import { ElMessageBox } from 'element-plus'
import { useFormGuard } from '@/composables/useFormGuard'
```

在 `const form = reactive({ id: undefined, name: '', description: '', sort: 0, status: 1 })` 之后添加：
```typescript
const { isDirty, reset } = useFormGuard(form)
```

修改 `handleAdd` 和 `handleEdit`，在设置 `dialogVisible.value = true` 之前调用 `reset()`：
```typescript
function handleAdd() { isEdit.value = false; Object.assign(form, { id: undefined, name: '', description: '', sort: 0, status: 1 }); reset(); dialogVisible.value = true }
function handleEdit(row: any) { isEdit.value = true; Object.assign(form, row); reset(); dialogVisible.value = true }
```

修改 el-dialog 模板：
```html
<el-dialog v-model="dialogVisible" :title="isEdit?'编辑角色':'新增角色'" width="500px" :before-close="handleDialogClose">
```

添加 `handleDialogClose`：
```typescript
async function handleDialogClose(done: () => void) {
  if (!isDirty()) return done()
  try {
    await ElMessageBox.confirm('有未保存的更改，确定关闭吗？', '离开确认', { type: 'warning' })
    done()
  } catch { /* 取消关闭 */ }
}
```

在 `handleSave` 中 `ElMessage.success(...)` 之后调用 `reset()`。

---

### Task 8: Admin 资源管理弹窗接入

**Files:**
- Modify: `front/src/views/admin/system/resource/index.vue`

- [ ] **Step 1: 添加 import**

在 `import { ElMessage } from 'element-plus'` 后新增：
```typescript
import { ElMessageBox } from 'element-plus'
import { useFormGuard } from '@/composables/useFormGuard'
```

在 `const form = reactive({ id: undefined, name: '', url: '', description: '', categoryId: undefined })` 之后添加：
```typescript
const { isDirty, reset } = useFormGuard(form)
```

修改 `handleAdd` 和 `handleEdit`：
```typescript
function handleAdd() { isEdit.value = false; Object.assign(form, { id: undefined, name: '', url: '', description: '', categoryId: undefined }); reset(); dialogVisible.value = true }
function handleEdit(row: any) { isEdit.value = true; Object.assign(form, row); reset(); dialogVisible.value = true }
```

修改 el-dialog 模板：
```html
<el-dialog v-model="dialogVisible" :title="isEdit?'编辑资源':'新增资源'" width="500px" :before-close="handleDialogClose">
```

添加 `handleDialogClose`：
```typescript
async function handleDialogClose(done: () => void) {
  if (!isDirty()) return done()
  try {
    await ElMessageBox.confirm('有未保存的更改，确定关闭吗？', '离开确认', { type: 'warning' })
    done()
  } catch { /* 取消关闭 */ }
}
```

在 `handleSave` 中 `ElMessage.success(...)` 之后调用 `reset()`。

---

### Task 9: Admin 资讯管理弹窗接入

**Files:**
- Modify: `front/src/views/admin/news/index.vue`

- [ ] **Step 1: 添加 import**

在 `import { ElMessage } from 'element-plus'` 后新增：
```typescript
import { ElMessageBox } from 'element-plus'
import { useFormGuard } from '@/composables/useFormGuard'
```

在 `const form = reactive({ id: undefined, title: '', content: '', newsType: 1, source: '', status: 1, publishTime: '' })` 之后添加：
```typescript
const { isDirty, reset } = useFormGuard(form)
```

修改 `handleAdd` 和 `handleEdit`：
```typescript
function handleAdd() { isEdit.value = false; Object.assign(form, { id: undefined, title: '', content: '', newsType: 1, source: '', status: 1, publishTime: '' }); reset(); dialogVisible.value = true }
function handleEdit(row: any) { isEdit.value = true; Object.assign(form, row); reset(); dialogVisible.value = true }
```

修改 el-dialog 模板：
```html
<el-dialog v-model="dialogVisible" :title="isEdit?'编辑资讯':'新增资讯'" width="600px" :before-close="handleDialogClose">
```

添加 `handleDialogClose`：
```typescript
async function handleDialogClose(done: () => void) {
  if (!isDirty()) return done()
  try {
    await ElMessageBox.confirm('有未保存的更改，确定关闭吗？', '离开确认', { type: 'warning' })
    done()
  } catch { /* 取消关闭 */ }
}
```

在 `handleSave` 中 `ElMessage.success(...)` 之后调用 `reset()`。

---

### Task 10: Admin 消息管理弹窗接入

**Files:**
- Modify: `front/src/views/admin/message/index.vue`

- [ ] **Step 1: 添加 import**

在 `import { ElMessage } from 'element-plus'` 后新增：
```typescript
import { ElMessageBox } from 'element-plus'
import { useFormGuard } from '@/composables/useFormGuard'
```

在 `const form = reactive({ id: undefined, userId: undefined, msgType: 1, msgTitle: '', msgContent: '' })` 之后添加：
```typescript
const { isDirty, reset } = useFormGuard(form)
```

修改 `handleAdd` 和 `handleEdit`：
```typescript
function handleAdd() { isEdit.value = false; Object.assign(form, { id: undefined, userId: undefined, msgType: 1, msgTitle: '', msgContent: '' }); reset(); dialogVisible.value = true }
function handleEdit(row: any) { isEdit.value = true; Object.assign(form, row); reset(); dialogVisible.value = true }
```

修改 el-dialog 模板：
```html
<el-dialog v-model="dialogVisible" :title="isEdit?'编辑消息':'发送消息'" width="550px" :before-close="handleDialogClose">
```

添加 `handleDialogClose`：
```typescript
async function handleDialogClose(done: () => void) {
  if (!isDirty()) return done()
  try {
    await ElMessageBox.confirm('有未保存的更改，确定关闭吗？', '离开确认', { type: 'warning' })
    done()
  } catch { /* 取消关闭 */ }
}
```

在 `handleSave` 中 `ElMessage.success(...)` 之后调用 `reset()`。

---

### Task 11: 全量编译验证

**Files:**
- Check: all modified files compile

- [ ] **Step 1: TypeScript 类型检查**

```bash
cd front && npx vue-tsc --noEmit --strict 2>&1
```
Expected: No type errors.

- [ ] **Step 2: 构建验证**

```bash
cd front && npx vite build 2>&1 | tail -20
```
Expected: Build succeeds with no errors.
