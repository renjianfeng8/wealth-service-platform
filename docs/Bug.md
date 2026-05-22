# Bug 记录文档

> 记录项目开发中遇到的关键问题及解决方案，供后续排查参考。

---

## Bug-001: ES 搜索报 ConversionException（日期格式不匹配）
**日期**: 2026-05-12
**模块**: wealth-search
**影响**: ES 搜索接口返回 500，无法查询数据
### 现象

调用 `GET /search/product/search?keyword=xxx` 返回：
```json
{"code":500,"message":"系统错误：Conversion exception when converting document id 1"}
```

但 ES 集群本身查询正常（docker exec 直接查询 ES 成功），索引文档也存在（count=8）。
### 原因

`ProductDocument.java` 中 `createTime` 和 `updateTime` 字段定义：
```java
@Field(type = FieldType.Date)
private LocalDateTime createTime;
```

未指定日期格式时，Spring Data Elasticsearch 默认使用 `date_optional_time` 格式存储。ES 返回的 `_source` 中日期被截断为纯日期字符串（如 `"2026-05-10"`），但 Java 实体字段类型为 `LocalDateTime`，反序列化时无法将 `"2026-05-10"` 转换为 `LocalDateTime`，抛出 `ConversionException`。
### 修复

显式指定日期格式为 `DateFormat.date_hour_minute_second_millis`：
```java
@Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
private LocalDateTime createTime;
```

同时索引数据时须传入完整 ISO 日期时间格式（如 `"2026-05-10T11:29:46.000"`）。
### 涉及文件

- `wealth-search/src/main/java/com/wealth/platform/search/entity/ProductDocument.java`

### 排查要点（后续遇到类似问题先查此清单）
- [ ] ES 查询是否报 `ConversionException` / 搜索接口返回 500
- [ ] ES mapping 中日期字段格式是否与 Java 实体 `@Field` 声明一致
- [ ] 索引文档的 `_source` 中日期值是否为完整格式（含时间部分）
- [ ] `@Field` 中 `FieldType` 是否与 Java 类型匹配（如 `BigDecimal` → `Scaled_Float`）
- [ ] 无 Redis 依赖的模块启动是否报 `NoClassDefFoundError: RedisSerializer`
      → 检查 `RedisConfig` / `RedisUtil` 是否有 `@ConditionalOnClass`
- [ ] IK 分词器是否生效 → 检查 ES mapping 中 `analyzer` 是否为 `ik_max_word`

---

## Bug-002: wealth-search 启动失败（RedisSerializer NoClassDefFoundError）
**日期**: 2026-05-12
**模块**: wealth-common / wealth-search

### 现象

wealth-search 启动时报：
```
NoClassDefFoundError: org.springframework.data.redis.serializer.RedisSerializer
```

### 原因

`RedisConfig.java` 和 `RedisUtil.java` 位于 wealth-common 中，但 wealth-search 在 pom.xml 中排除了 Redis 依赖（`spring-boot-starter-data-redis`）。Spring 启动时扫描到这两个类并尝试加载，因缺少 Redis 类而失败。
### 修复

在两个类上添加 `@ConditionalOnClass(name = "org.springframework.data.redis.core.RedisTemplate")`：
```java
@Configuration
@ConditionalOnClass(name = "org.springframework.data.redis.core.RedisTemplate")
public class RedisConfig { ... }

@Component
@ConditionalOnClass(name = "org.springframework.data.redis.core.RedisTemplate")
public class RedisUtil { ... }
```

### 涉及文件

- `wealth-common/src/main/java/com/wealth/common/config/RedisConfig.java`
- `wealth-common/src/main/java/com/wealth/common/utils/RedisUtil.java`

---

## Bug-003: ES 索引数据为空（索引重建后未同步）

**日期**: 2026-05-12
**模块**: wealth-search / wealth-product

### 现象

ES 索引 `wea_product` 存在但文档数为 0，搜索无结果。
### 原因

ES 索引被删除重建后，MySQL 中的产品数据未自动同步到 ES。项目目前无自动同步机制，需手动通过 search 服务的 save API 重新索引。
### 重建步骤

