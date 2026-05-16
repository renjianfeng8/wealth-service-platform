FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="renjianfeng8" \
      description="wealth-message - 娑堟伅鏈嶅姟锛堣储缁忚祫璁€佺珯鍐呮秷鎭帹閫侊級" \
      version="1.0.0"

WORKDIR /app

COPY target/wealth-message-1.0.0.jar app.jar

EXPOSE 8087

ENV TZ=Asia/Shanghai

ENTRYPOINT ["java", "-jar", "app.jar"]
