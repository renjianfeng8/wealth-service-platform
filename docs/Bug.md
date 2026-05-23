# Bug 记录文档

> 记录项目开发中遇到的关键问题及解决方案，供后续排查参考。

---

## 活跃问题

### Bug-014: 12 个 list() 端点缺少分页参数（已用 LIMIT 1000 兜底）

**日期**: 2026-05-23
**模块**: 全模块
**影响**: list() 接口始终返回最多 1000 条，前端无法分批加载

#### 现象

所有 Controller 的 `GET` 无参 `list()` 端点未提供 `pageNum`/`pageSize` 参数，内部通过 MyBatis-Plus `.last("LIMIT 1000")` 限制最大返回行数。前端只能拿到前 1000 条，无法翻页。

#### 涉及文件（12 个）

- `wealth-system/.../controller/UmsAdminController.java`
- `wealth-system/.../controller/UmsRoleController.java`
- `wealth-system/.../controller/UmsAdminRoleRelationController.java`
- `wealth-system/.../controller/UmsRoleResourceRelationController.java`
- `wealth-system/.../controller/UmsResourceController.java`
- `wealth-user/.../controller/UserController.java`
- `wealth-product/.../controller/ProductController.java`
- `wealth-product/.../controller/MarketDataController.java`
- `wealth-product/.../controller/UserFavoriteController.java`
- `wealth-trade/.../controller/TradeOrderController.java`
- `wealth-message/.../controller/NewsController.java`
- `wealth-message/.../controller/MessageController.java`

#### 备注

各模块均有对应的 `GET /page` 分页端点可用。当前前端未调用这些 `list()` 端点，实际影响有限。

---

### Bug-015: UserController.deleteBatch 缺少 @Valid 注解

**日期**: 2026-05-23
**模块**: wealth-user
**影响**: 违反 CLAUDE.md 规范，`@RequestBody` 无 `@Valid`

**文件**: `wealth-user/.../controller/UserController.java:99`
```java
public Result<Boolean> deleteBatch(@RequestBody List<Long> ids) {
```

#### 备注

`List<Long>` 为简单类型，无 Bean Validation 注解，实际无校验遗漏风险，但不符合规范一致性要求。

---

### Bug-016: UmsAdminServiceImpl.updateAdmin 存在死代码

**日期**: 2026-05-23
**模块**: wealth-system
**影响**: 无用代码，未使用 `copyNonNullProperties`

**文件**: `wealth-system/.../service/impl/UmsAdminServiceImpl.java:244`
```java
public Boolean updateAdmin(UmsAdmin admin) {
    admin.setPassword(null);
    return updateById(admin);
}
```

Controller 层已直接在调用处使用 `BeanConvertUtil.copyNonNullProperties(dto, existing)` + `updateById`，此 Service 方法未被任何代码调用。

---

## 历史已修复 Bug

### Bug-001: ES 搜索报 ConversionException（日期格式不匹配）

**日期**: 2026-05-12 | **状态**: 已修复（commit 不详）
**模块**: wealth-search
**影响**: ES 搜索接口返回 500

#### 根因

`ProductDocument.java` 中 `createTime` / `updateTime` 的 `@Field(type = FieldType.Date)` 未指定格式，ES 返回纯日期字符串（如 `"2026-05-10"`）无法转为 `LocalDateTime`。

#### 修复

显式指定 `format = DateFormat.date_hour_minute_second_millis`。

**验证**: `ProductDocument.java:46-50` 确认已包含 `format` 参数。

---

### Bug-002: wealth-search 启动失败（RedisSerializer NoClassDefFoundError）

**日期**: 2026-05-12 | **状态**: 已修复
**模块**: wealth-common / wealth-search
**影响**: 无 Redis 依赖的模块启动崩溃

#### 根因

`RedisConfig.java` / `RedisUtil.java` 在 wealth-common 中无条件加载，但 wealth-search 排除了 `spring-boot-starter-data-redis`。

#### 修复

添加 `@ConditionalOnClass(name = "org.springframework.data.redis.core.RedisTemplate")`。

