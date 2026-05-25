# Stitch 专用专业 UI 设计提示词

> 生成日期：2026-05-25
> 基于财富管理服务平台后端代码全量分析产出

---

## 项目概述

**项目名称：** 财富管理服务平台后台管理系统（Wealth Service Platform Admin）

**项目定位：** 面向理财/贵金属交易场景的企业级后台管理系统，覆盖产品管理、行情监控、交易委托、资讯消息、用户管理、权限控制等全链路业务。

**风格要求：** 现代、简洁、响应式、企业级/产品级。采用左侧导航 + 右侧内容区的经典后台布局。配色以深蓝色（#1a365d）为主色调，白色为底色，辅以绿色（涨）和红色（跌）表示行情变化。

**技术约束：** Vue 3 + Element Plus + TypeScript。所有接口通过 Gateway（端口 8080）转发。

---

## 一、布局结构

### 1.1 全局布局 (AppLayout)

- **左侧导航栏**（宽度 220px，可折叠至 64px）
  - Logo 区域：显示系统名称"财富管理平台" + 图标
  - 菜单项：分组导航
  - 当前激活菜单高亮
- **顶部导航栏**
  - 左侧：面包屑导航
  - 右侧：全屏切换按钮、消息通知图标（未读数量徽标）、用户头像下拉菜单（个人信息/退出登录）
- **内容区域**：右侧主内容区，标签页风格 Tab 切换（可关闭标签页）
- **页脚**：版本号信息

### 1.2 路由结构（对应导航菜单）

```
/                              → 工作台/首页（仪表盘）
/system                        → 系统管理（分组）
  /system/admin                → 管理员管理
  /system/role                 → 角色管理
  /system/resource             → 资源管理
  /system/admin-role           → 管理员角色关联
  /system/role-resource        → 角色资源关联
/user                          → 用户管理
  /user/list                   → 用户列表
/product                       → 产品管理（分组）
  /product/list                → 产品列表
  /product/market-data         → 行情数据
  /product/favorites           → 用户自选
/trade                         → 交易管理
  /trade/orders                → 交易委托
/message                       → 消息管理（分组）
  /message/list                → 站内消息
  /message/news                → 财经资讯
/search                        → 产品搜索（全局搜索入口）
```

---

## 二、登录页面

### 页面：Login

- **路径：** /login
- **布局：** 居中卡片式，背景为渐变色或抽象金融图形
- **表单字段：**

| 字段 | 类型 | 必填 | 校验规则 |
|------|------|------|---------|
| 用户名 | 文本输入框 | 是 | 不能为空 |
| 密码 | 密码输入框（带切换可见性） | 是 | 不能为空 |
| 验证码 | 文本输入框 + 图片验证码 | 否 | 4位字符 |
| 验证码 Key | 隐藏字段 | - | 系统自动获取 |

- **交互：**
  - 点击"获取验证码"图片可刷新
  - 登录按钮加载态（loading）
  - 登录成功后跳转至工作台
- **接口：**
  - `GET /captcha` — 获取验证码（返回 captchaKey + captchaImage Base64）
  - `POST /system/umsAdmin/login` — 管理员登录（默认 admin/admin123）

---

## 三、工作台首页（Dashboard）

### 页面：/ （Workbench）

- **统计卡片行（4个统计卡片）：**
  1. 产品总数 — 调用产品列表总数
  2. 今日委托单数 — 交易订单统计
  3. 待处理消息数 — 未读消息数
  4. 在线用户数 — 占位

- **实时行情看板：**
  - 采用卡片网格展示所有产品实时行情（SSE 推送）
  - 每个卡片显示：产品名称、产品编码、当前价格、涨跌幅（红色/绿色）
  - 价格实时更新，涨跌幅动画过渡
  - SSE 端点：`GET /product/wea-market-data/sse`

- **最近资讯列表：**
  - 显示最近 5 条财经资讯（标题 + 发布时间）
  - 点击跳转资讯详情

