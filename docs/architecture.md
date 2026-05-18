# 模块架构与配置体系
> 跨模块开发时引用 — 模块架构、端口、依赖层级、网关路由、Nacos 配置体系。

---

# 一、项目模块架构
wealth-service-platform (pom)
├── wealth-common      # 公共依赖模块：DTO、工具类、Feign接口、统一返回、异常处理、通用配置
├── wealth-gateway     # 网关服务（Spring Cloud Gateway 路由转发、全局CORS）
├── wealth-system      # 系统服务（后台权限管理 ums_* 表、管理员JWT登录、RBAC权限拦截）
├── wealth-user        # 用户服务（前端用户管理 sys_user）
├── wealth-account     # 账户服务（自选管理 wea_user_favorite）
├── wealth-product     # 产品服务（产品 wea_product + 行情 wea_market_data + SSE 实时推送）
├── wealth-trade       # 交易服务（委托交易 wea_trade_order）
├── wealth-message     # 消息服务（资讯 wea_news + 站内消息 wea_message）
└── wealth-search      # 搜索服务（基于 ES 8 的产品搜索，无数据库依赖）
## 依赖层级

- wealth-common 被除 gateway 外的所有模块依赖（修改后需先 `mvn clean install -pl wealth-common`）
- wealth-gateway 不依赖 wealth-common（避免 spring-boot-starter-web 与 WebFlux 冲突）
- 业务模块间通过 Feign 接口调用，FeignClient 定义在 wealth-common 中
- wealth-system 显式覆盖 mybatis-spring 版本为 3.0.5（父 POM 中 3.0.4）
## 各模块端口号

| 模块 | 端口 | context-path | 说明 |
|------|------|-------------|------|
| wealth-gateway | 8080 | - | Spring Cloud Gateway（WebFlux）|
| wealth-system  | 8082 | /system | 后台权限管理 |
| wealth-user    | 8083 | /user | 前端用户管理 |
| wealth-product | 8084 | /product | 产品 + 行情 |
| wealth-trade   | 8085 | /trade | 交易委托 |
| wealth-account | 8086 | /account | 用户自选 |
| wealth-message | 8087 | /message | 资讯 + 消息 |
| wealth-search  | 8089 | - | ES 搜索 |

## 各模块 Java 包基路径

| 模块 | 基础包 |
|------|--------|
| wealth-common  | com.wealth.common |
| wealth-gateway | com.wealth.gateway |
| wealth-user    | com.wealth.user |
| 其余业务模块    | com.wealth.platform.{模块名} |

## 网关路由

