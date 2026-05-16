FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="renjianfeng8" \
      description="wealth-search - 鎼滅储鏈嶅姟锛堝熀浜嶦lasticSearch鐨勪骇鍝佸叏鏂囨悳绱級" \
      version="1.0.0"

WORKDIR /app

COPY target/wealth-search-1.0.0.jar app.jar

EXPOSE 8089

ENV TZ=Asia/Shanghai

ENTRYPOINT ["java", "-jar", "app.jar"]
