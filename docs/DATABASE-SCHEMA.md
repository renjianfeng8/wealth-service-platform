# 数据库表结构与字段
> 写实体类时引用 — 表结构、字段、BaseEntity 继承规则。

---

# 一、数据库规范（必须严格遵守）

1. 数据库名：wealth
2. 字符集：utf8mb4
3. 所有表必须包含：id、create_time、update_time、del_flag
4. 逻辑删除：del_flag 0=未删除 1=已删除
5. 主键统一使用 BIGINT 自增
6. 时间字段：DATETIME
7. 禁止使用外键，业务层关联
8. 索引必须按建表语句创建
完整建表 SQL：`wealth-common/src/main/resources/sql/init.sql`

数据库特殊例外：
- `wea_user_favorite` 无 del_flag 和 update_time 列（唯一无逻辑删除的表）
- `ums_admin` 无 update_time 列
# 二、当前项目所有表（必须严格对应）

## 1. 用户模块
sys_user              # 系统用户表
## 2. 产品&行情模块
wea_product           # 产品表
wea_market_data       # 行情数据表
## 3. 自选模块
wea_user_favorite     # 用户自选表（无 del_flag 列，物理删除）
## 4. 交易模块
wea_trade_order       # 交易委托单
## 5. 资讯&消息
wea_news              # 财经资讯
wea_message           # 站内消息

## 6. 后台权限模块
ums_admin             # 管理员
ums_role              # 角色
ums_resource          # 资源
ums_admin_role_relation
ums_role_resource_relation

# 三、BaseEntity 继承规范

## 实体类规范
1. 所有 Entity 必须继承 `com.wealth.common.entity.BaseEntity`（自动包含 id/create_time/update_time/del_flag 四个基础字段）
2. `@TableName("表名")` — 必须明确指定表名
3. `@TableLogic` 已在 BaseEntity.delFlag 上定义，子类无需重复声明
4. 若表无 del_flag 列，子类中重写 `@TableField(exist = false) private Integer delFlag;`
5. 字段映射统一使用 `@TableField("列名")`
6. 自动填充字段：create_time 使用 `@TableField(fill = FieldFill.INSERT)`，update_time 使用 `@TableField(fill = FieldFill.INSERT_UPDATE)`

### BaseEntity 定义（wealth-common/entity/BaseEntity.java）
```java
@Data
public class BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("del_flag")
    private Integer delFlag;
}
```

### BaseEntity 继承规则

所有 Entity 必须继承 BaseEntity，每个基础字段按以下规则处理：

| 字段 | BaseEntity 定义 | 无对应列的子类处理方式 |
|------|----------------|----------------------|
| id | `@TableId(type = IdType.AUTO)` | 无需处理，自动继承 |
| create_time | `@TableField(fill = FieldFill.INSERT)` | 若表中无该列，子类中重写：`@TableField(exist = false) private LocalDateTime createTime;` |
| update_time | `@TableField(fill = FieldFill.INSERT_UPDATE)` | 若表中无该列，子类中重写：`@TableField(exist = false) private LocalDateTime updateTime;` |
| del_flag | `@TableLogic @TableField("del_flag")` | 若表中无该列，子类中重写：`@TableField(exist = false) private Integer delFlag;` |

当前项目中：
- **WeaUserFavorite** — 唯一覆盖 delFlag（`exist=false`）和 updateTime（`exist=false`）的实体
- **UmsAdmin** — 覆盖 updateTime（`exist=false`），`ums_admin` 表无该列
> 注意：子类重写字段时须同时使用 `@EqualsAndHashCode(callSuper = true)`（或在类上加 `@Getter @Setter @EqualsAndHashCode(callSuper = true)` 替代 `@Data`），以确保 Lombok 正确处理父类字段。