**验证**: `RedisConfig.java:20` 和 `RedisUtil.java:11` 确认已标注。

---

### Bug-003: ES 索引数据为空（索引重建后未同步）

**日期**: 2026-05-12 | **状态**: 已记录（操作性问题）
**模块**: wealth-search / wealth-product

#### 说明

ES 索引重建后产品数据未自动同步。项目无自动同步机制，需手动通过 search 服务 save API 重新索引。操作步骤已记录在 Bug.md 历史中。

---

### Bug-004: 交易委托提交提示"用户信息异常"（userId 为 0）

**日期**: 2026-05-12 | **状态**: 已修复（commit 不详）
**模块**: front-user / wealth-user
**影响**: 登录后无法提交交易委托单

#### 根因

登录后前端通过 `getUserList()` 二次查询来获取 userId，该调用可能失败导致 userId 保持 0。

#### 修复

登录接口改为返回 `LoginVO { token, userId, nickname }`，前端直接从登录响应中获取 userId。

**验证**: `front-user/src/store/index.ts:20` 确认从 `res.data` 解构 `userId`。

---

### Bug-005: 交易委托分页筛选不生效（orderStatus 参数被忽略）

**日期**: 2026-05-12 | **状态**: 已修复（commit `6f7d0a44`）
**模块**: wealth-trade

#### 根因

`TradeOrderController.page()` 只接收 `pageNum`/`pageSize`，未声明 `orderStatus` 参数。

**验证**: `TradeOrderController.java:50-56` 确认已包含 `@RequestParam(required = false) Integer orderStatus`。

---

### Bug-006: 产品中心分类筛选不生效（productType 参数被忽略）

**日期**: 2026-05-12 | **状态**: 已修复（commit `d6603220`）
**模块**: wealth-product

**验证**: `ProductController.java:56-58` 确认已包含 `@RequestParam(required = false) Integer productType`。

---

### Bug-007: 财经资讯/消息中心分类筛选不生效（newsType/userId 参数被忽略）

**日期**: 2026-05-12 | **状态**: 已修复（commit `ed7b778c`）
**模块**: wealth-message

**验证**: `NewsController.java:53` 确认 `newsType`；`MessageController.java:53` 确认 `userId`。

---

### Bug-008: 停售产品仍可点击"去交易"跳转交易页

**日期**: 2026-05-12 | **状态**: 已修复（commit `c4eb96f3`）
**模块**: front-user

**验证**: `front-user/.../product/index.vue:117` 确认 `:disabled="detailItem?.status !== 1"`。

---

### Bug-009: Nacos Zipkin 配置属性不生效（zipkin.base-url 在 Spring Boot 3.x 中无效）

**日期**: 2026-05-17 | **状态**: 已修复
**模块**: Nacos 配置中心

#### 修复

将 `wealth-shared.yaml` 中的 `zipkin.base-url` 更正为 `management.zipkin.tracing.endpoint`。

**验证**: 本地代码中无 `zipkin.base-url` 引用。Nacos 配置需单独确认。

---

### Bug-010: docker-compose YAML 锚点语法错误

**日期**: 2026-05-17 | **状态**: 已修复
**模块**: docker-compose.yml

**验证**: 无 `x-image-prefix` 或 `*image-prefix`，全部使用完整镜像名。

---

### Bug-011: Nginx 启动时上游 DNS 解析失败导致 crash

**日期**: 2026-05-17 | **状态**: 已修复
**模块**: nginx.conf

**验证**: `nginx.conf:12-14` 确认包含 `resolver 127.0.0.11` + `set $gateway_upstream` 变量化解析。

---

### Bug-012: Alpine MariaDB 客户端连接 MySQL 8 失败（SSL + 认证插件兼容性）

**日期**: 2026-05-17 | **状态**: 已修复
**模块**: scripts/backup-scheduler.sh

**验证**: `backup-scheduler.sh:26` 确认包含 `--ssl=0`。

---

