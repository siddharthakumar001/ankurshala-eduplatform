############################
# 1) Build stage (no Docker Hub)
############################
FROM public.ecr.aws/amazoncorretto/amazoncorretto:17 AS build
WORKDIR /src

# Install Maven 3.8+
RUN yum update -y && \
    yum install -y wget tar gzip && \
    wget https://archive.apache.org/dist/maven/maven-3/3.8.8/binaries/apache-maven-3.8.8-bin.tar.gz && \
    tar -xzf apache-maven-3.8.8-bin.tar.gz -C /opt && \
    ln -s /opt/apache-maven-3.8.8 /opt/maven && \
    echo 'export PATH=/opt/maven/bin:$PATH' >> /etc/profile && \
    rm apache-maven-3.8.8-bin.tar.gz && \
    yum clean all

# Warm up dependency cache
COPY pom.xml ./
# If you use a Maven wrapper, also:
# COPY .mvn/ .mvn/
# COPY mvnw .
RUN --mount=type=cache,target=/root/.m2 \
    /opt/maven/bin/mvn -q -DskipTests dependency:go-offline

# Build the JAR
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    /opt/maven/bin/mvn -q -Dmaven.test.skip=true clean package

############################
# 2) Runtime stage (non-root, minimal)
############################
# Option A (preferred): Distroless (very small, non-root by default)
FROM gcr.io/distroless/java17-debian12 AS runtime

# Option B (if gcr.io is blocked), comment A and uncomment B below
# FROM public.ecr.aws/amazoncorretto/amazoncorretto:17-alpine AS runtime
# RUN adduser -D -H appuser && mkdir -p /app && chown -R appuser:appuser /app
# USER appuser

WORKDIR /app

# Copy the fat jar
# Tip: if your artifact name is fixed, replace *.jar with the exact name to fail fast.
COPY --from=build /src/target/*.jar /app/app.jar

# Sensible JVM defaults for containers
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC -Dfile.encoding=UTF-8" \
    SERVER_PORT=8080 \
    SPRING_PROFILES_ACTIVE=local

EXPOSE 8080

# Distroless has no shell; call java directly
ENTRYPOINT ["/usr/bin/java","-jar","/app/app.jar"]
