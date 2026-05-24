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

---

## 十、方案审查报告（2026-05-24）

> 基于项目当前代码结构的深度审查结论。

### 10.1 可行性评估

**总体评分：6.5/10**。方案技术上可行，但实施复杂度被严重低估。最大风险点不是 Bean 冲突而是 **6 层 WebMvcConfigurer 拦截器互相叠加** 和 **Feign 自引用循环**，这两个问题会导致功能直接不可用。

### 10.2 已确认无风险的判断

| 原方案判断 | 审查结论 |
|-----------|---------|
| Spring Bean 命名冲突概率中 | 实际各模块 Bean 命名空间隔离良好，冲突概率 **低** |
| @RefreshScope 失效 | grep 确认项目无任何 `@RefreshScope` 使用 |
| Mapper 扫描路径 | MyBatis-Plus 统一配置在 wealth-common 中 |
| 前端构建版本 | 独立部署不受影响 |

### 10.3 审查发现的关键风险（按严重程度排序）

#### 🔴 致命风险

**R1. 6 层拦截器叠加（严重）**

5 个业务模块的 `WebConfig` + 1 个 `SystemWebConfig` 都实现 `WebMvcConfigurer` 并注册 `addPathPatterns("/**")`。当前各模块独立部署互不影响，合并到同一 JVM 后每个请求将触发：
- 5 次 `LoginInterceptor` JWT 校验（冗余）
- 5 次 `PermissionCheckInterceptor` Feign 自调用权限校验（循环）
- 1 次 `PermissionInterceptor` 系统权限校验

后果：API 响应时间增加 5-10 倍，且 Feign 自调用在限流/超时下会异常熔断。

**R2. Feign 自引用循环（严重）**

合并后 4 个 FeignClient 变为同进程 HTTP 自调用：

| FeignClient | 调用方 | 合并后影响 |
|-------------|--------|-----------|
| `PermissionCheckFeignClient` → system | 5 个模块的 WebConfig | 循环调用自身，拦截器→Feign→Controller→拦截器 |
| `MessageFeignClient` → message | trade 的 `FinTradeOrderServiceImpl` | 本地方言调用，增加序列化开销 |
| `ProductSyncFeignClient` → search | product 的 `ProductSyncServiceImpl` | 同进程调用 |
| `ProductFeignClient` → product | 其他模块 | 同进程调用 |

#### 🟠 高风险

**R3. 所有 Controller @RequestMapping 需加模块前缀**

12 个 Controller 的路径注解需全部修改，无 context-path 后用户路径必须显式携带模块前缀：

| Controller 文件 | 当前路径 | 合并后路径 |
|----------------|---------|-----------|
| system/UmsAdminController | `/umsAdmin` | `/system/umsAdmin` |
| system/UmsRoleController | `/umsRole` | `/system/umsRole` |
| system/UmsResourceController | `/umsResource` | `/system/umsResource` |
| system/UmsAdminRoleRelationController | `/umsAdminRoleRelation` | `/system/umsAdminRoleRelation` |
| system/UmsRoleResourceRelationController | `/umsRoleResourceRelation` | `/system/umsRoleResourceRelation` |
| user/UserController | `@RequestMapping`(空) + `@GetMapping("/{id}")` | `/user/{id}`（需显式指定） |
| product/ProductController | `/wea-product` | `/product/wea-product` |
| product/MarketDataController | `/wea-market-data` | `/product/wea-market-data` |
| product/UserFavoriteController | `/wea-user-favorite` | `/product/wea-user-favorite` |
| trade/TradeOrderController | `/wea-trade-order` | `/trade/wea-trade-order` |
| message/MessageController | `/wea-message` | `/message/wea-message` |
| message/NewsController | `/wea-news` | `/message/wea-news` |
| search/ProductSearchController | `/product` | `/search/product` |

**R4. SentinelConfig PostConstruct 规则互相覆盖**

`wealth-trade` 和 `wealth-message` 各有一个 `SentinelConfig`，都在 `@PostConstruct` 中调用 `FlowRuleManager.loadRules()`。后初始化的配置会完全覆盖前者的规则，导致：
- trade 限流规则丢失
- 或 message 限流规则丢失

**R5. @GlobalTransactional 依赖 Nacos**

`FinTradeOrderServiceImpl.createOrder()` 标注 `@GlobalTransactional`，Seata TM 通过 Nacos 注册 TC 客户端。Nacos 移除后：
- Seata 启动报错（无法发现 TC）
- AT 事务退化为本地事务

**R6. Gateway lb:// → http:// 路由需要调整**

当前 Gateway 路由 `lb://wealth-system` 依靠目标服务自身的 `context-path: /system` 处理路径前缀。改为 `http://wealth-service:8081` 后：
- 如果保持 Controller 加前缀方案，Gateway 无需 StripPrefix（路径 `/system/umsAdmin/login` 完整转发）
- 但 Knife4j 的 `service-name` 模式（依赖 Nacos 发现）不可用，需改为 `url` 模式

**R7. 搜索模块依赖 exclusion 冲突**

