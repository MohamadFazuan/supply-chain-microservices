package com.supply.chain.microservice.ai.service;

import com.supply.chain.microservice.ai.model.AIRequest;
import com.supply.chain.microservice.ai.model.AIResponse;
import reactor.core.publisher.Mono;

/**
 * Strategy pattern interface for different AI providers
 */
public interface AIProviderStrategy {
    
    Mono<AIResponse> processRequest(AIRequest request);
    
    AIRequest.AIProvider getProviderType();
    
    boolean supports(AIRequest.AIRequestType requestType);
    
    boolean isAvailable();
    
    int getPriority();
    
    String getHealthStatus();
}
