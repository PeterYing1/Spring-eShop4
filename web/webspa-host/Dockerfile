# =============================================================================
# WebSPA Host — multi-stage Docker build
# =============================================================================
#
# IMPORTANT — Angular build artifacts
# ------------------------------------
# Before building this image, the Angular SPA must be compiled and its output
# copied into the Spring Boot static-resource directory:
#
#   1. cd C:\Projects\eShopOnContainers\src\Web\WebSPA\Client
#   2. npm install
#   3. npm run build:prod
#   4. Copy dist/ contents → web/webspa-host/src/main/resources/static/
#
# Without this step the container serves the placeholder index.html.
# =============================================================================

# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /workspace

# Copy parent POM and the web aggregate POM first so Maven can resolve the
# reactor structure before pulling in source, allowing layer caching on
# dependency resolution.
COPY pom.xml                    ./pom.xml
COPY common/pom.xml             ./common/pom.xml
COPY web/pom.xml                ./web/pom.xml
COPY web/webspa-host/pom.xml    ./web/webspa-host/pom.xml

# Stub POMs for common, services, gateways, sibling web apps, and tests so the root reactor resolves
COPY common/event-bus/pom.xml                            common/event-bus/pom.xml
COPY common/event-log/pom.xml                            common/event-log/pom.xml
COPY common/web-support/pom.xml                          common/web-support/pom.xml
COPY common/security/pom.xml                             common/security/pom.xml
COPY common/test-support/pom.xml                         common/test-support/pom.xml
COPY services/pom.xml                                    services/pom.xml
COPY services/catalog-service/pom.xml                    services/catalog-service/pom.xml
COPY services/basket-service/pom.xml                     services/basket-service/pom.xml
COPY services/identity-service/pom.xml                   services/identity-service/pom.xml
COPY services/ordering-service/pom.xml                   services/ordering-service/pom.xml
COPY services/ordering-background-service/pom.xml        services/ordering-background-service/pom.xml
COPY services/ordering-notification-service/pom.xml      services/ordering-notification-service/pom.xml
COPY services/payment-service/pom.xml                    services/payment-service/pom.xml
COPY services/marketing-service/pom.xml                  services/marketing-service/pom.xml
COPY services/location-service/pom.xml                   services/location-service/pom.xml
COPY services/webhooks-service/pom.xml                   services/webhooks-service/pom.xml
COPY gateways/pom.xml                                    gateways/pom.xml
COPY gateways/web-shopping-aggregator/pom.xml            gateways/web-shopping-aggregator/pom.xml
COPY gateways/mobile-shopping-aggregator/pom.xml         gateways/mobile-shopping-aggregator/pom.xml
COPY web/webmvc-app/pom.xml                              web/webmvc-app/pom.xml
COPY web/webstatus-app/pom.xml                           web/webstatus-app/pom.xml
COPY web/webhook-client-app/pom.xml                      web/webhook-client-app/pom.xml
COPY tests/pom.xml                                       tests/pom.xml
COPY tests/application-acceptance-tests/pom.xml          tests/application-acceptance-tests/pom.xml

# Pre-fetch dependencies (cached unless a POM changes).
RUN mvn -B dependency:go-offline \
        -pl web/webspa-host \
        -am \
        -q

# Copy full source tree and build.
COPY . .
RUN mvn -B clean package \
        -pl web/webspa-host \
        -am \
        -DskipTests \
        -q

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy AS runtime

LABEL maintainer="eShop Team"
LABEL org.opencontainers.image.title="WebSPA Host"
LABEL org.opencontainers.image.description="Spring Boot host for the eShop Angular SPA"

WORKDIR /app

# Copy the fat jar produced by the build stage.
COPY --from=build /workspace/web/webspa-host/target/webspa-host-*.jar app.jar

EXPOSE 5104

ENTRYPOINT ["java", "-jar", "app.jar"]
