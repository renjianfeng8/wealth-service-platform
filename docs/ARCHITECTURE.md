
# 模块架构与配置体系

> 跨模块开发时引用 — 模块依赖、端口路由、配置体系、基础设施。

---

## 目录

- [项目模块架构](#项目模块架构)
- [架构演进](#架构演进)
- [依赖层级](#依赖层级)
- [端口与路由](#端口与路由)
- [业务域一览](#业务域一览)
- [配置体系](#配置体系)
- [基础设施容器](#基础设施容器)
- [部署架构](#部署架构)

---

## 项目模块架构

```
wealth-service-platform (pom)
├── wealth-common      # 公共依赖：DTO、工具类、Contract 接口、统一返回、异常处理、通用配置
├── wealth-gateway     # 网关（Spring Cloud Gateway，路由转发、全局 CORS，无数据源）
├── wealth-service     # 业务聚合服务：所有业务域合并为单个可部署单元
└── front              # 前端 SPA（Vue 3 + Element Plus + Pinia + TypeScript）
```

### 前端架构

前端为单一 SPA 项目（`front/`），通过 History 模式路由分发所有页面：

```
front/
├── src/
│   ├── api/           # API 接口层（TypeScript，每个业务域一个文件）
│   ├── layouts/       # 布局组件
│   │   ├── UserLayout.vue        # 顶部导航布局（公开页 + 用户端）
│   │   ├── AdminLayout.vue       # 侧栏导航布局（管理端）
│   │   ├── Navbar.vue            # 导航栏组件
│   │   └── Sidebar.vue           # 侧栏组件
│   ├── views/         # 页面组件（13 个页面目录）
│   │   ├── home/      # 首页（公开）
│   │   ├── auth/      # 登录
│   │   ├── register/  # 注册
│   │   ├── market/    # 行情
│   │   ├── trade/     # 交易委托
│   │   ├── products/  # 产品列表
│   │   ├── news/      # 财经资讯
│   │   ├── message/   # 站内消息
│   │   ├── dashboard/ # 仪表盘（用户/管理）
│   │   ├── profile/   # 个人中心
│   │   ├── favorite/  # 我的自选
│   │   ├── error/     # 404 / 403
│   │   └── admin/     # 管理端（用户/产品/权限等）
│   ├── router/        # 路由配置（公开路由 + 用户路由 + 管理路由）
│   ├── store/         # Pinia 状态管理（auth、user）
│   └── utils/         # 工具函数（auth、request、sse）
```

### 认证流程

```
用户打开 http://localhost:3000/auth/login
  → 输入用户名密码 → POST /user/identify-login（统一登录，自动识别用户/管理员）
  → 后端返回 token + refreshToken + userType，并写入 Cookie
  → 前端存入 sessionStorage，设置 wealth_logged_in + wealth_role 标志
  → 路由守卫根据 role 自动跳转：
       - admin  → /admin/dashboard
       - user   → /user/dashboard
  → 退出登录清除 sessionStorage，跳转回 /auth/login
```

> 接口例外约定：SSE `/product/wea-market-data/sse` 为全站唯一不包 `Result` 信封的接口，事件载荷为裸 `List<MarketDataVO>` 数组。

---

## 架构演进

v1.8.0 完成模块合并，6 个独立微服务合并为单一的 `wealth-service`，保留 Gateway 网关层：

```
v1.7.2 (微服务)                  v1.8.0+ (单体聚合)
wealth-system (8082)             
wealth-user   (8083)             
wealth-product (8084)            →  wealth-service (8081)
wealth-trade   (8085)             →  （5 个业务域包）
wealth-message (8087)             
wealth-search  (8089)            
wealth-gateway (8080)            →  wealth-gateway (8080)
```

v1.8.1 完成前端合并，3 个独立 SPA 合并为单一 `front/`：

```
v1.8.0 (三 SPA)                  v1.8.1+ (单一 SPA)
front-landing/ (3002)            
front-user/    (3001)            →  front/ (3000)
front/         (3000)            
```

---

## 依赖层级

- `wealth-common` 被所有模块依赖（修改后需先 `mvn clean install -pl backend/wealth-common -DskipTests`）
- `wealth-gateway` 依赖 `wealth-common`（工具类、DTO、常量）
- 跨域调用通过 contract 接口（`com.wealth.common.contract`）直接调用，无需 Feign

---

## 端口与路由

### 端口分配

| 模块 | 端口 | context-path | 说明 |
|------|:----:|:-----------:|------|
| wealth-gateway | 8080 | — | Spring Cloud Gateway（WebFlux） |
| wealth-service | 8081 | / | 所有业务域聚合 |
| 前端 Vite | 3000 | / | 开发服务器 |

### 网关路由

Gateway 使用静态 HTTP 路由，无需 Nacos。所有请求转发到同一 `wealth-service` 实例：

| 路由前缀 | 转发目标 | 对应业务域 |
|---------|---------|-----------|
| /system/** | http://localhost:8081 | 后台权限 |
| /user/** | http://localhost:8081 | 用户管理 |
| /product/** | http://localhost:8081 | 产品行情 |
| /trade/** | http://localhost:8081 | 交易委托 |
| /message/** | http://localhost:8081 | 消息推送 |

> 不同前缀在服务端通过 context-path 区分。

### 前端路由

| 路由 | 布局 | 权限 | 说明 |
|------|------|------|------|
| `/auth/login` | — | 公开 | 登录页 |
| `/auth/register` | — | 公开 | 注册页 |
| `/home` | UserLayout | 公开 | 首页 |
| `/products` | UserLayout | 公开 | 产品列表 |
| `/market/:code` | UserLayout | 公开 | 行情详情 |
| `/news` | UserLayout | 公开 | 资讯列表 |
| `/user/*` | UserLayout | 用户 | 用户端页面 |
| `/admin/*` | AdminLayout | 管理员 | 管理端页面 |

---

## 业务域一览

所有业务域在 `wealth-service` 模块内，基包 `com.wealth.platform.{domain}`：

| 业务域 | domain | 表前缀 | 说明 |
|--------|--------|--------|------|
| 后台权限 | system | ums_* | 管理员、角色、资源、RBAC 权限拦截 |
| 用户管理 | user | sys_user | 前端用户注册/登录/信息管理 |
| 产品行情 | product | wea_product, wea_market_data, wea_user_favorite | 产品管理、行情 SSE 推送、用户自选 |
| 交易委托 | trade | wea_trade_order | 交易订单发起、撤单、查询 |
| 消息推送 | message | wea_news, wea_message | 财经资讯、站内消息（DB 轮询）|

---

## 配置体系

### 配置架构

项目采用**环境变量 + application.yml 本地配置**模式，不依赖外部配置中心：

```
deploy/env/.env（docker-compose 注入）
  ├── backend/wealth-gateway/.env（网关专用）
  └── backend/wealth-service/.env  （业务服务专用）
         ↓
application.yml（本地配置，引用 ${ENV_VAR} 占位符）
         ↓
application-prod.yml（生产环境配置覆盖）
```

### Spring 配置加载顺序

```
application.yml              # 1. 本地配置 —— 端口、数据源、Redis
  └─→ application-prod.yml   # 2. 生产环境覆盖（spring.profiles.active=prod）
       └─→ .env 环境变量     # 3. 环境变量注入 ${VAR:default}
```

### JWT 配置

```yaml
jwt:
  secret: ${JWT_SECRET}
  access-expire: ${JWT_ACCESS_EXPIRE:1800000}
  refresh-expire: ${JWT_REFRESH_EXPIRE:604800000}
```

JWT 密钥值来自模块 `.env` 文件（已在 `.gitignore` 中排除）。`JwtUtil` 在 `@PostConstruct` 中校验密钥字节≥32，启动时即失败而非运行时。`JwtUtil` 读取 `jwt.access-expire` / `jwt.refresh-expire` 控制 access/refresh 双 token 时效。

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

---

## 基础设施容器

| 服务 | 镜像 | 端口 | 必需 | 说明 |
|------|------|:----:|:----:|------|
| MySQL | mysql:8.0 | 3306 | 是 | 数据库 |
| Redis | redis:latest | 6379 | 是 | 缓存 |
| Nginx | nginx:latest | 80, 443 | 是 | 反向代理 |
| Zipkin | openzipkin/zipkin:latest | 9411 | 否 | 链路追踪 |
| Prometheus | prom/prometheus:latest | 9090 | 否 | 监控指标存储 |
| Grafana | grafana/grafana:latest | 3001 | 否 | 监控仪表盘 |

> Nacos、RabbitMQ、Seata 已停用。

---

## 部署架构

```
                                    ┌──────────────────┐
                                    │   Nginx :80/443  │
                                    │  (SSL 终止 +     │
                                    │   反向代理)       │
                                    └────────┬─────────┘
                                             │
                          ┌──────────────────┴──────────────────┐
                          │                                     │
                   ┌──────▼───────┐                    ┌───────▼────────┐
                   │  Gateway     │                    │  前端 SPA      │
                   │  (8080)      │                    │  front/ dist   │
                   └──────┬───────┘                    └────────────────┘
                          │
                   ┌──────▼───────┐
                   │ wealth-serv. │
                   │  (8081)      │
                   └──────┬───────┘
                          │
     ┌─────────┬──────────┬─────────┐
┌────▼───┐┌───▼────┐┌───▼────┐┌───▼─────┐
│ MySQL  ││ Redis  ││ Zipkin ││Prometheus│
│ (3306) ││ (6379) ││ (9411) ││ (9090)   │
└────────┘└────────┘└────────┘└──────────┘
```

> 生产环境 Nginx 负责 SSL 终止，静态 SPA 资源由 Nginx 直接 serve（`front/dist`），API 请求转发至 Gateway 内部端口 8080。
