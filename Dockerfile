# Enterprise Accounting & Revenue Recognition System - Production Container
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Non-root user for enterprise production security
RUN groupadd -r appgroup && useradd -r -g appgroup -d /app appuser

# Copy pre-packaged spring boot fat jar
COPY target/*.jar app.jar

RUN chown appuser:appgroup app.jar
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
