# backend/Dockerfile
# Stage 1: build with Maven inside Docker (no local Maven required)
FROM maven:3.8.5-openjdk-17 AS builder
WORKDIR /home/app

# copy only what we need first for caching
COPY pom.xml .
COPY src ./src

# build the jar (skip tests for speed, remove -DskipTests to run tests)
RUN mvn -B -DskipTests clean package spring-boot:repackage

# Stage 2: slim runtime image
FROM openjdk:17-jdk-slim
WORKDIR /app

COPY --from=builder /home/app/target/*.jar ./app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
