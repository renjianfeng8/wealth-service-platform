
# 数据库表结构与字段

> 写实体类时引用 — 表结构、字段、BaseEntity 继承规则。
> 完整建表 SQL 见 `wealth-common/src/main/resources/sql/init.sql`。

---

## 一、数据库规范

1. **数据库名**：`wealth`（字符集 `utf8mb4`）
2. **所有表必须包含**：`id`、`create_time`、`update_time`、`del_flag`
3. **逻辑删除**：`del_flag` — 0=未删除，1=已删除
4. **主键**：统一使用 `BIGINT` 自增
5. **时间字段**：`DATETIME`
6. **禁止使用外键**，业务层维护关联
7. **索引**：必须按建表语句创建

### 特殊例外

| 表 | 例外 |
|----|------|
| `wea_user_favorite` | 无 `del_flag` 和 `update_time` 列（物理删除）|
| `ums_admin` | 无 `update_time` 列 |

---

## 二、所有表结构

### 1. 用户模块

```sql
CREATE TABLE sys_user (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    username     VARCHAR(64)  NOT NULL COMMENT '用户名',
    password     VARCHAR(255) NOT NULL COMMENT '密码（BCrypt）',
    nickname     VARCHAR(64)           COMMENT '昵称',
    email        VARCHAR(128)          COMMENT '邮箱',
    phone        VARCHAR(20)           COMMENT '手机号',
    status       INT DEFAULT 1         COMMENT '状态：0-禁用，1-启用',
    create_time  DATETIME              COMMENT '创建时间',
    update_time  DATETIME              COMMENT '更新时间',
    del_flag     INT DEFAULT 0         COMMENT '逻辑删除'
) COMMENT '系统用户表';
```

### 2. 产品模块

```sql
CREATE TABLE wea_product (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_code     VARCHAR(32)  NOT NULL COMMENT '产品编码',
    product_name     VARCHAR(128) NOT NULL COMMENT '产品名称',
    product_type     INT                   COMMENT '产品类型：1-黄金，2-基金，3-理财',
    status           INT DEFAULT 1         COMMENT '状态：0-停售，1-在售',
    price            DECIMAL(18,2)         COMMENT '当前价格',
    description      TEXT                  COMMENT '产品描述',
    create_time      DATETIME              COMMENT '创建时间',
    update_time      DATETIME              COMMENT '更新时间',
    del_flag         INT DEFAULT 0         COMMENT '逻辑删除',
    UNIQUE KEY uk_product_code (product_code)
) COMMENT '产品表';
```

```sql
CREATE TABLE wea_market_data (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_code   VARCHAR(32)  NOT NULL COMMENT '产品编码',
    price          DECIMAL(18,2)         COMMENT '当前价格',
    high_price     DECIMAL(18,2)         COMMENT '最高价',
    low_price      DECIMAL(18,2)         COMMENT '最低价',
    open_price     DECIMAL(18,2)         COMMENT '开盘价',
    pre_close_price DECIMAL(18,2)        COMMENT '昨收价',
    volume         BIGINT                COMMENT '成交量',
    create_time    DATETIME              COMMENT '创建时间',
    update_time    DATETIME              COMMENT '更新时间',
    del_flag       INT DEFAULT 0         COMMENT '逻辑删除',
    KEY idx_product_code (product_code)
) COMMENT '行情数据表';
```

### 3. 自选模块

```sql
CREATE TABLE wea_user_favorite (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT       NOT NULL COMMENT '用户ID',
    product_code VARCHAR(32)  NOT NULL COMMENT '产品编码',
    create_time  DATETIME              COMMENT '创建时间',
    UNIQUE KEY uk_user_product (user_id, product_code)
) COMMENT '用户自选表（物理删除，无 del_flag）';
```

### 4. 交易模块

```sql
CREATE TABLE wea_trade_order (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id           BIGINT        NOT NULL COMMENT '用户ID',
    product_code      VARCHAR(32)   NOT NULL COMMENT '产品编码',
    product_name      VARCHAR(128)           COMMENT '产品名称',
    order_type        INT                    COMMENT '委托类型：1-买入，2-卖出',
    order_status      INT DEFAULT 1          COMMENT '状态：1-已提交，2-已成交，3-已撤销',
    order_price       DECIMAL(18,2)          COMMENT '委托价格',
    order_quantity    DECIMAL(18,4)          COMMENT '委托数量',
    order_amount      DECIMAL(18,2)          COMMENT '委托金额',
    deal_price        DECIMAL(18,2)          COMMENT '成交价格',
    deal_quantity     DECIMAL(18,4)          COMMENT '成交数量',
    deal_amount       DECIMAL(18,2)          COMMENT '成交金额',
    remark            VARCHAR(500)           COMMENT '备注',
    create_time       DATETIME               COMMENT '创建时间',
    update_time       DATETIME               COMMENT '更新时间',
    del_flag          INT DEFAULT 0          COMMENT '逻辑删除',
    KEY idx_user_id (user_id),
    KEY idx_product_code (product_code),
    KEY idx_order_status (order_status)
) COMMENT '交易委托单表';
```

### 5. 资讯与消息模块

