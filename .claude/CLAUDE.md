# 理财服务平台 — 项目指南（正式版）

> 本项目规约文档，优先级高于一切编码规范，是 **Claude Code 与 Codex 双 AI 代理的最高强制红线 + 项目全栈开发手册**。
> 所有规则均为**强制约束**，面向 AI 阅读友好，规则直白、硬性、可落地。
> 详细代码规范手册见 [docs/CODE-STANDARDS.md](../docs/CODE-STANDARDS.md)。

---

## 一、文档说明与项目概览

### 1.1 文档定位

| 项目 | 说明 |
|------|------|
| 平台 | 理财服务平台（金融级） |
| 架构 | Spring Boot 单体服务 + Spring Cloud Gateway 网关 + Vue 3 + TypeScript SPA |
| 部署形态 | 各业务域合并部署在 `wealth-service` 模块 |
| 适用对象 | Claude Code、Codex 双 AI 代理 + 全体开发者 |
| 文档状态 | 正式版，修订需走变更审批（§十四） |

### 1.2 关键文档索引

| 关键文档 | 路径 |
|---------|------|
| **建表 SQL（唯一真理）** | `backend/wealth-common/src/main/resources/sql/init.sql` |
| 架构文档 | `docs/ARCHITECTURE.md` |
| 表结构 / BaseEntity 规范 | `docs/DATABASE-SCHEMA.md` |
| 已知问题 | `docs/BUG.md` |
| **代码规范手册** | **`docs/CODE-STANDARDS.md`** |
| 环境搭建与启动 | `docs/STARTUP.md` |
| 文档索引 | `docs/README.md` |

> **文档链检查**：任何改动必须追踪到最底层数据源（init.sql → 数据库 → 代码）再动手，禁止凭记忆猜测表结构。

---

## 二、【最高优先级】AI-Agent 强制操作红线

> 本节内容合并原 `AGENTS.md` 全部管控规则，任何 AI 代理违反本节即视为高危操作。原 AGENTS.md 文件已并入本文档。

### 2.1 高危操作人工审批清单

**所有高危动作必须先提供「改动原因 + 风险评估 + 回滚方案」，等待用户明确审批后方可执行。** 审批一次仅对当次动作生效，不构成后续默认授权。

| # | 高危动作 | 风险 | 审批要求 | 回滚方案 |
|---|---------|------|---------|---------|
| 1 | 修改 yml 配置（application.yml / application-prod.yml） | 影响运行参数、连接信息 | 必须审批，说明改动原因与影响面 | git revert / 恢复原配置 |
| 2 | 改动 pom 依赖（新增/升级/删除） | 版本冲突、安全漏洞、不兼容 | 必须审批，评估安全与兼容性 | 回退版本号 |
| 3 | 编辑 init.sql 建表脚本 | 影响全部环境数据模型 | 必须审批，同步更新 DATABASE-SCHEMA.md | 恢复脚本 + 反向 DDL |
| 4 | 删除源码 | 不可逆丢失工作 | 必须审批，先确认是否他人工作 | git 恢复（若已提交） |
| 5 | git commit / git push | 影响共享代码库 | 必须审批，禁止自动提交推送 | revert / reset |
| 6 | 操作 Docker（容器启停/镜像构建/删除） | 影响运行环境 | 必须审批 | 重新 pull / 重建容器 |
| 7 | 新增第三方 Maven 依赖 | 供应链安全风险 | 必须审批，评估 license 与漏洞 | 移除依赖 |
| 8 | 修改拦截器与权限逻辑（LoginInterceptor / PermissionInterceptor / JwtAuthGlobalFilter / `AuthConstant.PERMIT_ALL_URLS`） | 权限绕过、认证失效 | 必须审批，全链路评估 | 恢复旧权限逻辑 |
| 9 | 修改本 CLAUDE.md 或 CODE-STANDARDS.md | 规则失效 | 必须审批 | git revert |

### 2.2 文件读写权限约束

| 类别 | 文件 | 约束 |
|------|------|------|
| 锁文件（只读，改需审批） | `init.sql`、`application*.yml`、`pom.xml`、`docs/DATABASE-SCHEMA.md`、`.claude/CLAUDE.md` | 禁止未审批修改；init.sql 与 DATABASE-SCHEMA.md 必须同步一致 |
| 业务源码 | `backend/**/src/main/java/**` | 在规范范围内可自由修改 |
| 测试代码 | `backend/**/src/test/**`、`front/src/**/__tests__/**` | 可自由创建/修改 |
| 普通文档 | `docs/**`（核心文档除外） | 可修改，遵循 §十四 审批要求 |
| 环境文件 | `.env`、`deploy/env/*` | 密钥类严禁提交仓库；内容修改需审批 |

**删除文件铁律**：先确认用途与是否他人进行中工作，git-tracked 文件需说明可恢复性；禁止 `rm -rf` 式无差别删除。

### 2.3 AI 标准编码工作流程

AI 完成任何编码任务必须遵循以下流程：

```
① 需求理解 → ② 读关键文件建立上下文（init.sql、DATABASE-SCHEMA.md、目标模块代码）
→ ③ 生成/修改代码（遵守本文档全部规范）→ ④ 自检（§十六 七大扫描清单）
→ ⑤ 验证（编译 / 测试 / 启动冒烟）→ ⑥ 汇报改动与验证结果
```

**AI 生成代码规则（必须遵守，合并自 AGENTS.md）：**

1. 必须严格按照 DATABASE-SCHEMA.md 表结构生成 Entity、Mapper、Service、Controller、Vo、Dto
2. 必须使用 MyBatis-Plus
3. Entity 必须继承 BaseEntity，按 DATABASE-SCHEMA.md §三处理字段覆盖
4. 必须自动填充 create_time、update_time
5. 接口必须遵循 RESTful 规范
6. 必须加 Swagger 注解（`@Tag` / `@Operation`）
7. 必须符合项目技术栈
8. **不允许生成不存在的表或字段**
9. **生成代码必须能直接运行**
10. 写操作（增删改）必须加 `@Transactional(rollbackFor = Exception.class)`
11. 所有 `@RequestBody` DTO 必须加 `@Valid` 参数校验注解
12. 新增接口须确认是否需要加入权限白名单 `AuthConstant.PERMIT_ALL_URLS`

### 2.4 Agent 禁止行为黑名单

