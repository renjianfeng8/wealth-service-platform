# 模块合并残留清理设计

**日期:** 2026-05-24
**状态:** 已审批

## 清理项

### 1. 移除 @EnableDiscoveryClient

**文件:** `wealth-gateway/src/main/java/com/wealth/gateway/WealthGatewayApplication.java:8`
**操作:** 删除该注解。Nacos 已从项目中完全移除，该注解在无发现客户端时无意义。

### 2. 统一包路径 com.wealth.user → com.wealth.platform.user

**原因:** 其他所有业务模块 (system, product, trade, message, search) 都在 `com.wealth.platform.*` 下，唯独用户模块在 `com.wealth.user`，属于历史迁移遗留。

**改动清单:**
- 移动 `wealth-service/src/main/java/com/wealth/user/` 全部内容到 `com/wealth/platform/user/`
- 更新所有 Java 文件的 `package` 声明
- 更新 `WealthServiceApplication.java` 的 `@SpringBootApplication(scanBasePackages = ...)` 和 `@MapperScan(...)` — 去掉独立的 `com.wealth.user` 扫描（`com.wealth.platform` 已覆盖）
- 更新 `application.yml` 中 MyBatis-Plus 的 `type-aliases-package` 和 `mapper-locations`

### 3. Feign 自环调用 → 直接方法调用

**原因:** 模块合并后所有服务在同一 JVM，Feign 打到 `localhost:8081` 是自己调自己，增加 HTTP 开销并导致 Bug-001（Feign 调用无 JWT 返回 500）。

**方式:** 在 `wealth-common` 中定义纯 Java 接口（无 Spring 注解），在 `wealth-service` 中实现，调用方 `@Autowired` 注入。

#### 3.1 MessageFeignClient → MessageService 接口

- **调用方:** `FinTradeOrderServiceImpl.createOrder()`
- **接口名:** `com.wealth.common.contract.MessageService`
- **方法:** `void createMessage(MessageFeignDTO dto)` — 注意当前不返回 Result，需要调整
- **实现:** `com.wealth.platform.message.service.impl.MessageServiceImpl` 实现该接口

#### 3.2 PermissionCheckFeignClient → SystemService 接口

- **调用方:** `PermissionCheckInterceptor`
- **接口名:** `com.wealth.common.contract.SystemService`
- **方法:** `boolean checkPermission(Long userId, String url)`
- **实现:** `com.wealth.platform.system.service.impl.UmsAdminServiceImpl` 实现该接口

#### 3.3 ProductFeignClient → ProductService 接口

- **调用方:** 需要在代码中搜索 `@Autowired ProductFeignClient`
- **接口名:** `com.wealth.common.contract.ProductService`
- **方法:** `WeaProduct getById(Long id)`
- **实现:** `com.wealth.platform.product.service.impl.WeaProductServiceImpl`

#### 3.4 ProductSyncFeignClient → SearchService 接口

- **调用方:** `ProductSyncServiceImpl`
- **接口名:** `com.wealth.common.contract.SearchService`
- **方法:** 同步到 ES 的方法
- **实现:** `com.wealth.platform.search.service.impl.ProductSyncServiceImpl`

**删除文件:**
- `wealth-common/.../feign/MessageFeignClient.java`
- `wealth-common/.../feign/MessageFeignClientFallback.java`
- `wealth-common/.../feign/PermissionCheckFeignClient.java`
- `wealth-common/.../feign/ProductFeignClient.java`
- `wealth-common/.../feign/ProductSyncFeignClient.java`
- `wealth-common/.../feign/FeignConfig.java`

### 4. 清理过时注释和文档

- `JwtUtil.java:19` — 删除 `需在 Nacos 共享配置中定义` 注释
- `docs/nacos-config-reference.md` — 删除文件
- `scripts/nacos_check.py` — 删除文件

## 检查清单

执行完成后逐项确认:
- [ ] gateway 编译无错
- [ ] common 编译无错
- [ ] service 编译无错（全量 `mvn clean compile`）
- [ ] 用户 CRUD 接口正常（UserController 路由不变）
- [ ] 交易委托创建不再返回 500（Bug-001 修复）
- [ ] 权限检查拦截器正常
- [ ] 100 个 E2E 测试全通过
