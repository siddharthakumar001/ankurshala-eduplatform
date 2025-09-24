# syntax=docker/dockerfile:1.6
ARG APP_LABEL=com.ankurshala=1

FROM maven:3.9-eclipse-temurin-17 AS build
LABEL com.ankurshala=1
WORKDIR /src

# Copy sources and build
COPY . .
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests -Dmaven.test.skip=true clean package

FROM eclipse-temurin:17-jre
LABEL com.ankurshala=1
WORKDIR /app
COPY --from=build /src/target/*.jar /app/app.jar

ENV APP_BUILD_SHA=${GIT_SHA:-dev} APP_BUILD_TIME=${BUILD_TIME:-dev}
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]