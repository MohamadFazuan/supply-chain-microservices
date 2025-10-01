-- ============================================================================
-- Flyway Migration V2: Logistics Specific Tables
-- Description: Create tables for warehouse, fleet, and freight operations
-- Author: Supply Chain Team
-- Date: 2024-10-01
-- ============================================================================

-- ============================================================================
-- TRANSPORTATION FLEET TABLE (For Transportation Products)
-- ============================================================================
CREATE TABLE transportation_fleet (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    fleet_id VARCHAR(100) UNIQUE NOT NULL,
    vehicle_type VARCHAR(50) NOT NULL,
    
    -- Vehicle Details
    make VARCHAR(50),
    model VARCHAR(50),
    year_manufactured INT,
    license_plate VARCHAR(20),
    
    -- Capacity
    cargo_capacity DECIMAL(15,3),
    passenger_capacity INT,
    max_weight DECIMAL(15,3),
    
    -- Status
    operational_status ENUM('ACTIVE', 'MAINTENANCE', 'RETIRED', 'RESERVED') DEFAULT 'ACTIVE',
    current_location VARCHAR(100),
    last_service_date DATE,
    next_service_due DATE,
    
    -- Driver Assignment
    assigned_driver_id VARCHAR(100),
    driver_contact JSON,
    
    -- Tracking
    gps_enabled BOOLEAN DEFAULT TRUE,
    tracking_device_id VARCHAR(100),
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_product (product_id),
    INDEX idx_fleet_id (fleet_id),
    INDEX idx_vehicle_type (vehicle_type),
    INDEX idx_status (operational_status),
    INDEX idx_driver (assigned_driver_id),
    FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- WAREHOUSE STORAGE UNITS TABLE (For Warehouse Storage Products)
-- ============================================================================
CREATE TABLE warehouse_storage_units (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    unit_code VARCHAR(100) UNIQUE NOT NULL,
    warehouse_code VARCHAR(50) NOT NULL,
    
    -- Physical Details
    storage_type ENUM('AMBIENT', 'REFRIGERATED', 'FROZEN', 'HAZARDOUS', 'SECURE') NOT NULL,
    size_category ENUM('SMALL', 'MEDIUM', 'LARGE', 'EXTRA_LARGE', 'CUSTOM') NOT NULL,
    floor_area DECIMAL(10,2),
    height DECIMAL(10,2),
    
    -- Location within Warehouse
    zone VARCHAR(20),
    aisle VARCHAR(10),
    rack VARCHAR(10),
    level VARCHAR(10),
    
    -- Features
    climate_controlled BOOLEAN DEFAULT FALSE,
    security_level ENUM('STANDARD', 'HIGH', 'MAXIMUM') DEFAULT 'STANDARD',
    access_equipment JSON, -- Available forklifts, cranes, etc.
    
    -- Availability
    occupancy_status ENUM('VACANT', 'OCCUPIED', 'MAINTENANCE', 'RESERVED') DEFAULT 'VACANT',
    current_tenant VARCHAR(100),
    lease_start_date DATE,
    lease_end_date DATE,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_product (product_id),
    INDEX idx_unit_code (unit_code),
    INDEX idx_warehouse (warehouse_code),
    INDEX idx_storage_type (storage_type),
    INDEX idx_size_category (size_category),
    INDEX idx_occupancy (occupancy_status),
    FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- FREIGHT FORWARDING ROUTES TABLE (For Freight Forwarding Products)
-- ============================================================================
CREATE TABLE freight_forwarding_routes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    route_code VARCHAR(100) UNIQUE NOT NULL,
    
    -- Route Details
    origin_country VARCHAR(50) NOT NULL,
    origin_city VARCHAR(100) NOT NULL,
    origin_port_code VARCHAR(20),
    destination_country VARCHAR(50) NOT NULL,
    destination_city VARCHAR(100) NOT NULL,
    destination_port_code VARCHAR(20),
    
    -- Transportation Mode
    primary_transport_mode ENUM('SEA', 'AIR', 'LAND', 'MULTIMODAL') NOT NULL,
    service_type ENUM('FCL', 'LCL', 'EXPRESS', 'STANDARD', 'ECONOMY') NOT NULL,
    
    -- Service Details
    transit_time_days INT,
    frequency VARCHAR(50), -- Weekly, Daily, etc.
    available_days JSON, -- Days of week
    cutoff_hours VARCHAR(10),
    
    -- Capacity
    container_types JSON, -- 20ft, 40ft, etc.
    max_weight_per_shipment DECIMAL(15,3),
    dangerous_goods_allowed BOOLEAN DEFAULT FALSE,
    
    -- Pricing
    base_rate DECIMAL(15,2),
    fuel_surcharge_rate DECIMAL(5,2),
    documentation_fee DECIMAL(10,2),
    insurance_rate DECIMAL(5,4),
    
    -- Status
    route_status ENUM('ACTIVE', 'SUSPENDED', 'SEASONAL', 'DISCONTINUED') DEFAULT 'ACTIVE',
    seasonal_start_date DATE,
    seasonal_end_date DATE,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_product (product_id),
    INDEX idx_route_code (route_code),
    INDEX idx_origin (origin_country, origin_city),
    INDEX idx_destination (destination_country, destination_city),
    INDEX idx_transport_mode (primary_transport_mode),
    INDEX idx_service_type (service_type),
    INDEX idx_route_status (route_status),
    FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- PRODUCT AUDIT LOG TABLE
-- ============================================================================
CREATE TABLE product_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    action_type ENUM('CREATE', 'UPDATE', 'DELETE', 'STATUS_CHANGE', 'PRICE_CHANGE') NOT NULL,
    old_values JSON,
    new_values JSON,
    changed_fields JSON,
    reason VARCHAR(500),
    
    -- Tracking
    performed_by VARCHAR(100) NOT NULL,
    performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent TEXT,
    
    INDEX idx_product (product_id),
    INDEX idx_action_type (action_type),
    INDEX idx_performed_by (performed_by),
    INDEX idx_performed_at (performed_at),
    FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
