# =============================================================================
# Multi-stage Maven build for ordering-service
# =============================================================================

# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

# Copy parent POM files first (better layer caching)
COPY pom.xml .
COPY common/pom.xml common/pom.xml
COPY common/event-bus/pom.xml common/event-bus/pom.xml
COPY common/event-log/pom.xml common/event-log/pom.xml
COPY common/web-support/pom.xml common/web-support/pom.xml
COPY common/security/pom.xml common/security/pom.xml
COPY services/pom.xml services/pom.xml
COPY services/ordering-service/pom.xml services/ordering-service/pom.xml

# Resolve dependencies (cached unless POMs change)
RUN mvn dependency:go-offline -pl services/ordering-service -am -q

# Copy sources and build
COPY common/ common/
COPY services/ordering-service/ services/ordering-service/

RUN mvn package -pl services/ordering-service -am -DskipTests -q

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Expose HTTP and management ports
EXPOSE 5102 9102

# Copy the built JAR
COPY --from=build /workspace/services/ordering-service/target/ordering-service-*.jar app.jar

# Use non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

ENTRYPOINT ["java", "-jar", "app.jar"]
