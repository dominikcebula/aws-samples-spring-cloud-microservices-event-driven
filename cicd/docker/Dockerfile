FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/*.jar app.jar

ENTRYPOINT ["java", "-XX:InitialRAMPercentage=70.0", "-XX:MinRAMPercentage=70.0", "-XX:MaxRAMPercentage=70.0", "-jar", "app.jar"]
