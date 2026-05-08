# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# ==============================================

# 金融项目开发规范（自动遵守）

# 技术栈：SpringBoot3 + SpringCloud + MyBatis-Plus + MySQL8 + Redis + ES + RabbitMQ + JWT + Swagger + Nginx

# 数据库：finance（utf8mb4）

# ==============================================

# ======================

# 一、环境版本（严格锁定）

# ======================

JDK: 21.0.3
Maven: 3.9.9
MySQL: 8.0.37
Redis: 5.0.14.1
RabbitMQ: 3.10.20
ElasticSearch: 8.11.0
Docker: 29.4.0
docker-compose: v5.11.0
SpringBoot: 3.3.x
SpringCloud: 2023.0.x
MyBatis-Plus: 3.5.7    # 最后一个包含 PaginationInnerInterceptor 的稳定版本（3.5.9+ 已移除该拦截器类）

# ======================

# 二、数据库规范（必须严格遵守）

# ======================

1. 数据库名：finance
2. 字符集：utf8mb4
3. 所有表必须包含：id、create_time、update_time、del_flag
4. 逻辑删除：del_flag 0=未删除 1=已删除
5. 主键统一使用 BIGINT 自增
6. 时间字段：DATETIME
7. 禁止使用外键，业务层关联
8. 索引必须按建表语句创建

# ======================

# 三、当前项目所有表（必须严格对应）

# ======================

## 1. 用户模块

sys_user              # 系统用户表

## 2. 产品&行情模块

fin_product           # 产品表
fin_market_data       # 行情数据表

## 3. 自选模块

fin_user_favorite     # 用户自选表

## 4. 交易模块

fin_trade_order       # 交易委托单

## 5. 资讯&消息

fin_news              # 财经资讯
fin_message           # 站内消息

## 6. 后台权限模块

ums_admin             # 管理员
ums_role              # 角色
ums_resource          # 资源
ums_admin_role_relation
ums_role_resource_relation

# ======================

# 四、代码结构规范（强制）

# ======================

包结构必须如下：
com.finance.platform.模块名

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

# ======================

# 五、MyBatis-Plus 规范

# ======================

## 实体类规范

1. 所有 Entity 必须继承 `com.finance.common.entity.BaseEntity`（自动包含 id/create_time/update_time/del_flag 四个基础字段）
2. `@TableName("表名")` — 必须明确指定表名
3. `@TableLogic` — 仅当数据库表**真实存在** del_flag 列时使用；若表无 del_flag 列，则在子类中重写 delFlag 字段并标注 `@TableField(exist = false)`
4. 字段映射统一使用 `@TableField("列名")`
5. 自动填充字段：create_time 使用 `@TableField(fill = FieldFill.INSERT)`，update_time 使用 `@TableField(fill = FieldFill.INSERT_UPDATE)`

### BaseEntity 继承规则

所有 Entity 必须继承 BaseEntity，每个基础字段按以下规则处理：

| 字段 | BaseEntity 定义 | 无对应列的子类处理方式 |
|------|----------------|----------------------|
| id | `@TableId(type = IdType.AUTO)` | 无需处理，自动继承 |
| create_time | `@TableField(fill = FieldFill.INSERT)` | 若表中无该列，子类中重写：`@TableField(exist = false) private LocalDateTime createTime;` |
| update_time | `@TableField(fill = FieldFill.INSERT_UPDATE)` | 若表中无该列，子类中重写：`@TableField(exist = false) private LocalDateTime updateTime;` |
| del_flag | `@TableField("del_flag")` | 若表中无该列，子类中重写：`@TableField(exist = false) private Integer delFlag;` 若表中有该列，子类中重写并加 `@TableLogic`：`@TableLogic @TableField("del_flag") private Integer delFlag;` |

> 注意：子类重写字段时须同时使用 `@EqualsAndHashCode(callSuper = true)`（或在类上加 `@Getter @Setter @EqualsAndHashCode(callSuper = true)` 替代 `@Data`），以确保 Lombok 正确处理父类字段。

## Mapper / Service 规范

1. mapper 继承 BaseMapper
2. service 继承 IService / ServiceImpl
3. 禁止手写复杂SQL

## 分页插件配置

`finance-common` 模块的 `com.finance.common.config.MyBatisPlusConfig` 已全局配置分页插件：

```java
@Configuration
public class MyBatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
```