> 以下行为**一律禁止**，违反即为红线事故：

- 禁止自动执行 `git commit` / `git push`（必须等用户明确指令）
- 禁止未审批修改配置、依赖、init.sql、权限逻辑
- 禁止硬编码密码、密钥、IP、内网地址
- 禁止生成不存在的表/字段，禁止凭空虚构项目内类名与配置
- 禁止全表 `service.list()` 无分页
- 禁止通配符导入、禁止空 catch、禁止吞异常
- 禁止 Controller 承载业务逻辑（Token 解析、权限校验、金额计算等必须下沉 Service）
- 禁止跨层反向调用（Controller↔Service↔Mapper 反向、Service 依赖 Controller 等）
- 禁止在 prod 环境打开 Swagger 文档或 SQL 控制台打印
- 禁止删除他人进行中工作、覆盖未提交变更
- 禁止虚构测试通过、编造验证结果（未验证必须如实说明）

### 2.5 角色定位与协作模式

| 角色 | 工具 | 职责 |
|------|------|------|
| 主控端 | Codex | 架构设计、任务分解、方案评审、代码审查 |
| 执行端 | Claude Code | 按指令完成实现、验证与汇报 |

- 改动前先读取关键文件建立上下文（跨模块切换、涉及 DB 变更时尤需注意）
- 文档、代码、数据库三方对齐后再修改
- 重要决策与约定写入本文档或 docs/CODE-STANDARDS.md

---

## 三、完整技术栈对照表

### 3.1 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 21 | 运行语言 |
| Spring Boot | 3.3.13 | 基础框架 |
| Spring Cloud | 2023.0.6 | 微服务组件 |
| Spring Cloud Alibaba | 2023.0.3.4 | SCA 组件管理 |
| MyBatis-Plus | 3.5.9 | ORM |
| MyBatis-Spring | 3.0.5 | MyBatis 集成 |
| MySQL | 8 | 主数据库（connector 由 SB parent 管理） |
| Redis | 5 (spring-boot-starter-data-redis) | 缓存 / 分布式锁 / 限流 |
| JWT (jjwt) | 0.12.6 | 认证令牌 |
| Knife4j (Swagger) | 4.5.0 | 接口文档 |
| Sentinel | 由 SCA BOM 管理 | 限流熔断（当前无规则） |
| Micrometer Tracing | 1.3.6 (Brave + Zipkin) | 链路追踪 |
| Prometheus | micrometer-registry-prometheus | 指标采集 |
| Elasticsearch | 8.8.2（可选，search 域降级 MySQL LIKE） | 全文检索（可降级） |
| Nacos | 由 SCA BOM 管理（注册中心 + 配置中心，已禁用） | 已禁用 |

### 3.2 前端

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | ^3.5.13 | UI 框架 |
| Vite | ^6.3.1 | 构建工具 |
| Element Plus | ^2.9.7 | 组件库 |
| Pinia | ^2.3.1 | 状态管理 |
| TypeScript | ~5.7.2 | 类型系统 |
| Vue Router | ^4.5.0 | 路由 |
| Axios | ^1.7.9 | HTTP 客户端 |
| ECharts | ^5.5.1 | 图表 |
| dayjs | ^1.11.13 | 日期处理 |

### 3.3 中间件与测试组件

| 组件 | 版本 | 用途 |
|------|------|------|
| JUnit 5 + Mockito | 由 SB parent 管理 | 后端单元/集成测试 |
| Nginx | — | 反向代理 / 前端静态资源 |
| Docker / docker compose | — | 本地环境（mysql / redis / nginx） |

> **依赖版本铁律**：所有依赖版本由父 `pom.xml` 统一管理，子模块禁止独立声明版本；禁止引入未经安全评估的第三方组件（见 §7.13）。

---

## 四、工程模块、包分层结构规范

### 4.1 模块结构

```
wealth-service-platform (pom)
├── wealth-common   # 公共模块：工具类、Contract 接口、统一返回、通用配置、异常、常量
├── wealth-gateway  # 网关（Spring Cloud Gateway，无数据源）
├── wealth-service  # 业务聚合服务（所有 domain 合并部署，端口 8081）
└── front           # Vue 3 SPA
```

### 4.2 业务域 domain 映射

| 表前缀 | 业务 | domain |
|--------|------|--------|
| sys_user | 用户管理 | user |
| wea_product / wea_market_data / wea_user_favorite | 产品 / 行情 / 自选 | product |
| wea_trade_order | 交易委托 | trade |
| wea_news / wea_message | 资讯 / 消息推送 | message |
| ums_* | 后台权限 | system |

### 4.3 包结构（`com.wealth.platform.{domain}`）

```
controller → service → mapper → entity   # 标准分层
vo / dto / config / constant / interceptor
```

通用包（`com.wealth.common`）：`result`（Result/ResultCode）、`exception`（GlobalExceptionHandler/ServiceException）、`utils`（BeanConvertUtil/RedisUtil）、`config`、`constant`（AuthConstant）、`interceptor`（LoginInterceptor）、`audit`（AntiReplay）、`auth`（AuthSupport）。

### 4.4 分层单向调用黑名单

**强制单向调用链：`Controller → Service → Mapper`，禁止一切反向与越层调用。**

| # | 禁止行为 | 示例 |
|---|---------|------|
| 1 | Service 反向依赖 Controller | Service 中 `@Autowired` Controller |
| 2 | Mapper 依赖 Service | Mapper 方法内调 Service |
| 3 | Controller 直接注入 Mapper | Controller 直接查库 |
| 4 | Controller 承载业务逻辑 | Controller 内做 Token 解析、权限校验、金额计算 |
| 5 | Service 与 Service 循环依赖 | A→B→A |
| 6 | 跨域越层直接操作他域 Mapper | user 域直接调 product 域 Mapper |

> 跨域复用统一走 **Contract 接口**（wealth-common 定义接口 → wealth-service 注入实现），禁止跨域直接依赖 Mapper。

---

## 五、多环境（dev/test/prod）隔离强制规约

### 5.1 环境定义与配置分离

| 环境 | profile 文件 | 用途 | 数据库 | 说明 |
|------|-------------|------|--------|------|
| dev | `application.yml`（默认） | 本地开发 | 本地独立库 | 可开启 SQL 打印、Swagger |
| test | `application-test.yml`（须补充） | 联调/测试 | 测试独立库 | 可开启 SQL 打印，不开 Swagger |
| prod | `application-prod.yml` | 生产 | 生产独立库 | **关闭 Swagger、关闭 SQL 打印** |

