FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="renjianfeng8" \
      description="wealth-product - 浜у搧涓庤鎯呮湇鍔★紙浜у搧绠＄悊銆佸疄鏃惰鎯咃級" \
      version="1.0.0"

WORKDIR /app

COPY target/wealth-product-1.0.0.jar app.jar

EXPOSE 8084

ENV TZ=Asia/Shanghai

ENTRYPOINT ["java", "-jar", "app.jar"]
