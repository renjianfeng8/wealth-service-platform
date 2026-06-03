
# Bug 记录

> 记录项目开发中遇到的关键问题及解决方案，供后续排查参考。
> 此文档与 [CLAUDE.md](../CLAUDE.md#十一项目健康检查规则强制遵守) 的健康检查清单配合使用。

---

## 活跃问题

### Bug-014: 12 个 list() 端点缺少分页参数（已用 LIMIT 1000 兜底）

**日期**: 2026-05-23
**模块**: 全模块
**影响**: list() 接口始终返回最多 1000 条，前端无法分批加载

#### 现象

所有 Controller 的 `GET` 无参 `list()` 端点未提供 `pageNum`/`pageSize` 参数，内部通过 MyBatis-Plus `page(new Page<>(1, 1000))` 限制最大返回行数。前端只能拿到前 1000 条，无法翻页。

#### 涉及文件（12 个）

- `UmsAdminController.java`、`UmsRoleController.java`、`UmsAdminRoleRelationController.java`
- `UmsRoleResourceRelationController.java`、`UmsResourceController.java`
- `UserController.java`、`ProductController.java`、`MarketDataController.java`
- `UserFavoriteController.java`、`TradeOrderController.java`
- `NewsController.java`、`MessageController.java`

#### 备注

各模块均有对应的 `GET /page` 分页端点可用。当前前端未调用这些 `list()` 端点，实际影响有限。

---

## 已修复

### Bug-015: UserController.deleteBatch 缺少 @Valid 注解

**日期**: 2026-05-23
**状态**: 已修复（当前代码已为 `@Valid @NotEmpty @RequestBody`）
**模块**: wealth-service（user 域）
**影响**: 违反 CLAUDE.md 规范，`@RequestBody` 无 `@Valid`

**文件**: `UserController.java:99`
```java
public Result<Boolean> deleteBatch(@Valid @NotEmpty(message = "ID列表不能为空") @RequestBody List<Long> ids) {
```

`List<Long>` 已补齐 `@Valid` 与 `@NotEmpty`，符合当前规范一致性要求。

---

### Bug-016: UmsAdminServiceImpl.updateAdmin 存在死代码

**日期**: 2026-05-23
**状态**: 已修复（2026-05-26, commit `dc5e7a31`）
**模块**: wealth-service（system 域）

未使用的 `updateAdmin()` 方法及对应测试已删除。Controller 层使用 `BeanConvertUtil.copyNonNullProperties(dto, existing)` + `updateById`。

---

### Bug-013: Redis 配置在 Docker 容器内被忽略，始终连接 localhost:6379

**日期**: 2026-05-22 | **状态**: 已修复（2026-05-23）
**模块**: wealth-common（`RedisConfig`）

**根因**: `RedisConfig` 使用 `@Configuration` 并通过 `AutoConfiguration.imports` 注册，但未声明对 `RedisAutoConfiguration` 的依赖顺序，导致 Lettuce 连接工厂在 `RedisProperties` 绑定前初始化。

**修复**: 改为 `@AutoConfiguration(after = RedisAutoConfiguration.class)`，确保 `RedisProperties` 已绑定后再初始化自定义 `RedisTemplate`。

**验证**: `RedisConfig.java:29` 确认 `@AutoConfiguration(after = RedisAutoConfiguration.class)`。

---

### Bug-012: Alpine MariaDB 客户端连接 MySQL 8 失败

**日期**: 2026-05-17 | **状态**: 已修复
**文件**: `scripts/backup-scheduler.sh:26` 确认 `--ssl=0`。

---

### Bug-011: Nginx 启动时上游 DNS 解析失败导致 crash

**日期**: 2026-05-17 | **状态**: 已修复
**文件**: `nginx.conf:12-14` 确认 `resolver 127.0.0.11` + `set $gateway_upstream`。

---

### Bug-010: docker-compose YAML 锚点语法错误

**日期**: 2026-05-17 | **状态**: 已修复
全部使用完整镜像名，无 `x-image-prefix` 锚点引用。

---

### Bug-009: Nacos Zipkin 配置属性不生效（zipkin.base-url 在 Spring Boot 3.x 中无效）

**日期**: 2026-05-17 | **状态**: 已修复
`zipkin.base-url` → `management.zipkin.tracing.endpoint`。

---

### Bug-008: 停售产品仍可点击"去交易"跳转交易页

**日期**: 2026-05-12 | **状态**: 已修复（commit `c4eb96f3`）
添加 `:disabled="detailItem?.status !== 1"` 禁用条件。

---

### Bug-007: 财经资讯/消息中心分类筛选不生效

**日期**: 2026-05-12 | **状态**: 已修复（commit `ed7b778c`）
`NewsController.java:53` 确认 `newsType`；`MessageController.java:53` 确认 `userId`。

---

### Bug-006: 产品中心分类筛选不生效（productType 参数被忽略）

**日期**: 2026-05-12 | **状态**: 已修复（commit `d6603220`）
`ProductController.java:56-58` 确认 `@RequestParam(required = false) Integer productType`。

---

### Bug-005: 交易委托分页筛选不生效（orderStatus 参数被忽略）

**日期**: 2026-05-12 | **状态**: 已修复（commit `6f7d0a44`）
`TradeOrderController.java:50-56` 确认包含 `@RequestParam(required = false) Integer orderStatus`。

---

### Bug-004: 交易委托提交提示"用户信息异常"（userId 为 0）

**日期**: 2026-05-12 | **状态**: 已修复
登录接口改为返回 `LoginVO { token, userId, nickname }`，前端直接从登录响应中获取 userId。

---

### Bug-003: ES 索引数据为空（索引重建后未同步）

**日期**: 2026-05-12 | **状态**: 已记录（操作性问题）
需手动通过 search 服务 save API 重新索引。

---

### Bug-002: RedisSerializer NoClassDefFoundError（无 Redis 依赖模块启动崩溃）

**日期**: 2026-05-12 | **状态**: 已修复
`@ConditionalOnClass(name = "org.springframework.data.redis.core.RedisTemplate")`。

---

### Bug-001: ES 搜索报 ConversionException（日期格式不匹配）

**日期**: 2026-05-12 | **状态**: 已修复
显式指定 `format = DateFormat.date_hour_minute_second_millis`。

---

## OOM 风险审查（2026-05-21 审计）

### 高风险（H1-H3）✅ 全部已修复

| 编号 | 问题 | 状态 |
|------|------|------|
| H1 | SSE Emitter 无界增长 | 已修复 |
| H2 | 全表 selectList(null) | 已修复 |
| H3 | 全 Controller list 无分页 | 已修复（LIMIT 1000） |

### 中风险（M1-M4）

| 编号 | 问题 | 状态 |
|------|------|------|
| M1 | MarketDataSimulation 全量查询 + 每 2 秒全量 VO 转换 | ⚠️ 数据已缓存，演示规模无风险 |
| M2 | 无自定义线程池 | ✅ 已修复（AsyncConfig: core=4, max=8, queue=200）|
| M3 | JwtUtil 每次创建 SecretKey | ✅ 已修复（`@PostConstruct` 缓存）|
| M4 | BeanConvertUtil 反射无缓存 | ✅ 已修复（ConcurrentHashMap 缓存）|
