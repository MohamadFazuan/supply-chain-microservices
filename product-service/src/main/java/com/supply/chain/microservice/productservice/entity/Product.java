package com.supply.chain.microservice.productservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Product Entity for Tiong Nam Logistics
 * Represents all types of logistics products including warehouse storage,
 * freight forwarding, transportation, and custom packages
 */
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_product_code", columnList = "productCode"),
    @Index(name = "idx_category", columnList = "category"),
    @Index(name = "idx_product_type", columnList = "productType"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_name", columnList = "name"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_code", unique = true, nullable = false, length = 100)
    @NotBlank(message = "Product code is required")
    @Size(max = 100, message = "Product code must not exceed 100 characters")
    private String productCode;

    @Column(name = "sku", unique = true, length = 50)
    @Size(max = 50, message = "SKU must not exceed 50 characters")
    private String sku;

    @Column(name = "name", nullable = false, length = 200)
    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name must not exceed 200 characters")
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "short_description", length = 500)
    @Size(max = 500, message = "Short description must not exceed 500 characters")
    private String shortDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @NotNull(message = "Product category is required")
    private ProductCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false)
    @NotNull(message = "Product type is required")
    private ProductType productType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;

    // Physical Specifications stored as JSON
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "specifications", columnDefinition = "JSON")
    private JsonNode specifications;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dimensions", columnDefinition = "JSON")
    private JsonNode dimensions;

    @Column(name = "weight_capacity", precision = 15, scale = 3)
    @DecimalMin(value = "0.0", message = "Weight capacity must be non-negative")
    private BigDecimal weightCapacity;

    @Column(name = "weight_unit", length = 10)
    @Builder.Default
    private String weightUnit = "KG";

    // Service Details
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "service_areas", columnDefinition = "JSON")
    private JsonNode serviceAreas;

    @Column(name = "service_type", length = 100)
    private String serviceType;

    @Column(name = "special_requirements", columnDefinition = "TEXT")
    private String specialRequirements;

    // Pricing Information
    @Column(name = "base_price", precision = 15, scale = 2)
    @DecimalMin(value = "0.0", message = "Base price must be non-negative")
    private BigDecimal basePrice;

    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "MYR";

    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_model")
    @Builder.Default
    private PricingModel pricingModel = PricingModel.FIXED;

    // Metadata
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", columnDefinition = "JSON")
    private JsonNode tags;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "images", columnDefinition = "JSON")
    private JsonNode images;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "documents", columnDefinition = "JSON")
    private JsonNode documents;

    // Relationships
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<ProductInventory> inventory = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<ProductPricing> pricing = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<ProductAttribute> attributes = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<TransportationFleet> transportationFleet = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<WarehouseStorageUnit> storageUnits = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<FreightForwardingRoute> freightRoutes = new ArrayList<>();

    // Audit fields
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "version")
    @Version
    @Builder.Default
    private Integer version = 1;

    /**
     * Product Types for Tiong Nam Logistics
     */
    public enum ProductType {
        WAREHOUSE_STORAGE("Warehouse Storage"),
        FREIGHT_FORWARDING("Freight Forwarding"),
        TRANSPORTATION("Transportation"),
        CUSTOM_PACKAGE("Custom Package");

        private final String displayName;

        ProductType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Product Status
     */
    public enum ProductStatus {
        ACTIVE("Active"),
        INACTIVE("Inactive"),
        DISCONTINUED("Discontinued"),
        COMING_SOON("Coming Soon"),
        PENDING_APPROVAL("Pending Approval"),
        DELETED("Deleted");

        private final String displayName;

        ProductStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Pricing Models
     */
    public enum PricingModel {
        FIXED("Fixed Price"),
        TIERED("Tiered Pricing"),
        QUOTE_BASED("Quote Based");

        private final String displayName;

        PricingModel(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Get the category path for this product
     */
    public String getCategoryPath() {
        return category != null ? category.getCategoryPath() : "";
    }

    /**
     * Check if the product is available
     */
    public boolean isAvailable() {
        return status == ProductStatus.ACTIVE;
    }

    /**
     * Check if the product has active inventory
     */
    public boolean hasActiveInventory() {
        return inventory.stream()
                .anyMatch(inv -> inv.getAvailable() && inv.getAvailableQuantity() > 0);
    }

    /**
     * Get total available quantity across all locations
     */
    public int getTotalAvailableQuantity() {
        return inventory.stream()
                .filter(ProductInventory::getAvailable)
                .mapToInt(ProductInventory::getAvailableQuantity)
                .sum();
    }

    /**
     * Check if product is warehouse storage type
     */
    public boolean isWarehouseProduct() {
        return productType == ProductType.WAREHOUSE_STORAGE;
    }

    /**
     * Check if product is freight forwarding type
     */
    public boolean isFreightProduct() {
        return productType == ProductType.FREIGHT_FORWARDING;
    }

    /**
     * Check if product is transportation type
     */
    public boolean isTransportationProduct() {
        return productType == ProductType.TRANSPORTATION;
    }

    /**
     * Check if product is custom package type
     */
    public boolean isCustomPackage() {
        return productType == ProductType.CUSTOM_PACKAGE;
    }

    /**
     * Get product name (alias for getName())
     */
    public String getProductName() {
        return this.name;
    }

    /**
     * Set product name (alias for setName())
     */
    public void setProductName(String productName) {
        this.name = productName;
    }

    // Inner Enums
    public enum PricingTier {
        STANDARD,
        PREMIUM,
        BULK,
        PROMOTIONAL,
        ENTERPRISE
    }

    public enum PricingStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED,
        ARCHIVED
    }
}
