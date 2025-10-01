package com.supply.chain.microservice.productservice.service;

import com.supply.chain.microservice.productservice.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for publishing product-related events
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductEventService {

    private final RabbitTemplate rabbitTemplate;

    private static final String PRODUCT_EXCHANGE = "product.exchange";
    private static final String PRODUCT_CREATED_ROUTING_KEY = "product.created";
    private static final String PRODUCT_UPDATED_ROUTING_KEY = "product.updated";
    private static final String PRODUCT_DELETED_ROUTING_KEY = "product.deleted";
    private static final String PRODUCT_STATUS_CHANGED_ROUTING_KEY = "product.status.changed";

    /**
     * Publish product created event
     */
    public void publishProductCreatedEvent(Product product) {
        try {
            log.info("Publishing product created event for product ID: {}", product.getId());
            
            ProductEvent event = ProductEvent.builder()
                .eventType("PRODUCT_CREATED")
                .productId(product.getId())
                .productSku(product.getSku())
                .productName(product.getProductName())
                .categoryId(product.getCategory().getId())
                .status(product.getStatus().name())
                .timestamp(java.time.LocalDateTime.now())
                .build();
            
            rabbitTemplate.convertAndSend(PRODUCT_EXCHANGE, PRODUCT_CREATED_ROUTING_KEY, event);
            log.debug("Product created event published successfully for product ID: {}", product.getId());
            
        } catch (Exception e) {
            log.error("Failed to publish product created event for product ID: {}", product.getId(), e);
        }
    }

    /**
     * Publish product updated event
     */
    public void publishProductUpdatedEvent(Product updatedProduct, Product originalProduct) {
        try {
            log.info("Publishing product updated event for product ID: {}", updatedProduct.getId());
            
            ProductEvent event = ProductEvent.builder()
                .eventType("PRODUCT_UPDATED")
                .productId(updatedProduct.getId())
                .productSku(updatedProduct.getSku())
                .productName(updatedProduct.getProductName())
                .categoryId(updatedProduct.getCategory().getId())
                .status(updatedProduct.getStatus().name())
                .previousStatus(originalProduct.getStatus().name())
                .timestamp(java.time.LocalDateTime.now())
                .build();
            
            rabbitTemplate.convertAndSend(PRODUCT_EXCHANGE, PRODUCT_UPDATED_ROUTING_KEY, event);
            log.debug("Product updated event published successfully for product ID: {}", updatedProduct.getId());
            
        } catch (Exception e) {
            log.error("Failed to publish product updated event for product ID: {}", updatedProduct.getId(), e);
        }
    }

    /**
     * Publish product deleted event
     */
    public void publishProductDeletedEvent(Product product) {
        try {
            log.info("Publishing product deleted event for product ID: {}", product.getId());
            
            ProductEvent event = ProductEvent.builder()
                .eventType("PRODUCT_DELETED")
                .productId(product.getId())
                .productSku(product.getSku())
                .productName(product.getProductName())
                .categoryId(product.getCategory().getId())
                .status(product.getStatus().name())
                .timestamp(java.time.LocalDateTime.now())
                .build();
            
            rabbitTemplate.convertAndSend(PRODUCT_EXCHANGE, PRODUCT_DELETED_ROUTING_KEY, event);
            log.debug("Product deleted event published successfully for product ID: {}", product.getId());
            
        } catch (Exception e) {
            log.error("Failed to publish product deleted event for product ID: {}", product.getId(), e);
        }
    }

    /**
     * Publish product status changed event
     */
    public void publishProductStatusChangedEvent(Product product, String action) {
        try {
            log.info("Publishing product status changed event for product ID: {} with action: {}", 
                     product.getId(), action);
            
            ProductEvent event = ProductEvent.builder()
                .eventType("PRODUCT_STATUS_CHANGED")
                .productId(product.getId())
                .productSku(product.getSku())
                .productName(product.getProductName())
                .categoryId(product.getCategory().getId())
                .status(product.getStatus().name())
                .action(action)
                .timestamp(java.time.LocalDateTime.now())
                .build();
            
            rabbitTemplate.convertAndSend(PRODUCT_EXCHANGE, PRODUCT_STATUS_CHANGED_ROUTING_KEY, event);
            log.debug("Product status changed event published successfully for product ID: {}", product.getId());
            
        } catch (Exception e) {
            log.error("Failed to publish product status changed event for product ID: {}", product.getId(), e);
        }
    }

    /**
     * Product event DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ProductEvent {
        private String eventType;
        private Long productId;
        private String productSku;
        private String productName;
        private Long categoryId;
        private String status;
        private String previousStatus;
        private String action;
        private java.time.LocalDateTime timestamp;
    }
}