---

## 四、系统管理模块

### 4.1 管理员管理

**页面：/system/admin**

#### 4.1.1 搜索/筛选栏

| 字段 | 类型 | 说明 |
|------|------|------|
| 用户名 | 文本输入框 | 模糊搜索 |
| 状态 | 下拉选择 | 全部/正常/禁用 |

#### 4.1.2 表格（分页）

| 列名 | 数据类型 | 说明 |
|------|---------|------|
| ID | Long | 自增主键 |
| 用户名 | String | 登录账号 |
| 昵称 | String | 显示名称 |
| 邮箱 | String | Email |
| 头像 | Image | 头像图片链接 |
| 状态 | Enum | 0=禁用（红色标签）/ 1=正常（绿色标签） |
| 创建时间 | DateTime | yyyy-MM-dd HH:mm:ss |
| 最后登录时间 | DateTime | 最后登录时间 |
| 操作 | Button Group | 编辑 / 删除 |

#### 4.1.3 新增/编辑弹窗

**表单字段：**

| 字段 | 类型 | 必填 | 校验规则 |
|------|------|------|---------|
| 用户名 | 文本输入框 | 是 | 不能为空 |
| 密码 | 密码输入框 | 是（新增） | 不能为空，编辑时不显示 |
| 昵称 | 文本输入框 | 否 | 最多50字符 |
| 邮箱 | 文本输入框 | 否 | Email格式，最多64字符 |
| 状态 | 开关/单选 | 否 | 0禁用 1正常 |
| 头像 | URL输入/图片上传 | 否 | 最多255字符 |

- **接口：** `POST /system/umsAdmin`（新增）、`PUT /system/umsAdmin/{id}`（修改）、`DELETE /system/umsAdmin/{id}`（删除）
- **分页：** `GET /system/umsAdmin/page?pageNum=&pageSize=`

### 4.2 角色管理

**页面：/system/role**

#### 4.2.1 表格列

| 列名 | 说明 |
|------|------|
| ID | 自增主键 |
| 角色名称 | name |
| 描述 | description |
| 状态 | 0禁用/1正常 |
| 排序 | sort（数值） |
| 创建时间 | createTime |
| 操作 | 编辑/删除 |

#### 4.2.2 新增/编辑弹窗

| 字段 | 类型 | 必填 | 校验 |
|------|------|------|------|
| 角色名称 | 文本输入框 | 是 | 不能为空 |
| 描述 | 文本域 | 否 | 最多200字符 |
| 状态 | 开关 | 否 | 0/1 |
| 排序 | 数字输入框 | 否 | 0-9999 |

- **接口：** 标准 CRUD：`GET /system/umsRole/page`、`POST /system/umsRole`、`PUT /system/umsRole/{id}`、`DELETE /system/umsRole/{id}`

### 4.3 资源管理

**页面：/system/resource**

#### 4.3.1 表格列

| 列名 | 说明 |
|------|------|
| ID | 自增主键 |
| 资源名称 | name |
| 资源URL | url（API路径） |
| 描述 | description |
| 分类ID | categoryId |
| 创建时间 | createTime |
| 操作 | 编辑/删除 |

#### 4.3.2 新增/编辑弹窗

| 字段 | 类型 | 必填 | 校验 |
|------|------|------|------|
| 资源名称 | 文本输入框 | 是 | 不能为空 |
| 资源URL | 文本输入框 | 是 | 不能为空 |
| 描述 | 文本域 | 否 | 最多200字符 |
| 分类ID | 数字输入框 | 是 | 不能为空 |

- **接口：** 标准 CRUD

### 4.4 管理员-角色关联

**页面：/system/admin-role**

#### 4.4.1 表格列

| 列名 | 说明 |
|------|------|
| ID | 自增主键 |
| 管理员ID | adminId |
| 角色ID | roleId |
| 操作 | 编辑/删除 |

#### 4.4.2 新增/编辑弹窗

