# 财富服务平台 — 生产环境部署前全面评估报告

**评估日期**：2026-05-18
**项目版本**：1.0.0
**技术栈**：Spring Boot 3.3.5 / Spring Cloud 2023.0.3 / Spring Cloud Alibaba 2023.0.1.2 / JDK 21

---

## 问题总览

| 严重级别 | 数量 | 说明 |
|---------|------|------|
| 致命 | 7 | 必须立即修复，否则禁止上线 |
| 严重 | 16 | 必须上线前修复 |
| 优化建议 | 14 | 建议上线前修复或规划迭代修复 |

---

## 一、致命问题（上线阻塞）

### F1. 基础设施弱密码与无认证

各大中间件均存在弱密码或未启用认证，攻击者一旦网络可达即可完全控制。

| 组件 | 发现 | 文件 |
|------|------|------|
| MySQL | `.env` 中 `MYSQL_ROOT_PASSWORD=123456`，已提交到 git 仓库 | `.env:3` |
| Nacos | 配置中心无认证（"无需认证"） | `docs/nacos-config-reference.md:4` |
| RabbitMQ | `username: guest` / `password: guest` 硬编码 | `wealth-message/src/main/resources/application.yml:15-16` |
| Grafana | `GF_SECURITY_ADMIN_PASSWORD=admin` | `docker-compose.yml:158` |
| Elasticsearch | `xpack.security.enabled=false` | `docker-compose.yml:99` |
| Seata | Nacos 连接用户名密码均为空 | `seata-config/application.yml:18-19,28-29` |

**修复方案**：
- `.env` 文件加入 `.gitignore`（已存在但未生效），生成强密码并轮换
- 所有基础设施均启用认证，密码通过环境变量注入
- RabbitMQ 凭证改为 `${RABBITMQ_USERNAME}` / `${RABBITMQ_PASSWORD}`

---

### F2. 所有非 system 模块无权限控制

**涉及文件**：
- `wealth-user/src/main/java/com/wealth/user/config/WebConfig.java`
- `wealth-trade/src/main/java/com/wealth/platform/trade/config/WebConfig.java`
- `wealth-account/src/main/java/com/wealth/platform/account/config/WebConfig.java`
- `wealth-product/src/main/java/com/wealth/platform/product/config/WebConfig.java`
- `wealth-message/src/main/java/com/wealth/platform/message/config/WebConfig.java`
- `wealth-search/src/main/java/com/wealth/platform/search/config/WebConfig.java`

**问题**：以上 6 个模块仅注册了 `LoginInterceptor`（JWT 认证），**未注册任何权限拦截器**。任何持有有效 Token 的用户（包括普通用户）可以在这些模块执行任意 CRUD 操作，包括：

| 危险端点 | 操作 |
|---------|------|
| `DELETE /product/WeaProduct/{id}` | 删除产品 |
| `DELETE /trade/WeaTradeOrder/{id}` | 删除交易委托单 |
| `DELETE /user/{id}` / `DELETE /user/batch` | 删除 / 批量删除用户 |
| `DELETE /message/WeaMessage/{id}` | 删除消息 |
| `DELETE /account/WeaUserFavorite/{id}` | 删除他人自选（未校验用户归属） |

**修复方案**：
- 短期：为敏感接口添加 `@PreAuthorize` 注解
- 长期：在所有模块统一引入类似 `PermissionInterceptor` 的拦截器机制
- 关键：`delete` / `update` 类接口必须校验资源归属权或管理员角色

---

### F3. 完全缺失 XSS 防护

**范围**：全项目

**问题**：未使用任何 HTML 过滤器、输出编码、Content Security Policy 或输入清理。搜索 `XSS` / `HtmlUtils` / `Jsoup` / `escapeHtml` / `encodeHtml` 等关键词均无匹配。存储型 XSS 的主要风险入口：

