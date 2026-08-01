# M1 · pageWithFilter 分页样板 DRY 修复实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.
>
> **提交约束（项目 CLAUDE.md，优先级最高）：** 本计划所有任务**不自动 git commit**。实现 + 验证完成后，等用户明确指令再提交/推送。

**Goal:** 将 8 处 `pageWithFilter` 分页样板收敛为 `BaseBizServiceImpl` 上的一个泛型模板方法，like/eq 守卫与 `LikeUtil.escape` 转义收敛到唯一入口。

**Architecture:** 在 `BaseBizServiceImpl` 增加 `MatchType` / `Condition<E>` / `OrderSpec<E>` 三种嵌套类型与 `like` / `eq` / `positiveEq` / `orderByAsc` / `orderByDesc` 工厂方法，加一个 `@SafeVarargs protected final` 模板方法 `pageWithFilter(pageNum, pageSize, order, conditions...)`。8 个 Service 的 `pageWithFilter` 改写为一行声明式调用并清理失效 import。模板与 Service 方法签名不同，构成重载而非重写，无冲突。

**Tech Stack:** Java 21、Spring Boot 3.3.13、MyBatis-Plus 3.5.9、JUnit 5 + Mockito。

**设计文档：** `docs/superpowers/specs/2026-08-01-m1-page-with-filter-dry-design.md`

---

## 文件结构总览

| 文件 | 操作 |
|------|------|
| `wealth-service/.../common/base/BaseBizServiceImpl.java` | Modify：加模板基础设施 |
| `wealth-service/.../common/base/BaseBizServiceImplTest.java` | Modify：加 6 个模板测试，改实体类型 |
| `wealth-service/.../system/service/impl/UmsAdminServiceImpl.java` | Modify：改写 + 清理 import |
| `wealth-service/.../system/service/impl/UmsRoleServiceImpl.java` | Modify：改写 + 清理 import |
| `wealth-service/.../system/service/impl/UmsResourceServiceImpl.java` | Modify：改写 + 清理 import |
| `wealth-service/.../system/service/impl/UmsAdminRoleRelationServiceImpl.java` | Modify：改写 + 清理 import |
| `wealth-service/.../system/service/impl/UmsRoleResourceRelationServiceImpl.java` | Modify：改写 + 清理 import |
| `wealth-service/.../user/service/impl/UserServiceImpl.java` | Modify：改写 + 清理 import |
| `wealth-service/.../product/service/impl/UserFavoriteServiceImpl.java` | Modify：改写 + 清理 import |
| `wealth-service/.../product/service/impl/MarketDataServiceImpl.java` | Modify：改写 + 清理 import |

**不在范围：** 4 处 VO 变体（`pageProducts` / `pageOrders` / `pageNews` / `pageMessages`）、Controller、`init.sql`、`application.yml`、前端。

---

### Task 1: BaseBizServiceImpl 分页模板基础设施（TDD）

**Files:**
- Modify: `wealth-service/src/main/java/com/wealth/platform/common/base/BaseBizServiceImpl.java`
- Modify: `wealth-service/src/test/java/com/wealth/platform/common/base/BaseBizServiceImplTest.java`

本任务同时改测试与实现。模板方法 `pageWithFilter` 与测试类 `TestServiceImpl` 的同名方法 `pageWithFilter(Integer, Integer, String, Integer)` 是**重载**，模板为四参签名 `(Integer, Integer, OrderSpec<E>, Condition<E>...)`，无冲突。

- [ ] **Step 1: 改写测试文件 — 实体类型 + 新增 6 个模板测试**

打开 `BaseBizServiceImplTest.java`，做以下三处修改。

**(a) 修改 import 区。** 在 `import static org.mockito.Mockito.when;` 之后新增：

```java
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
```

在 `import com.baomidou.mybatisplus.core.mapper.BaseMapper;` 之前新增：

```java
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
```

在 `import com.wealth.common.exception.ServiceException;` 之后新增：

```java
import lombok.Data;
import lombok.EqualsAndHashCode;
```

