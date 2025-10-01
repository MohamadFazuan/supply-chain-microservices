package com.supply.chain.microservice.credential.service;

import com.supply.chain.microservice.credential.service.impl.AESGCMEncryptionStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Factory pattern implementation for encryption strategies
 * Provides appropriate encryption strategy based on algorithm name
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EncryptionStrategyFactory {
    
    private final List<EncryptionStrategy> encryptionStrategies;
    
    public EncryptionStrategy getStrategy(String algorithm) {
        Optional<EncryptionStrategy> strategy = encryptionStrategies.stream()
                .filter(s -> s.supports(algorithm))
                .findFirst();
        
        if (strategy.isPresent()) {
            log.debug("Found encryption strategy for algorithm: {}", algorithm);
            return strategy.get();
        }
        
        log.warn("No encryption strategy found for algorithm: {}, using default AES-GCM", algorithm);
        return encryptionStrategies.stream()
                .filter(s -> s instanceof AESGCMEncryptionStrategy)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No default encryption strategy available"));
    }
    
    public List<String> getSupportedAlgorithms() {
        return encryptionStrategies.stream()
                .map(EncryptionStrategy::getAlgorithmName)
                .toList();
    }
}
