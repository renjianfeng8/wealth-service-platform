FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="renjianfeng8" \
      description="wealth-system - 后台权限管理服务（RBAC、JWT登录）" \
      version="1.0.0"

WORKDIR /app

COPY target/wealth-system-1.0.0.jar app.jar

EXPOSE 8082

ENV TZ=Asia/Shanghai

ENTRYPOINT ["java", "-jar", "app.jar"]
