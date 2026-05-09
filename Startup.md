# 金融微服务中台 — 项目启动流程

## 一、环境要求

| 组件 | 版本 | 检查命令 |
|------|------|---------|
| JDK | 21.0.3+ | `java -version` |
| Maven | 3.9.9+ | `mvn -version` |
| Docker | 24.0+ | `docker --version` |
| Node.js | 18+ | `node -v` |
| MySQL | 8.0.37 | `mysql --version` |

## 二、启动基础设施（Docker）

在项目根目录执行以下命令启动所有中间件容器：

```bash
# 启动 Nacos（注册中心/配置中心）
docker run -d --name nacos -p 8848:8848 -p 9848:9848 -e MODE=standalone nacos/nacos-server:v2.3.0

# 启动 MySQL
docker run -d --name mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=123456 mysql:8.0.37

# 启动 Redis
docker run -d --name redis -p 6379:6379 redis:5.0.14.1

# 启动 RabbitMQ
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3.10.20-management

# 启动 ElasticSearch
docker run -d --name es -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" elasticsearch:8.11.0
```

> 首次启动 Nacos 后，需在 Nacos 控制台（http://localhost:8848/nacos）创建共享配置 `finance-shared.yaml`：
> ```yaml
> jwt:
>   secret: finance-micro-service-20260501-very-safe-secret-key-123456789
>   expire: 604800000
> ```

验证所有容器运行正常：
```bash
docker ps --format "table {{.Names}}\t{{.Status}}"
```

## 三、初始化数据库

```bash
# 创建数据库
mysql -u root -p123456 -e "CREATE DATABASE IF NOT EXISTS finance DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 导入建表脚本（如有）
mysql -u root -p123456 finance < sql/init.sql
```

默认预置管理员账号：
- 用户名：`admin` / 密码：`admin123`

## 四、编译项目

编译顺序：公共模块 → 业务模块。公共模块有变更时必须先 install。

```bash
# 1. 编译公共模块并安装到本地仓库
mvn clean install -pl finance-common -DskipTests

# 2. 编译所有模块
mvn clean compile

# 3. 打包所有模块
mvn clean package -DskipTests

# 4. （可选）安装所有模块到本地仓库
mvn clean install -DskipTests
```

各模块 JAR 包路径：`{模块名}/target/{模块名}-1.0.0.jar`

## 五、启动后端服务（按顺序）

**必须按以下顺序启动**，后启动的服务依赖 Nacos 中已注册的前置服务。每个服务通过数据库环境变量 `DB_PASSWORD` 连接 MySQL。

```bash
# 数据库密码统一通过环境变量传入
export DB_PASSWORD=123456
```

### 5.1 启动网关（优先级最高）
```bash
java -jar finance-gateway/target/finance-gateway-1.0.0.jar > gateway.log 2>&1 &
```
- 端口：8080
- Nacos 注册：finance-gateway

### 5.2 启动系统服务（提供登录鉴权）
```bash
DB_PASSWORD=123456 java -jar finance-system/target/finance-system-1.0.0.jar > system.log 2>&1 &
```
- 端口：8082，context-path：`/system`
- Nacos 注册：finance-system
- 提供接口：管理员 CRUD、角色管理、资源管理、JWT 登录

### 5.3 启动业务服务（无先后依赖，可并行）
```bash
# 用户服务
DB_PASSWORD=123456 java -jar finance-user/target/finance-user-1.0.0.jar > user.log 2>&1 &

# 产品服务
DB_PASSWORD=123456 java -jar finance-product/target/finance-product-1.0.0.jar > product.log 2>&1 &

# 账户服务（自选管理）
DB_PASSWORD=123456 java -jar finance-account/target/finance-account-1.0.0.jar > account.log 2>&1 &

# 交易服务
DB_PASSWORD=123456 java -jar finance-trade/target/finance-trade-1.0.0.jar > trade.log 2>&1 &

# 消息服务
DB_PASSWORD=123456 java -jar finance-message/target/finance-message-1.0.0.jar > message.log 2>&1 &

# 搜索服务
DB_PASSWORD=123456 java -jar finance-search/target/finance-search-1.0.0.jar > search.log 2>&1 &
```

