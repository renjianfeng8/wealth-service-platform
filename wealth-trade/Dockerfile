FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="renjianfeng8" \
      description="wealth-trade - 交易委托服务（委托下单、订单管理）" \
      version="1.0.0"

WORKDIR /app

COPY target/wealth-trade-1.0.0.jar app.jar

EXPOSE 8085

ENV TZ=Asia/Shanghai

ENTRYPOINT ["java", "-Xms256m", "-Xmx256m", "-XX:+UseContainerSupport", "-XX:+ExitOnOutOfMemoryError", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