| 字段 | 类型 | 必填 | 校验 |
|------|------|------|------|
| 管理员ID | 数字输入框/下拉选择 | 是 | 不能为空 |
| 角色ID | 数字输入框/下拉选择 | 是 | 不能为空 |

### 4.5 角色-资源关联

**页面：/system/role-resource**

- 与 4.4 相同模式，字段为 roleId + resourceId

---

## 五、用户管理模块

### 页面：/user/list

#### 5.1 搜索/筛选栏

| 字段 | 类型 | 说明 |
|------|------|------|
| 用户名 | 文本输入框 | 模糊搜索 |
| 手机号 | 文本输入框 | 精确搜索 |
| 状态 | 下拉选择 | 全部/正常/禁用 |

#### 5.2 操作按钮行

- 「新增用户」按钮 → 弹出新增表单
- 「批量删除」按钮 → 勾选多条后出现，确认弹窗

#### 5.3 表格列

| 列名 | 数据类型 | 说明 |
|------|---------|------|
| 多选框 | checkbox | 批量操作 |
| ID | Long | 自增主键 |
| 账号 | String | 用户名 |
| 昵称 | String | 显示名称 |
| 手机号 | String | 1开头的11位手机号 |
| 头像 | Image | 头像 |
| 状态 | Enum | 0=禁用（红色）/ 1=正常（绿色） |
| 创建时间 | DateTime | yyyy-MM-dd HH:mm:ss |
| 操作 | Button Group | 编辑 / 删除 |

#### 5.4 新增/编辑弹窗

| 字段 | 类型 | 必填 | 校验规则 |
|------|------|------|---------|
| 用户名 | 文本输入框 | 是 | 不能为空 |
| 密码 | 密码输入框 | 是（新增） | 不能为空 |
| 昵称 | 文本输入框 | 否 | 最多50字符 |
| 手机号 | 文本输入框 | 否 | 正则 `^1[3-9]\d{9}$` |
| 头像 | URL/上传 | 否 | 最多255字符 |
| 状态 | 开关 | 否 | 0/1 |

#### 5.5 操作流程

- **用户注册**（前台）：`POST /user/register`
- **用户登录**（前台）：`POST /user/login` → 返回 JWT Token + userId + nickname
- **重置密码**：`POST /user/resetPassword`（需传 userId + 新密码）
- **修改用户**：`PUT /user/{id}`（禁止通过此接口修改密码）
- **批量删除**：`DELETE /user/batch`（传ID数组）

---

## 六、产品管理模块

### 6.1 产品列表

**页面：/product/list**

#### 6.1.1 搜索/筛选栏

| 字段 | 类型 | 说明 |
|------|------|------|
| 产品名称 | 文本输入框 | 模糊搜索 |
| 产品类型 | 下拉选择 | 全部 / 各类型编码（0-999） |
| 状态 | 下拉选择 | 全部 / 正常 / 停售 |

#### 6.1.2 操作按钮行

- 「创建产品」按钮
- 「同步到 ES」按钮 → `POST /product/wea-product/syncES`（手动同步所有产品到 Elasticsearch）

#### 6.1.3 表格列

| 列名 | 数据类型 | 说明 |
|------|---------|------|
| ID | Long | 主键 |
| 产品名称 | String | 产品全称 |
| 产品编码 | String | 唯一编码 |
| 产品类型 | Integer | 类型编号（需字典显示） |
| 价格 | BigDecimal | 当前价格，保留2位小数 |
| 涨跌幅 | BigDecimal | riseFall |
| 涨跌率 | BigDecimal | riseFallRate（百分比显示） |
| 状态 | Enum | 0=停售/1=在售 |
| 排序 | Integer | 展示排序号 |
| 创建时间 | DateTime | |
| 更新时间 | DateTime | |
| 操作 | Button Group | 编辑 / 删除 |

#### 6.1.4 新增/编辑弹窗

