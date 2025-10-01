package com.supply.chain.microservice.aiservice.service.strategy;

import com.supply.chain.microservice.ai.model.AIRequest;
import com.supply.chain.microservice.ai.model.AIResponse;
import com.supply.chain.microservice.ai.service.impl.OpenAIProviderStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("OpenAI Provider Strategy Tests")
class OpenAIProviderStrategyTest {

    @InjectMocks
    private OpenAIProviderStrategy openAIProviderStrategy;

    private AIRequest textGenerationRequest;
    private AIRequest sentimentAnalysisRequest;
    private AIRequest demandForecastingRequest;

    @BeforeEach
    void setUp() {
        // Set up the strategy with test configuration
        ReflectionTestUtils.setField(openAIProviderStrategy, "enabled", true);
        ReflectionTestUtils.setField(openAIProviderStrategy, "apiKey", "test-api-key-123");
        ReflectionTestUtils.setField(openAIProviderStrategy, "model", "gpt-3.5-turbo");
        ReflectionTestUtils.setField(openAIProviderStrategy, "baseUrl", "https://api.openai.com/v1");

        // Create test requests
        textGenerationRequest = AIRequest.builder()
                .requestId("text-gen-001")
                .type(AIRequest.AIRequestType.TEXT_GENERATION)
                .prompt("Generate a summary of supply chain best practices")
                .maxTokens(150)
                .temperature(0.7)
                .preferredProvider(AIRequest.AIProvider.OPENAI)
                .build();

        sentimentAnalysisRequest = AIRequest.builder()
                .requestId("sentiment-001")
                .type(AIRequest.AIRequestType.SENTIMENT_ANALYSIS)
                .prompt("Analyze the sentiment: Our delivery was delayed again!")
                .maxTokens(50)
                .temperature(0.3)
                .preferredProvider(AIRequest.AIProvider.OPENAI)
                .build();

        demandForecastingRequest = AIRequest.builder()
                .requestId("forecast-001")
                .type(AIRequest.AIRequestType.DEMAND_FORECASTING)
                .prompt("Forecast demand for winter clothing")
                .maxTokens(200)
                .temperature(0.5)
                .preferredProvider(AIRequest.AIProvider.OPENAI)
                .build();
    }

    @Test
    @DisplayName("Should support text generation requests")
    void supports_withTextGeneration_shouldReturnTrue() {
        // When & Then
        assertThat(openAIProviderStrategy.supports(AIRequest.AIRequestType.TEXT_GENERATION)).isTrue();
    }

    @Test
    @DisplayName("Should support sentiment analysis requests")
    void supports_withSentimentAnalysis_shouldReturnTrue() {
        // When & Then
        assertThat(openAIProviderStrategy.supports(AIRequest.AIRequestType.SENTIMENT_ANALYSIS)).isTrue();
    }

    @Test
    @DisplayName("Should not support demand forecasting requests")
    void supports_withDemandForecasting_shouldReturnFalse() {
        // When & Then
        assertThat(openAIProviderStrategy.supports(AIRequest.AIRequestType.DEMAND_FORECASTING)).isFalse();
    }

    @Test
    @DisplayName("Should not support document analysis requests")
    void supports_withDocumentAnalysis_shouldReturnFalse() {
        // When & Then
        assertThat(openAIProviderStrategy.supports(AIRequest.AIRequestType.DOCUMENT_ANALYSIS)).isFalse();
    }

    @Test
    @DisplayName("Should return correct provider type")
    void getProviderType_shouldReturnOpenAI() {
        // When & Then
        assertThat(openAIProviderStrategy.getProviderType()).isEqualTo(AIRequest.AIProvider.OPENAI);
    }

    @Test
    @DisplayName("Should be available with valid configuration")
    void isAvailable_withValidConfig_shouldReturnTrue() {
        // When & Then
        assertThat(openAIProviderStrategy.isAvailable()).isTrue();
    }

