# 全链路配置对齐检查报告

> 生成日期：2026-05-20（最近同步：2026-05-21）
> 检查范围：本地开发环境 ↔ 云服务器生产环境 (124.222.155.20)
> 检查内容：前端配置、后端配置、Nginx、Docker、Nacos、环境变量

---

## 第 I 部分：本地配置一致性检查

### 1.1 前端配置

| 前端 | Vite 端口 | API baseURL | Vite 代理 |
|------|----------|-------------|----------|
| front (管理后台) | 3000 | `/api/v1` | `/api/v1` → `localhost:8080` |
| front-user (用户端) | 3001 | `/api/v1` | `/api/v1` → `localhost:8080` |

**结论：✅ 前端路径一致**（无 .env 文件，baseURL 硬编码于 `src/api/index.ts`）

### 1.2 后端端口与路由

| 服务 | 服务名 | dev 端口 | context-path | Docker 映射 | Gateway 路由 |
|------|--------|---------|-------------|------------|-------------|
| gateway | wealth-gateway | 8080 | - | 8080:8080 | - |
| system | wealth-system | 8082 | /system | 8082:8082 | /system/** |
| user | wealth-user | 8083 | /user | 8083:8083 | /user/** |
| product | wealth-product | 8084 | /product | 8084:8084 | /product/** |
| trade | wealth-trade | 8085 | /trade | 8085:8085 | /trade/** |
| account | wealth-account | 8086 | /account | 8086:8086 | /account/** |
| message | wealth-message | 8087 | /message | 8087:8087 | /message/** |
| search | wealth-search | 8089 | /search | 8089:8089 | /search/** |

**结论：✅ 全部一致**

### 1.3 Nginx 路径流转验证

```
请求: /api/v1/system/user/list
→ Nginx location /api/v1/system/ { rewrite ^/api/v1(/.*) $1 break }
→ proxy_pass → http://gateway:8080/system/user/list
→ Gateway route /system/** → lb://wealth-system
→ System context-path=/system, 实际处理路径 /user/list
```

**结论：✅ Nginx → Gateway → 微服务路径全部对齐**

### 1.4 本地已知不一致项

| 问题 | 等级 | 说明 |
|------|------|------|
| 数据库密码变量名 `DB_PASSWORD` vs `MYSQL_ROOT_PASSWORD` | 🟠 中 | application.yml 使用 `${DB_PASSWORD}`，.env 使用 `MYSQL_ROOT_PASSWORD` |
| docker-compose 未设置 `spring.profiles.active=prod` | 🟠 中 | HikariCP 连接池调优参数不生效 |
| Message 模 RabbitMQ 默认连接 `localhost` | 🔴 严重 | docker-compose 中缺 `RABBITMQ_HOST=rabbitmq` 环境变量 |
| Gateway prod SSL 8443 端口未使用 | 🟡 低 | prod profile 未激活，docker 中用 8080 配合 nginx SSL 终止 |
| 前端 .env 文件不存在 | 🟡 低 | 路径硬编码，不影响运行 |

---

## 第 II 部分：本地 vs 云服务器配置对比

> **注**：本报告生成后生产服务器已有进一步部署（2026-05-20 晚），后端 8 个服务已全部启动运行，但内存压力极大（3.3/3.6GB），以下逐项对比仍反映根本性配置差异。

### 2.1 docker-compose.yml 对比

| 配置项 | 本地 (当前工作副本) | 云服务器 (运行中) | 状态 |
|-------|-------------------|-----------------|:----:|
| **MySQL 端口映射** | `3307:3306` | `3306:3306` | ❌ |
| **Gateway - CORS_ALLOWED_ORIGINS** | 无 | `https://rjfwealth.cn` | ❌ |
| **Gateway - JWT_SECRET** | 无 | `wealth-micro-service-...` **(出现 2 次)** | ❌ |
| **Gateway - JWT_EXPIRE** | 无 | `604800000` **(出现 2 次)** | ❌ |
| **Gateway - spring.redis.host** | 无 | `redis` | ❌ |
| **Gateway - spring.elasticsearch.uris** | `http://elasticsearch:9200` | `http://elasticsearch:9200` | ✅ |
| **所有后端服务 - JWT_SECRET** | 无 | 每个服务都有 | ❌ |
| **所有后端服务 - JWT_EXPIRE** | 无 | 每个服务都有 | ❌ |
| **System - spring.application.name** | 无 | `wealth-system` | ❌ |
| **Search - JWT_SECRET** | 无 | **(出现 2 次)** | ❌ |
| **Search - JWT_EXPIRE** | 无 | **(出现 2 次)** | ❌ |
| **Nacos auth 变量名** | `NACOS_AUTH_TOKEN` | `NACOS_AUTH_TOKEN` | ✅ |
| **Seata config 挂载** | `./seata-config/` | `./seata-config/` | ✅ |
| **Nginx SSL 挂载** | `./ssl` | `./ssl` | ✅ |

### 2.2 .env 文件对比

| 变量 | 本地 (.env) | 云服务器 (.env) | 状态 |
|-----|------------|----------------|:----:|
| **MYSQL_ROOT_PASSWORD** | `WealthPlatform@2026` | `WealthPlatform@2026` | ✅ |
| **NACOS_USERNAME** | `nacos` | `nacos` | ✅ |
| **NACOS_PASSWORD** | `nacos` | `nacos` | ✅ |
| **NACOS_AUTH_TOKEN** | `RGV2TmFjb3NTZWNyZXRLZXlGb3JXZWFsdGhQbGF0Zm9ybTIwMjY=` (DevNacosSecretKeyForWealthPlatform2026) | `V2VhbHRoUGxhdGZvcm1AaW5mcmEtc2VjcmV0LWtleS0yMDI2` (WealthPlatform@infra-secret-key-2026) | ❌ |
| **ES_USERNAME** | `elastic` | `elastic` | ✅ |
| **ES_PASSWORD** | `WealthPlatform@2026` | `WealthPlatform@2026` | ✅ |
| **ES_SECURITY_ENABLED** | `true` | `true` | ✅ |
| **RABBITMQ_DEFAULT_USER** | `guest` | `wealth` | ❌ |
| **RABBITMQ_DEFAULT_PASS** | `guest` | `WealthPlatform@2026` | ❌ |
| **GRAFANA_ADMIN_PASSWORD** | `admin` | `WealthPlatform@2026` | ❌ |
| **SEATA_SECRET_KEY** | `DevSeataSecretKey0123...` | `WealthPlatformSeataSecretKey2026` | ❌ |

### 2.3 nginx.conf 对比

**结论：✅ 内容完全一致**

### 2.4 Nacos 共享配置 (wealth-shared.yaml)

```yaml
jwt:
  secret: wealth-micro-service-20260501-very-safe-secret-key-123456789
  expire: 604800000

auth:
  permit_all_urls:
    - /system/admin/login
    - /system/admin/register
    - /user/user/login
    - /user/user/register
    - /product/wea-market-data/sse
    - /product/wea-market-data/list
    - /product/wea-market-data/page
    - /product/wea-product/list
    - /product/wea-product/page
    - /news/wea-news/list
    - /news/wea-news/page
    - /search/**
    - /actuator/health
    - /actuator/prometheus
    - /v3/api-docs
    - /swagger-ui/**
```

| 检查项 | 结果 |
|-------|:----:|
| JWT secret 长度 ≥ 32 | ✅ (55 字符) |
| JWT expire | ✅ `604800000` (7天) |
| permit_all_urls 含 SSE | ✅ `/product/wea-market-data/sse` |
| Nacos 中含 Redis 配置 | ❌ **未发现**，应用使用默认值 |
| wealth-system.yaml 配置 | ⚠️ 不完整（URL 缺少参数） |

### 2.5 云服务器容器运行状态（2026-05-21 更新）

| 容器 | 状态 | 端口映射 |
|------|:----:|---------|
| wealth-nacos | ✅ | 8848:8848, 9848:9848 |
| wealth-mysql | ✅ healthy | 3306:3306 |
| wealth-redis | ✅ (不可用) | 6379:6379 |
| wealth-rabbitmq | ✅ | 5672:5672 |
| wealth-es | ✅ | 9200:9200 |
| wealth-nginx | ✅ | 80:80, 443:443 |
| wealth-zipkin | ✅ | 9411:9411 |
| wealth-prometheus | ✅ | 9090:9090 |
| wealth-grafana | ✅ | 3001:3001 |
| wealth-sentinel | ❌ | - |
| wealth-seata | ❌ | - |
| wealth-gateway | ✅ | 8080:8080 |
| wealth-system | ✅ (Redis 不可用致 health=DOWN) | 8082:8082 |
| wealth-user | ✅ | 8083:8083 |
| wealth-product | ✅ | 8084:8084 |
| wealth-trade | ✅ | 8085:8085 |
| wealth-account | ✅ | 8086:8086 |
| wealth-message | ✅ | 8087:8087 |
| wealth-search | ✅ | 8089:8089 |
| wealth-front | ✅ | 3000:80 |
| wealth-front-user | ✅ | 3002:80 |
| wealth-mysql-backup | ❌ | - |

**Nacos 已注册服务**：8 个后端服务已全部注册 ✅

> 当前问题：内存 92%（3.3/3.6GB），Redis 因 Alpine musl 兼容性问题不可用（已降级处理），Nacos 配置未发布。

---

## 第 III 部分：问题清单

### 🔴 严重问题

#### 🔴 P1. 服务器内存严重不足（已全量部署但处于临界状态）

- **现状**：8 个后端服务已全部启动运行 ✅，但 **内存高达 92%（3.3/3.6GB）**，swap 已用 2.5GB/5GB
- **根因**：JVM 堆虽已调优（v1.7.1），但 15 个容器并发仍超过 3.6GB 物理内存上限
- **影响**：内存耗尽风险高，可能触发 OOM

#### 🔴 P2. 服务器 docker-compose.yml 与本地严重偏离

- 服务器 docker-compose.yml 被手动添加了大量本地没有的环境变量
- Gateway 和 Search 存在 **重复的 JWT_SECRET/JWT_EXPIRE**（各出现2次）
- MySQL 端口本地改为 3307 但服务器仍是 3306
- 后续重新部署时若直接用本地 docker-compose.yml 覆盖，会丢失 JWT、CORS、Redis 等环境变量

#### 🔴 P3. 部分基础设施服务未运行

- rabbitmq、es ✅ **已启动**（message/search 可连接）
- sentinel、seata ❌ **未启动**
- zipkin、prometheus、grafana ✅ **已启动**（可观测性可用）
- Redis ❌ **已启动但不可用**（Alpine musl 兼容性问题，已做降级处理）

#### 🔴 P4. NACOS_AUTH_TOKEN 本地与服务器值不同

- 本地：`DevNacosSecretKeyForWealthPlatform2026`
- 服务器：`WealthPlatform@infra-secret-key-2026`
- 若直接覆盖服务器 .env，Nacos 将因 token 不匹配而拒绝服务

### 🟠 中等问题

#### 🟠 P5. 数据库密码变量名不统一

- `application.yml`: `${DB_PASSWORD}`
- `.env` / docker-compose: `MYSQL_ROOT_PASSWORD`
- 本地 dev 启动（非 docker）需要额外设置 DB_PASSWORD

#### 🟠 P6. docker-compose 未激活 prod profile

- 所有服务以 default profile 运行，`application-prod.yml` 不加载
- HikariCP 连接池参数（maximum-pool-size: 30 等）不生效

#### 🟠 P7. Message 模块 RabbitMQ 地址错误

- docker-compose.yml 中 message 服务缺少 `RABBITMQ_HOST=rabbitmq`
- 容器内默认 `localhost:5672`，无法连接 rabbitmq 容器

### 🟡 低风险问题

#### 🟡 P8. 服务器 .env 密码值与本地不同

| 变量 | 本地 | 服务器 | 说明 |
|-----|------|-------|------|
| RABBITMQ_DEFAULT_USER | `guest` | `wealth` | 服务器使用规范账号 |
| RABBITMQ_DEFAULT_PASS | `guest` | `WealthPlatform@2026` | 服务器使用生产密码 |
| GRAFANA_ADMIN_PASSWORD | `admin` | `WealthPlatform@2026` | 服务器使用生产密码 |
| SEATA_SECRET_KEY | `DevSeataSecretKey...` | `WealthPlatformSeataSecretKey2026` | 安全级别不同 |

> 服务器使用生产密码是正确做法，但需在文档中记录并确保本地 .env.example 对齐

#### 🟡 P9. Nacos 配置不完整

- `wealth-system.yaml` 中的 datasource URL 缺少完整参数
- `wealth-shared.yaml` 中没有 Redis 配置

#### 🟡 P10. 服务器磁盘空间 44% 且镜像占用大

- 40GB 磁盘已用 18GB（44%），Docker 镜像占 11GB（其中 7.5GB 可回收）
- 长期运行需考虑清理或扩容

---

## 第 IV 部分：全链路一致性总结矩阵

| 链路 | 前端→Gateway | Gateway→后端服务 | 后端→MySQL | 后端→Redis | 后端→RabbitMQ | 后端→ES | Nginx 路径 |
|-----|-------------|----------------|-----------|-----------|--------------|--------|-----------|
| **本地开发** | ✅ localhost:3000 → 8080 | ✅ lb://service | ✅ localhost:3306 | ✅ Nacos 默认 | ✅ localhost:5672 | ✅ localhost:9200 | N/A |
| **本地 Docker** | ✅ frontend:80 → gateway:8080 | ✅ lb://service | ✅ mysql:3306 | ✅ Nacos 默认 | ❌ message → localhost | ✅ elasticsearch:9200 | ✅ rewrite 对齐 |
| **云服务器** | ✅ rjfwealth.cn → nginx → gateway:8080 | ✅ lb://service | ✅ mysql:3306 | ⚠️ redis:6379（不可用）| ✅ rabbitmq:5672 | ✅ elasticsearch:9200 | ✅ rewrite 对齐 |

---

## 附录：服务器环境信息

| 项目 | 值 |
|------|-----|
| 服务器 IP | 124.222.155.20 (腾讯云) |
| OS | OpenCloudOS 9.4 |
| CPU | 4 vCPU |
| RAM | 3.6 GB |
| 磁盘 | 40 GB (已用 18 GB, 44%) |
| Docker 版本 | CE (Compose v2) |
| 运行时长 | 1h44min (最近重启过) |
| 证书域名 | rjfwealth.cn / www.rjfwealth.cn |
| 证书有效期 | 至 2026-08-16 |
