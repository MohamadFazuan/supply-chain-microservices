package com.supply.chain.microservice.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI request model using the Builder pattern
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIRequest {
    
    private String requestId;
    private AIRequestType type;
    private String prompt;
    private List<String> context;
    private Map<String, Object> parameters;
    private String userId;
    private String sessionId;
    private LocalDateTime timestamp;
    private AIProvider preferredProvider;
    private Integer maxTokens;
    private Double temperature;
    private String systemPrompt;
    
    public enum AIRequestType {
        TEXT_GENERATION,
        DEMAND_FORECASTING,
        INVENTORY_OPTIMIZATION,
        SENTIMENT_ANALYSIS,
        DOCUMENT_ANALYSIS,
        TRANSLATION,
        SUMMARIZATION,
        CLASSIFICATION
    }
    
    public enum AIProvider {
        OPENAI,
        AZURE_OPENAI,
        HUGGING_FACE,
        OLLAMA,
        ANTHROPIC
    }
}
