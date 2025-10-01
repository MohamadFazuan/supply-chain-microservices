package com.supply.chain.microservice.credential.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Credential entity using the Entity pattern
 * Stores encrypted credentials and API keys
 */
@Entity
@Table(name = "credentials")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Credential {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(nullable = false)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CredentialType type;
    
    @Column(name = "encrypted_value", nullable = false, columnDefinition = "TEXT")
    private String encryptedValue;
    
    @Column(name = "encryption_algorithm")
    private String encryptionAlgorithm;
    
    @Column(name = "key_version")
    private Integer keyVersion;
    
    @Column(name = "owner_id")
    private String ownerId;
    
    @Column(name = "service_id")
    private String serviceId;
    
    @Column(name = "environment")
    private String environment;
    
    @Column(name = "is_active")
    private Boolean active = true;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by")
    private String createdBy;
    
    @Column(name = "updated_by")
    private String updatedBy;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum CredentialType {
        API_KEY,
        DATABASE_PASSWORD,
        SERVICE_TOKEN,
        CERTIFICATE,
        PRIVATE_KEY,
        SECRET_KEY,
        OAUTH_CLIENT_SECRET,
        WEBHOOK_SECRET
    }
}
