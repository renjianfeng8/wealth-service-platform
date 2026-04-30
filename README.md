基于 Spring Boot 3.x + Spring Cloud 2023.x 构建的金融业务中台，提供用户、账户、产品、交易等核心金融服务。
本项目是一个金融业务微服务中台，采用前后端分离架构，基于 Spring Cloud 微服务生态开发，旨在为金融场景提供高可用、可扩展的服务化解决方案。
主要模块包括：
用户服务：用户管理、登录认证、权限控制
账户服务：用户自选、资金账户管理
产品服务：金融产品信息维护与查询
交易服务：订单、持仓、交易记录管理
消息服务：通知、消息推送
搜索服务：产品与资讯搜索
系统服务：系统配置、字典管理
网关服务：请求路由、鉴权、限流
技术 / 框架	版本	用途
Spring Boot	3.2.5	微服务基础框架
Spring Cloud	2023.0.1	微服务治理
Spring Cloud Alibaba	2023.0.1.1	Nacos、Sentinel 等
Nacos	2.x	服务注册与配置中心
OpenFeign	4.x	微服务远程调用
Knife4j / Swagger	4.x	API 文档
MyBatis-Plus	3.5.x	ORM 框架
MySQL	8.x	主数据库
Redis	6.x	缓存
Docker	29.x	容器化部署
Maven	3.9.x	项目构建
JDK	21	运行环境
finance-mid-platform
├── finance-common          # 公共依赖模块（DTO、工具类、Feign 接口）
├── finance-gateway         # 网关服务
├── finance-system          # 系统服务（配置、字典）
├── finance-user            # 用户服务
├── finance-account         # 账户服务
├── finance-product         # 产品服务
├── finance-trade           # 交易服务
├── finance-message         # 消息服务
└── finance-search          # 搜索服务

# 克隆项目
git clone https://github.com/renjianfeng8/finance-mid-platform.git

# 进入项目目录
cd finance-mid-platform

# 安装公共模块（关键步骤）
mvn clean install -pl finance-common

# 编译所有模块
mvn clean compile

📡 API 文档
服务启动后，访问地址：
Swagger 文档：http://localhost:8080/swagger-ui.html
Knife4j 文档：http://localhost:8080/doc.html