| 字段 | 类型 | 必填 | 校验规则 |
|------|------|------|---------|
| 产品名称 | 文本输入框 | 是 | 不能为空 |
| 产品编码 | 文本输入框 | 是 | 不能为空 |
| 产品类型 | 数字输入框/下拉 | 是 | 0-999 |
| 价格 | 金额输入框 | 是 | 不能为空 |
| 涨跌幅 | 金额输入框 | 否 | |
| 涨跌率 | 百分比输入框 | 否 | |
| 状态 | 开关 | 否 | 0/1 |
| 排序 | 数字输入框 | 否 | 0-9999 |

#### 6.1.5 操作流程

- **创建产品** → 自动同步产品到 ES（Elasticsearch）
- **更新产品** → 同时更新 ES 索引
- **删除产品** → 同时删除 ES 索引
- **分页查询**：`GET /product/wea-product/page?productType=&pageNum=&pageSize=`

### 6.2 行情数据

**页面：/product/market-data**

#### 6.2.1 行情总览

- **实时行情看板**（顶部区域）：
  - 每个产品一个卡片，2秒自动刷新（SSE推送）
  - 卡片内容：

| 字段 | 数据类型 | 说明 |
|------|---------|------|
| 产品编码 | String | productCode |
| 当前价格 | BigDecimal | currentPrice，红色=涨/绿色=跌 |
| 开盘价 | BigDecimal | openPrice |
| 收盘价 | BigDecimal | closePrice（昨收） |
| 最高价 | BigDecimal | highestPrice |
| 最低价 | BigDecimal | lowestPrice |
| 涨跌幅 | BigDecimal | riseFall |
| 涨跌率 | BigDecimal | riseFallRate（+2.35%格式） |
| 行情时间 | DateTime | marketTime |

- **SSE 推送机制**：`GET /product/wea-market-data/sse` → 建立 SSE 连接，事件名 `market-update`
- 模拟行情每2秒变化一次，高斯随机游走，幅度约0.2%

#### 6.2.2 行情数据管理表格

| 列名 | 说明 |
|------|------|
| ID | 主键 |
| 产品编码 | productCode |
| 当前价格 | currentPrice |
| 开盘价 | openPrice |
| 昨收价 | closePrice |
| 最高价 | highestPrice |
| 最低价 | lowestPrice |
| 涨跌幅 | riseFall |
| 涨跌率 | riseFallRate（百分比） |
| 行情时间 | marketTime |
| 创建时间 | createTime |
| 操作 | 编辑/删除 |

#### 6.2.3 新增/编辑弹窗

| 字段 | 类型 | 必填 | 校验 |
|------|------|------|------|
| 产品编码 | 文本输入框 | 是 | 不能为空 |
| 当前价格 | 金额输入框 | 是 | 不能为空 |
| 开盘价 | 金额输入框 | 否 | |
| 收盘价 | 金额输入框 | 否 | |
| 最高价 | 金额输入框 | 否 | |
| 最低价 | 金额输入框 | 否 | |
| 涨跌幅 | 金额输入框 | 否 | |
| 涨跌率 | 金额输入框 | 否 | |
| 行情时间 | 日期时间选择器 | 否 | |

### 6.3 用户自选管理

**页面：/product/favorites**

#### 6.3.1 搜索栏

| 字段 | 类型 |
|------|------|
| 用户ID | 数字输入框 |

#### 6.3.2 表格列

| 列名 | 说明 |
|------|------|
| ID | 主键 |
| 用户ID | userId |
| 产品编码 | productCode |
| 创建时间 | createTime |
| 操作 | 编辑/删除 |

> 注意：自选关注不允许重复（同 userId + productCode 唯一），接口返回"已关注该产品，请勿重复添加"。
> 物理删除（无逻辑删除），无 del_flag 和 update_time 列。

#### 6.3.3 新增弹窗

| 字段 | 类型 | 必填 | 校验 |
|------|------|------|------|
| 用户ID | 数字输入框 | 是 | 不能为空 |
| 产品编码 | 文本输入框 | 是 | 不能为空 |

