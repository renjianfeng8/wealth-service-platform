# 表单离开确认与保存反馈设计

## 概述

为前端表单添加两个 UX 优化：
1. **离开确认** — 表单有未保存更改时，离开当前页面/关闭弹窗前弹出确认
2. **保存反馈** — 编辑保存后统一使用 ElMessage.success，配合按钮状态变化提示

## 核心实现：useFormGuard composable

文件：`front/src/composables/useFormGuard.ts`

### API

```typescript
function useFormGuard<T extends Record<string, any>>(form: T): {
  isDirty: () => boolean       // 判断表单是否有未保存更改
  reset: () => void            // 保存成功后调用，更新初始快照
}
```

### 实现逻辑

1. 首次调用时深拷贝 form 初始值存入 `snapshot`
2. `isDirty()` 通过 `JSON.stringify(form) !== snapshot` 比较
3. `reset()` 用当前 form 值更新快照

## 使用场景

### 场景一：独立路由页面

在页面组件中使用 `onBeforeRouteLeave` 守卫：

```typescript
const { isDirty, reset } = useFormGuard(form)

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
  onUnmounted(() => window.removeEventListener('beforeunload', handler))
})

async function handleSave() {
  // ... 调用 API
  ElMessage.success('保存成功')
  reset()
}
```

### 场景二：管理后台弹窗表单

在 el-dialog 的 `before-close` 事件中检查：

```typescript
const { isDirty, reset } = useFormGuard(form)

async function handleDialogClose(done: () => void) {
  if (!isDirty()) return done()
  try {
    await ElMessageBox.confirm('有未保存的更改，确定关闭吗？', '离开确认', { type: 'warning' })
    done()
  } catch { /* 取消关闭 */ }
}
```

## 涉及页面

### 独立路由页面（添加离开确认）

| 页面 | 文件 |
|------|------|
| 个人资料编辑 | `views/profile/index.vue` |
| 用户注册 | `views/register/index.vue` |

### 弹窗表单（添加关闭确认）

| 页面 | 文件 |
|------|------|
| 管理后台-产品 | `views/admin/product/index.vue` |
| 管理后台-用户 | `views/admin/user/index.vue` |
| 管理后台-管理员 | `views/admin/system/admin/index.vue` |
| 管理后台-角色 | `views/admin/system/role/index.vue` |
| 管理后台-资源 | `views/admin/system/resource/index.vue` |
| 管理后台-资讯 | `views/admin/news/index.vue` |
| 管理后台-消息 | `views/admin/message/index.vue` |

### 保存提示检查

上述所有页面当前都已存在 `ElMessage.success` 调用，无需额外修改。

## 不涉及的页面

- 交易页面（trade）已有确认流程，不重复拦截
- 自选管理（favorite）取消已有确认，不重复
- 登录页（login）无需离开确认
- 首页/行情/资讯等纯展示页面无表单
