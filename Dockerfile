# Build stage
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Build application
COPY src ./src
ARG CACHEBUST=2026100902
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Ensure CA certificates are present for TLS/SSL connections
RUN apt-get update && apt-get install -y --no-install-recommends ca-certificates && rm -rf /var/lib/apt/lists/*

# Create non-root system user
RUN useradd -m -u 1001 -s /bin/bash appuser

# Copy built artifact
COPY --from=builder /build/target/pfm.jar app.jar

# Set permissions
RUN chown -R appuser:appuser /app
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
