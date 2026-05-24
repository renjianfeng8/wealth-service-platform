# 代码质量清理设计（第二批）

**日期:** 2026-05-24
**状态:** 已审批

## 修复项

### 1. 空目录清理

删除 5 个无文件空目录：

| 目录 | 操作 |
|------|------|
| `wealth-service/.../message/config/` | `git rm -r` |
| `wealth-service/.../message/mq/` | `git rm -r` |
| `wealth-service/.../product/config/` | `git rm -r` |
| `wealth-service/.../search/config/` | `git rm -r` |
| `wealth-service/.../trade/config/` | `git rm -r` |

### 2. 删除重复 WeaProduct 实体

`search/entity/WeaProduct.java` 和 `search/mapper/WeaProductMapper.java` 与 product 模块功能相同，删除并改用 product 模块的类。

**删除文件：**
- `wealth-service/.../search/entity/WeaProduct.java`
- `wealth-service/.../search/mapper/WeaProductMapper.java`

**修改文件：**
- `wealth-service/.../search/service/impl/ProductSearchServiceImpl.java` — import 路径从 `com.wealth.platform.search.entity.WeaProduct` 改为 `com.wealth.platform.product.entity.WeaProduct`；mapper 改为 `FinProductMapper`

### 3. 移除无用 OpenFeign 依赖

Feign 自环调用已全部替换为 contract 接口，OpenFeign 不再被代码引用。

**修改文件：**
- `wealth-common/pom.xml` — 删除 `spring-cloud-starter-openfeign`
- `wealth-service/pom.xml` — 删除 `spring-cloud-starter-openfeign`

### 4. 硬编码值外移（3 处，Page 1000 不动）

| 文件 | 当前值 | 改为 |
|------|--------|------|
| `MarketDataSimulationService.java` | `@Scheduled(fixedDelay = 2000)` | `@Scheduled(fixedDelayString = "${market.simulation.interval:2000}")` |
| `ProductSyncServiceImpl.java` | `@Scheduled(fixedRate = 120000)` | `@Scheduled(fixedRateString = "${product.sync.interval:120000}")` |
| `MarketDataPushService.java` | `new SseEmitter(86400_000L)` | `@Value("${market.sse.timeout:86400000}") private Long sseTimeout;` + `new SseEmitter(sseTimeout)` |

**改动文件：**
- `wealth-service/.../product/service/MarketDataSimulationService.java`
- `wealth-service/.../product/service/impl/ProductSyncServiceImpl.java`
- `wealth-service/.../product/service/MarketDataPushService.java`
- `wealth-service/src/main/resources/application.yml` — 追加 3 个配置项

## 改动文件清单

| 操作 | 文件 |
|------|------|
| 删除 | 5 个空目录 |
| 删除 | `search/entity/WeaProduct.java` |
| 删除 | `search/mapper/WeaProductMapper.java` |
| 修改 | `ProductSearchServiceImpl.java`（import 改路径） |
| 修改 | `wealth-common/pom.xml`（删 openfeign） |
| 修改 | `wealth-service/pom.xml`（删 openfeign） |
| 修改 | `MarketDataSimulationService.java`（外移 fixedDelay） |
| 修改 | `ProductSyncServiceImpl.java`（外移 fixedRate） |
| 修改 | `MarketDataPushService.java`（外移 SSE timeout） |
| 修改 | `application.yml`（追加 3 个配置项） |

## 不包含的内容

- Page 1000 硬编码（5 处，保持现状）
- Sentinel 规则持久化（留待后续）