各业务模块无需重复配置，引入 finance-common 依赖后自动生效。
新增模块如需自定义拦截器，可在本模块添加 `@Configuration` 标注的 MyBatisPlusConfig，或注入额外的 `InnerInterceptor` Bean（`MybatisPlusInnerInterceptorAutoConfiguration` 会自动收集）。

# ======================

# 六、接口统一返回格式

# ======================

{
  "code": 200,
  "message": "success",
  "data": {}
}

状态码：
200 成功
400 参数错误
401 未登录
403 无权限
500 服务器异常

# ======================

# 七、命名规范（强制）

# ======================

类名：大驼峰
方法名：小驼峰
变量名：小驼峰
常量：大写+下划线
表名：小写+下划线
字段名：小写+下划线

# ======================

# 八、业务模块对应关系（必须遵守）

# ======================

sys_user          → 用户管理
fin_product       → 产品管理
fin_market_data   → 行情实时数据
fin_user_favorite → 用户自选
fin_trade_order   → 交易委托
fin_news          → 资讯
fin_message       → 消息推送
ums_*             → 后台权限

# ======================

# 九、AI 生成规则

# ======================

1. 必须严格按照上面的表结构生成 Entity（继承 BaseEntity）、Mapper、Service、Controller、Vo、Dto
2. 必须使用 MyBatis-Plus
3. Entity 必须继承 BaseEntity，按照「五、MyBatis-Plus 规范 > BaseEntity 继承规则」处理字段覆盖
4. 必须自动填充 create_time、update_time
5. 接口必须遵循 RESTful 规范
6. 必须加 Swagger 注解
7. 必须加注释
8. 必须符合项目技术栈
9. 不允许生成不存在的表或字段
10. 生成代码必须能直接运行

# ======================

# 十、禁止行为

# ======================

- 禁止修改表结构
- 禁止使用不兼容的依赖版本
- 禁止乱命名
- 禁止 Controller 写业务逻辑
- 禁止硬编码
- 禁止无注释
- 禁止生成与当前项目无关的代码

# ======================

# 十一、项目模块架构（多模块 Maven + Spring Cloud Alibaba）

# ======================

finance-mid-platform (pom)
├── finance-common      # 公共依赖模块（DTO、工具类、Feign接口、统一返回、异常处理、通用配置）
├── finance-gateway     # 网关服务（Spring Cloud Gateway 路由转发、全局CORS）
├── finance-system      # 系统服务（后台权限管理 ums_* 表、管理员JWT登录）
├── finance-user        # 用户服务（前端用户管理 sys_user）
├── finance-account     # 账户服务（自选管理 fin_user_favorite）
├── finance-product     # 产品服务（产品 fin_product + 行情 fin_market_data）
├── finance-trade       # 交易服务（委托交易 fin_trade_order）
├── finance-message     # 消息服务（资讯 fin_news + 站内消息 fin_message）
└── finance-search      # 搜索服务（基于 ES 的产品搜索）

## 依赖层级

- finance-common 被所有模块依赖（修改后需先 mvn clean install -pl finance-common）
- 业务模块间通过 Feign 接口调用（FeignClient 定义在 finance-common 中）

## 各模块端口号

| 模块 | 端口 | context-path |
|------|------|-------------|
| finance-gateway | 8080 | - |
| finance-system  | 8082 | /system |
| finance-trade   | 8085 | /trade |
| finance-message | 8087 | /message |
| finance-search  | 8089 | - |
| finance-account | 8086 | /account |
| finance-product | 8084 | /product |
| finance-user    | 8083 | /user |

## 各模块 Java 包基路径

| 模块 | 基础包 |
|------|--------|
| finance-common  | com.finance.common |
| finance-gateway | com.finance.gateway |
| finance-user    | com.finance.user |
| 其余业务模块    | com.finance.platform.{模块名} |

# ======================

# 十二、开发常用命令

# ======================

```bash
# 1. 编译公共模块（必须先执行，修改 common 后要重新 install）
mvn clean install -pl finance-common -DskipTests

# 2. 编译所有模块
mvn clean compile

# 3. 安装所有模块到本地仓库
mvn clean install -DskipTests

# 4. 运行单个模块（示例：系统服务）
mvn spring-boot:run -pl finance-system

# 5. 打包
mvn clean package -DskipTests

# 6. 运行打包后的 jar（示例）
java -jar finance-system/target/finance-system-1.0.0.jar

# 7. 运行测试（根 pom.xml 默认跳过测试，需显式开启）
mvn test -pl finance-common -DskipTests=false
```

