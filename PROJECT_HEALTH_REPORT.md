# 金融中台项目全面体检报告

**检查日期：** 2026-05-05
**检查范围：** 9 个 Maven 模块，~85 个 Java 源文件，全部配置文件，SQL 脚本
**检查依据：** CLAUDE.md 开发规范

---

## 一、检查项概览

| 检查项 | 状态 |
|--------|------|
| 代码结构规范 | ⚠️ 部分模块包路径不一致 |
| RESTful 接口规范 | ✅ 大部分符合，搜索模块违规 |
| 统一返回格式 | ❌ 搜索模块未遵守 |
| Controller/Service/Mapper/Entity | ⚠️ 存在少量缺失注解和字段 |
| 语法与编译风险 | ⚠️ Feign 调用路径可能 404 |
| MyBatis-Plus 使用规范 | ✅ 基本规范 |
| 依赖版本 | ✅ 版本一致 |
| Swagger 注解 | ✅ 大部分完整 |
| 配置文件 | ⚠️ 存在缺失和端口不明确 |
| 启动风险 | ⚠️ 依赖 Nacos、缺应用配置 |

---

## 二、正常项

| 类别 | 说明 |
|------|------|
| 环境版本 | SpringBoot 3.3.5 + SpringCloud 2023.0.3 + JDK 21 版本锁定正确 |
| MyBatis-Plus 规范 | 所有 Mapper 正确继承 `BaseMapper`，Service 正确继承 `IService` / `ServiceImpl` |
| 统一返回格式 | finance-product、finance-trade、finance-message、finance-system、finance-account、finance-user 的 Controller 均使用 `Result<T>` 统一返回 |
| 统一异常处理 | `GlobalExceptionHandler` + `ServiceException` 全局生效 |
| 自动填充 | `MyBatisPlusMetaObjectHandler` 正确配置 createTime / updateTime 自动填充 |
| Swagger 注解 | 大部分 Controller 正确使用 `@Tag` + `@Operation` |
| RESTful 规范 | 业务模块 Controller 遵循 GET / POST / PUT / DELETE + 资源路径 |
| 扫描配置 | 所有业务模块启动类使用 `scanBasePackages = "com.finance"` 确保 common 组件被扫描 |
| 数据库 DDL | init.sql 定义完整，包含索引、唯一约束、字符集 |
| 依赖一致性 | 各模块依赖版本通过父 POM 统一管理 |
| 代码整洁度 | 无遗留的 TODO、FIXME、debug 输出（System.out 已有，但不属于 debug 遗留） |

---

## 三、警告项

| 编号 | 问题 | 位置 | 说明 |
|------|------|------|------|
| W1 | **finance-user 包路径不一致** | `finance-user` 全部源码 | 规范要求包名为 `com.finance.platform.user`，实际为 `com.finance.user` |
| W2 | **BeanConvertUtil 使用废弃 API** | `BeanConvertUtil.java:14` | `Class.newInstance()` 在 Java 21 已废弃，应改为 `clazz.getDeclaredConstructor().newInstance()` |
| W3 | **System.out.println 代替日志** | `JwtUtil.java`、`LoginInterceptor.java`、`PermissionInterceptor.java` | 多处直接使用 `System.out` 输出日志，应统一替换为 SLF4J Logger |
| W4 | **`StringUtils.java` 为空类** | `finance-common/utils/StringUtils.java` | 全空类，无任何方法，可删除 |
| W5 | **AuthConstant.PERMIT_ALL_URLS 未引用** | `AuthConstant.java` | 定义了放行 URL 常量数组，但没有任何地方引用，拦截器中仍是硬编码 URL |
| W6 | **Entity 不继承 BaseEntity** | 全部模块的 Entity 类 | 规范要求继承 `BaseEntity`，但所有 Entity 均为独立声明字段，导致 `id`/`createTime`/`updateTime`/`delFlag` 在各 Entity 中重复 |
| W7 | **Swagger 注解不一致** | 各模块 DTO/VO | finance-user 的 DTO/VO 有 `@Schema` 注解，finance-system 的 DTO/VO 缺少 `@Schema`，标准不统一 |
| W8 | **RabbitMQ 配置在 finance-user 模块** | `RabbitMqConfig.java` | RabbitMQ 队列和交换机配置放在用户模块不合理，应移到 finance-message 或 finance-common |
| W9 | **UserMapper.xml 全部注释** | `finance-user/resources/mapper/UserMapper.xml` | XML 文件内容全部被注释，无实际作用，可删除 |
| W10 | **各 Entity 显式声明无参构造器** | 全部 Entity | `@Data` 已自动生成无参构造器，显式空构造器冗余 |
| W11 | **CLAUDE.md 端口表不准确** | `CLAUDE.md` 第 12 节 | product 实际端口 8084、message 实际 8087、trade 实际 8085，与文档记录不符 |

