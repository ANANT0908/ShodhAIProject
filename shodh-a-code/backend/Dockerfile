# Stage 1 - build the Spring Boot fat/executable jar
FROM maven:3.9.0-eclipse-temurin-17 AS build
WORKDIR /src

# Copy Maven files first (better cache)
COPY pom.xml .
# If you have mvnw or .mvn, copy them too:
# COPY .mvn .mvn
# COPY mvnw .

# Pre-download dependencies
RUN mvn -B -DskipTests dependency:go-offline

# Copy sources and build executable jar
COPY src ./src

# Ensure spring-boot repackage runs to create an executable jar with Main manifest
RUN mvn -B -DskipTests clean package spring-boot:repackage

# Optional: show the manifest in build logs to help debugging (won't be in final image)
RUN ls -la target/*.jar || true
RUN unzip -p target/*.jar META-INF/MANIFEST.MF || true

# Stage 2 - runtime image with docker CLI installed (Debian-based)
FROM eclipse-temurin:17-jre-jammy

# Install docker CLI (docker.io) and minimal tools
RUN apt-get update \
 && apt-get install -y --no-install-recommends \
        ca-certificates \
        curl \
        gnupg \
        lsb-release \
        docker.io \
 && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy built jar from the build stage. This expects exactly one jar in target/.
COPY --from=build /src/target/*.jar /app/app.jar

# Small check at container build time — optional but helpful diagnostics
RUN [ -f /app/app.jar ] || (echo "ERROR: app.jar missing" && false)
# (Don't try to run the jar here; we just want it present and package correct.)

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
