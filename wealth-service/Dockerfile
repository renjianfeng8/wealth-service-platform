FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="renjianfeng8" \
      description="wealth-service - 合并后单体业务服务（含 system/user/product/trade/message/search）" \
      version="1.0.0"

WORKDIR /app

COPY target/wealth-service-1.0.0.jar app.jar

EXPOSE 8081

ENV TZ=Asia/Shanghai

ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-XX:+UseContainerSupport", "-XX:+ExitOnOutOfMemoryError", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