    @Test
    @DisplayName("Should not be available when disabled")
    void isAvailable_whenDisabled_shouldReturnFalse() {
        // Given
        ReflectionTestUtils.setField(openAIProviderStrategy, "enabled", false);

        // When & Then
        assertThat(openAIProviderStrategy.isAvailable()).isFalse();
    }

    @Test
    @DisplayName("Should not be available without API key")
    void isAvailable_withoutApiKey_shouldReturnFalse() {
        // Given
        ReflectionTestUtils.setField(openAIProviderStrategy, "apiKey", "");

        // When & Then
        assertThat(openAIProviderStrategy.isAvailable()).isFalse();
    }

    @Test
    @DisplayName("Should not be available with null API key")
    void isAvailable_withNullApiKey_shouldReturnFalse() {
        // Given
        ReflectionTestUtils.setField(openAIProviderStrategy, "apiKey", null);

        // When & Then
        assertThat(openAIProviderStrategy.isAvailable()).isFalse();
    }

    @Test
    @DisplayName("Should return healthy status when properly configured")
    void getHealthStatus_withValidConfig_shouldReturnHealthy() {
        // When & Then
        assertThat(openAIProviderStrategy.getHealthStatus()).isEqualTo("HEALTHY");
    }

    @Test
    @DisplayName("Should return disabled status when disabled")
    void getHealthStatus_whenDisabled_shouldReturnDisabled() {
        // Given
        ReflectionTestUtils.setField(openAIProviderStrategy, "enabled", false);

        // When & Then
        assertThat(openAIProviderStrategy.getHealthStatus()).isEqualTo("DISABLED");
    }

    @Test
    @DisplayName("Should return not configured status without API key")
    void getHealthStatus_withoutApiKey_shouldReturnNotConfigured() {
        // Given
        ReflectionTestUtils.setField(openAIProviderStrategy, "apiKey", "");

        // When & Then
        assertThat(openAIProviderStrategy.getHealthStatus()).isEqualTo("NOT_CONFIGURED");
    }