---

## 七、交易管理模块

### 页面：/trade/orders

#### 7.1 搜索/筛选栏

| 字段 | 类型 | 说明 |
|------|------|------|
| 用户ID | 数字输入框 | 按用户筛选 |
| 订单状态 | 下拉选择 | 全部/已提交(1)/已成交(2)/已撤销(3) |

#### 7.2 操作按钮行

- 「创建委托单」按钮

#### 7.3 表格列

| 列名 | 数据类型 | 说明 |
|------|---------|------|
| ID | Long | 主键 |
| 订单编号 | String | orderNo（ORDER_ + 12位UUID） |
| 用户ID | Long | 委托用户 |
| 产品编码 | String | productCode |
| 交易类型 | Enum | 1=买入（红色）/ 2=卖出（绿色） |
| 委托价格 | BigDecimal | entrustPrice |
| 委托数量 | Integer | entrustNum |
| 订单状态 | Enum + Tag | 已提交(1)=蓝色 / 已成交(2)=绿色 / 已撤销(3)=灰色 |
| 创建时间 | DateTime | |
| 更新时间 | DateTime | |
| 操作 | Button Group | 查看/编辑/删除/撤销 |

#### 7.4 创建委托单弹窗

| 字段 | 类型 | 必填 | 校验规则 |
|------|------|------|---------|
| 用户ID | 数字输入框 | 是 | 不能为空 |
| 产品编码 | 文本输入框 | 是 | 不能为空 |
| 交易类型 | 单选按钮/下拉 | 是 | 1=买入 / 2=卖出 |
| 委托价格 | 金额输入框 | 是 | 不能为空 |
| 委托数量 | 数字输入框 | 是 | 不能为空，正整数 |
| 幂等键 | 隐藏字段 | 否 | 客户端UUID自动生成，防重复提交 |

#### 7.5 状态更新弹窗

| 字段 | 类型 | 必填 | 校验 |
|------|------|------|------|
| 订单状态 | 下拉选择 | 是 | 合法状态转换：已提交→已成交 / 已提交→已撤销 |

> **状态机规则（前端需禁用非法选项）：**
>
> - 已提交(1) → 可转为 已成交(2) 或 已撤销(3)
> - 已成交(2) → 终态，不可再变更
> - 已撤销(3) → 终态，不可再变更
> - 更新状态接口：`PUT /trade/wea-trade-order/{id}/status`

#### 7.6 业务逻辑

- 创建订单时自动生成订单编号 `ORDER_` + 12位随机字符
- 初始状态为"已提交"（1）
- 订单创建完成后通过 contract 接口自动发送站内消息通知用户
- 幂等键机制防止重复提交（Redis 24小时有效期）
- 删除为物理删除（removeById）

---

## 八、消息管理模块

### 8.1 站内消息

**页面：/message/list**

#### 8.1.1 搜索栏

| 字段 | 类型 | 说明 |
|------|------|------|
| 用户ID | 数字输入框 | 按接收用户筛选 |

#### 8.1.2 表格列

| 列名 | 数据类型 | 说明 |
|------|---------|------|
| ID | Long | 主键 |
| 用户ID | Long | 接收消息的用户 |
| 消息类型 | Enum + Tag | 1=系统（蓝色）/ 2=交易（橙色）/ 3=风控（红色） |
| 消息标题 | String | msgTitle |
| 消息内容 | String | msgContent（可展开显示） |
| 已读标记 | Enum | 0=未读（加粗）/ 1=已读 |
| 创建时间 | DateTime | |
| 操作 | Button | 查看详情 / 删除 |

#### 8.1.3 新增弹窗

| 字段 | 类型 | 必填 | 校验 |
|------|------|------|------|
| 用户ID | 数字输入框 | 是 | 不能为空 |
| 消息类型 | 下拉选择 | 否 | 1系统/2交易/3风控 |
| 消息标题 | 文本输入框 | 是 | 不能为空 |
| 消息内容 | 文本域 | 是 | 不能为空 |

