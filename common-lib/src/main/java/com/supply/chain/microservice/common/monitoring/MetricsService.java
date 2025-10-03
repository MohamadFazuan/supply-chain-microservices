package com.supply.chain.microservice.common.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Custom metrics service for business metrics tracking
 * Add to: common-lib/src/main/java/com/supply/chain/microservice/common/monitoring/
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsService {

    private final MeterRegistry meterRegistry;
    private final ConcurrentMap<String, Counter> counters = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Timer> timers = new ConcurrentHashMap<>();

    // Business Metrics
    public void incrementProductCreated() {
        getCounter("supply.chain.products.created").increment();
    }

    public void incrementOrderProcessed(String orderType) {
        getCounter("supply.chain.orders.processed", "type", orderType).increment();
    }

    public void incrementInventoryUpdate(String operation) {
        getCounter("supply.chain.inventory.updates", "operation", operation).increment();
    }

    public void recordAuthenticationAttempt(boolean success) {
        String status = success ? "success" : "failure";
        getCounter("supply.chain.auth.attempts", "status", status).increment();
    }

    public void recordDatabaseQueryTime(String operation, Duration duration) {
        getTimer("supply.chain.database.query.time", "operation", operation)
                .record(duration);
    }

    public void recordExternalServiceCall(String service, boolean success, Duration duration) {
        String status = success ? "success" : "failure";
        getCounter("supply.chain.external.calls", "service", service, "status", status).increment();
        getTimer("supply.chain.external.calls.time", "service", service)
                .record(duration);
    }

    public void recordCacheHit(String cacheName, boolean hit) {
        String status = hit ? "hit" : "miss";
        getCounter("supply.chain.cache.access", "cache", cacheName, "result", status).increment();
    }

    public void recordBusinessTransaction(String transactionType, String status, double amount) {
        getCounter("supply.chain.business.transactions", "type", transactionType, "status", status).increment();
        meterRegistry.gauge("supply.chain.business.transaction.amount", amount);
    }

    public void recordUserAction(String action, String userId) {
        getCounter("supply.chain.user.actions", "action", action, "user", userId).increment();
    }

    public void recordSystemHealth(String component, boolean healthy) {
        String status = healthy ? "healthy" : "unhealthy";
        meterRegistry.gauge("supply.chain.system.health", 
            Tags.of("component", component, "status", status), 
            healthy ? 1 : 0);
    }

    // AI Service Specific Metrics
    public void recordAIRequest(String provider, String requestType) {
        getCounter("supply.chain.ai.requests", "provider", provider, "type", requestType).increment();
    }

    public void recordAIResponse(String provider, boolean success, Duration responseTime, int tokensUsed) {
        String status = success ? "success" : "failure";
        getCounter("supply.chain.ai.responses", "provider", provider, "status", status).increment();
        getTimer("supply.chain.ai.response.time", "provider", provider).record(responseTime);
        meterRegistry.gauge("supply.chain.ai.tokens.used", 
            Tags.of("provider", provider), 
            tokensUsed);
    }

    // Security Metrics
    public void recordSecurityEvent(String eventType, String severity) {
        getCounter("supply.chain.security.events", "type", eventType, "severity", severity).increment();
    }

    public void recordFailedLogin(String reason) {
        getCounter("supply.chain.security.failed.logins", "reason", reason).increment();
    }

    // Helper methods
    private Counter getCounter(String name, String... tags) {
        String key = name + String.join(",", tags);
        return counters.computeIfAbsent(key, k -> 
            Counter.builder(name)
                   .tags(tags)
                   .description("Custom business metric for " + name)
                   .register(meterRegistry));
    }

    private Timer getTimer(String name, String... tags) {
        String key = name + String.join(",", tags);
        return timers.computeIfAbsent(key, k -> 
            Timer.builder(name)
                 .tags(tags)
                 .description("Custom timer metric for " + name)
                 .register(meterRegistry));
    }

    // Batch metrics recording
    public void recordBatchMetrics(String operation, int batchSize, Duration duration, int successCount, int failureCount) {
        getCounter("supply.chain.batch.operations", "operation", operation).increment();
        meterRegistry.gauge("supply.chain.batch.size", 
            Tags.of("operation", operation), 
            batchSize);
        getTimer("supply.chain.batch.duration", "operation", operation).record(duration);
        meterRegistry.gauge("supply.chain.batch.success.count", 
            Tags.of("operation", operation), 
            successCount);
        meterRegistry.gauge("supply.chain.batch.failure.count", 
            Tags.of("operation", operation), 
            failureCount);
    }
}
