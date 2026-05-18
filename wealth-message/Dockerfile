FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="renjianfeng8" \
      description="wealth-message - 消息服务（财经资讯、站内消息推送）" \
      version="1.0.0"

WORKDIR /app

COPY target/wealth-message-1.0.0.jar app.jar

EXPOSE 8087

ENV TZ=Asia/Shanghai

ENTRYPOINT ["java", "-Xms512m", "-Xmx512m", "-XX:+UseContainerSupport", "-XX:+ExitOnOutOfMemoryError", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
