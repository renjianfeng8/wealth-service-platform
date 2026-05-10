# 更新日志

## v1.4.0 (2026-05-10)

### 双端架构落地：用户前台 front-user + Playwright E2E

新增独立用户门户 `front-user/`，面向普通用户，与现有管理员后台 `front/` 构成完整的双端架构体系。同时配套 Playwright E2E 自动化测试套件，覆盖全部用户端业务场景。

#### 新增：用户前台 (front-user/)

全新的 Vue 3 + TypeScript 企业级金融用户端门户（端口 3001），包含 8 个核心功能模块：

- **登录/注册** — 基于 `sys_user` 表的普通用户 JWT 登录，包含错误密码验证
- **仪表盘** — 用户资产概览与全局数据看板
- **产品中心** — 金融产品浏览与查询
- **实时行情** — 产品市场价格数据实时展示
- **我的自选** — 用户自选产品管理（基于 `fin_user_favorite`）
- **交易委托** — 下单委托与订单历史查看（基于 `fin_trade_order`）
- **财经资讯** — 资讯列表与详情（基于 `fin_news`）
- **消息中心** — 站内消息接收与管理（基于 `fin_message`）
- **个人中心** — 用户信息查看与修改

技术特点：
- 企业级 UI 设计，移动端自适应布局（rem/vw 响应式方案）
- Vite 代理配置：`/api` → `http://localhost:8080`（网关统一入口）
- Pinia 状态管理（useUserStore），与后端 `sys_user` 接口对接
- Vue Router 路由守卫实现未登录自动拦截跳转

#### 新增：Playwright E2E 自动化测试

为 front-user 配置 37 条 Playwright 端到端测试用例，覆盖 11 个业务模块：

| 测试模块 | 测试内容 |
|---------|---------|
| 登录测试 | 正常登录、错误密码验证（错误提示文案检查） |
| 仪表盘 | 页面加载与核心元素渲染 |
| 产品中心 | 产品列表渲染、产品搜索 |
| 实时行情 | 行情数据表格加载、价格展示 |
| 我的自选 | 自选列表展示、添加/删除自选 |
| 交易委托 | 下单表单操作、订单历史查询 |
| 财经资讯 | 资讯列表、资讯详情页跳转 |
| 消息中心 | 消息列表、消息标记已读 |
| 个人中心 | 用户信息展示、个人信息编辑 |
| 退出登录 | 登出流程、登出后跳转登录页 |
| 导航菜单 | 所有菜单项的可点击性与路由跳转 |

测试命令：
```bash
cd front-user
npx playwright install chromium   # 首次安装浏览器
npm run test:e2e                  # 运行全部 37 条测试
npm run test:e2e:report           # 查看测试报告
npm run test:e2e:ui               # 调试模式
```

测试账号：`zhangwei` / `123456`（`sys_user` 表）

#### 文档重构

- **Startup.md** — 全面重构启动指南，新增 front-user 启动步骤、双端 E2E 测试说明、双测试账号对照表、全链路验证命令、端口对照表更新（增加用户前台 3001 端口）、常见问题排查更新
- 双端架构说明：管理员后台（`front/`, 端口 3000）+ 用户前台（`front-user/`, 端口 3001）

### 测试数据补充

#### 追加全量测试数据

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
| `ums_admin` | 3 | 系统管理员 |
| `ums_role` | 3 | 角色定义 |
| `ums_resource` | 8 | 权限资源 |
| `ums_admin_role_relation` | 3 | 管理员-角色关联 |
| `ums_role_resource_relation` | 8 | 角色-资源关联 |

密码统一使用 BCrypt 加密，关键关联字段（`product_code`、`user_id`）保持跨表一致性。

#### 修复测试数据空值

修复 `ums_admin`、`ums_role`、`ums_resource` 表 `INSERT` 语句中 `create_time` 为 `NULL` 的问题，确保数据导入后时间字段均有值。

### 项目规范更新