在 `import org.mockito.Mock;` 之后新增：

```java
import org.mockito.ArgumentCaptor;
```

**(b) 将测试实体改为带 name/sort 字段的 TestEntity。** 将：

```java
    interface TestMapper extends BaseMapper<BaseEntity> {}

    static class TestServiceImpl extends BaseBizServiceImpl<TestMapper, BaseEntity> {}
```

替换为：

```java
    @Data
    @EqualsAndHashCode(callSuper = true)
    static class TestEntity extends BaseEntity {
        @TableField("name")
        private String name;
        @TableField("sort")
        private Integer sort;
    }

    interface TestMapper extends BaseMapper<TestEntity> {}

    static class TestServiceImpl extends BaseBizServiceImpl<TestMapper, TestEntity> {}
```

**(c) 现有两处 `BaseEntity entity = new BaseEntity();` 改为 `TestEntity entity = new TestEntity();`**（第 63 行处 getEntityOrThrow_whenFound_returnsEntity 方法内；第 41 行处 `when(testMapper.selectById(99L)).thenReturn(null);` 不用改）。即：

```java
        TestEntity entity = new TestEntity();
```

**(d) 在类末尾（最后一个 `}` 之前）新增 captureWrapper 辅助方法与 6 个测试：**

```java
    @SuppressWarnings({"unchecked", "rawtypes"})
    private LambdaQueryWrapper<TestEntity> captureWrapper() {
        ArgumentCaptor<Wrapper> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(testMapper).selectPage(any(Page.class), captor.capture());
        return (LambdaQueryWrapper<TestEntity>) captor.getValue();
    }

    @Test
    @DisplayName("pageWithFilter-like自动转义通配符")
    void pageWithFilter_should_escape_like_wildcards() {
        when(testMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(new Page<>());

        testService.pageWithFilter(1, 10, null, like(TestEntity::getName, "50%"));

        assertTrue(captureWrapper().getParamNameValuePairs().values().contains("50\\%"));
    }

    @Test
    @DisplayName("pageWithFilter-like空白值跳过")
    void pageWithFilter_should_skip_like_when_blank() {
        when(testMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(new Page<>());

        testService.pageWithFilter(1, 10, null, like(TestEntity::getName, "   "));

        assertTrue(captureWrapper().getParamNameValuePairs().isEmpty());
    }

    @Test
    @DisplayName("pageWithFilter-eq null值跳过")
    void pageWithFilter_should_skip_eq_when_null() {
        when(testMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(new Page<>());

        testService.pageWithFilter(1, 10, null, eq(TestEntity::getId, null));

        assertTrue(captureWrapper().getParamNameValuePairs().isEmpty());
    }

    @Test
    @DisplayName("pageWithFilter-positiveEq 0值跳过")
    void pageWithFilter_should_skip_positiveEq_when_zero_or_negative() {
        when(testMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(new Page<>());

        testService.pageWithFilter(1, 10, null, positiveEq(TestEntity::getId, 0L));

        assertTrue(captureWrapper().getParamNameValuePairs().isEmpty());
    }

    @Test
    @DisplayName("pageWithFilter-应用排序")
    void pageWithFilter_should_apply_order_when_provided() {
        when(testMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(new Page<>());

        testService.pageWithFilter(1, 10, orderByDesc(TestEntity::getCreateTime));

        String sql = captureWrapper().getSqlSegment();
        assertTrue(sql.contains("create_time"));
        assertTrue(sql.contains("DESC"));
    }

    @Test
    @DisplayName("pageWithFilter-null排序不生效")
    void pageWithFilter_should_not_order_when_order_null() {
        when(testMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(new Page<>());

        testService.pageWithFilter(1, 10, null);

        assertFalse(captureWrapper().getSqlSegment().contains("ORDER BY"));
    }
```

