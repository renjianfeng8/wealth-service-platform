FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="renjianfeng8" \
      description="finance-message - 消息服务（财经资讯、站内消息推送）" \
      version="1.0.0"

WORKDIR /app

COPY target/finance-message-1.0.0.jar app.jar

EXPOSE 8087

ENV TZ=Asia/Shanghai

ENTRYPOINT ["java", "-jar", "app.jar"]
