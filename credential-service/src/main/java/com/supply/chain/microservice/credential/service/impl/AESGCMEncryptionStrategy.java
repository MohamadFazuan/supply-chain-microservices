package com.supply.chain.microservice.credential.service.impl;

import com.supply.chain.microservice.credential.service.EncryptionStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-GCM encryption strategy implementation using the Strategy pattern
 * Provides strong encryption for sensitive credentials
 */
@Component
@Slf4j
public class AESGCMEncryptionStrategy implements EncryptionStrategy {
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    
    @Value("${credential.encryption.key:defaultKey123456789012345678901234}")
    private String encryptionKey;
    
    @Override
    public String encrypt(String plaintext) {
        try {
            SecretKey secretKey = new SecretKeySpec(encryptionKey.getBytes(), ALGORITHM);
            
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes());
            
            // Combine IV and ciphertext
            byte[] encryptedWithIv = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
            System.arraycopy(ciphertext, 0, encryptedWithIv, iv.length, ciphertext.length);
            
            return Base64.getEncoder().encodeToString(encryptedWithIv);
            
        } catch (Exception e) {
            log.error("Failed to encrypt data", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }
    
    @Override
    public String decrypt(String ciphertext) {
        try {
            byte[] decodedData = Base64.getDecoder().decode(ciphertext);
            
            // Extract IV and ciphertext
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(decodedData, 0, iv, 0, iv.length);
            
            byte[] encrypted = new byte[decodedData.length - GCM_IV_LENGTH];
            System.arraycopy(decodedData, GCM_IV_LENGTH, encrypted, 0, encrypted.length);
            
            SecretKey secretKey = new SecretKeySpec(encryptionKey.getBytes(), ALGORITHM);
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
            
            byte[] plaintext = cipher.doFinal(encrypted);
            return new String(plaintext);
            
        } catch (Exception e) {
            log.error("Failed to decrypt data", e);
            throw new RuntimeException("Decryption failed", e);
        }
    }
    
    @Override
    public String getAlgorithmName() {
        return "AES-GCM";
    }
    
    @Override
    public boolean supports(String algorithm) {
        return "AES-GCM".equalsIgnoreCase(algorithm);
    }
}