> **执行修正记录（2026-08-01 实际执行时发现，均已落实）：**
> 1. **静态导入工厂方法**：测试类 `BaseBizServiceImplTest` 不是 `BaseBizServiceImpl` 的子类，`like`/`eq`/`positiveEq`/`orderByDesc` 是受保护静态方法，必须静态导入才能非限定调用：
>    `import static com.wealth.platform.common.base.BaseBizServiceImpl.like;`（4 个工厂同理）。
> 2. **TestEntity 需注册 TableInfo**：单元测试无 MyBatis 初始化，LambdaQueryWrapper 解析列名会报
>    `MybatisPlus can not find lambda cache`。解决：TestEntity 加 `@TableName("test_entity")`，并在 `setUp()` 中
>    `TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), TestEntity.class);`
>    （需 import `com.baomidou.mybatisplus.core.MybatisConfiguration`、`com.baomidou.mybatisplus.core.metadata.TableInfoHelper`、`org.apache.ibatis.builder.MapperBuilderAssistant`）。
> 3. **参数懒填充**：`getParamNameValuePairs()` 在条件渲染后才填充，断言前必须先调用 `w.getSqlSegment()`。
> 4. **like 值被 MP 包装为 `%值%`**：参数实际为 `%50\%%`，断言须用子串匹配而非 `Collection.contains` 相等：
>    `w.getParamNameValuePairs().values().stream().anyMatch(v -> String.valueOf(v).contains("50\\%"))`。

- [ ] **Step 2: 运行测试，确认编译失败**

Run: `mvn test -pl wealth-service -Dtest=BaseBizServiceImplTest -DskipTests=false`
Expected: FAIL —— 编译错误：`like` / `eq` / `positiveEq` / `orderByDesc` / `pageWithFilter` 在 `BaseBizServiceImpl` 中不存在。

- [ ] **Step 3: 实现模板基础设施**

打开 `BaseBizServiceImpl.java`。在 import 区（`import com.baomidou.mybatisplus.extension.plugins.pagination.Page;` 之后）新增：

```java
import com.baomidou.mybatisplus.core.metadata.IPage;
```

在 import 区（`import com.wealth.common.exception.ServiceException;` 之后）新增：

```java
import com.wealth.common.utils.LikeUtil;
```

在 import 区（`import java.util.List;` 之前）新增：

```java
import org.springframework.util.StringUtils;
```

在类末尾（`getEntityOrThrow` 方法结束的 `}` 之后、类结束的 `}` 之前）新增以下整块代码：

```java
    /** 分页条件匹配类型 */
    protected enum MatchType { LIKE, EQ }

    /** 分页条件描述：列 + 值 + 匹配方式 */
    protected record Condition<E>(SFunction<E, ?> column, Object value, MatchType type) {}

    /** 分页排序描述 */
    @FunctionalInterface
    protected interface OrderSpec<E> {
        void apply(LambdaQueryWrapper<E> wrapper);
    }

    /** 模糊条件：自动 hasText 守卫 + LikeUtil.escape 转义 */
    protected static <E> Condition<E> like(SFunction<E, ?> column, String value) {
        return new Condition<>(column, value, MatchType.LIKE);
    }

    /** 等值条件：自动 != null 守卫 */
    protected static <E> Condition<E> eq(SFunction<E, ?> column, Object value) {
        return new Condition<>(column, value, MatchType.EQ);
    }

    /** 正数等值条件：value 为 null 或 <= 0 时不参与过滤（如 userId，0 表示"全部"） */
    protected static <E> Condition<E> positiveEq(SFunction<E, ?> column, Long value) {
        return new Condition<>(column, value != null && value > 0 ? value : null, MatchType.EQ);
    }

    protected static <E> OrderSpec<E> orderByAsc(SFunction<E, ?> column) {
        return w -> w.orderByAsc(column);
    }

    protected static <E> OrderSpec<E> orderByDesc(SFunction<E, ?> column) {
        return w -> w.orderByDesc(column);
    }

    /**
     * 分页查询模板：收敛 new Page → LambdaQueryWrapper → if 守卫 → orderBy → selectPage 样板。
     * LIKE 条件自动 hasText 守卫 + LikeUtil.escape 转义；EQ 条件自动 != null 守卫。
     *
     * @param pageNum    页码（>=1）
     * @param pageSize   每页条数
     * @param order      排序，为 null 时不排序
     * @param conditions 过滤条件（like / eq / positiveEq）
     */
    @SafeVarargs
    protected final IPage<E> pageWithFilter(Integer pageNum, Integer pageSize,
            OrderSpec<E> order, Condition<E>... conditions) {
        Page<E> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<E> wrapper = new LambdaQueryWrapper<>();
        for (Condition<E> condition : conditions) {
            if (condition.type() == MatchType.LIKE) {
                String value = (String) condition.value();
                if (StringUtils.hasText(value)) {
                    wrapper.like(condition.column(), LikeUtil.escape(value));
                }
            } else if (condition.value() != null) {
                wrapper.eq(condition.column(), condition.value());
            }
        }
        if (order != null) {
            order.apply(wrapper);
        }
        return baseMapper.selectPage(page, wrapper);
    }
```

