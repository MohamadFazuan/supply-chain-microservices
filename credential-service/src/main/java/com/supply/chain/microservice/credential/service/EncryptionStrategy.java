package com.supply.chain.microservice.credential.service;

/**
 * Strategy pattern interface for different encryption algorithms
 */
public interface EncryptionStrategy {
    String encrypt(String plaintext);
    String decrypt(String ciphertext);
    String getAlgorithmName();
    boolean supports(String algorithm);
}
