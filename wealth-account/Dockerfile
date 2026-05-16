FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="renjianfeng8" \
      description="wealth-account - 鐢ㄦ埛鑷€夋湇鍔★紙鑷€変骇鍝佺鐞嗭級" \
      version="1.0.0"

WORKDIR /app

COPY target/wealth-account-1.0.0.jar app.jar

EXPOSE 8086

ENV TZ=Asia/Shanghai

ENTRYPOINT ["java", "-jar", "app.jar"]