- [ ] **Step 4: 运行测试，确认通过**

Run: `mvn test -pl wealth-service -Dtest=BaseBizServiceImplTest -DskipTests=false`
Expected: PASS —— 6 个新测试 + 3 个既有测试全部通过（Tests run: 9）。

- [ ] **Step 5: 编译验证（不提交）**

Run: `mvn compile -pl wealth-service`
Expected: BUILD SUCCESS。确认无误后进入 Task 2。

---

### Task 2: system 域 5 处 pageWithFilter 改写

**Files:**
- Modify: `wealth-service/src/main/java/com/wealth/platform/system/service/impl/UmsAdminServiceImpl.java`
- Modify: `wealth-service/src/main/java/com/wealth/platform/system/service/impl/UmsRoleServiceImpl.java`
- Modify: `wealth-service/src/main/java/com/wealth/platform/system/service/impl/UmsResourceServiceImpl.java`
- Modify: `wealth-service/src/main/java/com/wealth/platform/system/service/impl/UmsAdminRoleRelationServiceImpl.java`
- Modify: `wealth-service/src/main/java/com/wealth/platform/system/service/impl/UmsRoleResourceRelationServiceImpl.java`

- [ ] **Step 1: 改写 UmsAdminServiceImpl.pageWithFilter**

将 `UmsAdminServiceImpl.java` 中的：

```java
    @Override
    public IPage<UmsAdmin> pageWithFilter(Integer pageNum, Integer pageSize, String username, Integer status) {
        Page<UmsAdmin> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UmsAdmin> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            wrapper.like(UmsAdmin::getUsername, LikeUtil.escape(username));
        }
        if (status != null) {
            wrapper.eq(UmsAdmin::getStatus, status);
        }
        wrapper.orderByDesc(UmsAdmin::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }
```

替换为：

```java
    @Override
    public IPage<UmsAdmin> pageWithFilter(Integer pageNum, Integer pageSize, String username, Integer status) {
        return pageWithFilter(pageNum, pageSize, orderByDesc(UmsAdmin::getCreateTime),
                like(UmsAdmin::getUsername, username), eq(UmsAdmin::getStatus, status));
    }
```

删除该文件 import 区中不再使用的三行：

```java
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.common.utils.LikeUtil;
```

> 保留 `IPage`（方法返回类型）与 `StringUtils`（其他方法如 login 仍在使用）。

- [ ] **Step 2: 改写 UmsRoleServiceImpl.pageWithFilter**

将 `UmsRoleServiceImpl.java` 中的：

```java
    @Override
    public IPage<UmsRole> pageWithFilter(Integer pageNum, Integer pageSize, String name, Integer status) {
        Page<UmsRole> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UmsRole> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(UmsRole::getName, LikeUtil.escape(name));
        }
        if (status != null) {
            wrapper.eq(UmsRole::getStatus, status);
        }
        wrapper.orderByAsc(UmsRole::getSort);
        return baseMapper.selectPage(page, wrapper);
    }
```