```bash
# 1. 从产品服务获取所有产品
curl http://localhost:8080/product/WeaProduct

# 2. 逐条通过 search 服务写入 ES
# POST http://localhost:8080/search/product
# Body: { "id":1, "productName":"黄金ETF", "productCode":"GOLD001", ... }

# 3. 验证
docker exec es curl -s 'http://localhost:9200/wea_product/_count'
```

### 涉及文件

- `wealth-search/src/main/java/com/wealth/platform/search/controller/ProductSearchController.java`
- `wealth-product/src/main/java/com/wealth/platform/product/controller/WeaProductController.java`

---

## Bug-004: 交易委托提交提示"用户信息异常"（userId 为 0）
**日期**: 2026-05-12
**模块**: front-user / wealth-user
**影响**: 登录后无法提交交易委托单

### 现象

用户已登录（持有 JWT Token，能正常访问各页面），但提交交易委托时弹窗提示"用户信息异常，请重新登录"。Playwright 测试全部通过（34 项），仅手动提交流程触发该错误。
### 原因

`front-user/src/store/index.ts` 中 `login()` 方法流程：
```
登录成功 → 获取 token → setToken() → 调用 getUserList() 查询所有用户 → users.find(u => u.username === 登录用户名) → 匹配到则设置 userId
```

`getUserList()` 发 GET `/user` 请求依赖后端拦截器验证 Token，若该请求因任何原因失败（网络超时、服务未就绪、Token 校验异常等），`catch` 块静默吞掉错误，`userId` 保持为 0。`setStoredUser({ userId: 0, ... })` 将 0 写入 localStorage。后续页面 reload 后 `userId` 依然是 0。
交易委托页 `handleSubmit()` 检查 `if (!userStore.userId)` → `!0 === true` → 显示"用户信息异常"。
### 修复

**方案**：登录接口不再返回纯字符串 Token，改为返回 `LoginVO { token, userId, nickname }`，前端直接从登录响应中获取 userId，消除对 `getUserList()` 的二次调用依赖。
#### 后端改动

1. 新增 `LoginVO`（`wealth-user/vo/LoginVO.java`）：  
   ```java
   public class LoginVO {
       private String token;
       private Long userId;
       private String nickname;
   }
   ```

2. `UserService.login()` 返回类型由 `String` 改为 `LoginVO`
3. `UserController.login()` 返回类型由 `Result<String>` 改为 `Result<LoginVO>`

#### 前端改动

`front-user/src/store/index.ts` 中 `login()`：
```typescript
// 之前：取 token 后二次调用 getUserList()
const token = res.data as string
this.token = token
setToken(token)
// getUserList() 可能失败...

// 之后：直接从登录响应解构 token + userId
const { token, userId } = res.data
this.token = token
this.userId = userId
setToken(token)
setStoredUser({ username, userId, nickname, avatar })
```

### 排查要点（添加到已有清单）
- [ ] 前端"用户信息异常" → 检查 `userStore.userId` 是否为 0
- [ ] 检查登录接口响应中是否包含 `userId`
- [ ] 检查 localStorage 中 `wealth_user_info.userId` 值
- [ ] 更新代码后须重启 wealth-user 服务使 VO 变更生效

### 涉及文件

- `wealth-user/src/main/java/com/wealth/user/vo/LoginVO.java`（新增）
- `wealth-user/src/main/java/com/wealth/user/service/UserService.java`
- `wealth-user/src/main/java/com/wealth/user/service/impl/UserServiceImpl.java`
- `wealth-user/src/main/java/com/wealth/user/controller/UserController.java`
- `front-user/src/store/index.ts`

---

## Bug-005: 交易委托分页筛选不生效（orderStatus 参数被忽略）

**日期**: 2026-05-12
**模块**: wealth-trade
**影响**: 前端筛选"已成交/待成交/已撤销"无效，始终返回全部数据

### 现象

前端委托单列表的筛选下拉框选择"已成交"或"已撤销"后，列表数据未变化，始终展示全部订单。浏览器 Network 面板可看到 `orderStatus` 参数已正常发送。
### 原因

