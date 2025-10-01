package com.supply.chain.microservice.productservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for Product Service
 * Configures exchanges, queues, bindings, and message templates
 */
@Configuration
@EnableRabbit
@Slf4j
public class RabbitMQConfig {

    // Exchange names
    public static final String PRODUCT_EXCHANGE = "product.exchange";
    public static final String INVENTORY_EXCHANGE = "inventory.exchange";
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";

    // Queue names
    public static final String PRODUCT_CREATED_QUEUE = "product.created.queue";
    public static final String PRODUCT_UPDATED_QUEUE = "product.updated.queue";
    public static final String PRODUCT_DELETED_QUEUE = "product.deleted.queue";
    public static final String PRODUCT_STATUS_CHANGED_QUEUE = "product.status.changed.queue";
    
    public static final String INVENTORY_LOW_STOCK_QUEUE = "inventory.low.stock.queue";
    public static final String INVENTORY_UPDATED_QUEUE = "inventory.updated.queue";
    
    public static final String NOTIFICATION_EMAIL_QUEUE = "notification.email.queue";
    public static final String NOTIFICATION_SMS_QUEUE = "notification.sms.queue";

    // Routing keys
    public static final String PRODUCT_CREATED_ROUTING_KEY = "product.created";
    public static final String PRODUCT_UPDATED_ROUTING_KEY = "product.updated";
    public static final String PRODUCT_DELETED_ROUTING_KEY = "product.deleted";
    public static final String PRODUCT_STATUS_CHANGED_ROUTING_KEY = "product.status.changed";
    
    public static final String INVENTORY_LOW_STOCK_ROUTING_KEY = "inventory.low.stock";
    public static final String INVENTORY_UPDATED_ROUTING_KEY = "inventory.updated";
    
    public static final String NOTIFICATION_EMAIL_ROUTING_KEY = "notification.email";
    public static final String NOTIFICATION_SMS_ROUTING_KEY = "notification.sms";