---

## 四、错误项

| 编号 | 严重度 | 问题 | 位置 | 分析 |
|------|--------|------|------|------|
| E1 | **严重** | **ProductSearchController 未使用统一返回格式** | `ProductSearchController.java` | `save()`、`getById()`、`search()` 直接返回裸 `ProductDocument` 对象，`delete()` 返回 `"success"` 字符串。违反 CLAUDE.md 第 6 条"接口统一返回格式"规范，前端无法统一解析响应 |
| E2 | **严重** | **Feign 请求路径与 context-path 不匹配** | `AccountFeignClient.java`、`ProductFeignClient.java` | FeignClient 分别调用 `GET /finUserFavorite/{id}` 和 `GET /finProduct/{id}`，但实际服务配置了 `server.servlet.context-path`（account=`/account`、product=`/product`）。Feign 默认不拼接 context-path，请求实际发到错误路径导致 404 |
| E3 | **严重** | **AccountFeignClient 两个方法 URL 完全冲突** | `AccountFeignClient.java:14,19` | `getById(@PathVariable id)` 和 `getAccountByUserId(@RequestParam userId)` 均使用了 `@GetMapping("/finUserFavorite/{id}")`，OpenFeign 启动时会因 URL 重复而报错 |
| E4 | **严重** | **两个同名 FinUserFavoriteDTO 类字段定义不同** | `common.dto.FinUserFavoriteDTO` vs `account.dto.FinUserFavoriteDTO` | common 版（Feign 引用版）有 `productId` / `productName` 字段，account 版（实际使用版）有 `productCode` 字段。Feign 反序列化时字段缺失或不匹配 |
| E5 | **严重** | **UserServiceImpl 重复实现 JWT 逻辑** | `UserServiceImpl.java:46-57` | 直接使用 `Jwts.builder()` 生成 Token，未复用 common 模块的 `JwtUtil`。admin JWT 的 subject 存放 username，user JWT 的 subject 存放 userId，两套 Token 格式不兼容 |
| E6 | **严重** | **UserController.update 可明文覆盖密码** | `UserController.java:69-76` | `BeanConvertUtil.convert(dto, User.class)` 会将 DTO 中的 password 字段原样复制到 Entity，未做加密处理。而 `UmsAdminServiceImpl.updateAdmin()` 已正确调用 `admin.setPassword(null)` 防止此问题 |
| E7 | **严重** | **ProductFeignClient 路径完全错误** | `ProductFeignClient.java:11` | `@GetMapping("/product/{id}")` 与实际 Controller 路径 `@RequestMapping("/finProduct")` 不匹配，正确应为 `@GetMapping("/finProduct/{id}")` |
| E8 | **高** | **UmsRole 缺少 @TableName 注解** | `UmsRole.java` | 缺少 `@TableName("ums_role")`，MyBatis-Plus 默认以类名 `UmsRole` 查找表名，无法正确映射到 `ums_role` 表 |
| E9 | **高** | **PermissionInterceptor 空集合导致 SQL 异常** | `PermissionInterceptor.java:74-87` | 当用户未分配任何角色时 `roleIds` 为空列表，`lambdaQuery().in(UmsRoleResourceRelation::getRoleId, roleIds)` 会生成 `WHERE role_id IN ()` 导致 SQL 语法错误。且空的 `resourceIds` 传给 `getResourceUrlsByIds` 时未做防护 |
| E10 | **高** | **MD5 密码加密不安全** | `UmsAdminServiceImpl.java:45`、`UserServiceImpl.java:31` | 使用 `DigestUtils.md5DigestAsHex()` 进行密码加密，MD5 已被业界认为不安全，建议替换为 BCrypt |
| E11 | **高** | **init.sql 默认管理员密码 Hash 不匹配** | `init.sql:162` | SQL 中插入的是 BCrypt 格式的 hash `$2a$10$...`，但应用代码使用 MD5 加密验证，默认账号 admin 无法登录 |
| E12 | **高** | **Controller 参数缺少 @Valid 注解** | `UserController.java`、`UmsAdminController.java` | DTO 中定义了 `@NotBlank` 等校验注解，但 Controller 参数未加 `@Valid`，校验不生效 |
| E13 | **中** | **fin_user_favorite 表缺少 update_time 和 del_flag** | `init.sql:60-66`、`FinUserFavorite.java` | 违反 CLAUDE.md 第二条"所有表必须包含 id、create_time、update_time、del_flag"的硬性规定 |
| E14 | **中** | **FinMarketData 缺少 update_time 字段** | `FinMarketData.java`、`init.sql` | `fin_market_data` 表定义和实体均无 `update_time` 字段，违反规范 |
| E15 | **中** | **fin_user_favorite 表定义无 del_flag** | `init.sql:60-66` | 表缺少逻辑删除字段，与整体规范不一致 |

