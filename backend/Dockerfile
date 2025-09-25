# syntax=docker/dockerfile:1.6
# Multi-stage build for production-grade Spring Boot application

############################
# Stage 1: Build (Maven + Java 17)
############################
FROM maven:3.9.6-eclipse-temurin-17 AS build
LABEL com.ankurshala=1

# Set working directory
WORKDIR /app

# Copy only pom.xml first for better layer caching
COPY pom.xml ./
COPY .mvn ./.mvn
COPY mvnw ./

# Download dependencies (this layer will be cached unless pom.xml changes)
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests for faster builds)
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw clean package -DskipTests -Dmaven.test.skip=true -B

############################
# Stage 2: Runtime (Minimal JRE)
############################
FROM eclipse-temurin:17-jre AS runtime
LABEL com.ankurshala=1

# Create non-root user for security
RUN groupadd -g 1001 appgroup && \
    useradd -u 1001 -g appgroup -s /bin/bash appuser

# Set working directory
WORKDIR /app

# Copy the JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Environment variables for production
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+UseStringDeduplication -Dfile.encoding=UTF-8" \
    SPRING_PROFILES_ACTIVE=production \
    SERVER_PORT=8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/actuator/health || exit 1

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]