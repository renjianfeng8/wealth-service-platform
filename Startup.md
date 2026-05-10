# 金融中台项目 — 启动指南

## 项目简介

金融中台（Finance Mid Platform）是一个面向金融业务的双端架构系统，包含：

- **管理员后台**（front/）：后台管理系统，面向运营人员，管理用户、产品、权限等
- **用户前台**（front-user/）：用户端门户，面向普通用户，提供行情查看、交易委托、自选管理等

后端采用 Spring Cloud Alibaba 微服务架构，统一通过 Nacos 注册中心 + Spring Cloud Gateway 网关对外提供服务。

---

## 一、技术栈

| 层面 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot / Spring Cloud / Alibaba | 3.3.5 / 2023.0.3 / 2023.0.1.2 |
| ORM | MyBatis-Plus | 3.5.7 |
| 注册中心/配置中心 | Nacos | 2.3.0 |
| 网关 | Spring Cloud Gateway | 4.1.5 |
| 数据库 | MySQL | 8.0.37 |
| 缓存 | Redis | 5.0.14.1 |
| 消息队列 | RabbitMQ | 3.10.20 |
| 搜索引擎 | Elasticsearch | 8.11.0 |
| 前端（双端） | Vue 3 + Vite + Element Plus + Pinia + TypeScript | 3.5.13 / 6.3.1 / 2.9.7 / 2.3.1 / 5.7 |
| E2E 测试 | Playwright | 1.59+ |

---

## 二、前置环境准备

| 组件 | 版本要求 | 检查命令 |
|------|---------|---------|
| JDK | 21.0.3+ | `java -version` |
| Maven | 3.9.9+ | `mvn -version` |
| Node.js | 18+ | `node -v` |
| Docker | 24.0+ | `docker --version` |
| MySQL 客户端 | 8.0+ | `mysql --version` |

---

## 三、中间件启动（Docker）

### 3.1 启动容器

```bash
# Nacos（注册中心 + 配置中心）
docker run -d --name nacos -p 8848:8848 -p 9848:9848 -e MODE=standalone nacos/nacos-server:v2.3.0

# MySQL
docker run -d --name mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=123456 mysql:8.0.37

# Redis
docker run -d --name redis -p 6379:6379 redis:5.0.14.1

# RabbitMQ（含管理控制台）
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3.10.20-management

# Elasticsearch
docker run -d --name es -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" elasticsearch:8.11.0
```

验证运行状态：

```bash
docker ps --format "table {{.Names}}\t{{.Status}}"
```

### 3.2 Nacos 配置中心设置

