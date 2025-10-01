-- ============================================================================
-- Flyway Migration V3: Sample Data for Tiong Nam Logistics
-- Description: Insert sample categories and products for demonstration
-- Author: Supply Chain Team
-- Date: 2024-10-01
-- ============================================================================

-- ============================================================================
-- INSERT PRODUCT CATEGORIES
-- ============================================================================

-- Main categories
INSERT INTO product_categories (category_code, name, description, level, sort_order) VALUES
('WAREHOUSE', 'Warehouse Services', 'Storage and warehousing solutions for various cargo types', 0, 1),
('FREIGHT', 'Freight Forwarding', 'International and domestic freight forwarding services', 0, 2),
('TRANSPORT', 'Transportation', 'Fleet and transportation services', 0, 3),
('CUSTOM', 'Custom Logistics', 'Tailored logistics solutions for specialized requirements', 0, 4);

-- Warehouse subcategories
INSERT INTO product_categories (category_code, name, description, parent_category_id, level, sort_order) 
SELECT 'WAREHOUSE_AMBIENT', 'Ambient Storage', 'Room temperature storage units for general cargo', id, 1, 1
FROM product_categories WHERE category_code = 'WAREHOUSE';

INSERT INTO product_categories (category_code, name, description, parent_category_id, level, sort_order) 
SELECT 'WAREHOUSE_COLD', 'Cold Storage', 'Refrigerated and frozen storage for perishables', id, 1, 2
FROM product_categories WHERE category_code = 'WAREHOUSE';

INSERT INTO product_categories (category_code, name, description, parent_category_id, level, sort_order) 
SELECT 'WAREHOUSE_HAZMAT', 'Hazardous Materials Storage', 'Specialized storage for hazardous materials', id, 1, 3
FROM product_categories WHERE category_code = 'WAREHOUSE';

-- Freight subcategories
INSERT INTO product_categories (category_code, name, description, parent_category_id, level, sort_order) 
SELECT 'FREIGHT_SEA', 'Sea Freight', 'Ocean freight services for bulk and container cargo', id, 1, 1
FROM product_categories WHERE category_code = 'FREIGHT';

INSERT INTO product_categories (category_code, name, description, parent_category_id, level, sort_order) 
SELECT 'FREIGHT_AIR', 'Air Freight', 'Air cargo services for time-sensitive shipments', id, 1, 2
FROM product_categories WHERE category_code = 'FREIGHT';

INSERT INTO product_categories (category_code, name, description, parent_category_id, level, sort_order) 
SELECT 'FREIGHT_LAND', 'Land Freight', 'Overland freight services for regional distribution', id, 1, 3
FROM product_categories WHERE category_code = 'FREIGHT';

-- Transportation subcategories
INSERT INTO product_categories (category_code, name, description, parent_category_id, level, sort_order) 
SELECT 'TRANSPORT_TRUCK', 'Trucking Services', 'Road transportation for various cargo types', id, 1, 1
FROM product_categories WHERE category_code = 'TRANSPORT';

INSERT INTO product_categories (category_code, name, description, parent_category_id, level, sort_order) 
SELECT 'TRANSPORT_CONTAINER', 'Container Services', 'Container transportation and handling services', id, 1, 2
FROM product_categories WHERE category_code = 'TRANSPORT';

INSERT INTO product_categories (category_code, name, description, parent_category_id, level, sort_order) 
SELECT 'TRANSPORT_SPECIAL', 'Special Transport', 'Specialized transportation for oversized or sensitive cargo', id, 1, 3
FROM product_categories WHERE category_code = 'TRANSPORT';

-- ============================================================================
-- INSERT SAMPLE PRODUCTS
-- ============================================================================

