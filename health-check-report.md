# 全项目代码质量扫描报告

**扫描日期**: 2026-05-09
**扫描范围**: 全部 8 个后端模块 + 前端 34 个文件 + 全部配置文件
**总计发现问题**: 101 个（按严重级别：HIGH 19, MEDIUM 41, LOW 41）

---

## 一、严重问题汇总（按模块）

### 1.1 后端—finance-system（18 个问题）

| # | 严重度 | 文件 | 行号 | 问题描述 |
|---|--------|------|------|---------|
| S-1 | **HIGH** | UmsAdminServiceImpl | 47 | `admin.getPassword()` 可能为 null，导致 `BCryptPasswordEncoder.matches()` 抛出 IAE |
| S-2 | **HIGH** | UmsAdminRoleRelationService/Impl | 13/19 | `getRoleIdByAdminId()` 定义并实现但从未被调用 |
| S-3 | **HIGH** | UmsRoleResourceRelationService/Impl | 13/19 | `getResourceIdByRoleIds()` 定义并实现但从未被调用 |
| S-4 | **HIGH** | UmsResourceService/Impl | 12/17 | `getUrlByResourceIds()` 定义并实现但从未被调用 |
| S-5 | **HIGH** | UmsAdminServiceImpl | 36,44,48 | login() 抛出 `RuntimeException`，被全局异常捕获为 500，应返回 401 |
| S-6 | MEDIUM | SystemWebConfig | 3 | 未使用的导入 `AuthConstant` |
| S-7 | MEDIUM | AuthConstant/SystemWebConfig | 7/24-30 | PERMIT_ALL_URLS 常量路径含 `/system` 前缀但 WebConfig 使用去掉前缀的路径 |
| S-8 | ~~MEDIUM~~ | PermissionInterceptor | 多处 | 中文编码问题 **✅ 已修复** |
| S-9 | MEDIUM | UmsAdminDTO/UmsAdminServiceImpl | 18/64 | password 在 DTO 中 `@NotBlank` 但在 update 中被置 null，API 设计矛盾 |
| S-10 | MEDIUM | UmsAdminServiceImpl | 35-37 | login() 中 `StringUtils.hasText()` 检查为死代码（`@Valid` 已拦截） |
| S-11 | MEDIUM | PermissionInterceptor | 65-111 | 每次请求 4 次数据库查询，无缓存 |
| S-12 | LOW | UmsRoleServiceImpl | 10-12 | 空 service 实现类 |
| S-13 | LOW | 5 个 entity 类 | 16-17 | `@Getter` `@Setter` 与父类 `@Data` 重复 |
| S-14 | LOW | 多个 Controller | 多处 | 缺少 `@Transactional` |
| S-15 | LOW | PermissionInterceptor | 67 | 冗余 `.eq(UmsAdmin::getDelFlag, 0)`（`@TableLogic` 已自动添加） |
| S-16 | LOW | UmsAdmin.java, UmsRole.java | 多处 | `@TableField` 使用风格不一致 |
| S-17 | LOW | PermissionInterceptor | 48-119 | response 处理健壮性不足 |

### 1.2 后端—finance-user / finance-product / finance-account（21 个问题）

| # | 严重度 | 模块 | 文件 | 行号 | 问题描述 |
|---|--------|------|------|------|---------|
| U-1 | **HIGH** | user | User.java | 34 | `delFlag` 字段在 BaseEntity 和子类中重复声明，内存双份 |
| U-2 | **HIGH** | common | JwtUtil.java | 31 | JWT secret key 长度无检查，短于 32 字符运行时崩溃 |
| U-3 | MEDIUM | user | UserController.java | 77 | update 端口的 `@Valid` 要求 password 但立即置 null |
| U-4 | LOW | user | UserController.java | 77 | 依赖 MyBatis-Plus 默认 `FieldStrategy.NOT_NULL` 保护密码 |
| U-5 | MEDIUM | user | WebConfig/UserServiceImpl | 23-33 | JWT 路径/设计问题 |
| U-6 | LOW | user | UserController.java | 93-98 | register() 接受可控的 id 字段 |
| U-7 | MEDIUM | user | UserServiceImpl.java | 57 | JWT 只含 username，不含 userId |
| P-1 | **HIGH** | product | FinProduct/FinMarketData | 48/51-56 | `delFlag` 字段重复声明 |
| P-2 | **HIGH** | product | FinMarketData.java | 50-52 | `updateTime` 字段冲突 |
| P-3 | MEDIUM | product | FinProduct/FinMarketDataServiceImpl | 51-53 | update 查不到实体静默返回 false |
| P-4 | LOW | product | FinProductServiceImpl | 42-46 | 缺少 `productName` 的 `@NotBlank` 校验 |
| P-5 | LOW | product | FinProduct.java | 22-44 | `@TableField` 全部冗余 |
| A-1 | **HIGH** | account | FinUserFavorite.java | 28-33 | `delFlag`/`updateTime` 字段重复声明 |
| A-2 | **HIGH** | account/common | FinUserFavoriteDTO | 两处 | `FinUserFavoriteDTO` 在两个模块中重复定义 |
| A-3 | MEDIUM | account | FinUserFavoriteServiceImpl | 51-53 | update 查不到实体静默返回 false |
| A-4 | MEDIUM | account | FinUserFavoriteController | 113-117 | 物理删除文档注释需确认 |
| A-5 | MEDIUM | account | FinUserFavoriteServiceImpl | 42-47 | create 未检查重复关注 |
| C-1 | MEDIUM | common | ProductFeignClient | 13 | 返回类型为 `Result<?>` 擦除类型 |
| C-2 | LOW | product/account | pom.xml | 多处 | 缺少显式 openfeign 依赖声明 |
| C-3 | **HIGH** | all three | application.yml | 多处 | 缺少 `jwt.secret`/`jwt.expire` 本地开发默认值 |
| C-4 | LOW | common | BeanConvertUtil | 21 | 不可变 List **✅ 已修复** |
| C-5 | MEDIUM | user | UserServiceImpl.java | 41-43 | 逻辑删除用户仍可登录风险 |

