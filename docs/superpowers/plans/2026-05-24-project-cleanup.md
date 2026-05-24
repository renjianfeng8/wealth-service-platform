# 项目文件清理 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 删除项目中的杂乱无用文件（构建产物、日志、空目录、调试文件、旧计划文档），更新 `.gitignore` 防止再提交

**Architecture:** 纯文件系统操作 + `.gitignore` 编辑，无代码逻辑变更。按类别分批删除，每批独立提交。

**Tech Stack:** N/A（文件清理）

---

### Task 1: 删除前端构建产物与测试报告

**Files:**
- Delete: `front/dist/`
- Delete: `front-user/dist/`
- Delete: `front-user/playwright-report/`
- Delete: `front-user/test-results/`
- Delete: `.vite/deps/`
- Delete: `api-e2e/api-e2e-report/`
- Delete: `api-e2e/test-results/`
- Delete: `api-e2e/node_modules/`

- [ ] **Step 1: 删除所有构建产物和测试报告目录**

```bash
rm -rf front/dist front-user/dist front-user/playwright-report front-user/test-results .vite/deps api-e2e/api-e2e-report api-e2e/test-results api-e2e/node_modules
```

### Task 2: 删除运行时日志

**Files:**
- Delete: `logs/`
- Delete: `gateway.log`
- Delete: `service.log`

- [ ] **Step 1: 删除日志文件和目录**

```bash
rm -rf logs gateway.log service.log
```

### Task 3: 删除空目录与调试临时文件

**Files:**
- Delete: `api-e2e/D:demowealth-*`（8个空目录）
- Delete: `api-e2e/api-e2e-report/screenshots/`
- Delete: `wealth-service/src/main/java/com/wealth/platform/search/mapper/`
- Delete: `wealth-service/src/main/java/com/wealth/platform/user/config/`
- Delete: `wealth-service/src/test/resources/`
- Delete: `wealth-service/deps.txt`
- Delete: `front/tsconfig.tsbuildinfo`

- [ ] **Step 1: 删除空目录和临时文件**

```bash
rm -rf api-e2e/D:demowealth-* api-e2e/api-e2e-report/screenshots wealth-service/src/main/java/com/wealth/platform/search/mapper wealth-service/src/main/java/com/wealth/platform/user/config wealth-service/src/test/resources wealth-service/deps.txt front/tsconfig.tsbuildinfo
```

### Task 4: 删除已完成的旧计划文档

**Files:**
- Delete: `docs/superpowers/plans/2026-05-24-code-quality-cleanup.md`
- Delete: `docs/superpowers/plans/2026-05-24-infrastructure-hardening.md`
- Delete: `docs/superpowers/plans/2026-05-24-module-merge-cleanup.md`

- [ ] **Step 1: 删除旧计划文档**

```bash
rm docs/superpowers/plans/2026-05-24-code-quality-cleanup.md docs/superpowers/plans/2026-05-24-infrastructure-hardening.md docs/superpowers/plans/2026-05-24-module-merge-cleanup.md
```

### Task 5: 更新 `.gitignore` 保护

**Files:**
- Modify: `.gitignore`

- [ ] **Step 1: 添加缺失的 `front-user/test-results/` 到 `.gitignore`**

在 `.gitignore` 的 `front-user/playwright-report/` 行后添加一行 `front-user/test-results/`。
