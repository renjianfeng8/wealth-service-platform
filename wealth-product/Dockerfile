FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="renjianfeng8" \
      description="wealth-product - 产品与行情服务（产品管理、实时行情）" \
      version="1.0.0"

WORKDIR /app

COPY target/wealth-product-1.0.0.jar app.jar

EXPOSE 8084

ENV TZ=Asia/Shanghai

ENTRYPOINT ["java", "-jar", "app.jar"]
