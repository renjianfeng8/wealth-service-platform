# 理财服务平台 (Wealth Service Platform)

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

- **证券/基金行情展示**：SSE 实时行情推送，每 2 秒模拟价格波动并主动推送到前端
- **金融产品管理**：产品上架、分类、查询
- **用户自选管理**：用户自选产品关注
- **交易委托**：交易订单发起与管理
- **资讯消息**：财经资讯推送、站内消息通知
- **后台权限管理**：统一后台管理员、角色、资源权限控制
### 核心功能模块

| 领域 | 模块 | 核心能力 |
|------|------|----------|
| 用户域 | wealth-user | 系统用户注册/登录、个人信息管理 |
| 产品域 | wealth-product | 金融产品管理、行情数据 |
| 账户域 | wealth-account | 用户自选产品管理 |
| 交易域 | wealth-trade | 交易委托单发起、撤单、查询 |
| 消息域 | wealth-message | 财经资讯、站内消息 |
| 搜索域 | wealth-search | 基于 ES 的产品全文检索 |
| 系统域 | wealth-system | 管理员、角色、资源、权限拦截 |
| 网关域 | wealth-gateway | 统一路由、CORS |
| 公共域 | wealth-common | 工具类、Feign 接口、全局异常处理 |

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
| Elasticsearch | 8.8.2 | 搜索引擎 |
| Sentinel | 1.8.6 | 熔断限流 |
| Seata | 2.0.0 | 分布式事务 |

### 前端

| 技术 | 说明 |
|------|------|
| Vue 3 | 渐进式前端框架 |
| TypeScript | 类型安全的 JavaScript 超集 |
| Element Plus | 基于 Vue 3 的企业级 UI 组件库 |
| Vite | 前端构建与开发服务器 |
| Pinia | 状态管理 |
| Axios | HTTP 请求库 |
| Vue Router | 前端路由 |

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
| Micrometer Tracing + Brave + Zipkin | 全链路追踪 |
| Micrometer Prometheus Registry | 监控指标暴露 |
| Prometheus + Grafana | 指标存储与可视化 |
| Sentinel | 熔断限流 |
| Seata | 分布式事务 |

### 中间件版本对应
| 中间件 | 端口 | 部署方式 | 用途 |
|--------|:----:|----------|------|
| Nacos Server | 8848 / 9848 | Docker | 注册中心 + 配置中心 |
| MySQL | 3306 | Docker / 本地 | 数据库 |
| Redis | 6379 | Docker | 缓存 |
| RabbitMQ | 5672 / 15672 | Docker | 消息队列 |
| Elasticsearch | 9200 / 9300 | Docker | 搜索引擎 |
| Sentinel Dashboard | 8858 | Docker | 熔断限流控制台 |
| Seata Server | 7091 / 8091 | Docker | 分布式事务协调器 |
| Nginx | 80 | Docker | 反向代理 |
| Zipkin | 9411 | Docker | 链路追踪 |
| Prometheus | 9090 | Docker | 监控指标存储 |
| Grafana | 3001 | Docker | 监控仪表盘 |

---

## 模块架构

### 模块依赖关系

```
wealth-service-platform (pom)
├── wealth-common      → 所有模块依赖（公共工具、Feign 接口、统一返回、异常处理、全局配置）
├── wealth-gateway     → 网关路由（依赖 common）
├── wealth-system      → 后台权限（依赖 common，通过 Feign 调用 account/product）
├── wealth-user        → 用户服务（依赖 common）
├── wealth-account     → 自选服务（依赖 common）
├── wealth-product     → 产品服务（依赖 common）
├── wealth-trade       → 交易服务（依赖 common）
├── wealth-message     → 消息服务（依赖 common）
└── wealth-search      → 搜索服务（依赖 common）
```

> **注意**：修改 `wealth-common` 后必须先执行 `mvn clean install -pl wealth-common -DskipTests`，其他模块才能引用最新版本。
### 端口与上下文路径

| 模块 | 端口 | context-path | 服务名 |
|------|------|-------------|--------|
| wealth-gateway | 8080 | - | wealth-gateway |
| wealth-system | 8082 | /system | wealth-system |
| wealth-product | 8084 | /product | wealth-product |
| wealth-trade | 8085 | /trade | wealth-trade |
| wealth-account | 8086 | /account | wealth-account |
| wealth-message | 8087 | /message | wealth-message |
| wealth-user | 8083 | /user | wealth-user |
| wealth-search | 8089 | - | wealth-search |

### 包路径规范
| 模块 | 基础包 |
|------|--------|
| wealth-common | `com.wealth.common` |
| wealth-gateway | `com.wealth.gateway` |
| wealth-user | `com.wealth.user` |
| 其余业务模块 | `com.wealth.platform.{模块名}` |

### 各模块包结构