-- Warehouse Storage Products
INSERT INTO products (
    product_code, name, description, short_description, category_id, product_type, status,
    specifications, dimensions, weight_capacity, weight_unit, service_areas, service_type,
    base_price, currency, pricing_model, tags, created_by
) VALUES (
    'WH-AMB-001',
    'Standard Ambient Warehouse Unit - Small',
    'Climate-controlled ambient storage unit suitable for general cargo, electronics, and dry goods. Features 24/7 security monitoring, fire suppression system, and easy access via forklift.',
    'Small ambient storage unit with 24/7 security and climate control',
    (SELECT id FROM product_categories WHERE category_code = 'WAREHOUSE_AMBIENT'),
    'WAREHOUSE_STORAGE',
    'ACTIVE',
    JSON_OBJECT(
        'climate_controlled', true,
        'security_level', 'STANDARD',
        'fire_suppression', true,
        'loading_dock_access', true,
        'forklift_accessible', true,
        'power_outlets', 4,
        'lighting', 'LED'
    ),
    JSON_OBJECT('length', 10, 'width', 8, 'height', 4, 'unit', 'meters'),
    5000.000,
    'KG',
    JSON_ARRAY('Klang Valley', 'Selangor', 'Kuala Lumpur'),
    'AMBIENT_STORAGE',
    850.00,
    'MYR',
    'FIXED',
    JSON_ARRAY('warehouse', 'ambient', 'small', 'secure', 'klang'),
    'SYSTEM'
);

INSERT INTO products (
    product_code, name, description, short_description, category_id, product_type, status,
    specifications, dimensions, weight_capacity, weight_unit, service_areas, service_type,
    base_price, currency, pricing_model, tags, created_by
) VALUES (
    'WH-COLD-001',
    'Refrigerated Storage Unit - Medium',
    'Temperature-controlled cold storage unit maintaining 2-8°C for pharmaceuticals, fresh produce, and perishable goods. Includes temperature monitoring, backup power, and compliance certifications.',
    'Medium cold storage unit for pharmaceuticals and perishables',
    (SELECT id FROM product_categories WHERE category_code = 'WAREHOUSE_COLD'),
    'WAREHOUSE_STORAGE',
    'ACTIVE',
    JSON_OBJECT(
        'temperature_range', '2-8°C',
        'humidity_control', true,
        'backup_power', true,
        'temperature_monitoring', true,
        'gdp_certified', true,
        'haccp_compliant', true,
        'alarm_system', true
    ),
    JSON_OBJECT('length', 15, 'width', 12, 'height', 5, 'unit', 'meters'),
    8000.000,
    'KG',
    JSON_ARRAY('Port Klang', 'Shah Alam', 'Subang'),
    'COLD_STORAGE',
    1850.00,
    'MYR',
    'TIERED',
    JSON_ARRAY('warehouse', 'cold', 'medium', 'pharmaceutical', 'gdp'),
    'SYSTEM'
);

-- Freight Forwarding Products
INSERT INTO products (
    product_code, name, description, short_description, category_id, product_type, status,
    specifications, service_areas, service_type, base_price, currency, pricing_model, tags, created_by
) VALUES (
    'FF-SEA-MY-SG',
    'Malaysia-Singapore Sea Freight FCL',
    'Full Container Load (FCL) sea freight service between Malaysia and Singapore. Includes customs clearance, documentation, and door-to-door delivery options. Regular weekly sailings with 2-3 days transit time.',
    'FCL sea freight service Malaysia to Singapore',
    (SELECT id FROM product_categories WHERE category_code = 'FREIGHT_SEA'),
    'FREIGHT_FORWARDING',
    'ACTIVE',
    JSON_OBJECT(
        'container_types', JSON_ARRAY('20ft', '40ft', '40ft HC'),
        'transit_time_days', 3,
        'frequency', 'Daily',
        'customs_clearance', true,
        'door_to_door', true,
        'tracking', true,
        'insurance_available', true
    ),
    JSON_ARRAY('Klang Valley', 'Johor', 'Singapore'),
    'FCL_SEA_FREIGHT',
    2800.00,
    'MYR',
    'QUOTE_BASED',
    JSON_ARRAY('sea freight', 'fcl', 'malaysia', 'singapore', 'container'),
    'SYSTEM'
);

