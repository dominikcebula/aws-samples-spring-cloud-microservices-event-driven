FROM openjdk:21-jdk-slim

ENV JAVA_MEM_OPTS=""

WORKDIR /app

COPY target/*.jar app.jar

ENTRYPOINT java ${JAVA_MEM_OPTS} -jar app.jar
