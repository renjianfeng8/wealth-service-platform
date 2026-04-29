-- 创建新库
CREATE DATABASE IF NOT EXISTS finance DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
USE finance;

-- ==============================
-- 1. 用户模块表 (finance-user)
-- ==============================
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL COMMENT '账号',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) COMMENT '昵称',
    phone VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(255) COMMENT '头像',
    status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1正常',
    del_flag TINYINT DEFAULT 0 COMMENT '逻辑删除 0否 1是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '系统用户表';
CREATE UNIQUE INDEX idx_username ON sys_user(username);

-- ==============================
-- 2. 产品与行情模块表 (finance-product)
-- ==============================
CREATE TABLE fin_product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_name VARCHAR(100) NOT NULL COMMENT '产品名称',
    product_code VARCHAR(50) NOT NULL COMMENT '产品编码',
    product_type TINYINT COMMENT '类型 1黄金 2白银 3理财',
    price DECIMAL(10,2) COMMENT '当前单价',
    rise_fall DECIMAL(10,2) COMMENT '涨跌额',
    rise_fall_rate DECIMAL(5,2) COMMENT '涨跌幅',
    status TINYINT DEFAULT 1 COMMENT '上下架 0下架 1上架',
    sort INT DEFAULT 0 COMMENT '排序',
    del_flag TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '理财/贵金属产品表';
CREATE UNIQUE INDEX idx_product_code ON fin_product(product_code);

CREATE TABLE fin_market_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_code VARCHAR(50) NOT NULL COMMENT '产品编码',
    current_price DECIMAL(10,2) NOT NULL COMMENT '实时价格',
    open_price DECIMAL(10,2) COMMENT '开盘价',
    close_price DECIMAL(10,2) COMMENT '昨收价',
    highest_price DECIMAL(10,2) COMMENT '最高价',
    lowest_price DECIMAL(10,2) COMMENT '最低价',
    rise_fall DECIMAL(10,2) COMMENT '涨跌',
    rise_fall_rate DECIMAL(5,2) COMMENT '涨跌幅',
    market_time DATETIME NOT NULL COMMENT '行情时间',
    del_flag TINYINT DEFAULT 0 COMMENT '删除标识：0=未删除 1=已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '行情数据表';
CREATE INDEX idx_product_time ON fin_market_data(product_code,market_time);

-- ==============================
-- 3. 用户自选表 (finance-account)
-- ==============================
CREATE TABLE fin_user_favorite (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_code VARCHAR(50) NOT NULL COMMENT '产品编码',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY idx_user_product (user_id,product_code)
) COMMENT '用户自选关注表';

-- ==============================
-- 4. 交易模块表 (finance-trade)
-- ==============================
CREATE TABLE fin_trade_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(64) NOT NULL COMMENT '委托单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_code VARCHAR(50) NOT NULL COMMENT '产品编码',
    trade_type TINYINT NOT NULL COMMENT '交易类型 1买入 2卖出',
    entrust_price DECIMAL(10,2) NOT NULL COMMENT '委托价格',
    entrust_num INT NOT NULL COMMENT '委托数量',
    order_status TINYINT COMMENT '状态 1待委托 2已完成 3已撤销',
    del_flag TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '模拟委托交易单';
CREATE UNIQUE INDEX idx_order_no ON fin_trade_order(order_no);
CREATE INDEX idx_user_id ON fin_trade_order(user_id);

