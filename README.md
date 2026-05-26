
<p align="center">
  <img src="https://img.shields.io/badge/Java-21-blue?logo=openjdk" alt="Java 21">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.3.13-brightgreen?logo=springboot" alt="Spring Boot 3.3.13">
  <img src="https://img.shields.io/badge/Vue-3.5-4FC08D?logo=vuedotjenkis" alt="Vue 3">
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql" alt="MySQL 8">
  <img src="https://img.shields.io/badge/Redis-5-DC382D?logo=redis" alt="Redis 5">
  <img src="https://img.shields.io/badge/license-MIT-green" alt="MIT License">
  <img src="https://img.shields.io/badge/PRs-welcome-brightgreen" alt="PRs Welcome">
</p>

<h1 align="center">Wealth Service Platform — 理财服务平台</h1>

<p align="center">
  基于 Spring Boot 3.x + Vue 3 的金融级单体聚合架构项目，覆盖用户、产品、交易、消息等核心业务领域，提供高可用、高扩展的企业级金融解决方案。
</p>

<p align="center">
  <a href="#核心特性">核心特性</a> •
  <a href="#技术栈">技术栈</a> •
  <a href="#项目结构">项目结构</a> •
  <a href="#快速开始">快速开始</a> •
  <a href="#接口文档">接口文档</a> •
  <a href="#部署">部署</a> •
  <a href="#相关文档">相关文档</a>
</p>

---

## 核心特性

- **🔐 双 Token 认证** — JWT access_token（30 分钟）+ refresh_token（7 天），refresh 一次性使用防重放
- **🛡️ RBAC 权限体系** — 管理员、角色、资源三层模型，URL 级细粒度鉴权
- **🚦 限流熔断** — Sentinel 集成，保障服务稳定性
- **📈 实时行情推送** — SSE（Server-Sent Events）每 2 秒推送行情快照
- **🔍 产品全文检索** — 基于 Elasticsearch 的产品搜索（支持降级为 MySQL LIKE）
- **📊 全链路追踪** — Micrometer Tracing + Zipkin，请求链路可视化
- **📋 监控指标** — Prometheus + Grafana，JVM/接口/业务指标全覆盖
- **🛡️ 安全防护** — XSS 过滤、暴力破解锁定、BCrypt 密码加密、参数校验
- **📱 统一前端** — Vue 3 SPA，用户端与管理端同一项目

## 架构演进

项目最初采用 Spring Cloud Alibaba 微服务架构。v1.7.3 完成**模块合并**，将 6 个独立业务服务合并为单一 `wealth-service`，保留 Gateway 网关层用于统一入口和路由。

```
微服务架构（v1.7.2 及之前）     →    单体聚合架构（v1.7.3+）
wealth-system (8082)             →
wealth-user (8083)                →   wealth-service (8081)  
wealth-product (8084)             →   （所有业务域合并为一个服务
wealth-trade (8085)               →     + 6 个业务域包）
wealth-message (8087)             →
wealth-search (8089)              →

wealth-gateway (8080)             →   wealth-gateway (8080)
                                    （保留统一入口网关）
```

> Nacos（注册中心/配置中心）已禁用，Gateway 使用静态 HTTP 路由。
> OpenFeign 已移除，跨模块调用替换为本地 contract 接口。

## 技术栈

### 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| JDK | 21 | 长期支持版本 |
| Spring Boot | 3.3.13 | 应用基础框架 |
| Spring Cloud | 2023.0.6 | 微服务组件 |
| MyBatis-Plus | 3.5.9 | ORM 框架 |
| MySQL | 8.0 | 关系型数据库 |
| Redis | 5+ | 缓存、暴力破解锁定 |
| Elasticsearch | 8.8.2 | 全文检索（可选） |
| Knife4j | 4.5.0 | API 文档（OpenAPI） |
| JWT (jjwt) | 0.12.6 | 无状态认证 |
| Sentinel | 1.8.8 | 限流熔断 |
| Micrometer Tracing | 1.3.6 | 全链路追踪 |

### 前端

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.5.13 | 渐进式前端框架 |
| TypeScript | 5.7 | 类型安全的 JS 超集 |
| Vite | 6.3.1 | 构建与开发服务器 |
| Element Plus | 2.9.7 | UI 组件库 |
| Pinia | 2.3.1 | 状态管理 |
| Axios | — | HTTP 请求库 |

### 中间件

| 服务 | 端口 | 部署方式 |
|------|:----:|----------|
| MySQL | 3306 | Docker / 本地 |
| Redis | 6379 | Docker |
| Nginx | 80 | Docker |
| Elasticsearch | 9200 | Docker（可选）|
| Prometheus | 9090 | Docker |
| Grafana | 3001 | Docker |
| Zipkin | 9411 | Docker（可选）|

