package com.supply.chain.microservice.credential.service;

import com.supply.chain.microservice.credential.entity.Credential;
import com.supply.chain.microservice.credential.dto.CredentialRequest;
import com.supply.chain.microservice.credential.dto.CredentialResponse;

import java.util.List;
import java.util.Optional;

/**
 * Credential vault service interface using the Repository pattern
 * Provides secure credential management operations
 */
public interface CredentialVaultService {
    
    CredentialResponse storeCredential(CredentialRequest request);
    
    Optional<String> retrieveCredential(String name, String serviceId);
    
    Optional<CredentialResponse> getCredentialInfo(String name, String serviceId);
    
    List<CredentialResponse> getCredentialsByService(String serviceId);
    
    List<CredentialResponse> getCredentialsByOwner(String ownerId);
    
    boolean updateCredential(String name, String serviceId, CredentialRequest request);
    
    boolean deleteCredential(String name, String serviceId);
    
    boolean rotateCredential(String name, String serviceId, String newValue);
    
    List<CredentialResponse> getExpiringCredentials(int daysBeforeExpiry);
    
    boolean validateCredential(String name, String serviceId, String value);
}
