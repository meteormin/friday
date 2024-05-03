FROM gradle:8.4 AS build

WORKDIR /home/gradle

COPY --chown=gradle:gradle build.gradle /home/gradle/build.gradle
COPY --chown=gradle:gradle settings.gradle /home/gradle/settings.gradle
COPY --chown=gradle:gradle src /home/gradle/src

RUN gradle build -x test

FROM openjdk:21-jdk-slim AS deployment

WORKDIR /app

ARG JAR_FILE

ENV JAR_FILE=$JAR_FILE
ENV JAVA_OPTS=""
ENV TZ=Asia/Seoul

RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

EXPOSE 8080

COPY --from=build /home/gradle/build /app

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar libs/$JAR_FILE"]
