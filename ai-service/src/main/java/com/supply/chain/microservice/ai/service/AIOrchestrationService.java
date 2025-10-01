package com.supply.chain.microservice.ai.service;

import com.supply.chain.microservice.ai.model.AIRequest;
import com.supply.chain.microservice.ai.model.AIResponse;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Main AI orchestration service using Facade pattern
 */
public interface AIOrchestrationService {
    
    Mono<AIResponse> processRequest(AIRequest request);
    
    List<String> getAvailableProviders();
    
    List<String> getSupportedRequestTypes();
    
    String getServiceHealth();
    
    Mono<AIResponse> processWithFallback(AIRequest request);
}
