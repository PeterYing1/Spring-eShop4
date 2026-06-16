# ─────────────────────────────────────────────────────────────────────────────
# Stage 1: build the webhooks-service JAR inside the multi-module Maven tree
# ─────────────────────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

# ── Copy parent and aggregator POMs first so Maven can resolve the reactor ──
COPY pom.xml                                        pom.xml
COPY common/pom.xml                                 common/pom.xml
COPY common/event-bus/pom.xml                       common/event-bus/pom.xml
COPY common/event-log/pom.xml                       common/event-log/pom.xml
COPY common/web-support/pom.xml                     common/web-support/pom.xml
COPY common/security/pom.xml                        common/security/pom.xml
COPY services/pom.xml                               services/pom.xml
COPY services/webhooks-service/pom.xml              services/webhooks-service/pom.xml

# Stub POMs for all other service modules so the reactor does not fail when
# Maven resolves the parent <modules> list. Only the POM is needed — no sources.
COPY services/catalog-service/pom.xml               services/catalog-service/pom.xml
COPY services/basket-service/pom.xml                services/basket-service/pom.xml
COPY services/identity-service/pom.xml              services/identity-service/pom.xml
COPY services/ordering-service/pom.xml              services/ordering-service/pom.xml
COPY services/ordering-background-service/pom.xml   services/ordering-background-service/pom.xml
COPY services/ordering-notification-service/pom.xml services/ordering-notification-service/pom.xml
COPY services/payment-service/pom.xml               services/payment-service/pom.xml
COPY services/marketing-service/pom.xml             services/marketing-service/pom.xml
COPY services/location-service/pom.xml              services/location-service/pom.xml

# ── Pre-fetch dependencies (layer cached until any POM changes) ──
RUN mvn dependency:go-offline \
        -pl services/webhooks-service \
        -am \
        -q

# ── Copy only the modules required to build webhooks-service ──
COPY common/event-bus/src       common/event-bus/src
COPY common/event-log/src       common/event-log/src
COPY common/web-support/src     common/web-support/src
COPY common/security/src        common/security/src
COPY services/webhooks-service/src services/webhooks-service/src

# ── Build (skip tests — run separately) ──
RUN mvn package \
        -pl services/webhooks-service \
        -am \
        -DskipTests \
        -q

# ─────────────────────────────────────────────────────────────────────────────
# Stage 2: minimal runtime image
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /workspace/services/webhooks-service/target/*.jar app.jar

# 5113 = HTTP REST / Swagger
# 9113 = management / debug port
EXPOSE 5113 9113

ENTRYPOINT ["java", "-jar", "app.jar"]