### 1.3 后端—finance-trade / finance-message / finance-search（18 个问题）

| # | 严重度 | 模块 | 文件 | 行号 | 问题描述 |
|---|--------|------|------|------|---------|
| T-1 | **HIGH** | trade | FinTradeOrderServiceImpl | 65 | BeanUtils.copyProperties 覆盖 null 值到实体 |
| T-2 | MEDIUM | trade | FinTradeOrderServiceImpl | 34 | list() 无分页/排序 |
| T-3 | LOW | trade | FinTradeOrderServiceImpl | 72-74 | delete 未找到时静默返回 false |
| T-4 | LOW | trade | FinTradeOrderController | 73-78 | 分页代码冗余可抽取 |
| M-1 | **HIGH** | message | FinNewsServiceImpl | 56 | BeanUtils.copyProperties 覆盖 null 值 |
| M-2 | **HIGH** | message | FinMessageServiceImpl | 56 | BeanUtils.copyProperties 覆盖 null 值 |
| M-3 | MEDIUM | message | RabbitMqConfig | 全部 | 声明了 Queue/Exchange 但无消费者 |
| M-4 | MEDIUM | message | FinMessage/FinNewsServiceImpl | 33 | list() 无分页/排序 |
| M-5 | LOW | message | FinMessageDTO | 17 | msgType 缺少 @NotNull 校验 |
| M-6 | LOW | message | FinMessage/FinNews.java | 38-42 | updateTime 覆盖冲突 |
| E-1 | **HIGH** | search | ProductDocument | 29,33,36 | BigDecimal 使用 FieldType.Double 精度丢失 |
| E-2 | **HIGH** | search | pom.xml | 19-38 | 未排除 `spring-boot-starter-amqp` 传递依赖 |
| E-3 | MEDIUM | search | ProductRepository | 11 | Containing 在 Keyword 字段上生成慢速通配符查询 |
| E-4 | MEDIUM | search | ProductSearchServiceImpl | 36 | deleteById 未处理文档不存在的异常 |
| E-5 | MEDIUM | search | ProductSearchController | 23 | POST 直接接受 ProductDocument 实体 |
| E-6 | MEDIUM | common | ProductFeignClient | 12 | Result<?> 擦除类型 |
| E-7 | MEDIUM | trade/message/search | WebConfig | 25-30 | 缺少 `/actuator/**`/`/health` 排除路径 |
| E-8 | LOW | trade/message | Controller | 多处 | update 返回 200+false 语义模糊 |

### 1.4 前端（18 个问题）

| # | 严重度 | 文件 | 行号 | 问题描述 |
|---|--------|------|------|---------|
| F-1 | **HIGH** | search/index.vue | 31 | `description` 列不存在 **✅ 已修复** |
| F-2 | **HIGH** | product/index.vue | 76 | 缺少 `price` 前端校验 **✅ 已修复** |
| F-3 | ~~MEDIUM~~ | product/index.vue | 67 | 未使用的导入 `getProductList` **✅ 已修复** |
| F-4 | ~~MEDIUM~~ | market/index.vue | 64 | 未使用的导入 `getMarketDataList` **✅ 已修复** |
| F-5 | ~~MEDIUM~~ | product/index.vue | 107-108 | 未使用的 CSS `.table-action-bar` **✅ 已修复** |
| F-6 | ~~MEDIUM~~ | dashboard/index.vue | 73 | 未使用的 CSS `.dashboard-section-title` **✅ 已修复** |
| F-7 | MEDIUM | store/index.ts | 5-8 | `UserInfo` 接口定义但未使用 |
| F-8 | MEDIUM | router/index.ts | 89-93 | 404 路由指向登录页 |
| F-9 | MEDIUM | 多个视图 | 多处 | 表单缺少 required 校验规则 |
| F-10 | MEDIUM | 所有视图 | handleEdit | `Object.assign(form, row)` 传播 unexpected 字段 |
| F-11 | MEDIUM | user/index.vue | 137 | 编辑时清空密码可能误覆盖 |
| F-12 | MEDIUM | 所有视图 | fetchData | API 失败无错误状态 |
| F-13 | MEDIUM | 所有视图 | catch | 空 catch 块 |
| F-14 | MEDIUM | 多处 | 多处 | 广泛使用 `any` 类型 |
| F-15 | LOW | 多处视图 | template | 内联箭头函数造成重复渲染 |
| F-16 | LOW | API 模块 | 多处 | 未使用的 API 导出函数 |
| F-17 | LOW | utils/auth.ts | 4-6 | localStorage 存储 JWT |
| F-18 | LOW | login/index.vue | 224-227 | 密码可见性切换 |

