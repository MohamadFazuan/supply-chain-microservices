package com.supply.chain.microservice.productservice.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Warehouse Storage Unit Entity
 * Manages individual storage units within warehouses
 */
@Entity
@Table(name = "warehouse_storage_units", indexes = {
    @Index(name = "idx_product", columnList = "product"),
    @Index(name = "idx_unit_code", columnList = "unitCode"),
    @Index(name = "idx_warehouse", columnList = "warehouseCode"),
    @Index(name = "idx_storage_type", columnList = "storageType"),
    @Index(name = "idx_size_category", columnList = "sizeCategory"),
    @Index(name = "idx_occupancy", columnList = "occupancyStatus")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseStorageUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull(message = "Product is required")
    private Product product;

    @Column(name = "unit_code", unique = true, nullable = false, length = 100)
    @NotBlank(message = "Unit code is required")
    @Size(max = 100, message = "Unit code must not exceed 100 characters")
    private String unitCode;

    @Column(name = "warehouse_code", nullable = false, length = 50)
    @NotBlank(message = "Warehouse code is required")
    @Size(max = 50, message = "Warehouse code must not exceed 50 characters")
    private String warehouseCode;

    // Physical Details
    @Enumerated(EnumType.STRING)
    @Column(name = "storage_type", nullable = false)
    @NotNull(message = "Storage type is required")
    private StorageType storageType;

    @Enumerated(EnumType.STRING)
    @Column(name = "size_category", nullable = false)
    @NotNull(message = "Size category is required")
    private SizeCategory sizeCategory;

    @Column(name = "floor_area", precision = 10, scale = 2)
    private BigDecimal floorArea;

    @Column(name = "height", precision = 10, scale = 2)
    private BigDecimal height;

    // Location within Warehouse
    @Column(name = "zone", length = 20)
    private String zone;

    @Column(name = "aisle", length = 10)
    private String aisle;

    @Column(name = "rack", length = 10)
    private String rack;

    @Column(name = "level", length = 10)
    private String level;

    // Features
    @Column(name = "climate_controlled")
    @Builder.Default
    private Boolean climateControlled = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "security_level")
    @Builder.Default
    private SecurityLevel securityLevel = SecurityLevel.STANDARD;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "access_equipment", columnDefinition = "JSON")
    private JsonNode accessEquipment;

    // Availability
    @Enumerated(EnumType.STRING)
    @Column(name = "occupancy_status")
    @Builder.Default
    private OccupancyStatus occupancyStatus = OccupancyStatus.VACANT;

    @Column(name = "current_tenant", length = 100)
    private String currentTenant;

    @Column(name = "lease_start_date")
    private LocalDate leaseStartDate;

    @Column(name = "lease_end_date")
    private LocalDate leaseEndDate;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Storage Types for different cargo requirements
     */
    public enum StorageType {
        AMBIENT("Ambient Storage"),
        REFRIGERATED("Refrigerated Storage"),
        FROZEN("Frozen Storage"),
        HAZARDOUS("Hazardous Materials Storage"),
        SECURE("Secure Storage");

        private final String displayName;

        StorageType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Size Categories for storage units
     */
    public enum SizeCategory {
        SMALL("Small"),
        MEDIUM("Medium"),
        LARGE("Large"),
        EXTRA_LARGE("Extra Large"),
        CUSTOM("Custom Size");

        private final String displayName;

        SizeCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Security Levels for storage units
     */
    public enum SecurityLevel {
        STANDARD("Standard Security"),
        HIGH("High Security"),
        MAXIMUM("Maximum Security");

        private final String displayName;

        SecurityLevel(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Occupancy Status for storage units
     */
    public enum OccupancyStatus {
        VACANT("Vacant"),
        OCCUPIED("Occupied"),
        MAINTENANCE("Under Maintenance"),
        RESERVED("Reserved");

        private final String displayName;

        OccupancyStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Check if storage unit is available for lease
     */
    public boolean isAvailable() {
        return occupancyStatus == OccupancyStatus.VACANT;
    }

    /**
     * Check if storage unit is currently occupied
     */
    public boolean isOccupied() {
        return occupancyStatus == OccupancyStatus.OCCUPIED && currentTenant != null;
    }

    /**
     * Check if storage unit is under maintenance
     */
    public boolean isUnderMaintenance() {
        return occupancyStatus == OccupancyStatus.MAINTENANCE;
    }

    /**
     * Check if storage unit is reserved
     */
    public boolean isReserved() {
        return occupancyStatus == OccupancyStatus.RESERVED;
    }

    /**
     * Check if lease is currently active
     */
    public boolean hasActiveLease() {
        if (!isOccupied() || leaseStartDate == null) return false;
        
        LocalDate today = LocalDate.now();
        boolean leaseStarted = !today.isBefore(leaseStartDate);
        boolean leaseNotExpired = leaseEndDate == null || !today.isAfter(leaseEndDate);
        
        return leaseStarted && leaseNotExpired;
    }

    /**
     * Check if lease is expiring soon (within 30 days)
     */
    public boolean isLeaseExpiringSoon() {
        if (leaseEndDate == null) return false;
        
        LocalDate today = LocalDate.now();
        LocalDate warningDate = today.plusDays(30);
        
        return !leaseEndDate.isAfter(warningDate) && !leaseEndDate.isBefore(today);
    }

    /**
     * Check if lease has expired
     */
    public boolean isLeaseExpired() {
        if (leaseEndDate == null) return false;
        return LocalDate.now().isAfter(leaseEndDate);
    }

    /**
     * Calculate storage volume
     */
    public BigDecimal getVolume() {
        if (floorArea == null || height == null) return null;
        return floorArea.multiply(height);
    }

    /**
     * Get full location address within warehouse
     */
    public String getFullLocation() {
        StringBuilder location = new StringBuilder();
        
        if (zone != null) location.append("Zone: ").append(zone);
        if (aisle != null) {
            if (location.length() > 0) location.append(", ");
            location.append("Aisle: ").append(aisle);
        }
        if (rack != null) {
            if (location.length() > 0) location.append(", ");
            location.append("Rack: ").append(rack);
        }
        if (level != null) {
            if (location.length() > 0) location.append(", ");
            location.append("Level: ").append(level);
        }
        
        return location.toString();
    }

    /**
     * Check if storage unit requires special handling
     */
    public boolean requiresSpecialHandling() {
        return storageType == StorageType.HAZARDOUS || 
               storageType == StorageType.REFRIGERATED || 
               storageType == StorageType.FROZEN ||
               securityLevel == SecurityLevel.HIGH ||
               securityLevel == SecurityLevel.MAXIMUM;
    }

    /**
     * Check if storage unit is climate controlled
     */
    public boolean isClimateControlled() {
        return climateControlled || 
               storageType == StorageType.REFRIGERATED || 
               storageType == StorageType.FROZEN;
    }

    /**
     * Check if storage unit is suitable for perishable goods
     */
    public boolean suitableForPerishables() {
        return storageType == StorageType.REFRIGERATED || storageType == StorageType.FROZEN;
    }

    /**
     * Check if storage unit has high security
     */
    public boolean hasHighSecurity() {
        return securityLevel == SecurityLevel.HIGH || securityLevel == SecurityLevel.MAXIMUM;
    }

    /**
     * Get remaining lease days
     */
    public Long getRemainingLeaseDays() {
        if (leaseEndDate == null) return null;
        
        LocalDate today = LocalDate.now();
        if (today.isAfter(leaseEndDate)) return 0L;
        
        return java.time.temporal.ChronoUnit.DAYS.between(today, leaseEndDate);
    }

    /**
     * Get lease duration in days
     */
    public Long getLeaseDurationDays() {
        if (leaseStartDate == null || leaseEndDate == null) return null;
        return java.time.temporal.ChronoUnit.DAYS.between(leaseStartDate, leaseEndDate);
    }
}
