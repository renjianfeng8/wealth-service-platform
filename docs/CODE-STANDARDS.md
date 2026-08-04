# 理财服务平台 · 代码规范手册（V2）

> 以《阿里巴巴 Java 开发手册（泰山版）》、字节跳动后端编码规范、大厂安全编码标准为基准，结合本项目（金融级理财服务平台）业务场景定制。
> 本手册为纯规范约束，不收录具体缺陷案例；审计发现的问题统一记录于 [BUG.md](BUG.md) 的「代码规范审计问题」章节。
> 每条规约附错误示例 / 正确示例，并标注执行等级。

---

## 目录

1. [前言（规范基准与适用范围）](#1-前言规范基准与适用范围)
2. [工程分包与分层架构规约](#2-工程分包与分层架构规约)
3. [命名规范](#3-命名规范)
4. [代码格式化与排版规范](#4-代码格式化与排版规范)
5. [常量、枚举与魔法数字处理规范](#5-常量枚举与魔法数字处理规范)
6. [OOP 面向对象开发规约](#6-oop-面向对象开发规约)
7. [集合与数组编码规范](#7-集合与数组编码规范)
8. [并发、线程与异步线程池规范](#8-并发线程与异步线程池规范)
9. [分支循环与条件判断编码规范](#9-分支循环与条件判断编码规范)
10. [注释标准与废弃代码清理](#10-注释标准与废弃代码清理)
11. [全局异常处理与自定义异常规约](#11-全局异常处理与自定义异常规约)
12. [日志强制规范](#12-日志强制规范)
13. [MySQL、MyBatis-Mapper 与 SQL 编写规范](#13-mysqlmybatis-mapper-与-sql-编写规范)
14. [参数校验与 DTO 校验规范](#14-参数校验与-dto-校验规范)
15. [Redis 与缓存使用规范](#15-redis-与缓存使用规范)
16. [事务使用规范与陷阱禁令](#16-事务使用规范与陷阱禁令)
17. [安全编码规约](#17-安全编码规约)
18. [重复代码治理与工具类抽取规范](#18-重复代码治理与工具类抽取规范)
19. [单元测试与接口开发规范](#19-单元测试与接口开发规范)
20. [新增强制检查清单](#20-新增强制检查清单)

> **等级定义**
>
> | 等级 | 含义 | 违例后果 |
> |------|------|---------|
> | 【强制禁止】 | 出现即红线事故 | 资金/安全/数据正确性风险，必须整改 |
> | 【必须遵守】 | 所有新代码必须符合 | 规范偏离，Code Review 直接打回 |
> | 【推荐优化】 | 鼓励按此落地 | 可读性/性能/可维护性提升 |
> | 【风格建议】 | 团队统一风格 | 一致性要求，不强制 |

---

## 1. 前言（规范基准与适用范围）

### 1.1 规范基准

本手册以以下规范为基准，结合本项目（金融级理财服务平台）业务特性定制：

- 《阿里巴巴 Java 开发手册（泰山版）》全章节
- 字节跳动后端编码规范（分层隔离、接口幂等、参数校验、结构化日志、敏感数据脱敏、异常包装）
- OWASP Top 10 与金融级安全编码标准（本项目为资金/个人信息敏感平台）

### 1.2 适用范围与优先级

- 适用范围：`backend/**` 全部 Java 代码、`front/src/**` TypeScript/Vue 代码、`init.sql` 与 Mapper SQL、`application*.yml` 配置。
- 优先级：本手册 < 项目根 `.claude/CLAUDE.md`（红线规则）< 用户明确指令。
- **规约只有与本项目现状绑定才有约束力**：凡本手册未覆盖之处，以阿里泰山版默认条款为准。

### 1.3 手册定位

本手册为项目**唯一**代码规范手册，取代历史 CODE-STANDARDS 版本。审计发现的具体问题与整改状态见 `docs/BUG.md`，不在此重复。

---

## 2. 工程分包与分层架构规约

### 2.1 强制单向依赖链

**【强制禁止】** 分层反向/越层调用。

```
Controller → Service → Mapper → DB
              ↑          ↓
        DTO/VO 转换      SQL/XML
```

| # | 禁止行为 | 违例表现 |
|---|---------|---------|
| 1 | Controller 组装实体、调用 Mapper、写业务逻辑 | Controller 内 `new Entity()`、直接查库、操作 Redis |
| 2 | Service 处理 HTTP 响应（返回 Result/ResponseEntity、读写 Cookie） | Service 方法返回 `Result<T>` |
| 3 | 跨域直接操作他域 Mapper | user 域直接调 product 域 Mapper |
| 4 | Service 反向注入 Controller | Service 内 `@Autowired` Controller |

**正确示例** — Controller 只做三件事（校验、路由、包装）：

```java
// ❌ 禁止：手工组装实体，业务散落 Controller
@PostMapping("/reset-password")
public Result<Boolean> resetPassword(@RequestBody ResetPasswordDTO dto) {
    User user = new User();
    user.setId(dto.getId());
    user.setPassword(dto.getPassword());   // 密码逻辑应在 Service
    return Result.success(userService.resetPassword(user, dto.getOldPassword()));
}

// ✅ 正确：Controller 透传 DTO，实体组装与校验下沉 Service
@PostMapping("/reset-password")
public Result<Boolean> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
    return Result.success(userService.resetPassword(dto));
}
```

### 2.2 Controller 职责边界

**【必须遵守】** Controller 方法内禁止出现：Token 解析、权限判断、金额计算、单位换算、直接操作 Redis/Mapper、new Entity 组装。

```java
// ❌ 禁止：单位换算写在 Controller
public Result<LoginVO> login(LoginDTO dto) {
    ResponseCookie cookie = CookieUtil.buildTokenCookie(pair.accessToken(), pair.expiresIn() / 1000);
    ...
}

// ✅ 正确：单位换算/业务计算下沉 Service，Controller 只路由
public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
    return Result.success(authService.login(dto));
}
```

### 2.3 统一返回类型

**【必须遵守】** 所有接口统一返回 `Result<T>`，禁止混用 `ResponseEntity<Result<T>>` 与其他包装。

```java
// ❌ 禁止：返回 ResponseEntity 包装
public ResponseEntity<Result<TokenPair>> login(LoginDTO dto)

// ✅ 正确：全项目统一 Result<T>；HTTP 状态码由 GlobalExceptionHandler 统一映射
public Result<TokenPair> login(@Valid @RequestBody LoginDTO dto) {
    return Result.success(authService.login(dto));
}
```

### 2.4 Service 层职责

**【必须遵守】**
- Service 承载全部业务逻辑、事务控制、跨域 Contract 调用。
- Service 不处理 HTTP 相关对象（`HttpServletRequest`、Cookie、`@RequestHeader`），不返回 `Result`。
- `getById` 空值必须抛 `ServiceException`（404），禁止返回 `null` 冒充错误或让上层 NPE。

```java
// ✅ 正确
@Override
public TradeOrderVO getById(Long id) {
    TradeOrder entity = baseMapper.selectById(id);
    if (entity == null) {
        throw new ServiceException(ResultCode.NOT_FOUND, "交易委托不存在");
    }
    return BeanConvertUtil.convert(entity, TradeOrderVO.class);
}
```

### 2.5 跨域复用走 Contract 接口

**【必须遵守】** 跨业务域（user/product/trade/message/system）复用必须通过 `wealth-common` 定义的 Contract 接口（如 `MessageFeignDTO` / `MessageContract`），由 wealth-service 注入实现。禁止跨域直接注入他域 Mapper 或 Service 实现类。

### 2.6 实体/枚举/常量归属

**【强制禁止】** 实体只能由 Service 层构造并填充；Controller 禁止 `new Entity` 传给 Service。常量与枚举按业务域归位：

```
com.wealth.platform.{domain}
├── controller / service / mapper / entity
├── vo / dto
├── config / constant / enums
└── converter(推荐新增)
```

状态与业务类型必须放 `enums` 包（见 §5.2），禁止散落各包。

### 2.7 避免「上帝 Controller」

**【推荐优化】** 超过 300 行 / 超过 8 个方法的 Controller 拆分；关联密集的后台管理接口按职责拆独立 Controller，禁止把一类后台管理全塞进一个类。

---

## 3. 命名规范

### 3.1 Java 标识符

**【必须遵守】**

| 元素 | 规则 | 反例 |
|------|------|------|
| 类名 | 大驼峰 `PascalCase` | `user_service` |
| 方法/变量 | 小驼峰 `camelCase` | `User_name`、`nickName` |
| 常量 | `UPPER_SNAKE_CASE` | `maxLoginAttempts` |
| 包名 | 全小写 | `Com.wealth.platform` |
| 布尔字段 | `isXxx`/`Xxx` 风格统一，禁止无意义 `flag` | `readFlag` 建议 `read`/`isRead` |

**【必须遵守】** 同义字段全项目命名必须一致，禁止同一含义字段在多个类中大小写/拼写不同（如 `nickName` 与 `nickname` 并存），否则 Bean 复制与接口字段极易写错。

```java
// ❌ 禁止：同一含义字段命名分裂
private String nickName;   // 类 A
private String nickname;   // 类 B

// ✅ 正确：全项目统一
private String nickname;
```

### 3.2 方法命名语义

**【必须遵守】** 方法名必须精确表达语义，禁止 `list` 却返回分页单页、禁止 `get` 返回集合、禁止 `find` 做写操作。

```java
// ❌ 禁止：方法名 list，入参分页，返回裸 List
public List<UmsAdminVO> list(Integer pageNum, Integer pageSize)

// ✅ 正确：page 方法返回 IPage，语义自洽
public Result<IPage<UmsAdminVO>> page(@RequestParam(defaultValue = "1") @Min(1) Integer pageNum,
                                      @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer pageSize)
```

### 3.3 接口地址命名

**【必须遵守】** RESTful 资源路径统一**小写连字符**（`kebab-case`），禁止驼峰 URL、禁止动词裸用。

```java
// ❌ 禁止：驼峰 URL
@PostMapping("/resetPassword")
@RequestMapping("/system/umsAdmin")

// ✅ 正确：小写连字符
@PostMapping("/reset-password")
@RequestMapping("/system/ums-admin")
```

### 3.4 数据库表/字段命名

**【必须遵守】** 表名 `业务域前缀 + snake_case`（`wea_`/`sys_`/`ums_`），字段 `snake_case`，与 init.sql 保持一致（详见 §13）。

### 3.5 常量与枚举命名

**【必须遵守】** 常量 `UPPER_SNAKE_CASE`，枚举类名 `XxxEnum`、枚举成员 `UPPER_SNAKE_CASE`（详见 §5）。

### 3.6 定时任务命名

**【推荐优化】** 定时任务方法名 `{动作}{对象}`，如 `refreshMarketSnapshot`、`cleanExpiredToken`；禁止无意义 `task1`、`run`。

### 3.7 测试命名

**【必须遵守】** `{方法名}_should_{预期行为}`，如 `login_should_return_token_when_password_correct`；断言禁止依赖硬编码 Token 串（见 §19）。

---

## 4. 代码格式化与排版规范

### 4.1 导入规范

**【强制禁止】** 通配符导入；**【必须遵守】** 按分组排序（java → io/jakarta → lombok → spring → baomidou → com.wealth → static），组间空行。

### 4.2 缩进、空行与换行

**【必须遵守】**
- 缩进 4 空格，禁止 Tab；文件末尾保留单个换行。
- 类之间空两行、方法之间空一行；方法体内部按逻辑块空行分组。
- 单行超过 120 字符换行，换行后缩进 8 空格对齐。

### 4.3 大括号与语句

**【必须遵守】**
- 大括号左与表达式同行（K&R），`if/else`、`for` 强制加花括号，禁止单行省略。
- 禁止空语句体（空 `if` 落空、空 `catch`——见 §11）。

### 4.4 换行与逗号

**【推荐优化】** 多行参数、多行链式调用每行一个参数/一个 `.`；尾随逗号风格团队统一。

### 4.5 前端模板格式

**【必须遵守】** Vue template 缩进与层级清晰，`v-if/v-else`、`v-for`、嵌套组件标签保持正确缩进层级，禁止层级错乱。

---

## 5. 常量、枚举与魔法数字处理规范

### 5.1 魔法数字禁令

**【强制禁止】** 业务代码中散落裸数字/裸字符串，必须抽取为 `private static final` 常量或枚举。

```java
// ❌ 禁止：交易类型裸数字、消息类型裸数字、订单号截断长度裸数
if (dto.getTradeType() != 1 && dto.getTradeType() != 2) { throw new ServiceException(400, "仅支持买入/卖出"); }
String orderNo = "ORDER_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
msg.setMsgType(2);

// ✅ 正确：枚举 + 常量
if (!TradeTypeEnum.isValid(dto.getTradeType())) {
    throw new ServiceException(ResultCode.PARAM_ERROR, "仅支持买入/卖出");
}
private static final int ORDER_NO_RANDOM_LENGTH = 12;
String orderNo = OrderNoGenerator.generate(OrderNoType.TRADE, ORDER_NO_RANDOM_LENGTH);
msg.setMsgType(MessageTypeEnum.TRADE_NOTICE.getCode());
```

### 5.2 状态与业务类型必须用枚举

**【强制禁止】** 状态字段用 `int`/`String` 裸判断而不用枚举。业务域常见状态一律建枚举（tradeType、orderStatus、readFlag、msgType、productType、period 等）。

```java
// ✅ 正确：枚举定义
@Getter
@AllArgsConstructor
public enum TradeTypeEnum {
    BUY(1, "买入"), SELL(2, "卖出");

    private final int code;
    private final String desc;

    public static boolean isValid(Integer code) {
        return code != null && Arrays.stream(values()).anyMatch(e -> e.code == code);
    }
}

// ✅ 正确：SQL 引用枚举常量（而非裸数字）
@Select("SELECT * FROM wea_trade_order WHERE order_status = #{status}")
List<WeaTradeOrder> findByStatus(@Param("status") int status);
// 调用处：orderMapper.findByStatus(OrderStatusEnum.MATCHED.getCode())
```

### 5.3 魔法时间/阈值/限制值

**【必须遵守】** 超时、TTL、分页上限、锁时长、随机波动等一律常量化。

```java
// ❌ 禁止：TTL、锁时长、LIMIT 硬编码
redisUtil.set(key, token, 7, TimeUnit.DAYS);
redisUtil.setIfAbsent(lockKey, reqId, 30, TimeUnit.SECONDS);
.last("LIMIT 1000")

// ✅ 正确
private static final long REFRESH_TOKEN_TTL_DAYS = 7;
private static final int REFRESH_LOCK_TIMEOUT_SECONDS = 30;
private static final int KLINE_FETCH_LIMIT = 1000;
redisUtil.set(key, token, REFRESH_TOKEN_TTL_DAYS, TimeUnit.DAYS);
```

### 5.4 错误码必须走 ResultCode

**【强制禁止】** 代码中裸写业务错误码。统一使用 `ResultCode` 或分域错误码枚举（见 §11.3）。

### 5.5 前端魔法值

**【必须遵守】** 前端状态判断收敛到 `types/index.ts` 的 `DictItem` 常量，禁止散落裸数字/裸字符串。

```ts
// ❌ 禁止：裸数字
cancelTradeOrder(id, { orderStatus: 3 })
if (status === 1) { ... }

// ✅ 正确：收敛到常量
import { ORDER_STATUS } from '@/types'
cancelTradeOrder(id, { orderStatus: ORDER_STATUS.MATCHED })
if (status === ORDER_STATUS.MATCHED) { ... }
```

---

## 6. OOP 面向对象开发规约

### 6.1 避免实体成为「失血模型」

**【推荐优化】** 状态/类型相关的判断逻辑尽量收敛到枚举或实体自身的领域方法（如 `TradeOrder.isMatched()`），避免 Service 中到处 `if (status == 2)`。

### 6.2 构造器注入统一

**【强制禁止】** `@Autowired` 字段注入；**【必须遵守】** 统一 `@RequiredArgsConstructor` + `private final` 字段。

```java
// ✅ 正确：构造器注入（含解决事务自调用的自注入）
@Service
@RequiredArgsConstructor
public class MarketDataSimulationService {
    private final MarketDataSimulationService self;   // Spring 注入自身代理
}
```

**例外情况（保留手动构造器）**：`ObjectProvider<T>.getIfAvailable()`、`@Qualifier` 多实现注入、构造器内额外初始化逻辑。

### 6.3 禁止 new 出已注入的依赖

**【强制禁止】** Service 内 `new` 其他 Service/工具类替代注入；禁止 `new SimpleDateFormat` 等非线程安全对象为成员变量（见 §8）。

### 6.4 equals/hashCode

**【必须遵守】** 继承 `BaseEntity` 的实体使用 `@EqualsAndHashCode(callSuper = true)`；短期实体无影响，跨时间比较需评估 `onlyExplicitlyIncluded = true`。

### 6.5 DTO/VO 语义隔离

**【必须遵守】**
- 入参用 DTO，出参用 VO，禁止实体直接作为接口出入参（敏感字段 `password` 明文出网风险，见 §17）。

```java
// ❌ 禁止：Service 返回实体分页，敏感字段可能出网
Result<IPage<User>> page(...)

// ✅ 正确：Service 层完成 Entity→VO 转换
Result<IPage<UserVO>> page(...) {
    Page<User> page = pageVoWithFilter(new Page<>(current, size), wrapper);
    return Result.success(BeanConvertUtil.convertPage(page, UserVO.class));
}
```

### 6.6 常量类 vs 枚举

**【推荐优化】** 类型集合用枚举，参数常量用 `@Getter` 常量类或 `ResultCode`；禁止无意义常量类堆积。
---

## 7. 集合与数组编码规范

### 7.1 禁止全表 list() 后内存过滤

**【强制禁止】** 列表查询必须分页（`IPage`）+ 条件过滤，禁止 `service.list()` 拉全表再 `stream().filter`。

### 7.2 禁止循环内查库 / 循环内 IO

**【强制禁止】** 循环体内禁止发起 DB 查询、Redis IO、网络调用。

```java
// ❌ 禁止：循环内逐条 update（N+1 更新）
for (WeaMarketData data : cachedMarketData) {
    baseMapper.updateById(data);
}

// ✅ 正确：批量更新
updateBatchById(newPriceList);

// ❌ 禁止：循环内逐个清缓存
for (Long roleId : roleIds) { redisUtil.delete("perm:role:" + roleId); }

// ✅ 正确：批量收集后一次删除
redisUtil.delete(roleIds.stream().map(id -> "perm:role:" + id).toArray(String[]::new));
```

### 7.3 集合判空与取值

**【必须遵守】**
- 从 `Map` 取值必须判空：`Map#get` 可能返回 null，禁止直接 `.toString()` / `.equals()`。
- 返回集合接口，约定非 null（返回空集合 `Collections.emptyList()`），避免调用方 NPE。
- 禁止使用 `new HashMap<>()` 后逐字段 `put` 组装出参，改用 VO。

### 7.4 集合初始化与工具类

**【推荐优化】**
- 已知容量的 `ArrayList(capacity)` / `HashMap(capacity)` 预估容量；禁止不设初始容量的无脑 `new`。
- 优先 `stream()` + 收集器，禁止手工 for 拼集合；注意 `Collectors.toList()` 不可靠（阿里告警）。

### 7.5 前端数组/集合

**【必须遵守】** 前端列表渲染必须判空（`v-for` 前 `array?.length` 防御），禁止对 `undefined` 直接 `v-for`；接口返回数组必须有类型（见 §14）。

---

## 8. 并发、线程与异步线程池规范

### 8.1 共享可变状态禁止并发读写

**【强制禁止】** 被多线程读写的集合/对象必须同步或不可变。

```java
// ❌ 禁止：volatile List 就地修改元素，写线程与读线程并发 → 数据竞争
private volatile List<WeaMarketData> cachedMarketData;
// 更新时：list.set(i, newPrice)  ← 就地改元素

// ✅ 正确：写时复制不可变快照，读线程拿到一致快照
private volatile List<WeaMarketData> cachedMarketData;
// 更新时：
this.cachedMarketData = List.copyOf(newList);   // 整体替换引用，而非就地改元素
```

### 8.2 非线程安全对象禁止作成员变量

**【强制禁止】** `SimpleDateFormat`、`Random`（并发共享）、`StringBuilder` 等禁止作实例字段跨线程使用；日期统一 `java.time`。

### 8.3 幂等/防重必须原子化

**【强制禁止】** 「检查 → 写入」两步必须原子（`SET NX EX` 或 DB 唯一键），禁止 check-then-set 竞态。

```java
// ❌ 禁止：先查 Redis 幂等键，再写库 —— 并发双击可同时通过校验 → 重复下单（TOCTOU）
if (redisUtil.get(IDEMPOTENT_KEY_PREFIX + key) != null) { throw ...; }
createOrder(dto);

// ✅ 正确：用 SET NX EX 原子占位，占位失败即重复
Boolean first = redisUtil.setIfAbsent(IDEMPOTENT_KEY_PREFIX + key, orderNo, 30, TimeUnit.SECONDS);
if (!Boolean.TRUE.equals(first)) {
    throw new ServiceException(ResultCode.DUPLICATE_SUBMIT, "订单重复提交，请勿重复操作");
}
```

### 8.4 异步任务统一线程池

**【必须遵守】** 所有 `@Async` / 定时任务 / 手动线程必须使用统一线程池（`spring.task.execution.pool` 或 `AsyncConfig` 中定义的 `taskExecutor`），禁止裸 `new Thread`、禁止无界队列。

### 8.5 定时任务与调度

**【强制禁止】** 声明 `@Scheduled` 必须同时启用 `@EnableScheduling`，否则任务静默失效。

```java
// ✅ 正确：@Scheduled 方法所在模块必须有 @EnableScheduling 启动
@Configuration
@EnableScheduling
public class SchedulingConfig { }

@Service
public class MarketDataSimulationService {
    @Scheduled(fixedDelayString = "${market.simulation.interval:5000}")
    public void simulateMarketTick() { ... }
}
```

**【必须遵守】** 定时任务内部必须 try-catch 兜底并打 `error` 日志，禁止一个任务异常导致后续轮次全部跳过（Spring 默认单线程串行）。

### 8.6 锁使用规范

**【必须遵守】**
- 分布式锁基于 `RedisUtil.setIfAbsent`，**必须设置超时**、释放时校验持有者（value 用唯一请求 id）。
- 锁 key 必须含业务维度（`lock:trade:{orderId}`），禁止全局一把锁。

```java
// ✅ 正确：分布式锁释放校验持有者
String lockKey = "lock:trade:" + orderId;
String reqId = UUID.randomUUID().toString();
if (Boolean.TRUE.equals(redisUtil.setIfAbsent(lockKey, reqId, 30, TimeUnit.SECONDS))) {
    try {
        // 业务逻辑
    } finally {
        if (reqId.equals(redisUtil.get(lockKey))) {   // 校验持有者，防误删他人锁
            redisUtil.delete(lockKey);
        }
    }
}
```

### 8.7 并发集合选择

**【推荐优化】** 读多写少的缓存列表用 `CopyOnWriteArrayList`；高频读用不可变快照；`ConcurrentHashMap` 作缓存时注意 `computeIfAbsent` 的递归死锁陷阱（阿里告警）。

---

## 9. 分支循环与条件判断编码规范

### 9.1 卫语句优先

**【推荐优化】** 提前返回/抛异常的卫语句优先，避免多层嵌套 if；禁止超过 3 层缩进嵌套。

```java
// ❌ 禁止：深嵌套
if (dto != null) {
    if (dto.getId() != null) {
        if (service.exists(dto.getId())) { ... } else { throw ...; }
    }
}

// ✅ 正确：卫语句
if (dto == null || dto.getId() == null) { throw new ServiceException(ResultCode.PARAM_ERROR, "参数缺失"); }
if (!service.exists(dto.getId())) { throw new ServiceException(ResultCode.NOT_FOUND, "记录不存在"); }
```

### 9.2 分支覆盖所有合法取值

**【必须遵守】** 枚举/类型 switch 必须 `default` 处理非法值；`if-else` 链条件互斥完整；非法值禁止静默兜底默认分支。

```java
// ❌ 禁止：非法值静默当默认处理
int days = "30D".equals(period) ? 30 : 7;

// ✅ 正确：非法值明确报错或显式默认
PeriodEnum periodEnum = PeriodEnum.fromCode(period);   // 非法抛 PARAM_ERROR
```

### 9.3 循环内禁止修改集合结构

**【强制禁止】** 遍历 `List`/`Map` 时禁止 `remove`/`add`（增强 for 会抛 ConcurrentModificationException）；需删除用 `removeIf` / 收集后统一处理。

### 9.4 条件表达式

**【必须遵守】**
- 布尔判断禁止 `== true` / `!= false`，直接 `if (flag)`。
- `String` 判断用 `StringUtils.hasText()`，禁止 `str != null && str.length() > 0`。
- 比较用常量前置或 `Objects.equals`，禁止 `1 == x`（阿里反例）。
- 前端条件用 `===` 全等，禁止 `==`。

### 9.5 避免在循环内做重计算

**【推荐优化】** 循环外可计算的值（正则 Pattern、DateFormat、常量映射）提出循环；禁止循环内重复 `new` 大对象。

---

## 10. 注释标准与废弃代码清理

### 10.1 注释分级

**【必须遵守】**

| 位置 | 要求 |
|------|------|
| Controller 类 | 保留类级 JavaDoc |
| Controller 方法 | **不写**方法级 JavaDoc，用 `@Operation(summary = "")` |
| Service 接口核心方法 | 建议写 |
| Service 实现 | 保留或参考接口 |
| Entity | 类级可选，字段级不写（用 `@TableField` 与列注释） |
| Mapper 复杂 SQL | 必须写 |
| 工具类/公共组件 | 必须写 |

### 10.2 注释内容规范

**【必须遵守】**
- 注释只解释「为什么」（非显而易见的约束、历史原因、规避的坑），禁止逐行翻译代码。
- `@Operation` summary 禁止过长、禁止塞实现细节。
- 禁止注释与代码不一致的过期注释。

### 10.3 废弃代码清理

**【强制禁止】**
- 禁止 `System.out.println` 调试输出残留；调试用 `log.debug`。
- 废弃的类/方法直接删除或标注 `@Deprecated` + 说明，禁止「注释掉大段代码」留在仓库。
- 只有配置没有使用的死代码/死配置须及时清理或补全功能。
- 前端禁止 `console.log` 上线（`console.warn/error` 仅限有意义的告警，见 §12）。

### 10.4 魔法值必须注释含义

**【推荐优化】** 无法完全消除的魔法值（如指数退避 1s→30s、波动幅度）必须就近注释业务含义。

---

## 11. 全局异常处理与自定义异常规约

### 11.1 统一异常出口

**【强制禁止】** 各 Controller 各自 try-catch；一切业务异常抛 `ServiceException(code, message)`，由 `GlobalExceptionHandler`（`com.wealth.common.exception`）统一兜底。

### 11.2 HTTP 状态码与业务码解耦

**【必须遵守】** `GlobalExceptionHandler` 必须返回与语义一致的 `HttpStatus`（400/401/403/404/405/500），同时 body 保留业务 `code`，前后端与监控口径统一。

```java
// ❌ 禁止：全局返回 Result 且无 @ResponseStatus，HTTP 恒 200
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ServiceException.class)
    public Result<?> handle(ServiceException e) { ... }        // HTTP 200
}

// ✅ 正确：绑定 HTTP 状态
@ExceptionHandler(ServiceException.class)
@ResponseStatus(HttpStatus.BAD_REQUEST)                          // 或按 e.getCode() 动态映射
public Result<?> handle(ServiceException e) { ... }
```

### 11.3 错误码收敛到 ResultCode

**【强制禁止】** 错误码裸数字。错误码必须按分段规约登记到 `ResultCode` 或分域错误码枚举：

```
通用 200~499 | user 1xxx | product 2xxx | trade 3xxx | message 4xxx | system 5xxx | 基础设施 6xxx
```

```java
// ❌ 禁止：散落 ServiceException(400/401/404/429/500, ...)
throw new ServiceException(401, "Token已过期");

// ✅ 正确：引用 ResultCode 或分域枚举常量
throw new ServiceException(ResultCode.TOKEN_EXPIRED);
// trade 域自定义：throw new ServiceException(TradeErrorCode.INSUFFICIENT_BALANCE);
```

### 11.4 禁止吞异常与丢堆栈

**【强制禁止】** 空 catch、`catch (Exception e) {}`；**【必须遵守】** 捕获后必须记录完整堆栈，或作为 cause 链式传递。

```java
// ❌ 禁止：捕获后抛新异常，原始堆栈丢失
} catch (Exception e) {
    throw new ServiceException(401, "refreshToken 无效或已过期");
}

// ✅ 正确：打日志保留堆栈（或 as cause 链式传递）
} catch (Exception e) {
    log.warn("刷新令牌失败: userId={}", userId, e);
    throw new ServiceException(ResultCode.TOKEN_INVALID, "刷新令牌无效或已过期");
}
```

### 11.5 禁止向客户端暴露内部细节

**【强制禁止】** 对外错误信息禁止泄露 SQL、表结构、内部类名、技术栈、字段名拼装；堆栈只写日志。

```java
// ❌ 禁止：把字段名拼进错误回显客户端
return Result.error(400, fieldName + " " + errorMessage);

// ✅ 正确：返回可读中文文案
return Result.error(ResultCode.PARAM_ERROR, "参数校验失败");
```

### 11.6 异常信息语言统一

**【强制禁止】** 异常与提示信息统一中文，禁止中英文混杂、禁止符号混杂。

```java
// ❌ 禁止
throw new ServiceException(400, "仅支持 1=买入 2=卖出");
msg.setTitle("trade order submitted");

// ✅ 正确
throw new ServiceException(ResultCode.PARAM_ERROR, "仅支持买入/卖出");
msg.setTitle("交易委托提交成功");
```

---

## 12. 日志强制规范

### 12.1 日志使用方式

**【必须遵守】**
- 统一 `@Slf4j`，禁止手动声明 Logger、禁止 `System.out`。
- 用占位符 `{}` 拼接，禁止字符串 `+` 拼接。

### 12.2 日志级别选择

**【必须遵守】**

| 级别 | 用途 | 违例 |
|------|------|------|
| debug | 开发/排障细节 | 用 debug 打关键操作 |
| info | 关键业务流转（下单、登录成功、状态变更） | — |
| warn | 可恢复异常、降级路径 | — |
| error | 业务/系统异常，必须带堆栈 | 用 error 打正常流程 |

### 12.3 关键链路必须打业务唯一 id

**【必须遵守】** 下单/登录/交易等关键链路日志必须包含业务唯一 id（订单号、userId、traceId），便于全链路排查。

```java
log.info("创建订单成功: orderNo={}, userId={}, amount={}", order.getOrderNo(), userId, order.getAmount());
```

### 12.4 异常日志必须带完整堆栈

**【强制禁止】** `log.error(e.getMessage())` 丢堆栈；必须 `log.error("msg", e)`。

### 12.5 敏感信息禁止打印

**【强制禁止】** 密码、Token、手机号、身份证、卡号、防重放 nonce 等禁止明文打印；按脱敏规则输出。

```java
// ❌ 禁止：日志明文打印凭证类字段
log.debug("防重放校验通过: nonce={}", nonce);

// ✅ 正确：只打印关键信息，nonce 不落日志
log.debug("防重放校验通过: username={}, timestamp={}", username, timestamp);
```

### 12.6 脱敏实现必须可靠

**【必须遵守】** 脱敏逻辑必须覆盖同义字段（token/accessToken/tokenValue），且不得破坏 JSON 结构。推荐用 Jackson 树模型按字段名递归脱敏，避免正则字符串替换导致 JSON 损坏。

### 12.7 日志性能

**【推荐优化】**
- 高频路径先 `if (log.isDebugEnabled())`。
- 禁止循环内打大量日志；SSE 推送等高频广播频控日志。
- 前端禁止 `console.log` 上线；`console.warn/error` 避免打印完整 err 对象携带 URL/参数。
---

## 13. MySQL、MyBatis-Mapper 与 SQL 编写规范

### 13.1 建表与文档链同步

**【强制禁止】** 建表/改表必须：① 增量脚本 + 审批；② 同步 `init.sql` 与 `docs/DATABASE-SCHEMA.md`；③ 代码 Entity 对齐。禁止凭记忆改表、禁止直接改线上表结构。

### 13.2 表结构强制项

**【强制禁止】** 每张业务表必须包含 `create_time` / `update_time` 自动填充字段（阿里规约），否则实体继承 BaseEntity 后填充失效。

```sql
-- ✅ 正确：所有业务表统一三件套
create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
del_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除'
```

### 13.3 高频查询必须建索引

**【强制禁止】** 高频 WHERE / ORDER BY / 连接关联列无索引全表扫描。

```sql
CREATE INDEX idx_relation_admin_id ON ums_admin_role_relation (admin_id);
CREATE INDEX idx_relation_role_id   ON ums_role_resource_relation (role_id);
CREATE INDEX idx_trade_order_ctime  ON wea_trade_order (create_time);
```

> 阿里规约：`DATE(create_time)=CURDATE()` 函数式过滤无法走索引，改为范围查询 `create_time >= ? AND create_time < ?`。

### 13.4 SQL 注入禁令

**【强制禁止】** `${}` 拼接、字符串拼 SQL；全部走 MyBatis-Plus / `#{}` 参数绑定；LIKE 查询统一经 `LikeUtil.escape` 转义后绑定。

### 13.5 禁止 SELECT *

**【强制禁止】** 只查需要的列。

```java
// ❌ 禁止：SELECT *，全列且量不可控
@Select("SELECT * FROM wea_market_data WHERE del_flag = 0")

// ✅ 正确：显式列名 + LIMIT
@Select("SELECT id, product_code, current_price, market_time FROM wea_market_data " +
        "WHERE del_flag = 0 ORDER BY market_time DESC LIMIT #{limit}")
List<WeaMarketData> findSnapshot(@Param("limit") int limit);
```

### 13.6 分页与大数据量

**【必须遵守】**
- 列表一律 `IPage` 分页；禁止一次性拉全量进内存（K 线/快照/缓存加载设可控上限并常量化）。
- 大表聚合/历史数据场景改游标、流式或分批。
- `.last("LIMIT ...")` 硬编码上限必须常量化并注释（见 §5.3）。

### 13.7 DELETE/UPDATE 安全

**【强制禁止】**
- 无条件 `UPDATE`/`DELETE`；必须带主键或明确 WHERE。
- 禁止 `DROP`/`TRUNCATE`（除审批通过的初始化脚本）。
- 更新必须带乐观锁或条件更新，防止丢失更新（见 §13.8）。

### 13.8 并发更新防丢失（乐观锁）

**【强制禁止】** read-modify-write 整行 `updateById` 无乐观锁，并发互相覆盖。

```java
// ✅ 正确：实体加 @Version 乐观锁
@Version
@TableField("version")
private Integer version;

// ✅ 正确：条件更新替代 read-modify-write
// UPDATE wea_trade_order SET order_status = #{newStatus}
// WHERE id = #{id} AND order_status = #{expectStatus}   -- 期望态匹配才更新
int rows = orderMapper.updateStatusIfMatches(id, expectStatus, newStatus);
if (rows == 0) { throw new ServiceException(ResultCode.STATE_CONFLICT, "订单状态已变更，请刷新"); }
```

### 13.9 逻辑删除与物理删除统一

**【强制禁止】** 同一实体族的删除语义必须统一。`del_flag` 与 `@TableLogic` 继承冲突必须每表显式决策「逻辑删」或「物理删」并在 Entity 注释声明；无 `del_flag` 列的表走物理删除，方法名须注明（如 `deleteFavoritePhysically`），禁止裸 `removeById` 造成删除语义漂移。

### 13.10 删除后清理关联数据

**【必须遵守】** 删除主数据必须同步清理关联数据（本项目无物理外键，按阿里「禁止物理外键」规范靠代码维护）。

```java
@Transactional(rollbackFor = Exception.class)
public void deleteRole(Long roleId) {
    roleMapper.deleteById(roleId);
    roleResourceRelationService.deleteByRoleId(roleId);   // 清理关联
    permissionCacheCleaner.clearRoleCache(roleId);         // 清权限缓存
}
```

### 13.11 Mapper 选型：XML vs 注解

**【推荐优化】** 复杂多表 SQL（嵌套子查询、多条件动态 SQL）建议抽 `Mapper XML`（同包同名）替代长注解内联——XML 便于复用与 resultType 声明；简单查询可用注解。

### 13.12 手写 SQL 的 del_flag 陷阱

**【必须遵守】** 注解/XML 手写 SQL 不会自动拼接 `@TableLogic` 条件，**必须显式写 `del_flag = 0`**；并在 Mapper 类注释统一声明此约定。

### 13.13 主键策略

**【推荐优化】** 金融级分布式场景 `BaseEntity` 主键评估由 `IdType.AUTO`（库自增）切换 `ASSIGN_ID`（雪花），避免分库/分表/迁移时撞键；若维持自增需在文档声明单库限制。

### 13.14 数据类型与精度

**【强制禁止】** 金额/涨跌幅用 `double`/`float`；统一 `DECIMAL` + `BigDecimal`（金额 `DECIMAL(10,2)`、涨跌幅 `DECIMAL(8,4)`）。`del_flag` 等同类字段全库类型口径统一，禁止 INT/TINYINT 混用。

### 13.15 前端查询参数

**【必须遵守】** 列表页查询参数与后端 `pageNum/pageSize`、排序字段白名单对齐；禁止把任意用户输入字符串直接当排序字段（见 §14）。

---

## 14. 参数校验与 DTO 校验规范

### 14.1 后端 DTO 校验

**【强制禁止】** 所有 `@RequestBody` DTO 必须加 `@Valid`，关键字段必须声明约束注解（`@NotBlank`/`@NotNull`/`@Size`/`@Pattern` 等）。

```java
// ✅ 正确：DTO 字段级校验
public class UmsAdminCreateDTO {
    @NotBlank(message = "用户名不能为空")
    @Size(max = 64, message = "用户名长度不能超过64")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 32, message = "密码长度需在8-32位")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "密码需包含字母和数字")
    private String password;
}
```

### 14.2 Controller 参数校验

**【必须遵守】**
- 分页参数加边界：`@Min(1) @Max(100)`。
- `@PathVariable`/`@RequestParam` 关键参数用 `@Validated` 触发校验。
- 排序字段必须白名单校验，禁止透传任意字符串进 ORDER BY：

```java
// ✅ 正确：排序白名单
private static final Set<String> ALLOWED_ORDER_BY = Set.of("price", "rise_fall_rate", "create_time");
private static final Set<String> ALLOWED_ORDER_DIR = Set.of("asc", "desc");

public Result<IPage<ProductVO>> page(Integer pageNum, Integer pageSize,
                                     String orderBy, String orderDir) {
    if (orderBy != null && !ALLOWED_ORDER_BY.contains(orderBy)) {
        throw new ServiceException(ResultCode.PARAM_ERROR, "不支持的排序字段");
    }
    // orderDir 默认 asc，非法值抛参
}
```

### 14.3 密码强度校验

**【必须遵守】** 注册、重置密码必须校验强度（长度 + 字符组合），禁止仅 `@NotBlank`。

### 14.4 验证码不可静默跳过

**【强制禁止】** 登录/注册验证码在「未提供 key」或「缓存不可用」时禁止静默放行；验证码必须为登录/注册前置强校验，缓存不可用时该接口返回错误或走明确降级策略（如频率上限收紧），并打 `warn` 日志。

### 14.5 前端表单校验

**【必须遵守】**
- 提交前必须校验必填与格式，禁止把 `undefined` 透传给后端。
- 数字输入必须约束类型/精度（整数/小数、步进）。
- 提交按钮防连点（loading/防抖），关键操作二次确认（见 §17 防重）。

### 14.6 前端类型校验兜底

**【推荐优化】** 接口返回 `any`/`Record<string, any>` 必须收敛为 interface（见 §19.6）；对响应结构做轻量 schema 防御，避免拦截器解构失败静默。

---

## 15. Redis 与缓存使用规范

### 15.1 统一 RedisUtil 入口

**【强制禁止】** 直接散用 `RedisTemplate`；缓存统一经 `RedisUtil`（注入 `jsonRedisTemplate`）。

### 15.2 防穿透 / 击穿 / 雪崩

**【必须遵守】**
- 穿透：空值缓存 + 参数合法性前置校验。
- 击穿：互斥重建（`setIfAbsent` 锁）。
- 雪崩：TTL 加随机扰动；热点不过期 + 异步续期。

### 15.3 缓存 key 规范

**【必须遵守】** 缓存 key 统一 `模块:业务:标识`，禁止裸 key；涉及用户输入/随机串拼 key 时必须归一化（哈希或截断），禁止超长/畸形 key。

```java
// ✅ 正确：key 归一化
String nonceKey = NONCE_PREFIX + username + ":" + DigestUtils.sha256Hex(nonce);
```

### 15.4 Redis 不可用降级

**【必须遵守】** 主链路禁止被 Redis 故障拖垮：读缓存用 `RedisUtil.safeExecute/safeExecuteVoid` 兜底返回默认值；写缓存也应包 safeExecute 并容忍失败。

- **但安全类校验（验证码、防重放）不可静默降级放行**：缓存不可用时写接口拒绝 + `warn` 告警，仅读缓存可降级。

### 15.5 缓存一致性

**【必须遵守】** 先更新库再删缓存（Cache Aside），禁止只写缓存不落库；缓存数据必须 TTL，禁止永久 key。

### 15.6 分布式锁

**【必须遵守】** 锁必须有超时 + 释放校验持有者；锁 key 含业务维度。新增锁场景严格按 CLAUDE.md §7.10 与 §8.6。

### 15.7 序列化安全

**【推荐优化】** 带类型信息的 JSON 序列化（`@class`）存在反序列化投毒面；评估改用固定类型序列化或配置 `ObjectMapper` 的类型白名单策略。

---

## 16. 事务使用规范与陷阱禁令

### 16.1 写操作必须加事务

**【强制禁止】** 任何写操作（增/删/改）缺 `@Transactional(rollbackFor = Exception.class)`。

- 注意 `rollbackFor = Exception.class`，禁止 `RuntimeException.class`（会漏回滚部分异常）。
- 仅单表 Redis 写操作（登出、清缓存）可不加，但涉及 DB 写入必须补。

### 16.2 事务内禁止远程 IO / 长耗时

**【强制禁止】** 事务内做远程调用、消息发送、外部 HTTP、SSE 广播等；异步/外呼放到事务外（`@TransactionalEventListener` 或事务同步）。

### 16.3 跨资源一致性（Redis + DB）

**【强制禁止】** 事务内先写 Redis 再提交 DB 的「双写」必须处理回滚残留。整改方向：
- ① Redis 键写事务外（DB 提交成功后写，成功即不可重放）；
- ② 或注册事务同步 `registerSynchronization(commit → set)`；
- ③ 或 key 含短 TTL 自动过期兜底。

### 16.4 事务自调用陷阱

**【强制禁止】** 同类 `this` 调用不触发 `@Transactional` 代理。正确做法：注入自身代理 Bean（构造器注入）或拆独立 Service。

### 16.5 事务传播与隔离

**【必须遵守】** 独立子事务（审计、消息推送失败不影响主流程）显式 `REQUIRES_NEW`；只读查询可 `readOnly = true`；禁止滥用 `NOT_SUPPORTED/MANDATORY`。默认 `REQUIRED`。

### 16.6 事务体积控制

**【推荐优化】** 单事务内禁止逐条循环 `updateById`（N 次小事务，锁面广）；改 `updateBatchById` 或事务外分批。

### 16.7 乐观锁

**【必须遵守】** 资金/状态类并发更新必须 `@Version` 或条件更新，禁止整行 `updateById` 覆盖（见 §13.8）。

---

## 17. 安全编码规约

### 17.1 密钥禁止硬编码与默认兜底

**【强制禁止】** 密码、JWT secret、数据库/Redis 连接串、内网地址禁止 hardcode；**禁止 yml 提供可提交仓库的默认密钥兜底**；漏配必须启动失败而非静默用默认值。

```yaml
# ❌ 禁止：带默认密钥，生产漏配即用已知密钥
secret: ${JWT_SECRET:change-me-to-a-random-256-bit-key}

# ✅ 正确：无默认值，漏配即启动失败
secret: ${JWT_SECRET}
```

- 数据库/Redis 密码、zipkin 等内网地址从提交配置剥离，一律走环境变量。

### 17.2 敏感字段脱敏与出网控制

**【强制禁止】**
- 实体含 `password` 等敏感字段必须 `@JsonIgnore`/`@ToString.Exclude`，禁止依赖「手工转 VO」兜底。
- 日志/响应禁止明文手机号、身份证、卡号；脱敏格式 `138****1234`。
- 前端 `refresh_token` **禁止明文存 localStorage/sessionStorage**（XSS 可窃取续期凭证），改 httpOnly Cookie 或内存态。

### 17.3 权限与越权（IDOR）

**【强制禁止】** 仅凭 ID 直查他人资源、无归属校验。

```java
// ✅ 正确：归属校验下沉 Service，校验当前登录用户
public UserVO getUserById(Long id) {
    User current = authSupport.getCurrentUser();           // 从上下文取当前用户
    if (!current.isAdmin() && !current.getId().equals(id)) {
        throw new ServiceException(ResultCode.FORBIDDEN, "无权访问他人资料");
    }
    ...
}
```

### 17.4 认证链与令牌安全

**【必须遵守】**
- 密码 BCrypt 存储，禁止明文、MD5、SHA。
- Token Cookie 必须 `httpOnly` + `Secure` + `SameSite`（防 CSRF）。
- 网关与 Service 层令牌校验职责单一，避免同一 Token 双次验签逻辑漂移。
- JWT 建议含 `issuer/audience` 声明；refresh 与 access 区分用途。

### 17.5 权限白名单与端点收敛

**【必须遵守】**
- 新接口默认不放白名单；改 `AuthConstant.PERMIT_ALL_URLS` 走 CLAUDE.md §2.1 审批。
- 管理端点 `/actuator/**` 仅 admin 或仅内网可访问；生产环境只暴露 `health,info`。
- 生产环境关闭 Swagger/Knife4j 与 SQL 打印（含网关聚合开关）。

### 17.6 防重复提交 / 防重放

**【强制禁止】** 写接口防重放头校验不可静默降级；资金写接口叠加幂等键（前端基于业务参数生成幂等键 + 后端原子占位 + `@AntiReplay`）。

### 17.7 注入 / XSS / 开放跳转

**【强制禁止】**
- SQL 注入：全 `#{}` + 参数绑定（见 §13.4）。
- XSS：前端禁止 `v-html` 渲染用户输入；后端输出统一 JSON 序列化。
- 开放跳转：`redirect` 参数必须校验 `startsWith('/')` 白名单后再跳转。

### 17.8 生产开关铁律

**【强制禁止】** prod 关闭 Swagger、SQL 打印；管理端点仅 `health,info`；密钥全环境变量。对照 CLAUDE.md §5.2，生产配置评审时逐项核对。

---

## 18. 重复代码治理与工具类抽取规范

### 18.1 DRY 原则

**【必须遵守】** 三处以上重复逻辑必须抽公共方法/工具类/composable；两处重复且逻辑复杂（≥5 行）也建议抽取。常见重复样板：分页查询 + VO 转换、验证码校验、登录响应组装、字段手工复制、表单校验、时间/数字格式化。

### 18.2 分页样板收敛

**【必须遵守】** 分页查询统一走公共基类方法（如 `BaseBizServiceImpl.pageVoWithFilter`），禁止各 Controller 复制「new Page + Wrapper + convertPage」样板。

### 18.3 工具类抽取规范

**【必须遵守】**
- 工具类纯静态、无共享可变状态、线程安全；类名 `XxxUtil`，构造器私有。
- 禁止为单个方法开「工具类」；禁止在工具类里依赖 Spring 容器（除非注入 bean）。
- 高频反射工具（`BeanConvertUtil`）必须缓存 `PropertyDescriptor` 提升性能。

### 18.4 反模式清理

**【强制禁止】** 双大括号初始化 `new Xxx(){{ ... }}`（生成匿名子类并持有外部实例，易内存泄漏）。

```java
// ❌ 禁止：双大括号初始化
new FlowRule() {{
    setResource("xxx");
    setCount(10);
}};

// ✅ 正确：显式构建
FlowRule rule = new FlowRule();
rule.setResource("xxx");
rule.setCount(10);
```

### 18.5 前端组件/composable 复用

**【必须遵守】** 通用能力（表格 CRUD、分页、表单、幂等键）一律走 `useCrudPage`/`AdminDataTable`/`AdminFormDialog`/`AdminFilterBar` 五件套与 composable，禁止各页面复制样板；时间/数字格式化统一走 `utils/format.ts`。

---

## 19. 单元测试与接口开发规范

### 19.1 测试分层与要求

**【必须遵守】** 对照 CLAUDE.md §13：Service 单测（Mockito mock 依赖）、Controller 测（MockMvc）、Mapper 测（需 DB）、接口/冒烟端到端。每个 Service 方法至少一个正向用例 + 异常分支。

### 19.2 测试禁止事项

**【强制禁止】**
- 禁止 mock 被测对象自身逻辑（`spy(被测类) + doReturn` 部分 mock）。
- 禁止跳过测试上线（`-DskipTests` 仅本地编译）。
- 禁止 hardcode 真实密码/生产连接/真实 Token 串。
- 禁止只写正向用例、禁止用假断言掩盖失败。

### 19.3 测试命名

**【必须遵守】** `{方法名}_should_{预期行为}`；断言必须覆盖 `code` 与关键 `data` 字段，禁止只断言 HTTP 200。

### 19.4 测试环境隔离

**【必须遵守】** 测试必须用独立 `application-test.yml`，禁止直接加载真实 `application.yml`/`application-prod.yml`，避免测试与线上配置耦合。

### 19.5 接口开发规范

**【必须遵守】**
- RESTful + `@Tag`/`@Operation`；`@RequestBody` 加 `@Valid`。
- 新增接口评估权限白名单；写接口评估防重放/幂等。
- 接口返回 `Result<T>`，DTO 校验前置，错误走 `ServiceException` + `GlobalExceptionHandler`。
- 新接口 `@Operation` 标注版本；废弃接口按 CLAUDE.md §18.3 流程。

### 19.6 前端类型化

**【强制禁止】** 前端接口定义禁止 `any`/`Record<string, any>` 兜底，禁止 `as any` 断言吞类型。

```ts
// ❌ 禁止：any 兜底
PageResult<Record<string, any>>

// ✅ 正确：定义共享 interface
export interface AdminRoleRelation {
  id: number
  adminId: number
  roleId: number
  roleName: string
}
// 接口：pageRoleRelations(params): Promise<PageResult<AdminRoleRelation>>
```

### 19.7 前端 Axios 封装强制走统一入口

**【强制禁止】** 业务代码绕过 `api/index.ts` 裸 `axios`/`fetch`；确有防递归等特殊需求必须收敛到 `api/index.ts` 内私有函数并注释，业务文件禁止出现裸 axios。续期成功路径必须校验业务码 `code === 200` 再应用新 token。

### 19.8 前端日志与错误提示

**【必须遵守】** 错误提示唯一来源（拦截器统一 `ElMessage`），视图禁止重复 toast；避免打印完整 err 对象。
---

## 20. 新增强制检查清单

> 新增/修改代码前逐项自查，全部通过才可提交评审。

### 20.1 分层与结构（必查）

- [ ] Controller 只做「校验 + 路由 + 包装 Result」，无业务逻辑、无 new Entity、无直接查库/Redis
- [ ] Controller 返回类型统一 `Result<T>`，无 `ResponseEntity` 混用
- [ ] 跨域复用走 Contract 接口，未直接依赖他域 Mapper/Service
- [ ] URL 小写连字符，RESTful；新接口已评估是否加入权限白名单

### 20.2 命名与常量（必查）

- [ ] 类/方法/变量/常量命名符合 §3 规范，同义字段全库统一
- [ ] 无魔法值/魔法数字；状态与类型用枚举（`enums` 包）
- [ ] 错误码引用 ResultCode/分域枚举，无裸数字
- [ ] `@TableField("列名")` 显式标注，与 init.sql 一致

### 20.3 安全（必查）

- [ ] 无密钥/密码/内网地址 hardcode，无默认密钥兜底，全走环境变量
- [ ] 实体敏感字段（password 等）已 `@JsonIgnore`/`@ToString.Exclude`；日志不打印明文敏感信息
- [ ] 写接口已评估防重放/幂等；防重放校验未静默降级
- [ ] 资源归属校验（IDOR）已下沉 Service
- [ ] DTO `@Valid` + 字段级约束（含密码强度）；验证码不可绕过

### 20.4 数据与事务（必查）

- [ ] 写操作 `@Transactional(rollbackFor = Exception.class)`，无同类 this 自调用
- [ ] 事务内无远程 IO/长耗时；跨资源（Redis+DB）双写已处理回滚残留
- [ ] 列表分页（IPage）；无 SELECT *、无 ${} 拼接、无循环内查库
- [ ] 状态更新用乐观锁/条件更新，未整行 updateById 覆盖
- [ ] 删除主数据已清理关联；逻辑/物理删除语义与表声明一致

### 20.5 异常与日志（必查）

- [ ] 异常抛 `ServiceException`，GlobalExceptionHandler 统一兜底；无空 catch、无丢堆栈
- [ ] `@Slf4j` + 占位符；error 带完整堆栈；关键链路含业务唯一 id

### 20.6 前端（必查）

- [ ] 无 `any`/`@ts-ignore`/`as any`；接口返回有 interface
- [ ] 走 `api/index.ts` 封装，无裸 axios/fetch；错误提示单一来源
- [ ] 表单必填/类型校验前置，无 undefined 透传；提交按钮防连点
- [ ] token 不落 localStorage/sessionStorage 明文；无 v-html 渲染用户输入
- [ ] KeepAlive 页面用 `onActivated/onDeactivated` 管数据与 SSE 订阅

### 20.7 测试与交付（必查）

- [ ] 每个新 Service 方法有正向 + 异常分支测试；测试用独立 application-test.yml
- [ ] 未 mock 被测对象自身逻辑；无硬编码口令/Token 串断言
- [ ] 改配置/DDL/依赖已走审批并附回滚方案；文档（init.sql / DATABASE-SCHEMA.md）已同步
- [ ] 本地编译通过（`mvn clean install -DskipTests`），测试通过，冒烟验证成功

---

> **修订记录**：2026-08-04 V2 发布。后续修改需走 CLAUDE.md §14 审批流程，防止规则漂移。
