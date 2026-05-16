FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="renjianfeng8" \
      description="wealth-system - 鍚庡彴鏉冮檺绠＄悊鏈嶅姟锛圧BAC銆丣WT鐧诲綍锛? \
      version="1.0.0"

WORKDIR /app

COPY target/wealth-system-1.0.0.jar app.jar

EXPOSE 8082

ENV TZ=Asia/Shanghai

ENTRYPOINT ["java", "-jar", "app.jar"]