- **CLAUDE.md** — 全面同步项目最新状态：
  - 新增前端技术栈版本锁定（Vue/Vite/Element Plus/Pinia/TypeScript/vue-tsc）
  - BaseEntity 继承规则表（明确各字段子类处理方式，含 @TableLogic 统一继承）
  - 新增基础设施类说明表（BaseEntity/Result/BeanConvertUtil/JwtUtil 等 13 个类）
  - 新增 6 种常见代码模式（Entity→VO 转换、分页查询、null 安全更新、业务异常、Feign 调用、JWT 流程、重复检查）
  - 更新健康检查清单和高频问题（JWT 配置、拦截器 context-path、LoginInterceptor 通配符等）
  - 合并端口表并补充网关路由说明
  - 新增第十五节「项目健康检查规则」

---

## v1.3.0 (2026-05-09)

### 数据库全量扫描 & init.sql 重建

完整扫描 `finance` 数据库全部 12 张表的实际结构，重新生成并覆盖 `init.sql`，确保初始化脚本与数据库完全一致。

#### 变更内容
- 使用 `information_schema` + `SHOW CREATE TABLE` 全面扫描所有表的字段、类型、默认值、注释、索引
- 覆盖 12 张表：`sys_user`、`fin_product`、`fin_market_data`、`fin_user_favorite`、`fin_trade_order`、`fin_news`、`fin_message`、`ums_admin`、`ums_role`、`ums_resource`、`ums_admin_role_relation`、`ums_role_resource_relation`

#### 修复
- **ums_admin** — 补充 `del_flag` 列（INT DEFAULT 0）
- **ums_role** — 补充 `del_flag` 列（INT DEFAULT 0）
- **ums_resource** — 补充 `del_flag` 列（INT DEFAULT 0）
- **ums_admin_role_relation** — 补充 `del_flag` 列（INT DEFAULT 0）
- **ums_role_resource_relation** — 补充 `del_flag` 列（INT DEFAULT 0）
- 所有表字符集统一为 `utf8mb4`（旧脚本 ums_* 表为 `utf8`）
- 所有字段补全完整中文注释

---

## v1.2.0 (2026-05-09)

### 全链路连通测试与修复

首次完成完整的前后端全链路 E2E 自动化测试，覆盖 8 个微服务 + 网关 + 前端，测试项 35 项全部通过。

#### 测试覆盖范围
- **基础设施**: Nacos、MySQL、Redis、Docker 容器连通性验证
- **页面加载**: 标题渲染、白屏检测、未登录自动跳转
- **登录流程**: 表单填写、JWT Token获取、登录后跳转控制台
- **菜单导航**: 12 个页面（控制台/用户管理/产品/行情/交易/自选/消息/资讯/系统管理/搜索）
- **API 请求**: 10 个业务接口通过网关调用（管理员/角色/资源/用户/产品/行情/自选/交易/消息/资讯）
- **前端代理**: Vite `/api` 代理到网关全链路验证
- **错误检查**: 浏览器控制台 JS 错误、路由跳转报错、页面内容渲染

#### 后端实体修复
- **UmsAdmin/UmsRole/UmsResource** — delFlag 从 `@TableField(exist = false)` 修正为 `@TableLogic @TableField("del_flag")`，修复 MyBatis-Plus Lambda 查询缓存崩溃（`can not find lambda cache for this property [delFlag]`）
- **UmsAdminRoleRelation/UmsRoleResourceRelation** — 同上修复，两表实际存在 `del_flag` 列
- 新增 `@TableLogic` 依赖导入

#### 用户服务路径修复
- **UserController** — 移除 `@RequestMapping("/user")`，消除与 `context-path: /user` 的路径冲突（修复 "No static resource page" 错误）
- **WebConfig** — 同步修正拦截器排除路径（`/user/login` → `/login`，依此类推）

#### 权限数据补充
- 创建管理员资源权限（`/system/umsAdmin/**`、`/system/umsRole/**`、`/system/umsResource/**` 等）
- 激活超级管理员角色并关联资源，修复 PermissionInterceptor 403 阻断

#### 自动化测试
- 新增 `e2e-test.mjs` — Playwright + Node.js 全链路测试脚本
- 支持基础设施自动检测、API 连通测试、真实浏览器交互、详细报告生成
- 测试环境：headless Chromium、1920x1080 viewport、30s 超时

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
- JDK 21.0.3 / SpringBoot 3.3.5 / SpringCloud 2023.0.3
- Vue 3.5.13 / Vite 6.3.1 / Element Plus 2.9.7
- Nacos 2.x / Redis 5.0.14 / MySQL 8.0.37 / RabbitMQ 3.10.20 / ES 8.11.0
- Playwright 1.59.1 | Node.js 22.14.0

