# 金融中台微服务平台
> 基于SpringBoot 3.x + SpringCloud Alibaba的金融级微服务架构项目，支撑用户、账户、产品、交易等核心业务场景

## 📌 项目背景
解决传统单体架构在金融业务中的扩展性差、耦合度高问题，实现业务模块解耦、服务独立部署、弹性扩缩容，满足高并发、高可用的金融业务需求。

## 🛠️ 技术栈
- 后端：Java 21, SpringBoot 3.2.5, SpringCloud 2023.0.1, SpringCloud Alibaba 2023.0.1
- 注册/配置中心：Nacos 2.x
- 网关：Spring Cloud Gateway
- 持久层：MyBatis-Plus, MySQL 8.0
- 消息队列：RabbitMQ（待集成）
- 搜索引擎：Elasticsearch 8.x + IK分词器
- 认证授权：JWT
- 接口文档：Knife4j

## 📦 模块说明
| 模块 | 功能 | 技术亮点 |
| :--- | :--- | :--- |
| finance-common | 公共依赖、工具类、全局异常处理 | 统一返回结果封装、全局异常拦截 |
| finance-gateway | 网关服务 | 路由转发、鉴权校验、限流 |
| finance-user | 用户服务 | 用户注册/登录、权限管理 |
| finance-account | 账户服务 | 账户开立、余额管理 |
| finance-product | 产品服务 | 理财产品管理、上架/下架 |
| finance-trade | 交易服务 | 订单创建、交易流水、幂等性处理 |
| finance-search | 搜索服务 | 产品全文检索、高亮显示 |
| finance-message | 消息服务 | 短信通知、异步消息推送 |
| finance-system | 系统管理服务 | 角色、菜单、字典管理 |

## 🚀 快速启动
### 前置条件
- JDK 21
- Maven 3.9+
- Docker（部署Nacos、MySQL、Elasticsearch）

### 启动步骤
1.  启动Nacos、MySQL、Elasticsearch
2.  克隆项目：`git clone https://github.com/renjianfeng8/finance-mid-platform.git`
3.  修改各模块 `application.yml` 中的数据库、Nacos配置
4.  按顺序启动服务：`gateway` → `system` → `user` → `account` → `product` → `trade` → `search` → `message`
5.  访问接口文档：`http://localhost:8080/doc.html`

## ✨ 项目亮点
- 基于DDD思想划分模块，业务边界清晰
- 实现VO/DTO/PO分层，数据传输与数据库模型解耦
- 集成OpenFeign实现服务间调用，支持超时重试与熔断
- 基于JWT实现无状态认证，网关统一鉴权
- 集成Elasticsearch实现产品全文检索，支持分词与高亮