> 创建时 readFlag 自动设为 0（未读）

### 8.2 财经资讯

**页面：/message/news**

#### 8.2.1 搜索栏

| 字段 | 类型 |
|------|------|
| 资讯类型 | 下拉选择（全部 / 各类型） |

#### 8.2.2 表格列

| 列名 | 数据类型 | 说明 |
|------|---------|------|
| ID | Long | 主键 |
| 标题 | String | title |
| 内容 | String | content（列表截断显示，点击展开全文） |
| 资讯类型 | Integer | newsType（字典值） |
| 来源 | String | source |
| 状态 | Integer | status |
| 发布时间 | DateTime | publishTime |
| 创建时间 | DateTime | createTime |
| 操作 | Button Group | 编辑 / 删除 |

#### 8.2.3 新增/编辑弹窗

| 字段 | 类型 | 必填 | 校验 |
|------|------|------|------|
| 标题 | 文本输入框 | 是 | 不能为空 |
| 内容 | 富文本编辑器/文本域 | 是 | 不能为空 |
| 资讯类型 | 下拉选择/数字输入 | 否 | |
| 来源 | 文本输入框 | 否 | |
| 状态 | 数字输入框 | 否 | |
| 发布时间 | 日期时间选择器 | 否 | |

---

## 九、产品搜索模块

### 页面：全局搜索入口（可放在顶部导航栏）

#### 9.1 搜索页面 /search

| 字段 | 类型 | 说明 |
|------|------|------|
| 关键词 | 搜索输入框 | 按产品名称/编码搜索 |

#### 9.2 搜索结果

| 列名 | 说明 |
|------|------|
| ID | ES文档ID |
| 产品名称 | 高亮匹配关键词（ik分词） |
| 产品编码 | 精确匹配 |
| 产品类型 | |
| 价格 | |
| 涨跌幅/率 | |
| 状态 | |
| 排序 | |
| 操作 | 查看详情 |

> **降级策略：** ES 不可用时自动降级为 MySQL LIKE 查询（productName 或 productCode 模糊匹配）

#### 9.3 ES 同步

- 创建/更新产品时自动同步到 ES
- 手动同步：`POST /product/wea-product/syncES`（同步全部产品到 ES）

---

## 十、通用组件规范

### 10.1 表格统一规范

- 分页组件：显示总数、每页条数选择（10/20/50/100）、页码跳转
- 加载态：表格加载时显示 skeleton 骨架屏
- 空状态：数据为空时显示空状态插画 + "暂无数据"提示
- 操作列固定右侧，宽度 150-200px
- 长文本省略号（...），鼠标悬停 tooltip 显示完整内容

### 10.2 弹窗统一规范

- 新增/编辑使用 Dialog 弹窗
- 删除操作使用 Popconfirm 二次确认："确定删除该记录吗？此操作不可撤销。"
- 表单提交时显示 loading，提交成功后自动关闭弹窗并刷新表格
- 表单校验失败时在对应字段下方显示红色错误提示

### 10.3 状态标签配色

| 状态 | 标签颜色 |
|------|---------|
| 正常/已成交/已读 | 绿色（success） |
| 禁用/停售/已撤销 | 灰色（info） |
| 已提交/未读 | 蓝色（primary） |
| 风控 | 红色（danger） |
| 交易类型-买入 | 红色（danger） |
| 交易类型-卖出 | 绿色（success） |

### 10.4 行情颜色规范

- **涨**（当前价格 >= 收盘价）：红色（#e74c3c），带 ▲ 上箭头
- **跌**（当前价格 < 收盘价）：绿色（#27ae60），带 ▼ 下箭头
- 涨跌率格式：+2.35% / -1.28%

### 10.5 响应式断点

- ≥1200px：全宽显示（max-width 1400px，居中）
- 992px-1199px：表格列适当隐藏次要字段
- <992px：导航栏折叠为图标模式

