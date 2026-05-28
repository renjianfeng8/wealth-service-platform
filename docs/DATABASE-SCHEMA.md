
# 数据库表结构与字段

> 写实体类时引用 — 表结构、字段、BaseEntity 继承规则。
> 完整建表 SQL 见 `wealth-common/src/main/resources/sql/init.sql`。

---

## 一、数据库规范

1. **数据库名**：`wealth`（字符集 `utf8mb4`）
2. **逻辑删除**：`del_flag` — 0=未删除，1=已删除
3. **主键**：统一使用 `BIGINT` 自增
4. **时间字段**：`DATETIME`，默认 `CURRENT_TIMESTAMP`
5. **禁止使用外键**，业务层维护关联
6. **索引**：必须按建表语句创建

### 无 update_time 列的表

| 表 | 说明 |
|----|------|
| `wea_market_data` | 行情数据表 |
| `wea_news` | 财经资讯表 |
| `wea_message` | 站内消息表 |
| `ums_admin` | 管理员表 |
| `ums_role` | 角色表 |
| `ums_resource` | 资源表 |
| `ums_admin_role_relation` | 管理员角色关联表 |
| `ums_role_resource_relation` | 角色资源关联表 |
| `wea_user_favorite` | 自选表（且无 del_flag） |

### 无 del_flag 列的表

| 表 | 说明 |
|----|------|
| `wea_user_favorite` | 物理删除 |

---

## 二、所有表结构

### 1. 系统用户表（sys_user）

```sql
CREATE TABLE sys_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    username    VARCHAR(50)  NOT NULL                COMMENT '账号',
    password    VARCHAR(100) NOT NULL                COMMENT '密码',
    nickname    VARCHAR(50)  DEFAULT NULL            COMMENT '昵称',
    phone       VARCHAR(20)  DEFAULT NULL            COMMENT '手机号',
    avatar      VARCHAR(255) DEFAULT NULL            COMMENT '头像',
    status      TINYINT      DEFAULT '1'             COMMENT '状态 0禁用 1正常',
    del_flag    TINYINT      DEFAULT '0'             COMMENT '逻辑删除 0否 1是',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';
```

**Entity**: `User` — 直接继承 BaseEntity。

### 2. 产品表（wea_product）

```sql
CREATE TABLE wea_product (
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    product_name   VARCHAR(100) NOT NULL                COMMENT '产品名称',
    product_code   VARCHAR(50)  NOT NULL                COMMENT '产品编码',
    product_type   TINYINT      DEFAULT NULL            COMMENT '类型 1黄金 2白银 3理财',
    price          DECIMAL(10,2) DEFAULT NULL           COMMENT '当前单价',
    rise_fall      DECIMAL(10,2) DEFAULT NULL           COMMENT '涨跌额',
    rise_fall_rate DECIMAL(5,2)  DEFAULT NULL           COMMENT '涨跌幅',
    status         TINYINT      DEFAULT '1'             COMMENT '上下架 0下架 1上架',
    sort           INT          DEFAULT '0'             COMMENT '排序',
    del_flag       TINYINT      DEFAULT '0'             COMMENT '逻辑删除 0未删除 1已删除',
    create_time    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY idx_product_code (product_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='理财/贵金属产品表';
```

**Entity**: `WeaProduct` — 直接继承 BaseEntity。

### 3. 行情数据表（wea_market_data）

```sql
CREATE TABLE wea_market_data (
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    product_code   VARCHAR(50)  NOT NULL                COMMENT '产品编码',
    current_price  DECIMAL(10,2) NOT NULL               COMMENT '实时价格',
    open_price     DECIMAL(10,2) DEFAULT NULL           COMMENT '开盘价',
    close_price    DECIMAL(10,2) DEFAULT NULL           COMMENT '昨收价',
    highest_price  DECIMAL(10,2) DEFAULT NULL           COMMENT '最高价',
    lowest_price   DECIMAL(10,2) DEFAULT NULL           COMMENT '最低价',
    rise_fall      DECIMAL(10,2) DEFAULT NULL           COMMENT '涨跌',
    rise_fall_rate DECIMAL(5,2)  DEFAULT NULL           COMMENT '涨跌幅',
    market_time    DATETIME     NOT NULL                COMMENT '行情时间',
    del_flag       TINYINT      DEFAULT '0'             COMMENT '删除标识 0未删除 1已删除',
    create_time    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_product_time (product_code, market_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='行情数据表';
```

> **注意**：本表无 `update_time` 列，Entity 中 `updateTime` 必须标注 `@TableField(exist = false)`。

**Entity**: `WeaMarketData` — 继承 BaseEntity，覆盖 `updateTime` 为 `exist = false`。

### 4. 用户自选表（wea_user_favorite）

```sql
CREATE TABLE wea_user_favorite (
    id           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id      BIGINT      NOT NULL                COMMENT '用户ID',
    product_code VARCHAR(50) NOT NULL                COMMENT '产品编码',
    create_time  DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    PRIMARY KEY (id),
    UNIQUE KEY idx_user_product (user_id, product_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户自选关注表';
```

