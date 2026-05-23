# 模块合并与中间件精简优化方案

> 本文档针对「模拟理财交易服务」演示项目的部署架构优化方案。
> 背景：当前 8 个 Java 模块 + 7 个中间件共 15 个容器运行在 4vCPU/3.6GB 服务器上，因内存超卖导致 OOM 杀容器及 ERR_CONNECTION_CLOSED 问题。

---

## 一、精简目标

- 服务数量：8 个 Java 进程 → **2 个**
- 容器总数：15 个 → **5 个**
- 内存占用：~4.5GB（超卖）→ **~1.3GB（空闲 2.3GB）**

---

## 二、服务合并方案

### 2.1 核心思路

六个业务服务模块合并为一个 `wealth-service` 单体应用。网关 `wealth-gateway` 保留独立进程作为流量入口，路由从 Nacos 服务发现改为静态指向。

### 2.2 合并前后对比

| 当前模块 | 端口 | 合并后 | 端口 |
|---------|------|--------|------|
| wealth-system | 8082 | → | |
| wealth-user | 8083 | → | |
| wealth-product | 8084 | → **wealth-service** | **8081** |
| wealth-trade | 8085 | → | |
| wealth-message | 8087 | → | |
| wealth-search | 8089 | → | |
| wealth-gateway | 8080 | → wealth-gateway（保留） | 8080 |
| wealth-common | — | → 公共库 jar，无容器 | — |

### 2.3 合并方式

创建新模块 `wealth-service`，将 6 个业务模块的 `controller/`、`service/`、`mapper/`、`entity/` 移入，保留各自的包路径前缀，在同一个 Spring Boot 应用中启动。

**路由区分：** 通过 `@RequestMapping` 路径首段区分各模块功能（`/system/**`、`/user/**`、`/product/**` 等），Gateway 按路径前缀转发。

**去重处理：** 6 套 `application.yml` 合并为一套，数据源、Redis、MyBatis-Plus 等公共配置只声明一次。依赖在同一个 `pom.xml` 中管理。

### 2.4 精简后的 Java 服务清单

| # | 服务 | 端口 | 职责 |
|---|------|------|------|
| 1 | wealth-gateway | 8080 | API 网关：路由转发、JWT 校验 |
| 2 | wealth-service | 8081 | 全部业务逻辑（原 6 个服务合并） |
| — | wealth-common | — | 公共库（jar，无容器） |

---

## 三、中间件精简方案

### 3.1 移除的中间件

| 中间件 | 移除理由 | 替代方案 | 节省内存 |
|--------|---------|---------|---------|
| Nacos | 合并为单体后不再需要服务发现和远程配置中心 | 配置直接写入 application.yml，Gateway 路由改为静态指向 | ~256MB |
| Prometheus | 演示项目不需要持续监控采集 | 按需使用 `curl /actuator/health` 或 `docker stats` | ~192MB |
| Grafana | 演示项目不需要监控可视化面板 | 同上 | ~128MB |
| mysql-backup | 演示数据无高价值，无需定时备份 | 手动 `mysqldump` 按需执行 | ~32MB |

### 3.2 保留的中间件

| 中间件 | 端口 | 保留理由 |
|--------|------|---------|
| MySQL | 3306 | 核心业务数据持久化 |
| Redis | 6379 | 缓存、登录态、防重放 |
| Nginx | 80/443 | SSL 终止、HTTPS、反向代理、静态资源服务 |

### 3.3 中间件数量变化

```
精简前：7 个（Nacos, MySQL, Redis, Nginx, Prometheus, Grafana, mysql-backup）
精简后：3 个（MySQL, Redis, Nginx）
```

---

## 四、前端容器取舍

| 方案 | 做法 | 容器变化 | 内存变化 |
|------|------|---------|---------|
| **方案 A（推荐）** | 前端 dist 直接放入 Nginx 容器，由 Nginx 直接 serve | 移除 front、front-user | 省 ~64MB |
| **方案 B（保守）** | 保留 front、front-user，内存限制降到 32m | 保留，限制内存 | 省 ~32MB |

---

## 五、部署架构

```
浏览器
  ↓ HTTPS
Nginx
  ├── / → 前端静态资源（管理端）
  ├── /user-portal/ → 前端静态资源（用户端）
  └── /api/v1/* → Gateway:8080
                    └── http://wealth-service:8081/*

中间件：MySQL + Redis
```

---

## 六、内存预算

### 6.1 精简后容器内存清单（方案 A）