-- ==============================
-- 5. 资讯与消息模块表 (finance-message)
-- ==============================
CREATE TABLE fin_news (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL COMMENT '资讯标题',
    content TEXT COMMENT '资讯内容',
    news_type TINYINT COMMENT '1行情快讯 2行业公告 3理财资讯',
    source VARCHAR(100) COMMENT '来源',
    status TINYINT DEFAULT 1 COMMENT '0草稿 1已发布',
    publish_time DATETIME COMMENT '发布时间',
    del_flag TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '财经资讯公告表';
CREATE INDEX idx_news_type ON fin_news(news_type);

CREATE TABLE fin_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT COMMENT '用户ID 0全局消息',
    msg_type TINYINT COMMENT '1行情提醒 2资讯推送 3委托通知',
    msg_title VARCHAR(100) COMMENT '消息标题',
    msg_content VARCHAR(255) COMMENT '消息内容',
    read_flag TINYINT DEFAULT 0 COMMENT '0未读 1已读',
    del_flag TINYINT DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '站内消息推送表';
CREATE INDEX idx_user_read ON fin_message(user_id,read_flag);

-- ==============================
-- 6. 后台权限模块表 (finance-system)
-- ==============================
CREATE TABLE ums_admin (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    password VARCHAR(64) NOT NULL,
    email VARCHAR(100) DEFAULT NULL,
    nick_name VARCHAR(200) DEFAULT NULL,
    status TINYINT DEFAULT NULL COMMENT '账号启用状态；0->禁用；1->启用',
    create_time DATETIME DEFAULT NULL,
    login_time DATETIME DEFAULT NULL,
    avatar VARCHAR(500) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='后台用户表';

CREATE TABLE ums_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '名称',
    description VARCHAR(500) DEFAULT NULL COMMENT '描述',
    status TINYINT DEFAULT 1 COMMENT '启用状态；0->禁用；1->启用',
    sort INT DEFAULT 0,
    create_time DATETIME DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='后台角色表';

CREATE TABLE ums_resource (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    create_time DATETIME DEFAULT NULL,
    name VARCHAR(200) NOT NULL COMMENT '资源名称',
    url VARCHAR(255) DEFAULT NULL COMMENT '资源URL',
    description VARCHAR(500) DEFAULT NULL,
    category_id BIGINT DEFAULT NULL COMMENT '资源分类ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='后台资源表';

CREATE TABLE ums_admin_role_relation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id BIGINT DEFAULT NULL,
    role_id BIGINT DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='后台用户和角色关系表';

CREATE TABLE ums_role_resource_relation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT DEFAULT NULL,
    resource_id BIGINT DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='后台角色资源关系表';

-- 插入默认管理员（账号：admin 密码：admin）
INSERT INTO ums_admin (username, password, status)
VALUES ('admin', '$2a$10$FMv7H1A1AeJzHqG8tX/JpeJtN1H1aG0aG0aG0aG0aG0aG0aG0a', 1);

-- 插入测试数据
INSERT INTO fin_market_data (product_code, current_price, open_price, close_price, highest_price, lowest_price, rise_fall, rise_fall_rate, market_time)
VALUES 
('XAUUSD',658.25,656.10,655.89,659.50,655.80,2.36,0.36,NOW()),
('XAGUSD',7.56,7.48,7.48,7.58,7.47,0.08,1.07,NOW()),
('FIN001',1.00,1.00,1.00,1.00,1.00,0.00,0.00,NOW());

INSERT INTO fin_user_favorite (user_id, product_code, create_time)
VALUES 
(1001, 'XAUUSD', NOW()),
(1001, 'XAGUSD', NOW()),
(1002, 'FIN001', NOW()),
(1003, 'XAUUSD', NOW());

INSERT INTO fin_trade_order (order_no, user_id, product_code, trade_type, 
                            entrust_price, entrust_num, order_status, del_flag, create_time)
VALUES 
('ORDER202501010001', 1001, 'XAUUSD', 1, 658.25, 10, 2, 0, NOW()),
('ORDER202501010002', 1001, 'XAGUSD', 1, 7.56, 100, 1, 0, NOW()),
('ORDER202501010003', 1002, 'FIN001', 2, 1.00, 50, 2, 0, NOW()),
('ORDER202501010004', 1003, 'XAUUSD', 2, 655.89, 5, 3, 0, NOW());

INSERT INTO fin_news (title, content, news_type, source, status, publish_time, del_flag, create_time)
VALUES 
('美联储降息预期升温，黄金震荡上行',
 '近期美国通胀数据回落，市场预计美联储将开启降息周期，美元指数走弱，贵金属价格获得支撑。',
 1, '财经快讯', 1, NOW(), 0, NOW()),

('国内理财新规落地，稳健型产品更受青睐',
 '监管层发布理财业务新规，强调风险管控，预计低风险、中低风险理财产品将成为市场主流。',
 3, '行业公告', 1, NOW(), 0, NOW()),

('白银工业需求回升，价格中期看涨',
 '新能源、光伏产业带动白银工业需求回升，叠加避险买盘，银价有望震荡上行。',
 1, '市场分析', 1, NOW(), 0, NOW());

INSERT INTO fin_message (user_id, msg_type, msg_title, msg_content, read_flag, create_time)
VALUES 
(1001, 3, '委托订单已成交', '您的委托单 ORDER202501010001 已成交', 0, NOW()),
(1001, 1, '黄金价格波动提醒', 'XAUUSD 日内涨幅已超 0.5%', 0, NOW()),
(1002, 2, '新资讯发布', '理财新规发布，速来查看', 1, NOW()),
(0, 2, '系统公告', '尊敬的用户，系统将于凌晨维护', 0, NOW());