> **注意**：物理删除，无 `del_flag` 和 `update_time` 列。Entity 中 `delFlag` 和 `updateTime` 必须标注 `@TableField(exist = false)`。

**Entity**: `WeaUserFavorite` — 继承 BaseEntity，覆盖 `delFlag` 和 `updateTime` 为 `exist = false`。

### 5. 交易委托单表（wea_trade_order）

```sql
CREATE TABLE wea_trade_order (
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    order_no       VARCHAR(64)  NOT NULL                COMMENT '委托单号',
    user_id        BIGINT       NOT NULL                COMMENT '用户ID',
    product_code   VARCHAR(50)  NOT NULL                COMMENT '产品编码',
    trade_type     TINYINT      NOT NULL                COMMENT '交易类型 1买入 2卖出',
    entrust_price  DECIMAL(10,2) NOT NULL               COMMENT '委托价格',
    entrust_num    INT          NOT NULL                COMMENT '委托数量',
    order_status   TINYINT      DEFAULT NULL            COMMENT '状态 1待委托 2已完成 3已撤销',
    del_flag       TINYINT      DEFAULT '0'             COMMENT '逻辑删除 0未删除 1已删除',
    create_time    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY idx_order_no (order_no),
    KEY idx_user_id (user_id),
    KEY idx_status_type (order_status, trade_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模拟委托交易单';
```

**Entity**: `WeaTradeOrder` — 直接继承 BaseEntity。

### 6. 财经资讯表（wea_news）

```sql
CREATE TABLE wea_news (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    title        VARCHAR(200) NOT NULL                COMMENT '资讯标题',
    content      TEXT         DEFAULT NULL            COMMENT '资讯内容',
    news_type    TINYINT      DEFAULT NULL            COMMENT '1行情快讯 2行业公告 3理财资讯',
    source       VARCHAR(100) DEFAULT NULL            COMMENT '来源',
    status       TINYINT      DEFAULT '1'             COMMENT '0草稿 1已发布',
    publish_time DATETIME     DEFAULT NULL            COMMENT '发布时间',
    del_flag     TINYINT      DEFAULT '0'             COMMENT '逻辑删除 0未删除 1已删除',
    create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_news_type (news_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='财经资讯公告表';
```

> **注意**：本表无 `update_time` 列，Entity 中 `updateTime` 必须标注 `@TableField(exist = false)`。

**Entity**: `WeaNews` — 继承 BaseEntity，覆盖 `updateTime` 为 `exist = false`。

### 7. 站内消息表（wea_message）

```sql
CREATE TABLE wea_message (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id     BIGINT       DEFAULT NULL            COMMENT '用户ID 0全局消息',
    msg_type    TINYINT      DEFAULT NULL            COMMENT '1行情提醒 2资讯推送 3委托通知',
    msg_title   VARCHAR(100) DEFAULT NULL            COMMENT '消息标题',
    msg_content VARCHAR(255) DEFAULT NULL            COMMENT '消息内容',
    read_flag   TINYINT      DEFAULT '0'             COMMENT '0未读 1已读',
    del_flag    TINYINT      DEFAULT '0'             COMMENT '逻辑删除 0未删除 1已删除',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user_read (user_id, read_flag, msg_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站内消息推送表';
```

> **注意**：本表无 `update_time` 列，Entity 中 `updateTime` 必须标注 `@TableField(exist = false)`。

**Entity**: `WeaMessage` — 继承 BaseEntity，覆盖 `updateTime` 为 `exist = false`。

### 8. 管理员表（ums_admin）

```sql
CREATE TABLE ums_admin (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    username    VARCHAR(64)  NOT NULL                COMMENT '用户名',
    password    VARCHAR(64)  NOT NULL                COMMENT '密码',
    email       VARCHAR(100) DEFAULT NULL            COMMENT '邮箱',
    nick_name   VARCHAR(200) DEFAULT NULL            COMMENT '昵称',
    status      TINYINT      DEFAULT NULL            COMMENT '账号启用状态 0禁用 1启用',
    create_time DATETIME     DEFAULT NULL            COMMENT '创建时间',
    login_time  DATETIME     DEFAULT NULL            COMMENT '最后登录时间',
    avatar      VARCHAR(500) DEFAULT NULL            COMMENT '头像',
    del_flag    INT          DEFAULT '0'             COMMENT '逻辑删除 0未删除 1已删除',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='后台用户表';
```

> **注意**：本表无 `update_time` 列，Entity 中 `updateTime` 必须标注 `@TableField(exist = false)`。`nick_name` 列含下划线，Java 字段 `nickName` 配合 `map-underscore-to-camel-case` 自动映射。

**Entity**: `UmsAdmin` — 继承 BaseEntity，覆盖 `updateTime` 为 `exist = false`。

### 9. 角色表（ums_role）

```sql
CREATE TABLE ums_role (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name        VARCHAR(100) NOT NULL                COMMENT '名称',
    description VARCHAR(500) DEFAULT NULL            COMMENT '描述',
    status      TINYINT      DEFAULT '1'             COMMENT '启用状态 0禁用 1启用',
    sort        INT          DEFAULT '0'             COMMENT '排序',
    create_time DATETIME     DEFAULT NULL            COMMENT '创建时间',
    del_flag    INT          DEFAULT '0'             COMMENT '逻辑删除 0未删除',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='后台角色表';
```