替换为：

```java
    @Override
    public IPage<UmsRole> pageWithFilter(Integer pageNum, Integer pageSize, String name, Integer status) {
        return pageWithFilter(pageNum, pageSize, orderByAsc(UmsRole::getSort),
                like(UmsRole::getName, name), eq(UmsRole::getStatus, status));
    }
```

删除该文件 import 区中不再使用的四行：

```java
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.common.utils.LikeUtil;
import org.springframework.util.StringUtils;
```

> 保留 `IPage`（方法返回类型）。

- [ ] **Step 3: 改写 UmsResourceServiceImpl.pageWithFilter**

将 `UmsResourceServiceImpl.java` 中的：

```java
    @Override
    public IPage<UmsResource> pageWithFilter(Integer pageNum, Integer pageSize, String name, String url) {
        Page<UmsResource> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UmsResource> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(UmsResource::getName, LikeUtil.escape(name));
        }
        if (StringUtils.hasText(url)) {
            wrapper.like(UmsResource::getUrl, LikeUtil.escape(url));
        }
        wrapper.orderByDesc(UmsResource::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }
```

替换为：

```java
    @Override
    public IPage<UmsResource> pageWithFilter(Integer pageNum, Integer pageSize, String name, String url) {
        return pageWithFilter(pageNum, pageSize, orderByDesc(UmsResource::getCreateTime),
                like(UmsResource::getName, name), like(UmsResource::getUrl, url));
    }
```

删除该文件 import 区中不再使用的三行：

```java
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.common.utils.LikeUtil;
import org.springframework.util.StringUtils;
```

> 保留 `LambdaQueryWrapper`（`getUrlByResourceIds` 仍在使用）与 `IPage`。

- [ ] **Step 4: 改写 UmsAdminRoleRelationServiceImpl.pageWithFilter**

将 `UmsAdminRoleRelationServiceImpl.java` 中的：

```java
    @Override
    public IPage<UmsAdminRoleRelation> pageWithFilter(Integer pageNum, Integer pageSize, Long adminId) {
        Page<UmsAdminRoleRelation> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UmsAdminRoleRelation> wrapper = new LambdaQueryWrapper<>();
        if (adminId != null) {
            wrapper.eq(UmsAdminRoleRelation::getAdminId, adminId);
        }
        return baseMapper.selectPage(page, wrapper);
    }
```

替换为：

```java
    @Override
    public IPage<UmsAdminRoleRelation> pageWithFilter(Integer pageNum, Integer pageSize, Long adminId) {
        return pageWithFilter(pageNum, pageSize, null, eq(UmsAdminRoleRelation::getAdminId, adminId));
    }
```

删除该文件 import 区中不再使用的一行：

```java
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
```

> 保留 `LambdaQueryWrapper`（`getRoleIdByAdminId` / `getAdminIdByRoleId` 仍在使用）与 `IPage`。

- [ ] **Step 5: 改写 UmsRoleResourceRelationServiceImpl.pageWithFilter**

将 `UmsRoleResourceRelationServiceImpl.java` 中的：

```java
    @Override
    public IPage<UmsRoleResourceRelation> pageWithFilter(Integer pageNum, Integer pageSize, Long roleId) {
        Page<UmsRoleResourceRelation> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UmsRoleResourceRelation> wrapper = new LambdaQueryWrapper<>();
        if (roleId != null) {
            wrapper.eq(UmsRoleResourceRelation::getRoleId, roleId);
        }
        return baseMapper.selectPage(page, wrapper);
    }
```

替换为：

```java
    @Override
    public IPage<UmsRoleResourceRelation> pageWithFilter(Integer pageNum, Integer pageSize, Long roleId) {
        return pageWithFilter(pageNum, pageSize, null, eq(UmsRoleResourceRelation::getRoleId, roleId));
    }
```

删除该文件 import 区中不再使用的一行：

```java
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
```

