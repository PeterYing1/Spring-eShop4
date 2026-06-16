# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

# Copy parent POM
COPY pom.xml .

# Copy web parent POM
COPY web/pom.xml web/pom.xml

# Copy webmvc-app POM
COPY web/webmvc-app/pom.xml web/webmvc-app/pom.xml

# Copy common modules that webmvc-app depends on (for dependency resolution)
COPY common/pom.xml common/pom.xml
COPY common/web-support/pom.xml common/web-support/pom.xml
COPY common/security/pom.xml common/security/pom.xml

# Download dependencies (cache layer)
RUN mvn dependency:go-offline -pl web/webmvc-app -am -DskipTests --no-transfer-progress

# Copy all source
COPY common/ common/
COPY web/webmvc-app/src web/webmvc-app/src

# Build
RUN mvn package -pl web/webmvc-app -am -DskipTests --no-transfer-progress

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /workspace/web/webmvc-app/target/*.jar app.jar

EXPOSE 5100

ENTRYPOINT ["java", "-jar", "app.jar"]
