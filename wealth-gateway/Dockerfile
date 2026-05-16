FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="renjianfeng8" \
      description="wealth-gateway - API缃戝叧锛岀粺涓€璺敱杞彂涓庡叏灞€CORS" \
      version="1.0.0"

WORKDIR /app

COPY target/wealth-gateway-1.0.0.jar app.jar

EXPOSE 8080

ENV TZ=Asia/Shanghai

ENTRYPOINT ["java", "-jar", "app.jar"]
