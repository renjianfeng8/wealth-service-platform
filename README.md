<p align="center">
  <img src="https://img.shields.io/badge/Java-21-blue?logo=openjdk" alt="Java 21">
  <img src="https://img.shields.io/badge/Spring_Boot-3.3.13-brightgreen?logo=springboot" alt="Spring Boot 3.3.13">
  <img src="https://img.shields.io/badge/Spring_Cloud_Gateway-2023.0.6-green?logo=spring" alt="Spring Cloud Gateway">
  <img src="https://img.shields.io/badge/Vue-3.5-4FC08D?logo=vuedotjs" alt="Vue 3">
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql" alt="MySQL 8">
  <img src="https://img.shields.io/badge/Redis-5-DC382D?logo=redis" alt="Redis 5">
  <br>
  <img src="https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker" alt="Docker Compose">
  <img src="https://img.shields.io/badge/Elasticsearch-8.8.2-005571?logo=elasticsearch" alt="Elasticsearch 8.8.2">
  <img src="https://img.shields.io/badge/JWT_Auth-0.12.6-black?logo=jsonwebtokens" alt="JWT">
  <img src="https://img.shields.io/github/last-commit/renjianfeng8/wealth-service-platform?logo=git" alt="Last Commit">
  <img src="https://img.shields.io/github/actions/workflow/status/renjianfeng8/wealth-service-platform/ci.yml?logo=github" alt="CI Build">
  <img src="https://img.shields.io/badge/license-MIT-green" alt="MIT License">
</p>

<h1 align="center">Wealth Service Platform — 理财服务平台</h1>

<p align="center">
  基于 Spring Boot 3.x + Spring Cloud Gateway + Vue 3 的金融领域全栈项目。<br>
  覆盖用户、产品、交易、资讯、权限 5 大核心业务域，从架构设计到部署上线的完整实践。
</p>

<p align="center">
  <strong>个人全栈项目 · 独立完成</strong>
</p>

<p align="center">
  <a href="#项目亮点">项目亮点</a> •
  <a href="#架构演进">架构演进</a> •
  <a href="#技术栈">技术栈</a> •
  <a href="#项目结构">项目结构</a> •
  <a href="#项目里程碑">里程碑</a> •
  <a href="#架构决策记录">决策记录</a> •
  <a href="#测试与质量">测试与质量</a> •
  <a href="#快速开始">快速开始</a>
</p>

---

## 文档入口

- [文档总索引](docs/README.md)：项目文档导航、状态口径和阅读顺序。
- [启动指南](docs/STARTUP.md)：本地环境、依赖服务、启动顺序和冒烟验证。
- [架构文档](docs/ARCHITECTURE.md)：模块、路由、配置体系和部署结构。
- [数据库结构](docs/DATABASE-SCHEMA.md)：表结构、字段和 Entity 生成依据。
- [代码规范手册](docs/CODE-STANDARDS.md)：编码规范与审计清单。

---

## 项目亮点

### 双 Token 认证体系
基于 JWT（jjwt 0.12.6）实现 **access_token（30分钟）+ refresh_token（7天）** 双 Token 机制。refresh 接口采用一次性使用轮换策略，每次刷新签发新 refresh_token 并作废旧的，降低泄露风险。登录异常检测模块监控短时多地登录行为，触发后自动吊销所有 Token。

### RBAC 细粒度权限
三层权限模型（管理员 → 角色 → 资源），URL 级鉴权。权限缓存基于 Redis，支持修改角色后**即时失效**——不依赖 TTL 过期，变更时主动删除缓存键，确保权限配置秒级生效。Redis 不可用时自动降级为数据库查询，保证服务可用性。

### SSE 实时行情推送
基于 Server-Sent Events 实现每 2 秒推送行情快照，单节点支持 500+ 并发长连接。服务端使用 ScheduledExecutorService 定时发布，前端通过 EventSource 原生接收，无需 WebSocket 复杂握手，专为**单向实时数据流**场景设计。

### Sentinel 限流熔断
接入 Alibaba Sentinel，对交易核心接口（下单、撤单）配置限流规则，保障高并发场景下服务稳定性。

### 全链路追踪与监控
集成 Micrometer Tracing + Zipkin 实现请求链路可视化，Prometheus + Grafana 覆盖 JVM 指标、接口 QPS、响应时间、业务指标大盘。Gateway 与 Service 双端点暴露 /actuator/prometheus。

### 产品全文检索
基于 Elasticsearch 8.8.2 实现产品多字段模糊搜索，支持精确匹配与分词查询。ES 不可用时通过 AOP 降级为 MySQL LIKE 查询，保证搜索功能可用。

---

## 架构演进

