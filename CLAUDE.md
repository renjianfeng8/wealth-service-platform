# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

**相关文档：**
- [模块架构与配置体系](docs/architecture.md) — 跨模块开发时引用
- [数据库表结构与字段](docs/database-schema.md) — 写实体类时引用
- [Bug 记录](docs/Bug.md) — 排查已知问题

# 理财服务平台开发规范（自动遵守）

技术栈：SpringBoot 3.3.5 + SpringCloud 2023.0.3 + Spring Cloud Alibaba 2023.0.1.2 + MyBatis-Plus 3.5.7 + MySQL 8 + Redis 5 + RabbitMQ 3.10 + ES 8.8.2 + JWT (jjwt 0.11.5) + Knife4j 4.4.0 + Swagger + Nginx + Micrometer Tracing + Zipkin + Prometheus + Grafana + Sentinel + Seata

前端：Vue 3.5.13 + Vite 6.3.1 + Element Plus 2.9.7 + Pinia 2.3.1 + TypeScript 5.7

数据库：wealth（utf8mb4）

环境版本、基础设施类说明等引用式内容见 [docs/architecture.md](docs/architecture.md)。

# 一、代码结构规范（强制）

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

# 二、MyBatis-Plus 规范

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

# 三、接口统一返回格式

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

# 四、命名规范（强制）

类名：大驼峰
方法名：小驼峰
变量名：小驼峰
常量：大写+下划线
表名：小写+下划线
字段名：小写+下划线

# 五、业务模块对应关系（必须遵守）

sys_user          → 用户管理
wea_product       → 产品管理
wea_market_data   → 行情实时数据
wea_user_favorite → 用户自选
wea_trade_order   → 交易委托
wea_news          → 资讯
wea_message       → 消息推送
ums_*             → 后台权限

# 六、AI 生成规则

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

# 九、常见代码模式

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

# 十、项目健康检查规则（强制遵守）

**每次执行项目健康检查、错误扫描、启动异常排查时，必须优先查阅 [Bug.md](docs/Bug.md) 中的已知问题记录。**

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

# 十一、Git 提交规范（强制遵守）

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