- 三套环境必须使用**独立数据库实例与 Redis 实例**，禁止混用连接信息
- 环境差异通过 `.env` / 环境变量注入（`MYSQL_HOST`、`REDIS_HOST`、`JWT_SECRET` 等），**密钥禁止写入代码或提交仓库**
- 环境文件：根目录 `deploy/env/.env`（样例 `deploy/env/.env.example`、生产 `deploy/env/.env.prod`）+ 各模块 `.env`

### 5.2 生产环境硬性开关（最高优先级）

| # | 配置项 | prod 要求 |
|---|--------|-----------|
| 1 | Swagger / Knife4j 接口文档 | **关闭**（`springdoc.api-docs.enabled=false` / knife4j 关闭），禁止暴露接口清单 |
| 2 | SQL 控制台打印（mybatis-plus `log-impl`） | **关闭**，仅 dev/test 可开 |
| 3 | 管理端点 | 仅暴露 `health,info`（见 application.yml），禁止暴露 `env`、`beans`、`heapdump` |
| 4 | 数据库密码 / Redis 密码 | 必须来自环境变量，默认占位值（如 `change-me-to-a-strong-password`）禁止用于生产 |

### 5.3 配置隔离黑名单

- 禁止 prod 环境引用 dev 配置、dev 数据库连接
- 禁止在 application.yml 中 hardcode 密码 / 密钥 / 生产地址
- 禁止跨环境复用同一份 `.env`（各环境独立）
- 修改 `application.yml` / `application-prod.yml` 必须走 §十四 审批流程

---

## 六、金融项目专属安全开发规范（最高优先级）

> 理财平台涉及资金与个人信息，安全规范优先级高于一切编码效率。

### 6.1 密钥与敏感信息

- **禁止硬编码**密码、密钥、Token、内网 IP、数据库连接串；一律通过 `.env` / 环境变量注入
- 密钥文件（`.env`、`*.pem`、`jwt.secret` 真实值）必须加入 `.gitignore`，禁止提交仓库
- 日志、异常信息、接口响应中禁止输出明文密码、完整手机号、身份证、卡号等敏感字段

### 6.2 接口权限校验

- 新增接口必须评估权限：后台/交易类接口**必须**走鉴权（JWT + 权限拦截），默认不放开白名单
- 需免登录的接口（登录、注册、验证码、公共查询）才加入 `AuthConstant.PERMIT_ALL_URLS`
- **禁止为图方便把受限接口加入白名单**；改白名单必须走 §2.1 审批
- 网关 `JwtAuthGlobalFilter` 统一认证，服务侧 `LoginInterceptor` / `PermissionInterceptor` 二次校验

### 6.3 入参防御攻击

| 攻击类型 | 防御要求 |
|---------|---------|
| SQL 注入 | 禁止拼接 SQL，全部走 MyBatis-Plus / 参数绑定（`#{}`）；禁止 `${}` 拼接用户输入 |
| XSS | 前端对用户输入转义；后端输出做 HTML 转义 |
| CSRF | 写操作走防重放头（`X-Timestamp` / `X-Nonce` + 后端 `@AntiReplay` 校验） |
| 越权访问 | 资源归属校验（如自选、交易必须校验归属当前用户），禁止仅凭 ID 直接操作 |
| 非法参数 | `@RequestBody` DTO 必须加 `@Valid`，参数校验前置 |

### 6.4 敏感字段日志脱敏

- 密码、手机号、身份证、卡号、Token 等字段**禁止明文打印**
- 手机号/邮箱脱敏：`138****1234` 形式；Token 仅打印前 8 位
- 异常堆栈中若含敏感参数，先脱敏再记录
- `@Slf4j` 占位符打印对象时，实体中的敏感字段需 `@JsonIgnore` / `@ToString.Exclude`

### 6.5 接口限流与防刷

- 限流框架：Sentinel（SCA 已引入，按需配置规则）；轻量场景可用 `RedisUtil.increment` 计数限流
- 登录、验证码、短信等接口必须限流，防止暴力破解（结合 `MAX_LOGIN_ATTEMPTS` 失败锁定）
- 交易、资金类写接口必须防重提交（见 §6.7）

### 6.6 密码与传输安全

- 密码存储统一使用 **BCrypt**（`PasswordEncoderConfig` / `AuthSupport`），禁止明文、MD5、SHA 直接存储
- 生产环境必须 HTTPS；JWT 通过 **httpOnly Cookie** 传输，禁止前端 localStorage 存明文 token
- 认证令牌必须有过期时间（`jwt.access-expire` 默认 30 分钟，`jwt.refresh-expire` 默认 7 天）

### 6.7 交易防重提交

- 前端：表单提交防抖（`useFormGuard` / 提交按钮 loading），防止连点
- 后端：写接口使用 `@AntiReplay`（校验 `X-Timestamp` / `X-Nonce` 防重放）
- 关键资金操作：基于业务唯一键（订单号、流水号）做幂等，配合 Redis `setIfAbsent` 防并发重复
- 交易金额计算必须使用 `BigDecimal`，禁止 `double` / `float`（见 §7.9）

### 6.8 异常与错误信息防护

- **禁止向外暴露异常堆栈**：全局异常由 `GlobalExceptionHandler` 统一处理，对外返回统一 `{code, message, data}`，堆栈只写日志
- 对外错误信息禁止泄露 SQL、表结构、内部类名、技术栈细节
- 所有异常提示统一中文，禁止中英文混杂

---

## 七、Java 后端详细编码规范

### 7.1 分层职责

- Controller：参数校验与路由，**不写业务逻辑**（Token 解析、权限校验、金额计算等必须下沉 Service）
- Service：业务逻辑、事务控制、调用 Mapper
- Mapper：数据访问，只做 SQL / MP 操作

### 7.2 接口规范

- 统一返回：`Result.success(data)` → `{code, message, data}`（`com.wealth.common.result.Result`）
- RESTful + Swagger 注解：`@Tag(name = "模块管理")`、`@Operation(summary = "操作描述")`
- `@RequestBody` DTO 必须加 `@Valid`
- Controller 不写方法级 JavaDoc（用 `@Operation` 替代）