`WeaTradeOrderController.page()` 方法只接收 `pageNum` 和 `pageSize` 两个参数，未声明 `orderStatus` 和 `userId` 参数。`orderStatus` 和 `userId` 虽以 query string 形式发送到后端，但被 Spring MVC 忽略。
```java
// 修复前：只有分页参数，无筛选参数
@GetMapping("/page")
public Result<IPage<WeaTradeOrderVO>> page(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize) {
    Page<WeaTradeOrder> page = new Page<>(pageNum, pageSize);
    IPage<WeaTradeOrder> entityPage = WeaTradeOrderService.page(page); // 无条件查询全部
    ...
}
```

### 涉及文件

- `wealth-trade/src/main/java/com/wealth/platform/trade/controller/WeaTradeOrderController.java`
- `wealth-trade/src/main/java/com/wealth/platform/trade/service/WeaTradeOrderService.java`
- `wealth-trade/src/main/java/com/wealth/platform/trade/service/impl/WeaTradeOrderServiceImpl.java`

---

## Bug-006: 产品中心分类筛选不生效（productType 参数被忽略）

**日期**: 2026-05-12
**模块**: wealth-product
**影响**: 前端筛选"贵金属/理财产品/基金/股票"无效，始终显示全部产品

### 原因

同 Bug-005 相同模式 — `WeaProductController.page()` 只接收 `pageNum` 和 `pageSize`，未声明 `productType` 参数，前端传参被 Spring MVC 忽略。
### 涉及文件

- `wealth-product/src/main/java/com/wealth/platform/product/controller/WeaProductController.java`
- `wealth-product/src/main/java/com/wealth/platform/product/service/WeaProductService.java`
- `wealth-product/src/main/java/com/wealth/platform/product/service/impl/WeaProductServiceImpl.java`

---

## Bug-007: 财经资讯/消息中心分类筛选不生效（newsType/userId 参数被忽略）

**日期**: 2026-05-12
**模块**: wealth-message
**影响**: 财经资讯的分类筛选（行业动态/市场分析/政策解读/公司公告）和消息中心的用户筛选不生效

### 原因

同 Bug-005/Bug-006 相同模式 — `WeaNewsController.page()` 和 `WeaMessageController.page()` 只接收分页参数，未声明 `newsType`/`userId` 筛选参数。
### 涉及文件

- `wealth-message/src/main/java/com/wealth/platform/message/controller/WeaNewsController.java`
- `wealth-message/src/main/java/com/wealth/platform/message/service/WeaNewsService.java`
- `wealth-message/src/main/java/com/wealth/platform/message/service/impl/WeaNewsServiceImpl.java`
- `wealth-message/src/main/java/com/wealth/platform/message/controller/WeaMessageController.java`
- `wealth-message/src/main/java/com/wealth/platform/message/service/WeaMessageService.java`
- `wealth-message/src/main/java/com/wealth/platform/message/service/impl/WeaMessageServiceImpl.java`

---

## Bug-008: 停售产品仍可点击"去交易"跳转交易页
**日期**: 2026-05-12
**模块**: front-user
**影响**: 标记为"停售"的产品，用户仍可通过详情弹窗中的"去交易"按钮进入交易页下单
### 现象

产品卡片上显示"停售"标签的产品，点击查看详情后，详情弹窗底部的"去交易"按钮仍可点击，会跳转到交易委托页并带入产品代码，用户可能对停售产品下单。
### 原因

详情弹窗的"去交易"按钮未根据 `status` 字段做条件禁用，始终可点击：

```html
<!-- 修复前：始终可点击 -->
<el-button type="primary" @click="goTrade(detailItem)">去交易</el-button>

<!-- 修复后：停售时禁用 -->
<el-button type="primary" :disabled="detailItem?.status !== 1" @click="goTrade(detailItem)">去交易</el-button>
```

### 涉及文件

- `front-user/src/views/product/index.vue`

---

## Bug-009: Nacos Zipkin 配置属性不生效（zipkin.base-url 在 Spring Boot 3.x 中无效）
**日期**: 2026-05-17
**模块**: Nacos 配置中心 / wealth-common
**影响**: 链路追踪 Span 无法上报到 Zipkin 服务端

### 现象

