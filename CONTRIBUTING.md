# 贡献指南

## 项目结构

```
wealth-service-platform
├── wealth-common     # 公共模块：工具类、Contract 接口、全局配置
├── wealth-gateway    # 网关层（端口 8080）
├── wealth-service    # 业务服务（端口 8081）
└── front             # Vue 3 SPA
```

## 开发环境

- JDK 21+, Maven 3.9+, Node.js 20+
- MySQL 8.0 + Redis 5+（Docker）
- 启动前执行 `init.sql` 初始化数据库

## 开发流程

```bash
# 编译公共模块（修改 common 后必须执行）
mvn clean install -pl wealth-common -DskipTests

# 全量编译
mvn clean install -DskipTests

# 启动（顺序：gateway → service）
mvn spring-boot:run -pl wealth-gateway
mvn spring-boot:run -pl wealth-service

# 前端
cd front && npm install && npx vite
```

完整启动流程见 [STARTUP.md](docs/STARTUP.md)。

## 代码规范

所有规范以 [CLAUDE.md](.claude/CLAUDE.md) 和 [CODE-STANDARDS.md](docs/CODE-STANDARDS.md) 为准，关键约束：

- Controller 只做路由，不写业务逻辑
- 构造器注入使用 `@RequiredArgsConstructor`
- 实体字段显式标注 `@TableField`
- 写操作加 `@Transactional(rollbackFor = Exception.class)`
- 禁止通配符导入、魔法值、空 catch

## 提交规范

遵循 [Conventional Commits](https://www.conventionalcommits.org/)：

```
<type>(<scope>): <description>
```

type: `feat` / `fix` / `docs` / `refactor` / `test` / `chore`
scope（可选）：`common` / `gateway` / `service`

示例：
```
feat(service): 添加产品分页查询接口
fix(service): 修复交易委托金额计算精度问题
docs: 更新 README 部署说明
```

## 数据库

- 表结构以 `wealth-common/src/main/resources/sql/init.sql` 为准
- 字段映射参考 [DATABASE-SCHEMA.md](docs/DATABASE-SCHEMA.md)
