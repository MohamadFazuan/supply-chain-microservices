package com.supply.chain.microservice.ai.service.impl;

import com.supply.chain.microservice.ai.model.AIRequest;
import com.supply.chain.microservice.ai.service.AIRequestProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Request validation processor using Chain of Responsibility pattern
 */
@Component
@Slf4j
public class ValidationProcessor implements AIRequestProcessor {
    
    private AIRequestProcessor next;
    
    @Override
    public void setNext(AIRequestProcessor next) {
        this.next = next;
    }
    
    @Override
    public AIRequestProcessor getNext() {
        return next;
    }
    
    @Override
    public boolean canProcess(AIRequest request) {
        return true; // Always processes for validation
    }
    
    @Override
    public AIRequest process(AIRequest request) {
        log.debug("Validating AI request: {}", request.getRequestId());
        
        // Validate request
        if (request.getPrompt() == null || request.getPrompt().trim().isEmpty()) {
            throw new IllegalArgumentException("Prompt cannot be empty");
        }
        
        if (request.getType() == null) {
            throw new IllegalArgumentException("Request type is required");
        }
        
        // Set defaults if missing
        if (request.getRequestId() == null) {
            request.setRequestId(UUID.randomUUID().toString());
        }
        
        if (request.getMaxTokens() == null) {
            request.setMaxTokens(1000);
        }
        
        if (request.getTemperature() == null) {
            request.setTemperature(0.7);
        }
        
        log.debug("Request validation completed: {}", request.getRequestId());
        
        // Continue chain
        if (next != null && next.canProcess(request)) {
            return next.process(request);
        }
        
        return request;
    }
}