## 项目结构

```
wealth-service-platform
├── wealth-common              # 公共模块：工具类、全局配置、统一返回、异常处理、Contract 接口
├── wealth-gateway (8080)      # 网关层：统一入口、路由、CORS
├── wealth-service (8081)      # 业务服务：6 个业务域聚合
│   ├── system                 # 后台权限管理（管理员、角色、资源）
│   ├── user                   # 前端用户管理（注册、登录、个人信息）
│   ├── product                # 产品管理、行情数据、用户自选
│   ├── trade                  # 交易委托（下单、撤单、查询）
│   ├── message                # 财经资讯、站内消息
│   └── search                 # 产品全文检索
└── front                      # 前端 SPA（Vue 3 + Element Plus + TypeScript）
    └── src
        ├── api                # API 接口层
        ├── layouts            # 布局组件（UserLayout / AdminLayout）
        ├── views              # 页面组件
        ├── router             # 路由配置（History 模式）
        ├── store              # Pinia 状态管理
        └── utils              # 工具函数
```

### 路由架构

| 路由前缀 | context-path | 目标服务 |
|---------|:-----------:|----------|
| /system/** | /system | 后台权限管理 |
| /user/** | /user | 前端用户管理 |
| /product/** | /product | 产品 + 行情 + 自选 |
| /trade/** | /trade | 交易委托 |
| /message/** | /message | 资讯 + 消息 |
| /search/** | /search | 产品搜索 |

### 端口分配

| 模块 | 端口 |
|------|:----:|
| wealth-gateway | 8080 |
| wealth-service | 8081 |
| Frontend (Vite) | 3004 |
| MySQL | 3306 |
| Redis | 6379 |
| Nginx | 80 |

## 快速开始

### 前置要求

- JDK 21+
- Maven 3.9+
- Node.js 20+
- MySQL 8.0
- Redis 5+（Docker）
- Docker（可选，运行中间件容器）

### 1. 启动基础设施

```bash
# 启动 MySQL、Redis、Nginx 等中间件
docker start mysql redis nginx
```

### 2. 初始化数据库

创建 `Wealth` 库（字符集 `utf8mb4`），执行建表脚本：

```bash
mysql -u root -p Wealth < wealth-common/src/main/resources/sql/init.sql
```

### 3. 编译项目

```bash
# 编译公共模块（修改 common 后必须先执行此步）
mvn clean install -pl wealth-common -DskipTests

# 全量编译
mvn clean install -DskipTests
```

### 4. 启动后端

按顺序启动：

```bash
# 1. 启动网关
mvn spring-boot:run -pl wealth-gateway

# 2. 启动业务服务
mvn spring-boot:run -pl wealth-service
```

### 5. 启动前端

```bash
cd front
npm install
npx vite
```

前端默认运行在 `http://localhost:3004`，通过网关 `http://localhost:8080` 调用后端接口。

### 6. 验证启动

```bash
# 管理员登录测试
curl -s localhost:8080/system/umsAdmin/login \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

预期返回 JWT access_token + refresh_token。

## 接口文档

启用 Knife4i（需登录获取 Token 后访问）：

| 入口 | 地址 |
|------|------|
| 网关统一文档 | http://localhost:8080/doc.html |
| 直接访问 | http://localhost:8081/swagger-ui/index.html |

### 统一返回格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未登录 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器异常 |

## 部署

项目支持 Docker 容器化部署，CI/CD 通过 GitHub Actions 自动构建：

```bash
# 构建 Docker 镜像
docker build -f wealth-gateway/Dockerfile -t wealth-gateway wealth-gateway
docker build -f wealth-service/Dockerfile -t wealth-service wealth-service

# 使用 docker-compose 启动
docker-compose up -d
```

详见部署配置 [docker-compose.yml](docker-compose.yml) 与 Nginx 配置 [nginx.conf](nginx.conf)。

## 相关文档

| 文档 | 说明 |
|------|------|
| [ARCHITECTURE.md](docs/ARCHITECTURE.md) | 模块架构与配置体系 |
| [DATABASE-SCHEMA.md](docs/DATABASE-SCHEMA.md) | 数据库表结构与字段 |
| [STARTUP.md](docs/STARTUP.md) | 环境搭建与启动指南 |
| [CHANGELOG.md](docs/CHANGELOG.md) | 版本变更记录 |
| [BUG.md](docs/BUG.md) | 已知问题与排查 |
| [CONTRIBUTING.md](CONTRIBUTING.md) | 贡献指南 |

## 贡献

欢迎提交 Pull Request 或 Issue。

详见 [CONTRIBUTING.md](CONTRIBUTING.md)。

## 许可证

[MIT](LICENSE)
