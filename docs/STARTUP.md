# 理财服务平台 — 启动指南

## 项目简介

理财服务平台（Wealth Service Platform），聚合架构，包含：

- **管理员后台**（front/）：后台管理系统，面向运营人员，管理用户、产品、权限等
- **用户前台**（front-user/）：用户端门户，面向普通用户，提供行情查看、交易委托、自选管理等
- **统一登录门户**（front-landing/）：前端统一入口，承载登录页，按角色自动跳转对应 SPA

后端采用 Gateway + 业务服务两层架构，统一通过 Nginx / Gateway 对外提供服务。

---

## 一、技术栈

| 层面 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot / Spring Cloud / Alibaba | 3.3.13 / 2023.0.6 / 2023.0.3.4 |
| ORM | MyBatis-Plus | 3.5.9 |
| 网关 | Spring Cloud Gateway | - |
| 数据库 | MySQL | 8.0 |
| 缓存 | Redis | 5.0+ |
| 搜索引擎 | Elasticsearch | 8.8.2（可选，降级 MySQL LIKE）|
| 前端（双端） | Vue 3 + Vite + Element Plus + Pinia + TypeScript | 3.5.13 / 6.3.1 / 2.9.7 / 2.3.1 / 5.7 |
| E2E 测试 | Playwright | 1.59+ |

---

## 二、前置环境准备

| 组件 | 版本要求 | 检查命令 |
|------|---------|---------|
| JDK | 21+ | `java -version` |
| Maven | 3.9+ | `mvn -version` |
| Node.js | 18+ | `node -v` |
| Docker | 24.0+ | `docker --version` |
| MySQL 客户端 | 8.0+ | `mysql --version` |

---

## 三、中间件启动（Docker）

```bash
# 启动必需中间件
docker compose up -d mysql redis nginx

# 可选：Elasticsearch（搜索服务需要）
docker compose up -d elasticsearch

# 可选：链路追踪 + 监控
docker compose up -d zipkin prometheus grafana
```

### 中间件容器详情

| 服务 | 端口 | 必需 | 说明 |
|------|:----:|:----:|------|
| MySQL | 3306 | 是 | 数据库 |
| Redis | 6379 | 是 | 缓存 |
| Nginx | 80 | 是 | 反向代理 |
| Elasticsearch | 9200 / 9300 | 否 | 搜索引擎（降级 MySQL LIKE）|
| Zipkin | 9411 | 否 | 链路追踪 |
| Prometheus | 9090 | 否 | 监控指标采集 |
| Grafana | 3001 | 否 | 监控可视化仪表盘 |

> Nacos、RabbitMQ、Seata、Sentinel 已停用，docker-compose.yml 中已移除对应服务块。

---

## 四、环境变量配置

项目使用 `.env` 文件管理环境变量，各模块独立配置：

```bash
# 确保以下 .env 文件已创建并填入正确密码
# 根目录 .env（MySQL/Redis/ES 密码）
# wealth-gateway/.env（JWT 密钥、路由等）
# wealth-service/.env（JWT 密钥、数据源、Redis、ES 等）
```

模板参考根目录 `.env.example`。

---

## 五、数据库初始化

```bash
# 创建数据库
mysql -u root -p"${MYSQL_ROOT_PASSWORD}" -e "CREATE DATABASE IF NOT EXISTS wealth DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 导入建表语句 + 测试数据
mysql -u root -p"${MYSQL_ROOT_PASSWORD}" wealth < wealth-common/src/main/resources/sql/init.sql
```

---

## 六、编译项目

**编译顺序**：必须先编译 `wealth-common`，再编译其他模块。

```bash
# 1. 编译公共模块并安装到本地仓库（修改 common 后必须重新执行）
mvn clean install -pl wealth-common -DskipTests

# 2. 全量编译
mvn clean compile

# 3. 打包
mvn clean package -DskipTests
```

JAR 包路径：`wealth-service/target/wealth-service-1.0.0.jar`

---

## 七、后端服务启动

**启动顺序**：gateway → wealth-service

### 7.1 启动网关

```bash
mvn spring-boot:run -pl wealth-gateway
# 或
java -jar wealth-gateway/target/wealth-gateway-1.0.0.jar
```

- 端口：**8080**

### 7.2 启动业务服务

```bash
mvn spring-boot:run -pl wealth-service
# 或
java -jar wealth-service/target/wealth-service-1.0.0.jar
```

- 端口：**8081**
- 所有业务域（system/user/product/trade/message/search）均在此服务

### 7.3 验证

```bash
# 检查端口监听
netstat -ano | findstr ':8080 :8081'

# 检查启动日志（应看到 2 行 Started xxxApplication）
```

---

## 八、前端启动

### 8.1 管理员后台

```bash
cd front
npm install
npm run dev
```

- 端口：**3000**
- Vite 代理：`/api` → `http://localhost:8080`（网关）
- 登录账号：`admin` / `admin123`

### 8.2 用户前台

```bash
cd front-user
npm install
npm run dev
```

- 端口：**3001**
- Vite 代理：`/api` → `http://localhost:8080`（网关）
- 登录账号：`zhangwei` / `123456`

### 8.3 统一登录门户

