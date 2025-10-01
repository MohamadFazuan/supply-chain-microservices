package com.supply.chain.microservice.credentialservice.service.encryption;

import com.supply.chain.microservice.credential.service.impl.AESGCMEncryptionStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("AES-GCM Encryption Strategy Tests")
class AESGCMEncryptionStrategyTest {

    private AESGCMEncryptionStrategy encryptionStrategy;
    private final String testEncryptionKey = "testKey123456789012345678901234"; // 32 bytes for AES-256

    @BeforeEach
    void setUp() {
        encryptionStrategy = new AESGCMEncryptionStrategy();
        ReflectionTestUtils.setField(encryptionStrategy, "encryptionKey", testEncryptionKey);
    }

    @Test
    @DisplayName("Should encrypt and decrypt text successfully")
    void encryptDecrypt_withValidInput_shouldReturnOriginalValue() {
        // Given
        String plaintext = "sensitive-api-key-12345";

        // When
        String encrypted = encryptionStrategy.encrypt(plaintext);
        String decrypted = encryptionStrategy.decrypt(encrypted);

        // Then
        assertThat(encrypted).isNotNull().isNotEmpty();
        assertThat(encrypted).isNotEqualTo(plaintext);
        assertThat(decrypted).isEqualTo(plaintext);
        assertThat(encrypted).matches("^[A-Za-z0-9+/]+=*$"); // Base64 pattern
    }

    @Test
    @DisplayName("Should produce different encrypted values for same input")
    void encrypt_withSameInput_shouldProduceDifferentOutputs() {
        // Given
        String plaintext = "test-credential-value";

        // When
        String encrypted1 = encryptionStrategy.encrypt(plaintext);
        String encrypted2 = encryptionStrategy.encrypt(plaintext);

        // Then
        assertThat(encrypted1).isNotEqualTo(encrypted2); // Due to random IV/nonce
        
        // Both should decrypt to same value
        String decrypted1 = encryptionStrategy.decrypt(encrypted1);
        String decrypted2 = encryptionStrategy.decrypt(encrypted2);
        assertThat(decrypted1).isEqualTo(plaintext);
        assertThat(decrypted2).isEqualTo(plaintext);
    }

