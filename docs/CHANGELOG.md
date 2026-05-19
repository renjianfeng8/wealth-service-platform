# 更新日志

> 本日志仅记录项目级别的更新、功能新增、架构调整与优化。
> 具体的 Bug 修复细节请参阅 [Bug.md](./Bug.md)。

---

## v1.7.0 (2026-05-19 — 当前开发版)

### 基础设施安全认证

- **Nacos 认证启用**：`NACOS_AUTH_ENABLE=true`，所有服务通过配置的用户名密码连接 Nacos
- **Elasticsearch 安全认证启用**：`xpack.security.enabled=true`，search 服务连接 ES 需提供用户名密码
- `.env` 文件从 git 历史中永久清理（使用 filter-branch 重写全部历史，移除泄露的凭据）
- `docker-compose.yml` 历史中硬编码的 `123456` 密码已替换为环境变量引用
- `seata-config/application.yml` 中硬编码的 `secretKey` 已改为环境变量注入（`${SEATA_SECRET_KEY}`）

### JWT 双 Token 机制

- **双 Token 登录**：登录返回 `access_token`（30 分钟有效）+ `refresh_token`（7 天有效）
- **Refresh 端点**：`POST /umsAdmin/refresh` 支持无感续期
- **安全增强**：refresh_token 一次性使用（防重放），jti 存入 Redis 支持手动吊销

### 安全加固（XSS + 暴力破解防护）

- **XSS 全局过滤器**：`XssFilter` + `StringXssDeserializer` 覆盖 GET/POST 参数和 JSON 请求体
- **登录暴力破解防护**：连续 5 次失败锁定 15 分钟（Redis），含验证码支持
- **Swagger/Knife4j 白名单移除**：三层防护（Gateway → LoginInterceptor → PermissionCheckInterceptor）

### 跨模块权限控制

- **PermissionCheckInterceptor**：所有业务模块的 POST/PUT/DELETE 请求统一调用 system 模块 RBAC 鉴权
- **PermissionCheckFeignClient**：Feign 调用链路（business → system → 权限判定）

### 架构优化

- 抽取分页 VO 转换通用方法，消除 Controller 层重复代码
- 修复全模块中文注释乱码、YAML 重复键
- 统一 update 方法 null 安全更新模式（`BeanConvertUtil.copyNonNullProperties`）
- 添加 `@Validated` 注解到所有 Controller 类

### 文档

- 添加[生产环境部署前全面评估报告](docs/production-readiness-assessment.md)
- 修复评估报告发现的 20+ 个安全与稳定性问题
- 文档治理：标准化所有 `.md` 文件，精简 README 与 Startup.md 重复内容

---

## v1.6.3 (2026-05-18)

### 文档精简

- 精简 README.md 启动说明，移除与 Startup.md 重复的 Nacos 配置内容
- 启动文档结构优化，减少维护负担

---

## v1.6.2 (2026-05-18)

### 文档治理与工程结构优化

- 全项目文档标准化治理（README、Startup、架构文档一致性对齐）
- 工程结构优化，移除冗余文件
- 文件编码与格式标准化

---

## v1.6.1 (2026-05-18)

### 全模块编码与配置修复

- **中文注释乱码修复**：修复全模块 Java 文件中中文注释显示为乱码的问题
- **YAML 重复键修复**：清理各模块 application.yml 中重复的配置键
- **update 模式标准化**：统一各模块 ServiceImpl 中 update 方法的 null 安全更新模式

---

## v1.6.0 (2026-05-18)

### SSE 实时行情推送体系

前端行情页（market）和自选页（favorite）的数据获取方式从 HTTP 轮询升级为 SSE（Server-Sent Events）服务端主动推送，实现行情数据实时更新。

#### 新增：SSE 推送服务端

- **MarketDataPushService** — SSE 发射器管理，使用 `CopyOnWriteArrayList` 线程安全存储客户端连接，支持 `createEmitter`（86400s 超时）、`broadcastMarketUpdate`（全量广播）、`getEmitterCount`（监控连接数）
- **MarketDataSimulationService** — `@Scheduled(fixedRate = 2000)` 定时模拟行情变化，高斯随机游走（约 0.2% 波动），先更新数据库再广播全量快照，`@PostConstruct` 启动时加载 8 条产品行情
- SSE 端点 `GET /WeaMarketData/sse?token=xxx`，先推送全量快照再维持长连接，JWT Token 通过查询参数传递（EventSource 限制），Controller 内手动校验
- 端点路径从 LoginInterceptor 排除，Gateway JWT 过滤器放行，Nacos AuthConstant 同步更新白名单