### 10.6 错误处理

- 401 → 跳转登录页
- 403 → 显示"无权限访问"提示页
- 404 → 显示"资源不存在"提示页
- 500 → 显示"服务器异常"提示页 + 错误ID
- 网络错误 → 顶部全局通知 + 重试按钮

---

## 十一、权限控制

- 左侧菜单根据当前管理员角色动态显示（RBAC）
- 按钮级权限：编辑/删除按钮根据资源权限控制显示/隐藏
- 权限校验流程：
  1. 管理员登录 → 获取 JWT Token
  2. 每次请求在 Header 携带 `Authorization: Bearer <token>`
  3. Gateway 拦截校验 → 转发到具体服务
  4. 服务端 LoginInterceptor 校验白名单（无需登录的接口）

**无需登录的白名单接口：**

- `POST /system/umsAdmin/login` — 管理员登录
- `GET /captcha` — 验证码
- `POST /user/login` — 前台用户登录
- `POST /user/register` — 前台用户注册
- `GET /product/wea-market-data/sse/**` — SSE 实时行情

---

## 十二、JWT 与 Token 管理

- **登录返回：** access_token（短时效）+ refresh_token（长时效，7天）
- **Token 刷新：** `POST /system/umsAdmin/refresh`（传 refresh_token 换取新 token 对）
- **httpOnly Cookie：** 登录同时设置 `wealth_token` Cookie（防 XSS 窃取）
- **30分钟有效期：** access_token 默认 30 分钟
- **安全特性：** refresh_token 一次性使用，用完即吊销（防重放攻击）

---

## 十三、完整接口清单（供 Stitch 数据对接）

### 13.1 系统管理（前缀 /system）

```
GET    /captcha                               # 获取验证码
POST   /system/umsAdmin/login                 # 管理员登录
POST   /system/umsAdmin/refresh               # 刷新 Token
GET    /system/umsAdmin/{id}                  # 管理员详情
GET    /system/umsAdmin                       # 管理员列表
GET    /system/umsAdmin/page                  # 管理员分页
POST   /system/umsAdmin                       # 新增管理员
PUT    /system/umsAdmin/{id}                  # 修改管理员
DELETE /system/umsAdmin/{id}                  # 删除管理员
GET    /system/umsRole/{id}                   # 角色详情
GET    /system/umsRole                        # 角色列表
GET    /system/umsRole/page                   # 角色分页
POST   /system/umsRole                        # 创建角色
PUT    /system/umsRole/{id}                   # 更新角色
DELETE /system/umsRole/{id}                   # 删除角色
GET    /system/umsResource/{id}               # 资源详情
GET    /system/umsResource                    # 资源列表
GET    /system/umsResource/page               # 资源分页
POST   /system/umsResource                    # 创建资源
PUT    /system/umsResource/{id}               # 更新资源
DELETE /system/umsResource/{id}               # 删除资源
GET    /system/umsAdminRoleRelation/{id}      # 关联详情
GET    /system/umsAdminRoleRelation           # 关联列表
GET    /system/umsAdminRoleRelation/page      # 关联分页
POST   /system/umsAdminRoleRelation           # 创建关联
PUT    /system/umsAdminRoleRelation/{id}      # 更新关联
DELETE /system/umsAdminRoleRelation/{id}      # 删除关联
GET    /system/umsRoleResourceRelation/{id}   # 关联详情
GET    /system/umsRoleResourceRelation        # 关联列表
GET    /system/umsRoleResourceRelation/page   # 关联分页
POST   /system/umsRoleResourceRelation        # 创建关联
PUT    /system/umsRoleResourceRelation/{id}   # 更新关联
DELETE /system/umsRoleResourceRelation/{id}   # 删除关联
```

### 13.2 前台用户管理（前缀 /user）

