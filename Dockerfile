# Multi-stage build for Spring Boot application
FROM openjdk:21-slim as builder

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw* ./
COPY pom.xml .
COPY .mvn/ .mvn/

# Make Maven wrapper executable
RUN chmod +x ./mvnw

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src/ src/

# Build the application
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM openjdk:21-slim

# Create non-root user for security
RUN useradd --create-home --shell /bin/bash gymmate

# Set working directory
WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/target/gymmate-backend-*.jar app.jar

# Change ownership to non-root user
RUN chown -R gymmate:gymmate /app
USER gymmate

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