    /**
     * JSON message converter for RabbitMQ
     */
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        log.info("JSON message converter configured for RabbitMQ");
        return converter;
    }

    /**
     * RabbitMQ template configuration
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                       Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setMandatory(true);
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.debug("Message delivered successfully: {}", correlationData);
            } else {
                log.error("Message delivery failed: {}, cause: {}", correlationData, cause);
            }
        });
        template.setReturnsCallback(returned -> {
            log.error("Message returned: {}, reply code: {}, reply text: {}, exchange: {}, routing key: {}",
                     returned.getMessage(), returned.getReplyCode(), returned.getReplyText(),
                     returned.getExchange(), returned.getRoutingKey());
        });
        
        log.info("RabbitMQ template configured with JSON converter and callbacks");
        return template;
    }

    /**
     * Rabbit listener container factory
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        factory.setPrefetchCount(10);
        factory.setDefaultRequeueRejected(false);
        
        log.info("Rabbit listener container factory configured");
        return factory;
    }

    // ===============================
    // PRODUCT EXCHANGE AND QUEUES
    // ===============================

    /**
     * Product exchange
     */
    @Bean
    public TopicExchange productExchange() {
        TopicExchange exchange = new TopicExchange(PRODUCT_EXCHANGE);
        log.info("Product exchange created: {}", PRODUCT_EXCHANGE);
        return exchange;
    }

    /**
     * Product created queue
     */
    @Bean
    public Queue productCreatedQueue() {
        return QueueBuilder.durable(PRODUCT_CREATED_QUEUE)
            .withArgument("x-message-ttl", 86400000) // 24 hours TTL
            .withArgument("x-max-retries", 3)
            .build();
    }

    /**
     * Product updated queue
     */
    @Bean
    public Queue productUpdatedQueue() {
        return QueueBuilder.durable(PRODUCT_UPDATED_QUEUE)
            .withArgument("x-message-ttl", 86400000) // 24 hours TTL
            .withArgument("x-max-retries", 3)
            .build();
    }

    /**
     * Product deleted queue
     */
    @Bean
    public Queue productDeletedQueue() {
        return QueueBuilder.durable(PRODUCT_DELETED_QUEUE)
            .withArgument("x-message-ttl", 86400000) // 24 hours TTL
            .withArgument("x-max-retries", 3)
            .build();
    }

    /**
     * Product status changed queue
     */
    @Bean
    public Queue productStatusChangedQueue() {
        return QueueBuilder.durable(PRODUCT_STATUS_CHANGED_QUEUE)
            .withArgument("x-message-ttl", 86400000) // 24 hours TTL
            .withArgument("x-max-retries", 3)
            .build();
    }

    // Product exchange bindings
    @Bean
    public Binding productCreatedBinding() {
        return BindingBuilder.bind(productCreatedQueue())
            .to(productExchange())
            .with(PRODUCT_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding productUpdatedBinding() {
        return BindingBuilder.bind(productUpdatedQueue())
            .to(productExchange())
            .with(PRODUCT_UPDATED_ROUTING_KEY);
    }

    @Bean
    public Binding productDeletedBinding() {
        return BindingBuilder.bind(productDeletedQueue())
            .to(productExchange())
            .with(PRODUCT_DELETED_ROUTING_KEY);
    }

    @Bean
    public Binding productStatusChangedBinding() {
        return BindingBuilder.bind(productStatusChangedQueue())
            .to(productExchange())
            .with(PRODUCT_STATUS_CHANGED_ROUTING_KEY);
    }

    // ===============================
    // INVENTORY EXCHANGE AND QUEUES
    // ===============================

    /**
     * Inventory exchange
     */
    @Bean
    public TopicExchange inventoryExchange() {
        TopicExchange exchange = new TopicExchange(INVENTORY_EXCHANGE);
        log.info("Inventory exchange created: {}", INVENTORY_EXCHANGE);
        return exchange;
    }

    /**
     * Low stock notification queue
     */
    @Bean
    public Queue inventoryLowStockQueue() {
        return QueueBuilder.durable(INVENTORY_LOW_STOCK_QUEUE)
            .withArgument("x-message-ttl", 86400000) // 24 hours TTL
            .withArgument("x-max-retries", 5) // More retries for inventory alerts
            .build();
    }

    /**
     * Inventory updated queue
     */
    @Bean
    public Queue inventoryUpdatedQueue() {
        return QueueBuilder.durable(INVENTORY_UPDATED_QUEUE)
            .withArgument("x-message-ttl", 3600000) // 1 hour TTL
            .withArgument("x-max-retries", 3)
            .build();
    }

    // Inventory exchange bindings
    @Bean
    public Binding inventoryLowStockBinding() {
        return BindingBuilder.bind(inventoryLowStockQueue())
            .to(inventoryExchange())
            .with(INVENTORY_LOW_STOCK_ROUTING_KEY);
    }

    @Bean
    public Binding inventoryUpdatedBinding() {
        return BindingBuilder.bind(inventoryUpdatedQueue())
            .to(inventoryExchange())
            .with(INVENTORY_UPDATED_ROUTING_KEY);
    }

    // ===============================
    // NOTIFICATION EXCHANGE AND QUEUES
    // ===============================

    /**
     * Notification exchange
     */
    @Bean
    public TopicExchange notificationExchange() {
        TopicExchange exchange = new TopicExchange(NOTIFICATION_EXCHANGE);
        log.info("Notification exchange created: {}", NOTIFICATION_EXCHANGE);
        return exchange;
    }

    /**
     * Email notification queue
     */
    @Bean
    public Queue notificationEmailQueue() {
        return QueueBuilder.durable(NOTIFICATION_EMAIL_QUEUE)
            .withArgument("x-message-ttl", 3600000) // 1 hour TTL
            .withArgument("x-max-retries", 5)
            .build();
    }

    /**
     * SMS notification queue
     */
    @Bean
    public Queue notificationSmsQueue() {
        return QueueBuilder.durable(NOTIFICATION_SMS_QUEUE)
            .withArgument("x-message-ttl", 1800000) // 30 minutes TTL
            .withArgument("x-max-retries", 3)
            .build();
    }

    // Notification exchange bindings
    @Bean
    public Binding notificationEmailBinding() {
        return BindingBuilder.bind(notificationEmailQueue())
            .to(notificationExchange())
            .with(NOTIFICATION_EMAIL_ROUTING_KEY);
    }

    @Bean
    public Binding notificationSmsBinding() {
        return BindingBuilder.bind(notificationSmsQueue())
            .to(notificationExchange())
            .with(NOTIFICATION_SMS_ROUTING_KEY);
    }

    // ===============================
    // DEAD LETTER QUEUE CONFIGURATION
    // ===============================

    /**
     * Dead letter exchange for failed messages
     */
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange("dlx.exchange");
    }

    /**
     * Dead letter queue
     */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable("dlq.product.service")
            .withArgument("x-message-ttl", 86400000) // Keep dead letters for 24 hours
            .build();
    }

    /**
     * Dead letter binding
     */
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
            .to(deadLetterExchange())
            .with("dlq");
    }
}
