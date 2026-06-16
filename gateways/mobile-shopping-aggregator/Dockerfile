# ── Stage 1: build ──────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /workspace

# Copy root POM
COPY pom.xml .

# Copy gateways aggregator POM
COPY gateways/pom.xml gateways/

# Copy common module POMs (dependency resolution before source copy)
COPY common/pom.xml common/
COPY common/event-bus/pom.xml common/event-bus/
COPY common/event-log/pom.xml common/event-log/
COPY common/security/pom.xml common/security/
COPY common/web-support/pom.xml common/web-support/

# Copy mobile-shopping-aggregator POM
COPY gateways/mobile-shopping-aggregator/pom.xml gateways/mobile-shopping-aggregator/

# Download dependencies (layer-cache friendly — runs only when POM changes)
RUN mvn dependency:go-offline -pl gateways/mobile-shopping-aggregator -am -B --no-transfer-progress

# Copy common module sources
COPY common/event-bus/src common/event-bus/src
COPY common/event-log/src common/event-log/src
COPY common/security/src common/security/src
COPY common/web-support/src common/web-support/src

# Copy mobile-shopping-aggregator sources
COPY gateways/mobile-shopping-aggregator/src gateways/mobile-shopping-aggregator/src

# Build (skip tests in Docker build; run them in CI pipeline)
RUN mvn package -pl gateways/mobile-shopping-aggregator -am -B --no-transfer-progress -DskipTests

# ── Stage 2: runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Non-root user for security
RUN groupadd --system appgroup && useradd --system --gid appgroup appuser
USER appuser

COPY --from=build /workspace/gateways/mobile-shopping-aggregator/target/*.jar app.jar

EXPOSE 5122

ENTRYPOINT ["java", "-jar", "app.jar"]
