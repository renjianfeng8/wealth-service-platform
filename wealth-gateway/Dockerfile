FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="renjianfeng8" \
      description="wealth-gateway - API网关，统一路由转发与全局CORS" \
      version="1.0.0"

WORKDIR /app

COPY target/wealth-gateway-1.0.0.jar app.jar

EXPOSE 8080

ENV TZ=Asia/Shanghai

ENTRYPOINT ["java", "-jar", "app.jar"]
