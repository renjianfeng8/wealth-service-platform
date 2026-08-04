
# Bug 记录

> 记录项目开发中遇到的关键问题及解决方案，供后续排查参考。
> 此文档与 [CLAUDE.md](../.claude/CLAUDE.md#十一项目健康检查规则强制遵守) 的健康检查清单配合使用。

---

## 活跃问题

### Bug-017: 管理员个人信息页 API 加载失败时表单离开守卫误判

**日期**: 2026-07-24
**模块**: front（admin/profile/index.vue）
**影响**: 后端接口异常时，`useFormGuard` 的 `reset()` 在 `try` 块内未被调用，username 被 store 前端赋值后与空快照不一致，`isDirty()` 返回 true，未修改字段直接导航仍弹出确认框

#### 现象
1. `fetchProfile()` 中 `adminInfo.username = userStore.username` 在 `try` 前执行
2. API 调用进入 `catch` 块，`reset()` 未执行
3. 快照仍为 composable 初始化时的空值 `{username:'', nickName:'', email:''}`
4. 当前 adminInfo 的 `username` 不为空 → `isDirty()=true` → 导航拦截误弹窗

#### 改进方向
将 `reset()` 移至 `finally` 块，无论成功/失败均以最终状态更新快照

---

### Bug-018: 交易页幂等键参数改回原值后 hash 重算

**日期**: 2026-07-24
**模块**: front（trade/index.vue）
**影响**: 极低频边缘场景，用户提交失败→修改参数→改回原值→再次提交，幂等键与第一次不同，后端视作新请求

#### 现象
1. 提交 A（hash-A）→ 失败 → key 缓存 `lastOrderHash=hash-A`
2. 用户改参数 → hash-B → `lastOrderHash=hash-B`
3. 用户改回原参数 → hash-A（≠ lastOrderHash=hash-B）→ 生成新 key
4. 若第一次提交实际成功（仅网络超时），第二次产生重复订单

#### 改进方向
引入更稳定的去重策略（如服务端基于业务参数的幂等判断），前端侧影响可控

---

### Bug-019: 自选页产品分批加载 API 错误时降级不完整

**日期**: 2026-07-24
**模块**: front（favorite/index.vue）
**影响**: `enrichFavorites` 分批产品过程中某页 API 出错，partial data 仍被使用，部分产品名称回退为 productCode

#### 现象
`while(true)` 循环中 `catch { break }`，已加载的部分数据保留，未加载的产品名显示为编码

#### 改进方向
可增加重试机制或标记降级状态 UI 提示

---

### Bug-020: KeepAlive exclude 残留 defineOptions

**日期**: 2026-07-24
**模块**: front（login/index.vue、register/index.vue）
**影响**: 无害残留。App.vue 已切换为路由 meta 动态 key 方案，`defineOptions({ name: 'LoginPage' })` 不再被 KeepAlive 使用

#### 现象
`LoginPage`/`RegisterPage` 组件名称声明仅对 Vue DevTools 有辅助作用，无功能影响

#### 改进方向
下阶段可移除，或保留作为 DevTools 调试标识

---

### Bug-021: list() 接口未提供分页参数，固定返回 1000 条

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
**文件**: `scripts/db/backup-scheduler.sh:26` 确认 `--ssl=0`。

---

### Bug-011: Nginx 启动时上游 DNS 解析失败导致 crash

**日期**: 2026-05-17 | **状态**: 已修复
**文件**: `deploy/nginx/nginx.conf:12-14` 确认 `resolver 127.0.0.11` + `set $gateway_upstream`。

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

---

## 代码规范审计问题（2026-08-04）

> 依据 `docs/CODE-STANDARDS.md`（V2）全量审计（187 Java + 77 前端 + init.sql）。
> 全部问题**待整改**；修复指引指向规范手册对应章节。

### 高危（安全 / 资金 / 数据正确性 / 功能停摆，优先整改）

| # | 位置 | 问题 | 状态 | 修复指引 |
|---|------|------|------|---------|
| H1 | `wealth-service/application.yml:48`、`wealth-gateway/application.yml:6`、`JwtUtil.java:26-27` | JWT secret 提供可提交仓库的默认值 `${JWT_SECRET:change-me-to-a-random-256-bit-key}`，生产漏配即用已知密钥，Token 可被伪造 | 待整改 | CODE-STANDARDS §17.1 |
| H2 | `front/src/utils/auth.ts:52-61` | `refresh_token` 明文存 sessionStorage（`wealth_refresh`），XSS 可窃取续期凭证 | 待整改 | §17.2 |
| H3 | `TradeOrderServiceImpl.java:145-161` | 幂等校验「查 Redis → 写库」非原子（check-then-set），并发双击重复下单 | 待整改 | §8.3 |
| H4 | `TradeOrderServiceImpl.java:83-85` | `createOrder` 事务内先写 Redis 幂等键，DB 回滚后键残留，同 key 重试被误拦 | 待整改 | §16.3 |
| H5 | `TradeOrderServiceImpl.java:114-133`、`BaseBizServiceImpl.java:100-108`、`UserServiceImpl.java:180-187`、`UmsAdminCrudServiceImpl.java:57-64` | read-modify-write 整行 `updateById` 无乐观锁/条件更新，并发互相覆盖 | 待整改 | §13.8 |
| H6 | `UmsAdminCrudServiceImpl.java:48` | `password` 无 `@NotBlank`，创建时不传 → `passwordEncoder.encode(null)` NPE | 待整改 | §14.1 |
| H7 | `MarketDataSimulationService.java:56` | 声明 `@Scheduled` 但全库无 `@EnableScheduling`，行情模拟/SSE 广播永不执行 | 待整改 | §8.5 |
| H8 | `MarketDataSimulationService.java:38` | `volatile List` 就地修改元素，调度线程写 / SSE 线程读 → 数据竞争 | 待整改 | §8.1 |
| H9 | `init.sql`（8 张表） | `wea_market_data/wea_user_favorite/wea_news/wea_message/ums_role/ums_resource/ums_admin_role_relation/ums_role_resource_relation` 缺 `update_time` 列 | 待整改 | §13.2 |
| H10 | `init.sql:168-184` | `ums_admin_role_relation` 按 admin_id 查、`ums_role_resource_relation` 按 role_id IN 查，两表仅主键无查询索引 | 待整改 | §13.3 |
| H11 | `UserController.java:47-62` | `/user/{id}`、`/user/page` 仅登录级保护，无 RBAC/归属校验（IDOR 越权） | 待整改 | §17.3 |
| H12 | `App.vue:9-11` + `views/market:156-163`、`views/dashboard:412-421`、`views/favorite:244-253`、`views/admin/market:236-243` | KeepAlive 缓存页只在 `onMounted` 订阅 SSE / `onUnmounted` 退订，缓存下退订永不执行 → handler 累积；且全库无 `onActivated` 刷新，返回后展示过期数据 | 待整改 | §20.6 |

### 中风险（规范严重偏离，逐步整改）

| # | 位置 | 问题 | 状态 | 修复指引 |
|---|------|------|------|---------|
| M1 | 全 Service/Controller | 魔法值 14 处（tradeType 1/2、msgType 2、TTL 7天×5、锁 30s、LIMIT 1000/100000、days 30/7、multiply 30、period 字符串） | 待整改 | §5.1 |
| M2 | 全项目 | `enums` 包缺失，5 处状态/类型裸数字判断 | 待整改 | §5.2 |
| M3 | `UserController.java:91,119-123`、`UmsAdminController.java:53` | Controller 分层越界（new Entity、直接 removeByIds、ms→s 换算） | 待整改 | §2.2 |
| M4 | `UmsAdminController.java:49` 等 | 登录/刷新返回 `ResponseEntity<Result>`，其余 `Result`，返回类型不统一 | 待整改 | §2.3 |
| M5 | `UserController.java:114`、system 域 Controller | 驼峰 URL（`/resetPassword`、`/system/umsAdmin`） | 待整改 | §3.3 |
| M6 | system 域 5 处 | `list(pageNum,pageSize)` 返回裸 List，语义混乱 | 待整改 | §3.2 |
| M7 | 全 Service 层 + `ResultCode.java` | `ServiceException(400/401/404/429/500, ...)` 魔法错误码散落，ResultCode 仅 4 个枚举值 | 待整改 | §11.3 |
| M8 | `GlobalExceptionHandler.java:31-103` | HTTP 恒 200 无 `@ResponseStatus`；校验失败把字段名拼回显客户端 | 待整改 | §11.2、§11.5 |
| M9 | `UmsAdminAuthServiceImpl.java:115-117`、`UmsAdminCrudServiceImpl.java:51-52` | catch 吞原始异常丢堆栈 | 待整改 | §11.4 |
| M10 | `AuditLogAspect.java:117` | 脱敏正则只匹配 `"token"`，accessToken 不脱敏且会损坏 JSON | 待整改 | §12.6 |
| M11 | `AntiReplayAspect.java:47-56,79` | nonce 原值拼 key 无哈希；Redis 不可用/未带头静默放行 | 待整改 | §15.3、§15.4、§17.6 |
| M12 | `CaptchaServiceImpl.java:47-50`、`UserServiceImpl.java:56-58,88-90` | 验证码在 Redis 不可用/未带 key 时跳过，可绕过 | 待整改 | §14.4 |
| M13 | `CookieUtil.java:20-26` | Token Cookie 缺 `Secure`/`SameSite` | 待整改 | §17.4 |
| M14 | `LoginInterceptor.java:39` + `JwtAuthGlobalFilter.java:68` | 双次验签解析，逻辑易漂移 | 待整改 | §17.4 |
| M15 | `AuthConstant.java:72`、prod yml | `/actuator/**` 放行任意登录用户；prod 暴露 prometheus | 待整改 | §17.5 |
| M16 | `application-prod.yml`（gateway） | `knife4j.gateway.enabled` 生产未关闭 | 待整改 | §17.5 |
| M17 | `UmsAdminDTO.java:32` vs `User.java` | `nickName` vs `nickname` 同义字段命名分裂 | 待整改 | §3.1 |
| M18 | `MarketDataMapper.java:26`、`MarketDataServiceImpl.java:77-87` | `SELECT *` 全列；`LIMIT 100000` 全量进内存 | 待整改 | §13.5、§13.6 |
| M19 | `MarketDataSimulationService.java:80-109`、`UmsRoleResourceRelationServiceImpl.java:87-93` | 循环内逐条 DB 更新 / 循环内逐个清缓存 | 待整改 | §7.2 |
| M20 | `AsyncConfig.java`、`application.yml:39-44` | `@EnableAsync`+线程池定义但无 `@Async` 使用，且与 `spring.task.execution.pool` 冲突 | 待整改 | §8.4 |
| M21 | `WeaUserFavorite.java:31-33` | `@TableField(exist=false)` 覆盖 `@TableLogic` 退回物理删除，与其余 11 表混用 | 待整改 | §13.9 |
| M22 | `UmsRoleServiceImpl.java:48-51`、`UmsAdminCrudServiceImpl.java:67-70` | 删除主数据不清理 relation 关联，孤儿数据 | 待整改 | §13.10 |
| M23 | `MarketDataMapper/TradeOrderMapper` 注解 SQL | 手写 SQL 需显式 `del_flag = 0`，漏写即查回已删数据 | 待整改 | §13.12 |
| M24 | `BaseEntity.java:19,28-30` | 主键 `IdType.AUTO`；`delFlag` 实体无默认值 | 待整改 | §13.13 |
| M25 | `UserController.java:94-100`、`UmsAdminResetPasswordDTO.java:20-22` | 注册/重置密码仅 `@NotBlank`，无强度校验 | 待整改 | §14.3 |
| M26 | 前端 `api/index.ts:87`、`types/index.ts:1,7,27`、`useAdminDashboard.ts:40-42`、`profile/index.vue:232,286-287` | 拦截器 `Promise<any>`、默认泛型 `any`、`Record<string,any>`、`as any` 断言散落 | 待整改 | §19.6 |
| M27 | 前端 `api/system.ts:18-22`、`api/index.ts:51` | 裸 axios 绕过封装（防递归注释但散落） | 待整改 | §19.7 |
| M28 | 前端 `views/trade/index.vue:357-358`、views/message/favorite/profile | 双重 toast、console 打印完整 err | 待整改 | §19.8 |
| M29 | 前端 `views/admin/message/index.vue:46-50`、`views/admin/trade/index.vue:210-237` | userId 无校验可提交 undefined、form.id 透传创建接口 | 待整改 | §14.5 |
| M30 | 前端 `api/trade.ts:56`、多视图 | `{ orderStatus: 3 }`、`status===1/0` 魔法数散落 | 待整改 | §5.5 |
| M31 | `JwtConfigContractTest.java:57,84`、`IdentifyLoginTest.java:108-109` | 测试加载真实 application.yml/prod；spy 部分 mock 被测对象 | 待整改 | §19.2、§19.4 |
| M32 | `UnifiedSentinelConfig.java:23-34` | 双大括号初始化反模式 | 待整改 | §18.4 |
| M33 | gateway `application.yml:61,68,76`、service prod:21 | CORS `allowed-headers:*`+credentials、zipkin localhost 硬编码 | 待整改 | §17.1 |

### 低风险（正确性/一致性隐患）

| # | 位置 | 问题 | 状态 | 修复指引 |
|---|------|------|------|---------|
| L1 | system 域 Controller | `list` 方法名语义误导、`@Tag` 缺 description、`@Operation` summary 过长含实现细节 | 待整改 | §3.2、§10.2 |
| L2 | 大部分 Controller | 缺类级 JavaDoc | 待整改 | §10.1 |
| L3 | `ProductController.java:52-53` | `orderBy/orderDir` 无白名单校验 | 待整改 | §14.2 |
| L4 | `BeanConvertUtil.java:47-59` | 反射拷贝无 PropertyDescriptor 缓存 | 待整改 | §18.3 |
| L5 | `SentinelGatewayConfig.java:23-28` | 路由 QPS 魔法值无注释 | 待整改 | §10.4 |
| L6 | `JwtUtil.java:58-66,151-158` | Token 无 issuer/audience，refresh 与 access 同密钥同算法 | 待整改 | §17.4 |
| L7 | 前端 `views/market/index.vue:26-92`、`views/admin/dashboard/index.vue:32-43` | 模板缩进错乱；props 传函数而非 import | 待整改 | §4.5、§18.5 |
| L8 | 前端 dashboard/home/admin-dashboard | formatTime/formatNumber/状态映射重复三份 | 待整改 | §18.1 |
| L9 | 前端 `views/profile`、`views/admin/profile` | 密码强度逻辑 ~40 行重复两份 | 待整改 | §18.1 |
| L10 | 前端 `views/trade/index.vue:236-253`、`views/admin/trade/index.vue:199-208` | 幂等键 resolve 逻辑复制两份 | 待整改 | §18.1 |
| L11 | `UserServiceImplTest.java:158,216`、`UmsAdminAuthServiceImplTest.java:96`、`UmsAdminControllerTest.java:41` | 硬编码口令/Token 串断言 | 待整改 | §19.2 |
| L12 | `init.sql` | `del_flag` 类型口径不一（ums_* INT vs sys_user/wea_* TINYINT） | 待整改 | §13.14 |
| L13 | `MarketDataSimulationService.java:90-92` | 金额中间计算经 `double`（changeFactor） | 待整改 | §13.14 |
| L14 | `DashboardServiceImpl.java:124-140` | 资产指数累乘仅末次 setScale，舍入误差未控 | 待整改 | §13.14 |

### 风格优化项（一致性 / 可读性）

| # | 位置 | 项 |
|---|------|----|
| S1 | 全库 | 统一 K&R 大括号、120 字符换行、类/方法间空行（§4） |
| S2 | `useCrudPage.ts:56-62` | 未用参数 `_row/_column` 简化 formatter 签名（§18.5） |
| S3 | `utils/auth.ts:10,34` | 前端 30 分钟过期标记与后端 `jwt.access-expire` 硬编码对齐，属脆弱契约，建议由后端下发（§17.4） |
| S4 | `store/index.ts:23,38` | `role` 存 sessionStorage 与 Pinia 双轨，建议收敛单一路径（§19.6） |
| S5 | `views/auth/login/index.vue:137-144` | `route.query.redirect` 未校验 `startsWith('/')`（§17.7） |
| S6 | 前端组件 | 通用表格组件建议泛型化 `AdminDataTable<T>`（§19.6） |
