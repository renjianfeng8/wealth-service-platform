# 文档治理规则

本文定义 `wealth-service-platform` 的文档分类、权威来源、命名规则、归档规则和更新检查清单。

## 权威来源

遇到文档冲突时，按以下顺序判断：

1. `CLAUDE.md`：项目开发规范、技术栈、编码约束、测试规范和协作规则。
2. `AGENTS.md`：Codex 在本仓库中的补充行为规则。
3. `docs/DATABASE-SCHEMA.md`：数据库表、字段、Entity 生成依据。
4. `docs/ARCHITECTURE.md`：模块架构、路由、配置体系、部署结构。
5. `docs/STARTUP.md`：本地启动和冒烟验证流程。
6. `docs/BUG.md`：已知问题和排查记录。

`docs/superpowers/**` 是阶段性交付物，提供设计、计划和验证记录；它们可以说明上下文，但不能覆盖上述权威来源。

## 文档分类

| 分类 | 位置 | 说明 |
| --- | --- | --- |
| 对外入口 | `README.md` | 项目简介、亮点、快速开始、核心链接 |
| 协作入口 | `CONTRIBUTING.md` | 贡献流程、提交规范、PR 要求 |
| 项目规范 | `CLAUDE.md`、`AGENTS.md` | 开发和 Agent 行为规范 |
| 核心文档 | `docs/*.md` | 架构、启动、数据库、Bug、变更记录 |
| 阶段交付 | `docs/superpowers/specs/*.md` | 设计规格、原型说明、接口建议 |
| 执行计划 | `docs/superpowers/plans/*.md` | 实施计划、落地汇总、验证记录 |
| QA 产物 | `output/playwright/*.png` | 本地截图证据，默认不作为权威文档 |

## 命名规则

- 核心文档使用大写英文名：`ARCHITECTURE.md`、`STARTUP.md`。
- Superpowers 规格使用：`YYYY-MM-DD-<topic>.md`。
- Superpowers 计划使用：`YYYY-MM-DD-<topic>.md`。
- 文件名使用小写英文和短横线，避免空格。
- 同一主题有设计和计划时：
  - 设计放 `docs/superpowers/specs/`
  - 计划或落地汇总放 `docs/superpowers/plans/`

## 归档规则

- 当前有效的核心规范保留在根目录或 `docs/` 一级。
- 过期但仍有参考价值的阶段文档保留在 `docs/superpowers/`，并在文档顶部说明状态。
- 明确废弃、重复、错误或仅供临时调试的文档应删除，不长期堆放。
- Playwright 截图默认视为 QA 产物；除非 PR 或验收明确需要，否则不纳入正式文档索引。
- `.superpowers/brainstorm/**` 是原型工作区，正式结论应沉淀到 `docs/superpowers/**`。

## 更新检查清单

新增或修改文档前后检查：

- [ ] 是否存在更权威的文档需要同步。
- [ ] 是否需要加入 [docs/README.md](README.md)。
- [ ] 是否需要记录到 [CHANGELOG.md](CHANGELOG.md)。
- [ ] 是否有过期链接、重复入口或冲突描述。
- [ ] 是否说明了文档状态：权威来源、维护中、阶段性交付物、QA 产物。
- [ ] 是否避免把临时调试信息写入长期文档。

## 本轮治理范围

2026-06-09 文档治理采用 A 方案：

- 新增 `docs/README.md` 文档总索引。
- 新增 `docs/DOCUMENTATION-GOVERNANCE.md` 治理规则。
- 在根 `README.md` 增加文档入口。
- 在 `docs/CHANGELOG.md` 记录文档治理与管理端前端落地。
- 增加结构测试，防止索引和治理规则缺失。