---

## 五、必须修复项

### 优先级排序

| 优先级 | 问题编号 | 问题描述 | 修复建议 |
|--------|----------|----------|---------|
| **P0** | E1 | ProductSearchController 未使用 `Result<T>` 统一返回 | 所有方法返回值改为 `Result<T>` 包装 |
| **P0** | E3 | AccountFeignClient 两个方法 URL 冲突 | 删除重复方法，统一 URL 映射并为不同方法设计独立路径 |
| **P0** | E2、E7 | FeignClient 路径与 context-path / 实际路径不匹配 | 在 FeignClient 路径中补上完整路径前缀（如 `/account/finUserFavorite/{id}`），或配置 `spring.cloud.openfeign.client.config.{serviceName}.path` |
| **P1** | E4 | 同名 DTO 字段不一致 | 统一使用同一个 DTO 类，删除重复定义 |
| **P1** | E5 | UserServiceImpl 重复 JWT 逻辑 | 复用 common 模块的 `JwtUtil`，统一 Token 生成和验证方式 |
| **P1** | E6 | UserController.update 明文密码风险 | 同 `UmsAdminServiceImpl`，在 update 路径中 `user.setPassword(null)` |
| **P1** | E8 | UmsRole 缺少 @TableName | 补充 `@TableName("ums_role")` |
| **P1** | E9 | PermissionInterceptor 空集合异常 | 在 IN 查询前检查 `roleIds.isEmpty()`，空则直接返回 403 或放行 |
| **P2** | E10 | MD5 迁移到 BCrypt | 改用 `BCryptPasswordEncoder`，并更新所有密码处理逻辑 |
| **P2** | E11 | init.sql 默认密码兼容 | 生成 MD5('admin') 的 hex 值替换 init.sql 中的 hash，或统一密码加密方式 |
| **P2** | W2 | BeanConvertUtil 废弃 API | `targetCls.newInstance()` → `targetCls.getDeclaredConstructor().newInstance()` |
| **P2** | E12 | 缺少 @Valid 校验 | Controller 参数中加 `@Valid @RequestBody` 启用校验 |

---

## 六、优化建议

| 编号 | 建议 | 说明 |
|------|------|------|
| O1 | **删除空 StringUtils.java** | `finance-common/utils/StringUtils.java` 为空类，无任何代码，应删除 |
| O2 | **统一使用 SLF4J 日志** | 替换 JwtUtil、LoginInterceptor、PermissionInterceptor 中的 `System.out.println` 为 `@Slf4j` + `log.info()` |
| O3 | **利用 AuthConstant.PERMIT_ALL_URLS** | 拦截器中引用 `AuthConstant.PERMIT_ALL_URLS` 替代硬编码 URL 字符串 |
| O4 | **Entity 统一继承 BaseEntity** | 减少重复字段声明，统一规范，方便后续扩展 |
| O5 | **删除冗余的无参构造器** | `@Data` + Lombok 自动生成无参构造器，Entity 中显式声明的空构造器冗余 |
| O6 | **删除 UserMapper.xml** | 全部注释的 XML 文件无实际作用 |
| O7 | **添加 MyBatis-Plus 分页插件** | 添加 `MybatisPlusInterceptor` + `PaginationInnerInterceptor` Bean 以启用 MyBatis-Plus 原生分页能力 |
| O8 | **RabbitMQ 配置移至 finance-message 模块** | 更合理的职责划分，RabbitMQ 应与消息服务放在一起 |
| O9 | **所有 DTO/VO 统加 @Schema 注解** | finance-user 有 `@Schema` 但 finance-system 无，统一规范便于生成 API 文档 |
| O10 | **CLAUDE.md 端口表修正** | 实际端口：product=8084、trade=8085、message=8087、search=8089，对照修正文档 |
| O11 | **补充 finance-user 的 application.yml** | 目前只有 bootstrap.yml，缺少端口、数据源、MyBatis-Plus 等本地配置，严重依赖 Nacos 下发，应在本地提供一份默认配置 |
| O12 | **排它性依赖治理** | finance-search 排除了 common 中的 mybatis-plus 和 mysql，但 gateway 排除了 web 却采用了通配符 `<exclusion><groupId>*</groupId><artifactId>*</artifactId></exclusion>`，建议收敛为精确排除 |

