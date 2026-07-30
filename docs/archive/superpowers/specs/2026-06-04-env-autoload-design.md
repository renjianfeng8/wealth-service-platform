# 环境变量自动加载方案

> 使用 `properties-maven-plugin` 读取 `.env` 文件，通过 `spring-boot-maven-plugin` 注入系统环境变量，实现 `mvn spring-boot:run` 自动加载配置。

---

## 问题

通过 `mvn spring-boot:run` 启动时需要手动设置 `$env:MYSQL_ROOT_PASSWORD` 等环境变量，否则启动失败。之前 commit `8f040560` 移除了旧的 .env 加载机制后，CMD 开发时每次需手动 export。

## 方案

```
initialize 阶段                     process-classes 阶段
  properties-maven-plugin            spring-boot-maven-plugin
  ↓                                 ↓
  .env  →  Maven Properties  →  <environmentVariables>  →  forked JVM
                                                               ↓
                                                     application.yml ${VAR}
```

## 涉及模块

| 模块 | .env 路径 | 需要注入的变量 |
|------|-----------|---------------|
| `wealth-service` | `wealth-service/.env` | JWT_SECRET, MYSQL_HOST, MYSQL_ROOT_PASSWORD, REDIS_HOST, REDIS_PORT, REDIS_PASSWORD |
| `wealth-gateway` | `wealth-gateway/.env` | JWT_SECRET, JWT_EXPIRE, SERVICE_HOST, CORS_ALLOWED_ORIGINS |

## 改动清单

### 1. wealth-service/pom.xml

在 `<build><plugins>` 中添加两个插件：

**properties-maven-plugin**（读取 .env 为 Maven 属性）：
- phase: `initialize`
- goal: `read-project-properties`
- files: `.env`
- ignoreFileNotFound: true（.env 在 git 中不存在，首次 clone 时不会报错）

**spring-boot-maven-plugin** 增加 `<configuration><environmentVariables>`，映射所需变量名：
- JWT_SECRET
- MYSQL_HOST
- MYSQL_ROOT_PASSWORD
- REDIS_HOST
- REDIS_PORT
- REDIS_PASSWORD

### 2. wealth-gateway/pom.xml

同样操作：

**properties-maven-plugin**：
- files: `.env`
- ignoreFileNotFound: true

**spring-boot-maven-plugin** 增加 `<environmentVariables>`：
- JWT_SECRET
- JWT_EXPIRE
- SERVICE_HOST
- CORS_ALLOWED_ORIGINS

### 3. .env 文件

已有存在于 `.gitignore` 中，无需改动。pom.xml 只引用 `${变量名}`，不包含实际值。

## 安全性

| 文件 | 内容 | 提交 |
|------|------|:----:|
| `.env` | 明文密码 | `.gitignore` 排除，不提交 |
| `pom.xml` | `${变量名}` 占位符 | 提交 |
| `application.yml` | `${变量名}` 占位符 | 已提交 |

## 验证方式

```bash
# 不需要手动 set 环境变量，直接启动
mvn spring-boot:run -pl wealth-service

# 观察日志出现 "HikariPool-1 - Start completed" 即通过
```

## 回滚方式

回滚 pom.xml 中的 plugin 配置即可。
