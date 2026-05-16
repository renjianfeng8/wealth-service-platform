FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="renjianfeng8" \
      description="wealth-trade - 浜ゆ槗濮旀墭鏈嶅姟锛堝鎵樹笅鍗曘€佽鍗曠鐞嗭級" \
      version="1.0.0"

WORKDIR /app

COPY target/wealth-trade-1.0.0.jar app.jar

EXPOSE 8085

ENV TZ=Asia/Shanghai

ENTRYPOINT ["java", "-jar", "app.jar"]
