FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml /app/
COPY src /app/src/

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

COPY --from=builder /app/target/weather-sdk-1.0-SNAPSHOT.jar /app/weather-sdk.jar

ENTRYPOINT ["java", "-jar", "/app/weather-sdk.jar"]
