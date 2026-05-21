FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY target/*.jar /app/app.jar

EXPOSE 8080

ENV JAVA_OPTS=""
ENV TZ=Asia/Shanghai

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app/app.jar"]