```
GET    /user/{id}                             # 用户详情
GET    /user                                  # 用户列表
GET    /user/page                             # 用户分页
POST   /user                                  # 新增用户
PUT    /user/{id}                             # 修改用户（禁止改密码）
DELETE /user/{id}                             # 删除用户
DELETE /user/batch                            # 批量删除
POST   /user/register                         # 用户注册
POST   /user/login                            # 用户登录
POST   /user/resetPassword                    # 重置密码
```

### 13.3 产品管理（前缀 /product）

```
GET    /product/wea-product/{id}              # 产品详情
GET    /product/wea-product                   # 产品列表
GET    /product/wea-product/page              # 产品分页(?productType=)
POST   /product/wea-product                   # 创建产品
PUT    /product/wea-product/{id}              # 更新产品
DELETE /product/wea-product/{id}              # 删除产品
POST   /product/wea-product/syncES            # 手动同步ES
GET    /product/wea-market-data/{id}          # 行情详情
GET    /product/wea-market-data               # 行情列表
GET    /product/wea-market-data/page          # 行情分页
GET    /product/wea-market-data/sse           # SSE实时行情推送
POST   /product/wea-market-data               # 创建行情
PUT    /product/wea-market-data/{id}          # 更新行情
DELETE /product/wea-market-data/{id}          # 删除行情
GET    /product/wea-user-favorite/{id}        # 自选详情
GET    /product/wea-user-favorite             # 自选列表(?userId=)
GET    /product/wea-user-favorite/page        # 自选分页
POST   /product/wea-user-favorite             # 添加自选
PUT    /product/wea-user-favorite/{id}        # 更新自选
DELETE /product/wea-user-favorite/{id}        # 删除自选
```

### 13.4 交易管理（前缀 /trade）

```
GET    /trade/wea-trade-order/{id}            # 订单详情
GET    /trade/wea-trade-order                 # 订单列表
GET    /trade/wea-trade-order/page            # 订单分页(?userId=&orderStatus=)
POST   /trade/wea-trade-order                 # 创建委托单
PUT    /trade/wea-trade-order/{id}            # 更新委托单
PUT    /trade/wea-trade-order/{id}/status     # 更新状态（状态机）
DELETE /trade/wea-trade-order/{id}            # 删除委托单
```

### 13.5 消息管理（前缀 /message）

```
GET    /message/wea-message/{id}              # 消息详情
GET    /message/wea-message                   # 消息列表
GET    /message/wea-message/page              # 消息分页(?userId=)
POST   /message/wea-message                   # 创建消息
PUT    /message/wea-message/{id}              # 更新消息
DELETE /message/wea-message/{id}              # 删除消息
GET    /message/wea-news/{id}                 # 资讯详情
GET    /message/wea-news                      # 资讯列表
GET    /message/wea-news/page                 # 资讯分页(?newsType=)
POST   /message/wea-news                      # 创建资讯
PUT    /message/wea-news/{id}                 # 更新资讯
DELETE /message/wea-news/{id}                 # 删除资讯
```

### 13.6 ES 搜索（前缀 /search）

```
POST   /search/product                        # 新增/更新ES文档
GET    /search/product/{id}                   # 查询ES文档
GET    /search/product/search                 # 搜索(?keyword=&page=&size=)
DELETE /search/product/{id}                   # 删除ES文档
```

---

## 十四、后端数据模型汇总

### 14.1 统一返回格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 14.2 分页请求参数

- pageNum: 页码（默认1）
- pageSize: 每页条数（默认10）

### 14.3 分页返回结构

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  }
}
```

### 14.4 关键字典值

| 字段 | 值含义 |
|------|--------|
| status（通用状态） | 0=禁用/停售，1=正常/在售 |
| delFlag（逻辑删除） | 0=未删除，1=已删除 |
| tradeType（交易类型） | 1=买入，2=卖出 |
| orderStatus（订单状态） | 1=已提交，2=已成交，3=已撤销 |
| msgType（消息类型） | 1=系统，2=交易，3=风控 |
| readFlag（已读标记） | 0=未读，1=已读 |
