FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="renjianfeng8" \
      description="wealth-user - 鍓嶇鐢ㄦ埛绠＄悊鏈嶅姟锛堟敞鍐屻€佺櫥褰曘€佷釜浜轰俊鎭級" \
      version="1.0.0"

WORKDIR /app

COPY target/wealth-user-1.0.0.jar app.jar

EXPOSE 8083

ENV TZ=Asia/Shanghai

ENTRYPOINT ["java", "-jar", "app.jar"]
