# 理财服务平台 — 项目指南

> 项目级文档，涵盖技术栈、规范、约束与协作约定。
> 详细代码规范手册见 [docs/CODE-STANDARDS.md](../docs/CODE-STANDARDS.md)。

---

## 目录

- [一、项目概览](#一项目概览)
- [二、技术栈](#二技术栈)
- [三、模块与目录](#三模块与目录)
- [四、开发命令](#四开发命令)
- [五、编码规范（概要）](#五编码规范概要)
- [六、Git 提交规范](#六git-提交规范)
- [七、常见代码模式](#七常见代码模式)
- [八、测试规范](#八测试规范)
- [九、协作约定](#九协作约定)
- [十、启动验证](#十启动验证)
- [十一、代码扫描清单](#十一代码扫描清单)
- [十二、历史教训](#十二历史教训)

---

## 一、项目概览

理财服务平台，Spring Boot 单体服务 + Spring Cloud Gateway 网关 + Vue 3 前端 SPA。各业务域合并部署在 `wealth-service` 模块。

| 关键文档 | 路径 |
|---------|------|
| **建表 SQL（唯一真理）** | `wealth-common/src/main/resources/sql/init.sql` |
| 架构文档 | `docs/ARCHITECTURE.md` |
| 表结构 / BaseEntity 规范 | `docs/DATABASE-SCHEMA.md` |
| 已知问题 | `docs/BUG.md` |
| **代码规范手册** | **`docs/CODE-STANDARDS.md`** |

---

## 二、技术栈

### 后端

| 技术 | 版本 |
|------|------|
| Java | 21 |
| Spring Boot | 3.3.13 |
| Spring Cloud | 2023.0.6 |
| Spring Cloud Alibaba | 2023.0.3.4 |
| MyBatis-Plus | 3.5.9 |
| MyBatis-Spring | 3.0.5 |
| MySQL | 8 (connector 由 SB parent 管理) |
| Redis | 5 (spring-boot-starter-data-redis) |
| JWT (jjwt) | 0.12.6 |
| Knife4j (Swagger) | 4.5.0 |
| Sentinel | 由 SCA BOM 管理 |
| Micrometer Tracing | 1.3.6 (Brave + Zipkin) |
| Prometheus | micrometer-registry-prometheus |
| Elasticsearch | 8.8.2（可选，search 域降级 MySQL LIKE）|
| Nacos | 由 SCA BOM 管理（注册中心 + 配置中心，已禁用）|

### 前端

| 技术 | 版本 |
|------|------|
| Vue | ^3.5.13 |
| Vite | ^6.3.1 |
| Element Plus | ^2.9.7 |
| Pinia | ^2.3.1 |
| TypeScript | ~5.7.2 |
| Vue Router | ^4.5.0 |
| Axios | ^1.7.9 |
| ECharts | ^5.5.1 |
| dayjs | ^1.11.13 |

### 测试

| 技术 | 版本 | 用途 |
|------|------|------|
| JUnit 5 + Mockito | 由 SB parent 管理 | 后端单元/集成测试 |

---

## 三、模块与目录

```
wealth-service-platform (pom)
├── wealth-common   # 公共模块：工具类、Contract 接口、统一返回、通用配置
├── wealth-gateway  # 网关（Spring Cloud Gateway，无数据源）
├── wealth-service  # 业务聚合服务（所有 domain 合并部署）
└── front           # Vue 3 SPA
```

### 业务域 domain 映射

| 表前缀 | 业务 | domain |
|--------|------|--------|
| sys_user | 用户管理 | user |
| wea_product / wea_market_data / wea_user_favorite | 产品 / 行情 / 自选 | product |
| wea_trade_order | 交易委托 | trade |
| wea_news / wea_message | 资讯 / 消息推送 | message |
| ums_* | 后台权限 | system |

### 包结构（`com.wealth.platform.{domain}`）

```
controller → service → mapper → entity   # 标准分层
vo / dto / config / constant / interceptor
```

---

## 四、开发命令

```bash
# 1. 编译公共模块（修改 common 后必须执行）
mvn clean install -pl wealth-common -DskipTests

# 2. 全量编译 / 安装
mvn clean install -DskipTests

# 3. 运行（先 gateway 后 service）
mvn spring-boot:run -pl wealth-gateway
mvn spring-boot:run -pl wealth-service

# 4. 前端
cd front && npm install && npx vite

# 5. 测试
mvn test -pl wealth-service -DskipTests=false
mvn test -pl wealth-service -Dtest=XxxTest -DskipTests=false
```

> 配置修改需审批：application.yml / application-prod.yml / pom.xml 默认锁定，改前询问用户并给出强理由。

---

## 五、编码规范（概要）

> 详细规范（含示例和正误对比）请查阅 [docs/CODE-STANDARDS.md](../docs/CODE-STANDARDS.md)。

### 5.1 实体与数据库
- Entity 继承 `BaseEntity`，字段映射以 `init.sql` 列名为准（`@TableField("列名")`）
- Mapper 继承 `BaseMapper`，Service 继承 `IService` / `ServiceImpl`
- 自动填充 `create_time` / `update_time`
- 分页插件已全局配置，各域无需重复配置
- 若表缺少某列（如 `del_flag`），使用 `@TableField(exist = false)` 排除，并确保删除操作走物理删除

### 5.2 接口
- 统一返回：`{code, message, data}`
- RESTful + Swagger 注解（`@Tag` / `@Operation`）
- `@RequestBody` DTO 必须加 `@Valid`
- Controller 只做参数校验与路由，**不写业务逻辑**

### 5.3 核心代码规范速览
| 规范 | 要求 |
|------|------|
| 导入 | 禁止通配符，按 `java.* → org.* → com.*` 顺序 |
| 构造器注入 | `@RequiredArgsConstructor`，禁止 `@Autowired` 字段注入 |
| 日志 | `@Slf4j`，禁止手动声明 Logger |
| 魔法值 | 全部抽取为 `private static final` 常量 |
| Controller JavaDoc | 不写方法级 JavaDoc（用 `@Operation` 替代） |
| 实体 `@TableField` | 全部显式标注 |
| 更新操作 | `BeanConvertUtil.copyNonNullProperties`（禁止 `BeanUtils.copyProperties`）|
| 异常 | 业务异常抛 `ServiceException(code, message)`，禁止空 catch |
| 事务 | 写操作加 `@Transactional(rollbackFor = Exception.class)` |

### 5.4 禁止
- 修改表结构 · Controller 写业务逻辑 · hardcode 密码/IP · 通配符导入 · 空 catch · 全限定类名字段 · 全表 `service.list()` 无分页 · 不兼容依赖版本

---

## 六、Git 提交规范

遵循 [约定式提交](https://www.conventionalcommits.org/)：

```
<type>(<scope>): <description>
```

| type | 说明 |
|------|------|
| feat / fix | 新功能 / 修复 |
| docs / style | 文档 / 格式 |
| refactor / perf | 重构 / 性能 |
| test / chore | 测试 / 构建 |
| ci / build / revert | CI / 构建系统 / 回退 |

scope（可选）：common / gateway / service
description：命令式语气、首字母小写、末尾无句号

```
feat(service): 添加产品分页查询接口
fix(service): 修复交易委托金额计算精度问题
docs: 更新代码规范手册
```

### 操作约束
- **提交和推送必须等用户明确指令**，禁止自动执行 `git commit` 或 `git push`

---

## 七、常见代码模式

### Entity → VO 转换
```java
return Result.success(BeanConvertUtil.convert(entity, XxxVO.class));
// 批量：BeanConvertUtil.convertList(list, XxxVO.class)
// 分页：BeanConvertUtil.convertPage(page, XxxVO.class)
```

### update null 安全
```java
public boolean updateXxx(Long id, XxxDTO dto) {
    Xxx entity = getById(id);
    if (entity == null) return false;
    BeanConvertUtil.copyNonNullProperties(dto, entity);
    entity.setId(id);
    return updateById(entity);
}
```

### 业务异常
```java
throw new ServiceException(400, "参数不合法");
```

### Contract 接口（替代 Feign）
```java
// wealth-common 定义接口 → wealth-service 中 @Autowired 注入实现
```

### JWT 登录流程
```
UmsAdminController.login(LoginDTO) → UmsAdminService 验证 → 返回 JWT token
后续请求由 LoginInterceptor 拦截校验
```

### Swagger 注解
```java
@Tag(name = "模块管理")
@Operation(summary = "操作描述")
```

### 常量定义
```java
private static final int COOKIE_MAX_AGE_SECONDS = 1800;
private static final int MAX_LOGIN_ATTEMPTS = 5;
private static final long LOCK_DURATION_MINUTES = 15;
```

---

## 八、测试规范

### 后端测试
- 框架：JUnit 5 + Mockito
- Controller 层：MockMvc 验证 HTTP 状态码和返回 JSON 结构
- Service 层：Mockito mock 依赖，只测当前类逻辑
- Mapper 层：`@MyBatisPlusTest` 或 `@SpringBootTest`（需要数据库环境）
- 命名：`{被测类名}Test.java` 放在 `src/test/java`，包路径与被测类一致
- 方法命名：`{方法名}_should_{预期行为}`（如 `login_should_return_token_when_password_correct`）
- 每个 Service 方法至少有一个正向用例
- 分支逻辑必须覆盖异常路径（参数非法、资源不存在、状态冲突等）

---

## 九、协作约定

- **CLAUDE.md** 和 **docs/CODE-STANDARDS.md** 是核心规范文档，重要决策和约定写入此处
- 改动前先读取关键文件建立上下文（跨模块切换、涉及 DB 变更时尤需注意）
- 文档、代码、数据库三方对齐后再修改

---

## 十、启动验证

- [ ] Docker 容器运行（mysql / redis / nginx）
- [ ] `.env` 已配置（根目录 + gateway + service）
- [ ] 全量编译通过
- [ ] 启动顺序：gateway → wealth-service
- [ ] 日志出现 "HikariPool-1 - Start completed"
- [ ] 前端可访问
- [ ] 冒烟：`POST /system/umsAdmin/login` 返回 JWT

---

## 十一、代码扫描清单

每次排查逐项过：

- [ ] 实体类 `@EqualsAndHashCode(callSuper=true)` 或 `@Getter @Setter` + callSuper
- [ ] 实体字段全部标注 `@TableField("列名")`，与 init.sql 一致
- [ ] 无 `@TableLogic` 与 `@TableField(exist = false)` 冲突（物理删除检查）
- [ ] 写操作 `@Transactional(rollbackFor = Exception.class)`
- [ ] `@RequestBody` 有 `@Valid`
- [ ] getById 空值返回 404
- [ ] list() 带分页
- [ ] update 用 `copyNonNullProperties`
- [ ] Controller 无业务逻辑（Token 解析、权限校验等下沉到 Service）
- [ ] 无通配符导入、无全限定类名字段
- [ ] 魔法值已抽取常量
- [ ] 构造器注入使用 `@RequiredArgsConstructor`
- [ ] Logger 使用 `@Slf4j`
- [ ] 拦截器 pathPatterns 与 context-path 一致（不能加前缀）
- [ ] redis 配置用 `spring.data.redis.*`
- [ ] 链路追踪用 `management.zipkin.tracing.endpoint`
- [ ] 异常信息中英文统一

---

## 十二、历史教训

| # | 规则 | 要点 |
|---|------|------|
| 1 | **文档链检查** | 追踪到最底层数据源（init.sql、数据库、代码）再动手 |
| 2 | **批量改一个先验证** | 改一个 → 验证 → 再批量 |
| 3 | **改完主动提验证** | 涉及 DB/数据流/配置，主动提议启动验证 |
| 4 | **三方一致** | 文档、代码、数据库三方对齐后再改 |
| 5 | **跨域切换先读文件** | 从前端切后端 / 跨模块时，先读关键文件建立上下文 |
| 6 | **@TableLogic 继承冲突** | BaseEntity 带 `@TableLogic`，无 `del_flag` 列的子表须走物理删除 |
| 7 | **Controller 职责边界** | 所有 Bearer Token 解析/校验必须下沉到 Service，Controller 只路由 |
| 8 | **异常信息语言统一** | 所有异常提示使用中文，禁止中英文混杂 |
