-- ============================================================================
-- Flyway Migration V1: Initial Product Service Database Schema
-- Description: Create core tables for Tiong Nam Logistics Product Management
-- Author: Supply Chain Team
-- Date: 2024-10-01
-- ============================================================================

-- ============================================================================
-- PRODUCT CATEGORIES TABLE
-- ============================================================================
CREATE TABLE product_categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    parent_category_id BIGINT,
    level INT NOT NULL DEFAULT 0,
    sort_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    
    INDEX idx_category_code (category_code),
    INDEX idx_parent_category (parent_category_id),
    INDEX idx_level (level),
    INDEX idx_active (is_active),
    FOREIGN KEY (parent_category_id) REFERENCES product_categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- PRODUCTS TABLE
-- ============================================================================
CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_code VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    short_description VARCHAR(500),
    category_id BIGINT NOT NULL,
    product_type ENUM('WAREHOUSE_STORAGE', 'FREIGHT_FORWARDING', 'TRANSPORTATION', 'CUSTOM_PACKAGE') NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'DISCONTINUED', 'COMING_SOON') DEFAULT 'ACTIVE',
    
    -- Physical Specifications
    specifications JSON,
    dimensions JSON, -- {length, width, height, unit}
    weight_capacity DECIMAL(15,3),
    weight_unit VARCHAR(10) DEFAULT 'KG',
    
    -- Service Details
    service_areas JSON, -- Geographic coverage
    service_type VARCHAR(100),
    special_requirements TEXT,
    
    -- Pricing
    base_price DECIMAL(15,2),
    currency VARCHAR(3) DEFAULT 'MYR',
    pricing_model ENUM('FIXED', 'TIERED', 'QUOTE_BASED') DEFAULT 'FIXED',
    
    -- Metadata
    tags JSON,
    images JSON,
    documents JSON,
    
    -- Tracking
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version INT DEFAULT 1,
    
    INDEX idx_product_code (product_code),
    INDEX idx_category (category_id),
    INDEX idx_product_type (product_type),
    INDEX idx_status (status),
    INDEX idx_name (name),
    INDEX idx_created_at (created_at),
    FULLTEXT idx_search (name, description, short_description),
    FOREIGN KEY (category_id) REFERENCES product_categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- PRODUCT INVENTORY TABLE
-- ============================================================================
CREATE TABLE product_inventory (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    location_code VARCHAR(50) NOT NULL,
    warehouse_id VARCHAR(100),
    
    -- Stock Information
    available_quantity INT DEFAULT 0,
    reserved_quantity INT DEFAULT 0,
    total_capacity INT DEFAULT 0,
    minimum_stock_level INT DEFAULT 0,
    
    -- Availability
    is_available BOOLEAN DEFAULT TRUE,
    availability_start_date DATE,
    availability_end_date DATE,
    
    -- Location Details
    location_details JSON,
    coordinates JSON, -- {latitude, longitude}
    
    -- Tracking
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    
    UNIQUE KEY unique_product_location (product_id, location_code),
    INDEX idx_product (product_id),
    INDEX idx_location (location_code),
    INDEX idx_availability (is_available),
    INDEX idx_warehouse (warehouse_id),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- PRODUCT PRICING TABLE
-- ============================================================================
CREATE TABLE product_pricing (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    pricing_tier VARCHAR(50) NOT NULL DEFAULT 'STANDARD',
    
    -- Pricing Details
    unit_price DECIMAL(15,2) NOT NULL,
    minimum_quantity INT DEFAULT 1,
    maximum_quantity INT,
    discount_percentage DECIMAL(5,2) DEFAULT 0.00,
    
    -- Validity
    effective_from DATE NOT NULL,
    effective_to DATE,
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Special Conditions
    customer_segment VARCHAR(50),
    location_specific JSON,
    special_conditions TEXT,
    
    -- Tracking
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    
    INDEX idx_product (product_id),
    INDEX idx_pricing_tier (pricing_tier),
    INDEX idx_effective_dates (effective_from, effective_to),
    INDEX idx_active (is_active),
    INDEX idx_customer_segment (customer_segment),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- PRODUCT ATTRIBUTES TABLE (EAV Pattern for Dynamic Attributes)
-- ============================================================================
CREATE TABLE product_attributes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    attribute_name VARCHAR(100) NOT NULL,
    attribute_value TEXT,
    attribute_type ENUM('STRING', 'NUMBER', 'BOOLEAN', 'DATE', 'JSON') DEFAULT 'STRING',
    display_order INT DEFAULT 0,
    is_searchable BOOLEAN DEFAULT FALSE,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY unique_product_attribute (product_id, attribute_name),
    INDEX idx_product (product_id),
    INDEX idx_attribute_name (attribute_name),
    INDEX idx_searchable (is_searchable),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