### 7.3 依赖注入与构造

- 构造器注入：类上加 `@RequiredArgsConstructor` + `private final` 字段
- **禁止 `@Autowired` 字段注入**
- 禁止通配符导入；导入按 `java.* → org.* → com.*` 顺序

### 7.4 日志规约

| # | 规则 | 要求 |
|---|------|------|
| 1 | 使用方式 | 类上加 `@Slf4j`，禁止手动声明 Logger |
| 2 | 日志级别 | `debug`（开发细节）→ `info`（关键业务流转）→ `warn`（可恢复异常）→ `error`（业务/系统异常）。禁止用 `error` 打正常流程，禁止用 `debug` 打关键操作 |
| 3 | 业务唯一 id | 关键链路打印业务唯一 id（订单号、用户 id、请求 traceId），便于全链路排查 |
| 4 | 异常堆栈 | `error` 日志必须输出完整堆栈（`log.error("msg", e)`），禁止 `e.getMessage()` 丢堆栈 |
| 5 | 敏感信息屏蔽 | 密码、Token、手机号等按 §6.4 脱敏后打印 |
| 6 | 日志格式 | 用占位符 `{}` 拼接，禁止字符串 `+` 拼接 |
| 7 | 链路追踪 | 使用 `management.zipkin.tracing.endpoint`（Brave + Zipkin），sampling 0.1 |

### 7.5 异常规约

- 业务异常抛 `ServiceException(code, message)`，禁止返回 `null` 冒充错误
- 禁止空 catch、禁止 `catch (Exception e) {}` 吞异常
- 资源不存在（getById 空值）→ 返回 404 / `ServiceException`
- 全局异常由 `GlobalExceptionHandler`（`com.wealth.common.exception`）统一兜底，禁止各 Controller 各自 try-catch
- 所有异常提示统一中文

### 7.6 常量与魔法值

- 魔法值全部抽取为 `private static final` 常量：

```java
private static final int COOKIE_MAX_AGE_SECONDS = 1800;
private static final int MAX_LOGIN_ATTEMPTS = 5;
private static final long LOCK_DURATION_MINUTES = 15;
```

- 跨类共享常量放 `Constant` 类 / `AuthConstant`，禁止散落

### 7.7 事务与传播机制

- **所有写操作（增删改）必须加 `@Transactional(rollbackFor = Exception.class)`**（默认 `Exception` 才回滚，禁止用 `RuntimeException.class` 造成部分异常不回滚）
- 事务传播机制约束：

| 场景 | 传播行为 | 说明 |
|------|---------|------|
| 常规业务方法 | `REQUIRED`（默认） | 参与当前事务，无则新建 |
| 独立子事务（如独立审计、消息推送，失败不影响主流程） | `REQUIRES_NEW` | 必须显式标注，避免嵌套回滚吞主事务 |
| 只读查询 | 可加 `@Transactional(readOnly = true)` | 提升只读性能 |
| 禁止 | `NOT_SUPPORTED` / `MANDATORY` 滥用 | 语义不明时用默认 REQUIRED |

- **事务失效陷阱**：同类内部 `this` 调用不触发代理，须注入自身 Bean 或拆分到独立 Service；`@Transactional` 只对 public 方法生效
- 禁止在事务内做远程 IO / 长耗时操作（外部调用、发送消息应在事务外/异步）

### 7.8 金额与精度

- 金额计算**必须使用 `BigDecimal`**，禁止 `double` / `float`
- 金额存储用 `decimal` 列；分页/展示对齐 `rise_fall_rate` 等字段 2 位小数
- 换算（元↔分）统一工具方法，禁止各业务自行实现

### 7.9 Redis 缓存规范（缓存雪崩/击穿/穿透防护）

缓存统一通过 `RedisUtil`（`com.wealth.common.utils`，注入 `jsonRedisTemplate`）操作，禁止直接散用 `RedisTemplate`。

| 故障 | 防护手段 |
|------|---------|
| 缓存穿透（查不存在 key） | ① 空值缓存（null 也缓存短 TTL）；② 布隆过滤器前置过滤；③ 参数合法性前置校验 |
| 缓存击穿（热点 key 过期瞬间高并发打 DB） | ① 互斥锁重建（`RedisUtil.setIfAbsent` 加锁，重建后释放）；② 逻辑过期 |
| 缓存雪崩（大量 key 同时过期） | ① 过期时间加随机扰动（基础 TTL + 随机 1~5 分钟）；② 热点 key 不过期 + 异步续期 |

- 缓存 key 统一规范：`模块:业务:标识`（如 `product:detail:1001`），禁止裸 key
- 写库后缓存一致性：先更新库再删缓存（Cache Aside），禁止只写缓存不落库
- Redis 不可用降级：使用 `RedisUtil.safeExecute` / `safeExecuteVoid` 兜底返回默认值，**禁止 Redis 故障拖垮主链路**
- 缓存数据必须设置 TTL，禁止永久 key（除非热点续期方案）

### 7.10 分布式锁

- 分布式锁基于 Redis：`RedisUtil.setIfAbsent(key, value, timeout, unit)` 实现 SET NX EX
- 必须设置锁超时（如 30s），防止死锁；释放时校验持有者（value 用唯一请求 id）
- 锁 key 必须包含业务维度（`lock:trade:{orderId}`），禁止全局一把锁
- 复杂场景（可重入、看门狗）评估引入 Redisson 需走 §2.1 依赖审批

### 7.11 IO 与资源关闭规范

- 文件流、网络流、`Stream`、`ResultSet` 等必须 **try-with-resources** 或 finally 中关闭
- 禁止在循环内重复创建连接、线程、大对象
- 线程池统一配置（`spring.task.execution.pool`，core 4 / max 8 / queue 100），禁止无界 `new Thread`
- 定时任务、SSE、模拟行情推送（`market.simulation.interval`）等长连接资源注意超时与关闭

### 7.12 依赖管控（Maven）

| # | 规则 |
|---|------|
| 1 | 所有依赖版本由**父 `pom.xml`** 统一管理（dependencyManagement），子模块**禁止独立声明版本** |
| 2 | 新增第三方依赖必须走 §2.1 审批：评估 license、安全漏洞（SCA/CVE）、版本兼容性 |
| 3 | 禁止引入与现有技术栈冲突、重复职责的组件 |
| 4 | 技术栈版本锁定：Spring Boot 3.3.13 / Spring Cloud 2023.0.6 / SCA 2023.0.3.4 / MyBatis-Plus 3.5.9 / jjwt 0.12.6 / Knife4j 4.5.0 |
| 5 | 依赖升级单独提交、可回滚，禁止混入业务改动 |