gateway（端口 8080）负责统一路由转发，所有前端请求统一经网关访问各模块：
| 路由前缀 | 目标服务 |
|---------|---------|
| /system/** | wealth-system |
| /user/** | wealth-user |
| /product/** | wealth-product |
| /account/** | wealth-account |
| /trade/** | wealth-trade |
| /message/** | wealth-message |
| /search/** | wealth-search |

---

# 二、配置体系（强制锁定，不得修改）

## 配置总则（铁律）

> **⚠ 禁止修改现有配置** — 包括但不限于：所有模块的 application.yml、bootstrap.yml 中的现有参数、Nacos 配置中的现有键值。
> 所有业务配置已在 Nacos 配置中心统一管理，本地配置文件为一次性写入的固定值。
> 新增功能所需的依赖（如 pom.xml 添加新 dependency）和对应 Nacos 配置项不在禁止范围内，但须同步更新文档。

---

## Nacos 配置中心（Docker: nacos/nacos-server:v2.3.2）
地址：`localhost:8848`（无需认证）
### wealth-shared.yaml（DEFAULT_GROUP，YAML 格式）
所有模块共享的唯一 Nacos 配置。**完整内容及变更历史见 [Nacos 配置参考](nacos-config-reference.md)**。

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

> **作用范围**：`wealth-shared.yaml` 通过各模块 bootstrap.yml 的 `shared-configs` 引用，被所有模块加载。
> **覆盖优先级**：Nacos shared-configs 的优先级低于各模块本地 application.yml，但高于 bootstrap.yml 中的默认值。
> 本配置提供 JWT 密钥/过期时间、MySQL 数据源、链路追踪（Micrometer Tracing + Zipkin）、Prometheus 指标暴露配置，各模块凭此连接数据库、上报链路数据、暴露监控指标。
### 配置加载链路

```
bootstrap.yml                     # 1. 启动时加载 —— 配置 Nacos 地址、应用名
   └─→ Nacos (wealth-shared.yaml)  # 2. Nacos 远程配置 —— JWT + 数据源 + 链路追踪 + 监控暴露
       └─→ application.yml          # 3. 本地配置 —— 端口、context-path、mybatis-plus
```

---

## 本地配置文件清单（已有内容，禁止修改）
### 1. bootstrap.yml（所有业务模块统一模式）
所有 8 个模块的 `bootstrap.yml` 内容完全一致（仅 `application.name` 不同）：

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

各模块 `application.name` 对应值：

| 模块 | application.name |
|------|-----------------|
| gateway | wealth-gateway |
| system | wealth-system |
| user | wealth-user |
| product | wealth-product |
| account | wealth-account |
| trade | wealth-trade |
| message | wealth-message |
| search | wealth-search |

### 2. application.yml 各模块详情
#### wealth-gateway（端口 8080，无 context-path）
```yaml
server:
  port: 8080
spring:
  cloud:
    gateway:
      routes:
        - id: wealth-system
        - id: wealth-user
        - id: wealth-product
        - id: wealth-account
        - id: wealth-trade
        - id: wealth-message
        - id: wealth-search
      globalcors:
        cors-configurations:
          '[/**]':
            allowed-origin-patterns: http://localhost:3000, http://localhost:8080, http://127.0.0.1:3000
```
> gateway 无数据源，不依赖 wealth-common（WebFlux 与 spring-boot-starter-web 冲突）。
#### wealth-system（端口 8082，context-path: /system）
```yaml
server:
  port: 8082
  servlet:
    context-path: /system
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/wealth?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: ${DB_PASSWORD}
```
> 注：`password: ${DB_PASSWORD}` 由 Nacos `wealth-shared.yaml` 中的 `spring.datasource.password: 123456` 覆盖。
#### wealth-user（端口 8083，context-path: /user）
```yaml
server:
  port: 8083
  servlet:
    context-path: /user
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/wealth?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: ${DB_PASSWORD}
```

#### wealth-product（端口 8084，context-path: /product）
```yaml
server:
  port: 8084
  servlet:
    context-path: /product
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/wealth?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: ${DB_PASSWORD}
```

#### wealth-account（端口 8086，context-path: /account）
```yaml
server:
  port: 8086
  servlet:
    context-path: /account
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/wealth?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: ${DB_PASSWORD}
```

#### wealth-trade（端口 8085，context-path: /trade）
```yaml
server:
  port: 8085
  servlet:
    context-path: /trade
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/wealth?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: ${DB_PASSWORD}
```

#### wealth-message（端口 8087，context-path: /message）
```yaml
server:
  port: 8087
  servlet:
    context-path: /message
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/wealth?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: ${DB_PASSWORD}
```

#### wealth-search（端口 8089，无 context-path，无数据源）
```yaml
server:
  port: 8089
spring:
  elasticsearch:
    uris: ${ES_URIS:http://localhost:9200}
    username: ${ES_USERNAME:elastic}
    password: ${ES_PASSWORD:}
```
> wealth-search 不连接 MySQL，仅连接 ElasticSearch 8。无 `spring.datasource` 配置。

---

## 本地配置文件与 Nacos 覆盖关系

| 配置项 | 本地区域 | Nacos 覆盖 | 生效结果 |
|--------|---------|-----------|---------|
| server.port | application.yml | 无 | 本地值 |
| server.servlet.context-path | application.yml | 无 | 本地值 |
| spring.datasource.url | application.yml | 无 | 本地值（Nacos 同名配置已被此覆盖） |
| spring.datasource.username | application.yml | N/A | 本地值 |
| **spring.datasource.password** | application.yml (`${DB_PASSWORD}`) | **`123456`** | **Nacos 覆盖生效** |
| spring.elasticsearch.* | application.yml (search) | 无 | 本地值 |
| mybatis-plus.* | application.yml | 无 | 本地值 |
| springdoc.* | application.yml | 无 | 本地值 |
| **jwt.secret** | 无 | **wealth-shared.yaml** | **从 Nacos** |
| **jwt.expire** | 无 | **wealth-shared.yaml** | **从 Nacos** |
| **management.tracing.sampling.probability** | 无 | **wealth-shared.yaml** | **从 Nacos** |
| **management.zipkin.tracing.endpoint** | 无 | **wealth-shared.yaml** | **从 Nacos** |
| **management.endpoints.web.exposure.include** | 无 | **wealth-shared.yaml** | **从 Nacos** |

> 关键：`password: ${DB_PASSWORD}` 本身是无意义的环境变量引用（系统中未设置 `DB_PASSWORD`），数据库密码由 Nacos `wealth-shared.yaml` 中的 `spring.datasource.password: 123456` 提供。Nacos 配置优先级高于本地配置中的环境变量引用。

---

## 基础设施 Docker 容器

| 服务 | 镜像 | 端口 |
|------|------|------|
| Nacos | nacos/nacos-server:v2.3.2 | 8848, 9848-9849 |
| MySQL | 8.0.37 (本地安装) | 3306 |
| Redis | redis:latest | 6379 |
| RabbitMQ | rabbitmq:3.10-management | 5672, 15672 |
| ElasticSearch | elasticsearch:8.8.2 | 9200, 9300 |
| Nginx | nginx:latest | 80 |
| Zipkin | openzipkin/zipkin:latest | 9411 |
| Prometheus | prom/prometheus:latest | 9090 |
| Grafana | grafana/grafana:latest | 3001 |

---

## 已知 v1.4.0 配置限制（部分已修复，不可直接改配置文件）

| 问题 | 影响 | 说明 |
|------|------|------|
| ~~**RedisConfig 缺少 @ConditionalOnClass**~~ | ✅ 已修复 | `RedisConfig.java` 和 `RedisUtil.java` 已添加 `@ConditionalOnClass` 注解，wealth-search 启动正常。|
| ~~**AuthConstant.PERMIT_ALL_URLS 缺少 user 模块路径**~~ | ✅ 已修复 | 已包含 `/user/login`、`/user/register` 等路径。|
| ~~**PERMIT_ALL_URLS 未在 LoginInterceptor 注册模块中使用**~~ | ✅ 已修复 | Gateway JwtAuthGlobalFilter 和 LoginInterceptor 均正确引用。|
| ~~**Nacos zipkin.base-url 属性错误**~~ | ✅ 已修复 | 原使用 Spring Cloud Sleuth 旧属性 `zipkin.base-url`，已更正为 `management.zipkin.tracing.endpoint`。见 [Nacos 配置参考](nacos-config-reference.md)。|
