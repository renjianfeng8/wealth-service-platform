# 更新日志

> 本日志仅记录项目级别的更新、功能新增、架构调整与优化。
> 具体的 Bug 修复细节请参阅 [Bug.md](./Bug.md)。

---

## v1.5.0 (2026-05-17)

### 可观测性体系全面落地

完成日志（logback）+ 链路追踪（Zipkin）+ 监控指标（Prometheus+Grafana）三大可观测性支柱：

#### 新增：链路追踪（Micrometer Tracing + Brave + Zipkin）

- 集成 `micrometer-tracing-bridge-brave` + `zipkin-sender-okhttp3`，所有 8 个微服务自动拦截跨服务调用
- Gateway 及通过 wealth-common 引入的业务模块均携带追踪依赖
- Zipkin 容器 `openzipkin/zipkin:latest`（端口 9411）接收全链路 Span 数据
- **配置注意**：Spring Boot 3.x 须使用 `management.zipkin.tracing.endpoint` 而非旧版 `zipkin.base-url`

#### 新增：监控告警（Prometheus + Grafana）

- 添加 `micrometer-registry-prometheus` 依赖，所有服务暴露 `/actuator/prometheus` 端点
- Nacos `wealth-shared.yaml` 配置 `management.endpoints.web.exposure.include=health,info,prometheus`
- `prometheus.yml` 配置 8 个服务的抓取规则（含各模块 context-path）
- docker-compose 新增 Prometheus（端口 9090）和 Grafana（端口 3001，admin/admin）

#### 审计修复（Nacos 配置属性更正）

- 更正 `wealth-shared.yaml` 中无效的 `zipkin.base-url` 为 `management.zipkin.tracing.endpoint`

#### 文档更新

- **Startup.md**：中间件新增 Zipkin/Prometheus/Grafana/Sentinel/Seata/Nginx，Nacos 配置示例同步，端口表扩充，FAQ 补充链路追踪与监控排查项，依赖图增加可观测性层
- **docs/nacos-config-reference.md**：新增 Nacos 配置中心参考文档
- **docs/go-live-audit-report.md**：修复率更新为 24/30，监控告警标记已修复
- **CHANGELOG.md**：本次更新日志

---

## v1.4.1 (2026-05-12)

### 搜索服务增强

- ES 版本校准至 8.8.2（与 Docker 容器版本对齐）
- 集成 IK 中文分词器，产品名称支持中文分词检索
- 日期字段格式标准化（`date_hour_minute_second_millis`），确保 ES mapping 与 Java 类型一致
### 用户认证优化

- 登录接口返回结构增强，新增 `userId` 字段，前端可直接获取用户标识

### 消息服务优化

- 更新接口支持部分字段传递，适配已读标记等单字段更新场景

---

## v1.4.0 (2026-05-10)

### 双端架构落地：用户前台 front-user + Playwright E2E

新增独立用户门户 `front-user/`，面向普通用户，与现有管理员后台 `front/` 构成完整的双端架构体系。同时配备 Playwright E2E 自动化测试套件，覆盖全部用户端业务场景。
#### 新增：用户前台 (front-user/)

全新的 Vue 3 + TypeScript 企业级金融用户端门户（端口 3001），包含 8 个核心功能模块：

- **登录/注册** — 基于 `sys_user` 表的普通用户 JWT 登录
- **仪表盘** — 用户资产概览与全局数据看板
- **产品中心** — 金融产品浏览与查询
- **实时行情** — 产品市场价格数据实时展示
- **我的自选** — 用户自选产品管理
- **交易委托** — 下单委托与订单历史查看
- **财经资讯** — 资讯列表与详情
- **消息中心** — 站内消息接收与管理
- **个人中心** — 用户信息查看与修改
技术特点：
- 企业级 UI 设计，移动端自适应布局
- Vite 代理配置：`/api` → 网关统一入口
- Pinia 状态管理，与后端 `sys_user` 接口对接
- Vue Router 路由守卫实现未登录自动拦截跳转
#### 新增：Playwright E2E 自动化测试
配置 37 条端到端测试用例，覆盖登录、仪表盘、产品中心、实时行情、我的自选、交易委托、财经资讯、消息中心、个人中心、退出登录、导航菜单共 11 个业务模块。
测试命令：
```bash
cd front-user
npx playwright install chromium
npm run test:e2e
npm run test:e2e:report
```

测试账号：`zhangwei` / `123456`

#### 文档重构

- **Startup.md** — 全面重构启动指南，新增 front-user 启动步骤、双端 E2E 测试说明、双测试账号对照表、全链路验证命令、端口对照表更新
- 双端架构说明：管理员后台（`front/`, 端口 3000） 用户前台（`front-user/`, 端口 3001）
### 测试数据补充

在 `init.sql` 中追加 12 张表的完整测试数据，共计 83 条记录，覆盖全业务场景：

| 表名 | 记录数 | 说明 |
|------|:-----:|------|
| `sys_user` | 5 | 前台测试用户（含 zhangwei）|
| `wea_product` | 12 | 不同类别金融产品 |
| `wea_market_data` | 15 | 行情历史数据 |
| `wea_user_favorite` | 5 | 用户自选关联 |
| `wea_trade_order` | 8 | 不同状态交易委托 |
| `wea_news` | 10 | 财经资讯文章 |
| `wea_message` | 8 | 站内消息 |
| `ums_*` | 22 | 后台权限相关 5 表 |

密码统一使用 BCrypt 加密，关键关联字段（`product_code`、`user_id`）保持跨表一致性。
### 项目规范更新

- **CLAUDE.md** — 全面同步项目最新状态：
  - 前端技术栈版本锁定
  - BaseEntity 继承规则表
  - 基础设施类说明表
  - 常见代码模式指南
  - 健康检查清单和高频问题
  - 端口表与网关路由说明

---

## v1.3.0 (2026-04-28)

### 双端初步整合

- 完成 `finance-mid-platform` 初始模块搭建
- 基础微服务架构：gateway → system → user → product → account → trade → message → search
- Spring Cloud Alibaba 体系集成（Nacos 注册中心 + 配置中心）
- 统一返回格式 `Result<T>` 与全局异常处理落地
- MyBatis-Plus 分页插件全局配置
- JWT 认证体系搭建
- 管理员后台 Crud 基础框架（Element Plus 表格/表单/弹窗）
- 8 个微服务模块全部可启动并注册到 Nacos

---

## v1.2.0 (2026-04-15)

### 基础设施搭建

- Spring Boot 3.3.5 + Spring Cloud 2023.0.3 项目初始化
- Nacos 2.3.2 Docker 化部署
- MySQL 8.0.37 Docker 化部署
- Redis / RabbitMQ / Elasticsearch Docker 化部署
- Nginx 反向代理配置
- Swagger + Knife4j API 文档集成
- Maven 多模块项目结构搭建

---

## v1.1.0 (2026-04-01)

### 架构设计

- 项目整体技术选型论证
- 数据库表结构设计（12 张核心表）
- API 接口规范定义
- 双端架构规划（管理员后台 + 用户前台）

---

## v1.0.0 (2026-03-15)

### 项目初始化

- 项目立项与仓库初始化
- README / CLAUDE.md / CONTRIBUTING.md 等基础文档
- Docker 编排文件
- Git 工作流与提交规范制定
