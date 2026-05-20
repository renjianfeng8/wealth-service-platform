FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="renjianfeng8" \
      description="wealth-user - 前端用户管理服务（注册、登录、个人信息）" \
      version="1.0.0"

WORKDIR /app

COPY target/wealth-user-1.0.0.jar app.jar

EXPOSE 8083

ENV TZ=Asia/Shanghai

ENTRYPOINT ["java", "-Xms256m", "-Xmx256m", "-XX:+UseContainerSupport", "-XX:+ExitOnOutOfMemoryError", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
