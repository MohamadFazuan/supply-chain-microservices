package com.supply.chain.microservice.productservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Product Service Application for Tiong Nam Logistics
 * 
 * This microservice manages:
 * - Warehouse storage units and capacity management
 * - Freight forwarding routes and services
 * - Transportation fleet tracking and scheduling
 * - Custom logistics package configurations
 * - Product categorization and pricing
 * - Real-time inventory and availability tracking
 * 
 * Features:
 * - Redis caching for frequently accessed product data
 * - RabbitMQ messaging for asynchronous updates
 * - Eureka service discovery registration
 * - Circuit breaker patterns for resilience
 * - Comprehensive audit logging
 * 
 * @author Supply Chain Team
 * @version 1.0.0
 * @since 2024-10-01
 */
@SpringBootApplication(scanBasePackages = {
    "com.supply.chain.microservice.productservice",
    "com.supply.chain.microservice.common"
})
@EnableFeignClients
@EnableCaching
@EnableAsync
@EnableJpaRepositories
@EnableJpaAuditing
@EnableTransactionManagement
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
