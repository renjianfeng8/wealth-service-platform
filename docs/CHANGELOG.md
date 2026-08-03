
# 更新日志

> 记录项目级别的功能新增、架构调整与优化。
> Bug 修复细节请参阅 [BUG.md](BUG.md)。

---

## v1.9.0 (2026-08-03)

> 本版本覆盖 2026-07-24 ~ 2026-08-03 的 60 次提交（此前 6-09 至 7-24 为提交空档期）。

### 前端全面改版

- **仪表盘重构**：首页、用户仪表盘、管理端仪表盘全面改版；管理端新增搜索下拉实时预览、默认头像支持。
- **缺陷修复**：修复第一阶段审计 12 项阻塞性缺陷及 5 项高风险遗留问题；HTTP 401 提示、SSE 刷新、NProgress 时序、Navbar 图标等 6 项问题。
- **代码质量**：替换全部 `any` 类型；抽取 `useCrudPage` composable 统一 CRUD 管理页逻辑；SSE 全局单例容错及死代码清理。

### 全量查询分页优化

- **分页约束**：分页参数上限约束、`pageSize` 超限修复。
- **列表分页改造**：首页行情（pageSize=4）、用户仪表盘产品（8）、管理端仪表盘产品（200）、产品收藏（200）等改用分页查询。
- **查询优化**：产品中心支持按编码搜索、LIKE 查询转义；权限缓存读写、`@Select` 补 `del_flag`、查询加 `LIMIT` 与索引。

### 代码规范与 DRY 重构

- **规范对齐**：通配符导入/空 catch/异常信息/Logger/构造器注入/导入顺序统一；补充测试缺失的 mock 静态导入。
- **DRY 收敛**：JWT 解析收敛至 `JwtUtil`（网关复用）、Bearer Token 提取收敛至 `AuthConstant`、验证码 key 收敛至 `CaptchaConstant`、错误响应收敛至 `HttpResponseUtil`、Redis 降级收敛至 `RedisUtil.safeExecute`、唯一性校验收敛至 `checkUnique`、CRUD 样板收敛至 `BaseBizServiceImpl`、分页样板收敛至 `pageWithFilter`/`pageVoList`、登录权限链路收敛至 `AuthSupport`。
- **SOLID 整改**：`UmsAdmin` 上帝类拆分至 Crud/Auth/PermissionQuery 三聚焦服务；验证码下沉 `CaptchaService`（Controller 移除 Redis 直接操作）；行情模拟依赖下沉至 `MarketDataService` 接口。
- **清理**：移除 4 处未用 import 与死方法（`getEmitterCount`/`clearCache`）、零引用 `ResultCode` 常量。

### 安全认证完善

- 图形验证码（`identifyLogin` 校验并签发双 token）、登出黑名单、access token 静默续期。
- 清除 yml 明文密钥默认值与死配置 `jwt.expire`，统一 JWT 过期键名并新增配置契约测试。

### 用户端投资体验补全

- 行情 K 线弹窗、委托单详情、产品/资讯实时拉取、自选详情入口、首页/个人中心卡片深链。
- 激活闲置 GET 接口：`/product/wea-product/{id}`、`/message/wea-news/{id}`、`/trade/wea-trade-order/{id}`、`/system/dashboard/kline`，抽取 `ProductDetailDialog`/`NewsDetailDialog`/`MarketDetailDialog` 共享弹窗。

### 闲置接口治理

- **后端清理**：移除 20 个闲置废弃接口（认证登录 2、getById 6、无分页裸列表 9、关联表 update 2、checkPermission 内部回调 1），收敛 `AuthConstant` 白名单。
- **前端对齐**：删除 `loginApi`/`userLogin`/`getRoleById` 等废弃封装，`PUBLIC_AUTH_PATHS` 收敛为 `user/login` 与 `umsAdmin/login`。
- **激活接入**：消息中心详情全文、管理端批量删除（恢复 `DELETE /user/batch`）、仪表盘资产总览；`AdminDataTable` 新增 selectable 勾选能力。
- **测试清理**：删除 13 个陈旧前端结构测试（断言旧版仪表盘设计），保留 6 个现行有效用例。

### 日志与可观测性专项