### 7.13 代码质量黑名单

- 禁止全表 `service.list()` 无分页
- 禁止通配符导入（`import xxx.*`）
- 禁止空 catch、吞异常
- 禁止魔法值散落（未抽常量）
- 禁止 `@Autowired` 字段注入
- 禁止 `BeanUtils.copyProperties`（更新用 `BeanConvertUtil.copyNonNullProperties`）
- 禁止全限定类名字段、禁止 Controller 业务逻辑
- 禁止 hardcode 密码/IP/密钥

---

## 八、SQL、数据表、MyBatis-Plus 开发规范

### 8.1 表结构与建表规范

- 建表 SQL **唯一真理**：`backend/wealth-common/src/main/resources/sql/init.sql`
- 表命名按业务域前缀（`wea_` / `sys_` / `ums_`），字段命名 `snake_case`
- 每张表必须有 `create_time` / `update_time` 自动填充字段
- 建表、改表必须同步更新 `docs/DATABASE-SCHEMA.md`

### 8.2 数据库变更规范（增量 + 危险语句禁令）

| # | 规则 | 要求 |
|---|------|------|
| 1 | 禁止线上危险语句 | **禁止对线上库执行 `DROP`、无 `WHERE` 的 `DELETE`/`UPDATE`、无备份 `ALTER`** |
| 2 | 增量 SQL 脚本 | 所有变更走增量 SQL（新增脚本），禁止直接改动线上库表结构 |
| 3 | 审批 | DDL / DML 变更必须走 §十四 审批，同步更新文档 |
| 4 | 上线顺序 | 先脚本、再文档、最后代码对齐，三方一致 |
| 5 | 大表操作 | `ALTER` 大表评估锁表与耗时，需在低峰执行 |
| 6 | 数据一致性 | 字段精度（如 `rise_fall_rate` 2 位小数）、注释、类型与代码 Entity 对齐 |

### 8.3 MyBatis-Plus 开发规范

- Entity 继承 `BaseEntity`，字段映射以 init.sql 列名为准（`@TableField("列名")`），**全部显式标注**
- Mapper 继承 `BaseMapper`，Service 继承 `IService` / `ServiceImpl`
- 自动填充 `create_time` / `update_time`
- 分页插件已全局配置，各域**无需重复配置**
- 若表缺少某列（如 `del_flag`），使用 `@TableField(exist = false)` 排除，并确保删除操作走物理删除（BaseEntity 带 `@TableLogic`，无 `del_flag` 的子表须走物理删除，见 §17）
- 禁止 `${}` 拼接用户输入；动态条件用 `Wrapper` / `#{}` 参数绑定
- `select` 只查需要的列，禁止 `select *`

### 8.4 分页与查询

- 列表查询必须分页：`BeanConvertUtil.convertPage(page, XxxVO.class)` / `IPage`
- 禁止全表 `list()` 后内存过滤

### 8.5 SQL 黑名单

- 禁止 `DROP TABLE` / `TRUNCATE`（除审批通过的初始化）
- 禁止无条件 `UPDATE` / `DELETE`（必须带主键或明确 WHERE）
- 禁止 `SELECT *`、禁止 SQL 拼接注入
- 禁止在事务内长事务锁表、禁止无索引列做 WHERE 条件

---

## 九、Vue3 + TypeScript 前端全套编码规约

### 9.1 目录与分层

```
front/src
├── api/          # 接口层（api/index.ts 为 axios 封装，按业务域分文件）
├── store/        # Pinia 状态（store/index.ts、marketSSE.ts 等）
├── router/       # 路由 + 守卫
├── views/        # 页面
├── layouts/      # 布局（UserLayout / AdminLayout）
├── components/   # 通用组件（二次封装）
├── composables/  # 组合式函数（useFormGuard / useCrudPage / useAdminDashboard）
├── utils/        # 工具（auth.ts / format.ts / object.ts / uuid.ts）
└── types/        # 全局类型
```

### 9.2 TypeScript 类型规范

- **禁止 `any` 类型**（特殊情况必须 `// eslint-disable` 并注释原因）
- 接口返回数据必须有类型定义（`types/` 或各 api 文件内 `interface`）
- 常量与枚举用 `as const` / `enum`，禁止魔法字符串散落
- 组件 props 用 `defineProps<Type>()`，事件用 `defineEmits<Type>()`，全类型化

### 9.3 Pinia 状态管理

- 全局登录态、用户信息、角色放 Pinia（`useUserStore`），禁止散落 sessionStorage 业务状态
- 模块独立 store（如 `marketSSE`），避免单一巨大 store
- store 内只放跨组件共享状态，页面局部状态用组件内 `ref`
- 异步逻辑放 action，禁止在组件里裸写复杂接口流程

### 9.4 Axios 拦截封装（api/index.ts）

- 统一走 `api/index.ts` 封装的 `request`：`baseURL: '/api/v1'`，timeout 30s
- **必须**统一处理：业务码 `code` 判断、401 静默续期（refresh_token）+ 重放、错误 `ElMessage` 提示
- **禁止**在业务代码里绕过封装直接 `axios.create`
- 写操作自动带防重放头（`X-Timestamp` / `X-Nonce`），兼容后端 `@AntiReplay`
- 公开路径（登录/注册/验证码）401 只提示，不触发续期/登出（`PUBLIC_AUTH_PATHS`）

### 9.5 路由守卫鉴权（router/index.ts）

- 路由 `meta` 约定：`requiresAuth`（需登录）、`requiresAdmin`（需管理员）、`keepAlive`（是否缓存，默认 true）、`title`（页面标题）
- 全局前置守卫：登录态校验 → token 过期检测 → 管理员权限校验 → 动态标题，统一实现，**禁止各页面自行鉴权**
- 登录页、注册页、公开页面 `requiresAuth: false`
- 401 / 404 / 403 错误页统一兜底

### 9.6 表单防重复提交

- 提交按钮绑定 loading 状态，提交期间禁用（`useFormGuard` 封装）
- 提交成功 / 失败后重置，禁止用户在请求未返回时连点
- 关键操作（下单、资金）前端防重 + 后端 `@AntiReplay` / 幂等双重保障

