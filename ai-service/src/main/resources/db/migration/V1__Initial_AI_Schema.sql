-- ============================================================================
-- Flyway Migration V1: Initial AI Service Database Schema
-- Description: Create AI request/response tracking and audit tables
-- Author: Supply Chain Team
-- Date: 2024-10-01
-- ============================================================================

-- ============================================================================
-- AI_REQUESTS TABLE
-- ============================================================================
CREATE TABLE ai_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    request_id VARCHAR(100) NOT NULL UNIQUE,
    request_type VARCHAR(50) NOT NULL,
    prompt TEXT NOT NULL,
    context JSON,
    parameters JSON,
    user_id VARCHAR(100),
    session_id VARCHAR(100),
    preferred_provider VARCHAR(50),
    max_tokens INT,
    temperature DECIMAL(3,2),
    system_prompt TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_request_id (request_id),
    INDEX idx_request_type (request_type),
    INDEX idx_user_id (user_id),
    INDEX idx_session_id (session_id),
    INDEX idx_provider (preferred_provider),
    INDEX idx_created_at (created_at),
    
    CONSTRAINT chk_request_type CHECK (
        request_type IN (
            'TEXT_GENERATION',
            'DEMAND_FORECASTING',
            'INVENTORY_OPTIMIZATION',
            'SENTIMENT_ANALYSIS',
            'DOCUMENT_ANALYSIS',
            'TRANSLATION',
            'SUMMARIZATION',
            'CLASSIFICATION'
        )
    ),
    
    CONSTRAINT chk_provider CHECK (
        preferred_provider IN (
            'OPENAI',
            'AZURE_OPENAI',
            'HUGGING_FACE',
            'OLLAMA',
            'ANTHROPIC'
        )
    ),
    
    CONSTRAINT chk_temperature CHECK (temperature >= 0.0 AND temperature <= 2.0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- AI_RESPONSES TABLE
-- ============================================================================
CREATE TABLE ai_responses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    response_id VARCHAR(100) NOT NULL UNIQUE,
    request_id VARCHAR(100) NOT NULL,
    content TEXT,
    provider VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    error_message TEXT,
    metadata JSON,
    tokens_used INT,
    processing_time_ms BIGINT,
    confidence DECIMAL(5,4),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (request_id) REFERENCES ai_requests(request_id) ON DELETE CASCADE,
    
    INDEX idx_response_id (response_id),
    INDEX idx_request_id (request_id),
    INDEX idx_provider (provider),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_processing_time (processing_time_ms),
    
    CONSTRAINT chk_response_status CHECK (
        status IN (
            'SUCCESS',
            'PARTIAL_SUCCESS',
            'FAILED',
            'RATE_LIMITED',
            'TIMEOUT'
        )
    ),
    
    CONSTRAINT chk_confidence CHECK (confidence >= 0.0 AND confidence <= 1.0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- AI_USAGE_METRICS TABLE
-- ============================================================================
CREATE TABLE ai_usage_metrics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    date_recorded DATE NOT NULL,
    provider VARCHAR(50) NOT NULL,
    request_type VARCHAR(50) NOT NULL,
    total_requests INT DEFAULT 0,
    successful_requests INT DEFAULT 0,
    failed_requests INT DEFAULT 0,
    total_tokens_used BIGINT DEFAULT 0,
    avg_processing_time_ms DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_metrics (date_recorded, provider, request_type),
    INDEX idx_date_recorded (date_recorded),
    INDEX idx_provider_metrics (provider),
    INDEX idx_request_type_metrics (request_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- AI_FEEDBACK TABLE (For model improvement)
-- ============================================================================
CREATE TABLE ai_feedback (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    response_id VARCHAR(100) NOT NULL,
    user_id VARCHAR(100),
    rating INT CHECK (rating >= 1 AND rating <= 5),
    feedback_text TEXT,
    feedback_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (response_id) REFERENCES ai_responses(response_id) ON DELETE CASCADE,
    
    INDEX idx_response_id_feedback (response_id),
    INDEX idx_user_id_feedback (user_id),
    INDEX idx_rating (rating),
    INDEX idx_feedback_type (feedback_type),
    INDEX idx_created_at_feedback (created_at),
    
    CONSTRAINT chk_feedback_type CHECK (
        feedback_type IN (
            'ACCURACY',
            'RELEVANCE',
            'COMPLETENESS',
            'CLARITY',
            'GENERAL'
        )
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
