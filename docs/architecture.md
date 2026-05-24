# 模块架构与配置体系
> 跨模块开发时引用 — 模块架构、端口、依赖层级、网关路由、配置体系。

---

# 一、项目模块架构

```
wealth-service-platform (pom)
├── wealth-common      # 公共依赖：DTO、工具类、contract接口、统一返回、异常处理、通用配置
├── wealth-gateway     # 网关（Spring Cloud Gateway 路由转发、全局CORS，无数据源）
└── wealth-service     # 业务聚合服务：所有业务域合并为单个可部署单元
```

## 架构演进

v1.7.3 完成模块合并，6 个独立微服务合并为单一的 `wealth-service`，保留 Gateway 网关层：

```
v1.7.2 (微服务)                  v1.7.3+ (单体聚合)
wealth-system (8082)             
wealth-user   (8083)             
wealth-product (8084)            →  wealth-service (8081)
wealth-trade   (8085)            
wealth-message (8087)            
wealth-search  (8089)            
wealth-gateway (8080)            →  wealth-gateway (8080)
```

## 依赖层级

- `wealth-common` 被所有模块依赖（修改后需先 `mvn clean install -pl wealth-common`）
- `wealth-gateway` 依赖 `wealth-common`（工具类、DTO、常量）
- 跨域调用通过 contract 接口（`com.wealth.common.contract`）直接调用，无需 Feign

## 各模块端口号

| 模块 | 端口 | context-path | 说明 |
|------|:----:|:-----------:|------|
| wealth-gateway | 8080 | - | Spring Cloud Gateway（WebFlux） |
| wealth-service | 8081 | / | 所有业务域聚合 |

## 业务域与包路径

所有业务域在 `wealth-service` 模块内，基包 `com.wealth.platform.{domain}`：

| 业务域 | domain | 表前缀 | 说明 |
|--------|--------|--------|------|
| 后台权限 | system | ums_* | 管理员、角色、资源、RBAC 权限拦截 |
| 用户管理 | user | sys_user | 前端用户注册/登录/信息管理 |
| 产品行情 | product | wea_product, wea_market_data, wea_user_favorite | 产品管理、行情 SSE 推送、用户自选 |
| 交易委托 | trade | wea_trade_order | 交易订单发起、撤单、查询 |
| 消息推送 | message | wea_news, wea_message | 财经资讯、站内消息（DB 轮询） |
| 搜索服务 | search | - | ES 全文检索（ES 不可用时降级 MySQL LIKE）|

## 网关路由

gateway（端口 8080）负责统一路由转发（静态 HTTP 路由，无需 Nacos）：

| 路由前缀 | 转发目标 | 对应业务域 |
|---------|---------|-----------|
| /system/** | http://localhost:8081 | 后台权限 |
| /user/** | http://localhost:8081 | 用户管理 |
| /product/** | http://localhost:8081 | 产品行情 |
| /trade/** | http://localhost:8081 | 交易委托 |
| /message/** | http://localhost:8081 | 消息推送 |
| /search/** | http://localhost:8081 | 搜索服务 |

> 所有路由转发到同一 `wealth-service` 实例，不同前缀在服务端通过 context-path 区分。

---

# 二、配置体系

## 配置架构

项目采用**环境变量 + application.yml 本地配置**模式，不依赖外部配置中心：

```
.env（项目根目录，docker-compose 注入）
  ├── wealth-gateway/.env（网关专用）
  └── wealth-service/.env  （业务服务专用）
         ↓
application.yml（本地配置，引用 ${ENV_VAR} 占位符）
         ↓
application-prod.yml（生产环境配置覆盖）
```

### Nacos 配置中心（已禁用）

Nacos 服务端可保持运行（用于其他依赖），但当前所有模块的 Nacos 配置/注册均已关闭：

```yaml
spring:
  cloud:
    nacos:
      config:
        enabled: false
      discovery:
        enabled: false
```

## Spring 配置加载顺序

```
application.yml          # 1. 本地配置 —— 端口、数据源、Redis、ES
  └─→ application-prod.yml  # 2. 生产环境覆盖（spring.profiles.active=prod）
       └─→ .env 环境变量      # 3. 环境变量注入 ${VAR:default}
```

## JWT 配置

通过环境变量注入，避免明文入 Git：

```yaml
jwt:
  secret: ${JWT_SECRET}
  expire: ${JWT_EXPIRE:604800000}
```

JWT 密钥值来自模块 `.env` 文件（已在 `.gitignore` 中排除）。

---

# 三、基础设施容器

| 服务 | 镜像 | 端口 | 必需 | 说明 |
|------|------|:----:|:----:|------|
| MySQL | mysql:8.0 | 3306 | 是 | 数据库 |
| Redis | redis:latest | 6379 | 是 | 缓存 |
| Nginx | nginx:latest | 80, 443 | 是 | 反向代理 |
| Elasticsearch | elasticsearch:8.8.2 | 9200, 9300 | 否 | 搜索引擎（search 降级 MySQL 可关）|
| Zipkin | openzipkin/zipkin:latest | 9411 | 否 | 链路追踪 |
| Prometheus | prom/prometheus:latest | 9090 | 否 | 监控指标存储 |
| Grafana | grafana/grafana:latest | 3001 | 否 | 监控仪表盘 |

> Nacos、RabbitMQ、Seata、Sentinel 已停用或禁用。

---

# 四、网关配置参考

当前 `application.yml` 网关路由配置：

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: wealth-system
          uri: http://${SERVICE_HOST:localhost}:8081
          predicates:
            - Path=/system/**
        - id: wealth-user
          uri: http://${SERVICE_HOST:localhost}:8081
          predicates:
            - Path=/user/**
        - id: wealth-product
          uri: http://${SERVICE_HOST:localhost}:8081
          predicates:
            - Path=/product/**
        - id: wealth-trade
          uri: http://${SERVICE_HOST:localhost}:8081
          predicates:
            - Path=/trade/**
        - id: wealth-message
          uri: http://${SERVICE_HOST:localhost}:8081
          predicates:
            - Path=/message/**
        - id: wealth-search
          uri: http://${SERVICE_HOST:localhost}:8081
          predicates:
            - Path=/search/**
```

---

# 五、部署架构

```
                              ┌──────────────┐
                              │   Nginx 80   │
                              │  (反向代理)   │
                              └──────┬───────┘
                                     │
                              ┌──────▼───────┐
                              │  Gateway     │
                              │  (8080)      │
                              └──────┬───────┘
                                     │
                              ┌──────▼───────┐
                              │ wealth-serv. │
                              │  (8081)      │
                              └──────┬───────┘
              ┌─────────┬────────────┼────────────┬──────────┐
         ┌────▼───┐┌───▼────┐┌──────▼──────┐┌───▼────┐┌───▼─────┐
         │ MySQL  ││ Redis  ││Elasticsearch││ Zipkin ││Prometheus│
         │ (3306) ││ (6379) ││ (9200)      ││ (9411) ││ (9090)   │
         └────────┘└────────┘└─────────────┘└────────┘└──────────┘
```

> 生产环境 Nginx 负责 SSL 终止，HTTP 请求转发至 Gateway 内部端口 8080。
