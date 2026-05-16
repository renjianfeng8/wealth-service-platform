# 上线前完整审计报告：wealth-service-platform

> 审计日期：2026-05-16
> 审计范围：全部 8 个微服务 + 2 个前端 SPA + 基础设施（Gateway/Docker/Nginx/中间件）
> 审计标准：企业级微服务 + 金融中台上线标准

---

## 1. 项目结构与模块分析

| 模块 | 路径 | 端口 | 职责 | 估算规模 |
|------|------|------|------|---------|
| wealth-common | common | — | 公共组件（BaseEntity、JWT、Redis、FeignClient、异常处理、自动填充） | ~25 类 |
| wealth-gateway | gateway | 8080 | Spring Cloud Gateway 路由转发、CORS | ~2 类 |
| wealth-system | system | 8082 | 后台 RBAC（管理员、角色、资源 CRUD + PermissionInterceptor） | ~25 类 |
| wealth-user | user | 8083 | 前端用户注册/登录/CRUD（JWT + BCrypt） | ~10 类 |
| wealth-product | product | 8084 | 产品 + 行情数据 CRUD | ~14 类 |
| wealth-account | account | 8086 | 用户自选关注 CRUD | ~8 类 |
| wealth-trade | trade | 8085 | 交易委托单 CRUD | ~8 类 |
| wealth-message | message | 8087 | 财经资讯 + 站内消息 CRUD + RabbitMQ 配置 | ~14 类 |
| wealth-search | search | 8089 | Elasticsearch 产品搜索 | ~6 类 |
| front | 前端 | 80 | 后台管理 SPA（Vue 3 + Element Plus + ECharts） | ~20 页面 |
| front-user | 前端 | 80 | 用户端 SPA（Vue 3 + Element Plus + Playwright E2E） | — |

**整体规模**：约 100+ Java 类，2 个前端 SPA，8 个微服务，7 个基础设施中间件。属于**教学/演示级**项目，尚未达到生产就绪标准。

---

## 2. 已完成的核心功能

### 基础设施
- Nacos 注册中心 + 配置中心（每个服务通过 bootstrap.yml 集成 shared-configs）
- Nginx 反向代理（API 路由到 gateway，静态资源到前端）
- Docker Compose 一键部署（7 个中间件 + 8 个后端 + 1 个前端）
- 每个服务有独立 Dockerfile（eclipse-temurin:21-jre-alpine + 多阶段构建）
- GitHub Actions CI（编译 + 测试 + 打包）

### 网关
- Spring Cloud Gateway 路由（7 条路由：system/user/product/account/trade/message/search）
- 全局 CORS 配置（白名单 localhost:3000/8080/127.0.0.1:3000）
- LoadBalancer 集成（lb:// 负载均衡）

### 后台权限管理（wealth-system）
- JWT 登录（jjwt 0.11.5 + HMAC-SHA256）
- RBAC 四表权限体系（ums_admin → ums_admin_role_relation → ums_role → ums_role_resource_relation → ums_resource）
- PermissionInterceptor（AntPathMatcher + DB 驱动权限校验）
- 管理员/角色/资源 CRUD + 分页

### 用户系统（wealth-user）
- 用户注册/登录/密码重置
- BCrypt 密码加密
- JWT Token 生成（LoginVO 含 token + userId + nickname）

### 产品管理（wealth-product）
- 产品 CRUD + 分页查询 + 分类筛选
- 行情数据 CRUD + 分页

### 交易委托（wealth-trade）
- 买入/卖出委托 CRUD + 分页筛选
- 委托单号自动生成（UUID）
- null 安全更新（copyNonNullProperties）

### 账户自选（wealth-account）
- 用户自选关注 CRUD
- 重复关注检查

### 消息（wealth-message）
- 财经资讯 CRUD
- 站内消息 CRUD（含已读/未读）
- RabbitMQ 配置（2 个 DirectExchange + 2 个持久化队列）

