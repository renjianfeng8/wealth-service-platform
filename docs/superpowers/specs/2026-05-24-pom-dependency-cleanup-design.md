# POM 依赖清理与安全升级设计

## 背景

项目 POM 文件中存在多处重复依赖声明、已废弃的依赖残留以及部分依赖版本存在已知 CVE 漏洞，需要进行系统性清理和安全升级。

## 升级策略

安全补丁升级原则：仅升级到各依赖**当前次版本线内的最新补丁版本**，不跨次版本。

## 版本升级

| 依赖 | 当前版本 | 目标版本 | 影响 |
|------|---------|---------|------|
| Spring Boot | 3.3.5 | 3.3.13 | Tomcat 10.1.31→10.1.37+, Spring Framework 6.1.14→6.1.18+, Jackson 2.17.2→2.17.3+ |
| Spring Cloud | 2023.0.3 | 2023.0.6 | 子模块相应升级 |
| Spring Cloud Alibaba | 2023.0.1.2 | 2023.0.3.4 | Nacos 2.3.2→2.4.3，修复多处 Bug |
| MyBatis-Plus | 3.5.7 | 3.5.9 | 补丁级升级 |
| MyBatis-Spring | 3.0.4 | 3.0.5 | 补丁级升级 |
| Knife4j | 4.4.0 | 4.5.0 | 最新稳定版 |
| Micrometer Tracing BOM | 1.3.5 | 1.3.6 | 补丁级升级 |

> 以上版本的 Tomcat/Spring Framework/Jackson/Netty 等随 Spring Boot 版本升级自动更新。

## 依赖去重

当前 3 个 POM 文件存在多重重叠声明：

| 依赖 | 当前声明位置 | 处理 |
|------|------------|------|
| JWT (jjwt-api/impl/jackson) | 父POM + wealth-common + wealth-gateway | 保留父 POM，删除子模块重复 |
| Nacos discovery | 父POM + wealth-gateway | 保留父 POM，删除 gateway 重复 |
| Nacos config | 父POM + wealth-gateway | 保留父 POM，删除 gateway 重复 |
| Bootstrap | 父POM + wealth-gateway | 保留父 POM，删除 gateway 重复 |
| Loadbalancer | wealth-common + wealth-gateway | 保留 wealth-common，删除 gateway 重复 |
| Micrometer tracing | wealth-common + wealth-gateway | 保留 wealth-common，删除 gateway 重复 |
| Zipkin sender | wealth-common + wealth-gateway | 保留 wealth-common，删除 gateway 重复 |
| Prometheus registry | wealth-common + wealth-gateway | 保留 wealth-common，删除 gateway 重复 |
| Actuator | wealth-common + wealth-gateway | 保留 wealth-common，删除 gateway 重复 |

## 无用依赖移除

| 依赖 | 位置 | 理由 |
|------|------|------|
| spring-boot-starter-amqp (RabbitMQ) | wealth-common | 全项目无任何 RabbitMQ 代码引用 |
| spring-cloud-starter-loadbalancer | wealth-gateway | Gateway 使用静态 HTTP 路由，无需负载均衡 |

## 风险与回滚

- 每次修改后执行 `mvn clean compile -pl <module>` 验证编译
- 全量编译验证：`mvn clean install -DskipTests`
- 如升级后出现兼容性问题，回退至父 POM 版本号即可
