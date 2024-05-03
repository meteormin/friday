FROM openjdk:17-jdk-slim AS deployment

ARG JAR_FILE

WORKDIR /app

EXPOSE 8080
COPY build/libs/$JAR_FILE app.jar

ENTRYPOINT ["java", "-jar","app.jar"]