### 9.7 组件二次封装

- 通用能力（表格 CRUD、分页、表单）封装为可复用组件 / composable（`useCrudPage`），禁止各页面重复造轮子
- Element Plus 组件按需引入，全局统一样式主题
- 组件 props 类型化、事件向上抛，禁止组件内部直接操作全局 store 以外状态

### 9.8 KeepAlive 缓存管控

- 路由 `meta.keepAlive` 控制页面缓存（默认 true），交易、登录等敏感/实时页设 `false` 不缓存
- 使用 `<KeepAlive>` 时，`onActivated` / `onDeactivated` 处理数据刷新，禁止缓存页面残留过期数据
- 退出登录清空缓存页与用户状态

### 9.9 前端安全

- Token 存 httpOnly Cookie，前端**禁止**在 localStorage 存明文 token（仅存登录标记）
- 用户输入渲染前转义，防 XSS；动态 class/style 绑定防注入
- 禁止在控制台、日志打印 Token、密码、用户敏感信息

### 9.10 前端黑名单

- 禁止 `any`、禁止 `@ts-ignore` 滥用
- 禁止直接 `axios` 绕过封装、禁止裸 `fetch`
- 禁止页面内裸写鉴权逻辑（走路由守卫）
- 禁止 `v-html` 渲染用户输入
- 禁止无类型裸接口调用、禁止魔法字符串散落

---

## 十、通用代码模式与可复用代码示例库

### 10.1 Entity → VO 转换

```java
return Result.success(BeanConvertUtil.convert(entity, XxxVO.class));
// 批量：BeanConvertUtil.convertList(list, XxxVO.class)
// 分页：BeanConvertUtil.convertPage(page, XxxVO.class)
```

### 10.2 update null 安全

```java
public boolean updateXxx(Long id, XxxDTO dto) {
    Xxx entity = getById(id);
    if (entity == null) return false;
    BeanConvertUtil.copyNonNullProperties(dto, entity);
    entity.setId(id);
    return updateById(entity);
}
```

### 10.3 业务异常

```java
throw new ServiceException(400, "参数不合法");
```

### 10.4 Contract 接口（替代 Feign，跨域复用）

```java
// wealth-common 定义接口 → wealth-service 中 @Autowired 注入实现
```

### 10.5 JWT 登录流程

```
UmsAdminController.login(LoginDTO) → UmsAdminService 验证（BCrypt）→ 返回 JWT
JWT 写入 httpOnly Cookie（wealth_token），refresh_token 用于静默续期
后续请求由 LoginInterceptor / JwtAuthGlobalFilter 拦截校验
```

### 10.6 Swagger 注解

```java
@Tag(name = "模块管理")
@Operation(summary = "操作描述")
```

### 10.7 常量定义

```java
private static final int COOKIE_MAX_AGE_SECONDS = 1800;
private static final int MAX_LOGIN_ATTEMPTS = 5;
private static final long LOCK_DURATION_MINUTES = 15;
```

### 10.8 Redis 缓存防穿透模式（空值缓存 + 互斥重建）

```java
Object cache = redisUtil.get(key);
if (cache != null) return cache;                      // 命中（含空值占位）
if (redisUtil.setIfAbsent("lock:" + key, reqId, 3, TimeUnit.SECONDS)) {  // 互斥锁
    try {
        Object db = mapper.queryById(id);             // 查库
        redisUtil.set(key, db == null ? EMPTY : db, baseTtl + randomJitter(), TimeUnit.SECONDS);
        return db;
    } finally {
        redisUtil.delete("lock:" + key);              // 释放锁
    }
}
// 未抢到锁：短暂自旋后重读缓存
Thread.sleep(100);
return redisUtil.get(key);
```

### 10.9 分布式锁模式（SET NX EX）

```java
String lockKey = "lock:trade:" + orderId;
String reqId = UUID.randomUUID().toString();
if (Boolean.TRUE.equals(redisUtil.setIfAbsent(lockKey, reqId, 30, TimeUnit.SECONDS))) {
    try {
        // 业务逻辑
    } finally {
        if (reqId.equals(redisUtil.get(lockKey))) {   // 校验持有者，防误删他人锁
            redisUtil.delete(lockKey);
        }
    }
}
```

### 10.10 分页查询模式

```java
Page<Xxx> page = new Page<>(current, size);
LambdaQueryWrapper<Xxx> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(Xxx::getStatus, status).orderByDesc(Xxx::getCreateTime);
return Result.success(BeanConvertUtil.convertPage(xxxMapper.selectPage(page, wrapper), XxxVO.class));
```

---

## 十一、Git 约定式提交规范

### 11.1 提交格式