| 容器 | 预估内存 | 说明 |
|------|---------|------|
| MySQL | 400m | innodb_buffer_pool_size=256M |
| Redis | 96m | 缓存，数据量极小 |
| Nginx | 64m | 含 SSL + 静态文件 + 反向代理 |
| wealth-gateway | 256m | 网关 |
| wealth-service | 512m | 全部业务逻辑 |
| **总计** | **~1.33GB** | **占用 3.6GB 的 37%** |

### 6.2 精简后容器内存清单（方案 B）

| 容器 | 预估内存 |
|------|---------|
| MySQL | 400m |
| Redis | 96m |
| Nginx | 48m |
| front | 32m |
| front-user | 32m |
| wealth-gateway | 256m |
| wealth-service | 512m |
| **总计** | **~1.38GB** |

### 6.3 当前 vs 精简后对比

| 维度 | 当前 | 精简后 | 变化 |
|------|------|--------|------|
| Java 进程 | 8 个 | 2 个 | -75% |
| 中间件容器 | 7 个 | 3 个 | -57% |
| 总容器数 | 15 个 | 5-7 个 | -53%~67% |
| 预估内存占用 | ~4.5GB | ~1.3-1.4GB | -69%~71% |
| 内存 vs 3.6GB | 超卖 ~25% | 空闲 ~60% | 从 OOM 到绰绰有余 |

---

## 七、实施步骤

### Phase 1：服务合并（优先级最高）

```
Step 1  创建 wealth-service Maven 模块
        聚合 6 个业务模块的所有依赖到单一 pom.xml
        复制 6 个模块的 src/main/java 到 wealth-service
        合并 6 套 application.yml 为一套（端口 8081）
        合并 6 套 application-prod.yml 为一套
        处理 6 个模块的配置类 Bean 冲突检查

Step 2  调整 Gateway 路由
        路由从 lb://wealth-system 改为 http://wealth-service:8081/system
        去除 Nacos 相关依赖（bootstrap.yml、nacos-discovery、nacos-config）
        JWT 及 management 配置从 Nacos shared-configs 移入本地配置

Step 3  编译验证
        mvn clean install -DskipTests
        确保无编译错误
```

### Phase 2：中间件精简

```
Step 4  从 docker-compose.yml 移除 Prometheus、Grafana、mysql-backup
Step 5  从 docker-compose.yml 移除 Nacos
Step 6  docker-compose.yml 更新为 5 个服务：gateway + service + mysql + redis + nginx
Step 7  更新 nginx.conf，移除 Prometheus/Grafana 相关配置
Step 8  本地 docker-compose up 全链路测试
```

### Phase 3：前端合并（可选）

```
Step 9  将 front/dist 和 front-user/dist 复制到 Nginx 镜像或宿主目录
Step 10 更新 nginx.conf 的静态文件路径
Step 11 移除 front 和 front-user 容器
```

---

## 八、关键风险与应对

| 风险 | 概率 | 应对措施 |
|------|------|---------|
| Spring Bean 命名冲突 | 中 | 合并前 grep 所有 `@Bean`/`@Component`/`@Service` 确认唯一性 |
| MyBatis-Plus Mapper 扫描路径 | 中 | 确保 `@MapperScan` 覆盖全部 6 个模块的 mapper 包 |
| Interceptor 路径配置冲突 | 低 | 验证 6 套 WebMvcConfigurer 的 addPathPatterns 不重叠 |
| Gateway lb:// 改为 http:// 后失效 | 低 | 单实例无需负载均衡，直接路由到固定地址 |
| 前端构建版本不匹配 | 低 | 合并后重新 `npm run build` |
| Nacos 依赖去除后 @RefreshScope 失效 | 中 | 确认无使用动态刷新注解，配置改为启动时加载 |

---

## 九、预期收益总结

| 指标 | 当前 | 精简后 |
|------|------|--------|
| 容器总数 | 15 | 5 |
| 内存占用 | ~4.5GB（OOM） | ~1.3GB（空闲 2.3GB） |
| 启动顺序依赖 | 复杂（nacos→mysql→gateway→6 服务） | 简单（mysql→redis→service→nginx） |
| 配置管理 | Nacos + 7 套 application.yml | 2 套 application.yml |
| 部署运维 | 逐个启动，逐个检查 | 两个 Java 进程 |
| 开发调试 | 需启动 8 个 Java 进程 | 需启动 2 个 Java 进程 |

> 本方案仅描述模块合并与精简的优化思路，不涉及现有代码或配置文件的修改。
> 实现时需按 Phase 1→2→3 的顺序逐步落地，每完成一个 Phase 进行回归测试。
