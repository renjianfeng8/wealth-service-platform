FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="renjianfeng8" \
      description="finance-search - 搜索服务（基于ElasticSearch的产品全文搜索）" \
      version="1.0.0"

WORKDIR /app

COPY target/finance-search-1.0.0.jar app.jar

EXPOSE 8089

ENV TZ=Asia/Shanghai

ENTRYPOINT ["java", "-jar", "app.jar"]
