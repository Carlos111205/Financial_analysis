# Multi-stage Dockerfile for Enterprise Revenue Recognition System
# Stage 1: Build
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /workspace

# Copy Maven files
COPY pom.xml ./
COPY src ./src

# Build JAR with Maven package
RUN apt-get update && apt-get install -y maven && mvn clean package -DskipTests=true

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Security: Non-root user
RUN groupadd -r appgroup && useradd -r -g appgroup -d /app appuser

# Copy artifact from builder stage
COPY --from=builder /workspace/target/revenue-recognition-system-1.0.0-SNAPSHOT.jar app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