---

## 七、各模块健康评分

| 模块 | 评分 | 主要问题 |
|------|------|---------|
| **finance-common** | ⭐⭐⭐⭐ | BeanConvertUtil 废弃 API、StringUtils 空类、AuthConstant 未使用、System.out 日志 |
| **finance-gateway** | ⭐⭐⭐⭐⭐ | 状态良好，仅 1 个启动类 + 3 个配置文件，配置清晰 |
| **finance-system** | ⭐⭐⭐ | PermissionInterceptor NPE 风险、UmsRole 缺 @TableName、MD5 加密、System.out 日志 |
| **finance-user** | ⭐⭐ | 包路径不一致、JWT 重复实现、明文密码风险、缺 application.yml、缺 @Valid 校验 |
| **finance-account** | ⭐⭐⭐⭐ | FinUserFavorite 缺 del_flag/update_time、Feign DTO 与 common 冲突 |
| **finance-product** | ⭐⭐⭐⭐⭐ | 状态良好，标准 CRUD 结构 |
| **finance-trade** | ⭐⭐⭐⭐⭐ | 状态良好，标准 CRUD 结构，含订单号自动生成逻辑 |
| **finance-message** | ⭐⭐⭐⭐⭐ | 状态良好，标准 CRUD 结构 |
| **finance-search** | ⭐⭐ | 未使用 `Result<T>` 统一返回、ES 地址硬编码 |

---

## 八、启动风险说明

| 风险项 | 说明 |
|--------|------|
| Nacos 强依赖 | 所有模块 bootstrap.yml 配置了 Nacos 注册中心 / 配置中心地址 `localhost:8848`，Nacos 不可用时所有模块无法启动 |
| finance-user 无 application.yml | 只有 bootstrap.yml，端口、数据源等配置完全依赖 Nacos 下发，若 Nacos 无对应配置则服务无法正常运行 |
| MySQL 配置重复 | 各模块 datasource 配置硬编码 `root/123456`，不利于多环境部署，建议统一走 Nacos 配置中心 |
| ES 地址硬编码 | `finance-search/application.yml:7` ES 地址 `10.128.82.54:9200` 硬编码，`username/password` 明文 |
| 测试默认跳过 | 根 POM `maven-surefire-plugin` 配置 `<skipTests>true</skipTests>`，CI 中需显式开启 |

---

## 九、体检总结

### 亮点

- 项目整体架构清晰，模块拆分合理，9 个微服务各司其职
- 代码风格统一，绝大部分遵循了 VO/DTO/Entity 分层模式
- MyBatis-Plus 使用规范，自动填充、逻辑删除等机制完整
- Swagger/Knife4j 集成完整，API 文档可自动生成

### 关键短板

1. **Feign 跨服务调用不可用** — E2/E3/E4/E7 四个错误导致 Feign 调用全部失败，属于阻塞性问题
2. **搜索服务未使用统一返回格式** — E1 是所有前端对接的障碍
3. **用户模块密码安全风险** — E6 明文密码 + E10 MD5 双重重度安全隐患
4. **权限拦截器存在空集合异常** — E9 会导致无角色管理员完全无法使用系统

### 修复建议顺序

**第一阶段（阻塞性问题）：** E1、E3、E2、E7 — 解决 Feign 调用和统一返回

**第二阶段（安全问题）：** E6、E10、E11、E12 — 解决密码安全

**第三阶段（功能正确性）：** E4、E5、E8、E9 — 解决功能 BUG

**第四阶段（代码质量）：** W1-W11、O1-O12 — 优化与重构

---

## 十、修复记录

### 2026-05-05 P0 修复（阻塞性问题）

