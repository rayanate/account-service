# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /build/target/accountService-0.0.1-SNAPSHOT.jar app.jar

# Health check
HEALTHCHECK --interval=10s --timeout=5s --start-period=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8082/health || exit 1

# Expose port
EXPOSE 8082

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