### 1.5 配置文件（16 个问题）

| # | 严重度 | 文件 | 行号 | 问题描述 |
|---|--------|------|------|---------|
| CF-1 | ~~HIGH~~ | finance-search application.yml | 8 | ES_PASSWORD 无默认值 **✅ 已修复** |
| CF-2 | MEDIUM | finance-search pom.xml | 20-38 | 未排除 Redis/RabbitMQ 传递依赖 |
| CF-3 | MEDIUM | pom.xml | 29 | `mybatis-spring` 版本不一致 |
| CF-4 | MEDIUM | pom.xml | 150 | `skipTests=true` 跳过所有测试 |
| CF-5 | MEDIUM | front/package.json | 22-26 | 缺少 `@types/node` |
| CF-6 | LOW | finance-common pom.xml | 72-88 | jjwt 依赖重复 |
| CF-7 | LOW | finance-common pom.xml | 35-38 | Nacos 依赖重复 |
| CF-8 | LOW | finance-common pom.xml | 98-101 | Lombok 依赖重复 |
| CF-9 | LOW | finance-user pom.xml | 38 | 冗余 scope |
| CF-10 | LOW | pom.xml | 30 | jjwt 版本过旧 |
| CF-11 | LOW | pom.xml | 106-128 | 不必要的根 POM 依赖 |
| CF-12 | ~~LOW~~ | finance-search application.yml | 18 | `concat`→`contact` **✅ 已修复** |
| CF-13 | LOW | finance-system application.yml | 3 | sql.init.mode 不一致 |
| CF-14 | LOW | 项目根 | 缺失 | 无 docker-compose.yml |
| CF-15 | LOW | front/package.json | 缺失 | 缺少 engines 字段 |
| CF-16 | LOW | front/vite.config.ts | 18 | 代理重写可能产生空路径 |

---

## 二、已自动修复项（安全优化，不改变业务逻辑）

| # | 修复内容 | 文件 | 修改说明 |
|---|---------|------|---------|
| 1 | 设置 charset=UTF-8 和 flush | PermissionInterceptor.java | 6处中文错误输出添加编码 + 刷新 |
| 2 | BeanConvertUtil 返回可变 List | BeanConvertUtil.java | `List.of()` → `new ArrayList<>()` |
| 3 | ES_PASSWORD 添加空默认值 | finance-search application.yml | `${ES_PASSWORD}` → `${ES_PASSWORD:}` |
| 4 | 修复 Knife4j 配置拼写 | finance-search application.yml | `concat` → `contact` |
| 5 | 移除未使用的导入 | product/index.vue | 移除 `getProductList` |
| 6 | 移除未使用的导入 | market/index.vue | 移除 `getMarketDataList` |
| 7 | 移除未使用的 CSS | product/index.vue | 移除 `.table-action-bar` |
| 8 | 移除未使用的 CSS | dashboard/index.vue | 移除 `.dashboard-section-title` |
| 9 | 添加 price 表单校验 | product/index.vue | `rules` 添加 `price: [{ required: true }]` |
| 10 | 移除不存在的 desc 列 | search/index.vue | 移除 `description` 列（ProductDocument 无此字段） |

---

## 三、推荐修复优先级

### P0 — 运行时崩溃（需立即修复）
- [ ] U-2: JWT Secret 长度校验
- [ ] T-1/M-1/M-2: BeanUtils.copyProperties null 覆盖（3处）
- [ ] E-1: ES 价格字段 Double → Scaled_Float
- [ ] E-2/CF-2: search 模块排除 AMQP
- [ ] S-1: 登录空指针风险

### P1 — 业务逻辑错误
- [ ] S-5: login() RuntimeException → ServiceException(401)
- [ ] U-1/P-1/P-2/A-1: 实体字段重复声明修复
- [ ] A-5: 重复关注检查
- [ ] C-3: 本地开发 JWT 默认值
- [ ] S-2/S-3/S-4: 删除或重构死方法

### P2 — 代码清理和防御
- [ ] F-8: 404 路由指向专用组件
- [ ] CF-4: 重新启用测试
- [ ] F-12/F-13: 错误状态和 catch 处理
- [ ] E-7: 添加 `/actuator/**` 排除路径

### P3 — 长期质量提升
- [ ] S-11: 添加权限缓存
- [ ] F-14: 消除 `any` 类型
- [ ] F-16: 清理未使用的 API 导出
- [ ] CF-14: docker-compose.yml
