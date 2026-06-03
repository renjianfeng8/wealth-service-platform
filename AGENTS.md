# AGENTS.md

本文件提供 **Codex（Codex.ai/code）** 在此仓库中的行为指引。

**项目规范以 [CLAUDE.md](CLAUDE.md) 为准**，本文档仅补充 Codex 特有的行为规则，不重复项目内容。

**相关文档：**
- [项目规范（CLAUDE.md）](CLAUDE.md) — 技术栈、编码规范、Git 提交、代码模式、测试规范等所有项目级规则
- [模块架构与配置体系](docs/ARCHITECTURE.md) — 跨模块开发时引用
- [数据库表结构与字段](docs/DATABASE-SCHEMA.md) — 写实体类时引用
- [Bug 记录](docs/BUG.md) — 排查已知问题

---

## 一、角色定位

Codex 在此项目中的定位是**主控端**：

- 负责架构设计、任务分解、方案评审、代码审查
- Claude Code（执行端）按指令完成实现
- 详见 CLAUDE.md §九（协作约定）

## 二、AI 生成规则（Codex 特有）

当 Codex 生成或审查代码时，除遵守 CLAUDE.md 全部规范外，额外要求：

1. 必须严格按照 DATABASE-SCHEMA.md 中的表结构生成 Entity（继承 BaseEntity）、Mapper、Service、Controller、Vo、Dto
2. 必须使用 MyBatis-Plus
3. Entity 必须继承 BaseEntity，按照 DATABASE-SCHEMA.md §三处理字段覆盖
4. 必须自动填充 create_time、update_time
5. 接口必须遵循 RESTful 规范
6. 必须加 Swagger 注解（`@Tag` / `@Operation`）
7. 必须符合项目技术栈
8. 不允许生成不存在的表或字段
9. 生成代码必须能直接运行
10. **写操作（增删改）必须加 `@Transactional(rollbackFor = Exception.class)`**
11. **所有 `@RequestBody` DTO 必须加 `@Valid` 参数校验注解**
12. **新增接口须确认是否需要加入权限白名单 `AuthConstant.PERMIT_ALL_URLS`**

## 三、配置修改审批

- application.yml / application-prod.yml / pom.xml 默认**锁定状态**
- 如需修改必须询问用户并给出强理由

## 四、启动验证清单

执行启动验证时，逐项确认：

- Docker 容器全部运行（mysql、redis、nginx）
- `.env` 文件已配置（根目录 + gateway + service）
- 全量编译通过
- 启动顺序：gateway → wealth-service
- 冒烟测试：`POST /system/umsAdmin/login` 返回 JWT
- 前端可访问

> 详细启动流程见 [CLAUDE.md §十](CLAUDE.md#十启动验证) 和 [docs/STARTUP.md](docs/STARTUP.md)。
