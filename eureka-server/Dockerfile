FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="Supply Chain Team"
LABEL version="1.0.0"
LABEL description="Eureka Server for Supply Chain Microservices"

WORKDIR /app

COPY target/*.jar app.jar

RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

RUN chown -R appuser:appgroup /app

USER appuser

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8761/actuator/health || exit 1

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