```
com.wealth.platform.{模块名}
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

### 权限体系 (wealth-system)

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

### 用户模块 (wealth-user)

- 用户注册（BCrypt 加密密码）、登录（返回 JWT Token）
- 支持状态管理（禁用账号禁止登录）
- 密码重置与信息更新
- 使用 `JwtUtil`（common 模块统一工具）生成/验证 Token

### 产品与行情 (wealth-product)

- 金融产品 CRUD（按名称/编码/类型搜索）
- 实时行情数据管理（`wea_market_data`）
- 支持分页查询

### 自选管理 (wealth-account)

- 用户自选产品添加/删除
- 自选列表查询（按用户 ID）

### 交易委托 (wealth-trade)

- 交易委托单发起（买入/卖出）
- 委托单撤单（状态校验）
- 委托单分页查询（多条件筛选）

### 消息与资讯 (wealth-message)

- 财经资讯管理（`wea_news`）
- 站内消息推送（`wea_message`，集成 RabbitMQ）
- RabbitMQ 队列和交换机配置在 `RabbitMqConfig` 中统一管理

### 搜索服务 (wealth-search)

- 基于 Elasticsearch 的产品全文检索
- 产品文档索引管理（保存、删除、按 ID 查询、关键词搜索）

---

## 项目启动

完整的环境搭建、中间件启动、编译构建、服务启动顺序及验证步骤，详见 [Startup.md](docs/Startup.md)。

## 接口文档

### Swagger / Knife4j 访问地址

| 模块 | 文档地址 |
|------|----------|
| 网关统一入口 | http://localhost:8080/doc.html |
| 系统服务 | http://localhost:8082/system/doc.html |
| 用户服务 | http://localhost:8083/user/doc.html |
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
   ├── WeaProduct              # 表：wea_product（有 del_flag → @TableLogic）
   ├── WeaTradeOrder           # 表：wea_trade_order（有 del_flag → @TableLogic）
   ├── WeaMarketData           # 表：wea_market_data（无 update_time → exist=false）
   ├── WeaNews                 # 表：wea_news（无 update_time → exist=false）
   ├── WeaMessage              # 表：wea_message（无 update_time → exist=false）
   ├── WeaUserFavorite         # 表：wea_user_favorite（无 update_time/del_flag → exist=false）
   ├── UmsAdmin                # 表：ums_admin（无 update_time/del_flag → exist=false）
   ├── UmsRole                 # 表：ums_role（无 update_time/del_flag → exist=false）
   ├── UmsResource             # 表：ums_resource（无 update_time/del_flag → exist=false）
   ├── UmsAdminRoleRelation    # 关联表（仅 id → 全部 exist=false）
   └── UmsRoleResourceRelation # 关联表（仅 id → 全部 exist=false）
```

### 数据库规范
- 数据库名：`Wealth`，字符集：`utf8mb4`
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

### 2026-05-16 项目重命名重构

项目从 `finance-mid-platform` 重命名为 `wealth-service-platform`：

- 所有 Java 包名 `com.finance.*` → `com.wealth.*`
- 所有模块名 `finance-*` → `wealth-*`
- 数据库名 `finance` → `wealth`
- Nacos 配置服务名、JWT secret 同步更新
- 全链路编译 (10 模块 BUILD SUCCESS)
- 全量 API 测试通过（登录认证 / 网关路由 / 分页查询）
- Playwright 34 项 E2E 测试：33 通过，1 项预知问题（前端 dashboard 选择器）

### 2026-05-12 项目体检与质量修复

基于全面的项目健康检查，完成了以下关键修复：

#### P0 阻塞性问题
| 问题 | 修复内容 |
|------|----------|
| 搜索服务未使用统一返回 | `ProductSearchController` 所有方法返回值改为 `Result<T>` |
| Feign 请求 404 | FeignClient 路径补上服务端 context-path 前缀 |
| AccountFeignClient URL 冲突 | 独立路径设计，消除映射冲突 |

#### P1 功能正确性
| 问题 | 修复内容 |
|------|----------|
| DTO 字段不一致 | `WeaUserFavoriteDTO` 统一为 `productCode` 字段 |
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
| **MyBatisPlusConfig 分页插件** | `wealth-common` 中新建配置类，全局注入 `PaginationInnerInterceptor(DbType.MYSQL)` |
| **Entity 统一继承 BaseEntity** | 12 个 Entity 全部继承 `BaseEntity`，使用 `@TableField(exist = false)` 处理缺少对应列的情况，同时修复 ums_* 表 `@TableLogic` 引用不存在列的预置 bug |
| **System.out → SLF4J** | 全局替换为 `@Slf4j` + `log.info/warn/error` |
| **RabbitMqConfig 迁移** | 从 `wealth-user` 移至 `wealth-message` 模块 |
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
- `wealth-search` 中的 ES 地址应移到 Nacos 配置中，避免硬编码
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
