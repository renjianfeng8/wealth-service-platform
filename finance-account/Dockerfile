FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="renjianfeng8" \
      description="finance-account - 用户自选服务（自选产品管理）" \
      version="1.0.0"

WORKDIR /app

COPY target/finance-account-1.0.0.jar app.jar

EXPOSE 8086

ENV TZ=Asia/Shanghai

ENTRYPOINT ["java", "-jar", "app.jar"]