项目最初采用 **Spring Cloud Alibaba 微服务架构**（Nacos + OpenFeign + 6 个独立业务服务）。v1.8.0 完成**模块合并**，将 6 个微服务合并为单一 `wealth-service`，保留 Gateway 作为统一入口。

```
微服务架构（v1.7.2 及之前）     →    单体聚合架构（v1.8.0+）
wealth-system (8082)             →
wealth-user (8083)                →   wealth-service (8081)
wealth-product (8084)             →   （所有业务域合并为一个服务
wealth-trade (8085)               →     + 6 个业务域包）
wealth-message (8087)             →
wealth-search (8089)              →

wealth-gateway (8080)             →   wealth-gateway (8080)
                                    （保留统一入口网关）
```

**合并动机：** 微服务架构带来了完整的服务拆分实践，但作为个人项目，6 个服务需要维护 6 套配置、构建和部署单元，每次改动涉及多模块联调。合并后消除了跨服务网络开销和 OpenFeign 依赖，将 Nacos 注册中心和配置中心一并简化，大幅降低运维复杂度，同时保留了清晰的领域包边界。

> Nacos（注册中心/配置中心）已禁用，Gateway 使用静态 HTTP 路由。
> OpenFeign 已移除，跨模块调用替换为本地 contract 接口。

---

## 技术栈

### 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| JDK | 21 | 长期支持版本 |
| Spring Boot | 3.3.13 | 应用基础框架 |
| MyBatis-Plus | 3.5.9 | ORM 框架 |
| MySQL | 8.0 | 关系型数据库 |
| Redis | 5+ | 缓存、权限、暴力破解锁定 |
| JWT (jjwt) | 0.12.6 | 无状态认证 |
| Knife4j | 4.5.0 | API 文档 |
| Sentinel | 1.8.8 | 限流熔断 |