尽管 Micrometer Tracing + Brave + Zipkin 依赖已正确引入，Zipkin 容器运行正常（`http://localhost:9411` 可访问），但 `/api/v2/traces` 始终返回空数组 `[]`，表明各服务的 Span 未成功上报。

### 原因

`wealth-shared.yaml` 中错误地使用了 `zipkin.base-url` 配置：

```yaml
# ❌ 错误：该属性来自 Spring Cloud Sleuth（Spring Boot 2.x），
#    在 Spring Boot 3.x + Micrometer Tracing 中不被识别
zipkin:
  base-url: http://localhost:9411/
```

Spring Boot 3.x 中 Brave Zipkin 的正确配置属性为 `management.zipkin.tracing.endpoint`：

```yaml
# ✅ 正确：Standard Spring Boot 3.x Micrometer Tracing 属性
management:
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans
```

### 排查要点（添加到已有清单）
- [ ] Zipkin 页面可打开但没有 Span 数据 → 确认 Nacos `wealth-shared.yaml` 中配置的是 `management.zipkin.tracing.endpoint` 而非 `zipkin.base-url`
- [ ] 各服务启动日志中搜索 `BraveZipkinAutoConfiguration` 确认 Zipkin 发送器已自动装配
- [ ] 检查各服务是否引入 `micrometer-tracing-bridge-brave` + `zipkin-sender-okhttp3` 依赖

### 涉及文件

- Nacos 配置中心 `wealth-shared.yaml`

### 修复

通过 Nacos HTTP API 将 `zipkin.base-url` 更正为 `management.zipkin.tracing.endpoint`。详见 [Nacos 配置参考](docs/nacos-config-reference.md)。

---

## Bug-010: docker-compose YAML 锚点语法错误导致服务无法启动
**日期**: 2026-05-17
**模块**: 基础设施 / docker-compose.yml
**影响**: `docker-compose up -d` 执行失败，所有容器无法编排

### 现象

执行 `docker-compose up -d nginx` 报错：
```
yaml: line 168, column 13: unknown anchor 'image-prefix/wealth-gateway' referenced
```

### 原因

`docker-compose.yml` 使用了 YAML 锚点语法：
```yaml
x-image-prefix: &image-prefix ghcr.io/renjianfeng8/wealth-service-platform

gateway:
  image: *image-prefix/wealth-gateway:latest   # ❌ 语法错误
```

YAML 锚点 `*image-prefix` 只能作为完整的标量值引用，不能嵌入字符串中进行拼接。`*image-prefix/wealth-gateway:latest` 会被解析为锚点名 `image-prefix/wealth-gateway`（包含路径），而非预期的锚点 `image-prefix` 后拼接 `/wealth-gateway:latest`。

### 修复

移除锚点定义，改为内联完整镜像名：
```yaml
gateway:
  image: ghcr.io/renjianfeng8/wealth-service-platform/wealth-gateway:latest
```

### 涉及文件

- `docker-compose.yml`（共 10 处 `*image-prefix/wealth-*` 替换）

---

## Bug-011: Nginx 启动时上游 DNS 解析失败导致 crash
**日期**: 2026-05-17
**模块**: 基础设施 / nginx.conf
**影响**: nginx 容器反复 crash，无法提供反向代理服务

### 现象

启动 nginx 容器后立即退出，日志持续报错：
```
[emerg] host not found in upstream "frontend" in /etc/nginx/conf.d/default.conf:23
nginx: [emerg] host not found in upstream "frontend"
```

### 原因

Nginx 在启动时会解析 `proxy_pass` 指向的上游域名。当上游服务（如 `frontend`）因镜像拉取鉴权或其他原因无法启动时，对应容器不存在于 Docker 网络中，域名无法解析，nginx 直接退出（emerg 级别错误），而非降级为运行时 502。

此外，原 `docker-compose.yml` 中 nginx 通过 `depends_on: frontend` 声明依赖，但 `frontend` 依赖 ghcr.io 镜像需要认证，导致 `frontend` 容器无法运行，nginx 也因此受阻。

### 修复

1. 在 nginx server 块中添加 Docker DNS 解析器：
   ```nginx
   resolver 127.0.0.11 valid=10s ipv6=off;
   ```