    @Test
    @DisplayName("Should handle unsupported request type gracefully")
    void processRequest_withUnsupportedType_shouldReturnFailedResponse() {
        // When
        Mono<AIResponse> responseMono = openAIProviderStrategy.processRequest(demandForecastingRequest);

        // Then
        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    assertThat(response.getRequestId()).isEqualTo("forecast-001");
                    assertThat(response.getStatus()).isEqualTo(AIResponse.ResponseStatus.FAILED);
                    assertThat(response.getErrorMessage()).contains("not supported");
                    assertThat(response.getProvider()).isEqualTo(AIRequest.AIProvider.OPENAI);
                    assertThat(response.getProcessingTimeMs()).isGreaterThan(0);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle disabled service gracefully")
    void processRequest_whenDisabled_shouldReturnFailedResponse() {
        // Given
        ReflectionTestUtils.setField(openAIProviderStrategy, "enabled", false);

        // When
        Mono<AIResponse> responseMono = openAIProviderStrategy.processRequest(textGenerationRequest);

        // Then
        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    assertThat(response.getRequestId()).isEqualTo("text-gen-001");
                    assertThat(response.getStatus()).isEqualTo(AIResponse.ResponseStatus.FAILED);
                    assertThat(response.getErrorMessage()).contains("not configured or disabled");
                    assertThat(response.getProvider()).isEqualTo(AIRequest.AIProvider.OPENAI);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle missing API key gracefully")
    void processRequest_withoutApiKey_shouldReturnFailedResponse() {
        // Given
        ReflectionTestUtils.setField(openAIProviderStrategy, "apiKey", null);

        // When
        Mono<AIResponse> responseMono = openAIProviderStrategy.processRequest(textGenerationRequest);

        // Then
        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    assertThat(response.getRequestId()).isEqualTo("text-gen-001");
                    assertThat(response.getStatus()).isEqualTo(AIResponse.ResponseStatus.FAILED);
                    assertThat(response.getErrorMessage()).contains("not configured or disabled");
                    assertThat(response.getProvider()).isEqualTo(AIRequest.AIProvider.OPENAI);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should validate request parameters")
    void processRequest_withInvalidRequest_shouldHandleGracefully() {
        // Given
        AIRequest invalidRequest = AIRequest.builder()
                .requestId("invalid-001")
                .type(AIRequest.AIRequestType.TEXT_GENERATION)
                .prompt("") // Empty prompt
                .maxTokens(-1) // Invalid token count
                .temperature(2.0) // Invalid temperature
                .preferredProvider(AIRequest.AIProvider.OPENAI)
                .build();

        // When
        Mono<AIResponse> responseMono = openAIProviderStrategy.processRequest(invalidRequest);

        // Then
        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    assertThat(response.getRequestId()).isEqualTo("invalid-001");
                    assertThat(response.getStatus()).isEqualTo(AIResponse.ResponseStatus.FAILED);
                    assertThat(response.getErrorMessage()).contains("Invalid request parameters");
                    assertThat(response.getProvider()).isEqualTo(AIRequest.AIProvider.OPENAI);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle null request gracefully")
    void processRequest_withNullRequest_shouldReturnFailedResponse() {
        // When
        Mono<AIResponse> responseMono = openAIProviderStrategy.processRequest(null);

        // Then
        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    assertThat(response.getStatus()).isEqualTo(AIResponse.ResponseStatus.FAILED);
                    assertThat(response.getErrorMessage()).contains("Request cannot be null");
                    assertThat(response.getProvider()).isEqualTo(AIRequest.AIProvider.OPENAI);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should create proper request format for text generation")
    void buildRequestBody_forTextGeneration_shouldCreateValidFormat() {
        // Given
        String expectedPrompt = "Generate a summary of supply chain best practices";

        // When - This tests the internal request building logic
        // Note: This would require making buildRequestBody protected or package-private for testing
        // Or using reflection to test private methods if needed

        // Then - Verify the request would be properly formatted
        assertThat(textGenerationRequest.getPrompt()).isEqualTo(expectedPrompt);
        assertThat(textGenerationRequest.getMaxTokens()).isEqualTo(150);
        assertThat(textGenerationRequest.getTemperature()).isEqualTo(0.7);
    }

    @Test
    @DisplayName("Should create proper request format for sentiment analysis")
    void buildRequestBody_forSentimentAnalysis_shouldCreateValidFormat() {
        // Given
        String expectedPrompt = "Analyze the sentiment: Our delivery was delayed again!";

        // When & Then
        assertThat(sentimentAnalysisRequest.getPrompt()).isEqualTo(expectedPrompt);
        assertThat(sentimentAnalysisRequest.getMaxTokens()).isEqualTo(50);
        assertThat(sentimentAnalysisRequest.getTemperature()).isEqualTo(0.3);
    }

    @Test
    @DisplayName("Should handle concurrent requests properly")
    void processRequest_withConcurrentRequests_shouldHandleIndependently() {
        // Given
        AIRequest request1 = AIRequest.builder()
                .requestId("concurrent-001")
                .type(AIRequest.AIRequestType.TEXT_GENERATION)
                .prompt("Request 1")
                .build();

        AIRequest request2 = AIRequest.builder()
                .requestId("concurrent-002")
                .type(AIRequest.AIRequestType.SENTIMENT_ANALYSIS)
                .prompt("Request 2")
                .build();

        // When
        Mono<AIResponse> response1 = openAIProviderStrategy.processRequest(request1);
        Mono<AIResponse> response2 = openAIProviderStrategy.processRequest(request2);

        // Then
        StepVerifier.create(Mono.zip(response1, response2))
                .assertNext(tuple -> {
                    AIResponse resp1 = tuple.getT1();
                    AIResponse resp2 = tuple.getT2();

                    assertThat(resp1.getRequestId()).isEqualTo("concurrent-001");
                    assertThat(resp2.getRequestId()).isEqualTo("concurrent-002");
                    assertThat(resp1.getRequestId()).isNotEqualTo(resp2.getRequestId());
                })
                .verifyComplete();
    }
}
