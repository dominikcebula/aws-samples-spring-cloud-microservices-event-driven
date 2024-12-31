FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/*.jar app.jar

ENTRYPOINT ["java", "-XX:InitialRAMPercentage=80.0", "-XX:MinRAMPercentage=80.0", "-XX:MaxRAMPercentage=80.0", "-jar", "app.jar"]
