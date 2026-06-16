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