### 搜索（wealth-search）
- Elasticsearch 8.8.2 集成
- IK 分词器中文搜索
- 产品全文检索（productName match + productCode term）

### 前端
- 后台管理 SPA（10 个路由页面：Dashboard/用户/管理员/角色/资源/产品/行情/交易/自选/消息/资讯/搜索）
- 用户端 SPA + Playwright E2E 测试

---

## 3. 缺失内容（按优先级排序）

### 3.1 必须上线前完成（P0 — 阻断性）

| # | 缺失项 | 严重程度 | 说明 |
|---|--------|---------|------|
| 1 | **FeignClient 服务名不匹配** | **阻断** | `AccountFeignClient` 标注 `@FeignClient("finance-account")` 但 Nacos 注册名为 `wealth-account`；`ProductFeignClient` 标注 `"finance-product"` 但注册名为 `wealth-product`。跨服务调用 100% 失败 |
| 2 | **无分布式事务** | **阻断** | 交易委托创建、自选添加等操作无 Seata 或 TCC 方案。金融场景下资金与订单的一致性无保障 |
| 3 | **熔断限流零实现** | **阻断** | 无 Sentinel/Hystrix/Resilience4j。任何下游服务故障（如 ES 宕机 → search 超时 → gateway 线程池耗尽 → 全站崩溃） |
| 4 | **服务超时无配置** | **阻断** | Feign/OpenFeign 未配置 connectTimeout / readTimeout。默认永不超时，一个慢请求可耗尽连接池 |
| 5 | **用户模块抛出 RuntimeException** | **阻断** | `UserServiceImpl.login()` 多处 `throw new RuntimeException(...)`，全局异常处理器捕获后返回 500 而非 401。导致前端无法正确处理登录失败场景 |
| 6 | **Gateway 无任何认证拦截** | **阻断** | Gateway 仅做路由转发，`/user/**`、`/product/**`、`/trade/**` 等直接透传。外部可直接调用写接口 |
| 7 | **无生产级测试** | **阻断** | 整个项目只有 1 个空的 `FinanceCommonApplicationTests`。CI 的 `mvn test -DskipTests=false` 实际不执行任何断言 |

### 3.2 强烈建议上线前完成（P1 — 高风险）

| # | 缺失项 | 说明 |
|---|--------|------|
| 8 | **权限拦截器无缓存** | PermissionInterceptor 每次请求 4 次 DB 查询，Bug.md 已自认，无缓存上线即性能瓶颈 |
| 9 | **API 无任何防重放/防刷机制** | 无接口限流、无时间戳验证、无签名校验。交易委托、注册接口可被脚本无限调用 |
| 10 | **数据库索引不完整** | `wea_trade_order` 缺少 `order_status` + `trade_type` 联合索引，`wea_message` 的 `idx_user_read` 缺少 `msg_type` |
| 11 | **日志体系空白** | 无 logback-spring.xml 配置，无日志分级/切割/归档，无 ELK/Loki 日志采集 |
| 12 | **链路追踪空白** | 无 Micrometer Tracing / Sleuth + Zipkin，无法排查跨服务慢调用 |
| 13 | **监控/告警空白** | 无 Prometheus + Grafana 指标暴露，无 Spring Boot Actuator 健康端点 |
| 14 | **数据库备份策略空白** | MySQL 无定时备份脚本，无 binlog 保留策略，无灾备方案 |
| 15 | **密码/密钥硬编码** | docker-compose.yml 中 MySQL root 密码 123456、各服务多处硬编码环境变量 |
| 16 | **RabbitMQ 消息可靠性未保障** | 无死信队列(DLX)配置，无消息重试机制，无 Publisher Confirm/Return Callback |
| 17 | **部分 Service 使用 BeanUtils.copyProperties** | `FinProductServiceImpl.updateProduct()` 使用 BeanUtils.copyProperties 导致 null 字段可能覆盖数据库已有值 |

### 3.3 建议补充（P2 — 中等风险）

