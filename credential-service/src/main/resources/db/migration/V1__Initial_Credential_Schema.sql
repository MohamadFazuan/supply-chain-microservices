-- ============================================================================
-- Flyway Migration V1: Initial Credential Service Database Schema
-- Description: Create credential management tables for encrypted secrets
-- Author: Supply Chain Team
-- Date: 2024-10-01
-- ============================================================================

-- ============================================================================
-- CREDENTIALS TABLE
-- ============================================================================
CREATE TABLE credentials (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(500) NOT NULL,
    type VARCHAR(50) NOT NULL,
    encrypted_value TEXT NOT NULL,
    encryption_algorithm VARCHAR(50),
    key_version INT,
    owner_id VARCHAR(100),
    service_id VARCHAR(100),
    environment VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    expires_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    
    INDEX idx_name (name),
    INDEX idx_type (type),
    INDEX idx_owner_id (owner_id),
    INDEX idx_service_id (service_id),
    INDEX idx_environment (environment),
    INDEX idx_active (is_active),
    INDEX idx_expires_at (expires_at),
    INDEX idx_created_at (created_at),
    
    CONSTRAINT chk_credential_type CHECK (
        type IN (
            'API_KEY',
            'DATABASE_PASSWORD', 
            'SERVICE_TOKEN',
            'CERTIFICATE',
            'PRIVATE_KEY',
            'SECRET_KEY',
            'OAUTH_CLIENT_SECRET',
            'WEBHOOK_SECRET'
        )
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- CREDENTIAL ACCESS LOG TABLE (Optional for auditing)
-- ============================================================================
CREATE TABLE credential_access_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    credential_id BIGINT NOT NULL,
    accessed_by VARCHAR(100) NOT NULL,
    access_type ENUM('READ', 'DECRYPT', 'UPDATE', 'DELETE') NOT NULL,
    client_ip VARCHAR(45),
    user_agent VARCHAR(500),
    access_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    success BOOLEAN DEFAULT TRUE,
    error_message VARCHAR(500),
    
    FOREIGN KEY (credential_id) REFERENCES credentials(id) ON DELETE CASCADE,
    
    INDEX idx_credential_id (credential_id),
    INDEX idx_accessed_by (accessed_by),
    INDEX idx_access_type (access_type),
    INDEX idx_access_time (access_time),
    INDEX idx_success (success)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
