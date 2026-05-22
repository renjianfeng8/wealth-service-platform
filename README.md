# 理财服务平台 (Wealth Service Platform)

> 基于 Spring Boot 3.x + Spring Cloud Alibaba 的金融级微服务中台架构项目，覆盖用户、产品、交易、消息等核心业务领域，提供高可用、高扩展的企业级金融解决方案。

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
| 产品域 | wealth-product | 金融产品管理、行情数据、用户自选 |
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
| Elasticsearch | 8.8.2 | 搜索引擎（可选，降级 MySQL LIKE） |

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
| JWT (jjwt 0.12.6) | 无状态认证 + 双 Token 机制 |
| Knife4j (4.4.0) | API 文档 |
| BCrypt (spring-security-crypto) | 密码加密 |
| Lombok | 代码简化 |
| Micrometer Tracing + Brave + Zipkin | 全链路追踪 |
| Micrometer Prometheus Registry | 监控指标暴露 |
| Prometheus + Grafana | 指标存储与可视化 |

### 中间件版本对应
| 中间件 | 端口 | 部署方式 | 用途 |
|--------|:----:|----------|------|
| Nacos Server | 8848 / 9848 | Docker | 注册中心 + 配置中心 |
| MySQL | 3306 | Docker / 本地 | 数据库 |
| Redis | 6379 | Docker | 缓存 |
| Elasticsearch | 9200 / 9300 | Docker | 搜索引擎（可选） |
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
├── wealth-system      → 后台权限（依赖 common，通过 Feign 调用 product）
├── wealth-user        → 用户服务（依赖 common）
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
3. `PermissionCheckInterceptor` 跨模块调用 system 服务 RBAC 鉴权
4. 无角色或资源权限时直接返回 403

### 安全防护体系

- **XSS 防护**：全局 `XssFilter` + `StringXssDeserializer` 覆盖 GET/POST 参数和 JSON 请求体
- **暴力破解防护**：连续 5 次失败锁定 15 分钟（Redis），支持验证码
- **JWT 双 Token**：`access_token`（30 分钟）+ `refresh_token`（7 天），refresh 一次性使用防重放
- **跨模块权限**：`PermissionCheckInterceptor` 对所有业务模块 POST/PUT/DELETE 进行 RBAC 鉴权

### 用户模块 (wealth-user)

- 用户注册（BCrypt 加密密码）、登录（返回 JWT Token）
- 支持状态管理（禁用账号禁止登录）
- 密码重置与信息更新
- 使用 `JwtUtil`（common 模块统一工具）生成/验证 Token

### 产品与行情 (wealth-product)

- 金融产品 CRUD（按名称/编码/类型搜索）
- 实时行情数据管理（`wea_market_data`）
- **SSE 实时推送**：每 2 秒模拟行情变化并广播全量快照到所有客户端（`/WeaMarketData/sse` 端点）
- 支持分页查询

### 自选管理 (wealth-product)

- 用户自选产品添加/删除
- 自选列表查询（按用户 ID）

### 交易委托 (wealth-trade)

- 交易委托单发起（买入/卖出）
- 委托单撤单（状态校验）
- 委托单分页查询（多条件筛选）

### 消息与资讯 (wealth-message)

- 财经资讯管理（`wea_news`）
- 站内消息推送（`wea_message`，DB 轮询替代 RabbitMQ）

### 搜索服务 (wealth-search)

- 基于 Elasticsearch 的产品全文检索
- 产品文档索引管理（保存、删除、按 ID 查询、关键词搜索）

---

## 项目启动

完整的环境搭建、中间件启动、编译构建、服务启动顺序及验证步骤，详见 [Startup.md](docs/Startup.md)。

## 接口文档

### Swagger / Knife4j 访问地址

> **注意**：自 v1.7.0 起，Swagger/Knife4j 接口文档已移出白名单，需登录获取 Token 后访问。

| 模块 | 文档地址 |
|------|----------|
| 网关统一入口 | http://localhost:8080/doc.html |
| 系统服务 | http://localhost:8082/system/doc.html |
| 用户服务 | http://localhost:8083/user/doc.html |
| 产品服务 | http://localhost:8084/product/doc.html |
| 交易服务 | http://localhost:8085/trade/doc.html |
| 消息服务 | http://localhost:8087/message/doc.html |

### 常用接口示例

#### 管理员登录（v1.7.0+ 返回双 Token）
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
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "expireIn": 604800000
  }
}
```

#### Token 刷新
```bash
POST /system/umsAdmin/refresh
Authorization: Bearer <refresh_token>

Response:
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "eyJ..."
  }
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

> 变更记录详见 [CHANGELOG.md](docs/CHANGELOG.md)  
> 开发规范详见 [CLAUDE.md](CLAUDE.md)  
> 数据库设计详见 [database-schema.md](docs/database-schema.md)  
> 生产就绪评估详见 [production-readiness-assessment.md](docs/production-readiness-assessment.md)