---

## v1.1.0 (2026-05-08)

### 全栈健康检查与修复

#### 基础设施
- 父 pom 移除全局 `spring-boot-starter-web`，避免 gateway 与 WebFlux 冲突
- 移除根 pom `knife4j-openapi3-spring-boot-starter`（导致 gateway WebFlux 冲突）
- 7 个业务模块 pom.xml 添加 `spring-boot-maven-plugin`，修复 JAR 无启动清单问题
- gateway 添加 `spring-cloud-starter-loadbalancer` 依赖
- `BeanConvertUtil` 保留原始异常链（`e` 传入 RuntimeException）

#### 网关 (finance-gateway)
- 添加 7 条服务路由规则（system/user/product/account/trade/message/search）
- 修复全局 CORS 配置 `allow-credentials: true`
- 网关 application.yml 配置优化

#### 搜索模块 (finance-search)
- `FinanceSearchApplication` 添加 `scanBasePackages = "com.finance"`，修复 JwtUtil Bean 未找到
- `MyBatisPlusConfig`、`MyBatisPlusMetaObjectHandler` 添加 `@ConditionalOnClass`，使未引入 MyBatis-Plus 的模块能正常启动
- `ProductSearchController.getById()` 空值返回 404 而非 null
- Nacos 配置 `finance-search` 中 ES 地址 `10.128.82.54:9200` 改为 `localhost:9200`

#### 后端核心修复
- **拦截器**: `SystemWebConfig` 路径模式修复、`LoginInterceptor` 适配 `AntPathMatcher`
- **JWT**: `JwtUtil` 添加默认值、修复空指针
- **事务**: 14 个 Service 方法添加 `@Transactional(rollbackFor = Exception.class)`（update/delete 操作）
- **参数校验**: 11 个 DTO 添加 `jakarta.validation` 注解（@NotBlank/@NotNull），4 个 System Controller 添加 `@Valid`
- **实体**: 12 个 Entity 类添加 `@EqualsAndHashCode(callSuper = true)`
- **Feign**: `AccountFeignClient` 路径从 `/account/finUserFavorite/list` 修正为 `/account/finUserFavorite`
- **ProductFeignClient** 补充泛型参数
- **LoginDTO 转换**: `UserController.login()` 直接传递 LoginDTO，不再转为 User 实体
- **分页**: 6 个业务模块 Controller 新增 `/page` 分页查询接口
- **全局异常**: `GlobalExceptionHandler` 添加 `MethodArgumentNotValidException` 校验异常处理
- **UmsAdminController** create/update 改为接收 `UmsAdminDTO`
- **NULL 处理**: 12 个 Controller getById 空值返回 404

#### 前端 (Vue3 + Element Plus)
- **构建**: `vue-tsc -b` 改为 `vue-tsc --noEmit`，修复 TypeScript 类型声明错误
- **路由**: 添加 RouteMeta 类型声明，新增 404 通配路由，登录保留 redirect 参数
- **表格**: 所有列表页添加 `empty-text="暂无数据"`、`handleSizeChange()` 分页重置
- **删除**: 所有 delete 操作添加 try-catch 异常捕获
- **校验**: user 页面手机号正则验证、邮箱格式验证、编辑时密码非必填
- **搜索页**: price 列添加 formatPrice 格式化
- **登录页**: 登录成功后跳转至 `route.query.redirect || '/'`

### 配置安全
- application.yml 数据库密码改为环境变量 `${DB_PASSWORD:123456}`
- ES 地址改为环境变量 `${ES_URIS:http://localhost:9200}`
- Nacos 配置中心统一管理业务配置，本地仅保留基础框架配置

### 项目规范
- CLAUDE.md 新增第十五节「项目健康检查规则」，记录配置管理原则、端口表、已知高频问题、扫描检查清单
- memory 记录 health-check-skill 预设规则

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
