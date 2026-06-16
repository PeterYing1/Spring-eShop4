# ── Stage 1: build ──────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /workspace

# Copy root POM
COPY pom.xml .

# Copy services aggregator POM
COPY services/pom.xml services/

# Copy common module POMs (dependency resolution before source copy)
COPY common/pom.xml common/
COPY common/event-bus/pom.xml common/event-bus/
COPY common/event-log/pom.xml common/event-log/
COPY common/security/pom.xml common/security/
COPY common/web-support/pom.xml common/web-support/
COPY common/test-support/pom.xml common/test-support/

# Copy ordering-background-service POM
COPY services/ordering-background-service/pom.xml services/ordering-background-service/

# Stub POMs for sibling services, gateways, web, and tests so the root reactor resolves
COPY services/catalog-service/pom.xml               services/catalog-service/pom.xml
COPY services/basket-service/pom.xml                services/basket-service/pom.xml
COPY services/identity-service/pom.xml              services/identity-service/pom.xml
COPY services/ordering-service/pom.xml              services/ordering-service/pom.xml
COPY services/ordering-notification-service/pom.xml services/ordering-notification-service/pom.xml
COPY services/payment-service/pom.xml               services/payment-service/pom.xml
COPY services/marketing-service/pom.xml             services/marketing-service/pom.xml
COPY services/location-service/pom.xml              services/location-service/pom.xml
COPY services/webhooks-service/pom.xml              services/webhooks-service/pom.xml
COPY gateways/pom.xml                               gateways/pom.xml
COPY gateways/web-shopping-aggregator/pom.xml       gateways/web-shopping-aggregator/pom.xml
COPY gateways/mobile-shopping-aggregator/pom.xml    gateways/mobile-shopping-aggregator/pom.xml
COPY web/pom.xml                                    web/pom.xml
COPY web/webmvc-app/pom.xml                         web/webmvc-app/pom.xml
COPY web/webspa-host/pom.xml                        web/webspa-host/pom.xml
COPY web/webstatus-app/pom.xml                      web/webstatus-app/pom.xml
COPY web/webhook-client-app/pom.xml                 web/webhook-client-app/pom.xml
COPY tests/pom.xml                                  tests/pom.xml
COPY tests/application-acceptance-tests/pom.xml     tests/application-acceptance-tests/pom.xml

# Download dependencies (layer-cache friendly — runs only when POM changes)
RUN mvn dependency:go-offline -pl services/ordering-background-service -am -B --no-transfer-progress

# Copy common module sources
COPY common/event-bus/src common/event-bus/src
COPY common/event-log/src common/event-log/src
COPY common/security/src common/security/src
COPY common/web-support/src common/web-support/src

# Copy ordering-background-service sources
COPY services/ordering-background-service/src services/ordering-background-service/src

# Build (skip tests in Docker build; run them in CI pipeline)
RUN mvn package -pl services/ordering-background-service -am -B --no-transfer-progress -DskipTests

# ── Stage 2: runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Non-root user for security
RUN groupadd --system appgroup && useradd --system --gid appgroup appuser
USER appuser

COPY --from=build /workspace/services/ordering-background-service/target/*.jar app.jar

EXPOSE 5111 9111

ENTRYPOINT ["java", "-jar", "app.jar"]
