# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

**相关文档：**
- [模块架构与配置体系](docs/ARCHITECTURE.md) — 跨模块开发时引用
- [数据库表结构与字段](docs/DATABASE-SCHEMA.md) — 表结构概述、BaseEntity 继承规则（字段以 init.sql 为准）
- [完整建表 SQL](wealth-common/src/main/resources/sql/init.sql) — **表结构唯一真理，写 Entity 时以此为准**
- [Bug 记录](docs/BUG.md) — 排查已知问题

# 理财服务平台开发规范（自动遵守）

技术栈：SpringBoot 3.3.13 + SpringCloud 2023.0.6 + Spring Cloud Alibaba 2023.0.3.4 + MyBatis-Plus 3.5.9 + MySQL 8 + Redis 5 + ES 8.8.2 + JWT (jjwt 0.12.6) + Knife4j 4.5.0 + Swagger + Nginx + Micrometer Tracing + Zipkin + Prometheus + Grafana + Sentinel

前端：Vue 3.5.13 + Vite 6.3.1 + Element Plus 2.9.7 + Pinia 2.3.1 + TypeScript 5.7

数据库：wealth（utf8mb4）

环境版本、基础设施类说明等引用式内容见 [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)。

# 一、代码结构规范（强制）

包结构必须如下：
com.wealth.platform.领域名

```
controller    # 接口层
service       # 业务层
mapper        # DAO层
entity        # 数据库实体
vo            # 返回前端
dto           # 接收参数
config        # 配置
constant      # 常量
interceptor   # 拦截器
```

# 二、MyBatis-Plus 规范

## Mapper / Service 规范

1. mapper 继承 BaseMapper
2. service 继承 IService / ServiceImpl
3. 禁止手写复杂SQL

## 分页插件配置

`wealth-common` 模块的 `com.wealth.common.config.MyBatisPlusConfig` 已全局配置分页插件：

```java
@Configuration
@ConditionalOnClass(name = "com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor")
public class MyBatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
```

各业务域无需重复配置，引入 wealth-common 依赖后自动生效。

