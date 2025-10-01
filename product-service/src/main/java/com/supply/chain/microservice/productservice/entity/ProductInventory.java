package com.supply.chain.microservice.productservice.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Product Inventory Entity
 * Manages stock levels and availability across different locations
 */
@Entity
@Table(name = "product_inventory", 
    uniqueConstraints = @UniqueConstraint(
        name = "unique_product_location", 
        columnNames = {"product_id", "location_code"}
    ),
    indexes = {
        @Index(name = "idx_product", columnList = "product"),
        @Index(name = "idx_location", columnList = "locationCode"),
        @Index(name = "idx_availability", columnList = "available"),
        @Index(name = "idx_warehouse", columnList = "warehouseId")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull(message = "Product is required")
    private Product product;

    @Column(name = "location_code", nullable = false, length = 50)
    @NotBlank(message = "Location code is required")
    @Size(max = 50, message = "Location code must not exceed 50 characters")
    private String locationCode;

    @Column(name = "warehouse_id", length = 100)
    private String warehouseId;

    @Column(name = "warehouse_location", length = 200)
    private String warehouseLocation;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "country", length = 100)
    private String country;

    // Stock Information
    @Column(name = "available_quantity")
    @Min(value = 0, message = "Available quantity must be non-negative")
    @Builder.Default
    private Integer availableQuantity = 0;

    @Column(name = "reserved_quantity")
    @Min(value = 0, message = "Reserved quantity must be non-negative")
    @Builder.Default
    private Integer reservedQuantity = 0;

    @Column(name = "total_capacity")
    @Min(value = 0, message = "Total capacity must be non-negative")
    @Builder.Default
    private Integer totalCapacity = 0;

    @Column(name = "minimum_stock_level")
    @Min(value = 0, message = "Minimum stock level must be non-negative")
    @Builder.Default
    private Integer minimumStockLevel = 0;

    // Availability
    @Column(name = "is_available")
    @Builder.Default
    private Boolean available = true;

    @Column(name = "availability_start_date")
    private LocalDate availabilityStartDate;

    @Column(name = "availability_end_date")
    private LocalDate availabilityEndDate;

    // Location Details stored as JSON
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "location_details", columnDefinition = "JSON")
    private JsonNode locationDetails;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "coordinates", columnDefinition = "JSON")
    private JsonNode coordinates;

    // Tracking
    @LastModifiedDate
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    /**
     * Get total occupied quantity (available + reserved)
     */
    public Integer getTotalOccupiedQuantity() {
        return (availableQuantity != null ? availableQuantity : 0) + 
               (reservedQuantity != null ? reservedQuantity : 0);
    }

    /**
     * Get remaining capacity
     */
    public Integer getRemainingCapacity() {
        if (totalCapacity == null) return null;
        return totalCapacity - getTotalOccupiedQuantity();
    }

    /**
     * Check if stock is below minimum level
     */
    public boolean isLowStock() {
        if (minimumStockLevel == null || availableQuantity == null) return false;
        return availableQuantity <= minimumStockLevel;
    }

    /**
     * Check if location has capacity for additional stock
     */
    public boolean hasCapacity(int quantity) {
        Integer remaining = getRemainingCapacity();
        return remaining != null && remaining >= quantity;
    }

    /**
     * Check if currently available (within date range and status)
     */
    public boolean isCurrentlyAvailable() {
        if (!available) return false;
        
        LocalDate today = LocalDate.now();
        
        if (availabilityStartDate != null && today.isBefore(availabilityStartDate)) {
            return false;
        }
        
        if (availabilityEndDate != null && today.isAfter(availabilityEndDate)) {
            return false;
        }
        
        return true;
    }

    /**
     * Check if can fulfill the requested quantity
     */
    public boolean canFulfill(int requestedQuantity) {
        return isCurrentlyAvailable() && 
               availableQuantity != null && 
               availableQuantity >= requestedQuantity;
    }

    /**
     * Reserve stock quantity
     */
    public boolean reserveStock(int quantity) {
        if (!canFulfill(quantity)) return false;
        
        this.availableQuantity -= quantity;
        this.reservedQuantity = (this.reservedQuantity != null ? this.reservedQuantity : 0) + quantity;
        return true;
    }

    /**
     * Release reserved stock
     */
    public void releaseReservedStock(int quantity) {
        if (this.reservedQuantity != null && this.reservedQuantity >= quantity) {
            this.reservedQuantity -= quantity;
            this.availableQuantity = (this.availableQuantity != null ? this.availableQuantity : 0) + quantity;
        }
    }

    /**
     * Confirm reserved stock (convert to unavailable)
     */
    public boolean confirmReservedStock(int quantity) {
        if (this.reservedQuantity != null && this.reservedQuantity >= quantity) {
            this.reservedQuantity -= quantity;
            return true;
        }
        return false;
    }

    /**
     * Add new stock
     */
    public boolean addStock(int quantity) {
        if (!hasCapacity(quantity)) return false;
        
        this.availableQuantity = (this.availableQuantity != null ? this.availableQuantity : 0) + quantity;
        return true;
    }

    /**
     * Get utilization percentage
     */
    public Double getUtilizationPercentage() {
        if (totalCapacity == null || totalCapacity == 0) return null;
        return (double) getTotalOccupiedQuantity() / totalCapacity * 100;
    }

    /**
     * Alias for minimumStockLevel (for compatibility)
     */
    public Integer getLowStockThreshold() {
        return this.minimumStockLevel;
    }
}