2. 将静态 `proxy_pass` 改为变量化动态解析：
   ```nginx
   set $frontend_upstream http://frontend:80;
   set $gateway_upstream http://gateway:8080;
   proxy_pass $frontend_upstream;  # 运行时而非启动时解析
   ```
3. 移除 nginx 对 frontend 的 `depends_on` 依赖，允许 nginx 独立启动

这样当上游服务未就绪时，nginx 正常启动并返回 502，而非直接崩溃。

### 涉及文件

- `nginx.conf`（resolver + set + 变量 proxy_pass）
- `docker-compose.yml`（移除 depends_on: frontend）

---

## Bug-012: Alpine MariaDB 客户端连接 MySQL 8 失败（SSL + 认证插件兼容性）
**日期**: 2026-05-17
**模块**: 基础设施 / docker-compose
**影响**: `mysql-backup` 备份容器 mysqldump 命令连接 MySQL 失败，备份为空文件

### 现象

备份容器运行 `mysqldump` 连接 MySQL 8.0 时报错：
```
# 第一阶段：SSL 自签名证书错误
mysqldump: Got error: 2026: "TLS/SSL error: self-signed certificate in certificate chain"

# 第二阶段（修复 SSL 后）：caching_sha2_password 认证插件缺失
mysqldump: Got error: 1045: "Plugin caching_sha2_password could not be loaded:
  Error loading shared library /usr/lib/mariadb/plugin/caching_sha2_password.so"
```

备份文件仅 20 字节（gzip 空流），无明显报错（管道中 `$?` 只捕获了 `gzip` 的退出码）。

### 原因

1. **客户端差异**：Alpine `mysql-client` 包实际上是 MariaDB 客户端，与 Oracle MySQL 8 协议存在差异
2. **SSL 默认启用**：MariaDB 客户端默认使用 SSL 连接，MySQL 8 容器使用自签名证书会导致握手失败
3. **认证插件不兼容**：MySQL 8 默认使用 `caching_sha2_password`，而 Alpine MariaDB 客户端缺少该插件动态库
4. **管道错误码丢失**：`mysqldump ... | gzip > file` 中 `$?` 获取的是 `gzip` 而非 `mysqldump` 的退出码，失败被静默吞掉

### 修复

1. `mysqldump` 添加 `--ssl=0` 参数（MariaDB 语法），禁用 SSL 连接（Docker 内网安全）→ 解决 TLS 错误
2. 备份脚本改用临时文件 + 显式退出码检查取代管道：`mysqldump > file && gzip file` → 解决错误码被吞
3. 采用 Alpine 镜像 + `mysql-client` + 上述参数的方案最为简洁可靠。MySQL 官方镜像 `mysql:8.0.37` 不自带 `crond`，不适合用作调度容器基础镜像

### 涉及文件

- `scripts/backup-scheduler.sh`（`--ssl=0` + 分步写入替代管道）
- `docker-compose.yml`（mysql-backup 服务使用 alpine 镜像）

---

## Bug-013: Redis 配置在 Docker 容器内完全被忽略，始终连接 localhost:6379

**日期**: 2026-05-22
**模块**: wealth-system / wealth-user（所有引入 Redis 的模块）
**影响**: Redis 相关业务不可用，权限拦截器因未捕获的 `DataAccessException` 返回 500

### 现象

容器内所有配置方式均无法覆盖 `spring.redis.host` 默认值 `localhost`：

```
# 错误堆栈
Caused by: io.netty.channel.AbstractChannel$AnnotatedConnectException: Connection refused: localhost/127.0.0.1:6379
```

Redis 容器 `wealth-redis` 运行正常（172.18.0.7:6379），网络连通性验证通过（`nc -zv redis 6379` 成功），但应用仍然连 `localhost`。

### 尝试过的配置方式（全部无效）