INSERT INTO products (
    product_code, name, description, short_description, category_id, product_type, status,
    specifications, service_areas, service_type, base_price, currency, pricing_model, tags, created_by
) VALUES (
    'FF-AIR-MY-EU',
    'Malaysia-Europe Air Freight Express',
    'Express air freight service from Malaysia to major European cities. Same-day pickup, customs clearance, and 3-5 days delivery. Suitable for high-value and time-sensitive cargo.',
    'Express air freight Malaysia to Europe',
    (SELECT id FROM product_categories WHERE category_code = 'FREIGHT_AIR'),
    'FREIGHT_FORWARDING',
    'ACTIVE',
    JSON_OBJECT(
        'max_weight_kg', 500,
        'transit_time_days', 5,
        'pickup_time', 'Same day',
        'customs_clearance', true,
        'express_handling', true,
        'dangerous_goods', false,
        'temperature_control', true
    ),
    JSON_ARRAY('Kuala Lumpur', 'London', 'Amsterdam', 'Frankfurt', 'Paris'),
    'EXPRESS_AIR_FREIGHT',
    45.00,
    'MYR',
    'TIERED',
    JSON_ARRAY('air freight', 'express', 'europe', 'high value', 'time sensitive'),
    'SYSTEM'
);

-- Transportation Products
INSERT INTO products (
    product_code, name, description, short_description, category_id, product_type, status,
    specifications, dimensions, weight_capacity, weight_unit, service_areas, service_type,
    base_price, currency, pricing_model, tags, created_by
) VALUES (
    'TR-TRUCK-10T',
    '10-Ton Cargo Truck Service',
    'Dedicated 10-ton cargo truck service for local and regional deliveries. Includes professional driver, GPS tracking, and flexible scheduling. Suitable for palletized goods and general cargo.',
    '10-ton truck service for local deliveries',
    (SELECT id FROM product_categories WHERE category_code = 'TRANSPORT_TRUCK'),
    'TRANSPORTATION',
    'ACTIVE',
    JSON_OBJECT(
        'vehicle_type', 'Box Truck',
        'max_payload_kg', 10000,
        'gps_tracking', true,
        'professional_driver', true,
        'flexible_scheduling', true,
        'loading_assistance', true,
        'insurance_covered', true
    ),
    JSON_OBJECT('length', 8, 'width', 2.4, 'height', 2.8, 'unit', 'meters'),
    10000.000,
    'KG',
    JSON_ARRAY('Klang Valley', 'Selangor', 'Negeri Sembilan', 'Melaka'),
    'LOCAL_DELIVERY',
    380.00,
    'MYR',
    'FIXED',
    JSON_ARRAY('truck', '10 ton', 'local delivery', 'gps tracking', 'professional driver'),
    'SYSTEM'
);

-- Custom Logistics Package
INSERT INTO products (
    product_code, name, description, short_description, category_id, product_type, status,
    specifications, service_areas, service_type, base_price, currency, pricing_model, tags, created_by
) VALUES (
    'CL-ECOM-001',
    'E-commerce Fulfillment Package',
    'Complete e-commerce logistics solution including warehousing, inventory management, order processing, picking & packing, and last-mile delivery. Integrated with major e-commerce platforms.',
    'Complete e-commerce fulfillment solution',
    (SELECT id FROM product_categories WHERE category_code = 'CUSTOM'),
    'CUSTOM_PACKAGE',
    'ACTIVE',
    JSON_OBJECT(
        'warehousing', true,
        'inventory_management', true,
        'order_processing', true,
        'picking_packing', true,
        'last_mile_delivery', true,
        'platform_integration', JSON_ARRAY('Shopee', 'Lazada', 'Shopify', 'WooCommerce'),
        'return_handling', true,
        'reporting', true
    ),
    JSON_ARRAY('Klang Valley', 'Penang', 'Johor Bahru'),
    'ECOMMERCE_FULFILLMENT',
    1200.00,
    'MYR',
    'TIERED',
    JSON_ARRAY('ecommerce', 'fulfillment', 'warehousing', 'delivery', 'integration'),
    'SYSTEM'
);

-- ============================================================================
-- INSERT SAMPLE INVENTORY DATA
-- ============================================================================

