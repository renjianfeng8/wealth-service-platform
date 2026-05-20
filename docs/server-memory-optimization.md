# 云服务器内存优化方案

> 服务器配置：2C4G（实际 3.57GB），当前运行 8 个容器已占用 ~2.37GB（66%）
> 目标：在 3.57GB 内存约束下稳定运行全部 8 个后端微服务 + 必要中间件

---

## 一、当前状况

### 1.1 服务器硬件

| 项目 | 值 |
|---|---|
| 总内存 | 3.57 GB |
| 已使用 | 2.98 GB（含 buff/cache） |
| 可用 | 0.67 GB |
| Swap | 1 GB（已用 88 MB） |

### 1.2 当前运行容器及内存占用

| 容器 | 内存占用 | JVM 堆 | 说明 |
|---|---|---|---|
| wealth-nacos | **962 MB** | 1 GB | 默认配置，严重过量 |
| wealth-system | 607 MB | 512 MB | |
| wealth-gateway | 380 MB | 256 MB | |
| wealth-mysql | 374 MB | — | |
| wealth-redis | 33 MB | — | |
| wealth-nginx | 17 MB | — | |
| wealth-front | 9 MB | — | |
| wealth-front-user | 6 MB | — | |
| **合计** | **~2.37 GB** | | |

### 1.3 当前 JVM 堆配置

| 分组 | 服务 | 当前堆 |
|---|---|---|
| 512 MB 组 | system, user, product, trade, message | -Xms512m -Xmx512m |
| 256 MB 组 | gateway, account, search | -Xms256m -Xmx256m |

### 1.4 未启动的服务及预估需求

#### 缺失的后端微服务

| 服务 | 当前堆 | 预估实际占用 |
|---|---|---|
| wealth-user | 512 MB | ~600 MB |
| wealth-product | 512 MB | ~600 MB |
| wealth-trade | 512 MB | ~600 MB |
| wealth-message | 512 MB | ~600 MB |
| wealth-account | 256 MB | ~380 MB |
| wealth-search | 256 MB | ~380 MB |

#### 缺失的中间件

| 服务 | 预估占用 | 优先级 |
|---|---|---|
| RabbitMQ | ~150 MB | 中（消息推送依赖） |
| Elasticsearch | ~600 MB（当前 ES_JAVA_OPTS: 512M） | 中（搜索依赖） |
| Seata | ~200 MB | 低（分布式事务） |
| Sentinel | ~200 MB | 低（流量控制） |
| Zipkin | ~200 MB | 低（链路追踪） |
| Prometheus | ~200 MB | 低（监控） |
| Grafana | ~80 MB | 低（监控面板） |

### 1.5 瓶颈结论

**全部启动需要 ~6 GB+ 内存，当前仅 3.57 GB，缺口约 2.5 GB。**

---

## 二、优化方案

### 2.1 削堆：Nacos（最大收益）

Nacos standalone 默认 `-Xms1g -Xmx1g`，实际 256m 足够。

**措施：** 在 `docker-compose.yml` nacos 环境变量中增加：

```yaml
nacos:
  environment:
    JVM_XMS: 256m
    JVM_XMX: 256m
```

**效果：** 962 MB → ~350 MB，**节省 ~600 MB**

### 2.2 削堆：所有后端微服务

JVM RSS 估算公式：`Xmx + ~100 MB（线程栈、CodeCache、Metaspace 等 overhead）`

| 服务 | 当前堆 | 优化后 | 预估占用 |
|---|---|---|---|
| gateway | 256 MB | **128 MB** | ~200 MB |
| system | 512 MB | **256 MB** | ~350 MB |
| user | 512 MB | **256 MB** | ~350 MB |
| product | 512 MB | **256 MB** | ~350 MB |
| account | 256 MB | **128 MB** | ~200 MB |
| trade | 512 MB | **256 MB** | ~350 MB |
| message | 512 MB | **256 MB** | ~350 MB |
| search | 256 MB | **128 MB** | ~200 MB |
| **堆合计** | **3.5 GB** | **1.7 GB** | **~2.35 GB** |

**措施：** 修改 8 个 `Dockerfile` 的 `ENTRYPOINT` 中的 `-Xms`/`-Xmx` 参数。

**效果：** 8 个服务全部启动后从 ~4.2 GB 降到 ~2.35 GB，**节省 ~1.8 GB**

### 2.3 削堆：Elasticsearch

**措施：** 在 `docker-compose.yml` 中修改：

```yaml
elasticsearch:
  environment:
    ES_JAVA_OPTS: -Xms256m -Xmx256m   # 原 512m
```

**效果：** ~600 MB → ~350 MB，**节省 ~250 MB**

### 2.4 扩展 Swap（安全垫）

当前 1 GB swap，扩到 4 GB，防止内存峰值时 OOM。

```bash
# 在服务器上执行
fallocate -l 4G /swapfile
chmod 600 /swapfile
mkswap /swapfile
swapon /swapfile

# 写进 /etc/fstab 持久化
echo '/swapfile none swap sw 0 0' >> /etc/fstab
```

### 2.5 分级启动策略

按业务优先级分层，资源不足时优先保证上层：