```sql
CREATE TABLE wea_news (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    news_type       INT           COMMENT '资讯类型：1-财经，2-公告',
    title           VARCHAR(255)  NOT NULL COMMENT '标题',
    summary         TEXT                    COMMENT '摘要',
    content         LONGTEXT               COMMENT '正文',
    author          VARCHAR(64)            COMMENT '作者',
    create_time     DATETIME               COMMENT '创建时间',
    update_time     DATETIME               COMMENT '更新时间',
    del_flag        INT DEFAULT 0          COMMENT '逻辑删除'
) COMMENT '财经资讯表';
```

```sql
CREATE TABLE wea_message (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL COMMENT '接收用户ID',
    message_type    INT                    COMMENT '消息类型：1-系统，2-交易',
    title           VARCHAR(255)           COMMENT '标题',
    content         TEXT                   COMMENT '内容',
    is_read         INT DEFAULT 0          COMMENT '已读：0-未读，1-已读',
    create_time     DATETIME               COMMENT '创建时间',
    update_time     DATETIME               COMMENT '更新时间',
    del_flag        INT DEFAULT 0          COMMENT '逻辑删除',
    KEY idx_user_id (user_id)
) COMMENT '站内消息表';
```

### 6. 后台权限模块

```sql
CREATE TABLE ums_admin (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(64)  NOT NULL COMMENT '用户名',
    password      VARCHAR(255) NOT NULL COMMENT '密码（BCrypt）',
    nickname      VARCHAR(64)           COMMENT '昵称',
    email         VARCHAR(128)          COMMENT '邮箱',
    status        INT DEFAULT 1         COMMENT '状态：0-禁用，1-启用',
    create_time   DATETIME              COMMENT '创建时间',
    del_flag      INT DEFAULT 0         COMMENT '逻辑删除',
    UNIQUE KEY uk_username (username)
) COMMENT '管理员表';

CREATE TABLE ums_role (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(64)  NOT NULL COMMENT '角色名称',
    description   VARCHAR(255)          COMMENT '角色描述',
    status        INT DEFAULT 1         COMMENT '状态：0-禁用，1-启用',
    create_time   DATETIME              COMMENT '创建时间',
    update_time   DATETIME              COMMENT '更新时间',
    del_flag      INT DEFAULT 0         COMMENT '逻辑删除'
) COMMENT '角色表';

CREATE TABLE ums_resource (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(64)  NOT NULL COMMENT '资源名称',
    url           VARCHAR(255) NOT NULL COMMENT '资源URL',
    description   VARCHAR(255)          COMMENT '资源描述',
    category      VARCHAR(64)           COMMENT '资源分类',
    create_time   DATETIME              COMMENT '创建时间',
    update_time   DATETIME              COMMENT '更新时间',
    del_flag      INT DEFAULT 0         COMMENT '逻辑删除'
) COMMENT '资源表';

CREATE TABLE ums_admin_role_relation (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id      BIGINT NOT NULL COMMENT '管理员ID',
    role_id       BIGINT NOT NULL COMMENT '角色ID',
    create_time   DATETIME         COMMENT '创建时间',
    del_flag      INT DEFAULT 0    COMMENT '逻辑删除'
) COMMENT '管理员角色关系表';

CREATE TABLE ums_role_resource_relation (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id       BIGINT NOT NULL COMMENT '角色ID',
    resource_id   BIGINT NOT NULL COMMENT '资源ID',
    create_time   DATETIME         COMMENT '创建时间',
    del_flag      INT DEFAULT 0    COMMENT '逻辑删除'
) COMMENT '角色资源关系表';
```

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

### 继承规则

| 字段 | BaseEntity 定义 | 无对应列的子类处理 |
|------|----------------|-------------------|
| id | `@TableId(type = IdType.AUTO)` | 自动继承，无需处理 |
| create_time | `@TableField(fill = FieldFill.INSERT)` | `@TableField(exist = false)` |
| update_time | `@TableField(fill = FieldFill.INSERT_UPDATE)` | `@TableField(exist = false)` |
| del_flag | `@TableLogic @TableField("del_flag")` | `@TableField(exist = false)` |

### 当前项目中的覆盖

| 实体 | 覆盖字段 | 原因 |
|------|----------|------|
| `WeaUserFavorite` | `delFlag` + `updateTime`（均 `exist=false`）| 表无 del_flag 和 update_time 列 |
| `UmsAdmin` | `updateTime`（`exist=false`）| 表无 update_time 列 |

> **注意**：子类重写父类字段时，必须使用 `@EqualsAndHashCode(callSuper = true)`（或在类上使用 `@Getter @Setter @EqualsAndHashCode(callSuper = true)` 替代 `@Data`），确保 Lombok 正确处理父类字段。

### 实体类规范要点

1. 所有 Entity 必须继承 `BaseEntity`
2. `@TableName("表名")` — 必须明确指定表名
3. `@TableLogic` 已在 BaseEntity 上定义，子类无需重复声明
4. 若表无对应列，子类中重写该字段并标注 `@TableField(exist = false)`
5. 字段映射统一使用 `@TableField("列名")`
6. 自动填充：`create_time` 使用 `fill = FieldFill.INSERT`，`update_time` 使用 `fill = FieldFill.INSERT_UPDATE`