### Bug-013: Redis 配置在 Docker 容器内被忽略，始终连接 localhost:6379

**日期**: 2026-05-22 | **状态**: 已查明根因并修复（2026-05-23）
**模块**: wealth-common（`RedisConfig`）

#### 根因

`RedisConfig.java` 使用 `@Configuration` 并被注册在
`AutoConfiguration.imports` 中，作为 auto-configuration 加载。但它未声明对
`RedisAutoConfiguration` 的依赖顺序，导致 Spring Boot 处理 auto-configuration
时 **`RedisConfig` 可能在 `RedisAutoConfiguration` 之前执行**。

当 `RedisConfig.redisTemplate()` 创建 `@Primary RedisTemplate` 时，需要注入
`RedisConnectionFactory`，这会触发 `LettuceConnectionConfiguration` 提前初始化
连接工厂，而此时 `RedisAutoConfiguration` 尚未处理，
`@EnableConfigurationProperties(RedisProperties.class)` 未生效，
`RedisProperties` 未被绑定，始终使用默认值 `host=localhost`。

#### 修复（2026-05-23）

1. **根因修复**: `RedisConfig.java` 改为 `@AutoConfiguration(after = RedisAutoConfiguration.class)`，
   确保在 Spring Boot 的 Redis 自动配置完成（`RedisProperties` 已绑定，
   `RedisConnectionFactory` 已正确创建）之后再初始化自定义 `RedisTemplate`。
2. **防御性代码保留**: `PermissionInterceptor` 的 `try-catch(DataAccessException)`
   和 `AntiReplayAspect` 的 `ObjectProvider<RedisUtil>` 降级作为安全网保留。

**验证**: `RedisConfig.java:29` 确认包含 `@AutoConfiguration(after = RedisAutoConfiguration.class)`。

---

## OOM 风险审查（2026-05-21 审计，状态更新 2026-05-23）

### 【高风险 H1-H3】✅ 全部已修复

H1（SSE Emitter 无界增长）、H2（全表 selectList(null)）、H3（全 Controller list 无分页）已在 commit `f23d22a7` 及后续提交中修复。`list()` 端点已加 `LIMIT 1000` 保护。

### 【中风险 M1-M4】

| 编号 | 问题 | 状态 |
|------|------|------|
| M1 | MarketDataSimulation 全量 selectList(null) + 每2秒全量 VO 转换 | ⚠️ **未修复** — 数据已缓存到 `cachedMarketData`，但仍每 2 秒执行全量 `BeanConvertUtil.convertList`。演示规模无风险 |
| M2 | 无自定义线程池 | ✅ **已修复** — `AsyncConfig` 存在：core=4, max=8, queue=200 |
| M3 | JwtUtil 每次创建 SecretKey | ✅ **已修复** — `cachedSigningKey` 在 `@PostConstruct` 中缓存 |
| M4 | BeanConvertUtil 反射无缓存 | ✅ **已修复** — `NULL_PROPERTY_CACHE`（ConcurrentHashMap） |

### 排查要点清单（已更新）

- [x] SSE emitter 集合是否设定了最大数量上限
- [x] 全表 `selectList(null)` 是否改为分页或条件查询
- [x] 所有 `list()` 端点是否添加上限保护或改为分页
- [x] 行情模拟服务是否改为分批或条件查询
- [x] 线程池是否自定义配置（含队列上限）
- [x] JwtUtil SecretKey 是否缓存复用
- [x] BeanConvertUtil 反射是否使用 PropertyDescriptor 缓存
- [ ] ES 查询是否报 `ConversionException` → Bug-001 已修复
- [ ] 无 Redis 依赖的模块启动是否报 `NoClassDefFoundError: RedisSerializer` → Bug-002 已修复
- [ ] IK 分词器是否生效 → 检查 ES mapping
- [ ] 前端"用户信息异常" → Bug-004 已修复
- [ ] 配置修改需审查：`management.zipkin.tracing.endpoint` 而非 `zipkin.base-url`
- [ ] Redis 相关 500 错误 → Bug-013 已降级