| # | 配置方式 | 预期优先级 | 实际效果 |
|---|---------|-----------|---------|
| 1 | `application-prod.yml`: `spring.redis.host: redis` | 中等 | 被忽略 |
| 2 | `-Dspring.redis.host=redis`（JVM 系统属性，在 Dockerfile 和 docker-compose entrypoint） | 高 | 被忽略 |
| 3 | `SPRING_REDIS_HOST=redis`（OS 环境变量，下划线大写格式） | 高 | 被忽略 |
| 4 | `SPRING_APPLICATION_JSON={"spring":{"redis":{"host":"redis"}}}`（Spring Boot 最高优先级 env，仅次命令行参数） | 最高 | 被忽略 |

### 验证过的事实

- `/proc/1/cmdline` 确认 `-Dspring.redis.host=redis` 已传入 Java 进程
- `env | grep SPRING` 确认 `SPRING_APPLICATION_JSON` 环境变量值正确
- `docker exec wealth-system sh -c 'nc -zv redis 6379'` 网络连通正常
- Redis 容器运行正常，其他服务（如系统服务连 MySQL）通过同格式环境变量正常工作
- 项目代码中不存在自定义 `RedisConnectionFactory` bean 覆盖自动配置
- `application.yml` 中未定义 `spring.redis.host`（非默认值覆盖问题）

### 根因

**未查明。** 所有标准配置方式均被 Spring Boot 3.3.5 的 `RedisAutoConfiguration` 忽略。怀疑方向：

1. `RedisConfig`（`AutoConfiguration.imports` 注册）可能在 `RedisAutoConfiguration` 之前触发了 `RedisConnectionFactory` 的早期初始化，导致属性未绑定
2. Spring Boot 3.3.5 特定版本与 Lettuce 的自动配置存在竞态条件
3. Docker 镜像层（eclipse-temurin:21-jre-alpine）或 JVM 实现差异

### 修复

不在追究配置问题，改为业务代码防御性处理：

在 `PermissionInterceptor` 的 Redis 读写处加 `try-catch(DataAccessException)`，Redis 不可用时降级到数据库查询（不影响业务，仅失去缓存加速）：

```java
if (redisUtil != null) {
    try {
        Object cached = redisUtil.get(cacheKey);
        if (cached instanceof List) {
            allowedUrls = (List<String>) cached;
        }
    } catch (DataAccessException e) {
        log.warn("Redis 不可用，降级到数据库查询");
    }
}
```

`UmsAdminServiceImpl` 中原有的 `refresh_token` 写入已有 try-catch 兜底，无需改动。

### 涉及文件

- `wealth-system/src/main/java/com/wealth/platform/system/interceptor/PermissionInterceptor.java`

### 排查要点（后续遇到类似问题先查此清单）
- [ ] Redis 相关 500 错误 → 确认 `management.health.redis.enabled=false` 已配置
- [ ] 业务代码中 Redis 调用是否被 try-catch 包裹（Redis 不可用不应阻断业务）
- [ ] 如要调试配置绑定问题 → 临时暴露 `/actuator/env` 端点或使用 `jinfo -sysprops` 检查 JVM 属性

---

## OOM 风险审查报告 (2026-05-21)

> 审查范围：全部 8 个微服务模块 + wealth-common 公共模块
> 审查方法：代码静态分析，覆盖集合使用、大对象加载、资源管理、内存泄漏模式、SSE/定时任务
>
> ✅ **全部已修复** — commit `e2d4efd1~后续提交`

### 【高风险】真实 OOM 场景

#### H1. SSE Emitter 集合无界增长
**文件**: `wealth-product/.../service/MarketDataPushService.java:20`

```java
private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();
```

- `createEmitter()` 每次客户端连接无条件 `emitters.add(emitter)`，**无数量上限**
- `onCompletion`/`onTimeout`/`onError` 在客户端网络闪断时可能不触发，导致僵尸 emitter 泄漏
- 每个 `SseEmitter(86400_000L)` 持有 24h 超时的底层缓冲区

**OOM 路径**: 攻击者批量创建 SSE 连接后断网 → emitter 泄漏 → CopyOnWriteArrayList 持续膨胀 → OOM

**修改建议**:
1. 添加 emitter 数量上限（如 MAX_EMITTERS = 500），超限时拒绝新连接或替换最旧连接
2. 添加心跳检测，广播前使用 `emitters.removeIf(Emitters::isExpired)` 清理僵尸连接
3. 使用 `ExecutorService` 异步广播，避免串行阻塞定时任务

