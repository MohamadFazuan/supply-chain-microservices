-- ============================================================================
-- Enhanced Audit Logging Tables (Add to existing DDL files)
-- ============================================================================

-- Add this to all services' migration files for comprehensive audit logging

-- ============================================================================
-- SYSTEM_AUDIT_LOG TABLE
-- ============================================================================
CREATE TABLE system_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    service_name VARCHAR(50) NOT NULL,
    trace_id VARCHAR(100),
    span_id VARCHAR(100),
    request_id VARCHAR(100),
    user_id VARCHAR(100),
    session_id VARCHAR(100),
    action_type VARCHAR(50) NOT NULL,
    resource_type VARCHAR(50),
    resource_id VARCHAR(100),
    http_method VARCHAR(10),
    endpoint VARCHAR(500),
    client_ip VARCHAR(45),
    user_agent TEXT,
    request_payload JSON,
    response_payload JSON,
    status_code INT,
    duration_ms BIGINT,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_service_name (service_name),
    INDEX idx_trace_id (trace_id),
    INDEX idx_user_id (user_id),
    INDEX idx_action_type (action_type),
    INDEX idx_resource_type (resource_type),
    INDEX idx_created_at (created_at),
    INDEX idx_status_code (status_code),
    INDEX idx_duration (duration_ms)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- PERFORMANCE_METRICS TABLE
-- ============================================================================
CREATE TABLE performance_metrics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    service_name VARCHAR(50) NOT NULL,
    metric_name VARCHAR(100) NOT NULL,
    metric_type ENUM('COUNTER', 'TIMER', 'GAUGE', 'HISTOGRAM') NOT NULL,
    metric_value DECIMAL(20,6),
    tags JSON,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_service_metric (service_name, metric_name),
    INDEX idx_metric_type (metric_type),
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- ERROR_LOG TABLE
-- ============================================================================
CREATE TABLE error_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    service_name VARCHAR(50) NOT NULL,
    trace_id VARCHAR(100),
    error_type VARCHAR(100) NOT NULL,
    error_class VARCHAR(255),
    error_message TEXT NOT NULL,
    stack_trace TEXT,
    request_context JSON,
    user_id VARCHAR(100),
    endpoint VARCHAR(500),
    http_method VARCHAR(10),
    severity ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'MEDIUM',
    resolved BOOLEAN DEFAULT FALSE,
    resolution_notes TEXT,
    occurred_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    
    INDEX idx_service_name (service_name),
    INDEX idx_trace_id (trace_id),
    INDEX idx_error_type (error_type),
    INDEX idx_severity (severity),
    INDEX idx_resolved (resolved),
    INDEX idx_occurred_at (occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
