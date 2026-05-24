# 项目文件清理设计

## 背景

项目经过多次重构和模块合并后，积累了大量杂乱无用文件：构建产物、运行时日志、错误生成的空目录、调试临时文件、以及旧版设计文档。需要进行系统性清理，并更新 `.gitignore` 防止同类文件再次被提交。

## 清理范围（标准方案）

### 1. 构建产物（可重新生成）

| 路径 | 说明 |
|------|------|
| `front/dist/` | Vue 前端构建输出 |
| `front-user/dist/` | 用户端前端构建输出 |
| `front-user/playwright-report/` | Playwright E2E 测试 HTML 报告 |
| `front-user/test-results/` | Playwright 测试失败截图与缓存 |
| `.vite/deps/` | Vite 依赖缓存 |
| `api-e2e/api-e2e-report/` | API E2E 测试报告 |
| `api-e2e/test-results/` | API E2E 测试缓存 |
| `api-e2e/node_modules/` | API E2E 依赖 |

### 2. 运行时日志（无保留价值）

| 路径 | 大小 |
|------|------|
| `logs/` | 8MB |
| `gateway.log` | 160KB |
| `service.log` | 4.7MB |

### 3. 错误生成的空目录

| 路径 | 原因 |
|------|------|
| `api-e2e/D:demowealth-*`（8个） | 脚本 bug 产生，路径片段损坏 |
| `api-e2e/api-e2e-report/screenshots/` | 空 |
| `wealth-service/src/main/java/com/wealth/platform/search/mapper/` | MyBatis Mapper 目录从未使用 |
| `wealth-service/src/main/java/com/wealth/platform/user/config/` | SwaggerConfig 删除后残留 |
| `wealth-service/src/test/resources/` | 空 |

### 4. 临时调试文件

| 路径 | 说明 |
|------|------|
| `wealth-service/deps.txt` | Maven 依赖树调试输出 |
| `front/tsconfig.tsbuildinfo` | TypeScript 增量构建缓存 |

### 5. 已完成的旧计划文档

| 路径 | 说明 |
|------|------|
| `docs/superpowers/plans/2026-05-24-code-quality-cleanup.md` | 已完成 |
| `docs/superpowers/plans/2026-05-24-infrastructure-hardening.md` | 已完成 |
| `docs/superpowers/plans/2026-05-24-module-merge-cleanup.md` | 已完成 |


## `.gitignore` 保护措施

在根 `.gitignore` 中添加以下模式，防止构建产物/环境文件/日志等再次被提交：

```gitignore
front/dist/
front-user/dist/
front-user/playwright-report/
front-user/test-results/
.vite/
api-e2e/api-e2e-report/
api-e2e/test-results/
api-e2e/node_modules/
logs/
*.log
backups/
ssl/
.env
.env.prod
.env.example
```

## 不处理项（有意保留）

- `docs/superpowers/specs/*.md` — 设计归档，保留为架构参考
- `.idea/` 和 `.vscode/` — IDE 配置，可能包含用户特定设置，不强制清理但 `.gitignore` 会覆盖
- `.env` 和 `ssl/` — 虽然含敏感信息，但当前已被 git 追踪，清理方案需独立处理（不在本次范围）
- `backups/*.sql.gz` — 数据库备份，同样的 git 追踪问题，需独立处理
- `.claude/worktrees/` — Claude 工作树缓存，不在 git 中
