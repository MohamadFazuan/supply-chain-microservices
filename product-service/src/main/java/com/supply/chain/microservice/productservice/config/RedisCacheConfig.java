package com.supply.chain.microservice.productservice.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis caching configuration for Product Service
 * Configures cache managers, serializers, and TTL settings
 */
@Configuration
@EnableCaching
@Slf4j
public class RedisCacheConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.database:0}")
    private int redisDatabase;

    /**
     * Redis connection factory configuration
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        config.setDatabase(redisDatabase);
        
        if (redisPassword != null && !redisPassword.trim().isEmpty()) {
            config.setPassword(redisPassword);
        }
        
        LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
        log.info("Redis connection factory configured for host: {}:{}, database: {}", 
                 redisHost, redisPort, redisDatabase);
        
        return factory;
    }

    /**
     * Redis template for manual cache operations
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Configure serializers
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = createJsonSerializer();
        
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.afterPropertiesSet();
        log.info("Redis template configured with JSON serialization");
        
        return template;
    }

    /**
     * Cache manager configuration with different TTL for different cache types
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = createCacheConfiguration(Duration.ofMinutes(30));
        
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Product cache - 30 minutes TTL
        cacheConfigurations.put("products", createCacheConfiguration(Duration.ofMinutes(30)));
        
        // Product search cache - 15 minutes TTL (searches change more frequently)
        cacheConfigurations.put("productSearch", createCacheConfiguration(Duration.ofMinutes(15)));
        
        // Category-based product cache - 60 minutes TTL
        cacheConfigurations.put("productsByCategory", createCacheConfiguration(Duration.ofHours(1)));
        
        // Featured products cache - 2 hours TTL (changes less frequently)
        cacheConfigurations.put("featuredProducts", createCacheConfiguration(Duration.ofHours(2)));
        
        // Similar products cache - 4 hours TTL
        cacheConfigurations.put("similarProducts", createCacheConfiguration(Duration.ofHours(4)));
        
        // Product availability cache - 5 minutes TTL (inventory changes frequently)
        cacheConfigurations.put("productAvailability", createCacheConfiguration(Duration.ofMinutes(5)));
        
        // Category cache - 24 hours TTL (categories change rarely)
        cacheConfigurations.put("categories", createCacheConfiguration(Duration.ofHours(24)));
        
        // Inventory cache - 10 minutes TTL
        cacheConfigurations.put("inventory", createCacheConfiguration(Duration.ofMinutes(10)));
        
        RedisCacheManager cacheManager = RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .transactionAware()
            .build();
        
        log.info("Redis cache manager configured with {} cache configurations", cacheConfigurations.size());
        return cacheManager;
    }

    /**
     * Create cache configuration with specified TTL
     */
    private RedisCacheConfiguration createCacheConfiguration(Duration ttl) {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(ttl)
            .serializeKeysWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                .fromSerializer(createJsonSerializer()))
            .disableCachingNullValues();
    }

    /**
     * Create JSON serializer for Redis
     */
    private GenericJackson2JsonRedisSerializer createJsonSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.activateDefaultTyping(
            objectMapper.getPolymorphicTypeValidator(),
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.WRAPPER_ARRAY
        );
        
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    /**
     * Cache event logger for monitoring cache operations
     */
    @Bean
    public CacheEventLogger cacheEventLogger() {
        return new CacheEventLogger();
    }

    /**
     * Cache statistics and event logging
     */
    public static class CacheEventLogger {
        
        public void logCacheHit(String cacheName, Object key) {
            log.debug("Cache HIT - Cache: {}, Key: {}", cacheName, key);
        }
        
        public void logCacheMiss(String cacheName, Object key) {
            log.debug("Cache MISS - Cache: {}, Key: {}", cacheName, key);
        }
        
        public void logCacheEviction(String cacheName, Object key) {
            log.info("Cache EVICTION - Cache: {}, Key: {}", cacheName, key);
        }
    }
}
