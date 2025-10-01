package com.supply.chain.microservice.ai.service.impl;

import com.supply.chain.microservice.ai.model.AIRequest;
import com.supply.chain.microservice.ai.service.AIRequestProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Content enrichment processor using Chain of Responsibility pattern
 */
@Component
@Slf4j
public class ContentEnrichmentProcessor implements AIRequestProcessor {
    
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
        return request.getType() != null;
    }
    
    @Override
    public AIRequest process(AIRequest request) {
        log.debug("Enriching content for request: {}", request.getRequestId());
        
        // Add context-specific system prompts based on request type
        String systemPrompt = switch (request.getType()) {
            case DEMAND_FORECASTING -> 
                "You are an expert supply chain analyst specializing in demand forecasting. " +
                "Analyze the provided data and provide accurate demand predictions with confidence intervals.";
            
            case INVENTORY_OPTIMIZATION -> 
                "You are a supply chain optimization expert. Analyze inventory data and provide " +
                "recommendations for optimal stock levels, reorder points, and safety stock.";
            
            case SENTIMENT_ANALYSIS -> 
                "You are a sentiment analysis expert. Analyze the provided text and determine " +
                "the sentiment (positive, negative, neutral) with confidence scores.";
            
            case DOCUMENT_ANALYSIS -> 
                "You are a document analysis expert. Extract key information, summarize content, " +
                "and identify important entities from the provided document.";
            
            case TEXT_GENERATION -> 
                "You are a helpful AI assistant. Generate coherent, relevant, and informative " +
                "responses based on the user's prompt.";
            
            case TRANSLATION -> 
                "You are an expert translator. Provide accurate translations while maintaining " +
                "the original meaning and context.";
            
            case SUMMARIZATION -> 
                "You are a text summarization expert. Create concise, comprehensive summaries " +
                "that capture the key points of the provided content.";
            
            case CLASSIFICATION -> 
                "You are a classification expert. Categorize the provided content into appropriate " +
                "classes or categories with confidence scores.";
            
            default -> "You are a helpful AI assistant for supply chain management tasks.";
        };
        
        if (request.getSystemPrompt() == null || request.getSystemPrompt().isEmpty()) {
            request.setSystemPrompt(systemPrompt);
        }
        
        // Enhance prompt with supply chain context if needed
        if (isSupplyChainRelated(request.getType())) {
            String enhancedPrompt = enhanceWithSupplyChainContext(request.getPrompt());
            request.setPrompt(enhancedPrompt);
        }
        
        log.debug("Content enrichment completed for request: {}", request.getRequestId());
        
        // Continue chain
        if (next != null && next.canProcess(request)) {
            return next.process(request);
        }
        
        return request;
    }
    
    private boolean isSupplyChainRelated(AIRequest.AIRequestType type) {
        return type == AIRequest.AIRequestType.DEMAND_FORECASTING ||
               type == AIRequest.AIRequestType.INVENTORY_OPTIMIZATION;
    }
    
    private String enhanceWithSupplyChainContext(String originalPrompt) {
        return "In the context of supply chain management and logistics operations: " + originalPrompt;
    }
}