- 异常日志补传 `e` 输出完整堆栈（JwtUtil/RedisUtil/UmsAdminAuthServiceImpl/MarketDataPushService）。
- 拦截器与 SSE 高频日志降级 DEBUG；logback pattern 追加 `traceId`/`spanId`，`com.wealth` 默认 INFO（dev profile 才开 DEBUG）。
- `wealth-service` 默认开启 Micrometer Tracing；日志消息统一 `[module:operation]` 前缀。

### 接口契约兼容修复

- **后端**：LocalDateTime 反序列化空串容忍与统一空格格式；Page 分页序列化 `pageNum`/`pageSize`；涨跌幅改 `DECIMAL(8,4)` + 范围校验；13 个 VO 主键字符串化；`UserFavoriteDTO` 重命名 `UserFavoriteProviderDTO`；batch-read 补 `@Valid`；SSE 载荷格式文档化；Page 序列化注册加 `@ConditionalOnClass` 修复 gateway 无 mybatis-plus 启动失败。
- **前端**：axios 拦截器返回 `res.data` 全量调用点收口；行情/资讯日期录入空格格式；分页字段对齐 `pageNum`/`pageSize`；`id`/`userId` 放宽 `number|string`；涨跌幅单位提示；SSE 载荷注释。

### 前端上线专项修复

- KeepAlive 缓存键改 `route.path` 杜绝同组后台页面串页；AdminFormDialog 保存重入保护 + admin/trade 参数哈希稳定幂等键；`utils/uuid` 兼容非 HTTPS 安全上下文。
- 注册/手机号/行情价格校验补齐；后台搜索分页与 4 列表 `size-change` 页码修复；消息未读数走后端统计；头像本地预览去误导提示；SSE 断线指数退避；`format` 对 NaN/Invalid Date 兜底。
- 交易委托订单状态 off-by-one 修复（前端字典 0/1/2 改 1/2/3、撤销走状态机端点、合并 admin 重复映射）。

### 工程与文档治理

- 移除被 CI 替代的 `docker-build.ps1` 与冗余 `backup-mysql.ps1`，清理已合并模块的陈旧 worktree。
- 全量文档治理（精简/归档/修复/对齐）；更新 CLAUDE.md、新增代码规范手册 `docs/CODE-STANDARDS.md`；ARCHITECTURE.md 补 SSE 例外约定（全站唯一不包 Result 信封）。
- 精简 docs 体系：删除 superpowers 交付物、DOCUMENTATION-GOVERNANCE，AGENTS.md 迁入 `.claude/` 修链。

---

## v1.8.3 (2026-06-09)

### 管理端前端体验重构

- **运营控制台重构**：拆分仪表盘为指标、趋势、K 线、近期订单、任务队列、快捷入口和行情快照等组件。
- **Admin CRUD 通用组件**：新增 `AdminPageShell`、`AdminFilterBar`、`AdminDataTable`、`AdminFormDialog`，统一后台列表页结构。
- **后台页面批量接入**：用户、产品、行情、交易、收藏、消息、资讯、搜索、管理员、角色、资源、管理员角色关联、角色资源关联均接入通用组件。
- **消息通知复用**：抽取 `MessageNoticePopover`，统一管理端和用户端导航消息入口。
- **前端验证补强**：新增结构测试覆盖仪表盘、CRUD 通用组件、业务页、系统页、文档交付物和文档治理。

### 文档治理

- **新增文档总索引**：新增 `docs/README.md`，统一核心规范、启动部署、架构数据和 Superpowers 交付物入口。
- **新增治理规则**：新增 `docs/DOCUMENTATION-GOVERNANCE.md`，明确权威来源、文档分类、命名规则、归档规则和更新检查清单。
- **补齐交付文档**：新增 Admin Figma 原型规格、后端接口适配建议和前端落地汇总。
- **README 增加文档入口**：在根 README 增加文档导航，降低入口分散问题。

---

## v1.8.2 (2026-05-26)

### 文档全面重写

- **README 重写**：Badge 工具栏、核心特性、架构演进、技术栈分类、快速开始、部署指引等，对标热门 GitHub 项目风格
- **STARTUP 重写**：适配单一 SPA 架构（端口 3000），History 模式路由表，更新验证流程与排查表
- **ARCHITECTURE 重写**：移除三 SPA 描述、front-landing 登录流程，更新为 single SPA + History 模式认证流程
- **CONTRIBUTING 重写**：移除 Nacos、微服务等过时引用，同步当前 Gateway + service 两层架构
- **文件名统一大写**：ARCHITECTURE.md、BUG.md、DATABASE-SCHEMA.md、STARTUP.md
- **清理过期文档**：删除 15 份过时设计文档、实施计划与评估报告

