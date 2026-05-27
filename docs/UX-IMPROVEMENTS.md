# 用户体验优化清单

> 基于 2026-05-27 前端全量扫描整理，按优先级分组。每个条目包含问题描述、影响面、建议方案。

---

## P0 — 高优先级（工作量小、感知强）

### P0-1 页面切换缺少加载进度指示

**问题：** 路由跳转时没有顶部进度条或全局 loading，API 慢时页面卡住不动，用户不知道是否在加载。

**建议方案：** 引入 nprogress，在 `router.beforeEach` 中调用 `NProgress.start()`，在 `router.afterEach` 中调用 `NProgress.done()`。

**影响面：** 全局体验，所有页面切换都有感知提升。

**工作量：** 小。安装依赖 → 路由守卫加 2 行调用 → 全局样式引入。

---

### P0-2 首页行情数据为静态硬编码

**文件：** `front/src/views/home/index.vue`

**问题：** 首页展示的 4 个指数涨跌数据是硬编码的静态值（约第 100-105 行），没有调用任何 API，刷新后始终相同，与产品中心/实时行情页面数据不同步。

**建议方案：** 调用 `getMarketDataList` 接口获取前 N 条行情数据替代静态值。

**影响面：** 首页（公开访问，用户第一印象）。

**工作量：** 小。替换硬编码数组为 API 调用。

---

### P0-3 首页和个人资料页缺少加载骨架屏

**问题：** 产品中心、消息、自选等页面使用了 `el-skeleton` 骨架屏，但首页（`home/index.vue`）和个人资料页（`profile/index.vue`）在数据加载期间没有任何加载指示器，可能出现一闪而过的空白。

**建议方案：** 在首页和个人资料页的 loading 状态下添加 `el-skeleton` 组件。

**影响面：** 首页 + 个人资料页。

**工作量：** 小。参考产品中心页的骨架屏实现。

---

### P0-4 通知铃铛未读数量硬编码

**文件：** `front/src/layouts/Navbar.vue` 第 20 行

**问题：** 导航栏右上角的通知 badge 固定显示 `:value="3"`，未连接真实的未读消息计数 API。

**建议方案：** 调用 `getMessagePage` 或新增未读计数接口；或短期内先隐藏 badge 数值，避免上生产后始终显示为 3。

**影响面：** 所有页面（Navbar 全局组件）。

**工作量：** 小。新增接口或隐藏硬编码值。

---

### P0-5 路由切换缺少过渡动画

**问题：** 页面切换时生硬跳转，没有淡入淡出或滑动效果。

**建议方案：** 在 App.vue 的 `<router-view />` 外层包裹 `<transition name="fade">`，配合全局 CSS 过渡。

**影响面：** 全局。

**工作量：** 极小。App.vue + 几行 CSS。

---

## P1 — 中等优先级

### P1-1 缺少全局错误边界

**问题：** 没有设置 `app.config.errorHandler` 或 ErrorBoundary 组件。某个组件渲染报错可能导致整页白屏，且控制台也无明显提示。

**建议方案：** 创建 ErrorBoundary 组件包裹 `<router-view>`，捕获渲染异常并显示错误提示 + 重试按钮。

**影响面：** 全局，防止白屏。

**工作量：** 中。创建 ErrorBoundary 组件 → app.config.errorHandler 注册。

---

### P1-2 缺少 403 权限提示页

**问题：** 非 admin 用户访问 `/admin/*` 路由时，导航守卫静默重定向到 `/home`，用户不知道发生了什么（没有提示，也没有专门的拒绝访问页面）。

**建议方案：** 创建 403 页面组件，重定向前给出提示（如 ElMessage.warning），或直接跳转到 403 页面。

**影响面：** 权限校验流程。

**工作量：** 小。创建 403.vue 页面 → 路由注册 → 修改导航守卫。

---

### P1-3 管理后台仪表盘全部为 Mock 数据

**文件：** `front/src/views/admin/dashboard/index.vue`

**问题：** 每个 ECharts 图表和统计数据均使用 `Math.random()` 生成。资产总值、余额、每日收益、K 线 OHLC 数据全部是假的。管理仪表盘视觉上令人印象深刻但无实际功能。

**建议方案：** 接入真实 API（如 `getMarketDataPage`、`getMarketDataList` 或新增统计接口）替代 Mock 数据。

**影响面：** 管理后台仪表盘。

