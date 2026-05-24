# 基础设施安全加固设计（第一批）

**日期:** 2026-05-24
**状态:** 已审批

## 修复项

### 1. Grafana 默认密码

**现状:** `docker-compose.yml` 中 Grafana 未设置 `GF_SECURITY_ADMIN_PASSWORD`，默认 admin/admin
**修复:** 追加环境变量 `GF_SECURITY_ADMIN_PASSWORD=${GRAFANA_ADMIN_PASSWORD}`，值从 `.env` 读取

### 2. 默认密码加固

| 服务 | 位置 | 操作 |
|------|------|------|
| Grafana | `docker-compose.yml` | 追加 `GF_SECURITY_ADMIN_PASSWORD` 环境变量 |
| RabbitMQ | `.env` | 密码改为强口令（服务未部署，仅加固配置） |
| ES | 无需改动 | 本地开发空密码正常，docker-compose 未部署 ES |

### 3. wealth-service Dockerfile

**现状:** 模块合并后 Dockerfile 丢失，CI 中 `docker build -f wealth-service/Dockerfile` 会失败
**修复:** 新建 `wealth-service/Dockerfile`，参照 gateway 模板

```dockerfile
FROM eclipse-temurin:21-jre-alpine
LABEL maintainer="renjianfeng8" \
      description="wealth-service - 合并后单体业务服务（含 system/user/product/trade/message/search）"
WORKDIR /app
COPY target/wealth-service-1.0.0.jar app.jar
EXPOSE 8081
ENV TZ=Asia/Shanghai
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-XX:+UseContainerSupport", "-XX:+ExitOnOutOfMemoryError", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
```

### 4. Actuator Nginx 层拦截

**现状:** prod 配置暴露 `health,info,prometheus` 端点无认证，公网可访问
**修复:** `nginx.conf` 中新增 Actuator 路径拦截规则，返回 403

```nginx
location ~ ^/api/v1/(system|user|product|trade|message|search)/actuator/ {
    return 403;
}
```

**原理:** 公网请求 → nginx 443 → 匹配到 Actuator 路径 → 直接 403，不转发到 gateway。Prometheus 抓取走 Docker 内网直连 gateway:8080，不受影响。

## 改动文件清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `wealth-service/Dockerfile` | 新建 | CI/CD 修复 |
| `docker-compose.yml` | 修改 | Grafana 加密码 |
| `nginx.conf` | 修改 | Actuator 访问控制 |
| `.env` | 修改 | 加固密码 |
| `.env.example` | 修改 | 同步占位符 |

## 不包含在本批的内容

- 无用/重复依赖清理（spring-boot-starter-amqp、openfeign）
- 硬编码值外移
- Sentinel 规则持久化
- RabbitMQ 服务部署