# ======================

# 十三、核心基础设施类说明

# ======================

| 类 | 路径 | 用途 |
|----|------|------|
| BaseEntity | finance-common/entity/BaseEntity.java | 所有实体的基类（id, createTime, updateTime, delFlag） |
| Result\<T\> | finance-common/result/Result.java | 统一 API 返回封装（code + message + data） |
| ResultCode | finance-common/result/ResultCode.java | 状态码枚举 |
| BeanConvertUtil | finance-common/utils/BeanConvertUtil.java | Entity→VO 转换（基于 BeanUtils.copyProperties） |
| JwtUtil | finance-common/utils/JwtUtil.java | JWT Token 生成与验证 |
| GlobalExceptionHandler | finance-common/exception/GlobalExceptionHandler.java | 全局异常处理 |
| MyBatisPlusConfig | finance-common/config/MyBatisPlusConfig.java | MyBatis-Plus 分页插件配置（PaginationInnerInterceptor） |
| MyBatisPlusMetaObjectHandler | finance-common/config/MyBatisPlusMetaObjectHandler.java | 自动填充 createTime/updateTime |
| LoginInterceptor | finance-common/interceptor/LoginInterceptor.java | 登录拦截器 |
| RedisUtil | finance-common/utils/RedisUtil.java | Redis 操作工具类 |
| AuthConstant | finance-common/constants/AuthConstant.java | 权限相关常量 |

# ======================

# 十四、常见代码模式

# ======================

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

## Feign 跨服务调用

FeignClient 定义在 finance-common 中，各模块引入依赖后即可调用：

```java
@FeignClient("finance-account")
public interface AccountFeignClient {
    // 注意：路径必须包含服务端 context-path 前缀 /account
    @GetMapping("/account/finUserFavorite/{id}")
    Result<FinUserFavoriteDTO> getById(@PathVariable("id") Long id);
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

# ======================

# 十五、项目健康检查规则（强制遵守）

# ======================

**每次执行项目健康检查、错误扫描、启动异常排查时，必须优先调用 memory 中的 health-check-skill 并严格按其规则执行。**

## 配置管理原则

所有业务配置统一托管在 Nacos 配置中心，本地 application.yml 不存放业务配置，以 Nacos 远端配置为准。
本地 application.yml 仅保留：server.port、server.servlet.context-path、spring.datasource、mybatis-plus、基础框架配置。

数据库密码使用环境变量：`password: ${DB_PASSWORD:123456}`

## JWT 配置（固定值，必须存在于 Nacos）

```yaml
jwt:
  secret: finance-micro-service-20260501-very-safe-secret-key-123456789
  expire: 604800000
```

## 各模块实际端口

| 模块 | 端口 | context-path |
|------|------|-------------|
| finance-gateway | 8080 | - |
| finance-system  | 8082 | /system |
| finance-user    | 8083 | /user |
| finance-product | 8084 | /product |
| finance-trade   | 8085 | /trade |
| finance-account | 8086 | /account |
| finance-message | 8087 | /message |
| finance-search  | 8089 | - |

## 已知高频问题（启动排查速查）

1. **JWT 配置缺失** → 检查 Nacos 配置是否已发布/刷新，而非本地配置缺失
2. **SystemWebConfig 拦截器不生效** → addPathPatterns 使用了 context-path 前缀（错误），应改为 `/**`
3. **AuthConstant 通配符不匹配** → LoginInterceptor 使用 equals() 而非 PathMatcher
4. **UmsResource.delFlag 查询条件被忽略** → entity 标记了 @TableField(exist = false)
5. **网关启动异常** → 父 pom 的 spring-boot-starter-web 与 Gateway WebFlux 冲突

## 扫描检查清单（每次排查逐项过）

- [ ] 全量编译：mvn clean compile
- [ ] 所有实体类 @EqualsAndHashCode(callSuper=true)
- [ ] 所有写操作 @Transactional
- [ ] 所有 @RequestBody DTO 有 @Valid
- [ ] getById 返回 null 时返回 404
- [ ] list() 接口有分页
- [ ] application.yml 无明文密码、无硬编码 IP
- [ ] 拦截器 pathPatterns 与 context-path 一致