| # | 缺失项 | 说明 |
|---|--------|------|
| 18 | AuthConstant.PERMIT_ALL_URLS 不完整 | 缺少 user 模块的 `/user/user/login`、`/user/user/register` |
| 19 | ES 数据同步缺失 | 删除重建 ES 索引后，MySQL 数据无法自动同步（Bug-003），需 CDC 或定时任务 |
| 20 | 无 WebSocket/SSE 实时推送 | 行情数据无实时推送机制，前端只能轮询 |
| 21 | 无 API 版本管理 | 所有接口无 v1/v2 前缀 |
| 22 | BCryptPasswordEncoder 重复实例化 | 每个 Service 都 `new BCryptPasswordEncoder()`，应抽取为 Spring Bean |
| 23 | 前端 Token 存储有 XSS 风险 | localStorage 存储 JWT，无 httpOnly Cookie 方案 |
| 24 | Maven Surefire 跳过测试 | 根 pom.xml 强制 `<skipTests>true</skipTests>`，阻断自动化测试质量门禁 |
| 25 | init.sql 数据库名不统一 | SQL 中写 `finance` 但实际连接为 `wealth` |

### 3.4 可选优化（P3 — 低风险/体验）

| # | 缺失项 | 说明 |
|---|--------|------|
| 26 | Knife4j/Swagger 在 gateway 未聚合 | 需分别访问各服务地址查看 API 文档 |
| 27 | CI 中前端仅构建 `front-user`，遗漏 `front` | 后台管理前端无 CI 保障 |
| 28 | 无 Kubernetes 部署清单 | 当前仅 Docker Compose，无 K8s 编排 |
| 29 | 国际化(i18n) | 无中英文切换 |
| 30 | 前端错误监控 | 无 Sentry 等前端监控集成 |

---

## 4. 工程化缺失

| 维度 | 当前状态 | 缺失项 |
|------|---------|--------|
| **Docker** | 8 个 Dockerfile + 1 个 docker-compose.yml + docker-build.ps1 | 镜像未做安全扫描(Trivy)、无 `HEALTHCHECK` 指令、无镜像版本管理策略 |
| **部署脚本** | docker-build.ps1 存在 | 无灰度发布/蓝绿部署、无回滚脚本、无多环境(dev/staging/prod)管理 |
| **配置中心** | Nacos 已集成，shared-configs 配置 | 无配置变更审计、无配置版本管理（Nacos 原生支持但未使用） |
| **网关** | Spring Cloud Gateway + Nacos LB | 无网关级认证过滤器、无限流过滤器、无请求/响应日志过滤器、无重试过滤器 |
| **日志** | 仅 SLF4J 依赖，无 logback 配置 | 无日志分级、无切割策略(SizeBasedTriggeringPolicy)、无 ELK/Loki/Graylog |
| **监控** | 无 | 无 Micrometer + Prometheus + Grafana、无 Spring Boot Admin |
| **链路追踪** | 无 | 无 Micrometer Tracing + Zipkin，问题排查靠"猜" |

---

## 5. 安全缺失

| 维度 | 状态 | 说明 |
|------|------|------|
| **鉴权** | 部分完成 | JWT 存在但无 Token 刷新机制、无黑名单/撤销机制、无设备管理 |
| **权限** | system 模块完成 | 其他模块无 RBAC 保护，PermissionInterceptor 仅在 wealth-system 注册 |
| **密码加密** | BCrypt 完成 | 但 BCryptPasswordEncoder 各 Service 重复 new |
| **防刷限流** | 无 | 登录/注册/交易接口无任何限流，可被暴力破解 |
| **审计日志** | 无 | 无操作审计（谁在何时做了什么）、无敏感操作记录 |
| **防重放** | 无 | 无 nonce/timestamp 校验，请求可被抓包重放 |
| **HTTPS** | 未配置 | Nginx 仅 HTTP 80 端口，无 SSL/TLS 证书 |
| **SQL 注入** | 基本安全 | MyBatis-Plus 参数化查询，但无手写 SQL 审核流程 |
| **XSS/CSRF** | 无防护 | 前端无输入过滤，后端无 CSRF Token |

