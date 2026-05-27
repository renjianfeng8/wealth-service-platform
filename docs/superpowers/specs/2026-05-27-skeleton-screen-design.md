# 骨架屏实现方案

## 背景

首页和个人资料页缺少加载骨架屏：首页行情数据为硬编码，个人资料页 4 个 API 并行请求期间页面显示空表单。

## 改动范围

### 首页（home/index.vue）

#### 行情数据动态化
- 移除硬编码 `marketItems`，改为调用 `getMarketDataList()` API
- 按 API 返回数量渲染全部行情卡片（grid 自适应列数）
- 增加 `loading` 状态管理

#### 骨架屏
- 使用 `<el-skeleton animated>`，自定义 template 模拟 4 张行情卡片
- 每个骨架卡片包含：名称（短条）、价格（长条）、涨跌幅（中条）
- 布局与真实卡片一致（`el-row :gutter="16"`，`el-col :xs="12" :sm="6"`）

#### 状态处理
| 状态 | 表现 |
|------|------|
| loading | 4 张骨架卡片 |
| 成功且数据为空 | `<el-empty description="暂无行情数据" />` |
| 成功且有数据 | 行情卡片列表 |
| API 异常 | 静默降级为空（catch 中置空数组） |

### 个人资料页（profile/index.vue）

#### 骨架屏
- 使用 `<el-skeleton animated>` 覆盖两个核心区块（左侧头像卡 + 右侧个人信息表单）
- 左侧头像卡骨架：72px 圆形 + 2 条文字线 + 分割线 + 3 个数字占位
- 右侧表单骨架：3 行 input 占位（label + 矩形条）+ 按钮占位
- 安全设置卡片保持静态，不作骨架处理

#### 加载状态
- 使用统一 `loading` flag 控制整个页面骨架屏
- `onMounted` 中并行发起 4 个请求，期间展示骨架屏
- 请求全部完成后关闭 loading，一次性展示完整内容（避免分段出现引起的闪烁）
- 各请求异常在 catch 中静默处理，不阻塞其他请求

### 引入的 API

| 页面 | API |
|------|-----|
| 首页行情 | `getMarketDataList()` → `GET /product/wea-market-data` |
| 个人资料（已有） | `getUserInfo()`, `getFavoritePage()`, `getTradeOrderPage()`, `getMessagePage()` |

## 技术方案

- 框架：Element Plus `<el-skeleton>` 组件（`animated` 属性启用扫光动画）
- 风格：沿用产品中心页现有 skeleton 使用方式，保持项目一致
- 无需新增依赖

## 不涉及

- 安全设置卡（不变，保留展示）
- 首页 Hero 区域和特色卡片（纯静态，无 API）
- 路由、权限、后端接口

## 工作量估算

| 文件 | 改动类型 | 估算行数 |
|------|----------|----------|
| `front/src/views/home/index.vue` | template + script + style | ~50 行 |
| `front/src/views/profile/index.vue` | template + script + style | ~60 行 |