#### 新增：Nginx SSE 反向代理配置

新增独立 location `/api/v1/product/WeaMarketData/sse`：
- `proxy_http_version 1.1` + `proxy_set_header Connection ''` 禁用 HTTP/1.1 连接复用
- `proxy_buffering off` + `proxy_cache off` 禁用缓冲
- `proxy_read_timeout 86400s` 支持长连接

#### 新增：前端 SSE 集成

- 两个 SPA 各新增 `utils/sse.ts`：`createMarketSSE()`（EventSource 工厂）、`onMarketUpdate()`（事件注册）
- **用户行情页**（`front-user/views/market/index.vue`）：`onMounted` 建立 SSE 连接，`onUnmounted` 关闭，按 `productCode` 匹配更新表格行
- **用户自选页**（`front-user/views/favorite/index.vue`）：SSE 实时行情替代 `getMarketDataList` 轮询丰富价格数据
- **管理员行情页**（`front/views/market/index.vue`）：同上 SSE 实时更新

---

## v1.5.0 (2026-05-17)

### 可观测性体系全面落地

完成日志（logback）+ 链路追踪（Zipkin）+ 监控指标（Prometheus+Grafana）三大可观测性支柱：

#### 新增：链路追踪（Micrometer Tracing + Brave + Zipkin）

- 集成 `micrometer-tracing-bridge-brave` + `zipkin-sender-okhttp3`，所有 8 个微服务自动拦截跨服务调用
- Gateway 及通过 wealth-common 引入的业务模块均携带追踪依赖
- Zipkin 容器 `openzipkin/zipkin:latest`（端口 9411）接收全链路 Span 数据
- **配置注意**：Spring Boot 3.x 须使用 `management.zipkin.tracing.endpoint` 而非旧版 `zipkin.base-url`

#### 新增：监控告警（Prometheus + Grafana）

- 添加 `micrometer-registry-prometheus` 依赖，所有服务暴露 `/actuator/prometheus` 端点
- Nacos `wealth-shared.yaml` 配置 `management.endpoints.web.exposure.include=health,info,prometheus`
- `prometheus.yml` 配置 8 个服务的抓取规则（含各模块 context-path）
- docker-compose 新增 Prometheus（端口 9090）和 Grafana（端口 3001，admin/admin）

#### 审计修复（Nacos 配置属性更正）

- 更正 `wealth-shared.yaml` 中无效的 `zipkin.base-url` 为 `management.zipkin.tracing.endpoint`

#### 新增：HTTPS 证书配置（Nginx SSL）

- 生成自签名证书（RSA 2048，CN=localhost，有效期 1 年），覆盖开发/演示环境
- `nginx.conf` 新增 `listen 443 ssl` 服务块，配置 TLSv1.2/1.3、安全加密套件、SSL 会话缓存
- HTTP 80 端口全部 301 重定向至 HTTPS，杜绝明文传输
- 添加 `resolver 127.0.0.11` + 变量化 `proxy_pass`，解决上游服务未就绪时 nginx 启动失败问题
- 新增 `ssl/gen-cert.ps1` 证书重新生成脚本
- 同步更新 `.gitignore` 排除证书目录

#### 基础设施修复

- **docker-compose YAML 语法修复**：`*image-prefix` 锚点无法在字符串中拼接，改为内联完整镜像名（`ghcr.io/renjianfeng8/wealth-service-platform/wealth-{module}:latest`）
- **nginx 上游 DNS 启动时解析修复**：因 `frontend` 镜像需 ghcr.io 认证无法拉取，nginx 启动时因 `host not found in upstream "frontend"` 崩溃。通过 `resolver` + 变量 `proxy_pass` 改为运行时动态解析，服务未就绪时返回 502 而非 crash

#### 新增：MySQL 数据库备份策略

