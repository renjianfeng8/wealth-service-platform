# Nacos 配置中心参考文档

> 记录 Nacos 配置中心 `wealth-shared.yaml` 的内容及变更历史。
> Nacos 地址：`http://localhost:8848`（无需认证），Group：`DEFAULT_GROUP`。
> 各模块通过 `bootstrap.yml` 的 `shared-configs` 引用此配置。

---

## wealth-shared.yaml（当前生效版本）

```yaml
jwt:
  secret: wealth-micro-service-20260501-very-safe-secret-key-123456789
  expire: 604800000

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/wealth?useUnicode=true

management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
  tracing:
    sampling:
      probability: 1.0
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans
```

---

## 配置项说明

| 配置路径 | 值 | 说明 |
|---------|-----|------|
| `jwt.secret` | 56 字节密钥 | HMAC-SHA256 签名密钥，JwtUtil 启动时校验 ≥32 字节 |
| `jwt.expire` | 604800000 (7天) | Token 过期时间（毫秒） |
| `spring.datasource.driver-class-name` | com.mysql.cj.jdbc.Driver | MySQL 驱动 |
| `spring.datasource.url` | jdbc:mysql://localhost:3306/wealth?useUnicode=true | 数据源连接串（含编码与时区） |
| `management.tracing.sampling.probability` | 1.0 | 链路追踪采样率（1.0 = 100%，开发环境全采样） |
| `management.zipkin.tracing.endpoint` | http://localhost:9411/api/v2/spans | Zipkin 服务端 Span 上报地址 |
| `management.endpoints.web.exposure.include` | health,info,prometheus | Actuator 暴露的端点列表（新增 prometheus 供 Prometheus 抓取） |

---

## 变更历史

| 日期 | 变更人 | 说明 |
|------|--------|------|
| 2026-05-16 | 系统初始化 | 初始版本：JWT + 数据源 |
| 2026-05-17 | 审计修复 | 新增链路追踪配置。**注意**：`zipkin.base-url` 是 Spring Cloud Sleuth（Spring Boot 2.x）的旧属性，在 Spring Boot 3.x + Micrometer Tracing 中须使用 `management.zipkin.tracing.endpoint` |
| 2026-05-17 | 监控集成 | 新增 `management.endpoints.web.exposure.include: health,info,prometheus` 暴露 Prometheus 指标端点 |

---

## 覆盖优先级

```
bootstrap.yml                     # 1. 启动时加载 —— 配置 Nacos 地址、应用名
   └─→ Nacos (wealth-shared.yaml)  # 2. Nacos 远程配置 —— JWT + 数据源 + 链路追踪
       └─→ application.yml          # 3. 本地配置 —— 端口、context-path、mybatis-plus
```

> 注意：Nacos shared-configs 优先级低于各模块本地 application.yml。例如 `spring.datasource.url` 在 Nacos 中被定义为简写，实际以各模块 application.yml 中的完整连接串为准。`jwt.*` 和 `management.*` 在本地配置中未定义，完全由 Nacos 提供。
