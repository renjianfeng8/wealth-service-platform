# 更新日志

> 本日志仅记录项目级别的更新、功能新增、架构调整与优化。
> 具体的 Bug 修复细节请参阅 [Bug.md](./Bug.md)。

---

## v1.4.1 (2026-05-12)

### 搜索服务增强

- ES 版本校准至 8.8.2（与 Docker 容器版本对齐）
- 集成 IK 中文分词器，产品名称支持中文分词检索
- 日期字段格式标准化（`date_hour_minute_second_millis`），确保 ES mapping 与 Java 类型一致

### 用户认证优化

- 登录接口返回结构增强，新增 `userId` 字段，前端可直接获取用户标识

### 消息服务优化

- 更新接口支持部分字段传递，适配已读标记等单字段更新场景

---

## v1.4.0 (2026-05-10)

### 双端架构落地：用户前台 front-user + Playwright E2E

新增独立用户门户 `front-user/`，面向普通用户，与现有管理员后台 `front/` 构成完整的双端架构体系。同时配套 Playwright E2E 自动化测试套件，覆盖全部用户端业务场景。

#### 新增：用户前台 (front-user/)

全新的 Vue 3 + TypeScript 企业级金融用户端门户（端口 3001），包含 8 个核心功能模块：

- **登录/注册** — 基于 `sys_user` 表的普通用户 JWT 登录
- **仪表盘** — 用户资产概览与全局数据看板
- **产品中心** — 金融产品浏览与查询
- **实时行情** — 产品市场价格数据实时展示
- **我的自选** — 用户自选产品管理
- **交易委托** — 下单委托与订单历史查看
- **财经资讯** — 资讯列表与详情
- **消息中心** — 站内消息接收与管理
- **个人中心** — 用户信息查看与修改

技术特点：
- 企业级 UI 设计，移动端自适应布局
- Vite 代理配置：`/api` → 网关统一入口
- Pinia 状态管理，与后端 `sys_user` 接口对接
- Vue Router 路由守卫实现未登录自动拦截跳转

#### 新增：Playwright E2E 自动化测试

配置 37 条端到端测试用例，覆盖登录、仪表盘、产品中心、实时行情、我的自选、交易委托、财经资讯、消息中心、个人中心、退出登录、导航菜单共 11 个业务模块。

测试命令：
```bash
cd front-user
npx playwright install chromium
npm run test:e2e
npm run test:e2e:report
```

测试账号：`zhangwei` / `123456`

#### 文档重构

- **Startup.md** — 全面重构启动指南，新增 front-user 启动步骤、双端 E2E 测试说明、双测试账号对照表、全链路验证命令、端口对照表更新
- 双端架构说明：管理员后台（`front/`, 端口 3000）+ 用户前台（`front-user/`, 端口 3001）

### 测试数据补充

在 `init.sql` 中追加 12 张表的完整测试数据，共计 83 条记录，覆盖全业务场景：

| 表名 | 记录数 | 说明 |
|------|:-----:|------|
| `sys_user` | 5 | 前台测试用户（含 zhangwei） |
| `fin_product` | 12 | 不同类别金融产品 |
| `fin_market_data` | 15 | 行情历史数据 |
| `fin_user_favorite` | 5 | 用户自选关联 |
| `fin_trade_order` | 8 | 不同状态交易委托 |
| `fin_news` | 10 | 财经资讯文章 |
| `fin_message` | 8 | 站内消息 |
| `ums_*` | 22 | 后台权限相关 5 表 |

密码统一使用 BCrypt 加密，关键关联字段（`product_code`、`user_id`）保持跨表一致性。

### 项目规范更新

- **CLAUDE.md** — 全面同步项目最新状态：
  - 前端技术栈版本锁定
  - BaseEntity 继承规则表
  - 基础设施类说明表
  - 常见代码模式指南
  - 健康检查清单和高频问题
  - 端口表与网关路由说明

---

## v1.3.0 (2026-05-09)

### 数据库全量扫描 & init.sql 重建

完整扫描 `finance` 数据库全部 12 张表的实际结构，重新生成并覆盖 `init.sql`，确保初始化脚本与数据库完全一致。

- 使用 `information_schema` + `SHOW CREATE TABLE` 全面扫描所有表的字段、类型、默认值、注释、索引
- 覆盖全部 12 张表
- 所有表字符集统一为 `utf8mb4`
- 所有字段补全完整中文注释