| 风险入口 | 文件 |
|---------|------|
| `save()` 直接保存 `ProductDocument` 到 ES | `wealth-search/.../ProductSearchController.java:23` |
| `create()` 接收消息内容 | `wealth-message/.../FinMessageController.java:60` |

**修复方案**：
- 在 Gateway 层或 common 中添加统一的 XSS 过滤器（如 `JsoupCleanFilter` 或 `StringEscapeUtils`）
- 对所有用户输入的字符串字段做 HTML 标签清理
- Web 前端输出时启用内容转义

---

### F4. `MarketDataSimulationService` 并发崩溃

**文件**：`wealth-product/src/main/java/com/wealth/platform/product/service/MarketDataSimulationService.java:31,47-49`

**问题**（三重并发缺陷）：

1. **`@Scheduled(fixedRate = 2000)`** — 每 2 秒强制启动新任务，不等待上次完成。若某次执行超过 2 秒（例如广播超时），会导致多个线程同时读写数据库和 `cachedMarketData` 列表，造成数据竞争、死锁或 `ConcurrentModificationException`

2. **`cachedMarketData`** 是普通 `ArrayList`，被 `@PostConstruct`（初始化线程）、`@Scheduled`（定时器线程）、REST 端点（HTTP 线程池）三线程共享，无 `volatile` / `synchronized` / `CopyOnWriteArrayList`

3. **`@Transactional` 包裹了 DB 写入 + SSE 广播** — SSE 广播异常会导致 DB 更新也一并回滚，且定时器事务中持有数据库连接 2 秒，高并发下连接池耗尽

**修复方案**：

```java
// 1. fixedRate → fixedDelay，保证不重叠执行
@Scheduled(fixedDelay = 2000)
public void simulateMarketTick() { ... }

// 2. 添加 volatile 保证可见性
private volatile List<WeaMarketData> cachedMarketData;

// 3. 拆分为两个方法：DB写入用 @Transactional，广播在事务外
@Transactional(rollbackFor = Exception.class)
public void simulateTickDb() {
    for (WeaMarketData data : cachedMarketData) {
        marketDataMapper.updateById(data);
    }
}

// simulateMarketTick() 中先调 simulateTickDb() 再广播
```

---

### F5. `GlobalExceptionHandler` 无日志

**文件**：`wealth-common/src/main/java/com/wealth/common/exception/GlobalExceptionHandler.java:13-29`

**问题**：三个 `@ExceptionHandler` 方法均未记录任何日志。生产环境中发生异常时，客户端收到 500 响应但服务端日志无任何记录，导致无法排查问题。

**修复方案**：

```java
@ExceptionHandler(ServiceException.class)
public Result<?> handleServiceException(ServiceException e) {
    log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
    return Result.error(e.getCode(), e.getMessage());
}

@ExceptionHandler(MethodArgumentNotValidException.class)
public Result<?> handleValidationException(MethodArgumentNotValidException e) {
    String message = e.getBindingResult().getFieldErrors().stream()
            .map(f -> f.getField() + ": " + f.getDefaultMessage())
            .collect(Collectors.joining(", "));
    log.warn("参数校验失败: {}", message);
    return Result.error(400, "参数校验失败：" + message);
}

@ExceptionHandler(Exception.class)
public Result<?> handleException(Exception e) {
    log.error("未捕获异常", e);  // 关键：传入 e 记录堆栈
    return Result.error(500, "系统错误");
}
```

---

### F6. 登录接口无暴力破解防护

**涉及文件**：
- `wealth-user/.../UserController.java:113` — `POST /user/login`
- `wealth-system/.../UmsAdminController.java:33` — `POST /system/umsAdmin/login`

**问题**：登录端点无验证码、无账号锁定机制、无限流规则。攻击者可无限尝试密码爆破。

**修复方案**：
- 引入验证码（如 EasyCaptcha、Google Authenticator）
- 在 Redis 中记录失败次数：5 次失败锁定账户 15 分钟
- Gateway 层对 `/login` 路径添加 Sentinel 流控规则，设为 5 QPS
- 审计日志中记录所有登录失败事件

