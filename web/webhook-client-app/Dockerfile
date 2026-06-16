# ---- Build stage ----
FROM eclipse-temurin:21-jdk-jammy AS build

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