-- Inventory for warehouse units
INSERT INTO product_inventory (
    product_id, location_code, warehouse_id, available_quantity, total_capacity, 
    minimum_stock_level, is_available, location_details, coordinates, updated_by
) VALUES 
(
    (SELECT id FROM products WHERE product_code = 'WH-AMB-001'),
    'KL-CENTRAL',
    'WH-KL-001',
    15,
    20,
    2,
    TRUE,
    JSON_OBJECT('warehouse_name', 'Kuala Lumpur Central Warehouse', 'zone', 'A', 'level', '1'),
    JSON_OBJECT('latitude', 3.1390, 'longitude', 101.6869),
    'SYSTEM'
),
(
    (SELECT id FROM products WHERE product_code = 'WH-COLD-001'),
    'PKL-PORT',
    'WH-PKL-002',
    8,
    10,
    1,
    TRUE,
    JSON_OBJECT('warehouse_name', 'Port Klang Cold Storage', 'zone', 'B', 'level', '2'),
    JSON_OBJECT('latitude', 3.0319, 'longitude', 101.3900),
    'SYSTEM'
);

-- ============================================================================
-- INSERT SAMPLE PRICING DATA
-- ============================================================================

-- Pricing for warehouse products
INSERT INTO product_pricing (
    product_id, pricing_tier, unit_price, minimum_quantity, maximum_quantity, 
    discount_percentage, effective_from, customer_segment, created_by
) VALUES
(
    (SELECT id FROM products WHERE product_code = 'WH-AMB-001'),
    'STANDARD',
    850.00,
    1,
    5,
    0.00,
    CURDATE(),
    'STANDARD',
    'SYSTEM'
),
(
    (SELECT id FROM products WHERE product_code = 'WH-AMB-001'),
    'BULK',
    765.00,
    6,
    NULL,
    10.00,
    CURDATE(),
    'ENTERPRISE',
    'SYSTEM'
),
(
    (SELECT id FROM products WHERE product_code = 'WH-COLD-001'),
    'STANDARD',
    1850.00,
    1,
    3,
    0.00,
    CURDATE(),
    'STANDARD',
    'SYSTEM'
);

-- ============================================================================
-- INSERT SAMPLE LOGISTICS SPECIFIC DATA
-- ============================================================================

-- Transportation fleet data
INSERT INTO transportation_fleet (
    product_id, fleet_id, vehicle_type, make, model, year_manufactured, license_plate,
    cargo_capacity, max_weight, operational_status, current_location, assigned_driver_id,
    gps_enabled, tracking_device_id
) VALUES (
    (SELECT id FROM products WHERE product_code = 'TR-TRUCK-10T'),
    'TN-TRUCK-001',
    'Box Truck',
    'Isuzu',
    'NPR 85',
    2022,
    'WKL 1234 A',
    45.000,
    10000.000,
    'ACTIVE',
    'Kuala Lumpur',
    'DRV-001',
    TRUE,
    'GPS-TN-001'
);

-- Warehouse storage unit details
INSERT INTO warehouse_storage_units (
    product_id, unit_code, warehouse_code, storage_type, size_category,
    floor_area, height, zone, aisle, rack, level, climate_controlled, security_level,
    occupancy_status
) VALUES (
    (SELECT id FROM products WHERE product_code = 'WH-AMB-001'),
    'KL-A1-001',
    'WH-KL-001',
    'AMBIENT',
    'SMALL',
    80.00,
    4.00,
    'A',
    '1',
    'R01',
    'L1',
    TRUE,
    'STANDARD',
    'VACANT'
);

-- Freight forwarding route details
INSERT INTO freight_forwarding_routes (
    product_id, route_code, origin_country, origin_city, origin_port_code,
    destination_country, destination_city, destination_port_code,
    primary_transport_mode, service_type, transit_time_days, frequency,
    available_days, container_types, max_weight_per_shipment, route_status
) VALUES (
    (SELECT id FROM products WHERE product_code = 'FF-SEA-MY-SG'),
    'RT-MY-SG-001',
    'Malaysia',
    'Port Klang',
    'MYPKG',
    'Singapore',
    'Singapore',
    'SGSIN',
    'SEA',
    'FCL',
    3,
    'Daily',
    JSON_ARRAY('MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT'),
    JSON_ARRAY('20ft', '40ft', '40ft HC'),
    28000.000,
    'ACTIVE'
);