---

### F7. Swagger/Knife4j 接口文档生产环境可匿名访问

**文件**：
- `wealth-common/src/main/java/com/wealth/common/constants/AuthConstant.java:11-14`
- `wealth-gateway/src/main/java/com/wealth/gateway/filter/JwtAuthGlobalFilter.java:42-45`

**问题**：`/doc.html`、`/webjars/**`、`/swagger-resources/**`、`/v3/api-docs/**` 在所有环境白名单放行，暴露完整 API 结构、参数、数据结构。

**修复方案**：
- 通过 `springdoc.api-docs.enabled` 配置区分环境（prod=false）
- 或在生产环境的 Nacos 配置中设置 `springdoc.api-docs.enabled=false`
- 删除 AuthConstant 中的文档白名单路径，改为通过 Nginx 内部访问控制

---

## 二、严重问题（上线前必须修复）

### H1. jjwt 0.11.5 存在已知 CVE

| 项目 | 内容 |
|------|------|
| **文件** | `pom.xml:30`、`wealth-common/pom.xml:75`、`wealth-gateway/pom.xml:46` |
| **问题** | 0.11.5 版本存在多个已公开 CVE（反序列化相关问题） |
| **修复** | 升级至 `0.12.6`+（注意 0.12.x 对 API 有破坏性变更，需适配新 API） |

### H2. 无 Token 刷新机制

| 项目 | 内容 |
|------|------|
| **文件** | `JwtUtil.java:45-51` |
| **问题** | Token 过期后用户必须重新登录，用户体验差 |
| **修复** | 实现 Refresh Token 双 Token 机制：access_token（短时效 30min）+ refresh_token（长时效 7d） |

### H3. 无 Token 吊销能力

| 项目 | 内容 |
|------|------|
| **文件** | `JwtUtil.java:46-51` |
| **问题** | `generateToken()` 未生成 `jti`（JWT ID）声明，无法单独吊销某 Token |
| **修复** | JWT 中增加 `.setId(UUID.randomUUID().toString())`，Redis 维护 Token 黑名单 |

### H4. 所有配置文件均为单 Profile，无 dev/prod 分离

| 项目 | 内容 |
|------|------|
| **范围** | 全部 8 个服务的 `application.yml` |
| **问题** | 同一配置适用于所有环境，无法差异化生产设置 |
| **修复** | 增加 `application-prod.yml` 覆盖：日志级别（INFO）、连接池大小、Knife4j 开关、JVM 参数 |

### H5. 日志 `com.wealth` 包为 DEBUG 级别

| 项目 | 内容 |
|------|------|
| **文件** | `logback-spring.xml:66` |
| **问题** | DEBUG 日志在高流量下可产生数 GB/小时，易撑满磁盘 |
| **修复** | 生产 profile 覆盖为 `INFO` |
| **参考** | `<logger name="com.wealth" level="INFO" additivity="false">` |

### H6. 无 HikariCP 连接池生产配置

| 项目 | 内容 |
|------|------|
| **范围** | 全部 7 个数据源服务 |
| **问题** | 使用 HikariCP 默认值（maximum-pool-size=10），无法应对生产流量 |
| **修复** | 在 `application-prod.yml` 中添加： |

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 30
      minimum-idle: 10
      connection-timeout: 5000
      max-lifetime: 600000
      idle-timeout: 300000
```

### H7. Dockerfile 无 JVM 参数

| 项目 | 内容 |
|------|------|
| **范围** | 全部 8 个 Dockerfile |
| **问题** | `ENTRYPOINT ["java", "-jar", "app.jar"]` 无任何 JVM 参数，容器中 OOM 后不会自动退出 |
| **修复** | |

```dockerfile
ENTRYPOINT ["java", "-Xms512m", "-Xmx512m", "-XX:+UseContainerSupport",
            "-XX:+ExitOnOutOfMemoryError", "-Djava.security.egd=file:/dev/./urandom",
            "-jar", "app.jar"]