> **实体类规范（BaseEntity 继承、字段映射规则）见 [数据库表结构与字段](docs/DATABASE-SCHEMA.md#三baseentity-继承规范)**

# 三、接口统一返回格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

错误响应格式：
```json
{
  "code": 400,
  "message": "参数不合法",
  "data": null
}
```

状态码：
200 成功
400 参数错误
401 未登录
403 无权限
404 资源不存在
500 服务器异常

# 四、命名规范（强制）

类名：大驼峰
方法名：小驼峰
变量名：小驼峰
常量：大写+下划线
表名：小写+下划线
字段名：小写+下划线

# 五、业务模块对应关系（必须遵守）

所有业务域均在 `wealth-service` 模块内，包路径为 `com.wealth.platform.{domain}`：

| 表前缀 | 业务 | domain |
|--------|------|--------|
| sys_user | 用户管理 | user |
| wea_product | 产品管理 | product |
| wea_market_data | 行情实时数据 | product |
| wea_user_favorite | 用户自选 | product |
| wea_trade_order | 交易委托 | trade |
| wea_news | 资讯 | message |
| wea_message | 消息推送 | message |
| ums_* | 后台权限 | system |

# 六、AI 生成规则

1. 必须严格按照 [init.sql](wealth-common/src/main/resources/sql/init.sql) 中的列名生成 Entity 字段映射（`@TableField("列名")`），**列名以 init.sql 为准**，不对 DATABASE-SCHEMA.md 中的列名做任何信任
2. 必须使用 MyBatis-Plus
3. Entity 必须继承 BaseEntity，按照 [数据库表结构与字段 > BaseEntity 继承规范](docs/DATABASE-SCHEMA.md#三baseentity-继承规范) 处理字段覆盖
4. 必须自动填充 create_time、update_time
5. 接口必须遵循 RESTful 规范
6. 必须加 Swagger 注解
7. 必须加注释
8. 必须符合项目技术栈
9. 不允许生成不存在的表或字段
10. 生成代码必须能直接运行
11. **写操作（增删改）必须加 `@Transactional(rollbackFor = Exception.class)`**
12. **所有 `@RequestBody` DTO 必须加 `@Valid` 参数校验注解**
13. **新增接口须确认是否需要加入权限白名单 `AuthConstant.PERMIT_ALL_URLS`**

# 七、禁止行为

- 禁止修改表结构
- 禁止使用不兼容的依赖版本
- 禁止乱命名
- 禁止 Controller 写业务逻辑
- 禁止 hardcode 密码/IP
- 禁止无注释

# 八、开发常用命令

```bash
# 1. 编译公共模块（必须先执行，修改 common 后要重新 install）
mvn clean install -pl wealth-common -DskipTests

# 2. 全量编译
mvn clean compile

# 3. 安装所有模块到本地仓库
mvn clean install -DskipTests

# 4. 运行网关
mvn spring-boot:run -pl wealth-gateway

# 5. 运行业务服务
mvn spring-boot:run -pl wealth-service

# 6. 打包
mvn clean package -DskipTests

# 7. 运行测试（根 pom.xml 默认跳过测试，需显式开启）
mvn test -pl wealth-service -DskipTests=false

# 8. 运行指定测试类
mvn test -pl wealth-service -Dtest=UmsAdminServiceImplTest -DskipTests=false
```

# 九、单元测试 / 集成测试规范

## 框架

- 测试框架：JUnit 5 + Mockito
- Controller 层：MockMvc
- Service 层：Mockito mock 依赖，只测当前类逻辑
- Mapper 层：`@MybatisPlusTest` 或 `@SpringBootTest`（需要数据库环境）

## 规范

1. 测试类放在 `src/test/java`，包路径与被测类一致
2. 测试类命名：`{被测类名}Test.java`（如 `UserServiceTest.java`）
3. 方法命名：`{方法名}_should_{预期行为}`（如 `login_should_return_token_when_password_correct`）
4. 每个 Service 方法至少有一个正向用例
5. 分支逻辑必须覆盖异常路径（参数非法、资源不存在、状态冲突等）
6. Controller 测试用 MockMvc 验证 HTTP 状态码和返回 JSON 结构

## 运行命令

```bash
# 运行全部测试
mvn test -pl wealth-service -DskipTests=false

# 运行指定测试类
mvn test -pl wealth-service -Dtest=UmsAdminServiceImplTest -DskipTests=false
```

---

# 十、常见代码模式

## Entity → VO 转换

Controller 返回前必须将 Entity 转为 VO，使用 BeanConvertUtil：

```java
UmsAdmin admin = umsAdminService.getById(id);
return Result.success(BeanConvertUtil.convert(admin, UmsAdminVO.class));
```

## 批量转换

```java
List<UmsAdmin> list = umsAdminService.lambdaQuery().eq(UmsAdmin::getStatus, 1).list();
return Result.success(BeanConvertUtil.convertList(list, UmsAdminVO.class));
```

> 注意：禁止直接使用 `service.list()` 全表查询，必须带条件或分页，避免 OOM。

## 分页查询（含 VO 转换）

```java
Page<UmsAdmin> page = new Page<>(pageNum, pageSize);
IPage<UmsAdminVO> voPage = BeanConvertUtil.convertPage(umsAdminService.page(page), UmsAdminVO.class);
return Result.success(voPage);
```

## update 方法 null 安全更新

避免 DTO 中 null 字段覆盖数据库已有值，使用 copyNonNullProperties：

```java
public boolean updateOrder(Long id, WeaTradeOrderDTO dto) {
    WeaTradeOrder order = getById(id);
    if (order == null) return false;
    BeanConvertUtil.copyNonNullProperties(dto, order);
    order.setId(id);
    return updateById(order);
}
```

## 业务异常抛出

使用 ServiceException 携带业务状态码：

```java
throw new ServiceException(401, "密码错误");
throw new ServiceException(400, "参数不合法");
```

全局异常处理器会提取 code 和 message 返回给前端。

## Contract 接口（替代 Feign）

跨模块调用使用 contract 接口（定义在 wealth-common），业务模块内直接注入实现：

```java
// wealth-common/src/main/java/com/wealth/common/contract/ 中定义接口
public interface ProductService {
    Result<ProductDTO> getById(Long id);
}

// wealth-service 中直接注入
@Autowired
private ProductService productService;
```

## JWT 登录流程

1. UmsAdminController.login() 接收 LoginDTO（username + password）
2. UmsAdminService.login() 验证密码，返回 JWT token
3. 后续请求通过 LoginInterceptor 拦截校验

## Swagger 注解规范

```java
@RestController
@RequestMapping("/{domain}/action")
@Tag(name = "模块名管理")
public class XxxController {
    @PostMapping
    @Operation(summary = "操作描述")
    public Result<XxxVO> method(@RequestBody @Valid XxxDTO dto) { ... }
}
```

## 重复创建检查

```java
long count = lambdaQuery()
        .eq(WeaUserFavorite::getUserId, dto.getUserId())
        .eq(WeaUserFavorite::getProductCode, dto.getProductCode())
        .count();
if (count > 0) {
    return false;  // 或 throw new ServiceException(400, "记录已存在")
}
```

> 注意：该模式存在并发竞争条件（TOCTOU），两次请求同时检查返回 0 后都会执行插入。**必须配合数据库唯一索引**或使用 `INSERT ... ON DUPLICATE KEY UPDATE` 作为最终防线。

# 十一、项目健康检查规则（强制遵守）

**每次执行项目健康检查、错误扫描、启动异常排查时，必须优先查阅 [Bug.md](docs/BUG.md) 中的已知问题记录。**

## 配置修改审批原则

当前项目的所有配置文件（application.yml、application-prod.yml、pom.xml）默认 **锁定状态**，如需修改必须：
1. **询问用户并得到明确同意**
2. **给出强烈的修改理由**（说明为什么非改不可、不修改的后果）
3. **确认修改不破坏其他模块**

如遇启动问题，优先排查已知问题，确认无解后方可申请修改配置。

## JWT 配置

```yaml
jwt:
  secret: ${JWT_SECRET}
  expire: ${JWT_EXPIRE:604800000}
```

JWT 密钥通过 `.env` 或环境变量注入。JwtUtil 在 @PostConstruct 中校验密钥字节≥32，启动时即失败而非运行时。

## 启动验证清单

每次启动逐项确认：

- [ ] Docker 容器全部运行：`docker ps`（mysql、redis、nginx）
- [ ] MySQL 运行且 wealth 库存在
- [ ] `.env` 文件已配置（根目录 + wealth-gateway/.env + wealth-service/.env）
- [ ] 全量编译：`mvn clean install -DskipTests`（如 common 有变更，先单独 `-pl wealth-common`）
- [ ] 启动顺序：gateway → wealth-service
- [ ] wealth-service HikariPool 启动成功（日志中搜索 "HikariPool-1 - Start completed"）
- [ ] 各服务 /actuator/prometheus 端点可访问（验证监控指标暴露）
- [ ] 前端 `npm install && npx vite` 可正常访问
- [ ] 冒烟测试：`curl -s localhost:8080/system/umsAdmin/login -X POST -H "Content-Type: application/json" -d '{"username":"admin","password":"admin123"}'` 返回 JWT token

## 扫描检查清单（每次排查逐项过）

- [ ] 全量编译：mvn clean compile
- [ ] 所有实体类 `@EqualsAndHashCode(callSuper=true)` 或 `@Getter @Setter` + callSuper=true
- [ ] 所有写操作 `@Transactional(rollbackFor = Exception.class)`
- [ ] 所有 `@RequestBody` DTO 有 `@Valid`
- [ ] getById 返回 null 时返回 404
- [ ] list() 接口有分页
- [ ] 新增接口须同时更新 `AuthConstant.PERMIT_ALL_URLS`
- [ ] 拦截器 pathPatterns 与 context-path 一致（不能加前缀）
- [ ] update 方法使用 `BeanConvertUtil.copyNonNullProperties` 而非 `BeanUtils.copyProperties`
- [ ] 所有 `application.yml` 使用 `spring.data.redis.*` 而非 `spring.redis.*`
- [ ] 业务异常使用 `ServiceException(code, message)` 而非 RuntimeException
- [ ] 链路追踪配置使用 `management.zipkin.tracing.endpoint` 而非 `zipkin.base-url`
- [ ] Entity 字段映射与 init.sql 逐列核对：`@TableField("列名")` 必须与建表语句的列名完全一致，不信任 DATABASE-SCHEMA.md

# 十二、历史教训（必须遵守）

以下是从实际错误中总结的规则，每次修复/改动前逐条过一遍。

| # | 规则 | 说明 |
|---|------|------|
| 1 | **文档链检查** | 先确认文档与本地项目/数据库是否同步。文档链接指向的文件可能已过期，追踪到最底层数据源（init.sql、数据库、代码）再动手 |
| 2 | **批量改一个先验证** | 批量修改多个文件时，先改一个、验证通过、再批量改其余。批量操作的系统性错误（要么全对要么全错）需要逐项过滤 |
| 3 | **改完主动提验证** | 涉及数据库映射、数据流、配置的修改，改完后主动提议启动项目验证，不等用户发现问题 |
| 4 | **三方一致原则** | 当"修复"涉及改大量现有代码时，先确认文档、代码、数据库三方一致，找到真正不一致的源头，而不是默认代码是错的 |
| 5 | **跨域切换先读文件** | 从前端切后端、从 A 模块切 B 模块时，先读关键文件再改，不凭记忆直接写。跨域切换需要重新建立上下文 |

# 十三、Git 提交规范（强制遵守）

所有 git 提交必须遵循 [约定式提交 (Conventional Commits)](https://www.conventionalcommits.org/) 规范，格式如下：

```
<type>(<scope>): <description>
```

## type 类型

| type | 说明 |
|------|------|
| feat | 新功能 |
| fix | 修复 bug |
| docs | 文档变更 |
| style | 代码格式调整（不影响功能） |
| refactor | 代码重构（既非 feat 也非 fix） |
| perf | 性能优化 |
| test | 添加或修改测试 |
| chore | 构建过程或辅助工具变动 |
| ci | CI 配置变更 |
| build | 影响构建系统或外部依赖的变更 |
| revert | 回退提交 |

## scope 可选范围

本项目的 scope：common、gateway、service。

## description 要求

- 简洁的命令式语气（如 "添加用户登录接口" 而非 "添加了用户登录接口"）
- 首字母小写
- 末尾不加句号

## 示例

```
feat(service): 添加产品分页查询接口
fix(service): 修复交易委托金额计算精度问题
docs: 更新 README 部署说明
refactor(gateway): 提取 CORS 配置为独立类
chore: 升级 MyBatis-Plus 依赖版本
ci: 添加 GitHub Actions 自动构建
```

> 注：scope 可省略，type 不可省略。提交信息中英文均可，本项目统一使用中文描述。