---

## v1.2.0 (2026-05-09)

### 全链路连通测试

首次完成完整的前后端全链路 E2E 自动化测试，覆盖 8 个微服务 + 网关 + 前端，测试项 35 项全部通过。

#### 测试覆盖范围

- **基础设施**: Nacos、MySQL、Redis、Docker 容器连通性验证
- **页面加载**: 标题渲染、白屏检测、未登录自动跳转
- **登录流程**: 表单填写、JWT Token 获取、登录后跳转控制台
- **菜单导航**: 12 个页面全量路由导航
- **API 请求**: 10 个业务接口通过网关调用
- **前端代理**: Vite `/api` 代理到网关全链路验证
- **错误检查**: 浏览器控制台 JS 错误、路由跳转报错、页面内容渲染

#### 自动化测试

- 新增 `e2e-test.mjs` — Playwright + Node.js 全链路测试脚本
- 支持基础设施自动检测、API 连通测试、真实浏览器交互、详细报告生成

#### 测试结果

| 类别 | 通过 |
|------|:---:|
| 基础设施检查 | 3/3 |
| 后端 API 直接测试 | 1/1 |
| 页面加载测试 | 3/3 |
| 登录流程测试 | 3/3 |
| 菜单导航测试 | 12/12 |
| API 请求测试 | 10/10 |
| 前端代理测试 | 3/3 |
| 错误检查 | 3/3 |
| **合计** | **35/35 (100%)** |

#### 运行环境

JDK 21.0.3 / SpringBoot 3.3.5 / SpringCloud 2023.0.3 \
Vue 3.5.13 / Vite 6.3.1 / Element Plus 2.9.7 \
Nacos 2.x / Redis 5.0.14 / MySQL 8.0.37 / ES 8.8.2 \
Playwright 1.59.1 | Node.js 22.14.0

---

## v1.1.0 (2026-05-08)

### 全栈健康检查与优化

#### 基础设施

- 父 pom 移除全局 `spring-boot-starter-web`，避免 gateway 与 WebFlux 冲突
- 移除根 pom 中的 Knife4j 依赖声明（不与 Gateway WebFlux 兼容）
- 业务模块添加 `spring-boot-maven-plugin` 确保 JAR 可执行
- gateway 添加 `spring-cloud-starter-loadbalancer` 依赖

#### 网关 (finance-gateway)

- 添加 7 条服务路由规则（system/user/product/account/trade/message/search）
- 配置全局 CORS，支持跨域访问

#### 搜索模块 (finance-search)

- 添加 `scanBasePackages` 确保通用 Bean 可扫描
- 通用配置类添加条件加载注解，兼容无 MyBatis-Plus/Redis 的模块

#### 后端核心

- 拦截器路径匹配适配 AntPathMatcher
- JWT 工具类增强
- 新增 14 个 Service 方法事务注解
- 11 个 DTO 添加参数校验注解
- 12 个 Entity 统一继承 BaseEntity
- 6 个业务模块新增分页查询接口
- 全局异常处理器补充校验异常处理
- Controller getById 方法空值统一返回 404

#### 前端 (Vue 3 + Element Plus)

- TypeScript 类型声明完善
- 路由补充 RouteMeta 类型和 404 通配路由
- 列表页统一空数据展示与分页重置
- 表单校验增强（手机号、邮箱、密码等）
- 登录后跳转保留 redirect 参数

### 配置安全

- application.yml 数据库密码改用环境变量引用
- ES 地址改用环境变量引用
- Nacos 配置中心统一管理业务配置

### 项目规范

- CLAUDE.md 新增健康检查规则章节
- 记录已知高频问题与排查清单

---

## v1.0.0 (2026-05-04)

- 初始化项目，搭建基于 SpringCloud Alibaba 的微服务架构
- 集成 Nacos 注册中心与配置中心
- 完成用户、账户、产品、交易等核心模块开发
- 集成 Spring Cloud Gateway 网关
- 实现 JWT 无状态认证
- 集成 Elasticsearch 全文检索服务

### 项目结构

- finance-mid-platform (pom)
  - finance-common — 公共依赖模块
  - finance-gateway — 网关服务
  - finance-system — 系统服务（后台权限管理）
  - finance-user — 用户服务
  - finance-account — 账户服务
  - finance-product — 产品服务
  - finance-trade — 交易服务
  - finance-message — 消息服务
  - finance-search — 搜索服务（ES）
