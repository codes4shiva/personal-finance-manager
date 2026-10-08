# Build stage
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Build application
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Ensure up-to-date CA root certificates for SSL/TLS connections
RUN apk add --no-cache ca-certificates

# Create non-root system userEntity
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy built artifact
COPY --from=builder /build/target/pfm.jar app.jar

# Set permissions
RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
