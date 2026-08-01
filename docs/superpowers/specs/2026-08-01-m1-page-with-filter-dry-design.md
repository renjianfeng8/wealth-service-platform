# M1 · 分页查询样板 pageWithFilter DRY 修复 — 设计文档

> 日期：2026-08-01
> 关联：`docs/superpowers/specs/2026-08-01-m2-controller-crud-dry-design.md`（M2 先例：样板收敛进 `BaseBizServiceImpl`）

---

## 一、背景与目标

`new Page<>(...) → LambdaQueryWrapper → if 守卫 → orderBy → baseMapper.selectPage(...)` 的
分页样板在多个 Service 重复。目标是按《阿里巴巴 Java 开发手册》DRY 条款，将该样板收敛为
`BaseBizServiceImpl` 上的一个泛型模板方法，守卫与 LIKE 转义逻辑收敛到唯一入口。

**范围（用户已确认）：**
- 方案：**方案 A**（BaseBizServiceImpl 泛型模板 + 条件数组）
- 作用域：**仅 8 处 `pageWithFilter` 方法**；4 处 VO 变体（`pageProducts` / `pageOrders` /
  `pageNews` / `pageMessages`）本次不动。

**不在本次范围：** Controller 层、4 处 VO 变体、`init.sql`、`application.yml`、前端。

---

## 二、现状（检测结论）

`selectPage` 样板共 **12 处**。本次只处理其中 8 处 `pageWithFilter`：

| Service | 条件 | 排序 |
|---|---|---|
| UmsAdminServiceImpl | like(username), eq(status) | desc(createTime) |
| UmsRoleServiceImpl | like(name), eq(status) | **asc(sort)** |
| UmsResourceServiceImpl | like(name), like(url) | desc(createTime) |
| UmsAdminRoleRelationServiceImpl | eq(adminId) | **无** |
| UmsRoleResourceRelationServiceImpl | eq(roleId) | **无** |
| UserServiceImpl | like(username), eq(status) | desc(createTime) |
| UserFavoriteServiceImpl | **eq(userId)（>0 守卫）**, like(productCode) | desc(createTime) |
| MarketDataServiceImpl | like(productCode) | desc(marketTime) |

**检测修正：** 当前 8 处 `.like()` 全部已用 `LikeUtil.escape`，ticket 所述"3 处未转义"为
过时表述。风险是**潜伏型**——新增模糊字段时易漏写转义。收敛转义到唯一入口是本次修复的
核心价值。

**模板化必须保留的差异点：**
1. String 条件用 `hasText` + `like`（自动转义）；非 String 用 `!= null` + `eq`
2. UserFavorite 的 `userId > 0`（0 = 不筛选），需专用工厂 `positiveEq`
3. 排序：asc / desc / 无 三种（8 处内无动态排序）

---

## 三、方案 A 详细设计

### 3.1 BaseBizServiceImpl 新增（一次性基础设施）

在 `wealth-service/.../common/base/BaseBizServiceImpl.java` 新增：

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

新增 import：`com.baomidou.mybatisplus.core.metadata.IPage`、`com.wealth.common.utils.LikeUtil`、
`org.springframework.util.StringUtils`。

**与现有 8 个 Service 同名方法的关系：** 模板为 `(Integer, Integer, OrderSpec, Condition...)`
四参签名，Service 方法为 `(Integer, Integer, 各业务条件...)` 签名，二者是**重载**而非重写，
无冲突；模板声明为 `final` 以满足 `@SafeVarargs` 约束（instance 方法）。

### 3.2 8 处调用侧改写（声明式一行）

| Service | 改写后 |
|---|---|
| UmsAdminServiceImpl | `return pageWithFilter(pageNum, pageSize, orderByDesc(UmsAdmin::getCreateTime), like(UmsAdmin::getUsername, username), eq(UmsAdmin::getStatus, status));` |
| UmsRoleServiceImpl | `return pageWithFilter(pageNum, pageSize, orderByAsc(UmsRole::getSort), like(UmsRole::getName, name), eq(UmsRole::getStatus, status));` |
| UmsResourceServiceImpl | `return pageWithFilter(pageNum, pageSize, orderByDesc(UmsResource::getCreateTime), like(UmsResource::getName, name), like(UmsResource::getUrl, url));` |
| UmsAdminRoleRelationServiceImpl | `return pageWithFilter(pageNum, pageSize, null, eq(UmsAdminRoleRelation::getAdminId, adminId));` |
| UmsRoleResourceRelationServiceImpl | `return pageWithFilter(pageNum, pageSize, null, eq(UmsRoleResourceRelation::getRoleId, roleId));` |
| UserServiceImpl | `return pageWithFilter(pageNum, pageSize, orderByDesc(User::getCreateTime), like(User::getUsername, username), eq(User::getStatus, status));` |
| UserFavoriteServiceImpl | `return pageWithFilter(pageNum, pageSize, orderByDesc(WeaUserFavorite::getCreateTime), positiveEq(WeaUserFavorite::getUserId, userId), like(WeaUserFavorite::getProductCode, productCode));` |
| MarketDataServiceImpl | `return pageWithFilter(pageNum, pageSize, orderByDesc(WeaMarketData::getMarketTime), like(WeaMarketData::getProductCode, productCode));` |

### 3.3 行为等价性（不得改变任何 SQL 语义）

- `like(col, x)` ≡ 原 `if (hasText(x)) wrapper.like(col, LikeUtil.escape(x))`
- `eq(col, x)` ≡ 原 `if (x != null) wrapper.eq(col, x)`
- `positiveEq(col, x)` ≡ 原 `if (x != null && x > 0) wrapper.eq(col, x)`
- 排序与不排序逐一对齐

### 3.4 清理失效 import

改写后各 Service 中不再使用的 import 须删除（逐文件核对，保留该文件其他方法仍在用的）：
`LambdaQueryWrapper` / `Page` / `LikeUtil` / `StringUtils` 视各文件而定。

---

## 四、测试计划

在 `BaseBizServiceImplTest`（M2 已建）中新增模板方法测试，验证语义等价且不回归：

1. `pageWithFilter_should_escape_like_wildcards` — 含 `%` / `_` 的值被转义
2. `pageWithFilter_should_skip_like_when_blank` — 空白字符串不加 like 条件
3. `pageWithFilter_should_skip_eq_when_null` — null 不加 eq 条件
4. `pageWithFilter_should_skip_positiveEq_when_zero_or_negative` — 0 / 负数 / null 不加条件
5. `pageWithFilter_should_apply_order_when_provided` — asc / desc 生效
6. `pageWithFilter_should_not_order_when_order_null` — null 排序不生效

现有各 Service 的 `pageWithFilter` 无独立测试，改写后以模板测试覆盖语义，不新增
Service 级分页测试。

---

## 五、约束与禁止

- 不修改 Controller（8 处 Controller 仅做 `convertPage`，签名不变，无需改动）
- 不改动 4 处 VO 变体（`pageProducts` 等）
- 不修改 `init.sql` / `application.yml` / 前端
- 所有异常消息保持中文；无通配符 import；无魔法值
- 模板方法不对外暴露，保持 `protected`

---

## 六、验证清单

- [ ] `mvn clean install -pl wealth-common -DskipTests` 通过
- [ ] `mvn test -pl wealth-service -DskipTests=false` 全量通过
- [ ] grep 确认 8 处 Service 的 `pageWithFilter` 已收敛为模板调用
- [ ] 无残留失效 import（编译通过即证）
