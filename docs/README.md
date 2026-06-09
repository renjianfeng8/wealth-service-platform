# 文档总索引

本目录收纳 `wealth-service-platform` 的项目级文档。阅读顺序建议从本页开始，再按目标进入对应文档。

## 核心规范

| 文档 | 用途 | 状态 |
| --- | --- | --- |
| [CLAUDE.md](../CLAUDE.md) | 项目级开发规范、技术栈、协作约定、测试与启动要求 | 权威来源 |
| [AGENTS.md](../AGENTS.md) | Codex 在本仓库中的补充规则 | 权威来源 |
| [DOCUMENTATION-GOVERNANCE.md](DOCUMENTATION-GOVERNANCE.md) | 文档分类、命名、归档与更新检查规则 | 权威来源 |

## 启动与部署

| 文档 | 用途 | 状态 |
| --- | --- | --- |
| [STARTUP.md](STARTUP.md) | 本地环境、依赖服务、启动顺序和冒烟验证 | 权威来源 |
| [../README.md](../README.md) | 项目总览、技术栈、快速开始和里程碑 | 对外入口 |
| [../CONTRIBUTING.md](../CONTRIBUTING.md) | 贡献流程、提交规范和 PR 要求 | 协作入口 |

## 架构与数据

| 文档 | 用途 | 状态 |
| --- | --- | --- |
| [ARCHITECTURE.md](ARCHITECTURE.md) | 模块架构、端口路由、配置体系和部署结构 | 权威来源 |
| [DATABASE-SCHEMA.md](DATABASE-SCHEMA.md) | 数据库表结构、字段约束和 Entity 生成依据 | 权威来源 |
| [BUG.md](BUG.md) | 已知问题、历史缺陷和排查记录 | 维护中 |
| [CHANGELOG.md](CHANGELOG.md) | 项目级功能、架构和文档变更记录 | 维护中 |

## Superpowers 交付物

Superpowers 文档记录阶段性方案、计划和落地交付物，不替代核心规范。

| 目录 | 用途 | 状态 |
| --- | --- | --- |
| [superpowers/specs](superpowers/specs) | 设计规格、原型说明、接口建议 | 阶段性交付物 |
| [superpowers/plans](superpowers/plans) | 实施计划、落地汇总、验证记录 | 阶段性交付物 |

当前高价值交付物：

- [Admin Operations Console Design](superpowers/specs/2026-06-07-admin-operations-console-design.md)
- [Admin Figma Prototype](superpowers/specs/2026-06-09-admin-figma-prototype.md)
- [Admin Backend Interface Adaptation](superpowers/specs/2026-06-09-admin-backend-interface-adaptation.md)
- [Admin Frontend Rollout Summary](superpowers/plans/2026-06-09-admin-frontend-rollout-summary.md)

## 更新检查

新增或大改文档时请同步检查：

- 是否应加入本索引。
- 是否影响 [CLAUDE.md](../CLAUDE.md)、[STARTUP.md](STARTUP.md)、[ARCHITECTURE.md](ARCHITECTURE.md) 或 [DATABASE-SCHEMA.md](DATABASE-SCHEMA.md) 这些权威来源。
- 是否需要补 [CHANGELOG.md](CHANGELOG.md)。
- 是否符合 [文档治理规则](DOCUMENTATION-GOVERNANCE.md)。