```

### H8. Nacos 无命名空间隔离

| 项目 | 内容 |
|------|------|
| **范围** | 全部 `bootstrap.yml` |
| **问题** | 所有环境共享 Nacos public 命名空间，dev/staging/prod 配置相互可见 |
| **修复** | 在各 `bootstrap.yml` 中添加 `spring.cloud.nacos.config.namespace=prod-namespace-id` |

### H9. Sentinel 规则未持久化

| 项目 | 内容 |
|------|------|
| **文件** | `SentinelGatewayConfig.java:21-27`、trade/message 的 `SentinelConfig.java` |
| **问题** | 规则在 `@PostConstruct` 中以硬编码方式加载到内存，重启后丢失，无法热更新 |
| **修复** | 通过 Nacos 配置动态数据源： |

```yaml
spring:
  cloud:
    sentinel:
      datasource:
        ds:
          nacos:
            server-addr: ${NACOS_ADDR}
            data-id: sentinel-rules
            group-id: SENTINEL_GROUP
            rule-type: flow
```

### H10. Seata 使用文件存储

| 项目 | 内容 |
|------|------|
| **文件** | `seata-config/application.yml:31` |
| **问题** | `store.mode: file` 重启后丢失事务记录，不支持 HA 部署 |
| **修复** | 改为 `store.mode: db`，配置 MySQL 连接存储事务日志 |

### H11. Zipkin 链路追踪采样率 100%

| 项目 | 内容 |
|------|------|
| **来源** | Nacos 配置 `wealth-shared.yaml` |
| **问题** | `sampling.probability: 1.0` 全量采样，生产环境产生海量数据，增加存储和网络开销 |
| **修复** | 改为 `0.01`（1% 采样），生产环境推荐 `0.01` ~ `0.1` |

### H12. Gateway 限流 QPS 阈值过高且硬编码

| 项目 | 内容 |
|------|------|
| **文件** | `SentinelGatewayConfig.java:21-27` |
| **问题** | trade 50 QPS、其他 100 QPS，均为内存硬编码 |
| **修复** | 降低默认值，通过 Nacos 动态配置。登录接口建议单独设为 5 QPS |

### H13. `BeanConvertUtil` 使用 `RuntimeException` 而非 `ServiceException`

| 项目 | 内容 |
|------|------|
| **文件** | `BeanConvertUtil.java:21,45` |
| **问题** | `RuntimeException` 不会被 `GlobalExceptionHandler.handleServiceException()` 捕获，回退为 500 通用错误 |
| **修复** | 改为 `throw new ServiceException(500, "转换失败", e)` |

### H14. `BeanConvertUtil` 中文字符编码损坏

| 项目 | 内容 |
|------|------|
| **文件** | `BeanConvertUtil.java:21,45` |
| **问题** | 异常消息显示为乱码（多重编码转换导致原始 UTF-8 被二次编码） |
| **修复** | 确认文件保存为 UTF-8 without BOM，IDE 全局编码统一为 UTF-8 |

### H15. 登录接口返回值暴露用户 ID

| 项目 | 内容 |
|------|------|
| **文件** | `UserServiceImpl.java:59` — `new LoginVO(token, dbUser.getId(), dbUser.getNickname())` |
| **问题** | Token 中已包含用户身份信息，额外返回用户 ID 提升了攻击面（配合权限缺失可枚举用户） |
| **修复** | LoginVO 中移除 `userId` 或仅在需要时通过专用接口返回 |

### H16. CORS 地址硬编码为 localhost

| 项目 | 内容 |
|------|------|
| **文件** | `wealth-gateway/src/main/resources/application.yml:47-49` |
| **问题** | 只允许 `localhost:3000` / `localhost:8080` / `127.0.0.1:3000` |
| **修复** | 改为从 Nacos 配置或环境变量读取生产域名 |

---

## 三、优化建议

| # | 文件 | 行号 | 问题 | 建议 |
|---|------|------|------|------|
| M1 | `MarketDataPushService.java` | 62,78 | SSE 异常空 catch | 至少记录 warn 日志 |
| M2 | `ProductSyncServiceImpl.java` | 40-68 | ES 同步失败无重试 | 引入 MQ 重试队列或 `@Retryable` |
| M3 | `FinMarketDataController.java` | 68-70 | SSE 首次推送异常空 catch | 添加日志并记录失败指标 |
| M4 | `PermissionInterceptor.java` | 154 | 每请求创建 `new AntPathMatcher()` | 改为 `static final` 类字段 |
| M5 | 所有 Controller | 多处 | 分页 VO 转换 8 行重复代码 | 抽取 `BeanConvertUtil.convertPage()` 方法 |
| M6 | 所有 Controller | 类级别 | 缺少 `@Validated` | 加在 controller 类上开启参数校验 |
| M7 | `ProductSearchController.java` | 23 | 缺少 `@Valid` | 添加以校验 ES 文档字段 |
| M8 | `nginx.conf` | - | 缺少 HSTS header | 添加 `add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;` |
| M9 | 所有 Dockerfile | - | 缺少 `HEALTHCHECK` | 添加 `HEALTHCHECK --interval=30s --timeout=3s CMD wget -qO- http://localhost:port/actuator/health || exit 1` |
| M10 | `JwtAuthGlobalFilter.java` | 85-96 | 与 `JwtUtil.validateToken()` 重复逻辑 | 统一调用 `JwtUtil` |
| M11 | `UmsRoleServiceImpl.java` | 10-13 | 空实现类 | 移除或添加 TODO 注释 |
| M12 | `OrderStatusEnum.java` | 6 | 未使用的 `import EnumSet` | 移除 |
| M13 | `wealth-common/pom.xml` / `wealth-gateway/pom.xml` | 75/46 | jjwt 版本硬编码 | 改为 `${jjwt.version}` |
| M14 | `docker-compose.yml` | 多处 | 应用服务无 healthcheck | 添加 healthcheck 各服务健康探针 |