> 保留 `LambdaQueryWrapper`（`getResourceIdByRoleIds` 仍在使用）与 `IPage`。

- [ ] **Step 6: 编译并运行 system 域相关测试**

Run: `mvn test -pl wealth-service -Dtest=UmsAdminServiceImplTest,UmsRoleServiceImplTest,UmsResourceServiceImplTest -DskipTests=false`
Expected: PASS（UmsAdmin 与 UmsRole 的 Service 测试；UmsResource 无测试类则忽略）。

Run: `mvn compile -pl wealth-service`
Expected: BUILD SUCCESS。

---

### Task 3: user 域 UserServiceImpl 改写

**Files:**
- Modify: `wealth-service/src/main/java/com/wealth/platform/user/service/impl/UserServiceImpl.java`

- [ ] **Step 1: 改写 pageWithFilter**

将 `UserServiceImpl.java` 中的：

```java
    @Override
    public IPage<User> pageWithFilter(Integer pageNum, Integer pageSize, String username, Integer status) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            wrapper.like(User::getUsername, LikeUtil.escape(username));
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }
```

替换为：

```java
    @Override
    public IPage<User> pageWithFilter(Integer pageNum, Integer pageSize, String username, Integer status) {
        return pageWithFilter(pageNum, pageSize, orderByDesc(User::getCreateTime),
                like(User::getUsername, username), eq(User::getStatus, status));
    }
```

删除该文件 import 区中不再使用的三行：

```java
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.common.utils.LikeUtil;
```

> 保留 `IPage`（方法返回类型）与 `StringUtils`（login / register / resetPassword 等方法仍在使用，见第 44/54/79/134/137/145 行）。

- [ ] **Step 2: 编译并运行 user 域相关测试**

Run: `mvn test -pl wealth-service -Dtest=UserServiceImplTest -DskipTests=false`
Expected: PASS。

Run: `mvn compile -pl wealth-service`
Expected: BUILD SUCCESS。

---

### Task 4: product 域 UserFavorite / MarketData 改写

**Files:**
- Modify: `wealth-service/src/main/java/com/wealth/platform/product/service/impl/UserFavoriteServiceImpl.java`
- Modify: `wealth-service/src/main/java/com/wealth/platform/product/service/impl/MarketDataServiceImpl.java`

- [ ] **Step 1: 改写 UserFavoriteServiceImpl.pageWithFilter**

将 `UserFavoriteServiceImpl.java` 中的：

```java
    @Override
    public IPage<WeaUserFavorite> pageWithFilter(Integer pageNum, Integer pageSize, Long userId, String productCode) {
        Page<WeaUserFavorite> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<WeaUserFavorite> wrapper = new LambdaQueryWrapper<>();
        if (userId != null && userId > 0) {
            wrapper.eq(WeaUserFavorite::getUserId, userId);
        }
        if (StringUtils.hasText(productCode)) {
            wrapper.like(WeaUserFavorite::getProductCode, LikeUtil.escape(productCode));
        }
        wrapper.orderByDesc(WeaUserFavorite::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }
```

替换为：

```java
    @Override
    public IPage<WeaUserFavorite> pageWithFilter(Integer pageNum, Integer pageSize, Long userId, String productCode) {
        return pageWithFilter(pageNum, pageSize, orderByDesc(WeaUserFavorite::getCreateTime),
                positiveEq(WeaUserFavorite::getUserId, userId), like(WeaUserFavorite::getProductCode, productCode));
    }
```

删除该文件 import 区中不再使用的两行：

```java
import com.wealth.common.utils.LikeUtil;
import org.springframework.util.StringUtils;
```

> 保留 `LambdaQueryWrapper`（createFavorite 中 existsBy 仍在使用）与 `Page`（getFavoriteList 仍在使用）。

- [ ] **Step 2: 改写 MarketDataServiceImpl.pageWithFilter**

将 `MarketDataServiceImpl.java` 中的：