---

#### H2. selectList(null) 全表无界加载
**文件**: 
- `wealth-product/.../impl/ProductSyncServiceImpl.java:31`
- `wealth-product/.../service/MarketDataSimulationService.java:52`

```java
List<WeaProduct> products = productMapper.selectList(null);  // 全表加载
```

- `ProductSyncServiceImpl` 每 2 分钟无条件全量加载产品表
- `MarketDataSimulationService` 在 `@PostConstruct` 时全量加载行情表
- 当前演示数据仅几十条，但**生产环境数据达数万行时**将直接 OOM

**修改建议**:
1. `ProductSyncServiceImpl` 改为分页批量同步（MyBatis-Plus Page 分批取数 → 逐页同步 ES）
2. `MarketDataSimulationService` 若数据量增大，改为仅加载活跃产品

---

#### H3. 全 Controller list() 接口无分页全量查询（10+ 端点）
**涉及文件**: UserController、ProductController、MarketDataController、TradeOrderController、NewsController、MessageController、UmsAdminController、UmsRoleController、UmsResourceController 等全部 `@GetMapping` 无参 `list()` 端点

```java
@GetMapping
public Result<List<FinProductVO>> list() {
    return Result.success(finProductService.getProductList());  // → selectList(null)
}
```

- 全部调用 MyBatis-Plus `service.list()` → `selectList(null)`，无防呆、无上限
- 订单表、资讯表等持续增长的表一旦被遍历将导致堆溢出

**修改建议**:
1. 为每个 `list()` 端点添加上限保护（如 `LIMIT 1000`），或强制前端走分页接口
2. 移除前端不需要全量数据的 `list()` 端点

---

### 【中风险】特定条件下可能 OOM

#### M1. 行情广播全量拷贝 + 串行发送压力
**文件**: `wealth-product/.../service/MarketDataSimulationService.java:67`

- 每 2 秒对全量行情数据执行 `BeanConvertUtil.convertList()` 创建 VO 副本
- `broadcastMarketUpdate()` 同步串行遍历所有 emitter 发送
- emitter 数量大或行情数据多时，瞬时堆占用飙升

---

#### M2. 无自定义线程池配置
**文件**: 全局未发现 `ThreadPoolTaskExecutor` 或 `@EnableAsync` 配置

- `@Scheduled` 默认使用单线程执行器
- 如果引入 `@Async` 或异步任务，默认无界线程池将导致线程泄漏

---

#### M3. JwtUtil 每次调用创建 SecretKey 对象
**文件**: `wealth-common/.../utils/JwtUtil.java:46-48`

```java
private SecretKey getSigningKey() {
    byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
    return Keys.hmacShaKeyFor(keyBytes);  // 每次创建新对象
}
```

- 高并发下反复分配 `byte[]` + `SecretKey`，给 GC 增加压力

---

#### M4. BeanConvertUtil 反射无缓存
**文件**: `wealth-common/.../utils/BeanConvertUtil.java:50-58`

- `copyNonNullProperties` 每次调用反射遍历类的全部属性描述符
- 高频 PUT 接口反复触发反射，大量短生命周期 `HashSet` + `String[]` 增加 Young GC 频率

---

### 【低风险】关注点

- 项目中不存在文件上传、递归调用、大 JSON/XML 解析、Bitmap/图片处理等常见 OOM 场景
- 无 ThreadLocal 使用，不存在 ThreadLocal 泄漏
- Token 刷新路径无长度校验，但 JWT 自身签名校验可拦截恶意 payload

---

### 排查要点（审查时逐项过）
- [ ] SSE emitter 集合是否设定了最大数量上限
- [ ] 全表 `selectList(null)` 是否改为分页或条件查询
- [ ] 所有 `list()` 端点是否添加上限保护或改为分页
- [ ] 行情模拟服务是否改为分批或条件查询
- [ ] 线程池是否自定义配置（含队列上限）
- [ ] JwtUtil SecretKey 是否缓存复用
- [ ] BeanConvertUtil 反射是否使用 PropertyDescriptor 缓存
