
# Wealth Service Platform — 启动指南

> 面向开发者的本地环境搭建与启动手册。适用于 v1.8.0+（单体聚合架构）。

---

## 目录

- [技术栈](#技术栈)
- [前置要求](#前置要求)
- [基础设施启动](#基础设施启动)
- [环境变量配置](#环境变量配置)
- [数据库初始化](#数据库初始化)
- [后端编译与启动](#后端编译与启动)
- [前端启动](#前端启动)
- [验证启动](#验证启动)
- [端口对照表](#端口对照表)
- [测试账号](#测试账号)
- [常见问题排查](#常见问题排查)

---

## 技术栈

| 层面 | 技术 | 版本 |
|------|------|------|
| 基础框架 | Spring Boot | 3.3.13 |
| 微服务组件 | Spring Cloud / Alibaba | 2023.0.6 / 2023.0.3.4 |
| ORM | MyBatis-Plus | 3.5.9 |
| 数据库 | MySQL | 8.0 |
| 缓存 | Redis | 5+ |
| 前端 | Vue 3 + Vite + Element Plus + Pinia + TypeScript | 3.5.13 / 6.3.1 / 2.9.7 / 2.3.1 / 5.7 |
| API 文档 | Knife4j | 4.5.0 |
| 认证 | JWT (jjwt) | 0.12.6 |
| 链路追踪 | Micrometer Tracing + Zipkin | 1.3.6 |
| 监控 | Prometheus + Grafana | — |

---

## 前置要求

| 组件 | 最低版本 | 检查命令 |
|------|---------|----------|
| JDK | 21+ | `java -version` |
| Maven | 3.9+ | `mvn -version` |
| Node.js | 18+（推荐 20 LTS） | `node -v` |
| Docker | 24.0+ | `docker --version` |

---

## 基础设施启动

项目依赖 MySQL、Redis、Nginx 三个必需中间件，推荐通过 Docker 运行：

```bash
# 启动必需中间件（容器名与编排见 docker-compose.yml）
docker compose up -d mysql redis nginx
```

### 容器详情

| 服务 | 端口 | 必需 | 说明 |
|------|:----:|:----:|------|
| MySQL | 3306 | ✓ | 关系型数据库 |
| Redis | 6379 | ✓ | 缓存、暴力破解锁定 |
| Nginx | 80 | ✓ | 反向代理 |
| Zipkin | 9411 | — | 链路追踪 |
| Prometheus | 9090 | — | 监控指标采集 |
| Grafana | 3001 | — | 监控可视化 |

> Nacos、RabbitMQ、Seata 已停用，对应配置也已清理。

---

## 环境变量配置

项目使用 `.env` 文件管理敏感配置，需创建以下文件：

```
根目录 .env                  # MySQL/Redis 密码
wealth-gateway/.env          # JWT 密钥、路由配置
wealth-service/.env          # JWT 密钥、数据源、Redis 配置
```

模板参考根目录 `.env.example`。

关键变量：

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `JWT_SECRET` | JWT 签名密钥（≥32 字节） | — |
| `MYSQL_ROOT_PASSWORD` | MySQL 密码 | — |
| `REDIS_PASSWORD` | Redis 密码 | 空 |

---

## 数据库初始化

```bash
# 创建数据库
mysql -u root -p"${MYSQL_ROOT_PASSWORD}" -e \
  "CREATE DATABASE IF NOT EXISTS wealth DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 导入建表语句 + 测试数据
mysql -u root -p"${MYSQL_ROOT_PASSWORD}" wealth \
  < wealth-common/src/main/resources/sql/init.sql
```

---

## 后端编译与启动

> **编译顺序**：必须先编译 `wealth-common`，再编译其他模块。

### 编译

```bash
# 1. 编译公共模块并安装到本地仓库
# （修改 common 后必须重新执行此步）
mvn clean install -pl wealth-common -DskipTests

# 2. 全量编译
mvn clean compile

# 3. 打包（产出 JAR）
mvn clean package -DskipTests
```

JAR 包路径：
- `wealth-gateway/target/wealth-gateway-1.0.0.jar`
- `wealth-service/target/wealth-service-1.0.0.jar`

### 启动（按顺序）

```bash
# 1. 启动网关（端口 8080）
mvn spring-boot:run -pl wealth-gateway

# 2. 启动业务服务（端口 8081）
mvn spring-boot:run -pl wealth-service
```

所有业务域（system / user / product / trade / message）聚合在 `wealth-service` 中，无需分别启动。

### 查看日志

```bash
# 确认服务启动成功
# wealth-service 日志中搜索：
#   HikariPool-1 - Start completed     ← 数据库连接成功
#   Started WealthServiceApplication   ← 服务启动完成
```

---

## 前端启动

前端已合并为单一 SPA 项目（`front/`），通过 History 模式路由分发用户端和管理端页面。

```bash
cd front

# 安装依赖
npm install

# 启动开发服务器（端口 3000）
npx vite
```

### 前端架构

```
front/
├── src/
│   ├── api/           # API 接口层（TypeScript）
│   ├── layouts/       # 布局组件
│   │   ├── UserLayout.vue        # 顶部导航布局（用户端 + 公开页）
│   │   └── AdminLayout.vue       # 侧栏导航布局（管理端）
│   ├── views/         # 页面组件
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
│   ├── router/        # 路由配置（History 模式）
│   ├── store/         # Pinia 状态管理
│   └── utils/         # 工具函数（auth、request 等）
```

### 路由结构

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

## 验证启动

### 命令行验证

```bash
# 统一登录（自动识别管理员/用户）
curl -s -X POST "http://localhost:8080/user/identify-login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 预期返回（HTTP 200 + JWT Token + Set-Cookie）：
# {"code":200,"message":"success","data":{"token":"eyJ...","userId":"1","nickname":"...","userType":"admin","refreshToken":"eyJ..."}}
```

### 页面访问

| 页面 | URL |
|------|-----|
| 首页 | http://localhost:3000/home |
| 登录页 | http://localhost:3000/auth/login |
| 用户端 | http://localhost:3000/user/dashboard |
| 管理端 | http://localhost:3000/admin/dashboard |
| Knife4j 文档 | http://localhost:8080/doc.html |

### 业务接口测试

```bash
TOKEN="<从登录响应中获取的 accessToken>"

# 管理员分页
curl -s -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/system/umsAdmin/page?pageNum=1&pageSize=10"

# 产品分页
curl -s -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/product/wea-product/page?pageNum=1&pageSize=10"

# 交易订单
curl -s -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/trade/wea-trade-order/page?pageNum=1&pageSize=10"
```

---

## 端口对照表

| 模块 | 端口 | 说明 |
|------|:----:|------|
| **中间件** | | |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |
| Nginx | 80 | 反向代理 |
| **后端** | | |
| wealth-gateway | **8080** | 网关（统一入口）|
| wealth-service | **8081** | 业务聚合服务 |
| **前端** | | |
| Vite 开发服务器 | **3000** | 前端 SPA |

---

## 测试账号

| 身份 | 用户名 | 密码 | 登录入口 |
|------|--------|------|----------|
| 管理员 | admin | admin123 | `/auth/login`（自动识别为管理员）|
| 前台用户 | zhangwei | 123456 | `/auth/login`（自动识别为用户）|

---

## 常见问题排查

### 后端无法启动

| 现象 | 原因 | 解决 |
|------|------|------|
| HikariPool 连接失败 | MySQL 未启动或密码错误 | 检查容器状态 + `.env` 中的 `MYSQL_ROOT_PASSWORD` |
| Redis 连接拒绝 | Redis 未启动 | 检查容器状态 + `.env` 中的 `REDIS_HOST` |
| 表不存在 | 未执行 `init.sql` | 确认已导入建表脚本 |
| JWT 密钥异常 | `JWT_SECRET` 缺失或不足 32 字节 | 检查 `.env` 文件 |
| 启动时 ClassNotFoundException | Common 未 install | 执行 `mvn clean install -pl wealth-common -DskipTests` |

### 前端无法启动

| 现象 | 原因 | 解决 |
|------|------|------|
| `npm install` 失败 | 网络或 Node 版本 | 检查 Node.js ≥ 18，尝试切换 npm 镜像源 |
| 页面白屏 | 依赖或构建问题 | 删除 `node_modules` 和 `package-lock.json` 重试 |
| API 请求 502 | 后端未启动 | 确认网关 (8080) 和 wealth-service (8081) 已启动 |
| 登录后跳转异常 | 路由或认证状态异常 | 清除 sessionStorage 后刷新重试 |

### 端口占用

```bash
# 查看端口占用
netstat -ano | findstr ":8080"

# 终止进程
taskkill /PID <PID> /F
```

### 跨域问题

网关已全局配置 CORS，允许 Vite 开发服务器（`localhost:3000`）及生产环境域名访问。如果跳过网关直接访问 `localhost:8081` 需要同时在 wealth-service 配置 CORS。

---

## 启动顺序汇总

```
Docker 容器
  mysql → redis → nginx
  （可选：zipkin / prometheus / grafana）
       ↓
mvn install -pl wealth-common（必须先编译公共依赖）
       ↓
wealth-gateway（端口 8080，最先启动）
       ↓
wealth-service（端口 8081，所有业务域聚合）
       ↓
front/（端口 3000，Vite 开发服务器）
```

> 每次修改 `wealth-common` 后，必须重新执行 `mvn clean install -pl wealth-common -DskipTests` 才能被其他模块引用。
