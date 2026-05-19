# 金融微服务平台 — 启动指南

## 项目简介
金融微服务平台（Wealth Service Platform）是一个面向金融业务的双端架构系统，包含：

- **管理员后台**（front/）：后台管理系统，面向运营人员，管理用户、产品、权限等
- **用户前台**（front-user/）：用户端门户，面向普通用户，提供行情查看、交易委托、自选管理等

后端采用 Spring Cloud Alibaba 微服务架构，统一通过 Nacos 注册中心 + Spring Cloud Gateway 网关对外提供服务。

---

## 一、技术栈

| 层面 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot / Spring Cloud / Alibaba | 3.3.5 / 2023.0.3 / 2023.0.1.2 |
| ORM | MyBatis-Plus | 3.5.7 |
| 注册中心/配置中心 | Nacos | 2.3.2 |
| 网关 | Spring Cloud Gateway | 4.1.5 |
| 数据库 | MySQL | 8.0.37 |
| 缓存 | Redis | 5.0.14.1 |
| 消息队列 | RabbitMQ | 3.10.20 |
| 搜索引擎 | Elasticsearch | 8.8.2 |
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

推荐使用 Docker Compose 一键启动所有中间件（项目根目录下已提供 `docker-compose.yml`）：

```bash
# 一键启动全部 11 个中间件容器
docker compose up -d nacos mysql redis rabbitmq es nginx zipkin prometheus grafana sentinel-dashboard seata-server

# 验证运行状态
docker ps --format "table {{.Names}}\t{{.Status}}"
```

### 3.1 各中间件容器详情

当前项目依赖以下 11 个基础设施中间件：

| 服务 | 镜像 | 端口 | 说明 |
|------|------|:----:|------|
| Nacos | nacos/nacos-server:v2.3.2 | 8848 | 注册中心 + 配置中心 |
| MySQL | mysql:8.0.37 | 3306 | 数据库 |
| Redis | redis:5.0.14.1 | 6379 | 缓存 |
| RabbitMQ | rabbitmq:3.10-management | 5672 / 15672 | 消息队列 / 管理控制台 |
| Elasticsearch | elasticsearch:8.8.2 | 9200 / 9300 | 搜索引擎 |
| Sentinel | bladex/sentinel-dashboard:latest | 8858 | 熔断限流控制台 |
| Seata | seataio/seata-server:2.0.0 | 7091 / 8091 | 分布式事务 |
| Nginx | nginx:latest | 80 | 反向代理 |
| Zipkin | openzipkin/zipkin:latest | 9411 | 链路追踪 |
| Prometheus | prom/prometheus:latest | 9090 | 监控指标采集 |
| Grafana | grafana/grafana:latest | 3001 | 监控可视化仪表盘 |

> 若需单独启动某个容器，Docker run 命令示例可参考项目 `docker-compose.yml` 中的配置。

### 3.2 Nacos 配置中心设置

