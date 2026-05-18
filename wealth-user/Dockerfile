FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="renjianfeng8" \
      description="wealth-user - 前端用户管理服务（注册、登录、个人信息）" \
      version="1.0.0"

WORKDIR /app

COPY target/wealth-user-1.0.0.jar app.jar

EXPOSE 8083

ENV TZ=Asia/Shanghai

ENTRYPOINT ["java", "-jar", "app.jar"]