> **注意**：本表无 `update_time` 列，Entity 中 `updateTime` 必须标注 `@TableField(exist = false)`。

**Entity**: `UmsRole` — 继承 BaseEntity，覆盖 `updateTime` 为 `exist = false`。

### 10. 资源表（ums_resource）

```sql
CREATE TABLE ums_resource (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    create_time DATETIME     DEFAULT NULL            COMMENT '创建时间',
    name        VARCHAR(200) NOT NULL                COMMENT '资源名称',
    url         VARCHAR(255) DEFAULT NULL            COMMENT '资源URL',
    description VARCHAR(500) DEFAULT NULL            COMMENT '描述',
    category_id BIGINT       DEFAULT NULL            COMMENT '资源分类ID',
    del_flag    INT          DEFAULT '0'             COMMENT '逻辑删除 0未删除 1已删除',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='后台资源表';
```

> **注意**：本表无 `update_time` 列，Entity 中 `updateTime` 必须标注 `@TableField(exist = false)`。

**Entity**: `UmsResource` — 继承 BaseEntity，覆盖 `updateTime` 为 `exist = false`。

### 11. 管理员角色关联表（ums_admin_role_relation）

```sql
CREATE TABLE ums_admin_role_relation (
    id       BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    admin_id BIGINT DEFAULT NULL             COMMENT '管理员ID',
    role_id  BIGINT DEFAULT NULL             COMMENT '角色ID',
    del_flag INT    DEFAULT '0'              COMMENT '逻辑删除 0未删除 1已删除',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='后台用户和角色关系表';
```

> **注意**：本表无 `create_time` 和 `update_time` 列，Entity 中 `createTime` 和 `updateTime` 必须标注 `@TableField(exist = false)`。

**Entity**: `UmsAdminRoleRelation` — 继承 BaseEntity，覆盖 `createTime` 和 `updateTime` 为 `exist = false`。

### 12. 角色资源关联表（ums_role_resource_relation）

```sql
CREATE TABLE ums_role_resource_relation (
    id          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    role_id     BIGINT DEFAULT NULL             COMMENT '角色ID',
    resource_id BIGINT DEFAULT NULL             COMMENT '资源ID',
    del_flag    INT    DEFAULT '0'              COMMENT '逻辑删除 0未删除 1已删除',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='后台角色资源关系表';
```

> **注意**：本表无 `create_time` 和 `update_time` 列，Entity 中 `createTime` 和 `updateTime` 必须标注 `@TableField(exist = false)`。

**Entity**: `UmsRoleResourceRelation` — 继承 BaseEntity，覆盖 `createTime` 和 `updateTime` 为 `exist = false`。

---

## 三、BaseEntity 继承规范

### BaseEntity 定义

位于 `wealth-common/entity/BaseEntity.java`：

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

### 各实体覆盖情况

| Entity | 表 | 覆盖字段 | 原因 |
|--------|-----|----------|------|
| `WeaUserFavorite` | wea_user_favorite | `delFlag`（exist=false）、`updateTime`（exist=false） | 无 del_flag 和 update_time，物理删除 |
| `WeaMarketData` | wea_market_data | `updateTime`（exist=false） | 无 update_time 列 |
| `WeaNews` | wea_news | `updateTime`（exist=false） | 无 update_time 列 |
| `WeaMessage` | wea_message | `updateTime`（exist=false） | 无 update_time 列 |
| `UmsAdmin` | ums_admin | `updateTime`（exist=false） | 无 update_time 列 |
| `UmsRole` | ums_role | `updateTime`（exist=false） | 无 update_time 列 |
| `UmsResource` | ums_resource | `updateTime`（exist=false） | 无 update_time 列 |
| `UmsAdminRoleRelation` | ums_admin_role_relation | `createTime`（exist=false）、`updateTime`（exist=false） | 无 create_time 和 update_time 列 |
| `UmsRoleResourceRelation` | ums_role_resource_relation | `createTime`（exist=false）、`updateTime`（exist=false） | 无 create_time 和 update_time 列 |
| `User`、`WeaProduct`、`WeaTradeOrder` | 各自表 | 无覆盖 | 表含完整基础字段 |

> **注意**：子类重写父类字段时，必须使用 `@EqualsAndHashCode(callSuper = true)`（或在类上使用 `@Getter @Setter @EqualsAndHashCode(callSuper = true)` 替代 `@Data`），确保 Lombok 正确处理父类字段。

### 实体类规范要点

1. 所有 Entity 必须继承 `BaseEntity`
2. `@TableName("表名")` — 必须明确指定表名
3. `@TableLogic` 已在 BaseEntity 上定义，子类无需重复声明
4. 若表无对应列，子类中重写该字段并标注 `@TableField(exist = false)`
5. 字段映射统一使用 `@TableField("列名")`
6. 自动填充：`create_time` 使用 `fill = FieldFill.INSERT`，`update_time` 使用 `fill = FieldFill.INSERT_UPDATE`