**工作量：** 大。需要确认后端是否有对应统计接口，若无则新增。

---

### P1-4 SSE 连接未设置 withCredentials

**文件：** `front/src/utils/sse.ts` 第 7-9 行

**问题：** `new EventSource('/api/v1/product/wea-market-data/sse')` 未设置 `withCredentials: true`。如果 API 位于不同 origin（跨域），且后端依赖 httpOnly Cookie 认证，浏览器将不会发送 Cookie，导致 SSE 连接失败。

**建议方案：** 将 `EventSource` 替换为自定义封装，使用 `fetch` + `ReadableStream` 或确认单域名部署无需该配置。若前端通过 nginx 反向代理到后端（同域），则此问题不紧急。

**影响面：** 行情实时推送功能。

**工作量：** 中。

---

### P1-5 后台部分页面使用内联类型映射而非公共函数

**文件：**
- `front/src/views/admin/news/index.vue` 第 17 行：`row.newsType===1?'财经':row.newsType===2?'公告':'其他'`
- `front/src/views/admin/search/index.vue` 第 26 行：`row.productType===1?'股票':row.productType===2?'基金':'其他'`

**问题：** 使用内联三目运算符替代已有的公共格式化函数（`newsTypeText()`、`productTypeText()`），且映射值与 `NEWS_TYPE_OPTIONS` / `PRODUCT_TYPE_OPTIONS` 枚举不一致。新增类型时容易遗漏。

**建议方案：** 替换为 `newsTypeText()` 和 `productTypeText()` 调用。

**影响面：** 后台新闻管理 + 搜索页面。

**工作量：** 极小。

---

## P2 — 低优先级（锦上添花）

### P2-1 记住上次访问页面

**问题：** 刷新浏览器后回到首页，不记得用户上次在哪个页面。

**建议方案：** 在导航守卫中将最后访问路由存入 sessionStorage，刷新后重定向。

---

### P2-2 全局搜索入口（Ctrl+K）

**问题：** 缺少快速搜索产品的快捷键入口。

**建议方案：** 导航栏添加全局搜索框，支持 Ctrl+K 快捷键聚焦，搜索产品/资讯。

---

### P2-3 表单编辑中离开确认

**问题：** 编辑中的表单（如后台 CRUD 编辑弹窗、注册页面）若意外离开，已填写内容丢失。

**建议方案：** 使用 `router.beforeEach` 或 `onBeforeRouteLeave` 守卫，有未保存变更时弹出确认框。

---

### P2-4 暗色模式切换

**问题：** 不支持暗色模式。

**建议方案：** 使用 Element Plus 暗色模式 + CSS 变量切换，Pinia store 持久化用户偏好。

---

### P2-5 用户登录页面直接操作 Pinia store 属性

**文件：** `front/src/views/auth/login/index.vue` 第 106-131 行

**问题：** 直接赋值 `userStore.token`、`userStore.role`、`userStore.userId`，而非调用 `userStore.userLogin()` action。与管理后台登录（正确调用 `userStore.login()`）风格不一致。

**建议方案：** 统一改为调用 store action。

---

### P2-6 首页可添加更多动态交互

**问题：** 首页作为用户第一印象的页面，目前 Hero 区展示静态内容，缺少吸引力。

**建议方案：** Hero 区添加 Animate.css 滚动入场动画、行情数据轮播、打字机效果标语等。

---

## 附录：优化优先级判定矩阵

| 编号 | 项目 | 用户感知 | 工作量 | 风险 |
|------|------|----------|--------|------|
| P0-1 | 路由加载进度条 | 强 | 小 | 低 |
| P0-2 | 首页行情数据接入 | 强 | 小 | 低 |
| P0-3 | 骨架屏缺失页面补齐 | 中 | 小 | 低 |
| P0-4 | 通知未读数硬编码 | 中 | 小 | 低 |
| P0-5 | 路由切换动画 | 中 | 极小 | 低 |
| P1-1 | 全局错误边界 | 弱（出事时强） | 中 | 低 |
| P1-2 | 403 页面 | 中 | 小 | 低 |
| P1-3 | 后台仪表盘 Mock → 真实数据 | 强 | 大 | 中 |
| P1-4 | SSE withCredentials | 条件触发 | 中 | 低 |
| P1-5 | 内联类型映射 | 弱 | 极小 | 低 |
| P2-1~6 | 锦上添花 | 弱 | 小-中 | 低 |
