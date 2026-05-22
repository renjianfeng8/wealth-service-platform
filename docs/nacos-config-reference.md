# Nacos 配置中心参考文档

> 记录 Nacos 配置中心 `wealth-shared.yaml` 的内容及变更历史。
> Nacos 地址：`http://localhost:8848`（已启用认证，默认凭据：nacos/nacos），Group：`DEFAULT_GROUP`。
> 各模块通过 `bootstrap.yml` 的 `shared-configs` 引用此配置。

---

## wealth-shared.yaml（当前生效版本）

```yaml
jwt:
  secret: wealth-micro-service-20260501-very-safe-secret-key-123456789
  expire: 604800000

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

> 注意：**不含 `spring.datasource`**。数据源配置由各模块的 `application.yml` 或 docker-compose 环境变量提供，避免 Nacos 覆盖。

---

## 配置项说明

| 配置路径 | 值 | 说明 |
|---------|-----|------|
| `jwt.secret` | 56 字节密钥 | HMAC-SHA256 签名密钥，JwtUtil 启动时校验 ≥32 字节 |
| `jwt.expire` | 604800000 (7天) | Token 过期时间（毫秒） |
| `management.tracing.sampling.probability` | 1.0 | 链路追踪采样率（1.0 = 100%，开发环境全采样） |
| `management.zipkin.tracing.endpoint` | http://localhost:9411/api/v2/spans | Zipkin 服务端 Span 上报地址 |
| `management.endpoints.web.exposure.include` | health,info,prometheus | Actuator 暴露的端点列表（Prometheus 指标抓取） |

---

## 变更历史

| 日期 | 变更人 | 说明 |
|------|--------|------|
| 2026-05-16 | 系统初始化 | 初始版本：JWT + 数据源 |
| 2026-05-17 | 审计修复 | 新增链路追踪配置。**注意**：`zipkin.base-url` 是 Spring Cloud Sleuth（Spring Boot 2.x）的旧属性，在 Spring Boot 3.x + Micrometer Tracing 中须使用 `management.zipkin.tracing.endpoint` |
| 2026-05-17 | 监控集成 | 新增 `management.endpoints.web.exposure.include: health,info,prometheus` 暴露 Prometheus 指标端点 |
| 2026-05-22 | 文档治理 | 移除 `spring.datasource` 配置（数据源由 application.yml/env 提供），与 architecture.md 保持一致 |

---

## 覆盖优先级

```
bootstrap.yml                     # 1. 启动时加载 —— 配置 Nacos 地址、应用名
   └─→ Nacos (wealth-shared.yaml)  # 2. Nacos 远程配置 —— JWT + 链路追踪 + 监控暴露
       └─→ application.yml          # 3. 本地配置 —— 端口、context-path、数据源
```

> 注意：Nacos shared-configs 优先级低于各模块本地 application.yml。`jwt.*` 和 `management.*` 在本地未定义，由 Nacos 统一提供。`spring.datasource.*` 不在 Nacos 中定义，以各模块 application.yml 或 docker-compose 环境变量为准。