- `mysql/conf.d/binlog.cnf`：启用 binlog（ROW 格式，保留 7 天），支持 PITR 时间点恢复
- `scripts/backup-mysql.sh`：手动备份脚本（`docker exec` 方式，Linux/macOS）
- `scripts/backup-mysql.ps1`：手动备份脚本（Windows PowerShell）
- `scripts/restore-mysql.sh`：恢复脚本（带确认提示，支持 .gz 自动解压）
- docker-compose 新增 `mysql-backup` 服务：基于 alpine + mysql-client，每日 02:00 自动全量备份，保留 7 天
- `.gitignore` 添加 `backups/*.sql.gz` 排除备份文件

#### 新增：交易委托幂等性

- `constant/OrderStatusEnum.java`：订单状态枚举（已提交/已成交/已撤销）+ 合法转换表（已提交→已成交，已提交→已撤销）
- `FinTradeOrderDTO` 新增 `idempotentKey` 字段：客户端生成 UUID 传入，后端 Redis 校验（TTL 24h），重复提交返回 400
- `FinTradeOrderServiceImpl` 注入 `RedisUtil`，createOrder 前执行幂等性校验
- `FinTradeOrderStatusDTO` + `PUT /{id}/status` 端点：订单状态变更走状态机校验，非法转换返回 400
- 不传幂等键时向下兼容旧客户端，仅跳过校验

#### 文档更新

- **Startup.md**：中间件新增 Zipkin/Prometheus/Grafana/Sentinel/Seata/Nginx，Nacos 配置示例同步，端口表扩充，FAQ 补充链路追踪与监控排查项，依赖图增加可观测性层
- **docs/nacos-config-reference.md**：新增 Nacos 配置中心参考文档
- **docs/go-live-audit-report.md**：修复率更新为 25/30，HTTPS 标记已修复
- **CHANGELOG.md**：本次更新日志

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

新增独立用户门户 `front-user/`，面向普通用户，与现有管理员后台 `front/` 构成完整的双端架构体系。同时配备 Playwright E2E 自动化测试套件，覆盖全部用户端业务场景。
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
- 双端架构说明：管理员后台（`front/`, 端口 3000） 用户前台（`front-user/`, 端口 3001）
### 测试数据补充

在 `init.sql` 中追加 12 张表的完整测试数据，共计 83 条记录，覆盖全业务场景：

| 表名 | 记录数 | 说明 |
|------|:-----:|------|
| `sys_user` | 5 | 前台测试用户（含 zhangwei）|
| `wea_product` | 12 | 不同类别金融产品 |
| `wea_market_data` | 15 | 行情历史数据 |
| `wea_user_favorite` | 5 | 用户自选关联 |
| `wea_trade_order` | 8 | 不同状态交易委托 |
| `wea_news` | 10 | 财经资讯文章 |
| `wea_message` | 8 | 站内消息 |
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

## v1.3.0 (2026-04-28)

### 双端初步整合

- 完成 `finance-mid-platform` 初始模块搭建
- 基础微服务架构：gateway → system → user → product → account → trade → message → search
- Spring Cloud Alibaba 体系集成（Nacos 注册中心 + 配置中心）
- 统一返回格式 `Result<T>` 与全局异常处理落地
- MyBatis-Plus 分页插件全局配置
- JWT 认证体系搭建
- 管理员后台 Crud 基础框架（Element Plus 表格/表单/弹窗）
- 8 个微服务模块全部可启动并注册到 Nacos

---

## v1.2.0 (2026-04-15)

### 基础设施搭建

- Spring Boot 3.3.5 + Spring Cloud 2023.0.3 项目初始化
- Nacos 2.3.2 Docker 化部署
- MySQL 8.0.37 Docker 化部署
- Redis / RabbitMQ / Elasticsearch Docker 化部署
- Nginx 反向代理配置
- Swagger + Knife4j API 文档集成
- Maven 多模块项目结构搭建

---

## v1.1.0 (2026-04-01)

### 架构设计

- 项目整体技术选型论证
- 数据库表结构设计（12 张核心表）
- API 接口规范定义
- 双端架构规划（管理员后台 + 用户前台）

---

## v1.0.0 (2026-03-15)

### 项目初始化

- 项目立项与仓库初始化
- README / CLAUDE.md / CONTRIBUTING.md 等基础文档
- Docker 编排文件
- Git 工作流与提交规范制定