---

## 四、上线前检查清单

### 4.1 安全加固
- [ ] 修改所有基础设施初始密码（MySQL / Nacos / RabbitMQ / Grafana / ES / Seata）
- [ ] `.env` 文件确认已加入 `.gitignore`，清除 git 历史中的密码痕迹
- [ ] Nacos 启用认证（`nacos.core.auth.enabled=true`）
- [ ] ES 启用认证（`xpack.security.enabled=true`）
- [ ] RabbitMQ 使用非 `guest` 账户
- [ ] Seata 配置 Nacos 认证信息
- [ ] 非 system 模块增加 RBAC 权限控制
- [ ] 添加 XSS 全局过滤器
- [ ] 关闭生产环境 Swagger/Knife4j
- [ ] 添加登录验证码和失败锁定机制
- [ ] Gateway 降低登录接口 Sentinel QPS 阈值
- [ ] 升级 jjwt 至 0.12.6+
- [ ] 实现 JWT refresh token 机制
- [ ] 添加 `jti` 声明到 Token
- [ ] LoginVO 移除用户 ID 暴露

### 4.2 JVM & 容器
- [ ] 所有 Dockerfile 添加 `-Xms` / `-Xmx` 及 `-XX:+ExitOnOutOfMemoryError`
- [ ] 所有 Dockerfile 添加 `HEALTHCHECK`
- [ ] Nginx 添加 HSTS header
- [ ] 配置 Docker 资源限制（`deploy.resources.limits.memory` / `cpus`）

### 4.3 配置治理
- [ ] 创建 `application-prod.yml` profile 覆盖生产差异化配置
- [ ] 配置 HikariCP 连接池参数
- [ ] 日志级别生产环境切换为 INFO
- [ ] 配置 Nacos 命名空间隔离
- [ ] Nacos 启用配置历史与版本管理
- [ ] 将 CORS 域名改为 Nacos 动态配置
- [ ] `spring.sql.init.mode` 在所有服务中显式设为 `never`

