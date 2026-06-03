# 理财服务平台 — 项目指南

> 本文件是 **Claude Code 与 Codex（Codex.ai/code）共享的项目上下文**，涵盖技术栈、规范、约束与协作约定。
> 所有重要决策和约束写入此文件，持久化记忆各自独立不互通。

---

## 目录

- [一、项目概览](#一项目概览)
- [二、技术栈](#二技术栈)
- [三、模块与目录](#三模块与目录)
- [四、开发命令](#四开发命令)
- [五、编码规范](#五编码规范)
- [六、Git 提交规范](#六git-提交规范)
- [七、常见代码模式](#七常见代码模式)
- [八、测试规范](#八测试规范)
- [九、协作约定](#九协作约定双客户端)
- [十、启动验证](#十启动验证)
- [十一、代码扫描](#十一代码扫描)
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
| Playwright | ^1.60.0 | 前端 E2E 测试 |

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

## 五、编码规范

### 5.1 实体与数据库
- Entity 继承 `BaseEntity`，字段映射以 `init.sql` 列名为准（`@TableField("列名")`），**不信任 DATABASE-SCHEMA.md 中的列名**
- 自动填充 `create_time` / `update_time`
- Mapper 继承 `BaseMapper`，Service 继承 `IService` / `ServiceImpl`
- 禁止手写复杂 SQL
- 分页插件已全局配置，各域无需重复配置

### 5.2 接口
- 统一返回：`{code, message, data}` — 200 成功 / 400 参数错误 / 401 未登录 / 403 无权限 / 404 不存在 / 500 服务器异常
- RESTful + Swagger 注解（`@Tag` / `@Operation`）
- `@RequestBody` DTO 必须加 `@Valid`
- 写操作加 `@Transactional(rollbackFor = Exception.class)`
- 新增接口确认是否加入 `AuthConstant.PERMIT_ALL_URLS`
- Controller 只做参数校验与路由，不写业务逻辑

### 5.3 命名
| 元素 | 规则 |
|------|------|
| 类名 | 大驼峰 |
| 方法名 / 变量名 | 小驼峰 |
| 常量 | 大写+下划线 |
| 表名 / 字段名 | 小写+下划线 |

### 5.4 安全与异常
- update 用 `BeanConvertUtil.copyNonNullProperties` 防 null 覆盖（禁止 `BeanUtils.copyProperties`）
- 业务异常抛 `ServiceException(code, message)`
- JWT 密钥通过 `.env` 注入，启动时校验字节 ≥ 32
- 重复创建检查须配合数据库唯一索引作最终防线

### 5.5 禁止
- 修改表结构 · Controller 写业务逻辑 · hardcode 密码/IP · 无注释 · 不兼容依赖版本 · 直接 `service.list()` 全表查询

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
docs: 更新 README 部署说明
```

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

### 前端测试

- E2E 框架：Playwright
- 测试文件放在 `front/tests/`

---

## 九、协作约定（Codex 主控 / Claude Code 执行）

**Codex 为主控端**，负责架构设计、任务分解、决策把关。

**Claude Code 为执行端**，按指令完成实现，不自行决策架构变更。

### 职责边界

| 职责 | 负责方 | 说明 |
|------|--------|------|
| 架构设计 / 方案评审 | Codex | 涉及模块拆分、表结构变更、技术选型 |
| 任务分解与指派 | Codex | 明确告诉 CC 做什么、改哪些文件 |
| 代码实现 | Claude Code | 按指令完成编码，遵循 CLAUDE.md 规范 |
| Code Review | Codex | 审查 CC 的代码质量 |
| 文档维护 | 双方 | CLAUDE.md / docs/ 均可更新 |
| Git 提交 | 双方 | 谁改谁提，改前 `git pull` |

### 共享规则
- **CLAUDE.md 是共享记忆核心载体**，重要决策写在此处
- 持久化记忆各自独立，不互通
- 改动前 `git pull`，改动后及时 `git commit && git push`
- Claude Code 收到模糊指令时先问清楚，不自作主张

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

## 十一、代码扫描

每次排查逐项过：

- [ ] 实体类 `@EqualsAndHashCode(callSuper=true)` 或 `@Getter @Setter` + callSuper
- [ ] 写操作 `@Transactional(rollbackFor = Exception.class)`
- [ ] `@RequestBody` 有 `@Valid`
- [ ] getById 空值返回 404
- [ ] list() 带分页
- [ ] update 用 `copyNonNullProperties`
- [ ] 拦截器 pathPatterns 与 context-path 一致（不能加前缀）
- [ ] redis 配置用 `spring.data.redis.*`
- [ ] 链路追踪用 `management.zipkin.tracing.endpoint`
- [ ] Entity 字段与 init.sql 逐列核对

---

## 十二、历史教训

| # | 规则 | 要点 |
|---|------|------|
| 1 | **文档链检查** | 追踪到最底层数据源（init.sql、数据库、代码）再动手 |
| 2 | **批量改一个先验证** | 改一个 → 验证 → 再批量 |
| 3 | **改完主动提验证** | 涉及 DB/数据流/配置，主动提议启动验证 |
| 4 | **三方一致** | 文档、代码、数据库三方对齐后再改 |
| 5 | **跨域切换先读文件** | 从前端切后端 / 跨模块时，先读关键文件建立上下文 |