`wealth-search/pom.xml` 排除了 `mybatis-plus`、`spring-boot-starter-data-redis`、`mysql-connector-j`。合并到 wealth-service 后这些排除必须去除，但其他模块需要这些依赖。

#### 🟡 中风险

**R8. 数据库连接池过载**

原 6 个模块各一个 HikariCP 池（默认 `maximum-pool-size=10`），共 60 连接。合并后单池 10 连接在高并发时排队。

**R9. Redis 连接池过载**

原 6 个模块各一个 Lettuce 池（`max-active=8`），共 48 连接。合并后单池 8 连接被各模块争抢。

**R10. SSE 长连接占用 Tomcat 线程**

`/wea-market-data/sse/**` 在 Tomcat 线程模型中每个连接占用一个 worker 线程。合并后 SSE 连接会抢占业务 API 的线程资源。

**R11. Docker healthcheck 路径失效**

原各模块 healthcheck 使用 `context-path + /actuator/health`：
- `wget localhost:8082/system/actuator/health`
- `wget localhost:8083/user/actuator/health`
合并后统一为 `localhost:8081/actuator/health`，需更新 docker-compose。

**R12. JWT secret 从 Nacos 移入 Git 版本管理**

当前 JWT secret 在 Nacos `wealth-shared.yaml` 中。去除 Nacos 后需内联到 `application-prod.yml`，secret 进入 Git 版本管理，增加泄露风险。

### 10.4 修正建议汇总

| 编号 | 问题 | 修正方案 | 工作量 |
|------|------|---------|-------|
| R1 | 拦截器叠加 | 合并为一个 `ServiceWebConfig`，按路径前缀分流 | 2天 |
| R2 | Feign 自引用 | `PermissionCheckFeignClient` 改为直接 Service 注入；其余 Feign 改为本地 Service 调用或保留 HTTP 但加 `url` 属性 | 2天 |
| R3 | Controller 路径 | 12 个 Controller 加模块前缀 | 1天 |
| R4 | Sentinel 规则覆盖 | 合并为一个 `UnifiedSentinelConfig` 同时加载两条规则 | 0.5天 |
| R5 | Seata 依赖 Nacos | 二选一：移除 `@GlobalTransactional` / Seata 改为 file 注册 | 0.5天 |
| R6 | Gateway 路由 | 改为静态 `http://wealth-service:8081`，Knife4j 改为 `url` 模式 | 1天 |
| R7 | Search exclusion | 合并后 pom.xml 中去除 exclusion | 0.5天 |
| R8 | 连接池过载 | HikariCP max=30, Redis max-active=32, Tomcat max=400 | 0.5天 |
| R9 | Redis 连接池 | 同上 | — |
| R10 | SSE 线程占用 | 改用 WebFlux 或 DeferredResult + 独立线程池 | 1天 |
| R11 | Healthcheck 路径 | docker-compose 中统一为 `/actuator/health` | 0.5天 |
| R12 | JWT secret 泄露 | 保持使用环境变量注入，不从 Git 读取 | 0.5天 |

### 10.5 替代方案建议

在实施大合并前，建议先尝试 **轻量化方案**：

1. 保持现有 8 模块架构
2. docker-compose 中各业务模块 `mem_limit` 降至 128m（实测 idle 约 80MB/模块）
3. 移除 Prometheus/Grafana/Nacos/mysql-backup（省 ~608MB）
4. 保留 `lb://` 路由和 Nacos（仅 reduction，不做架构变更）

此方案改动量小一个数量级，预期内存从 4.5GB → ~2.3GB（64% 空闲），大概率解决 OOM，且风险可控。

### 10.6 简化执行清单（合并路线）

```
Phase 0 - 前置验证
  [ ] 先尝试轻量化方案（降内存 + 去 Nacos/Grafana/Prometheus）
  [ ] 如已解决问题则终止合并

Phase 1 - 基础设施准备
  [ ] 统一拦截器 ServiceWebConfig（修复 R1）
  [ ] FeignClient 改造为本地 Service 注入（修复 R2）
  [ ] 合并 SentinelConfig（修复 R4）
  [ ] Seata 处理（修复 R5）

Phase 2 - 模块合并
  [ ] 12 个 Controller 加路径前缀（修复 R3）
  [ ] 创建 wealth-service 聚合模块
  [ ] 合并 6 套配置为 2 套 + 调优连接池（修复 R6 R8 R9）
  [ ] 处理 Search exclusion（修复 R7）
  [ ] SSE 线程池分离（修复 R10）
  [ ] 更新 Healthcheck（修复 R11）

Phase 3 - 集成验证
  [ ] 全量编译通过
  [ ] 各模块路径回归测试（curl 全路径）
  [ ] Nginx → Gateway → Service 全链路测试
  [ ] SSE / 鉴权 / 下单流程测试
  [ ] 压测 P99 < 500ms

Phase 4 - 上线部署
  [ ] 构建 docker 镜像
  [ ] 更新 docker-compose（移除 10+ 容器）
  [ ] 灰度上线，观察 24h
```