访问 Nacos 控制台 [http://localhost:8848/nacos](http://localhost:8848/nacos)，在 `DEFAULT_GROUP` 下创建共享配置：
- **Data ID**：`wealth-shared.yaml`
- **配置格式**：YAML

> Nacos 已启用认证（`NACOS_AUTH_ENABLE=true`），默认凭据：`nacos/nacos`。

配置内容、配置项说明、变更历史及覆盖优先级详见 [Nacos 配置参考](nacos-config-reference.md)。

---

## 四、数据库初始化
```bash
# 创建数据库（密码从 .env 中获取）
mysql -u root -p"${MYSQL_ROOT_PASSWORD}" -e "CREATE DATABASE IF NOT EXISTS Wealth DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 导入建表语句 + 测试数据
mysql -u root -p"${MYSQL_ROOT_PASSWORD}" Wealth < wealth-common/src/main/resources/sql/init.sql
```

数据库 `Wealth` 包含 12 张表，覆盖全业务场景。

---

## 五、编译项目
**编译顺序**：必须先编译 `wealth-common`，再编译其他模块。
```bash
# 1. 编译公共模块并安装到本地仓库（修改 common 后必须重新执行此步）
mvn clean install -pl wealth-common -DskipTests

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

数据库密码通过 `.env` 文件（项目根目录）统一管理，Docker Compose 自动读取。若需在宿主机直接运行服务，需手动导出环境变量：

```bash
# Windows PowerShell
$env:DB_PASSWORD="your_password"

# Windows CMD
set DB_PASSWORD=your_password

# Linux / Mac
export DB_PASSWORD=your_password
```

> `DB_PASSWORD` 默认值见 `.env` 文件中的 `MYSQL_ROOT_PASSWORD`。

### 6.1 网关（最先启动，依赖 Nacos）
```bash
java -jar wealth-gateway/target/wealth-gateway-1.0.0.jar > gateway.log 2>&1 &
```

- 端口：**8080**
- Nacos 服务名：wealth-gateway
- 类型：Spring Cloud Gateway（WebFlux）
### 6.2 系统服务（提供登录鉴权和 RBAC 权限）
```bash
DB_PASSWORD=your_password java -jar wealth-system/target/wealth-system-1.0.0.jar > system.log 2>&1 &
```

- 端口：**8082**，context-path：`/system`
- Nacos 服务名：wealth-system
- 功能：管理员 CRUD、角色管理、资源管理、JWT 登录、权限拦截
### 6.3 业务服务（无先后依赖，可并行启动）
```bash
# 用户服务（前端用户管理）
DB_PASSWORD=your_password java -jar wealth-user/target/wealth-user-1.0.0.jar > user.log 2>&1 &

# 产品服务（产品 + 行情）
DB_PASSWORD=your_password java -jar wealth-product/target/wealth-product-1.0.0.jar > product.log 2>&1 &

# 账户服务（用户自选）
DB_PASSWORD=your_password java -jar wealth-account/target/wealth-account-1.0.0.jar > account.log 2>&1 &

# 交易服务（委托交易）
DB_PASSWORD=your_password java -jar wealth-trade/target/wealth-trade-1.0.0.jar > trade.log 2>&1 &

# 消息服务（资讯 + 站内消息）
DB_PASSWORD=your_password java -jar wealth-message/target/wealth-message-1.0.0.jar > message.log 2>&1 &

# 搜索服务（ES 产品搜索，无数据库依赖）
DB_PASSWORD=your_password java -jar wealth-search/target/wealth-search-1.0.0.jar > search.log 2>&1 &
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
  "http://localhost:8080/product/WeaProduct/page?pageNum=1&pageSize=10"

# 行情数据
curl -s --noproxy "*" -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/product/WeaMarketData/list?productCode=GOLD001"

# 交易订单分页
curl -s --noproxy "*" -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/trade/WeaTradeOrder/page?pageNum=1&pageSize=10"

# 新闻分页
curl -s --noproxy "*" -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/message/WeaNews/page?pageNum=1&pageSize=10"

# 用户自选列表
curl -s --noproxy "*" -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/account/WeaUserFavorite/list?userId=1"

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
### 10.1 用户前台 E2E 测试（Playwright 套件）
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
cd wealth-service-platform

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
| Nacos | 8848 / 9848 | - | - | 注册中心/配置中心 |
| MySQL | 3306 | - | - | 数据库 |
| Redis | 6379 | - | - | 缓存 |
| RabbitMQ | 5672 / 15672 | - | - | 消息队列 / 管理控制台 |
| Elasticsearch | 9200 / 9300 | - | - | 搜索引擎 |
| Sentinel | 8858 | - | - | 熔断限流控制台 |
| Seata | 7091 / 8091 | - | - | 分布式事务协调器 |
| Nginx | 80 | - | - | 反向代理 |
| Zipkin | 9411 | - | - | 链路追踪 UI |
| Prometheus | 9090 | - | - | 监控指标存储 |
| Grafana | 3001 | - | - | 监控仪表盘 |
| **后端服务** | | | | |
| wealth-gateway | **8080** | - | wealth-gateway | 网关（统一入口） |
| wealth-system | **8082** | /system | wealth-system | 后台权限管理 |
| wealth-user | **8083** | /user | wealth-user | 前端用户管理 |
| wealth-product | **8084** | /product | wealth-product | 产品 + 行情 |
| wealth-trade | **8085** | /trade | wealth-trade | 交易委托 |
| wealth-account | **8086** | /account | wealth-account | 用户自选 |
| wealth-message | **8087** | /message | wealth-message | 资讯 + 消息 |
| wealth-search | **8089** | - | wealth-search | ES 搜索 |
| **前端** | | | | |
| 管理员后台 | **3000** | - | - | front/（Vite 开发服务器）|
| 用户前台 | **3001** | - | - | front-user/（Vite 开发服务器）|

---

## 十二、测试账号说明
| 身份 | 用户名 | 密码 | 所属表 | 登录端 | 说明 |
|------|--------|------|--------|--------|------|
| 管理员 | `admin` | `admin123` | ums_admin | 管理员后台(port 3000) | 拥有后台全部权限 |
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

1. **Nacos 连接失败** → 检查 Nacos 容器是否运行：`docker ps | findstr nacos`
2. **JWT 配置缺失** → 确认 `wealth-shared.yaml` 已发布到 Nacos
3. **数据库连接失败** → 确认 `DB_PASSWORD` 环境变量已设置且密码正确
4. **数据库表不存在** → 确认已执行 `init.sql`
5. **链路追踪不生效** → 检查 Zipkin 容器 `docker ps | findstr zipkin`，确认 Nacos 配置中 `management.zipkin.tracing.endpoint` 指向正确的 Zipkin 地址
6. **监控指标抓取不到** → 访问 `http://localhost:{port}/actuator/prometheus` 验证端点是否返回数据，检查 Prometheus 容器 `docker ps | findstr prometheus`

### 前端启动失败

1. **依赖安装失败** → 删除 `node_modules` 重新安装：  
   ```bash
   rm -rf node_modules && npm install
   ```
2. **端口被占用** → 修改 `vite.config.ts` 中的 `server.port`
3. **代理 502** → 确认网关已启动并可访问 `http://localhost:8080`

### 跨域问题

网关已在 `wealth-gateway` 中全局配置 CORS，允许 `localhost:3000`、`localhost:3001` 和 `localhost:8080`。如果遇到跨域错误，检查网关是否正常运行。
### E2E 测试失败

1. **浏览器未安装** → 执行 `npx playwright install chromium`
2. **后端未启动** → 确保所有 8 个后端服务已在运行
3. **测试用户不存在** → 检查 `sys_user` 表中是否有 `zhangwei` 且密码为 BCrypt 加密的 `123456`

---

## 十四、启动顺序依赖图

```
基础设施中间件容器（Docker Compose 一键启动）
  nacos / mysql / redis / rabbitmq / es / nginx
  zipkin / prometheus / grafana / sentinel / seata
         ↓
wealth-common（Maven 依赖，必须先 mvn install）
         ↓
wealth-gateway（最先启动，依赖 Nacos）
         ↓
wealth-system（第二启动，提供登录鉴权 + 权限拦截）
         ↓
wealth-user ─ wealth-product ─ wealth-account
wealth-trade ─ wealth-message ─ wealth-search
（无先后依赖，可并行启动）
         ↓
         ├──────────────────────┤
         ↓                      ↓
管理员后台 front/             用户前台 front-user/
  (port 3000)                   (port 3001)
  npm run dev                   npm run dev

可观测性：
  Zipkin(9411)  ← 各服务上报链路 Span
  Prometheus(9090)  ← 各服务 /actuator/prometheus 抓取指标
  Grafana(3001)  ← Prometheus 数据源可视化
```

> **注意**：每次修改 `wealth-common` 中的代码后，必须重新执行 `mvn clean install -pl wealth-common -DskipTests`，再重新打包依赖它的业务模块。
