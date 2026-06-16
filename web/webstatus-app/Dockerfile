# ---- Build stage ----
FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /workspace

# Copy parent POM
COPY pom.xml ./pom.xml

# Copy web parent POM
COPY web/pom.xml ./web/pom.xml

# Copy module POM
COPY web/webstatus-app/pom.xml ./web/webstatus-app/pom.xml

# Download dependencies (cached layer when only POMs change)
RUN mvn -f pom.xml dependency:go-offline \
        -pl web/webstatus-app -am \
        -DskipTests --no-transfer-progress 2>/dev/null || true

# Copy sources
COPY web/webstatus-app/src ./web/webstatus-app/src

# Build
RUN mvn -f pom.xml package \
        -pl web/webstatus-app -am \
        -DskipTests --no-transfer-progress

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /workspace/web/webstatus-app/target/webstatus-app-*.jar app.jar

EXPOSE 5107

ENTRYPOINT ["java", "-jar", "app.jar"]
