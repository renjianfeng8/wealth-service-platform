# 环境变量自动加载 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现 `mvn spring-boot:run` 自动读取 `.env` 文件，无需手动设置环境变量。

**Architecture:** 利用 `properties-maven-plugin` 在 Maven `initialize` 阶段读取 `.env` 文件注册为 Maven 属性，再通过 `spring-boot-maven-plugin` 的 `<environmentVariables>` 注入到 forked JVM 进程中。

**Tech Stack:** Maven (properties-maven-plugin 1.2.1, spring-boot-maven-plugin 3.3.13), Spring Boot, Java 21

---

### Task 1: 修改 wealth-service/pom.xml

**Files:**
- Modify: `wealth-service/pom.xml:46-53`

- [ ] **Step 1: 在 build/plugins 中追加 properties-maven-plugin**

在 `spring-boot-maven-plugin` 之前添加：

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>properties-maven-plugin</artifactId>
    <version>1.2.1</version>
    <executions>
        <execution>
            <phase>initialize</phase>
            <goals>
                <goal>read-project-properties</goal>
            </goals>
            <configuration>
                <files>
                    <file>.env</file>
                </files>
                <ignoreFileNotFound>true</ignoreFileNotFound>
            </configuration>
        </execution>
    </executions>
</plugin>
```

- [ ] **Step 2: 修改 spring-boot-maven-plugin，添加 environmentVariables**

将原有：
```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
</plugin>
```

改为：
```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <environmentVariables>
            <JWT_SECRET>${JWT_SECRET}</JWT_SECRET>
            <MYSQL_HOST>${MYSQL_HOST}</MYSQL_HOST>
            <MYSQL_ROOT_PASSWORD>${MYSQL_ROOT_PASSWORD}</MYSQL_ROOT_PASSWORD>
            <REDIS_HOST>${REDIS_HOST}</REDIS_HOST>
            <REDIS_PORT>${REDIS_PORT}</REDIS_PORT>
            <REDIS_PASSWORD>${REDIS_PASSWORD}</REDIS_PASSWORD>
        </environmentVariables>
    </configuration>
</plugin>
```

- [ ] **Step 3: 验证编译**

```bash
cd D:\demo\wealth-service-platform
mvn compile -pl wealth-service -q
```
Expected: BUILD SUCCESS（无编译错误）

- [ ] **Step 4: 提交**

```bash
git add wealth-service/pom.xml
git commit -m "build(service): 通过 properties-maven-plugin 实现 .env 自动加载"
```

---

### Task 2: 修改 wealth-gateway/pom.xml

**Files:**
- Modify: `wealth-gateway/pom.xml:82-89`

- [ ] **Step 1: 在 build/plugins 中追加 properties-maven-plugin**

在 `spring-boot-maven-plugin` 之前添加：

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>properties-maven-plugin</artifactId>
    <version>1.2.1</version>
    <executions>
        <execution>
            <phase>initialize</phase>
            <goals>
                <goal>read-project-properties</goal>
            </goals>
            <configuration>
                <files>
                    <file>.env</file>
                </files>
                <ignoreFileNotFound>true</ignoreFileNotFound>
            </configuration>
        </execution>
    </executions>
</plugin>
```

- [ ] **Step 2: 修改 spring-boot-maven-plugin，添加 environmentVariables**

将原有：
```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
</plugin>
```

改为：
```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <environmentVariables>
            <JWT_SECRET>${JWT_SECRET}</JWT_SECRET>
            <JWT_EXPIRE>${JWT_EXPIRE}</JWT_EXPIRE>
            <SERVICE_HOST>${SERVICE_HOST}</SERVICE_HOST>
            <CORS_ALLOWED_ORIGINS>${CORS_ALLOWED_ORIGINS}</CORS_ALLOWED_ORIGINS>
        </environmentVariables>
    </configuration>
</plugin>
```

- [ ] **Step 3: 验证编译**

```bash
cd D:\demo\wealth-service-platform
mvn compile -pl wealth-gateway -q
```
Expected: BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
git add wealth-gateway/pom.xml
git commit -m "build(gateway): 通过 properties-maven-plugin 实现 .env 自动加载"
```

---

### Task 3: 集成验证

**Depends on:** Task 1, Task 2

- [ ] **Step 1: 全量编译**

```bash
cd D:\demo\wealth-service-platform
mvn clean install -DskipTests -q
```
Expected: BUILD SUCCESS

- [ ] **Step 2: 启动 gateway 验证**

先确保端口 8080 未被占用：
```bash
netstat -ano | findstr :8080
```

启动网关：
```bash
cd D:\demo\wealth-service-platform
mvn spring-boot:run -pl wealth-gateway
```

观察日志，预期出现：
```
Netty started on port 8080 (http)
Started WealthGatewayApplication in ...
```

`Ctrl+C` 停止。

- [ ] **Step 3: 启动 service 验证**

先确保端口 8081 未被占用。

启动：
```bash
cd D:\demo\wealth-service-platform
mvn spring-boot:run -pl wealth-service
```

观察日志，预期出现：
```
HikariPool-1 - Start completed.
Started WealthServiceApplication in ...
```

验证不需要手动设置 `$env:MYSQL_ROOT_PASSWORD=...` 即可自动启动成功。

`Ctrl+C` 停止。

- [ ] **Step 4: 提价最终提交**

```bash
git add .
git commit -m "build: 实现 Maven 启动自动加载 .env 环境变量"
```