访问 Nacos 控制台 [http://localhost:8848/nacos](http://localhost:8848/nacos)（默认账号：nacos / nacos），创建共享配置：

- **Data ID**：`finance-shared.yaml`
- **配置格式**：YAML
- **内容**：

```yaml
jwt:
  secret: finance-micro-service-20260501-very-safe-secret-key-123456789
  expire: 604800000
```

> JWT 密钥必须 ≥ 32 字节（当前密钥 58 字节），否则服务启动时会直接报错。

---

## 四、数据库初始化

```bash
# 创建数据库
mysql -u root -p123456 -e "CREATE DATABASE IF NOT EXISTS finance DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 导入建表语句 + 测试数据
mysql -u root -p123456 finance < finance-common/src/main/resources/sql/init.sql
```

数据库 `finance` 包含 12 张表，覆盖全业务场景。

---

## 五、编译项目

**编译顺序**：必须先编译 `finance-common`，再编译其他模块。

```bash
# 1. 编译公共模块并安装到本地仓库（修改 common 后必须重新执行此步）
mvn clean install -pl finance-common -DskipTests

# 2. 全量编译
mvn clean compile

# 3. 打包所有模块
mvn clean package -DskipTests

# 4. （可选）安装所有模块
mvn clean install -DskipTests
```

各模块 JAR 包路径：`{模块名}/target/{模块名}-1.0.0.jar`

---

## 六、后端服务启动（按顺序）

数据库密码统一通过环境变量传入（Windows 使用 `set`，Linux/Mac 使用 `export`）：

```bash
# Windows PowerShell
$env:DB_PASSWORD="123456"

# Windows CMD
set DB_PASSWORD=123456

# Linux / Mac
export DB_PASSWORD=123456
```

### 6.1 网关（最先启动，依赖 Nacos）

```bash
java -jar finance-gateway/target/finance-gateway-1.0.0.jar > gateway.log 2>&1 &
```

- 端口：**8080**
- Nacos 服务名：finance-gateway
- 类型：Spring Cloud Gateway（WebFlux）

### 6.2 系统服务（提供登录鉴权和 RBAC 权限）

```bash
DB_PASSWORD=123456 java -jar finance-system/target/finance-system-1.0.0.jar > system.log 2>&1 &
```

- 端口：**8082**，context-path：`/system`
- Nacos 服务名：finance-system
- 功能：管理员 CRUD、角色管理、资源管理、JWT 登录、权限拦截

### 6.3 业务服务（无先后依赖，可并行启动）

```bash
# 用户服务（前端用户管理）
DB_PASSWORD=123456 java -jar finance-user/target/finance-user-1.0.0.jar > user.log 2>&1 &

# 产品服务（产品 + 行情）
DB_PASSWORD=123456 java -jar finance-product/target/finance-product-1.0.0.jar > product.log 2>&1 &

# 账户服务（用户自选）
DB_PASSWORD=123456 java -jar finance-account/target/finance-account-1.0.0.jar > account.log 2>&1 &

# 交易服务（委托交易）
DB_PASSWORD=123456 java -jar finance-trade/target/finance-trade-1.0.0.jar > trade.log 2>&1 &

# 消息服务（资讯 + 站内消息）
DB_PASSWORD=123456 java -jar finance-message/target/finance-message-1.0.0.jar > message.log 2>&1 &

# 搜索服务（ES 产品搜索，无数据库依赖）
DB_PASSWORD=123456 java -jar finance-search/target/finance-search-1.0.0.jar > search.log 2>&1 &
```

### 6.4 验证后端服务

```bash
# 检查端口监听
netstat -ano | findstr ':8080 :8082 :8083 :8084 :8085 :8086 :8087 :8089'

# 检查启动日志
grep "Started" gateway.log system.log user.log product.log account.log trade.log message.log search.log
```

预期输出 8 行 `Started xxxApplication in ...`（每个服务一行）。

---

## 七、管理员后台启动（front/）

```bash
cd front

# 安装依赖（首次或依赖变更时执行）
npm install

# 启动开发服务器
npm run dev
```

- 端口：**3000**
- Vite 代理：`/api` → `http://localhost:8080`（网关）
- 登录账号：`admin` / `admin123`（ums_admin 表）

验证：

```bash
curl -s http://localhost:3000 | head -5
# 应包含 <div id="app"></div>
```

---

## 八、用户前台启动（front-user/）

```bash
cd front-user

# 安装依赖（首次或依赖变更时执行）
npm install

# 启动开发服务器
npm run dev
```

- 端口：**3001**
- Vite 代理：`/api` → `http://localhost:8080`（网关）
- 登录账号：`zhangwei` / `123456`（sys_user 表）

验证：

```bash
curl -s http://localhost:3001 | head -5
# 应包含 <div id="app"></div>
```

---

## 九、全链路验证

### 9.1 登录接口（管理员）

```bash
curl -s --noproxy "*" -X POST "http://localhost:8080/system/umsAdmin/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

预期返回 `200` + JWT Token。

### 9.2 登录接口（用户前台）

```bash
curl -s --noproxy "*" -X POST "http://localhost:8080/user/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"zhangwei","password":"123456"}'
```

预期返回 `200` + JWT Token。

### 9.3 业务接口测试（携带 Token）

```bash
# 设置 Token（替换为实际返回的 token）
TOKEN="eyJhbGciOiJIUzUxMiJ9..."

# 管理员分页
curl -s --noproxy "*" -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/system/umsAdmin/page?pageNum=1&pageSize=10"

# 用户分页
curl -s --noproxy "*" -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/user/page?pageNum=1&pageSize=10"

# 产品分页
curl -s --noproxy "*" -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/product/finProduct/page?pageNum=1&pageSize=10"

# 行情数据
curl -s --noproxy "*" -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/product/finMarketData/list?productCode=GOLD001"

# 交易订单分页
curl -s --noproxy "*" -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/trade/finTradeOrder/page?pageNum=1&pageSize=10"

# 新闻分页
curl -s --noproxy "*" -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/message/finNews/page?pageNum=1&pageSize=10"

# 用户自选列表
curl -s --noproxy "*" -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/account/finUserFavorite/list?userId=1"

# 搜索（需 ES 运行）
curl -s --noproxy "*" -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/search/product/search?keyword=黄金&page=1&size=10"
```

### 9.4 浏览器访问

| 应用 | URL |
|------|-----|
| 管理员后台 | [http://localhost:3000](http://localhost:3000) |
| 用户前台 | [http://localhost:3001](http://localhost:3001) |

---

## 十、E2E 自动化测试（Playwright）

### 10.1 用户前台 E2E 测试（新增 Playwright 套件）

```bash
cd front-user

# 首次运行安装 Playwright 浏览器
npx playwright install chromium

# 运行全部 37 个测试用例
npm run test:e2e

# 查看测试报告
npm run test:e2e:report

# 调试模式（带 UI）
npm run test:e2e:ui
```

测试覆盖（11 个模块共 37 个用例）：登录（含错误密码验证）、仪表盘、产品中心、实时行情、我的自选、交易委托、财经资讯、消息中心、个人中心、退出登录、导航菜单。

测试账号：`zhangwei` / `123456`（sys_user 表）

### 10.2 全链路 E2E 测试（旧版脚本）

```bash
# 确保在项目根目录
cd finance-mid-platform

# 安装依赖（首次）
npm install

# 运行全链路测试
node e2e-test.mjs
```

测试覆盖 35 项，包括：基础设施检查（Nacos/网关/前端）、API 登录、页面加载、菜单导航（12 页面）、API 请求（10 接口）、前端代理、JS 错误检查。报告输出到 `e2e-test-report.md`。

测试账号：`admin` / `admin123`（ums_admin 表）

---

## 十一、端口对照表

| 模块 | 端口 | context-path | Nacos 服务名 | 说明 |
|------|:----:|:-----------:|-------------|------|
| **中间件** | | | | |
| Nacos | 8848 | - | - | 注册中心/配置中心 |
| MySQL | 3306 | - | - | 数据库 |
| Redis | 6379 | - | - | 缓存 |
| RabbitMQ | 5672 / 15672 | - | - | 消息队列 / 管理控制台 |
| Elasticsearch | 9200 / 9300 | - | - | 搜索引擎 |
| **后端服务** | | | | |
| finance-gateway | **8080** | - | finance-gateway | 网关（统一入口） |
| finance-system | **8082** | /system | finance-system | 后台权限管理 |
| finance-user | **8083** | /user | finance-user | 前端用户管理 |
| finance-product | **8084** | /product | finance-product | 产品 + 行情 |
| finance-trade | **8085** | /trade | finance-trade | 交易委托 |
| finance-account | **8086** | /account | finance-account | 用户自选 |
| finance-message | **8087** | /message | finance-message | 资讯 + 消息 |
| finance-search | **8089** | - | finance-search | ES 搜索 |
| **前端** | | | | |
| 管理员后台 | **3000** | - | - | front/（Vite 开发服务器） |
| 用户前台 | **3001** | - | - | front-user/（Vite 开发服务器） |

---

## 十二、测试账号说明

| 身份 | 用户名 | 密码 | 所属表 | 登录端 | 说明 |
|------|--------|------|--------|--------|------|
| 管理员 | `admin` | `admin123` | ums_admin | 管理员后台 (port 3000) | 拥有后台全部权限 |
| 前台用户 | `zhangwei` | `123456` | sys_user | 用户前台 (port 3001) | E2E 测试默认用户 |

---

## 十三、常见问题排查

### 端口占用

```bash
# 查看占用端口的进程
netstat -ano | findstr ":8080"

# 强制终止进程（Windows）
taskkill /PID <PID> /F
```

### 后端启动失败

1. **Nacos 连接失败** — 检查 Nacos 容器是否运行：`docker ps | findstr nacos`
2. **JWT 配置缺失** — 确认 `finance-shared.yaml` 已发布到 Nacos
3. **数据库连接失败** — 确认 `DB_PASSWORD` 环境变量已设置且密码正确
4. **数据库表不存在** — 确认已执行 `init.sql`

### 前端启动失败

1. **依赖安装失败** — 删除 `node_modules` 重新安装：
   ```bash
   rm -rf node_modules && npm install
   ```
2. **端口被占用** — 修改 `vite.config.ts` 中的 `server.port`
3. **代理 502** — 确认网关已启动并可访问 `http://localhost:8080`

### 跨域问题

网关已在 `finance-gateway` 中全局配置 CORS，允许 `localhost:3000`、`localhost:3001` 和 `localhost:8080`。如果遇到跨域错误，检查网关是否正常运行。

### E2E 测试失败

1. **浏览器未安装** — 执行 `npx playwright install chromium`
2. **后端未启动** — 确保所有 8 个后端服务已在运行
3. **测试用户不存在** — 检查 `sys_user` 表中是否有 `zhangwei` 且密码为 BCrypt 加密的 `123456`

---

## 十四、启动顺序依赖图

```
Docker 容器（Nacos / MySQL / Redis / RabbitMQ / ES）
        │
        ▼
finance-common（Maven 依赖，必须先 mvn install）
        │
        ▼
finance-gateway（最先启动，依赖 Nacos）
        │
        ▼
finance-system（第二启动，提供登录鉴权 + 权限拦截）
        │
        ▼
finance-user │ finance-product │ finance-account
finance-trade │ finance-message │ finance-search
（无先后依赖，可并行启动）
        │
        ├────────────── ──────────────┐
        ▼                             ▼
  管理员后台 front/             用户前台 front-user/
  (port 3000)                   (port 3001)
  npm run dev                   npm run dev
```

> **注意**：每次修改 `finance-common` 中的代码后，必须重新执行 `mvn clean install -pl finance-common -DskipTests`，再重新打包依赖它的业务模块。