### 5.4 验证后端服务启动

```bash
# 检查端口监听
netstat -ano | findstr ':8080 :8082 :8083 :8084 :8085 :8086 :8087 :8089'

# 检查日志中的启动标识
grep "Started" gateway.log system.log user.log product.log account.log trade.log message.log search.log
```

预期输出（每个服务一行）：
```
Started FinanceGatewayApplication in ...
Started FinanceSystemApplication in ...
Started FinanceUserApplication in ...
...（依此类推）
```

## 六、启动前端

```bash
cd front
npm install     # 首次运行需要安装依赖
npx vite --port 3000 > ../frontend.log 2>&1 &
```

- 端口：3000
- 代理配置：Vite 将 `/api` 请求代理到 `http://localhost:8080`（网关）

验证前端正常运行：
```bash
curl -s http://localhost:3000 | head -5
# 输出应包含 <div id="app"></div>
```

## 七、验证全链路连通

### 7.1 登录测试
```bash
curl -s --noproxy "*" -X POST "http://localhost:8080/system/umsAdmin/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```
预期返回 200 + JWT Token。

### 7.2 API 连通测试（携带 Token）
```bash
TOKEN="<上一步返回的JWT Token>"

# 管理员分页
curl -s --noproxy "*" -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/system/umsAdmin/page?pageNum=1&pageSize=10"

# 用户分页
curl -s --noproxy "*" -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/user/page?pageNum=1&pageSize=10"

# 产品分页
curl -s --noproxy "*" -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/product/finProduct/page?pageNum=1&pageSize=10"
```

### 7.3 E2E 自动化测试
```bash
# 确保已在项目根目录安装 playwright
npm install playwright

# 运行全链路测试脚本（生成测试报告）
node e2e-test.mjs
```

测试覆盖 35 项，包括：页面加载、登录、菜单导航（12 页面）、API 请求（10 接口）、前端代理、JS 错误检查。报告输出到 `e2e-test-report.md`。

## 八、端口速查表

| 模块 | 端口 | context-path | 服务名 |
|------|:----:|:-----------:|--------|
| finance-gateway | 8080 | - | finance-gateway |
| finance-system | 8082 | /system | finance-system |
| finance-user | 8083 | /user | finance-user |
| finance-product | 8084 | /product | finance-product |
| finance-trade | 8085 | /trade | finance-trade |
| finance-account | 8086 | /account | finance-account |
| finance-message | 8087 | /message | finance-message |
| finance-search | 8089 | - | finance-search |
| Frontend (Vite) | 3000 | - | - |

## 九、常用操作命令

```bash
# 查看运行中的 Java 进程
jps -l

# 停止某个服务（通过端口查找 PID）
netstat -ano | findstr ":8082"
taskkill /PID <PID> /F

# 查看服务实时日志
tail -f system.log

# 重新编译并重启单个模块（以 system 为例）
# 1. 停止旧进程 → 2. 重新打包 → 3. 启动
taskkill /PID <PID> /F
mvn clean package -pl finance-system -am -DskipTests
DB_PASSWORD=123456 java -jar finance-system/target/finance-system-1.0.0.jar > system.log 2>&1 &
```

## 十、启动顺序依赖图

```
Docker 容器（Nacos / MySQL / Redis / RabbitMQ / ES）
        │
        ▼
   finance-common（Maven 依赖，需先 install）
        │
        ▼
   finance-gateway（最先启动，依赖 Nacos）
        │
        ▼
   finance-system（第二启动，提供登录鉴权）
        │
        ▼
   finance-user / finance-product / finance-account
   finance-trade / finance-message / finance-search
   （无先后依赖，可并行启动）
        │
        ▼
   Frontend（最后启动，依赖网关 8080）
```

> **注意**：每次修改 `finance-common` 中的代码后，必须重新执行 `mvn clean install -pl finance-common -DskipTests`，再重新打包依赖它的业务模块。
