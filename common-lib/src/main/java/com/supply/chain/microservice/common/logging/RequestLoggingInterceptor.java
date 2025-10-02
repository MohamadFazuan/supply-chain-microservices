package com.supply.chain.microservice.common.logging;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.time.Duration;
import java.util.UUID;

/**
 * HTTP Request/Response interceptor for detailed request logging
 * Add to: common-lib/src/main/java/com/supply/chain/microservice/common/logging/
 */
@Component
@Slf4j
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final String START_TIME = "startTime";
    private static final String REQUEST_ID = "requestId";

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        String userAgent = request.getHeader("User-Agent");
        String clientIp = getClientIpAddress(request);
        String userId = request.getHeader("X-User-Id"); // From JWT
        
        // Set MDC for correlation
        MDC.put("requestId", requestId);
        MDC.put("userId", userId != null ? userId : "anonymous");
        MDC.put("clientIp", clientIp);
        
        request.setAttribute(START_TIME, Instant.now());
        request.setAttribute(REQUEST_ID, requestId);
        
        log.info("🌐 [HTTP] Incoming {} {} from {} | User-Agent: {} | User: {}", 
                request.getMethod(), 
                request.getRequestURI(), 
                clientIp,
                userAgent != null ? userAgent.substring(0, Math.min(userAgent.length(), 50)) : "unknown",
                userId != null ? userId : "anonymous");
        
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, 
                               @NonNull Object handler, @Nullable Exception ex) {
        try {
            Instant startTime = (Instant) request.getAttribute(START_TIME);
            String requestId = (String) request.getAttribute(REQUEST_ID);
            
            if (startTime != null) {
                Duration duration = Duration.between(startTime, Instant.now());
                int status = response.getStatus();
                String statusEmoji = getStatusEmoji(status);
                
                log.info("📊 [HTTP] {} {} {} - Status: {} | Duration: {}ms", 
                        statusEmoji,
                        request.getMethod(), 
                        request.getRequestURI(),
                        status,
                        duration.toMillis());
                
                if (ex != null) {
                    log.error("💥 [HTTP] Request {} failed with exception: {}", requestId, ex.getMessage(), ex);
                }
                
                // Log slow requests
                if (duration.toMillis() > 2000) {
                    log.warn("🐌 [SLOW REQUEST] {} {} took {}ms", request.getMethod(), request.getRequestURI(), duration.toMillis());
                }
            }
        } finally {
            // Clean up MDC
            MDC.remove("requestId");
            MDC.remove("userId");
            MDC.remove("clientIp");
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP"};
        
        for (String headerName : headerNames) {
            String ip = request.getHeader(headerName);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        
        return request.getRemoteAddr();
    }

    private String getStatusEmoji(int status) {
        if (status >= 200 && status < 300) return "✅";
        if (status >= 300 && status < 400) return "↩️";
        if (status >= 400 && status < 500) return "⚠️";
        if (status >= 500) return "❌";
        return "❓";
    }
}
