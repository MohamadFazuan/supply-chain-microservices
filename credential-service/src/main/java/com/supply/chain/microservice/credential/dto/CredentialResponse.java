package com.supply.chain.microservice.credential.dto;

import com.supply.chain.microservice.credential.entity.Credential;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for credential responses (without exposing the actual credential value)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CredentialResponse {
    
    private Long id;
    
    private String name;
    
    private String description;
    
    private Credential.CredentialType type;
    
    private String encryptionAlgorithm;
    
    private Integer keyVersion;
    
    private String ownerId;
    
    private String serviceId;
    
    private String environment;
    
    private Boolean active;
    
    private LocalDateTime expiresAt;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private String createdBy;
    
    private String updatedBy;
    
    // Note: The actual credential value is NOT included for security reasons
    // Use retrieveCredential() method to get the decrypted value when needed
}
