FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="Supply Chain Team"
LABEL version="1.0.0"
LABEL description="Config Server for Supply Chain Microservices"

# Create app directory
WORKDIR /app

# Copy the jar file
COPY target/*.jar app.jar

# Create a non-root user
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Change ownership of the app directory
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8888/actuator/health || exit 1

# Expose port
EXPOSE 8888

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
