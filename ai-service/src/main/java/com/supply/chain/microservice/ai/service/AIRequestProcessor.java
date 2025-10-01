package com.supply.chain.microservice.ai.service;

import com.supply.chain.microservice.ai.model.AIRequest;

/**
 * Chain of Responsibility pattern interface for AI request processing
 */
public interface AIRequestProcessor {
    
    void setNext(AIRequestProcessor next);
    
    AIRequestProcessor getNext();
    
    boolean canProcess(AIRequest request);
    
    AIRequest process(AIRequest request);
}
