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
COPY common/test-support/pom.xml common/test-support/pom.xml
COPY services/pom.xml services/pom.xml
COPY services/ordering-service/pom.xml services/ordering-service/pom.xml

# Stub POMs for sibling services, gateways, web, and tests so the root reactor resolves
COPY services/catalog-service/pom.xml               services/catalog-service/pom.xml
COPY services/basket-service/pom.xml                services/basket-service/pom.xml
COPY services/identity-service/pom.xml              services/identity-service/pom.xml
COPY services/ordering-background-service/pom.xml   services/ordering-background-service/pom.xml
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
