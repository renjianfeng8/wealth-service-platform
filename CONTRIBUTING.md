
# 贡献指南

感谢你参与 wealth-service-platform 项目的贡献。本文档为项目贡献者提供统一的开发规范与协作流程。

---

## 目录

- [项目简介](#项目简介)
- [技术栈](#技术栈)
- [开发环境准备](#开发环境准备)
- [本地开发流程](#本地开发流程)
- [代码规范](#代码规范)
- [提交规范](#提交规范)
- [分支管理策略](#分支管理策略)
- [PR 提交与审核](#pr-提交与审核)
- [问题反馈](#问题反馈)

---

## 项目简介

wealth-service-platform 是一个基于 Spring Boot 3.x + Vue 3 的金融级单体聚合架构项目，提供产品管理、行情数据、交易委托、用户自选、财经资讯等核心金融服务能力。

项目采用 Gateway + 业务服务两层后端架构，前端为单一 Vue 3 SPA（History 模式路由），覆盖用户端和管理端全部页面。

### 模块架构

```
wealth-service-platform
├── wealth-common     # 公共依赖：DTO、工具类、Contract 接口、全局配置
├── wealth-gateway    # 网关层：统一入口、路由转发、CORS（端口 8080）
├── wealth-service    # 业务服务：6 个业务域聚合（端口 8081）
└── front             # 前端 SPA（Vue 3 + Element Plus + TypeScript）
```

---

## 技术栈

| 层面 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot / Spring Cloud / Alibaba | 3.3.13 / 2023.0.6 / 2023.0.3.4 |
| ORM | MyBatis-Plus | 3.5.9 |
| 数据库 | MySQL | 8.0 |
| 缓存 | Redis | 5+ |
| 搜索引擎 | Elasticsearch | 8.8.2（可选）|
| 前端 | Vue 3 / Vite / Element Plus / Pinia / TypeScript | 3.5.13 / 6.3.1 / 2.9.7 / 2.3.1 / 5.7 |
| 认证 | JWT (jjwt) | 0.12.6 |
| API 文档 | Knife4j | 4.5.0 |
| 限流熔断 | Sentinel | 1.8.8 |
| 链路追踪 | Micrometer Tracing + Zipkin | 1.3.6 |
| 监控 | Prometheus + Grafana | — |

---

## 开发环境准备

| 工具 | 版本要求 | 说明 |
|------|---------|------|
| JDK | 21+ | 必须使用 JDK 21 |
| Maven | 3.9+ | 项目构建工具 |
| Node.js | 18+（推荐 20 LTS）| 前端构建 |
| Docker | 24.0+ | 运行中间件容器 |
| MySQL | 8.0 | 数据库 |
| Git | latest | 版本管理 |

### Docker 中间件

| 服务 | 镜像 | 端口 |
|------|------|------|
| MySQL | mysql:8.0 | 3306 |
| Redis | redis:latest | 6379 |
| Nginx | nginx:latest | 80 |
| Elasticsearch | elasticsearch:8.8.2 | 9200（可选）|

---

## 本地开发流程

### 1. 拉取代码

```bash
git clone https://github.com/renjianfeng8/wealth-service-platform.git
cd wealth-service-platform
```

### 2. 启动基础设施

```bash
docker start mysql redis nginx
```

### 3. 初始化数据库

```bash
mysql -u root -p Wealth < wealth-common/src/main/resources/sql/init.sql
```

### 4. 编译项目

```bash
# 编译公共模块（修改 common 后必须重新执行）
mvn clean install -pl wealth-common -DskipTests

# 全量编译
mvn clean compile
```

### 5. 启动后端

按顺序启动：

```bash
# 启动网关（端口 8080）
mvn spring-boot:run -pl wealth-gateway

# 启动业务服务（端口 8081）
mvn spring-boot:run -pl wealth-service
```

### 6. 启动前端

```bash
cd front
npm install
npx vite
```

前端运行在 `http://localhost:3000`，通过网关 `http://localhost:8080` 调用后端接口。

---

## 代码规范

完整开发规范（包结构、Entity 继承、接口格式、命名规范、异常处理、数据库规范等）详见 [CLAUDE.md](CLAUDE.md)。

数据库表结构详见 [docs/DATABASE-SCHEMA.md](docs/DATABASE-SCHEMA.md)。

---

## 提交规范

Git 提交须遵循 [Conventional Commits](https://www.conventionalcommits.org/) 规范，格式为 `<type>(<scope>): <description>`。

### type 类型

| type | 说明 |
|------|------|
| feat | 新功能 |
| fix | 修复 bug |
| docs | 文档变更 |
| style | 代码格式调整 |
| refactor | 代码重构 |
| perf | 性能优化 |
| test | 添加或修改测试 |
| chore | 构建过程或辅助工具变动 |
| ci | CI 配置变更 |
| build | 构建系统或外部依赖变更 |

### scope

本项目 scope：`common`、`gateway`、`service`。

### 示例

```
feat(service): 添加产品分页查询接口
fix(service): 修复交易委托金额计算精度问题
docs: 更新 README 部署说明
refactor(gateway): 提取 CORS 配置为独立类
```

> scope 可省略，type 不可省略。提交信息统一使用中文描述。

---

## 分支管理策略

| 分支 | 用途 | 说明 |
|------|------|------|
| `main` | 稳定版本 | 保护分支，禁止直接推送 |
| `feature/*` | 新功能开发 | 从 `main` 切出，完成后通过 PR 合入 |
| `fix/*` | Bug 修复 | 从 `main` 切出，修复后通过 PR 合入 |

### 命名规范

- `feature/description` — 如 `feature/product-search`
- `fix/description` — 如 `fix/trade-amount-precision`

保持每个分支聚焦于单一改动。

---

## PR 提交与审核

### 提交前检查

- [ ] 编译通过：`mvn clean compile`
- [ ] 遵循代码规范和提交规范
- [ ] 自测通过，关键路径已验证
- [ ] 无多余调试代码或注释代码
- [ ] 分支已 rebase 到最新的 `main`

### PR 要求

1. 描述变更内容、原因及影响范围
2. 编译和测试通过
3. 至少 1 人 Review 通过后合并
4. 一个 PR 只解决一个问题，避免无关改动

### PR 描述模板

```markdown
## 变更内容
[简要描述改了什么，为什么改]

## 测试说明
[如何验证改动的正确性]

## 涉及模块
[gateway / service / common / front]
```

---

## 问题反馈

发现 Bug 或有改进建议时：

1. **优先查阅** [docs/BUG.md](docs/BUG.md) — 确认是否为已知问题
2. **提交 Issue** — 若为新问题，请附上复现步骤、期望行为与实际行为、相关日志或截图