### 前端合并收尾

- **多 SPA → 单一 SPA**：front-landing（统一登录门户）、front-user（用户前台）、front（管理后台）合并为单一 front/，History 模式路由统一分发
- **路由路径恢复**：管理后台快捷入口补全 `/admin/` 前缀（Bug-017）
- **认证统一**：sessionStorage 单键（`wealth_logged_in`）+ 角色标志（`wealth_role`），统一 store 认证
- **用户端功能保留**：产品浏览、行情查询、交易委托、自选管理、资讯消息均在统一 SPA 中

### 工程治理

- **CI 修复**：移除已删除的 frontend-user 构建任务，重命名 frontend-admin → frontend
- **后端死代码清理**：删除未使用的 `updateAdmin()` 方法及相关测试
- **VO/DTO 注解补全**：8 个 VO/DTO 添加 `@Schema` Swagger 注解
- **前端 JSDoc 补全**：9 个 API/工具模块 85+ 函数添加完整 JSDoc
- **注释修复**：修复 MyBatisPlusMetaObjectHandler、ProductRepository 中文乱码
- **@MapperScan 清理**：移除不存在的 search mapper 扫描路径

---

## v1.8.1 (2026-05-25)

### 多 SPA → 单一 SPA 合并

- **三 SPA 合并**：将 front-landing、front-user、front 合并为单一 SPA（`front/`），端口统一为 3000
- **History 模式路由**：`/auth/login`（登录）、`/home`（首页）、`/user/*`（用户端）、`/admin/*`（管理端）
- **双布局架构**：`UnifiedLayout`（顶部导航，用户端/公开页）+ `AdminLayout`（侧栏导航，管理端）
- **路由守卫自动跳转**：登录后根据角色（admin/user）自动跳转对应首页
- **sessionStorage 认证**：`wealth_logged_in` + `wealth_role` 双标志位，退出登录清除后跳转登录页

### 公开页面

- **首页**：Banner + 核心功能入口 + 产品行情卡片
- **产品列表页**：产品浏览与分类筛选
- **行情详情页**：实时行情 SSE 推送展示
- **资讯列表页**：财经资讯浏览

### 配置与清理

- 移除 front-landing、front-user 目录及相关构建配置
- 更新 Gateway CORS 配置适配单一 SPA 端口
- 更新 Nginx 配置适配单一 SPA 静态资源路径

---

## v1.8.0 (2026-05-24)

### 架构重构：微服务 → 单体聚合

- **6 个业务微服务合并为 wealth-service**：将 wealth-system、wealth-user、wealth-product、wealth-trade、wealth-message、wealth-search 合并为统一的 wealth-service 模块（端口 8081），消除跨服务网络开销
- **Gateway 路由简化**：从负载均衡（`lb://`）改为静态 HTTP 路由（`http://localhost:8081`），无需 Nacos 注册中心
- **Nacos 配置中心禁用**：所有模块 `nacos.config.enabled=false`，配置体系简化为本地 application.yml + 环境变量
- **OpenFeign 移除**：跨模块调用替换为本地 contract 接口（`com.wealth.common.contract`）
- **bootstrap.yml 移除**：Gateway 不再需要 bootstrap.yml
- **父 POM 精简**：从 9 模块减少到 3 模块

### 依赖安全升级与清理

- Spring Boot 3.3.5 → 3.3.13（Tomcat 10.1.31→10.1.37+, Jackson 2.17.2→2.17.3+）
- Spring Cloud 2023.0.3 → 2023.0.6
- Spring Cloud Alibaba 2023.0.1.2 → 2023.0.3.4
- MyBatis-Plus 3.5.7 → 3.5.9
- Knife4j 4.4.0 → 4.5.0
- 移除无用依赖：RabbitMQ、OpenFeign、Loadbalancer

### 配置规范化

- **Redis 属性迁移**：全模块 `spring.redis.*` → `spring.data.redis.*`（Spring Boot 3.x 规范）
- **Knife4j Java 配置**：替代已废弃的 YAML 配置方式
- **Gateway 配置修复**：补齐缺失的 `management` 配置段
- 各模块新增独立 `.env` 文件

