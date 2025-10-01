package com.supply.chain.microservice.credential.dto;

import com.supply.chain.microservice.credential.entity.Credential;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for creating and updating credentials
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CredentialRequest {
    
    @NotBlank(message = "Credential name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Credential type is required")
    private Credential.CredentialType type;
    
    @NotBlank(message = "Credential value is required")
    private String value;
    
    @NotBlank(message = "Owner ID is required")
    private String ownerId;
    
    @NotBlank(message = "Service ID is required")
    private String serviceId;
    
    private String environment;
    
    private LocalDateTime expiresAt;
    
    @Builder.Default
    private Boolean active = true;
    
    private String createdBy;
    
    private String updatedBy;
}
