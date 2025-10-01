package com.supply.chain.microservice.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * AI response model using the Builder pattern
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIResponse {
    
    private String requestId;
    private String responseId;
    private String content;
    private AIRequest.AIProvider provider;
    private ResponseStatus status;
    private String errorMessage;
    private Map<String, Object> metadata;
    private LocalDateTime timestamp;
    private Integer tokensUsed;
    private Long processingTimeMs;
    private Double confidence;
    
    public enum ResponseStatus {
        SUCCESS,
        PARTIAL_SUCCESS,
        FAILED,
        RATE_LIMITED,
        TIMEOUT
    }
}
