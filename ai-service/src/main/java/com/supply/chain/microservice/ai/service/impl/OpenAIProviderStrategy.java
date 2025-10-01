package com.supply.chain.microservice.ai.service.impl;

import com.supply.chain.microservice.ai.model.AIRequest;
import com.supply.chain.microservice.ai.model.AIResponse;
import com.supply.chain.microservice.ai.service.AIProviderStrategy;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * OpenAI provider strategy implementation using the Strategy pattern
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAIProviderStrategy implements AIProviderStrategy {
    
    @Value("${ai.openai.api-key:}")
    private String apiKey;
    
    @Value("${ai.openai.model:gpt-3.5-turbo}")
    private String model;
    
    @Value("${ai.openai.enabled:false}")
    private boolean enabled;
    
    @Override
    public Mono<AIResponse> processRequest(AIRequest request) {
        return Mono.fromCallable(() -> {
            long startTime = System.currentTimeMillis();
            
            try {
                if (!enabled || apiKey.isEmpty()) {
                    return AIResponse.builder()
                            .requestId(request.getRequestId())
                            .responseId(UUID.randomUUID().toString())
                            .status(AIResponse.ResponseStatus.FAILED)
                            .errorMessage("OpenAI service not configured or disabled")
                            .provider(getProviderType())
                            .timestamp(LocalDateTime.now())
                            .processingTimeMs(System.currentTimeMillis() - startTime)
                            .build();
                }
                
                OpenAiService service = new OpenAiService(apiKey);
                
                List<ChatMessage> messages = List.of(
                        new ChatMessage(ChatMessageRole.SYSTEM.value(), 
                                request.getSystemPrompt() != null ? request.getSystemPrompt() : 
                                "You are a helpful AI assistant for supply chain management."),
                        new ChatMessage(ChatMessageRole.USER.value(), request.getPrompt())
                );
                
                ChatCompletionRequest chatRequest = ChatCompletionRequest.builder()
                        .model(model)
                        .messages(messages)
                        .maxTokens(request.getMaxTokens() != null ? request.getMaxTokens() : 1000)
                        .temperature(request.getTemperature() != null ? request.getTemperature() : 0.7)
                        .build();
                
                var completion = service.createChatCompletion(chatRequest);
                ChatCompletionChoice choice = completion.getChoices().get(0);
                
                return AIResponse.builder()
                        .requestId(request.getRequestId())
                        .responseId(UUID.randomUUID().toString())
                        .content(choice.getMessage().getContent())
                        .status(AIResponse.ResponseStatus.SUCCESS)
                        .provider(getProviderType())
                        .timestamp(LocalDateTime.now())
                        .tokensUsed((int) completion.getUsage().getTotalTokens())
                        .processingTimeMs(System.currentTimeMillis() - startTime)
                        .confidence(0.85) // Default confidence for OpenAI
                        .build();
                
            } catch (Exception e) {
                log.error("OpenAI processing failed for request: {}", request.getRequestId(), e);
                
                return AIResponse.builder()
                        .requestId(request.getRequestId())
                        .responseId(UUID.randomUUID().toString())
                        .status(AIResponse.ResponseStatus.FAILED)
                        .errorMessage(e.getMessage())
                        .provider(getProviderType())
                        .timestamp(LocalDateTime.now())
                        .processingTimeMs(System.currentTimeMillis() - startTime)
                        .build();
            }
        });
    }
    
    @Override
    public AIRequest.AIProvider getProviderType() {
        return AIRequest.AIProvider.OPENAI;
    }
    
    @Override
    public boolean supports(AIRequest.AIRequestType requestType) {
        return switch (requestType) {
            case TEXT_GENERATION, SENTIMENT_ANALYSIS, SUMMARIZATION, 
                 TRANSLATION, CLASSIFICATION -> true;
            case DEMAND_FORECASTING, INVENTORY_OPTIMIZATION, DOCUMENT_ANALYSIS -> false;
        };
    }
    
    @Override
    public boolean isAvailable() {
        return enabled && !apiKey.isEmpty();
    }
    
    @Override
    public int getPriority() {
        return 1; // High priority
    }
    
    @Override
    public String getHealthStatus() {
        if (!enabled) return "DISABLED";
        if (apiKey.isEmpty()) return "NOT_CONFIGURED";
        return "HEALTHY";
    }
}
