# 金融微服务中台 (Finance Mid Platform)

> 基于 Spring Boot 3.x + Spring Cloud Alibaba 的金融级微服务中台架构项目，覆盖用户、账户、产品、交易、消息等核心业务领域，提供高可用、高扩展的企业级金融解决方案。

---

## 目录

- [项目简介](#项目简介)
- [技术栈清单](#技术栈清单)
- [模块架构](#模块架构)
- [核心功能说明](#核心功能说明)
- [环境搭建](#环境搭建)
- [项目启动步骤](#项目启动步骤)
- [接口文档](#接口文档)
- [项目规范](#项目规范)
- [变更记录](#变更记录)
- [后续开发建议](#后续开发建议)

---

## 项目简介

### 项目定位

面向金融业务场景的微服务中台，将传统单体架构拆分为多个独立部署的微服务模块，实现业务解耦、独立迭代、弹性伸缩。

### 业务场景

- **证券/基金行情展示**：实时行情数据接入与展示
- **金融产品管理**：产品上架、分类、查询
- **用户自选管理**：用户自选产品关注
- **交易委托**：交易订单发起与管理
- **资讯消息**：财经资讯推送、站内消息通知
- **后台权限管理**：统一后台管理员、角色、资源权限控制

### 核心功能模块

| 领域 | 模块 | 核心能力 |
|------|------|----------|
| 用户域 | finance-user | 系统用户注册/登录、个人信息管理 |
| 产品域 | finance-product | 金融产品管理、行情数据 |
| 账户域 | finance-account | 用户自选产品管理 |
| 交易域 | finance-trade | 交易委托单发起、撤销、查询 |
| 消息域 | finance-message | 财经资讯、站内消息 |
| 搜索域 | finance-search | 基于 ES 的产品全文检索 |
| 系统域 | finance-system | 管理员、角色、资源、权限拦截 |
| 网关域 | finance-gateway | 统一路由、CORS |
| 公共域 | finance-common | 工具类、Feign 接口、全局异常处理 |

---

## 技术栈清单

### 后端核心

| 技术 | 版本 | 说明 |
|------|------|------|
| JDK | 21.0.3 | 长期支持版本 |
| Maven | 3.9.9 | 项目构建管理 |
| Spring Boot | 3.3.5 | 应用基础框架 |
| Spring Cloud | 2023.0.3 | 微服务组件 |
| Spring Cloud Alibaba | 2023.0.1.2 | Alibaba 微服务生态 |
| MyBatis-Plus | **3.5.7** | ORM 框架（最后一个包含 PaginationInnerInterceptor 的稳定版本） |
| MySQL | 8.0.37 | 关系型数据库 |
| Redis | 5.0.14.1 | 缓存 |
| RabbitMQ | 3.10.20 | 消息队列 |
| Elasticsearch | 8.11.0 | 搜索引擎 |

### 核心依赖

| 组件 | 用途 |
|------|------|
| Nacos (2.x) | 服务注册发现 + 配置中心 |
| Spring Cloud Gateway | 网关路由转发 |
| OpenFeign | 服务间声明式调用 |
| JWT (jjwt 0.11.5) | 无状态认证 |
| Knife4j (4.4.0) | API 文档 |
| BCrypt (spring-security-crypto) | 密码加密 |
| Lombok | 代码简化 |

### 中间件版本对应

| 中间件 | 端口 | 部署方式 |
|--------|------|----------|
| Nacos Server | 8848 | Docker / 独立部署 |
| MySQL | 3306 | Docker / 本地安装 |
| Redis | 6379 | Docker |
| RabbitMQ | 5672 / 15672 | Docker |
| Elasticsearch | 9200 / 9300 | Docker |

---

## 模块架构

### 模块依赖关系

```
finance-mid-platform (pom)
├── finance-common      ← 所有模块依赖（公共工具、Feign 接口、统一返回、异常处理、全局配置）
├── finance-gateway     ← 网关路由（依赖 common）
├── finance-system      ← 后台权限（依赖 common，通过 Feign 调用 account/product）
├── finance-user        ← 用户服务（依赖 common）
├── finance-account     ← 自选服务（依赖 common）
├── finance-product     ← 产品服务（依赖 common）
├── finance-trade       ← 交易服务（依赖 common）
├── finance-message     ← 消息服务（依赖 common）
└── finance-search      ← 搜索服务（依赖 common）
```

> **注意**：修改 `finance-common` 后必须先执行 `mvn clean install -pl finance-common -DskipTests`，其他模块才能引用最新版本。

### 端口与上下文路径

| 模块 | 端口 | context-path | 服务名 |
|------|------|-------------|--------|
| finance-gateway | 8080 | - | finance-gateway |
| finance-system | 8082 | /system | finance-system |
| finance-product | 8084 | /product | finance-product |
| finance-trade | 8085 | /trade | finance-trade |
| finance-account | 8086 | /account | finance-account |
| finance-message | 8087 | /message | finance-message |
| finance-user | 8088 | /user | finance-user |
| finance-search | 8089 | - | finance-search |

### 包路径规范

| 模块 | 基础包 |
|------|--------|
| finance-common | `com.finance.common` |
| finance-gateway | `com.finance.gateway` |
| finance-user | `com.finance.user` |
| 其余业务模块 | `com.finance.platform.{模块名}` |

### 各模块包结构

```
com.finance.platform.{模块名}
├── controller    # RESTful 接口层
├── service       # 业务逻辑层
├── mapper        # MyBatis-Plus DAO 层
├── entity        # 数据库实体（继承 BaseEntity）
├── vo            # 返回给前端的数据对象
├── dto           # 接收前端参数的传输对象
├── config        # 模块配置
├── util          # 工具类
├── constant      # 常量
├── exception     # 异常
└── common        # 模块内公共
```

---

## 核心功能说明

### 权限体系 (finance-system)

基于 RBAC (Role-Based Access Control) 模型，实现细粒度的后台权限控制：

- **管理员表** (`ums_admin`)：系统管理员账号
- **角色表** (`ums_role`)：角色定义，支持状态启用/禁用
- **资源表** (`ums_resource`)：URL 资源定义，支持分类管理
- **关系表**：`ums_admin_role_relation`、`ums_role_resource_relation` 实现多对多关联

**权限拦截流程**：
1. 请求通过 Gateway 路由到具体服务
2. `LoginInterceptor` 校验 JWT Token 有效性（放行白名单 URL）
3. `PermissionInterceptor` 校验当前管理员是否拥有目标资源权限
4. 无角色或资源权限时直接返回 403

### 用户模块 (finance-user)

- 用户注册（BCrypt 加密密码）、登录（返回 JWT Token）
- 支持状态管理（禁用账号禁止登录）
- 密码重置与信息更新
- 使用 `JwtUtil`（common 模块统一工具）生成/验证 Token

### 产品与行情 (finance-product)

- 金融产品 CRUD（按名称/编码/类型搜索）
- 实时行情数据管理（`fin_market_data`）
- 支持分页查询

### 自选管理 (finance-account)

- 用户自选产品添加/删除
- 自选列表查询（按用户 ID）

### 交易委托 (finance-trade)

- 交易委托单发起（买入/卖出）
- 委托单撤销（状态校验）
- 委托单分页查询（多条件筛选）

### 消息与资讯 (finance-message)

- 财经资讯管理（`fin_news`）
- 站内消息推送（`fin_message`，集成 RabbitMQ）
- RabbitMQ 队列和交换机配置在 `RabbitMqConfig` 中统一管理

### 搜索服务 (finance-search)

- 基于 Elasticsearch 的产品全文检索
- 产品文档索引管理（保存、删除、按 ID 查询、关键词搜索）

---

## 环境搭建

### 前置条件

- JDK 21 ([下载](https://jdk.java.net/21/))
- Maven 3.9.x ([下载](https://maven.apache.org/download.cgi))
- Docker Desktop ([下载](https://www.docker.com/products/docker-desktop/))
- IDE：IntelliJ IDEA 2023+ 或 VS Code

### 数据库初始化

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS finance DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 执行建表脚本（项目根目录下）
source docs/sql/init.sql;
```

> 数据库初始化脚本包含全部业务表和权限数据，默认管理员账号：`admin` / `admin`（BCrypt 加密）。

### 中间件部署（Docker Compose）

```yaml
# docker-compose.yml 参考配置
version: '3.8'
services:
  nacos:
    image: nacos/nacos-server:2.2.3
    ports:
      - "8848:8848"
    environment:
      MODE: standalone

  mysql:
    image: mysql:8.0.37
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: 123456
      MYSQL_DATABASE: finance

  redis:
    image: redis:5.0.14
    ports:
      - "6379:6379"

  rabbitmq:
    image: rabbitmq:3.10.20-management
    ports:
      - "5672:5672"
      - "15672:15672"

  elasticsearch:
    image: elasticsearch:8.11.0
    ports:
      - "9200:9200"
      - "9300:9300"
    environment:
      discovery.type: single-node
```

### Nacos 配置

各模块的 `bootstrap.yml` 默认连接 `localhost:8848`。在 Nacos 中为每个模块创建对应的 Data ID 和配置内容（数据库连接、Redis、RabbitMQ 等）。

---

## 项目启动步骤

### 1. 克隆与编译

```bash
# 克隆项目
git clone https://github.com/renjianfeng8/finance-mid-platform.git
cd finance-mid-platform

# 编译公共模块（修改 common 后必须重新 install）
mvn clean install -pl finance-common -DskipTests

# 编译全部模块
mvn clean install -DskipTests
```

### 2. 启动中间件

确保以下服务已启动并可连接：

| 服务 | 地址 | 验证方式 |
|------|------|----------|
| Nacos | localhost:8848 | 访问 http://localhost:8848/nacos |
| MySQL | localhost:3306 | `mysql -uroot -p123456 -e "SELECT 1"` |
| Redis | localhost:6379 | `redis-cli ping` |
| RabbitMQ | localhost:5672 | 访问 http://localhost:15672 |
| Elasticsearch | localhost:9200 | `curl http://localhost:9200` |

### 3. 按顺序启动服务

建议启动顺序（从无依赖到有依赖）：

```bash
# 1. 启动 gateway（网关，无业务依赖）
mvn spring-boot:run -pl finance-gateway

# 2. 启动 system（后台权限，独立业务）
mvn spring-boot:run -pl finance-system

# 3. 启动无 Feign 调用的业务模块（user、product、trade、message）
mvn spring-boot:run -pl finance-user
mvn spring-boot:run -pl finance-product
mvn spring-boot:run -pl finance-trade
mvn spring-boot:run -pl finance-message

# 4. 启动有 Feign 调用的模块（account 依赖 product）
mvn spring-boot:run -pl finance-account

# 5. 启动 search（依赖 ES）
mvn spring-boot:run -pl finance-search
```

> 各模块启动时依赖 Nacos 配置中心。如果 Nacos 中未创建对应配置，模块将使用本地 `application.yml` 中的默认配置启动（finance-user 已内置，其他模块需确保 Nacos 有对应配置）。

### 4. 验证启动

应用启动后，通过网关访问各模块健康检查接口：

```
GET http://localhost:8080/actuator/health
```

---

## 接口文档

### Swagger / Knife4j 访问地址

| 模块 | 文档地址 |
|------|----------|
| 网关统一入口 | http://localhost:8080/doc.html |
| 系统服务 | http://localhost:8082/system/doc.html |
| 用户服务 | http://localhost:8088/user/doc.html |
| 产品服务 | http://localhost:8084/product/doc.html |
| 账户服务 | http://localhost:8086/account/doc.html |
| 交易服务 | http://localhost:8085/trade/doc.html |
| 消息服务 | http://localhost:8087/message/doc.html |

### 常用接口示例

#### 管理员登录

```bash
POST /system/umsAdmin/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin"
}

Response:
{
  "code": 200,
  "message": "success",
  "data": "eyJhbGciOiJIUzI1NiJ9..."
}
```

#### 分页查询管理员列表

```bash
GET /system/umsAdmin/list?pageNum=1&pageSize=10
Authorization: Bearer <token>

Response:
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [...],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

#### 用户注册

```bash
POST /user/user/register
Content-Type: application/json

{
  "username": "test_user",
  "password": "test123",
  "nickname": "测试用户",
  "phone": "13800138000"
}
```

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
| 500 | 服务器异常 |

---

## 项目规范

### 命名规范

| 元素 | 规范 | 示例 |
|------|------|------|
| 类名 | 大驼峰 | `UmsAdminController` |
| 方法名 | 小驼峰 | `getAdminList()` |
| 变量名 | 小驼峰 | `adminList` |
| 常量 | 大写+下划线 | `PERMIT_ALL_URLS` |
| 数据库表 | 小写+下划线 | `ums_admin` |
| 数据库字段 | 小写+下划线 | `create_time` |

### 分层规范

- **Entity**：继承 `BaseEntity`，`@TableName` 指定表名，字段使用 `@TableField` 映射
- **Mapper**：继承 `BaseMapper<T>`，禁止手写复杂 SQL
- **Service**：继承 `IService<T>` / `ServiceImpl<T>`
- **Controller**：不写业务逻辑，调用 Service 后返回 `Result.success(BeanConvertUtil.convert())`
- **VO**：返回前端的视图对象，与 Entity 分离
- **DTO**：接收前端参数的传输对象，支持 `@Valid` 校验

### Entity 继承规则

```
BaseEntity                    # 基础字段：id, create_time, update_time, del_flag
  ├── User                    # 表：sys_user（有 del_flag → @TableLogic）
  ├── FinProduct              # 表：fin_product（有 del_flag → @TableLogic）
  ├── FinTradeOrder           # 表：fin_trade_order（有 del_flag → @TableLogic）
  ├── FinMarketData           # 表：fin_market_data（无 update_time → exist=false）
  ├── FinNews                 # 表：fin_news（无 update_time → exist=false）
  ├── FinMessage              # 表：fin_message（无 update_time → exist=false）
  ├── FinUserFavorite         # 表：fin_user_favorite（无 update_time/del_flag → exist=false）
  ├── UmsAdmin                # 表：ums_admin（无 update_time/del_flag → exist=false）
  ├── UmsRole                 # 表：ums_role（无 update_time/del_flag → exist=false）
  ├── UmsResource             # 表：ums_resource（无 update_time/del_flag → exist=false）
  ├── UmsAdminRoleRelation    # 关联表（仅 id → 全部 exist=false）
  └── UmsRoleResourceRelation # 关联表（仅 id → 全部 exist=false）
```

### 数据库规范

- 数据库名：`finance`，字符集：`utf8mb4`
- 所有表包含：`id`(BIGINT 自增)、`create_time`(DATETIME)、`update_time`(DATETIME)、`del_flag`(TINYINT)
- 逻辑删除：`del_flag` = 0 未删除，1 已删除
- 禁止使用外键，关联在业务层处理
- 索引按建表语句创建

### 日志规范

使用 SLF4J + Lombok `@Slf4j` 注解，禁止使用 `System.out.println`：

```java
@Slf4j
public class SomeService {
    public void doSomething() {
        log.info("业务操作：{}", param);
        log.error("异常发生", exception);
    }
}
```

### API 规范

- 遵循 RESTful 规范（GET 查询、POST 创建、PUT 更新、DELETE 删除）
- 所有接口使用 `Result<T>` 统一返回
- 请求参数使用 `@Valid` 注解启用校验
- Controller 方法必须添加 Swagger `@Operation` 注解
- Feign 接口路径必须包含服务端 `context-path`

---

## 变更记录

### 2026-05-05 项目体检全量修复

基于全面的项目健康检查（详见 `PROJECT_HEALTH_REPORT.md`），完成了以下关键修复：

#### P0 阻塞性问题

| 问题 | 修复内容 |
|------|----------|
| 搜索服务未使用统一返回 | `ProductSearchController` 所有方法返回值改为 `Result<T>` |
| Feign 请求 404 | FeignClient 路径补上服务端 context-path 前缀 |
| AccountFeignClient URL 冲突 | 独立路径设计，消除映射冲突 |

#### P1 功能正确性

| 问题 | 修复内容 |
|------|----------|
| DTO 字段不一致 | `FinUserFavoriteDTO` 统一为 `productCode` 字段 |
| JWT 重复实现 | `UserServiceImpl` 复用 common 模块 `JwtUtil` |
| 密码明文覆盖 | `UserController.update` 增加密码防覆盖保护 |
| 权限拦截器空集合异常 | IN 查询前检查空集合，为空时直接返回 403 |
| 缺少 @TableName | `UmsRole` 补充 `@TableName("ums_role")` |

#### P2 安全与代码质量

| 问题 | 修复内容 |
|------|----------|
| MD5 密码加密 | 全面升级为 BCrypt（`BCryptPasswordEncoder`） |
| 缺少 @Valid 校验 | 所有 `@RequestBody` 参数补充 `@Valid` |
| BeanConvertUtil 废弃 API | `newInstance()` → `getDeclaredConstructor().newInstance()` |

#### 关键架构变更

| 变更项 | 说明 |
|--------|------|
| **MyBatis-Plus 3.5.10 → 3.5.7** | 降级原因：3.5.9+ 移除了 `PaginationInnerInterceptor` 类，3.5.7 是最后一个包含该类的版本 |
| **MyBatisPlusConfig 分页插件** | `finance-common` 中新建配置类，全局注入 `PaginationInnerInterceptor(DbType.MYSQL)` |
| **Entity 统一继承 BaseEntity** | 12 个 Entity 全部继承 `BaseEntity`，使用 `@TableField(exist = false)` 处理缺少对应列的情况，同时修复 ums_* 表 `@TableLogic` 引用不存在列的预存 bug |
| **System.out → SLF4J** | 全局替换为 `@Slf4j` + `log.info/warn/error` |
| **RabbitMqConfig 迁移** | 从 `finance-user` 移至 `finance-message` 模块 |
| **gateway 排除治理** | 通配符 `*:*` 改为精确排除 `spring-boot-starter-tomcat` + `spring-webmvc` |

---

## 后续开发建议

### 测试覆盖

项目当前 `pom.xml` 中 `maven-surefire-plugin` 配置了 `<skipTests>true</skipTests>`，建议：

- 为各模块 Service 层编写单元测试（JUnit 5 + Mockito）
- 为 Controller 层编写集成测试（`@SpringBootTest` + `@AutoConfigureMockMvc`）
- 为 Feign 接口编写契约测试
- 在 CI 流程中开启测试：`mvn test -DskipTests=false`

### 配置管理

- 各模块 `application.yml` 中的数据库密码、Redis 密码等敏感信息应通过 Nacos 配置中心管理
- 建议本地开发时使用 `application-local.yml` 或 Spring Profile 实现环境隔离
- `finance-search` 中的 ES 地址 `10.128.82.54:9200` 应移到 Nacos 配置中，避免硬编码

### 网关增强

- 添加网关级 JWT 鉴权过滤器，在网关层统一验证 Token
- 配置网关级限流（RequestRateLimiter）
- 补充全局 CORS 配置（已预配置，可按需调整）

### 服务治理

- 集成 Sentinel 实现服务熔断降级和流量控制
- 为 RabbitMQ 添加生产确认和消费重试机制
- 添加分布式事务支持（Seata）
- 补充 Feign 调用超时和重试配置

### 监控与运维

- 集成 Spring Boot Actuator（已预配置，按需启用详细端点）
- 添加 Prometheus + Grafana 监控
- 集成 SkyWalking 或 Arthas 实现分布式链路追踪
- 补充 Dockerfile 和 docker-compose 部署配置

### 代码质量

- 补充全局参数校验框架（`@Valid` 已集成，可扩展更多校验规则）
- 引入 MapStruct 替代 BeanUtils 提升 VO/Entity 转换性能（可选）
- 统一错误码枚举，丰富错误信息国际化

---

> 完整体检报告见 [PROJECT_HEALTH_REPORT.md](./PROJECT_HEALTH_REPORT.md)
> 开发规范详情见 [CLAUDE.md](./CLAUDE.md)