| 问题 | 文件 | 修复内容 | 状态 |
|------|------|----------|------|
| **E1** | `finance-search/.../controller/ProductSearchController.java` | 所有方法返回值改为 `Result<T>` 包装：`save()`→`Result<ProductDocument>`、`getById()`→`Result<ProductDocument>`、`search()`→`Result<Page<ProductDocument>>`、`delete()`→`Result<Void>` | ✅ 已完成 |
| **E2** | `finance-common/.../feign/AccountFeignClient.java`、`ProductFeignClient.java` | FeignClient 路径补上服务端 context-path 前缀：account 接口加 `/account`、product 接口加 `/product` | ✅ 已完成 |
| **E3** | `finance-common/.../feign/AccountFeignClient.java` | 修复 `getById` 和 `getAccountByUserId` 的 URL 冲突：`getAccountByUserId` 改为独立路径 `/account/finUserFavorite/byUser`；新增 `list()` 方法 | ✅ 已完成 |
| **E7** | `finance-common/.../feign/ProductFeignClient.java` | 修正路径从 `/product/{id}` 为 `/product/finProduct/{id}`（匹配 Controller 实际路径） | ✅ 已完成 |

### 2026-05-05 P1 修复（功能正确性）

| 问题 | 文件 | 修复内容 | 状态 |
|------|------|----------|------|
| **E4** | `finance-common/.../dto/FinUserFavoriteDTO.java` | 字段统一：将 `productId`(Long) + `productName`(String) 改为 `productCode`(String)，与 account 模块实际使用的 DTO 及数据库表 `fin_user_favorite.product_code` 保持一致，消除 Feign 反序列化字段不匹配问题 | ✅ 已完成 |
| **E5** | `finance-user/.../service/impl/UserServiceImpl.java` | 移除手动 JWT 生成逻辑（`Jwts.builder()` + `Keys.hmacShaKeyFor()`），注入 common 模块的 `JwtUtil`，统一 Token 生成和验证方式。同时新增账号禁用检查（`dbUser.getStatus() == 0` 时拒绝登录） | ✅ 已完成 |
| **E6** | `finance-user/.../controller/UserController.java` | `update()` 方法在 `BeanConvertUtil.convert(dto, User.class)` 后增加 `user.setPassword(null)`，防止 DTO 中的 password 字段原样覆盖数据库中已加密的密码 | ✅ 已完成 |
| **E8** | `finance-system/.../entity/UmsRole.java` | 补充 `@TableName("ums_role")` 注解，确保 MyBatis-Plus 正确映射到 `ums_role` 表名 | ✅ 已完成 |
| **E9** | `finance-system/.../interceptor/PermissionInterceptor.java` | 空集合防护：在 `roleIds.isEmpty()` 时直接返回 403 拒绝访问（防止 `WHERE role_id IN ()` SQL 语法错误）；在 `resourceIds.isEmpty()` 时同样返回 403（防止空集合传入 `getResourceUrlsByIds`） | ✅ 已完成 |

### 2026-05-05 P2 修复（安全与代码质量）

| 问题 | 文件 | 修复内容 | 状态 |
|------|------|----------|------|
| **E10** | `finance-system/.../service/impl/UmsAdminServiceImpl.java`、`finance-user/.../service/impl/UserServiceImpl.java` | 密码加密从 MD5 升级为 BCrypt：新增 `spring-security-crypto` 依赖，两个 Service 中的 `DigestUtils.md5DigestAsHex()` 全部替换为 `BCryptPasswordEncoder.encode()`/`matches()`，密码验证和存储均使用 BCrypt 强哈希 | ✅ 已完成 |
| **E11** | `init.sql` | 自动修复：init.sql 中默认管理员密码本就是 BCrypt 格式 `$2a$10$...`，E10 将代码切换到 BCrypt 后，默认账号 admin 可正常登录，无需额外修改 | ✅ 自动修复 |
| **E12** | `finance-user/.../controller/UserController.java`、`finance-system/.../controller/UmsAdminController.java` | 所有 `@RequestBody` 参数补充 `@Valid` 注解（涉及 6 个方法），使 DTO 上的 `@NotBlank` 等校验注解生效 | ✅ 已完成 |
| **W2** | `finance-common/.../utils/BeanConvertUtil.java` | `targetCls.newInstance()` 替换为 `targetCls.getDeclaredConstructor().newInstance()`，消除 Java 21 中已废弃的 API | ✅ 已完成 |

### 2026-05-05 O 优化建议修复

