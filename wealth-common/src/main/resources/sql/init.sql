-- ============================================================
-- 金融中台系统 数据库初始化脚本
-- 数据库: wealth (utf8mb4)
-- 说明: 执行本脚本会删除现有表并重新创建
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS wealth DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE wealth;

-- ============================================================
-- 1. 用户模块 (wealth-user) — 系统用户表
-- ============================================================
DROP TABLE IF EXISTS sys_user;
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

-- ============================================================
-- 2. 产品&行情模块 (wealth-product) — 产品表
-- ============================================================
DROP TABLE IF EXISTS wea_product;
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

-- ============================================================
-- 3. 产品&行情模块 (wealth-product) — 行情数据表
-- ============================================================
DROP TABLE IF EXISTS wea_market_data;
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

-- ============================================================
-- 4. 自选模块 (wealth-account) — 用户自选表
-- ============================================================
DROP TABLE IF EXISTS wea_user_favorite;
CREATE TABLE wea_user_favorite (
    id           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id      BIGINT      NOT NULL                COMMENT '用户ID',
    product_code VARCHAR(50) NOT NULL                COMMENT '产品编码',
    create_time  DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    PRIMARY KEY (id),
    UNIQUE KEY idx_user_product (user_id, product_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户自选关注表';

-- ============================================================
-- 5. 交易模块 (wealth-trade) — 交易委托单
-- ============================================================
DROP TABLE IF EXISTS wea_trade_order;
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

-- ============================================================
-- 6. 资讯&消息模块 (wealth-message) — 财经资讯
-- ============================================================
DROP TABLE IF EXISTS wea_news;
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

-- ============================================================
-- 7. 资讯&消息模块 (wealth-message) — 站内消息
-- ============================================================
DROP TABLE IF EXISTS wea_message;
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

-- ============================================================
-- 8. 后台权限模块 (wealth-system) — 管理员表
-- ============================================================
DROP TABLE IF EXISTS ums_admin;
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

-- ============================================================
-- 9. 后台权限模块 (wealth-system) — 角色表
-- ============================================================
DROP TABLE IF EXISTS ums_role;
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

-- ============================================================
-- 10. 后台权限模块 (wealth-system) — 资源表
-- ============================================================
DROP TABLE IF EXISTS ums_resource;
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

-- ============================================================
-- 11. 后台权限模块 (wealth-system) — 管理员角色关联表
-- ============================================================
DROP TABLE IF EXISTS ums_admin_role_relation;
CREATE TABLE ums_admin_role_relation (
    id       BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    admin_id BIGINT DEFAULT NULL             COMMENT '管理员ID',
    role_id  BIGINT DEFAULT NULL             COMMENT '角色ID',
    del_flag INT    DEFAULT '0'              COMMENT '逻辑删除 0未删除 1已删除',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='后台用户和角色关系表';

-- ============================================================
-- 12. 后台权限模块 (wealth-system) — 角色资源关联表
-- ============================================================
DROP TABLE IF EXISTS ums_role_resource_relation;
CREATE TABLE ums_role_resource_relation (
    id          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    role_id     BIGINT DEFAULT NULL             COMMENT '角色ID',
    resource_id BIGINT DEFAULT NULL             COMMENT '资源ID',
    del_flag    INT    DEFAULT '0'              COMMENT '逻辑删除 0未删除 1已删除',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='后台角色资源关系表';

-- ============================================================
-- 13. Seata AT 模式 — undo_log 表（参与分布式事务的模块均需此表）
-- ============================================================
DROP TABLE IF EXISTS undo_log;
CREATE TABLE undo_log (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    branch_id     BIGINT       NOT NULL                COMMENT '分支事务ID',
    xid           VARCHAR(128) NOT NULL                COMMENT '全局事务ID',
    context       VARCHAR(128) NOT NULL                COMMENT '上下文',
    rollback_info LONGBLOB     NOT NULL                COMMENT '回滚日志',
    log_status    INT          NOT NULL                COMMENT '状态 0正常 1已回滚',
    log_created   DATETIME     NOT NULL                COMMENT '创建时间',
    log_modified  DATETIME     NOT NULL                COMMENT '修改时间',
    ext           VARCHAR(100) DEFAULT NULL            COMMENT '扩展',
    PRIMARY KEY (id),
    UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Seata AT 模式回滚日志表';

-- ============================================================
-- 14. 审计日志表（由 @AuditLog 注解自动记录）
-- ============================================================
DROP TABLE IF EXISTS audit_log;
CREATE TABLE audit_log (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id     BIGINT       DEFAULT NULL            COMMENT '用户ID',
    username    VARCHAR(100) DEFAULT NULL            COMMENT '用户名',
    module      VARCHAR(100) DEFAULT NULL            COMMENT '模块名',
    operation   VARCHAR(200) DEFAULT NULL            COMMENT '操作描述',
    method_name VARCHAR(200) DEFAULT NULL            COMMENT '请求方法',
    request_url VARCHAR(255) DEFAULT NULL            COMMENT '请求URL',
    http_method VARCHAR(10)  DEFAULT NULL            COMMENT 'HTTP方法',
    params      TEXT         DEFAULT NULL            COMMENT '请求参数(JSON)',
    result      TEXT         DEFAULT NULL            COMMENT '响应结果(JSON)',
    ip          VARCHAR(50)  DEFAULT NULL            COMMENT '客户端IP',
    duration    BIGINT       DEFAULT NULL            COMMENT '执行耗时(ms)',
    status      TINYINT      DEFAULT '1'             COMMENT '状态 1成功 0失败',
    error_msg   VARCHAR(1000) DEFAULT NULL           COMMENT '错误信息',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user (user_id),
    KEY idx_module (module),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作审计日志表';