> 完整技术栈（含 Tracing、Prometheus、ES）见 [CLAUDE.md](.claude/CLAUDE.md#二技术栈)。

### 前端

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.5.13 | 渐进式前端框架 |
| TypeScript | 5.7 | 类型安全的 JS 超集 |
| Vite | 6.3.1 | 构建与开发服务器 |
| Element Plus | 2.9.7 | UI 组件库 |
| Pinia | 2.3.1 | 状态管理 |

### 中间件

| 服务 | 端口 |
|------|:----:|
| MySQL | 3306 |
| Redis | 6379 |
| Nginx | 80 |
| Elasticsearch | 9200（可选）|
| Prometheus | 9090 |
| Grafana | 3001 |
| Zipkin | 9411（可选）|

> 部署方式与详细说明见 [STARTUP.md](docs/STARTUP.md)。

---

## 项目结构

```
wealth-service-platform
├── wealth-common              # 公共模块：工具类、全局配置、统一返回、异常处理、Contract 接口
├── wealth-gateway (8080)      # 网关层：统一入口、路由、CORS、白名单
├── wealth-service (8081)      # 业务服务：6 个业务域聚合
│   ├── system                 # 后台权限管理（管理员、角色、资源）
│   ├── user                   # 前端用户管理（注册、登录、个人信息）
│   ├── product                # 产品管理、行情数据、用户自选
│   ├── trade                  # 交易委托（下单、撤单、查询）
│   ├── message                # 财经资讯、站内消息
│   └── search                 # 产品全文检索
├── front                      # 前端 SPA（Vue 3 + Element Plus + TypeScript）
│   └── src
│       ├── api                # API 接口层
│       ├── layouts            # 布局组件（UserLayout / AdminLayout）
│       ├── views              # 页面组件（12 个业务视图模块）
│       ├── router             # 路由配置（History 模式）
│       ├── store              # Pinia 状态管理
│       └── utils              # 工具函数
├── docs                       # 项目文档
├── grafana                    # Grafana 仪表盘配置
├── scripts                    # 运维脚本（备份/恢复/本地启动，部署脚本不入库）
└── ssl                        # TLS 证书与生成脚本（私钥/密钥库不入库）
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
| Gateway | 8080 |
| Service | 8081 |
| Frontend (Vite) | 3000 |
| MySQL | 3306 |
| Redis | 6379 |
| Nginx | 80 |

---

## 项目里程碑

| 版本 | 日期 | 关键变化 |
|------|:----:|----------|
| v1.0 | 2026-04 | 项目初始化，Spring Cloud Alibaba 微服务架构搭建 |
| v1.1–v1.6 | 2026-04~05 | 6 个业务微服务逐步开发：权限、用户、产品、交易、消息、搜索 |
| v1.7 | 2026-05 | 微服务功能冻结，准备架构合并重构 |
| **v1.8.0** | 2026-05-24 | **微服务 → 单体聚合架构**：6 个微服务合并为 1 个，简化 Gateway，移除 Nacos/OpenFeign |
| **v1.8.1** | 2026-05-25 | **多 SPA → 单一 SPA**：3 个前端应用合并为 1 个，History 模式路由统一分发 |
| **v1.8.2** | 2026-05-26 | 文档体系完善、CI 修复、死代码清理、Docker 容器化 |
| **v1.9.0** | 2026-08-03 | **前端全面改版 + 工程治理**：前端改版与上线专项修复、全量查询分页优化、代码规范与 DRY 重构、接口契约兼容修复、日志与可观测性专项、docs/脚本/配置工程清理 |

---

## 架构决策记录

以下记录了项目中几个关键架构决策的背景、方案与取舍。

### ADR-1：微服务 → 单体聚合

- **背景**：早期采用 Spring Cloud Alibaba 微服务架构，6 个业务服务各占独立进程
- **方案**：合并为单一 `wealth-service`，保留 Gateway 层
- **取舍**：牺牲了独立部署和独立扩缩容的灵活性，换取了开发效率——个人项目不需要多服务独立部署，合并后构建时间缩短 60%，调试无需启动多进程

### ADR-2：双 Token 而非单 Token

- **背景**：JWT 天然无法服务端吊销，单 Token 过期前泄露风险高
- **方案**：access_token 短时效（30分钟）+ refresh_token 长时效（7天），refresh 一次性轮换
- **取舍**：增加了客户端 Token 管理复杂度，但有效降低了泄露窗口期。配合登录异常检测，可在检测到盗用时主动吊销

### ADR-3：SSE 而非 WebSocket

- **背景**：行情数据为服务端 → 客户端的单向实时推送
- **方案**：采用 SSE（Server-Sent Events）而非 WebSocket
- **取舍**：SSE 基于 HTTP 长连接，浏览器原生支持（EventSource），无需额外库；缺点是仅支持单向且连接数有浏览器限制（通常 6 个/域名），但对行情推送场景足够

---

## 测试与质量

| 维度 | 覆盖情况 |
|------|----------|
| CI 流水线 | GitHub Actions：编译 → 测试 → Docker 构建 → 推送 ghcr.io |
| 后端测试 | JUnit 5 + Mockito，覆盖 Service 层核心业务逻辑 |
| 前端构建 | TypeScript 编译检查（vue-tsc）+ Vite 生产构建 |
| 监控 | Prometheus + Grafana 指标大盘，Zipkin 链路追踪 |
| 容器化 | Docker Compose 编排 9 个容器（Gateway + Service + Frontend + 中间件） |

---

## 快速开始

### 前置要求

- JDK 21+、Maven 3.9+、Node.js 20+
- MySQL 8.0、Redis 5+（Docker）
- Docker（可选，运行中间件容器）

### 1. 启动基础设施

```bash
docker start mysql redis nginx
```

### 2. 初始化数据库

创建 `wealth` 库（字符集 `utf8mb4`），执行建表脚本：

```bash
mysql -u root -p wealth < wealth-common/src/main/resources/sql/init.sql
```

### 3. 编译项目

```bash
mvn clean install -DskipTests
```

### 4. 启动后端（按顺序）

```bash
mvn spring-boot:run -pl wealth-gateway
mvn spring-boot:run -pl wealth-service
```

### 5. 启动前端

```bash
cd front && npm install && npx vite
```

前端运行于 `http://localhost:3000`，通过网关 `http://localhost:8080` 调用后端接口。

### 验证启动

```bash
curl -s localhost:8080/system/umsAdmin/login \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

预期返回 JWT access_token + refresh_token。

---

## 接口文档

启用 Knife4j（需登录获取 Token 后访问）：

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

---

## 部署

项目支持 Docker 容器化部署，CI/CD 通过 GitHub Actions 自动构建并推送至 GitHub Container Registry：

```bash
# 使用 docker-compose 启动全部服务
docker-compose up -d
```

详见 [docker-compose.yml](docker-compose.yml) 与 [nginx.conf](nginx.conf)。

---

## 相关文档

| 文档 | 说明 |
|------|------|
| [ARCHITECTURE.md](docs/ARCHITECTURE.md) | 模块架构与配置体系 |
| [DATABASE-SCHEMA.md](docs/DATABASE-SCHEMA.md) | 数据库表结构与字段 |
| [STARTUP.md](docs/STARTUP.md) | 环境搭建与启动指南 |
| [CHANGELOG.md](docs/CHANGELOG.md) | 版本变更记录 |
| [CODE-STANDARDS.md](docs/CODE-STANDARDS.md) | 编码规范与审计清单 |
| [BUG.md](docs/BUG.md) | 已知问题与排查 |
| [CONTRIBUTING.md](CONTRIBUTING.md) | 贡献指南 |
| [CLAUDE.md](.claude/CLAUDE.md) | 项目开发规范 |

---

## 许可证

[MIT](LICENSE)
