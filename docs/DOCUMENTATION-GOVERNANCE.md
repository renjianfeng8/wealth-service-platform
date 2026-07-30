# 文档治理规则

本文定义 `wealth-service-platform` 的文档分类、权威来源、命名规则、归档规则和更新检查清单。

## 权威来源

遇到文档冲突时，按以下顺序判断：

1. `CLAUDE.md`：项目开发规范、技术栈、编码约束、测试规范和协作规则。
2. `docs/CODE-STANDARDS.md`：编码规范手册（导入/注入/日志/实体/Controller 等详细约定）。
3. `docs/DATABASE-SCHEMA.md`：数据库表、字段、Entity 生成依据（实际以 `init.sql` 为准）。
4. `docs/DATABASE-SCHEMA.md`：数据库表、字段、Entity 生成依据（实际以 `init.sql` 为准）。
5. `docs/ARCHITECTURE.md`：模块架构、路由、配置体系、部署结构。
6. `docs/STARTUP.md`：本地启动和冒烟验证流程。
7. `docs/BUG.md`：已知问题和排查记录。

`docs/archive/**` 是历史阶段性交付物，提供设计、计划和验证记录；它们可以说明上下文，但不能覆盖上述权威来源。

## 文档分类

| 分类 | 位置 | 说明 |
| --- | --- | --- |
| 对外入口 | `README.md` | 项目简介、亮点、快速开始、核心链接 |
| 协作入口 | `CONTRIBUTING.md` | 贡献流程、提交规范、PR 要求 |
| 项目规范 | `CLAUDE.md`、`docs/CODE-STANDARDS.md` | 开发规范 + 编码规范手册 |
| 核心文档 | `docs/*.md` | 架构、启动、数据库、Bug、变更记录 |
| 历史归档 | `docs/archive/**` | 历史设计规格、实施计划、验证记录 |
| QA 产物 | `output/playwright/*.png` | 本地截图证据，默认不作为权威文档 |

## 命名规则

- 核心文档使用大写英文名：`ARCHITECTURE.md`、`STARTUP.md`。
- 历史归档使用：`YYYY-MM-DD-<topic>.md` 或按原目录结构存放于 `docs/archive/`。
- 文件名使用小写英文和短横线，避免空格。

## 归档规则

- 当前有效的核心规范保留在根目录或 `docs/` 一级。
- 过期但仍有参考价值的阶段文档保留在 `docs/archive/`，不纳入核心文档索引。
- 明确废弃、重复、错误或仅供临时调试的文档应删除，不长期堆放。
- Playwright 截图默认视为 QA 产物；除非 PR 或验收明确需要，否则不纳入正式文档索引。

## 更新检查清单

新增或修改文档前后检查：

- [ ] 是否存在更权威的文档需要同步。
- [ ] 是否需要加入 [docs/README.md](README.md)。
- [ ] 是否需要记录到 [CHANGELOG.md](CHANGELOG.md)。
- [ ] 是否有过期链接、重复入口或冲突描述。
- [ ] 是否说明了文档状态：权威来源、维护中、阶段性交付物、QA 产物。
- [ ] 是否避免把临时调试信息写入长期文档。

## 治理历史

### 2026-06-09：首次文档治理
- 新增 `docs/README.md` 文档总索引。
- 新增 `docs/DOCUMENTATION-GOVERNANCE.md` 治理规则。
- 在根 `README.md` 增加文档入口。
- 增加结构测试，防止索引和治理规则缺失。

### 2026-07-30：二次文档治理
- 删除 `wealth-common/HELP.md`（Spring Boot 自动生成模板，版本错误）。
- 归档 `AGENTS.md` → `docs/archive/AGENTS.md`（Codex 配置，当前工作流已不使用）。
- 归档 `docs/superpowers/` → `docs/archive/superpowers/`（历史设计文档）。
- 新增 `docs/CODE-STANDARDS.md`（代码规范手册）。
- 重写 `CONTRIBUTING.md`（从 5.8KB 精简至 ~2KB，去除重复技术栈和 PR 流程）。
- 重写 `docs/README.md`（修复路径引用，改为纯索引页）。
- 修复 `docs/ARCHITECTURE.md` 版本号矛盾（v1.7.3 → v1.8.0）。
- 修复 `docs/BUG.md` 格式损坏（孤儿段落 → Bug-021）。
- 同步 `docs/DATABASE-SCHEMA.md` 缺失的 `update_time` 列和唯一索引。
- 精简 `README.md` 技术栈表，消除与 `CLAUDE.md` 的三处重复。
- 更新 `docs/DOCUMENTATION-GOVERNANCE.md` 规则与前述变更对齐。

