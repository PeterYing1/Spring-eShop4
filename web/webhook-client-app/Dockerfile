# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

# Copy parent POM
COPY pom.xml ./pom.xml

# Copy web parent POM
COPY web/pom.xml ./web/pom.xml

# Copy common modules that webhook-client-app depends on
COPY common/pom.xml ./common/pom.xml
COPY common/security/pom.xml ./common/security/pom.xml

# Copy module POM
COPY web/webhook-client-app/pom.xml ./web/webhook-client-app/pom.xml

# Stub POMs for common, services, gateways, sibling web apps, and tests so the root reactor resolves
COPY common/event-bus/pom.xml                            common/event-bus/pom.xml
COPY common/event-log/pom.xml                            common/event-log/pom.xml
COPY common/web-support/pom.xml                          common/web-support/pom.xml
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
COPY web/webspa-host/pom.xml                             web/webspa-host/pom.xml
COPY web/webstatus-app/pom.xml                           web/webstatus-app/pom.xml
COPY tests/pom.xml                                       tests/pom.xml
COPY tests/application-acceptance-tests/pom.xml          tests/application-acceptance-tests/pom.xml

# Download dependencies (cached layer when only POMs change)
RUN mvn -f pom.xml dependency:go-offline \
        -pl web/webhook-client-app -am \
        -DskipTests --no-transfer-progress 2>/dev/null || true

# Copy common module sources
COPY common/security/src ./common/security/src

# Copy application sources
COPY web/webhook-client-app/src ./web/webhook-client-app/src

# Build
RUN mvn -f pom.xml package \
        -pl web/webhook-client-app -am \
        -DskipTests --no-transfer-progress

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /workspace/web/webhook-client-app/target/webhook-client-app-*.jar app.jar

EXPOSE 5114

ENTRYPOINT ["java", "-jar", "app.jar"]