### 工程清理

- 删除构建产物（front/dist、front-user/dist 等）
- 全量文档重构：README、CLAUDE.md、ARCHITECTURE.md、STARTUP.md 同步单体聚合架构

---

## v1.7.2 (2026-05-23)

### 安全加固

- **JWT 存储迁移**：前端移除 localStorage，改用 httpOnly Cookie + sessionStorage 标志位
- **Redis 配置加载修复**：修复 `RedisConfig` 加载顺序导致 `@Value` 未注入的根因
- **输入校验增强**：全模块补充 DTO `@NotBlank`、`@NotNull`、`@Size` 等注解
- **数据库连接池加固**：添加连接超时、空闲超时、最大生命周期等防泄漏配置

### 监控与可观测性

- **Prometheus + Grafana 内置**：docker-compose 新增 Prometheus（9090）和 Grafana（3001）
- **Docker 健康检查**：统一使用 wget（eclipse-temurin 内置），添加进程守护
- **链路追踪采样优化**：采样率 1.0 → 0.1

### 业务逻辑修复

- **404 响应标准化**：全模块 `getById` 返回 null 时返回 `ServiceException(404, "资源不存在")`
- **外部请求超时控制**：生产环境 RestTemplate/WebClient 超时配置（5s/10s）

---

## v1.7.0 (2026-05-19)

### 基础设施安全认证

- Nacos 认证启用、Elasticsearch 安全认证启用
- `.env` 文件从 git 历史中永久清理

### JWT 双 Token 机制

- 登录返回 `access_token`（30 分钟）+ `refresh_token`（7 天）
- refresh_token 一次性使用（防重放），jti 存入 Redis 支持手动吊销

### 安全加固

- XSS 全局过滤器覆盖 GET/POST 参数和 JSON 请求体
- 登录暴力破解防护：连续 5 次失败锁定 15 分钟
- Swagger/Knife4j 白名单移除

---

## v1.6.x 系列

### v1.6.3 (2026-05-18)
- README 与 Startup.md 文档精简，消除重复内容

### v1.6.2 (2026-05-18)
- 全项目文档标准化治理，工程结构优化

### v1.6.1 (2026-05-18)
- 中文注释乱码修复、YAML 重复键清理、update 模式标准化

### v1.6.0 (2026-05-18)
- **SSE 实时行情推送体系**：`MarketDataPushService` + `MarketDataSimulationService`（@Scheduled 每 2 秒）
- Nginx SSE 反向代理配置（proxy_buffering off, read_timeout 86400s）
- 前端 SSE 集成（utils/sse.ts + EventSource）

---

## v1.5.0 (2026-05-17)

### 可观测性体系

- **链路追踪**：Micrometer Tracing + Brave + Zipkin 集成
- **监控告警**：Prometheus + Grafana 集成，所有服务暴露 `/actuator/prometheus`
- **HTTPS 证书**：Nginx SSL 自签名证书（RSA 2048）
- **数据库备份**：binlog（ROW 格式，保留 7 天）+ 自动全量备份脚本
- **交易幂等性**：`idempotentKey` + Redis 校验

---

## v1.4.x 系列

### v1.4.1 (2026-05-12)
- ES 版本校准至 8.8.2，IK 中文分词器集成
- 登录接口返回 `userId` 字段

### v1.4.0 (2026-05-10)
- 新增用户前台 front-user（Vue 3 + TypeScript，端口 3001）
- Playwright E2E 自动化测试（37 条用例）
- 测试数据补充（12 张表 83 条记录）

---

## v1.3.0 (2026-04-28)
- 初始模块搭建，微服务架构落地
- Spring Cloud Alibaba 体系集成（Nacos）
- 统一返回格式 `Result<T>` 与全局异常处理
- JWT 认证体系搭建
- 管理员后台 CRUD 基础框架

## v1.2.0 (2026-04-15)
- 基础设施搭建：Spring Boot 3.3.5 + Spring Cloud 2023.0.3
- Nacos / MySQL / Redis / RabbitMQ / ES Docker 化部署
- Swagger + Knife4j API 文档集成

## v1.1.0 (2026-04-01)
- 技术选型论证、数据库表结构设计（12 张核心表）、API 规范定义

## v1.0.0 (2026-03-15)
- 项目立项、仓库初始化、基础文档、Docker 编排、Git 工作流
