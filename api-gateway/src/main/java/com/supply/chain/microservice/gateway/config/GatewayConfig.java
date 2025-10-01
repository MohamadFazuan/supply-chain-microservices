package com.supply.chain.microservice.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth Service Routes
                .route("auth-service", r -> r.path("/api/auth/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .circuitBreaker(c -> c.setName("auth-service-cb"))
                                .retry(retryConfig -> retryConfig.setRetries(3)))
                        .uri("lb://auth-service"))
                
                // Product Service Routes
                .route("product-service", r -> r.path("/api/products/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .circuitBreaker(c -> c.setName("product-service-cb"))
                                .requestRateLimiter(rl -> rl
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(exchange -> 
                                            exchange.getRequest().getHeaders().getFirst("X-User-Username") != null ?
                                            Mono.just(exchange.getRequest().getHeaders().getFirst("X-User-Username")) :
                                            Mono.just("anonymous"))))
                        .uri("lb://product-service"))
                
                // Credential Service Routes
                .route("credential-service", r -> r.path("/api/credentials/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .circuitBreaker(c -> c.setName("credential-service-cb")))
                        .uri("lb://credential-service"))
                
                // Health Check Routes
                .route("health-check", r -> r.path("/health")
                        .uri("lb://eureka-server/actuator/health"))
                
                .build();
    }

    @Bean
    public org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter redisRateLimiter() {
        return new org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter(10, 20, 1);
    }
}
