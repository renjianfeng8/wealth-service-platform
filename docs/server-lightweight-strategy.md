# 云服务器轻量化部署策略

> 目标：在 3.6GB RAM 的云服务器上稳定运行完整业务链路
> 策略：砍掉重量级中间件 + 模块分级管理 + 合并部署

---

## 一、背景

服务器配置：4 vCPU / 3.6GB RAM，运行 15 个容器，内存占用 92%。

当前瓶颈：
- 9 个基础设施容器（nacos、mysql、redis、rabbitmq、es、nginx、zipkin、prometheus、grafana）占去大半内存
- 8 个后端微服务 JVM 即使已调低堆大小，合计仍超 1.5GB
- 一人维护 8 个模块 + 9 个中间件，负担过重

---

## 二、中间件分级

### 2.1 必需（始终运行）

| 中间件 | 用途 | 估算内存 |
|--------|------|----------|
| Nacos | 服务注册发现、配置中心 | ~200MB |
| MySQL | 业务数据持久化 | ~300MB |
| Redis | 缓存、登录限流 | ~80MB |
| Nginx | 反向代理、SSL 终止 | ~30MB |

### 2.2 按需（开发/调试时启动，日常关闭）

| 中间件 | 用途 | 估算内存 | 关闭影响 |
|--------|------|----------|----------|
| Elasticsearch | 全文检索 | ~1GB | search 模块降级为 MySQL LIKE 查询 |
| RabbitMQ | 消息推送 | ~150MB | message 模块改为扫库轮询 |
| Zipkin | 链路追踪 | ~200MB | 无功能影响，丢失追踪数据 |
| Prometheus | 指标采集 | ~300MB | 丢失监控指标 |
| Grafana | 监控看板 | ~200MB | 无法查看监控面板 |
| Sentinel | 流量控制 | ~100MB | 熔断降级不生效 |
| Seata | 分布式事务 | ~200MB | 分布式事务不生效 |

### 2.3 日常运行推荐配置

```
始终运行：nacos mysql redis nginx
     关闭：es rabbitmq zipkin prometheus grafana sentinel seata
```

节省内存：**~2GB+**

---

## 三、模块分级维护

### 3.1 核心模块（全功能维护）

| 模块 | 职责 | 维护策略 |
|------|------|----------|
| gateway | 统一入口、路由、跨域 | 正常 feature 开发 |
| system | 登录、权限、RBAC | 正常 feature 开发 |
| product | 产品管理、行情数据 | 正常 feature 开发 |

### 3.2 基础模块（只修 bug，不增功能）

| 模块 | 职责 | 维护策略 |
|------|------|----------|
| user | 系统用户 CRUD | 保持现有接口，仅合入 bugfix |
| trade | 交易委托 | 保持现有接口，仅合入 bugfix |

### 3.3 精简模块（代码冻结，能跑就行）

| 模块 | 职责 | 维护策略 |
|------|------|----------|
| account | 用户自选 | 合并到 product 模块内部，停掉独立服务 |
| message | 消息推送 | 去掉 RabbitMQ 依赖，改为扫库轮询 |
| search | 全文搜索 | ES 不可用时降级为 MySQL LIKE 查询 |

---

## 四、部署合并方案

不改代码结构，只在部署层面减少 JVM 数量：

### 当前方案：8 个模块 → 8 个 JVM

```
gateway (8080) → system (8082) → user (8083) → product (8084)
→ trade (8085) → account (8086) → message (8087) → search (8089)
```

### 合并方案：8 个模块 → 5 个 JVM

```
gateway (8080)  ← 不变
system (8082)   ← 不变
product (8084)  ← 合并 account（自选接口作为 product 的子端点）
trade (8085)    ← 合并 message（消息推送作为 trade 的子端点）
user (8083)     ← 合并 search（ES 降级后轻量）
```

### JVM 堆分配

| 服务 | 堆大小 | 年内存 |
|------|--------|--------|
| gateway | 128m | ~150MB |
| system | 256m | ~300MB |
| product(+account) | 256m | ~300MB |
| trade(+message) | 128m | ~150MB |
| user(+search) | 128m | ~150MB |
| **合计** | | **~1.05GB** |

---

## 五、执行计划

### Phase 1 — 立即止损（今天）

1. 停止 ES、RabbitMQ、Zipkin、Prometheus、Grafana 容器
2. 确认 search 模块使用 MySQL 降级兜底
3. 观察内存使用降至安全水位

### Phase 2 — 精简模块改造（本周）

1. search 模块：ES 降级为 MySQL `LIKE` 查询
2. account 模块：功能合并到 product 模块，停掉独立 account 服务
3. message 模块：去掉 RabbitMQ 依赖，改为启动时扫库轮询
4. 调整 docker-compose.yml 只部署合并后的 5 个服务

### Phase 3 — 持续优化

1. 观察线上稳定性，按需调整 JVM 堆大小
2. 非核心模块不主动开发新功能
3. 中间件按需启动，日常只保留必需的最小集合

---

## 六、维护纪律

- 核心模块 → 正常 git flow，feature branch，code review
- 基础模块 → 直接维护，只合 bugfix，不接新需求
- 精简模块 → 不主动修改，除非被核心模块的接口变更波及
- 中间件 → 日常关闭，仅开发调试时按需启动
- 内存 > 85% 时优先关停非必需容器，而非扩容
