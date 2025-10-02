# syntax=docker/dockerfile:1.6
# Multi-stage build for production-grade Spring Boot application

############################
# Stage 1: Build (Maven + Java 17)
############################
FROM maven:3.9.6-eclipse-temurin-17 AS build
LABEL com.ankurshala=1

WORKDIR /app

# Cache deps with retry mechanism
COPY pom.xml ./
COPY .mvn ./.mvn
COPY mvnw ./
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw dependency:resolve -B || \
    (echo "First attempt failed, retrying..." && sleep 5 && ./mvnw dependency:resolve -B) || \
    (echo "Second attempt failed, trying without cache..." && ./mvnw dependency:resolve -B -U)

# Build with retry mechanism
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw clean package -DskipTests -Dmaven.test.skip=true -B || \
    (echo "Build failed, retrying without cache..." && ./mvnw clean package -DskipTests -Dmaven.test.skip=true -B -U)

############################
# Stage 2: Runtime (Minimal JRE)
############################
FROM eclipse-temurin:17-jre AS runtime
LABEL com.ankurshala=1

# Non-root user
RUN groupadd -g 1001 appgroup && useradd -u 1001 -g appgroup -s /bin/bash appuser

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN chown -R appuser:appgroup /app

# Install wget for healthcheck
USER root
RUN apt-get update && apt-get install -y wget && rm -rf /var/lib/apt/lists/*
USER appuser

# Runtime config
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+UseStringDeduplication -Dfile.encoding=UTF-8" \
    SPRING_PROFILES_ACTIVE=production \
    SERVER_PORT=8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/actuator/health || exit 1

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