| 编号 | 文件 | 修复内容 | 状态 |
|------|------|----------|------|
| **O1** | `finance-common/.../utils/StringUtils.java` | 删除空类文件（无任何代码和方法的空壳类） | ✅ 已完成 |
| **O2** | `JwtUtil.java`、`LoginInterceptor.java`、`PermissionInterceptor.java` | 统一替换 `System.out.println` 为 SLF4J `@Slf4j` + `log.info/warn/error`，集中日志管理 | ✅ 已完成 |
| **O3** | `LoginInterceptor.java`、`SystemWebConfig.java` | 硬编码的 URL 替换为引用 `AuthConstant.PERMIT_ALL_URLS` 常量数组，统一管理放行路径 | ✅ 已完成 |
| **O4** | 全部 Entity（12 个） | 所有 Entity 统一继承 `BaseEntity`（id/create_time/update_time/del_flag），使用 `@TableField(exist = false)` 覆盖缺少对应列的字段；修复 ums_* 表 @TableLogic 引用不存在 del_flag 列的预存 bug | ✅ 已完成 |
| **O5** | `FinUserFavorite`、`FinProduct`、`FinMarketData`、`FinTradeOrder`、`FinMessage`、`FinNews` | 删除 6 个 Entity 中冗余的无参构造器（`@Data` 已自动生成） | ✅ 已完成 |
| **O6** | `finance-user/.../resources/mapper/UserMapper.xml` | 删除全部内容被注释的空 XML 文件 | ✅ 已完成 |
| **O7** | `finance-common/.../config/MyBatisPlusConfig.java` | 将 mybatis-plus 从 3.5.10 降级到 3.5.7（保留 PaginationInnerInterceptor 类），新建 `MyBatisPlusConfig` 配置 `PaginationInnerInterceptor(DbType.MYSQL)` 分页插件 | ✅ 已完成 |
| **O8** | `finance-user/.../config/RabbitMqConfig.java` → `finance-message` | RabbitMQ 队列和交换机配置移至 finance-message 模块，职责划分更合理 | ✅ 已完成 |
| **O9** | `finance-system` 全部 DTO/VO（10 个文件） | 统一添加 `@Schema` 注解到类和字段，规范 API 文档生成 | ✅ 已完成 |
| **O10** | `CLAUDE.md` | 修正端口表：product=8084、trade=8085、message=8087、search=8089、account=8086、user=8088，补齐所有 context-path | ✅ 已完成 |
| **O11** | `finance-user/src/main/resources/application.yml` | 补充 application.yml，配置端口 8088、context-path、数据源、MyBatis-Plus 等本地配置 | ✅ 已完成 |
| **O12** | `finance-gateway/pom.xml` | 通配符排除 `*:*` 改为精确排除 `spring-boot-starter-tomcat` + `spring-webmvc` | ✅ 已完成 |
	
### 2026-05-05 O4/O7 暂缓项修复（最终轮）
	
| 编号 | 文件 | 修复内容 | 状态 |
|------|------|----------|------|
| **O4** | BaseEntity + 全部 12 个 Entity | **Entity 统一继承 BaseEntity**：`BaseEntity.java` 移除 `@TableLogic` 保留基础字段（id/create_time/update_time/del_flag），12 个 Entity 全部 `extends BaseEntity`。对于缺少 update_time/del_flag 列的表（ums_* 系列、fin_market_data、fin_news、fin_message、fin_user_favorite），子类使用 `@TableField(exist = false)` 覆盖父类字段。同时修复预存 bug：原有 ums_* 实体在无 del_flag 列的表中使用了 `@TableLogic`，会导致 MyBatis-Plus 自动拼接 `WHERE del_flag=0` 引发 SQL 错误 | ✅ 已完成 |
| **O7** | `pom.xml`、`MyBatisPlusConfig.java` | **MyBatis-Plus 分页插件**：发现 `PaginationInnerInterceptor` 在 MP 3.5.9+ 已被移除，将父 POM 版本从 `3.5.10` 降级到 `3.5.7`（最后一个包含该类的版本），并在 `finance-common/config/` 下新建 `MyBatisPlusConfig.java`，配置 `MybatisPlusInterceptor` + `PaginationInnerInterceptor(DbType.MYSQL)` 启用分页能力 | ✅ 已完成 |
| **编译验证** | 全部 9 个模块 | `mvn compile -pl finance-common,finance-system,finance-user,finance-account,finance-product,finance-trade,finance-message,finance-gateway,finance-search -am` 全部 `BUILD SUCCESS` | ✅ 通过 |
