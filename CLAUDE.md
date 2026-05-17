# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

**相关文档：**
- [模块架构与配置体系](docs/architecture.md) — 跨模块开发时引用
- [数据库表结构与字段](docs/database-schema.md) — 写实体类时引用
- [Bug 记录](Bug.md) — 排查已知问题

# 理财服务平台开发规范（自动遵守）

技术栈：SpringBoot 3.3.5 + SpringCloud 2023.0.3 + Spring Cloud Alibaba 2023.0.1.2 + MyBatis-Plus 3.5.7 + MySQL 8 + Redis 5 + RabbitMQ 3.10 + ES 8.8.2 + JWT (jjwt 0.11.5) + Knife4j 4.4.0 + Swagger + Nginx + Micrometer Tracing + Zipkin + Prometheus + Grafana + Sentinel + Seata

前端：Vue 3.5.13 + Vite 6.3.1 + Element Plus 2.9.7 + Pinia 2.3.1 + TypeScript 5.7

数据库：wealth（utf8mb4）

# 一、环境版本（严格锁定）

JDK: 21.0.3
Maven: 3.9.9
MySQL: 8.0.37
Redis: 5.0.14.1
RabbitMQ: 3.10.20
ElasticSearch: 8.8.2
Sentinel Dashboard: 1.8.6 (bladex/sentinel-dashboard:latest)
Seata: 2.0.0 (seataio/seata-server:2.0.0)
Zipkin: openzipkin/zipkin:latest
Prometheus: prom/prometheus:latest
Grafana: grafana/grafana:latest
Docker: 29.4.0
docker-compose: v5.11.0

SpringBoot: 3.3.5
SpringCloud: 2023.0.3
Spring Cloud Alibaba: 2023.0.1.2
MyBatis-Plus: 3.5.7    # 最后一个包含 PaginationInnerInterceptor 的稳定版本（3.5.9+ 已移除）
mybatis-spring: 3.0.4  # wealth-system 模块覆盖为 3.0.5
jjwt: 0.11.5
Knife4j: 4.4.0

前端:
  Vue: 3.5.13
  Vite: 6.3.1
  Element Plus: 2.9.7
  Pinia: 2.3.1
  Vue Router: 4.5.0
  Axios: 1.7.9
  TypeScript: 5.7
  vue-tsc: 2.2.8

# 二、代码结构规范（强制）

包结构必须如下：
com.wealth.platform.模块名

controller    # 接口层
service       # 业务层
mapper        # DAO层
entity        # 数据库实体
vo            # 返回前端
dto           # 接收参数
config        # 配置
util          # 工具
constant      # 常量
exception     # 异常
common        # 公共

# 三、MyBatis-Plus 规范

## Mapper / Service 规范

1. mapper 继承 BaseMapper
2. service 继承 IService / ServiceImpl
3. 禁止手写复杂SQL

## 分页插件配置

`wealth-common` 模块的 `com.wealth.common.config.MyBatisPlusConfig` 已全局配置分页插件（标注 `@ConditionalOnClass`，未引入 MyBatis-Plus 的模块如 wealth-search 不会因此启动失败）：

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

各业务模块无需重复配置，引入 wealth-common 依赖后自动生效。

