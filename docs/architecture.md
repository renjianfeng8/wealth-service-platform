# 模块架构与配置体系
> 跨模块开发时引用 — 模块架构、端口、依赖层级、网关路由、Nacos 配置体系。

---

# 一、项目模块架构

```
wealth-service-platform (pom)
├── wealth-common      # 公共依赖：DTO、工具类、Feign接口、统一返回、异常处理、通用配置
├── wealth-gateway     # 网关（Spring Cloud Gateway 路由转发、全局CORS，无数据源）
├── wealth-system      # 系统服务（后台权限管理 ums_*、管理员JWT登录、RBAC权限拦截）
├── wealth-user        # 用户服务（前端用户管理 sys_user）
├── wealth-product     # 产品服务（产品 wea_product + 行情 wea_market_data + SSE + 用户自选）
├── wealth-trade       # 交易服务（委托交易 wea_trade_order）
├── wealth-message     # 消息服务（资讯 wea_news + 站内消息 wea_message，DB 轮询替代 RabbitMQ）
└── wealth-search      # 搜索服务（ES 优先，ES 不可用时降级 MySQL LIKE 查询）
```

注：`wealth-account` 已合并到 `wealth-product`，不再独立部署。

## 依赖层级

- `wealth-common` 被除 gateway 外的所有模块依赖（修改后需先 `mvn clean install -pl wealth-common`）
- `wealth-gateway` 不依赖 `wealth-common`（避免 `spring-boot-starter-web` 与 WebFlux 冲突）
- 业务模块间通过 Feign 接口调用，FeignClient 定义在 `wealth-common` 中

## 各模块端口号

| 模块 | 端口 | context-path | 说明 |
|------|------|-------------|------|
| wealth-gateway | 8080 | - | Spring Cloud Gateway（WebFlux）|
| wealth-system  | 8082 | /system | 后台权限管理 |
| wealth-user    | 8083 | /user | 前端用户管理 |
| wealth-product | 8084 | /product | 产品 + 行情 + 自选 |
| wealth-trade   | 8085 | /trade | 交易委托 |
| wealth-message | 8087 | /message | 资讯 + 消息 |
| wealth-search  | 8089 | /search | 产品搜索 |

## 各模块 Java 包基路径

| 模块 | 基础包 |
|------|--------|
| wealth-common  | com.wealth.common |
| wealth-gateway | com.wealth.gateway |
| wealth-user    | com.wealth.user |
| 其余业务模块    | com.wealth.platform.{模块名} |

## 网关路由

gateway（端口 8080）负责统一路由转发：

| 路由前缀 | 目标服务 |
|---------|---------|
| /system/** | wealth-system |
| /user/** | wealth-user |
| /product/** | wealth-product |
| /trade/** | wealth-trade |
| /message/** | wealth-message |
| /search/** | wealth-search |

---

# 二、配置体系

## Nacos 配置中心（Docker: nacos/nacos-server:v2.3.2）

地址：`localhost:8848`（已启用认证，默认凭据：nacos/nacos）

### wealth-shared.yaml（DEFAULT_GROUP，YAML 格式）

所有模块共享的 Nacos 配置，完整内容及变更历史见 [Nacos 配置参考](nacos-config-reference.md)。

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
      probability: 0.1
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans
```

> **作用范围**：通过各模块 bootstrap.yml 的 `shared-configs` 引用，被所有模块加载。
> **不含 `spring.datasource`**：数据源配置由各模块的 application.yml 或 docker-compose 环境变量提供，避免 Nacos 覆盖。

### 配置加载链路

```
bootstrap.yml                     # 1. 启动时加载 —— 配置 Nacos 地址、应用名
   └─→ Nacos (wealth-shared.yaml)  # 2. Nacos 远程配置 —— JWT + 链路追踪 + 监控暴露
       └─→ application.yml          # 3. 本地配置 —— 端口、context-path、数据源
```

## bootstrap.yml（所有业务模块统一模式）

所有 7 个模块的 `bootstrap.yml` 内容一致（仅 `application.name` 不同）：

```yaml
spring:
  application:
    name: wealth-{模块名}
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
      config:
        server-addr: localhost:8848
        file-extension: yaml
        shared-configs:
          - data-id: wealth-shared.yaml
            refresh: true
```

| 模块 | application.name |
|------|-----------------|
| gateway | wealth-gateway |
| system | wealth-system |
| user | wealth-user |
| product | wealth-product |
| trade | wealth-trade |
| message | wealth-message |
| search | wealth-search |

---

# 三、基础设施容器

服务器上运行的 Docker 容器状态见 [轻量化部署策略](server-lightweight-strategy.md)，以下为本地开发常用配置：

| 服务 | 镜像 | 端口 | 说明 |
|------|------|------|------|
| Nacos | nacos/nacos-server:v2.3.2 | 8848, 9848 | 必需 |
| MySQL | mysql:8.0.37 | 3306 | 必需 |
| Redis | redis:latest | 6379 | 必需 |
| Nginx | nginx:latest | 80, 443 | 必需 |
| Elasticsearch | elasticsearch:8.8.2 | 9200, 9300 | 按需（search 降级 MySQL 可关）|
| RabbitMQ | rabbitmq:3.10-management | 5672, 15672 | 已停用（message 使用 DB 轮询）|
| Zipkin | openzipkin/zipkin:latest | 9411 | 按需 |
| Prometheus | prom/prometheus:latest | 9090 | 按需 |
| Grafana | grafana/grafana:latest | 3001 | 按需 |
| Sentinel | bladex/sentinel-dashboard:latest | 8858 | 按需 |
| Seata | seataio/seata-server:2.0.0 | 7091, 8091 | 已停用（`seata.enabled=false`）|

---

# 四、网关 application.yml 路由配置

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: wealth-system
          uri: lb://wealth-system
          predicates:
            - Path=/system/**
        - id: wealth-user
          uri: lb://wealth-user
          predicates:
            - Path=/user/**
        - id: wealth-product
          uri: lb://wealth-product
          predicates:
            - Path=/product/**,/account/**
        - id: wealth-trade
          uri: lb://wealth-trade
          predicates:
            - Path=/trade/**
        - id: wealth-message
          uri: lb://wealth-message
          predicates:
            - Path=/message/**
        - id: wealth-search
          uri: lb://wealth-search
          predicates:
            - Path=/search/**
```

注：`/account/**` 路由指向 `wealth-product`，因 account 功能已合并到 product 模块。
