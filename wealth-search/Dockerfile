FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="renjianfeng8" \
      description="wealth-search - 搜索服务（基于ElasticSearch的产品全文搜索）" \
      version="1.0.0"

WORKDIR /app

COPY target/wealth-search-1.0.0.jar app.jar

EXPOSE 8089

ENV TZ=Asia/Shanghai

ENTRYPOINT ["java", "-Xms128m", "-Xmx128m", "-XX:+UseContainerSupport", "-XX:+ExitOnOutOfMemoryError", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