### 4.4 可观测性
- [ ] `GlobalExceptionHandler` 增加所有异常日志
- [ ] `BeanConvertUtil` 异常改为 `ServiceException`
- [ ] Zipkin 采样率设为 0.01~0.1
- [ ] 验证所有服务 `/actuator/health` 可正常访问
- [ ] 验证所有服务 `/actuator/prometheus` 暴露指标
- [ ] 验证 Prometheus 已成功抓取所有目标
- [ ] 配置关键业务指标告警规则（Grafana Alerting）
- [ ] 配置日志归档与监控告警（磁盘空间、ERROR 速率）

### 4.5 数据一致性
- [ ] Seata 改为 `store.mode: db`
- [ ] Sentinel 规则持久化到 Nacos
- [ ] `ProductSyncServiceImpl` 添加 ES 同步重试机制
- [ ] `MarketDataSimulationService` 修复并发安全（fixedDelay + volatile + 事务拆分）

### 4.6 构建与部署
- [ ] 执行 `mvn clean install -DskipTests` 全量编译通过
- [ ] 每个服务 `mvn spring-boot:run` 独立启动成功
- [ ] 执行 `mvn test -DskipTests=false` 全部测试通过
- [ ] Docker 镜像构建脚本验证
- [ ] 按顺序启动验证：gateway → system → user → product → account → trade → message → search
- [ ] 验证前端 `npm ci && npx vite` 构建成功
- [ ] 验证端到端业务流程：登录 → 查看产品 → 交易 → 查看委托

### 4.7 依赖检查
- [ ] jjwt 版本冲突修复（子模块使用 `${jjwt.version}`）
- [ ] mybatis-spring 版本统一（3.0.4 / 3.0.5 只保留一个）
- [ ] 扫描所有 `pom.xml` 确认无重复依赖声明
- [ ] 检查传递性依赖中是否存在 CVE 组件（mvn dependency-check）

---

## 五、风险评分矩阵

| 类别 | 当前风险分 | 修复后风险分 | 说明 |
|------|-----------|------------|------|
| 认证授权 | 9/10 | 3/10 | 致命权限缺失 + 无暴力破解防护 |
| 基础设施安全 | 9/10 | 3/10 | 所有中间件无认证或弱密码 |
| 并发安全 | 8/10 | 2/10 | 行情模拟服务三重并发缺陷 |
| 异常与日志 | 8/10 | 2/10 | 全局异常无日志，转化异常吞没 |
| 可观测性 | 6/10 | 2/10 | 采样率过高但可调，缺关键日志 |
| 配置管理 | 7/10 | 3/10 | 单 profile、硬编码、无连接池调优 |
| 依赖安全 | 5/10 | 1/10 | jjwt 已知 CVE |
| **综合** | **7.4/10** | **2.3/10** | **严重不安全 → 可上线** |

---

## 附：运行验证命令参考

```bash
# 全量编译
mvn clean install -DskipTests

# 运行全部测试
mvn test -DskipTests=false

# 运行单个服务（示例：产品服务）
mvn spring-boot:run -pl wealth-product

# Docker 构建（使用项目脚本）
.\docker-build.ps1

# 健康检查
curl http://localhost:8080/actuator/health
curl http://localhost:8082/system/actuator/health

# Prometheus 指标
curl http://localhost:8082/system/actuator/prometheus
```

---

> **总结**：项目架构设计合理，已集成 Sentinel、Seata、Micrometer Tracing、审计日志、幂等性防重等企业级特性。但安全与运维配置存在严重缺失，综合风险评分 **7.4/10**。致命问题（无权限控制、XSS、基础设施无认证、并发崩溃、无日志、无暴力破解防护）是上线阻塞项。建议按优先级：安全加固 → 配置治理 → JVM/容器 → 可观测性。逐项修复后风险可降至 **2.3/10**，达到生产部署标准。