> **实体类规范（BaseEntity 继承、字段映射规则）见 [数据库表结构与字段](docs/database-schema.md#三baseentity-继承规范)**

# 四、接口统一返回格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

状态码：
200 成功
400 参数错误
401 未登录
403 无权限
404 资源不存在
500 服务器异常

# 五、命名规范（强制）

类名：大驼峰
方法名：小驼峰
变量名：小驼峰
常量：大写+下划线
表名：小写+下划线
字段名：小写+下划线

# 六、业务模块对应关系（必须遵守）

sys_user          → 用户管理
wea_product       → 产品管理
wea_market_data   → 行情实时数据
wea_user_favorite → 用户自选
wea_trade_order   → 交易委托
wea_news          → 资讯
wea_message       → 消息推送
ums_*             → 后台权限

# 七、AI 生成规则

1. 必须严格按照 [数据库表结构与字段](docs/database-schema.md) 中的表结构生成 Entity（继承 BaseEntity）、Mapper、Service、Controller、Vo、Dto
2. 必须使用 MyBatis-Plus
3. Entity 必须继承 BaseEntity，按照 [数据库表结构与字段 > BaseEntity 继承规范](docs/database-schema.md#三baseentity-继承规范) 处理字段覆盖
4. 必须自动填充 create_time、update_time
5. 接口必须遵循 RESTful 规范
6. 必须加 Swagger 注解
7. 必须加注释
8. 必须符合项目技术栈
9. 不允许生成不存在的表或字段
10. 生成代码必须能直接运行

# 八、禁止行为

- 禁止修改表结构
- 禁止使用不兼容的依赖版本
- 禁止乱命名
- 禁止 Controller 写业务逻辑
- 禁止 hardcode 密码/IP
- 禁止无注释

# 九、开发常用命令

```bash
# 1. 编译公共模块（必须先执行，修改 common 后要重新 install）
mvn clean install -pl wealth-common -DskipTests

# 2. 编译所有模块
mvn clean compile

# 3. 安装所有模块到本地仓库
mvn clean install -DskipTests

# 4. 运行单个模块（示例：系统服务）
mvn spring-boot:run -pl wealth-system

# 5. 打包
mvn clean package -DskipTests

# 6. 运行打包后的 jar（示例）
java -jar wealth-system/target/wealth-system-1.0.0.jar

# 7. 运行测试（根 pom.xml 默认跳过测试，需显式开启）
mvn test -pl wealth-common -DskipTests=false
```

# 十、核心基础设施类说明

| 类 | 路径 | 用途 |
|----|------|------|
| BaseEntity | wealth-common/entity/BaseEntity.java | 所有实体的基类（id, createTime, updateTime, delFlag + @TableLogic） |
| Result\<T\> | wealth-common/result/Result.java | 统一 API 返回封装（code + message + data） |
| ResultCode | wealth-common/result/ResultCode.java | 状态码枚举 |
| BeanConvertUtil | wealth-common/utils/BeanConvertUtil.java | Entity→VO 转换 + 新增 `copyNonNullProperties` 用于 null 安全更新 |
| JwtUtil | wealth-common/utils/JwtUtil.java | JWT Token 生成与验证，含 @PostConstruct 密钥长度校验（≥32字节） |
| ServiceException | wealth-common/exception/ServiceException.java | 业务异常，携带 int code 字段（全局异常处理器据此返回对应状态码） |
| GlobalExceptionHandler | wealth-common/exception/GlobalExceptionHandler.java | 全局异常处理（ServiceException / @Valid 校验失败 / 通用异常） |
| MyBatisPlusConfig | wealth-common/config/MyBatisPlusConfig.java | MyBatis-Plus 分页插件配置（@ConditionalOnClass 安全加载） |
| MyBatisPlusMetaObjectHandler | wealth-common/config/MyBatisPlusMetaObjectHandler.java | 自动填充 createTime/updateTime |
| LoginInterceptor | wealth-common/interceptor/LoginInterceptor.java | 用户模块登录拦截器（AntPathMatcher 匹配放行路径） |
| PermissionInterceptor | wealth-system/interceptor/PermissionInterceptor.java | 后台 RBAC 权限拦截器（数据库驱动：admin→角色→资源→URL） |
| SystemWebConfig | wealth-system/config/SystemWebConfig.java | 注册 PermissionInterceptor（排除 /umsAdmin/login、/doc.html、/webjars/**、/swagger-resources/**、/v3/api-docs/**） |
| RedisUtil | wealth-common/utils/RedisUtil.java | Redis 操作工具类 |
| AuthConstant | wealth-common/constants/AuthConstant.java | 权限相关常量 |
| JwtAuthGlobalFilter | wealth-gateway/filter/JwtAuthGlobalFilter.java | Gateway 全局 JWT 鉴权过滤器（HMAC-SHA256，白名单放行） |
| SentinelConfig | wealth-trade/config/SentinelConfig.java | 交易模块 Sentinel 限流规则（POST 下单接口 QPS=100） |
| SentinelGatewayConfig | wealth-gateway/config/SentinelGatewayConfig.java | 网关 Sentinel 路由限流（7 条路由规则，50-100 QPS） |
| RabbitMqConfig | wealth-common/config/RabbitMqConfig.java | 双 DLX/DLQ 队列、Publisher Confirm、3 次重试 |
| FeignConfig | wealth-common/config/FeignConfig.java | Feign 全局超时（connect=5s, read=10s）+ 3 次重试 |

# 十一、常见代码模式

## Entity → VO 转换

Controller 返回前必须将 Entity 转为 VO，使用 BeanConvertUtil：

```java
UmsAdmin admin = umsAdminService.getById(id);
return Result.success(BeanConvertUtil.convert(admin, UmsAdminVO.class));
```

## 批量转换

```java
List<UmsAdmin> list = umsAdminService.list();
return Result.success(BeanConvertUtil.convertList(list, UmsAdminVO.class));
```

## 分页查询（含 VO 转换）

```java
Page<UmsAdmin> page = new Page<>(pageNum, pageSize);
IPage<UmsAdmin> userPage = umsAdminService.page(page);
Page<UmsAdminVO> voPage = new Page<>();
voPage.setCurrent(userPage.getCurrent());
voPage.setSize(userPage.getSize());
voPage.setTotal(userPage.getTotal());
voPage.setPages(userPage.getPages());
voPage.setRecords(BeanConvertUtil.convertList(userPage.getRecords(), UmsAdminVO.class));
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

## Feign 跨服务调用

FeignClient 定义在 wealth-common 中，各模块引入依赖后即可调用：

```java
@FeignClient("wealth-account")
public interface AccountFeignClient {
    // 注意：路径必须包含服务端 context-path 前缀 /account
    @GetMapping("/account/weaUserFavorite/{id}")
    Result<WeaUserFavoriteDTO> getById(@PathVariable("id") Long id);
}
```

## JWT 登录流程

1. UmsAdminController.login() 接收 LoginDTO（username + password）
2. UmsAdminService.login() 验证密码，返回 JWT token
3. 后续请求通过 LoginInterceptor 拦截校验

## Swagger 注解规范

```java
@RestController
@RequestMapping("/path")
@Tag(name = "模块名管理")
public class XxxController {
    @PostMapping("/action")
    @Operation(summary = "操作描述")
    public Result<XxxVO> method(@RequestBody XxxDTO dto) { ... }
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

# 十二、项目健康检查规则（强制遵守）

**每次执行项目健康检查、错误扫描、启动异常排查时，必须优先查阅 [Bug.md](Bug.md) 中的已知问题记录。**

## 配置不可修改原则

当前项目的所有配置文件（application.yml、bootstrap.yml、pom.xml、Nacos 配置）均为 **锁定状态**，任何情况下不得修改。
如遇启动问题，优先排查以下已知问题，而非修改配置。

## JWT 配置（固定值，必须存在于 Nacos）

```yaml
jwt:
  secret: wealth-micro-service-20260501-very-safe-secret-key-123456789
  expire: 604800000
```

JwtUtil 在 @PostConstruct 中校验密钥字节≥32，启动时即失败而非运行时。

## 已知高频问题（启动排查速查）

1. **JWT 配置缺失** → 检查 Nacos `wealth-shared.yaml` 配置是否已发布，而非本地配置缺失。Nacos 地址 `localhost:8848`。密钥至少 32 字节。
2. **SystemWebConfig 拦截器不生效** → addPathPatterns 保持了 context-path 剥离后的 `/**`。excludePathPatterns 也是剥离 context-path 后的路径。
3. **LoginInterceptor 放行路径缺失** → `AuthConstant.PERMIT_ALL_URLS` 仅含 `/system/umsAdmin/login`，缺少 user 模块等路径。如需新增模块登录放行，须更新 `AuthConstant.java` 而非配置文件。`AntPathMatcher` 已用于路径匹配。
4. **wealth-user 全部接口 401** → user 模块无 `LoginInterceptor` 注册（无 `WebMvcConfigurer`），但 `AuthConstant.PERMIT_ALL_URLS` 中也未包含 user 登录路径。v1.4.0 已知问题，需通过 `AuthConstant.java` 添加 `/user/user/login` 修复。
5. **UmsResource.delFlag 查询条件被忽略** → 早期版本 delFlag 标注了 @TableField(exist = false)，现已统一继承 BaseEntity。
6. **网关启动异常（端口冲突）** → 确认 8080 端口未被占用。父 pom 的 spring-boot-starter-web 与 Gateway WebFlux 冲突的已在依赖层面解决。
7. **PermissionInterceptor 每次请求 4 次 DB 查询** → 当前无缓存，生产环境建议添加 Redis 缓存优化。
8. **wealth-search 启动失败（NoClassDefFoundError: RedisSerializer）** → v1.4.0 `RedisConfig.java` / `RedisUtil.java` 缺少 `@ConditionalOnClass` 条件注解。search 模块无 Redis 依赖，需在 `RedisConfig` 和 `RedisUtil` 类级别添加 `@ConditionalOnClass(name = "org.springframework.data.redis.core.RedisTemplate")`。这是 Java 源码问题，非配置问题。
9. **Zipkin 无 Span 数据** → 检查 Nacos `wealth-shared.yaml` 中配置的是 `management.zipkin.tracing.endpoint` 而非旧版 `zipkin.base-url`。Spring Boot 3.x + Micrometer Tracing 须使用新属性。见 [Bug-009](Bug.md#bug-001-es-搜索报-conversionexception日期格式不匹配)。

## 启动验证清单

每次启动逐项确认：

- [ ] Docker 容器全部运行：`docker ps`（nacos、mysql、redis、rabbitmq、es、nginx、zipkin、prometheus、grafana、sentinel、seata）
- [ ] MySQL 运行且 wealth 库存在
- [ ] Nacos 配置 `wealth-shared.yaml` 已发布且内容完整（JWT + 数据源 + management.tracing + management.endpoints.prometheus）
- [ ] 全量编译：`mvn clean install -DskipTests`（如 common 有变更，先单独 `-pl wealth-common`）
- [ ] 按顺序启动：gateway → system → user → product → account → trade → message → search
- [ ] 各服务 HikariPool 启动成功（日志中搜索 "HikariPool-1 - Start completed"）
- [ ] 各服务 /actuator/prometheus 端点可访问（验证监控指标暴露）
- [ ] 前端 `npm install && npx vite` 可正常访问

## 扫描检查清单（每次排查逐项过）

- [ ] 全量编译：mvn clean compile
- [ ] 所有实体类 @EqualsAndHashCode(callSuper=true) 或 @Getter @Setter + callSuper=true
- [ ] 所有写操作 @Transactional(rollbackFor = Exception.class)
- [ ] 所有 @RequestBody DTO 有 @Valid
- [ ] getById 返回 null 时返回 404
- [ ] list() 接口有分页
- [ ] 禁止修改 application.yml、bootstrap.yml、Nacos 配置
- [ ] 新增模块须同时更新 `AuthConstant.PERMIT_ALL_URLS`
- [ ] 拦截器 pathPatterns 与 context-path 一致（不能加前缀）
- [ ] update 方法使用 BeanConvertUtil.copyNonNullProperties 而非 BeanUtils.copyProperties
- [ ] 业务异常使用 ServiceException(code, message) 而非 RuntimeException
- [ ] 链路追踪配置使用 `management.zipkin.tracing.endpoint` 而非 `zipkin.base-url`
- [ ] 新增模块须引入 `micrometer-tracing-bridge-brave` + `zipkin-sender-okhttp3` 依赖
- [ ] 新增模块须引入 `micrometer-registry-prometheus` 依赖（暴露 /actuator/prometheus）

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

本项目的 scope 对应模块名：common、gateway、system、user、product、account、trade、message、search。

## description 要求

- 简洁的命令式语气（如 "添加用户登录接口" 而非 "添加了用户登录接口"）
- 首字母小写
- 末尾不加句号

## 示例

```
feat(product): 添加产品分页查询接口
fix(trade): 修复交易委托金额计算精度问题
docs: 更新 README 部署说明
refactor(gateway): 提取 CORS 配置为独立类
chore: 升级 MyBatis-Plus 依赖版本
ci: 添加 GitHub Actions 自动构建
```

> 注：scope 可省略，type 不可省略。提交信息中英文均可，本项目统一使用中文描述。