| 梯队 | 服务 | 优先级 | 说明 |
|---|---|---|---|
| **第一梯队** | nacos, mysql, redis, nginx, front, front-user | P0 | 基础设施，必须运行 |
| **第二梯队** | gateway, system | P0 | 网关 + 权限，必须运行 |
| **第三梯队** | user, product, account | P1 | 核心业务（登录、产品浏览、自选） |
| **第四梯队** | trade, message, search | P2 | 低频业务（交易、消息、搜索） |
| **第五梯队** | rabbitmq, elasticsearch | P2 | 增强功能中间件 |
| **第六梯队** | seata, sentinel, zipkin, prometheus, grafana | P3 | 监控治理，本地调试时关闭 |

---

## 三、优化后预算

### 3.1 优化后的内存预估

| 组件 | 优化 | 预估占用 |
|---|---|---|
| **基础设施** | | **~950 MB** |
| Nacos | 1G → 256M | ~350 MB |
| MySQL | — | ~380 MB |
| Redis | — | ~35 MB |
| Nginx + Front × 2 | — | ~35 MB |
| RabbitMQ | — | ~150 MB |
| **后端微服务** | | **~2.35 GB** |
| Gateway | 256M → 128M | ~200 MB |
| System | 512M → 256M | ~350 MB |
| User | 512M → 256M | ~350 MB |
| Product | 512M → 256M | ~350 MB |
| Account | 256M → 128M | ~200 MB |
| Trade | 512M → 256M | ~350 MB |
| Message | 512M → 256M | ~350 MB |
| Search | 256M → 128M | ~200 MB |
| **Elasticsearch** | 512M → 256M | ~350 MB |
| **总计** | | **~3.65 GB** |

### 3.2 预算分析

| 场景 | 内存需求 | 可行性 |
|---|---|---|
| 仅 P0-P1（基建 + gateway + system + user + product + account） | ~2.6 GB | ✅ 充足 |
| 全部后端（P0-P2，不含 ES 和中间件） | ~3.3 GB | ✅ 配合 swap 可行 |
| 全部后端 + ES + RabbitMQ | ~3.65 GB | ⚠️ 接近上限，需要 swap |
| 全部服务（含监控链路） | ~4.5 GB+ | ❌ 仅靠削堆不够 |

### 3.3 swap 的作用

4 GB swap 下，即使偶尔超出物理内存，系统也不会立即 OOM：

- 活跃数据留在物理内存，冷数据换出到 swap
- JVM 的 Full GC 会扫描所有对象，频繁 swap 会导致性能下降，需监控
- **如果发现持续 swap 使用 > 500 MB，说明物理内存不足，需扩容**

---

## 四、实施步骤

### Step 1：修改 Dockerfile（本地）

8 个服务的堆配置调整：

| 文件 | 当前值 | 修改为 |
|---|---|---|
| wealth-gateway/Dockerfile | -Xms256m -Xmx256m | -Xms128m -Xmx128m |
| wealth-system/Dockerfile | -Xms512m -Xmx512m | -Xms256m -Xmx256m |
| wealth-user/Dockerfile | -Xms512m -Xmx512m | -Xms256m -Xmx256m |
| wealth-product/Dockerfile | -Xms512m -Xmx512m | -Xms256m -Xmx256m |
| wealth-account/Dockerfile | -Xms256m -Xmx256m | -Xms128m -Xmx128m |
| wealth-trade/Dockerfile | -Xms512m -Xmx512m | -Xms256m -Xmx256m |
| wealth-message/Dockerfile | -Xms512m -Xmx512m | -Xms256m -Xmx256m |
| wealth-search/Dockerfile | -Xms256m -Xmx256m | -Xms128m -Xmx128m |

### Step 2：修改 docker-compose.yml（本地）

- nacos 添加 `JVM_XMS=256m` / `JVM_XMX=256m`
- elasticsearch 修改 `ES_JAVA_OPTS` 为 `-Xms256m -Xmx256m`

### Step 3：提交并推送代码

触发的 CI 会自动构建 10 个 Docker 镜像并推送到 ghcr.io。

### Step 4：服务器增加 swap

```bash
fallocate -l 4G /swapfile
chmod 600 /swapfile
mkswap /swapfile
swapon /swapfile
echo '/swapfile none swap sw 0 0' >> /etc/fstab
```

### Step 5：服务器拉取新镜像并重启

```bash
cd /data/wealth-service-platform
docker compose pull
docker compose up -d --force-recreate gateway system
```

### Step 6：按梯队启动其余服务

```bash
# 第三梯队
docker compose up -d --force-recreate user product account
# 第四梯队
docker compose up -d --force-recreate trade message search
# 第五梯队（按需）
docker compose up -d --force-recreate rabbitmq elasticsearch
```

---

## 五、监控与验证

启动后通过以下方式验证内存是否健康：

```bash
# 查看内存整体状况
free -h

# 查看各容器内存
docker stats --no-stream --format 'table {{.Name}}\t{{.MemUsage}}\t{{.MemPerc}}'

# 查看 swap 使用（持续 >500MB 说明物理内存不足）
swapon --show

# 查看 Java 进程实际堆使用
docker exec <container> jhsdb jmap --heap --pid 1
```