---

## 6. 高可用缺失

| 维度 | 状态 | 说明 |
|------|------|------|
| **熔断** | ❌ | 无 Sentinel/Hystrix/Resilience4j。任一服务不可用会连锁故障 |
| **限流** | ❌ | 无 Sentinel 流量控制。突发流量直接压垮服务 |
| **超时** | ❌ | Feign 无 connectTimeout/readTimeout 配置，默认永远等待 |
| **重试** | ❌ | 无 Feign 重试配置(Retryer)，临时网络抖动直接失败 |
| **分布式事务** | ❌ | 无 Seata AT/TCC。交易创建过程中若 ES 写入失败，订单已存但搜索无数据 |
| **消息可靠性** | 部分 | RabbitMQ 队列持久化(durable=true)，但无 Publisher Confirm、Return Callback、死信队列(DLX)、消息重试 |
| **幂等性** | ❌ | 交易委托 createOrder 无幂等校验，前端重复提交生成多个订单 |
| **服务容灾** | ❌ | 所有服务单副本，无多 AZ 部署 |

---

## 7. 数据库与中间件缺失

| 维度 | 状态 | 说明 |
|------|------|------|
| **索引** | 不完整 | `wea_trade_order` 缺少 `order_status + trade_type` 联合索引；`wea_message` 缺少 `msg_type` 索引；`wea_market_data` 缺少分页查询索引 |
| **备份** | ❌ | 无 mysqldump 定时任务，无 binlog 保留策略，无 PITR 恢复能力 |
| **分库分表** | ❌ | 所有表在单一 `wealth` 库，无 ShardingSphere，单表超千万行无拆分方案 |
| **ES 索引管理** | ❌ | 无索引生命周期管理(ILM)，无 reindex 脚本，无 mapping 变更流程 |
| **Redis** | 仅基础配置 | 无 Redis Cluster/Sentinel 高可用，无 key 淘汰策略配置，无连接池调优 |
| **初始化脚本** | 有 init.sql | 但数据库名仍写为 `finance`（实际连接为 `wealth`），表名部分未对应新命名规范 |

---

## 8. 上线风险点 TOP 10

| 排名 | 风险点 | 影响 | 可能性 |
|------|--------|------|--------|
| 1 | FeignClient 服务名写死错误（`finance-*` vs `wealth-*`） | 跨服务调用全挂 | 确定 |
| 2 | 无熔断限流 + 无超时 | 任一服务抖动即全站雪崩 | 高 |
| 3 | 无网关认证 | 绕过 gateway 直接访问服务端口即可调用接口 | 高 |
| 4 | 无分布式事务 | 金融核心场景资金对账不平 | 高 |
| 5 | UserServiceImpl 全量 RuntimeException | 业务异常返回 500，前端无法正确提示 | 确定 |
| 6 | 数据库单库无备份 | 误操作/数据损坏无法恢复 | 中 |
| 7 | ES 与 MySQL 数据不一致 | 搜索数据与实际数据不匹配 | 高 |
| 8 | RabbitMQ 无死信/重试 | 消息消费失败静默丢弃，交易通知丢失 | 中 |
| 9 | 无操作审计日志 | 出现安全事件无法溯源 | 中 |
| 10 | Maven 强制跳过测试 | 代码变更无质量门禁 | 确定 |

---

## 9. 最终结论

### 当前状态：**不可上线**

### 必须完成的核心工作（按执行顺序）

