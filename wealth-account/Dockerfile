FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="renjianfeng8" \
      description="wealth-account - 用户自选服务（自选产品管理）" \
      version="1.0.0"

WORKDIR /app

COPY target/wealth-account-1.0.0.jar app.jar

EXPOSE 8086

ENV TZ=Asia/Shanghai

ENTRYPOINT ["java", "-Xms128m", "-Xmx128m", "-XX:+UseContainerSupport", "-XX:+ExitOnOutOfMemoryError", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
