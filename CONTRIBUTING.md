# Contributing to finance-mid-platform

感谢你参与 finance-mid-platform 项目的贡献。本文档为项目贡献者提供统一的开发规范与协作流程。

## 项目简介

finance-mid-platform 是一个基于 Spring Cloud Alibaba 微服务架构的金融中台项目，提供产品管理、行情数据、交易委托、用户自选、财经资讯等核心金融服务能力。

### 技术栈

- **后端**: SpringBoot 3.3.5 + SpringCloud 2023.0.3 + Spring Cloud Alibaba 2023.0.1.2 + MyBatis-Plus 3.5.7
- **前端**: Vue 3.5.13 + Vite 6.3.1 + Element Plus 2.9.7 + TypeScript 5.7
- **中间件**: MySQL 8 + Redis 5 + RabbitMQ 3.10 + ElasticSearch 8.8.2 + Nacos 2.3.2
- **网关**: Spring Cloud Gateway（端口 8080）

完整模块架构参见 [docs/architecture.md](docs/architecture.md)。

## 开发环境准备

| 工具 | 版本要求 | 说明 |
|------|---------|------|
| JDK | 21.0.3 | 必须使用 JDK 21 |
| Maven | 3.9.9 | 项目构建工具 |
| Node.js | 18+（推荐 20 LTS） | 前端开发环境 |
| Docker | latest | 运行中间件容器 |
| MySQL | 8.0.37 | 本地安装，数据库 `finance`（utf8mb4） |
| Git | latest | 版本管理 |

### Docker 中间件

以下服务通过 Docker 运行，请确保本地 Docker 环境正常：

| 服务 | 镜像 | 端口 |
|------|------|------|
| Nacos | nacos/nacos-server:v2.3.2 | 8848 |
| Redis | redis:latest | 6379 |
| RabbitMQ | rabbitmq:3.10-management | 5672, 15672 |
| ElasticSearch | elasticsearch:8.8.2 | 9200 |
| Nginx | nginx:latest | 80 |

> 完整容器列表详见 [docs/architecture.md](docs/architecture.md#基础设施-docker-容器)。

## 本地开发流程

### 1. 拉取代码

```bash
git clone https://github.com/renjianfeng8/finance-mid-platform.git
cd finance-mid-platform
```

### 2. 启动基础设施

```bash
docker start nacos redis rabbitmq es nginx
```

### 3. 初始化数据库

在 MySQL 中创建 `finance` 库（字符集 `utf8mb4`），然后执行建表脚本：

```bash
mysql -u root -p finance < finance-common/src/main/resources/sql/init.sql
```

### 4. 配置 Nacos

访问 Nacos 控制台 `http://localhost:8848`，在 DEFAULT_GROUP 下创建共享配置 `finance-shared.yaml`，内容包含 JWT 密钥和数据源配置。详细配置内容参见 [docs/architecture.md](docs/architecture.md#nacos-配置中心docker-nacosnacos-serverv232)。

### 5. 编译项目

首次编译（或修改了 finance-common 后）需要先安装公共模块：

```bash
# 编译公共模块
mvn clean install -pl finance-common -DskipTests

# 全量编译
mvn clean install -DskipTests
```

### 6. 按顺序启动微服务

服务间存在依赖关系，请严格按照以下顺序启动：

```
gateway(8080) → system(8082) → user(8083) → product(8084)
→ account(8086) → trade(8085) → message(8087) → search(8089)
```

```bash
# 启动单个模块
mvn spring-boot:run -pl finance-{模块名}

# 示例：启动网关
mvn spring-boot:run -pl finance-gateway
```

### 7. 启动前端

```bash
cd front-user
npm install
npx vite
```

前端默认运行在 `http://localhost:3000`，通过网关 `http://localhost:8080` 调用后端接口。

## 代码规范

### Java 后端

- 包结构：`com.finance.platform.{模块名}`，按 controller/service/mapper/entity/vo/dto 分层
- 所有 Entity 必须继承 `BaseEntity`，自动填充 `create_time`/`update_time`
- 接口统一返回 `Result<T>` 格式（code + message + data）
- 使用 `BeanConvertUtil` 进行 Entity → VO 转换
- 更新操作使用 `copyNonNullProperties` 避免 null 覆盖
- 业务异常使用 `ServiceException(code, message)` 而非 RuntimeException
- 写操作必须加 `@Transactional(rollbackFor = Exception.class)`
- 所有 `@RequestBody` DTO 必须加 `@Valid`
- 分页查询使用 MyBatis-Plus `Page` + `PaginationInnerInterceptor`

详细规范参见 [CLAUDE.md](CLAUDE.md)。

### 前端

- Vue 3 组合式 API + TypeScript
- 组件库使用 Element Plus
- 状态管理使用 Pinia
- 路由使用 Vue Router 4

### 数据库

- 所有表必须包含 `id`、`create_time`、`update_time`、`del_flag`
- 逻辑删除：`del_flag` 0=未删除 1=已删除
- 主键统一使用 BIGINT 自增
- 禁止使用外键

表结构细节参见 [docs/database-schema.md](docs/database-schema.md)。

## 提交规范

所有 git 提交必须遵循 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

```
<type>(<scope>): <description>
```

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
| chore | 构建/工具变动 |
| ci | CI 配置变更 |
| build | 依赖/构建系统变更 |

- **scope**（可选）: common / gateway / system / user / product / account / trade / message / search
- **description**: 命令式语气，首字母小写，末尾不加句号

示例：

```
feat(product): 添加产品分页查询接口
fix(trade): 修复交易委托金额计算精度问题
docs: 更新 API 文档
refactor(gateway): 提取 CORS 配置为独立类
```

## 分支管理策略

| 分支 | 用途 | 说明 |
|------|------|------|
| `main` | 稳定版本 | 保护分支，禁止直接推送 |
| `feature/*` | 新功能开发 | 从 `main` 切出，完成后通过 PR 合入 `main` |
| `fix/*` | Bug 修复 | 从 `main` 切出，修复后通过 PR 合入 `main` |

### 分支命名规范

- `feature/description` — 如 `feature/product-search`
- `fix/description` — 如 `fix/trade-amount-precision`

保持每个分支聚焦于单一改动，一个分支只解决一个问题。

## PR 提交与审核流程

### 提交前检查

- [ ] 代码编译通过：`mvn clean compile`
- [ ] 遵循代码规范和提交规范
- [ ] 自测通过，关键路径已验证
- [ ] 无多余调试代码、注释代码
- [ ] 分支已 rebase 到最新的 `main`

### PR 要求

1. **关联 Issue** — 在 PR 描述中关联对应 Issue（`Closes #123`）
2. **描述变更** — 简要说明变更内容、原因及影响范围
3. **通过 CI** — 确保编译和测试通过
4. **代码审查** — 至少 1 人 Review 通过后方可合并
5. **保持简洁** — 一个 PR 只解决一个问题，避免无关改动

### PR 描述模板

```markdown
## 变更内容
[简要描述改了什么，为什么改]

## 关联 Issue
Closes #123

## 测试说明
[如何验证改动的正确性]

## 涉及模块
[gateway / system / user / product / ...]
```

## 问题反馈

发现 Bug 或有改进建议时：

1. **优先查阅** [Bug.md](Bug.md) — 确认是否为已知问题，查看已有的修复方案和排查要点
2. **提交 Issue** — 若为新问题，请附上：
   - 复现步骤
   - 期望行为与实际行为
   - 环境信息（模块、版本等）
   - 相关日志或截图