| 优先级 | 工作项 | 预估工作量 | 涉及模块 |
|--------|--------|-----------|---------|
| **P0** | 修复 FeignClient 服务名（finance-* → wealth-*） | 0.5 人天 | common（AccountFeignClient, ProductFeignClient） |
| **P0** | 集成 Sentinel（熔断 + 限流 + 热点参数） | 3 人天 | 全部服务 + common |
| **P0** | Gateway 添加全局 JWT 认证过滤器 | 2 人天 | gateway |
| **P0** | 用户模块异常替换为 ServiceException | 0.5 人天 | user |
| **P0** | 配置 Feign 超时 + 重试 | 1 人天 | common |
| **P0** | 补充核心业务单元测试 | 3 人天 | system, user, trade |
| **P1** | PermissionInterceptor 接入 Redis 缓存 | 2 人天 | system |
| **P1** | API 限流 + 防刷（Gateway 层 + Sentinel） | 2 人天 | gateway |
| **P1** | 数据库索引补全 + MySQL 定时备份脚本 | 1 人天 | DBA |
| **P1** | 集成 Micrometer Tracing + Zipkin | 2 人天 | 全部服务 |
| **P1** | Logback 配置（分级/切割/归档）+ ELK 搭建 | 2 人天 | common + 运维 |
| **P1** | Actuator + Prometheus + Grafana 监控 | 2 人天 | common + 运维 |
| **P1** | Nacos/docker-compose 移除硬编码密码 | 1 人天 | Nacos + docker-compose |
| **P1** | RabbitMQ 死信队列 + 重试 + Publisher Confirm | 2 人天 | message + trade |
| **P1** | 交易委托幂等性（唯一键 + 状态机） | 1 人天 | trade |
| **P2** | ES 数据同步（Canal 或 定时任务） | 2 人天 | product + search |
| **P2** | 修复 init.sql 数据库名/表名不一致 | 0.5 人天 | common/sql |
| **P2** | BeanUtils.copyProperties → BeanConvertUtil 统一 | 0.5 人天 | product, account |
| **P2** | 开启 Maven 测试 + 添加 CI 测试门禁 | 1 人天 | 根 pom + CI |
| **P2** | AuthConstant.PERMIT_ALL_URLS 补全 | 0.5 人天 | common |
| **P3** | Gateway Swagger 文档聚合 | 1 人天 | gateway |
| — | **总计** | **~28 人天** | — |

### 上线条件逐项检查清单

- [ ] **FeignClient** 服务名与 Nacos 注册名一致（`wealth-*`）
- [ ] **Gateway** 全局认证过滤器生效，未登录请求返回 401
- [ ] **Sentinel** 熔断限流配置完成并验证通过
- [ ] **Feign** 超时(retry+熔断)配置完成
- [ ] **UserServiceImpl** 异常统一为 ServiceException（拒绝 RuntimeException）
- [ ] **数据库** 索引全部补齐 + 定时备份脚本就绪 + binlog 开启
- [ ] **PermissionInterceptor** 缓存优化完成（非每次 4 次 DB 查询）
- [ ] **链路追踪**（Tracing） + **日志采集**（ELK） + **监控告警**（Prometheus+Grafana）就绪
- [ ] **RabbitMQ** 死信队列 + 重试机制 + Publisher Confirm 配置完成
- [ ] **交易** 幂等性实现（防重复下单）
- [ ] **密码/密钥** 从代码和 docker-compose 中移除，统一由 Nacos/环境变量管理
- [ ] **测试** 核心接口（登陆/注册/交易/产品）覆盖率 ≥ 80%
- [ ] **安全扫描**（Trivy + OWASP）无高危漏洞
- [ ] **日志审计** 操作日志记录完成
- [ ] **HTTPS** 证书配置完成

---

**一句结语**：当前项目处于可演示但不可运行的 Demo 状态（FeignClient 错误就足以阻断全部跨服务调用），距离生产上线约需 **28 人天** 补齐基础设施和安全缺项。建议优先修复 FeignClient 服务名和集成 Sentinel，这两项可以解决 60% 的阻断问题。

---

*审计人：Claude Code（基于 wealth-service-platform 项目 v1.0.0 代码静态分析）*
