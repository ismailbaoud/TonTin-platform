# Multi-stage build for optimized image size
# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Copy Maven wrapper and pom.xml first for better layer caching
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (cached if pom.xml hasn't changed)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application (skip tests for faster builds, run tests in CI/CD)
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the built artifact from the build stage
COPY --from=build /app/target/platform-0.0.1-SNAPSHOT.war app.war

# Expose the application port
EXPOSE 9090

# Use environment variables for configuration
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/tontin_test
ENV SPRING_DATASOURCE_USERNAME=happy
ENV SPRING_DATASOURCE_PASSWORD=happy
ENV SPRING_MAIL_HOST=smtp.gmail.com
ENV SPRING_MAIL_PORT=587
ENV SPRING_MAIL_USERNAME=""
ENV SPRING_MAIL_PASSWORD=""

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:9090/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.war"]