遵循 [约定式提交](https://www.conventionalcommits.org/)：

```
<type>(<scope>): <description>
```

| type | 说明 |
|------|------|
| feat / fix | 新功能 / 修复 |
| docs / style | 文档 / 格式 |
| refactor / perf | 重构 / 性能 |
| test / chore | 测试 / 构建 |
| ci / build / revert | CI / 构建系统 / 回退 |

scope（可选）：common / gateway / service
description：命令式语气、首字母小写、末尾无句号

```
feat(service): 添加产品分页查询接口
fix(service): 修复交易委托金额计算精度问题
docs: 更新代码规范手册
```

### 11.2 AI 禁止自动 git 推送约束

- **提交和推送必须等用户明确指令**，禁止自动执行 `git commit` / `git push`（最高红线）
- AI 完成改动后仅汇报 diff 与验证结果，由用户决定是否提交
- 禁止 `--no-verify` 跳过钩子、禁止 amend 已推送提交
- 禁止一次提交混入无关改动；依赖、配置改动单独提交

---

## 十二、项目开发 Shell 命令手册

```bash
# 1. 编译公共模块（修改 common 后必须执行）
mvn clean install -pl backend/wealth-common -DskipTests

# 2. 全量编译 / 安装
mvn clean install -DskipTests

# 3. 运行（先 gateway 后 service）
mvn spring-boot:run -pl backend/wealth-gateway
mvn spring-boot:run -pl backend/wealth-service

# 4. 前端
cd front && npm install && npx vite

# 5. 测试
mvn test -pl backend/wealth-service -DskipTests=false
mvn test -pl backend/wealth-service -Dtest=XxxTest -DskipTests=false

# 6. 本地基础设施（Docker：mysql / redis / nginx）
docker compose up -d
```

> 配置修改需审批：application.yml / application-prod.yml / pom.xml 默认锁定，改前询问用户并给出强理由。

---

## 十三、分层测试规范

### 13.1 测试层级与要求

| 层级 | 框架 | 范围 | 要求 |
|------|------|------|------|
| 单元测试 | JUnit 5 + Mockito | Service 单类逻辑 | mock 依赖，只测当前类；每个 Service 方法至少一个正向用例 |
| Controller 测试 | MockMvc | HTTP 状态码 + 返回 JSON 结构 | 验证入参校验、鉴权、错误码 |
| Mapper 测试 | `@MyBatisPlusTest` / `@SpringBootTest` | SQL 正确性 | 需要数据库环境 |
| 接口测试 | MockMvc / 冒烟脚本 | 端到端接口 | 覆盖主流程 + 异常路径 |
| 压力测试 | 压测工具（JMeter 等） | 性能与限流 | 上线前对核心接口压测，验证限流生效 |

### 13.2 测试命名与规范

- 命名：`{被测类名}Test.java` 放在 `src/test/java`，包路径与被测类一致
- 方法命名：`{方法名}_should_{预期行为}`（如 `login_should_return_token_when_password_correct`）
- 分支逻辑必须覆盖异常路径（参数非法、资源不存在、状态冲突等）
- 测试涉及敏感数据脱敏；禁止测试真实生产数据
- 接口测试必须断言 `code` 与关键 `data` 字段，禁止只断言 HTTP 200

### 13.3 测试黑名单

- 禁止跳过测试上线（`-DskipTests` 仅限本地编译）
- 禁止只写正向用例、不覆盖异常分支
- 禁止 mock 掉被测类自身逻辑、禁止用假断言掩盖失败
- 禁止在测试中 hardcode 真实密码 / 生产连接

---

## 十四、所有类型变更审批管控流程

### 14.1 变更分类与审批

| 变更类型 | 审批要求 | 说明 |
|---------|---------|------|
| 配置变更（application*.yml / .env） | 必须审批 | 给出改动原因、影响面、回滚方案（§2.1） |
| 数据库 DDL / 建表（init.sql） | 必须审批 | 增量脚本 + 同步 DATABASE-SCHEMA.md，禁止直接改线上表 |
| 数据库 DML（批量数据） | 必须审批 | 带上限、备份、回滚语句 |
| 接口变更（新增/修改/废弃） | 评审 | 新接口走 §18.3 版本与废弃规范；权限变更走 §2.1 |
| 依赖升级 / 新增依赖 | 必须审批 | 安全评估 + 版本兼容 + 可回滚（§7.12） |
| 拦截器 / 权限逻辑 | 必须审批 | 全链路评估权限影响 |
| 文档规范（CLAUDE.md / CODE-STANDARDS.md） | 必须审批 | 防规则漂移 |
| 常规业务代码 | 自检 + 测试 | 遵守本文档全部规范 |

### 14.2 审批前置信息（AI 必须提供）

```
1. 改动原因（为什么改）
2. 风险评估（影响哪些环境/接口/数据，回滚难度）
3. 回滚方案（如何恢复到改动前）
4. 验证计划（如何证明改动正确）
```

---

## 十五、环境启动、冒烟验证检查清单

### 15.1 启动前置检查

- [ ] Docker 容器运行（mysql / redis / nginx）
- [ ] `.env` 已配置（根目录 + gateway + service）
- [ ] 全量编译通过
- [ ] 启动顺序：gateway → wealth-service
- [ ] 日志出现 "HikariPool-1 - Start completed"
- [ ] 前端可访问

### 15.2 冒烟验证

- [ ] 冒烟：`POST /system/umsAdmin/login` 返回 JWT
- [ ] 登录后可访问需鉴权接口
- [ ] 前端首页 / 产品 / 行情页面可访问
- [ ] 生产环境验证 Swagger 已关闭、SQL 打印已关闭

---

## 十六、模块化代码自检扫描清单（七大分组）

> AI 完成改动后逐项自检，全部通过才可汇报完成。

### 16.1 实体类

- [ ] `@EqualsAndHashCode(callSuper=true)` 或 `@Getter @Setter` + callSuper
- [ ] 实体字段全部标注 `@TableField("列名")`，与 init.sql 一致
- [ ] 无 `@TableLogic` 与 `@TableField(exist = false)` 冲突（物理删除检查）
- [ ] 金额字段用 `BigDecimal` / `decimal`
- [ ] 敏感字段脱敏注解（`@JsonIgnore` / `@ToString.Exclude`）

### 16.2 Controller

- [ ] 无业务逻辑（Token 解析、权限校验等下沉到 Service）
- [ ] 统一 `Result` 返回 + Swagger 注解（`@Tag` / `@Operation`）
- [ ] `@RequestBody` 有 `@Valid`
- [ ] 无方法级 JavaDoc 冗余
- [ ] 写接口评估了防重提交 / 权限

### 16.3 Service

- [ ] 写操作 `@Transactional(rollbackFor = Exception.class)`
- [ ] getById 空值返回 404 / 抛 `ServiceException`
- [ ] list() 带分页
- [ ] update 用 `BeanConvertUtil.copyNonNullProperties`
- [ ] 日志规范（级别、业务 id、异常堆栈、脱敏）
- [ ] 无同类 `this` 调用导致事务失效

### 16.4 Mapper

- [ ] 继承 `BaseMapper`，无 `${}` 注入
- [ ] 无 `SELECT *`、无全表扫描无索引条件
- [ ] 分页走 IPage，禁止内存过滤大集合

### 16.5 配置

- [ ] redis 配置用 `spring.data.redis.*`
- [ ] 链路追踪用 `management.zipkin.tracing.endpoint`
- [ ] prod 无 Swagger 开启、无 SQL 打印
- [ ] 密钥走环境变量，无 hardcode

### 16.6 安全

- [ ] 新增接口权限评估（白名单 `AuthConstant.PERMIT_ALL_URLS` 是否必需）
- [ ] 密码 BCrypt 加密，Token 传输安全
- [ ] 无敏感信息日志泄露、无异常堆栈外泄
- [ ] 越权、注入、XSS、重放防护到位

### 16.7 前端

- [ ] 无 `any` / `@ts-ignore`
- [ ] 接口走 api 封装，路由走守卫
- [ ] 表单防重复提交、敏感信息不落 localStorage
- [ ] KeepAlive 缓存页数据刷新正确

### 16.8 通用项

- [ ] 无通配符导入、无全限定类名字段
- [ ] 魔法值已抽取常量
- [ ] 构造器注入使用 `@RequiredArgsConstructor`，Logger 使用 `@Slf4j`
- [ ] 异常信息中英文统一（全中文）
- [ ] 拦截器 pathPatterns 与 context-path 一致（不能加前缀）

---

## 十七、分类式历史踩坑教训

### 17.1 数据库坑

| # | 教训 | 要点 |
|---|------|------|
| 1 | **文档链检查** | 追踪到最底层数据源（init.sql、数据库、代码）再动手，禁止凭记忆改表 |
| 2 | **三方一致** | 文档、代码、数据库三方对齐后再改；改表必须同步 DATABASE-SCHEMA.md |
| 3 | **@TableLogic 继承冲突** | BaseEntity 带 `@TableLogic`，无 `del_flag` 列的子表须走物理删除 |
| 4 | **增量 SQL** | 禁止直接改动线上表结构，必须增量脚本 + 审批 |
| 5 | **金额精度** | 金额/涨跌幅用 `BigDecimal`/`decimal`，对齐 2 位小数，禁止 double 计算 |

### 17.2 后端编码坑

| # | 教训 | 要点 |
|---|------|------|
| 1 | **Controller 职责边界** | 所有 Bearer Token 解析/校验必须下沉到 Service，Controller 只路由 |
| 2 | **批量改一个先验证** | 改一个 → 验证 → 再批量，避免连锁错误 |
| 3 | **事务失效自调用** | 同类 `this` 调用不触发 `@Transactional` 代理 |
| 4 | **异常吞没** | 禁止空 catch；`log.error("msg", e)` 保留堆栈 |
| 5 | **跨域切换先读文件** | 从前端切后端 / 跨模块时，先读关键文件建立上下文 |

### 17.3 配置坑

| # | 教训 | 要点 |
|---|------|------|
| 1 | **prod 开关** | 生产环境必须关闭 Swagger 与 SQL 打印，管理端点仅暴露 health,info |
| 2 | **密钥 hardcode** | 密码/IP/密钥禁止写入配置与代码，走环境变量 |
| 3 | **环境隔离** | dev/test/prod 独立数据库与 Redis，禁止混用连接串 |

### 17.4 前端坑

| # | 教训 | 要点 |
|---|------|------|
| 1 | **Token 存储** | JWT 存 httpOnly Cookie，禁止 localStorage 明文存 token |
| 2 | **表单连点** | 提交按钮必须 loading / 防抖，防止重复下单 |
| 3 | **缓存过期数据** | KeepAlive 页面在 `onActivated` 刷新数据，避免残留过期内容 |

### 17.5 线上故障坑

| # | 教训 | 要点 |
|---|------|------|
| 1 | **改完主动提验证** | 涉及 DB/数据流/配置，主动提议启动验证与冒烟 |
| 2 | **缓存故障** | Redis 不可用必须降级（`safeExecute`），防止穿透/雪崩拖垮主链路 |
| 3 | **异常信息语言统一** | 所有异常提示使用中文，禁止中英文混杂 |
| 4 | **回滚预案** | 高危变更（配置/DDL/依赖）必须事前准备回滚方案 |

---

## 十八、附录

### 18.1 全局命名规范

| 类别 | 规范 | 示例 |
|------|------|------|
| 表名 | 业务域前缀 + `snake_case` | `wea_trade_order` |
| 字段 | `snake_case` | `create_time` |
| Java 类 | PascalCase | `UmsAdminController` |
| Java 方法/变量 | camelCase | `getUserById` |
| 常量 | UPPER_SNAKE_CASE | `MAX_LOGIN_ATTEMPTS` |
| 包名 | `com.wealth.{common\|platform}.{domain}` | `com.wealth.platform.trade` |
| Mapper XML | 与 Mapper 接口同包同名 | `TradeOrderMapper.xml` |
| 前端组件 | PascalCase | `ProductTable.vue` |
| 前端变量/函数 | camelCase | `fetchProductList` |
| API 路径 | RESTful 复数名词 | `/api/v1/product` |

### 18.2 全局错误码规约

统一返回结构：`{code, message, data}`。`ResultCode`（`com.wealth.common.result`）现有：

| code | message | 说明 |
|------|---------|------|
| 200 | 操作成功 | 成功 |
| 400 | 参数错误 | 入参校验失败 |
| 401 | Token已过期 / Token无效 | 认证失败 |

**错误码分段规约（新增错误码按此扩展，禁止随意定义）：**

| 段 | 范围 | 归属 |
|----|------|------|
| 通用 | 200 ~ 499 | 公共（沿用 ResultCode） |
| 用户域 | 1xxx | user |
| 产品/行情域 | 2xxx | product |
| 交易域 | 3xxx | trade |
| 资讯/消息域 | 4xxx | message |
| 后台权限域 | 5xxx | system |
| 内部/基础设施 | 6xxx | 框架、缓存、DB |

- 新增错误码必须在 `ResultCode`（或分域枚举）中登记，禁止在代码里裸写数字错误码
- message 统一中文，面向用户可理解，禁止暴露堆栈/内部细节
- 业务校验失败统一抛 `ServiceException(code, message)`，由 `GlobalExceptionHandler` 收敛

### 18.3 废弃接口管理规范

| # | 规则 | 要求 |
|---|------|------|
| 1 | 版本管控 | 新接口 `@Operation` 标注版本；不兼容变更升版本（如 `/api/v2/...`），禁止直接覆盖旧接口语义 |
| 2 | 废弃流程 | ① 标记 `@Deprecated` + Swagger 标注废弃 → ② 观察期至少一个版本 → ③ 审批后下线 |
| 3 | 下线确认 | 下线前确认无调用方（前端/其他服务），网关 `PERMIT_ALL_URLS` 同步清理 |
| 4 | 文档同步 | 接口废弃、下线必须同步更新 Swagger / docs |
| 5 | 禁止 | 禁止无替代方案直接删接口、禁止静默修改既有接口契约 |
