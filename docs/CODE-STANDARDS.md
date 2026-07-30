# 代码规范手册

> 基于 2026-07-30 全量代码审计生成，项目内所有 Java 代码须遵循以下规范。

---

## 目录

- [一、导入规范](#一导入规范)
- [二、构造器注入](#二构造器注入)
- [三、日志规范](#三日志规范)
- [四、实体规范](#四实体规范)
- [五、Controller 规范](#五controller-规范)
- [六、命名规范](#六命名规范)
- [七、常量与魔法值](#七常量与魔法值)
- [八、异常处理](#八异常处理)
- [九、事务规范](#九事务规范)
- [十、成员变量声明顺序](#十成员变量声明顺序)
- [十一、JavaDoc 规范](#十一javadoc-规范)
- [十二、审计遗留项](#十二审计遗留项)

---

## 一、导入规范

### 1.1 禁止通配符导入

```java
// ❌ 禁止
import java.util.*;
import com.wealth.platform.system.service.*;

// ✅ 正确
import java.util.List;
import com.wealth.platform.system.service.UmsAdminService;
```

### 1.2 导入顺序

按以下分组顺序排列，每组内按字母序，组间空行分隔：

```
1. java.*          — Java 标准库
2. io.* / jakarta.* — Jakarta EE / Swagger 等
3. lombok.*         — Lombok 注解
4. org.springframework.* — Spring 框架
5. com.baomidou.*   — MyBatis-Plus
6. com.wealth.*     — 项目自身代码
7. static imports   — 静态导入（放最后）
```

```java
import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.core.metadata.IPage;

import com.wealth.common.result.Result;

import static org.mockito.Mockito.when;
```

### 1.3 使用短类名

禁止在字段/变量声明中使用全限定类名：

```java
// ❌ 禁止
private java.math.BigDecimal entrustPrice;

// ✅ 正确（确保 import java.math.BigDecimal）
private BigDecimal entrustPrice;
```

---

## 二、构造器注入

统一使用 Lombok `@RequiredArgsConstructor` 替代手动构造器或 `@Autowired` 字段注入。

```java
@RestController
@RequiredArgsConstructor
public class XxxController {

    private final XxxService xxxService;    // 自动生成构造器注入

    @GetMapping("/{id}")
    public Result<XxxVO> getById(@PathVariable Long id) {
        // ...
    }
}
```

**规则：**
- 所有 `final` 字段通过 `@RequiredArgsConstructor` 自动注入
- 不使用 `@Autowired` 字段注入
- 不使用手动编写的构造器（除非需要 `ObjectProvider`、`@Qualifier` 等特殊逻辑）

**例外情况（保留手动构造器）：**
- `ObjectProvider<T>.getIfAvailable()` 模式
- `@Qualifier` 多实现注入
- 构造器内有额外初始化逻辑

---

## 三、日志规范

统一使用 Lombok `@Slf4j` 注解，禁止手动声明 Logger。

```java
// ✅ 正确
@Slf4j
@Service
public class XxxServiceImpl implements XxxService {
    public void doSomething() {
        log.info("操作成功: userId={}", userId);
        log.error("操作失败", exception);
    }
}
```

```java
// ❌ 禁止
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
private static final Logger log = LoggerFactory.getLogger(XxxService.class);
```

---

## 四、实体规范

### 4.1 继承 BaseEntity

所有业务实体继承 `com.wealth.common.entity.BaseEntity`，获得 `id` / `createTime` / `updateTime` / `delFlag`。

```java
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName("wea_product")
public class WeaProduct extends BaseEntity { ... }
```

### 4.2 @TableField 显式声明

所有自定义字段必须标注 `@TableField("列名")`，列名以 `init.sql` 为准。

```java
// ✅ 正确
@TableField("product_name")
private String productName;

@TableField("price")
private BigDecimal price;
```

### 4.3 字段不存在于数据库

使用 `@TableField(exist = false)` 排除，同时注意 `@TableLogic` 继承问题。

```java
/** 自选表无 del_flag 列 */
@TableField(exist = false)
private Integer delFlag;
```

**关键：** 若表无 `del_flag` 列，Service 层的删除操作必须使用物理删除，不能使用 `removeById()`（会触发 MP 逻辑删除生成 `UPDATE del_flag=1`）。

参见：[L6 — 物理删除示例](#TODO)

### 4.4 equals/hashCode

继承 `BaseEntity` 的类使用 `@EqualsAndHashCode(callSuper = true)`。注意：这会将 `id`、`createTime`、`updateTime`、`delFlag` 纳入计算。对于短期存在的 entity 实例通常无影响，但若需要跨时间比较，需考虑 `@EqualsAndHashCode(onlyExplicitlyIncluded = true)`。

---

## 五、Controller 规范

### 5.1 职责边界

Controller **只做三件事**：
1. 参数校验（`@Valid` / `@Validated` + 注解约束）
2. 路由分发（`@GetMapping` / `@PostMapping` 等）
3. 调用 Service 并包装返回结果

```java
// ✅ 正确 — Controller 仅路由
@PostMapping("/refresh")
public Result<TokenPair> refresh(@RequestHeader("Authorization") String authHeader) {
    return Result.success(umsAdminService.refreshToken(authHeader));
}
```

```java
// ❌ 禁止 — Token 提取/校验等业务逻辑
public Result<TokenPair> refresh(@RequestHeader("Authorization") String authHeader) {
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        return Result.error(ResultCode.TOKEN_INVALID);
    }
    String refreshToken = authHeader.substring(7);
    return Result.success(umsAdminService.refreshToken(refreshToken));
}
```

### 5.2 Swagger 注解

```java
@Tag(name = "模块名", description = "说明")
@Operation(summary = "接口功能描述")
```

### 5.3 禁止方法级 JavaDoc

Controller 方法以 `@Operation(summary = "")` 作为唯一描述来源，不写冗余的 JavaDoc。

```java
// ✅ 正确
@Operation(summary = "根据ID查询后台资源信息")
@GetMapping("/{id}")
public Result<UmsResourceVO> getById(@PathVariable Long id) { ... }

// ❌ 禁止 — JavaDoc 与 @Operation 重复
/**
 * 根据 ID 查询后台资源信息。
 */
@Operation(summary = "根据ID查询后台资源信息")
```

---

## 六、命名规范

| 元素 | 规则 | 示例 |
|------|------|------|
| 类名 | 大驼峰 | `UmsAdminService`, `ProductVO` |
| 方法名 / 变量名 | 小驼峰 | `getById()`, `pageWithFilter()` |
| 常量 | 大写+下划线 | `MAX_LOGIN_ATTEMPTS`, `COOKIE_MAX_AGE_SECONDS` |
| 表名 / 字段名 | 小写+下划线 | `wea_trade_order`, `order_no` |
| 包名 | 全小写 | `com.wealth.platform.system` |
| 测试方法 | 方法名_should_预期 | `login_should_return_token_when_password_correct` |

---

## 七、常量与魔法值

### 7.1 禁止魔法值

所有硬编码数值必须抽取为 `private static final` 常量，赋予业务含义的命名。

```java
// ✅ 正确
/** Cookie 有效期（秒），与 jwt.access-expire=1800000ms 对应（30分钟） */
private static final int COOKIE_MAX_AGE_SECONDS = 1800;

private static final int MAX_LOGIN_ATTEMPTS = 5;
private static final long LOCK_DURATION_MINUTES = 15;
private static final String KEY_LOGIN_FAIL_COUNT = "login:fail:count:";
```

```java
// ❌ 禁止
.maxAge(1800)
if (count >= 5) { ... }
```

### 7.2 状态码

HTTP 状态码和业务状态码使用 `ResultCode` 常量，禁止硬编码：

```java
// ✅ 正确
return Result.error(ResultCode.NOT_FOUND);

// ❌ 禁止
return Result.error(404, "不存在");
// 或
throw new ServiceException(401, "账号已锁定");  // 401 应用 ResultCode.UNAUTHORIZED
```

---

## 八、异常处理

### 8.1 业务异常

使用 `ServiceException(code, message)`：

```java
throw new ServiceException(404, "管理员不存在");
throw new ServiceException(400, "参数不合法");
```

### 8.2 禁止空 catch 块

```java
// ❌ 禁止
try {
    // ...
} catch (Exception e) {
    // 空的！吞异常
}

// ✅ 正确 — 至少记录日志
try {
    // ...
} catch (Exception e) {
    log.warn("操作异常: {}", e.getMessage());
}
```

### 8.3 异常信息语言统一

异常信息中保持单一语言（原则上使用中文），禁止中英文混杂：

```java
// ❌ 禁止
"用户不存在 or password wrong"
"username or password error"
"admin not found，请重新登录"

// ✅ 正确
"用户名或密码错误"
"管理员不存在"
```

---

## 九、事务规范

- 写操作（增、删、改）必须加 `@Transactional(rollbackFor = Exception.class)`
- 只读操作不需要 `@Transactional`
- 仅有单表 Redis 操作的写方法（如 `logout()`、`clearPermissionCache()`）当前不受影响，但新增涉及 DB 写入时须补上

---

## 十、成员变量声明顺序

类内成员按以下顺序声明：

```
1. private static final 常量
2. private final 注入字段
3. 普通成员变量（非 final、非 static）
4. 构造器（如果手动编写）
5. 方法（按逻辑分组）
```

```java
public class LoginInterceptor implements HandlerInterceptor {

    // 1. static final 常量
    private static final String TOKEN_COOKIE_NAME = "wealth_token";
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    // 2. final 注入字段
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    // 4. 方法
    @Override
    public boolean preHandle(...) { ... }
}
```

---

## 十一、JavaDoc 规范

| 位置 | 要求 |
|------|------|
| Controller 类 | 保留类级 JavaDoc |
| Controller 方法 | **不写**方法级 JavaDoc（用 `@Operation` 替代） |
| Service 接口 | 可选，核心方法建议写 |
| Service 实现 | 保留或参考接口 |
| Entity | 类级可选，字段级不写（用 `@TableField` 替代） |
| Mapper 自定义方法 | 复杂 SQL 写 JavaDoc |
| 工具类 | 建议写 |

---

## 十二、审计遗留项

以下问题在 2026-07-30 审计中识别，尚未修复：

| ID | 级别 | 位置 | 问题 | 工作量 |
|-----|------|------|------|--------|
| H3 | 高风险 | 约 12 处 | 魔法值未抽取常量 | ~20min |
| H4 | 高风险 | 5 个文件 | 异常信息中英文混杂 | ~10min |
| M7 | 中风险 | 全部 Entity | `@EqualsAndHashCode(callSuper=true)` 设计 | ~15min |
| L1 | 低风险 | 5 个方法 | 长方法（50~93 行）未拆分子方法 | ~30min |
| L3 | 低风险 | 2 个方法 | `@Transactional` 缺失 | ~10min |