    @Test
    @DisplayName("Should handle empty string encryption")
    void encrypt_withEmptyString_shouldReturnValidEncryption() {
        // Given
        String plaintext = "";

        // When
        String encrypted = encryptionStrategy.encrypt(plaintext);
        String decrypted = encryptionStrategy.decrypt(encrypted);

        // Then
        assertThat(encrypted).isNotEmpty();
        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Test
    @DisplayName("Should handle special characters in encryption")
    void encrypt_withSpecialCharacters_shouldHandleCorrectly() {
        // Given
        String plaintext = "!@#$%^&*()_+-={}[]|\\:;\"'<>,.?/~`áéíóú";

        // When
        String encrypted = encryptionStrategy.encrypt(plaintext);
        String decrypted = encryptionStrategy.decrypt(encrypted);

        // Then
        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Test
    @DisplayName("Should handle Unicode characters in encryption")
    void encrypt_withUnicodeCharacters_shouldHandleCorrectly() {
        // Given
        String plaintext = "测试数据 🚀 emoji テスト данные";

        // When
        String encrypted = encryptionStrategy.encrypt(plaintext);
        String decrypted = encryptionStrategy.decrypt(encrypted);

        // Then
        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Test
    @DisplayName("Should handle long text encryption")
    void encrypt_withLongText_shouldHandleCorrectly() {
        // Given
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("This is a long text for testing encryption capability. ");
        }
        String plaintext = sb.toString();

        // When
        String encrypted = encryptionStrategy.encrypt(plaintext);
        String decrypted = encryptionStrategy.decrypt(encrypted);

        // Then
        assertThat(decrypted).isEqualTo(plaintext);
        assertThat(encrypted.length()).isGreaterThan(plaintext.length()); // Due to encoding overhead
    }

    @Test
    @DisplayName("Should throw exception for null input during encryption")
    void encrypt_withNullInput_shouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> encryptionStrategy.encrypt(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Input cannot be null");
    }

    @Test
    @DisplayName("Should throw exception for null input during decryption")
    void decrypt_withNullInput_shouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> encryptionStrategy.decrypt(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Encrypted data cannot be null");
    }

    @Test
    @DisplayName("Should throw exception for invalid ciphertext")
    void decrypt_withInvalidCiphertext_shouldThrowException() {
        // Given
        String invalidCiphertext = "invalid-base64-data-!@#$%";

        // When & Then
        assertThatThrownBy(() -> encryptionStrategy.decrypt(invalidCiphertext))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Decryption failed");
    }

    @Test
    @DisplayName("Should throw exception for corrupted ciphertext")
    void decrypt_withCorruptedCiphertext_shouldThrowException() {
        // Given
        String validCiphertext = encryptionStrategy.encrypt("test");
        String corruptedCiphertext = validCiphertext.substring(0, validCiphertext.length() - 5) + "XXXXX";

        // When & Then
        assertThatThrownBy(() -> encryptionStrategy.decrypt(corruptedCiphertext))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Decryption failed");
    }

    @Test
    @DisplayName("Should throw exception for empty ciphertext")
    void decrypt_withEmptyCiphertext_shouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> encryptionStrategy.decrypt(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Encrypted data cannot be empty");
    }

    @Test
    @DisplayName("Should support AES-GCM algorithm")
    void supports_withAESGCM_shouldReturnTrue() {
        // When & Then
        assertThat(encryptionStrategy.supports("AES-GCM")).isTrue();
        assertThat(encryptionStrategy.getAlgorithmName()).isEqualTo("AES-GCM");
    }

    @Test
    @DisplayName("Should not support other algorithms")
    void supports_withOtherAlgorithms_shouldReturnFalse() {
        // When & Then
        assertThat(encryptionStrategy.supports("RSA")).isFalse();
        assertThat(encryptionStrategy.supports("AES-CBC")).isFalse();
        assertThat(encryptionStrategy.supports("DES")).isFalse();
        assertThat(encryptionStrategy.supports("")).isFalse();
        assertThat(encryptionStrategy.supports(null)).isFalse();
    }

    @Test
    @DisplayName("Should return correct algorithm name")
    void getAlgorithmName_shouldReturnAESGCM() {
        // When & Then
        assertThat(encryptionStrategy.getAlgorithmName()).isEqualTo("AES-GCM");
    }

    @Test
    @DisplayName("Should handle concurrent encryption operations")
    void encrypt_withConcurrentOperations_shouldBeThreadSafe() throws InterruptedException {
        // Given
        final int threadCount = 10;
        final String[] plaintexts = new String[threadCount];
        final String[] encrypted = new String[threadCount];
        final String[] decrypted = new String[threadCount];
        final Thread[] threads = new Thread[threadCount];

        // Initialize test data
        for (int i = 0; i < threadCount; i++) {
            plaintexts[i] = "test-data-" + i;
        }

        // When - Concurrent encryption
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                encrypted[index] = encryptionStrategy.encrypt(plaintexts[index]);
                decrypted[index] = encryptionStrategy.decrypt(encrypted[index]);
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Then
        for (int i = 0; i < threadCount; i++) {
            assertThat(decrypted[i]).isEqualTo(plaintexts[i]);
            assertThat(encrypted[i]).isNotEqualTo(plaintexts[i]);
        }
    }

    @Test
    @DisplayName("Should maintain encryption integrity over time")
    void encrypt_multipleOperations_shouldMaintainIntegrity() {
        // Given
        String plaintext = "consistency-test-data";
        
        // When - Perform multiple encrypt/decrypt cycles
        String result = plaintext;
        for (int i = 0; i < 10; i++) {
            String encrypted = encryptionStrategy.encrypt(result);
            result = encryptionStrategy.decrypt(encrypted);
        }

        // Then
        assertThat(result).isEqualTo(plaintext);
    }

    @Test
    @DisplayName("Should validate encryption key length requirements")
    void constructor_withInvalidKeyLength_shouldHandleGracefully() {
        // Given - This tests the internal key validation
        AESGCMEncryptionStrategy strategy = new AESGCMEncryptionStrategy();
        
        // When & Then - Invalid key should be handled gracefully
        // Note: This depends on how the strategy handles invalid keys
        // It might derive a proper key or throw an exception
        String shortKey = "short";
        ReflectionTestUtils.setField(strategy, "encryptionKey", shortKey);
        
        // The strategy should either work by deriving a proper key 
        // or fail gracefully with a clear error message
        try {
            String encrypted = strategy.encrypt("test");
            String decrypted = strategy.decrypt(encrypted);
            assertThat(decrypted).isEqualTo("test");
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains("key");
        }
    }

    @Test
    @DisplayName("Should validate Base64 encoding of encrypted output")
    void encrypt_shouldProduceValidBase64Output() {
        // Given
        String plaintext = "base64-validation-test";

        // When
        String encrypted = encryptionStrategy.encrypt(plaintext);

        // Then
        assertThat(encrypted).isNotNull();
        
        // Should be valid Base64
        try {
            byte[] decoded = Base64.getDecoder().decode(encrypted);
            assertThat(decoded).isNotEmpty();
        } catch (IllegalArgumentException e) {
            throw new AssertionError("Encrypted output is not valid Base64: " + encrypted, e);
        }
    }
}
