# 更新日志

## v1.1.0 (2026-05-08)

### 全栈健康检查与修复

#### 基础设施
- 父 pom 移除全局 `spring-boot-starter-web`，避免 gateway 与 WebFlux 冲突
- 移除根 pom `knife4j-openapi3-spring-boot-starter`（导致 gateway WebFlux 冲突）
- 7 个业务模块 pom.xml 添加 `spring-boot-maven-plugin`，修复 JAR 无启动清单问题
- gateway 添加 `spring-cloud-starter-loadbalancer` 依赖
- `BeanConvertUtil` 保留原始异常链（`e` 传入 RuntimeException）

#### 网关 (finance-gateway)
- 添加 7 条服务路由规则（system/user/product/account/trade/message/search）
- 修复全局 CORS 配置 `allow-credentials: true`
- 网关 application.yml 配置优化

#### 搜索模块 (finance-search)
- `FinanceSearchApplication` 添加 `scanBasePackages = "com.finance"`，修复 JwtUtil Bean 未找到
- `MyBatisPlusConfig`、`MyBatisPlusMetaObjectHandler` 添加 `@ConditionalOnClass`，使未引入 MyBatis-Plus 的模块能正常启动
- `ProductSearchController.getById()` 空值返回 404 而非 null
- Nacos 配置 `finance-search` 中 ES 地址 `10.128.82.54:9200` 改为 `localhost:9200`

#### 后端核心修复
- **拦截器**: `SystemWebConfig` 路径模式修复、`LoginInterceptor` 适配 `AntPathMatcher`
- **JWT**: `JwtUtil` 添加默认值、修复空指针
- **事务**: 14 个 Service 方法添加 `@Transactional(rollbackFor = Exception.class)`（update/delete 操作）
- **参数校验**: 11 个 DTO 添加 `jakarta.validation` 注解（@NotBlank/@NotNull），4 个 System Controller 添加 `@Valid`
- **实体**: 12 个 Entity 类添加 `@EqualsAndHashCode(callSuper = true)`
- **Feign**: `AccountFeignClient` 路径从 `/account/finUserFavorite/list` 修正为 `/account/finUserFavorite`
- **ProductFeignClient** 补充泛型参数
- **LoginDTO 转换**: `UserController.login()` 直接传递 LoginDTO，不再转为 User 实体
- **分页**: 6 个业务模块 Controller 新增 `/page` 分页查询接口
- **全局异常**: `GlobalExceptionHandler` 添加 `MethodArgumentNotValidException` 校验异常处理
- **UmsAdminController** create/update 改为接收 `UmsAdminDTO`
- **NULL 处理**: 12 个 Controller getById 空值返回 404

#### 前端 (Vue3 + Element Plus)
- **构建**: `vue-tsc -b` 改为 `vue-tsc --noEmit`，修复 TypeScript 类型声明错误
- **路由**: 添加 RouteMeta 类型声明，新增 404 通配路由，登录保留 redirect 参数
- **表格**: 所有列表页添加 `empty-text="暂无数据"`、`handleSizeChange()` 分页重置
- **删除**: 所有 delete 操作添加 try-catch 异常捕获
- **校验**: user 页面手机号正则验证、邮箱格式验证、编辑时密码非必填
- **搜索页**: price 列添加 formatPrice 格式化
- **登录页**: 登录成功后跳转至 `route.query.redirect || '/'`

### 配置安全
- application.yml 数据库密码改为环境变量 `${DB_PASSWORD:123456}`
- ES 地址改为环境变量 `${ES_URIS:http://localhost:9200}`
- Nacos 配置中心统一管理业务配置，本地仅保留基础框架配置

### 项目规范
- CLAUDE.md 新增第十五节「项目健康检查规则」，记录配置管理原则、端口表、已知高频问题、扫描检查清单
- memory 记录 health-check-skill 预设规则

---

## v1.0.0 (2026-05-04)
- 初始化项目，搭建基于 SpringCloud Alibaba 的微服务架构
- 集成 Nacos 注册中心与配置中心
- 完成用户、账户、产品、交易等核心模块开发
- 集成 Spring Cloud Gateway 网关
- 实现 JWT 无状态认证
- 集成 Elasticsearch 全文检索服务

### 项目结构
- finance-mid-platform (pom)
  - finance-common — 公共依赖模块
  - finance-gateway — 网关服务
  - finance-system — 系统服务（后台权限管理）
  - finance-user — 用户服务
  - finance-account — 账户服务
  - finance-product — 产品服务
  - finance-trade — 交易服务
  - finance-message — 消息服务
  - finance-search — 搜索服务（ES）