```bash
cd front-landing
npm install
npm run dev
```

- 端口：**3002**
- Vite 代理：`/api/v1` → `http://localhost:8081`（直连 wealth-service，不走网关）
- 统一登录页包含角色表情动画、眼球追踪交互效果

---

## 九、统一登录流程

```
用户访问 http://localhost:3002/login
  → 输入用户名密码
  → POST /api/v1/user/identify-login（后端自动识别用户类型）
  → 管理员 → 跳转 http://localhost:3000/admin/?token=xxx
  → 普通用户 → 跳转 http://localhost:3001/user/?token=xxx
  → 目标 SPA 登录页自动读取 token 完成登录
  → URL 中 token 被清除，进入首页
```

> **注意**：front-landing 不持久化 token（纯内存存储），刷新页面即清零。退出登录后自动跳转回 `http://localhost:3002/login`。

### 9.1 登录接口

```bash
# 管理员登录
curl -s -X POST "http://localhost:8080/system/umsAdmin/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 用户登录
curl -s -X POST "http://localhost:8080/user/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"zhangwei","password":"123456"}'
```

预期返回 `200` + JWT Token。

### 9.2 业务接口测试

```bash
TOKEN="eyJhbGciOiJIUzUxMiJ9..."

# 管理员分页
curl -s -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/system/umsAdmin/page?pageNum=1&pageSize=10"

# 产品分页
curl -s -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/product/WeaProduct/page?pageNum=1&pageSize=10"

# 行情数据
curl -s -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/product/WeaMarketData/list?productCode=GOLD001"

# 交易订单分页
curl -s -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/trade/WeaTradeOrder/page?pageNum=1&pageSize=10"

# 新闻分页
curl -s -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/message/WeaNews/page?pageNum=1&pageSize=10"

# 搜索
curl -s -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/search/product/search?keyword=黄金&page=1&size=10"
```

### 9.3 浏览器访问

| 应用 | URL |
|------|-----|
| 统一登录门户 | http://localhost:3002 |
| 管理员后台 | http://localhost:3000/admin/ |
| 用户前台 | http://localhost:3001/user/ |
| Knife4j 文档 | http://localhost:8080/doc.html |

---

## 十、E2E 测试

### 10.1 用户前台 E2E

```bash
cd front-user
npx playwright install chromium
npm run test:e2e
```

### 10.2 API E2E

```bash
cd api-e2e
npm install
npx playwright test
```

---

## 十一、端口对照表

| 模块 | 端口 | 说明 |
|------|:----:|------|
| **中间件** | | |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |
| Nginx | 80 | 反向代理 |
| Elasticsearch | 9200, 9300 | 搜索引擎（可选）|
| **后端** | | |
| wealth-gateway | **8080** | 网关（统一入口）|
| wealth-service | **8081** | 业务聚合服务 |
| **前端** | | |
| 管理员后台 | **3000** | front/ |
| 用户前台 | **3001** | front-user/ |
| 统一登录门户 | **3002** | front-landing/ |

---

## 十二、测试账号

| 身份 | 用户名 | 密码 | 所属表 | 统一登录入口 |
|------|--------|------|--------|------------|
| 管理员 | admin | admin123 | ums_admin | http://localhost:3002 |
| 前台用户 | zhangwei | 123456 | sys_user | http://localhost:3002 |

---

## 十三、常见问题排查

### 端口占用
```bash
netstat -ano | findstr ":8080"
taskkill /PID <PID> /F
```

### 后端启动失败

1. **数据库连接失败** → 检查 MySQL 容器 + `.env` 中 `MYSQL_ROOT_PASSWORD` 是否正确
2. **Redis 连接失败** → 检查 Redis 容器 + `.env` 中 `REDIS_HOST`/`REDIS_PASSWORD`
3. **数据库表不存在** → 确认已执行 `init.sql`
4. **JWT 密钥错误** → 确认 `wealth-gateway/.env` 和 `wealth-service/.env` 中的 `JWT_SECRET`

### 前端启动失败

1. **依赖安装失败** → 删除 `node_modules` 重新 `npm install`
2. **代理 502** → 确认网关已启动（`:8080`）

### 跨域问题

网关已全局配置 CORS，允许 `localhost:3000`、`localhost:3001`、`localhost:3002`、`localhost:5173`。front-landing 的 Vite 代理直接对接 wealth-service（`:8081`）不走网关，如需直连测试也需要对应 CORS 配置。

---

## 十四、启动顺序

```
基础设施中间件（Docker Compose）
  mysql / redis / nginx
  （可选：elasticsearch / zipkin / prometheus / grafana）
         ↓
wealth-common（Maven 依赖，必须先 mvn install）
         ↓
wealth-gateway（先启动）
         ↓
wealth-service（后启动，所有业务域合一）
         ↓
  ┌──────┴──────┐
  ↓              ↓
front/         front-user/
(3000)         (3001)

> **说明**：`front-landing`（端口 3002）为统一登录门户，承载登录页，登录后自动跳转至对应 SPA。开发时可按需独立启动，生产环境通过 Nginx 直接 serve 静态文件。

> 每次修改 `wealth-common` 后，必须重新执行 `mvn clean install -pl wealth-common -DskipTests`。