```java
    @Override
    public IPage<WeaMarketData> pageWithFilter(Integer pageNum, Integer pageSize, String productCode) {
        Page<WeaMarketData> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<WeaMarketData> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(productCode)) {
            wrapper.like(WeaMarketData::getProductCode, LikeUtil.escape(productCode));
        }
        wrapper.orderByDesc(WeaMarketData::getMarketTime);
        return baseMapper.selectPage(page, wrapper);
    }
```

替换为：

```java
    @Override
    public IPage<WeaMarketData> pageWithFilter(Integer pageNum, Integer pageSize, String productCode) {
        return pageWithFilter(pageNum, pageSize, orderByDesc(WeaMarketData::getMarketTime),
                like(WeaMarketData::getProductCode, productCode));
    }
```

删除该文件 import 区中不再使用的三行：

```java
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wealth.common.utils.LikeUtil;
import org.springframework.util.StringUtils;
```

> 保留 `LambdaQueryWrapper`（findCandles 仍在使用）与 `IPage`。

- [ ] **Step 3: 编译并运行 product 域相关测试**

Run: `mvn test -pl wealth-service -Dtest=UserFavoriteServiceImplTest,MarketDataServiceImplTest -DskipTests=false`
Expected: PASS。

Run: `mvn compile -pl wealth-service`
Expected: BUILD SUCCESS。

---

### Task 5: 全量验证

**Files:** 无代码改动

- [ ] **Step 1: 全量测试**

Run: `mvn test -pl wealth-service -DskipTests=false`
Expected: 全部通过（含 BaseBizServiceImplTest 9 个 + 各域 Service 测试）。

- [ ] **Step 2: 全量编译**

Run: `mvn clean install -pl wealth-service -DskipTests`
Expected: BUILD SUCCESS。

- [ ] **Step 3: 收敛确认（grep）**

Run: `grep -rn "return pageWithFilter(pageNum, pageSize" wealth-service/src/main/java`
Expected: 恰好 8 处（system 5 + user 1 + product 2），且全部以模板工厂调用形式出现。

Run: `grep -rln "LikeUtil" wealth-service/src/main/java/com/wealth/platform | grep "service/impl"`
Expected: 仅剩 `BaseBizServiceImpl.java`（模板内）与 `ProductServiceImpl` / `TradeOrderServiceImpl` / `NewsServiceImpl` / `MessageServiceImpl`（4 处 VO 变体，不在本次范围）。

- [ ] **Step 4: 结果汇报**

向用户汇报：8 处 pageWithFilter 已收敛为模板调用；`like/eq/positiveEq` 守卫与转义集中唯一入口；编译 + 全量测试通过。**不自动提交/推送，等待用户明确指令。**

---

## Self-Review（计划自检）

**Spec 覆盖：**
- BaseBizServiceImpl 模板基础设施 → Task 1 Step 3 ✓
- MatchType / Condition / OrderSpec / 工厂方法 → Task 1 Step 3 ✓
- 8 处调用改写 → Task 2/3/4 ✓
- 行为等价性 → 各 Step 替换代码与原实现逐一对齐 ✓
- import 清理 → 各 Step 逐文件列出 ✓
- 测试 → Task 1 六类语义 + Task 5 全量 ✓
- 不改 Controller / VO 变体 / init.sql / 前端 → 无对应任务 ✓

**占位符扫描：** 所有代码步骤均为完整代码块，无 TBD/TODO。

**类型一致性：**
- 模板签名 `pageWithFilter(Integer, Integer, OrderSpec<E>, Condition<E>...)` 全计划一致 ✓
- 工厂 `like(SFunction<E,?>, String)` / `eq(SFunction<E,?>, Object)` / `positiveEq(SFunction<E,?>, Long)` / `orderByAsc·orderByDesc(SFunction<E,?>)` 与各调用一致 ✓
- 测试中 `positiveEq(TestEntity::getId, 0L)`、`eq(TestEntity::getId, null)`、`like(TestEntity::getName, ...)` 与工厂签名一致 ✓
- import 保留/删除判断已按各文件其他方法实际使用逐一核对 ✓
