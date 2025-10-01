package com.supply.chain.microservice.productservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.supply.chain.microservice.productservice.entity.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO for product response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private String sku;
    private String productName;
    private String description;
    private Product.ProductType productType;
    private Product.ProductStatus status;

    // Category information
    private ProductCategoryResponse category;

    // Physical properties
    private BigDecimal weight;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;

    // Product details
    private String manufacturer;
    private String model;
    private String serialNumber;
    private Boolean dangerousGoods;
    private Boolean temperatureControlled;
    private Boolean fragile;
    private Boolean featured;
    private Integer displayOrder;
    private String imageUrl;
    private Map<String, Object> specifications;
    private Map<String, Object> attributes;
    private String tags;

    // Pricing information
    private List<ProductPricingResponse> pricing;
    private BigDecimal currentPrice;
    private String currency;

    // Inventory summary
    private ProductInventorySummary inventorySummary;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductCategoryResponse {
        private Long id;
        private String categoryCode;
        private String categoryName;
        private String hierarchyPath;
        private Integer level;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductPricingResponse {
        private Long id;
        private BigDecimal basePrice;
        private String currency;
        private Product.PricingTier pricingTier;
        private BigDecimal discountPercentage;
        private BigDecimal seasonalAdjustment;
        private LocalDateTime effectiveFromDate;
        private LocalDateTime effectiveToDate;
        private Product.PricingStatus status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductInventorySummary {
        private BigDecimal totalAvailableQuantity;
        private BigDecimal totalReservedQuantity;
        private Integer locationCount;
        private Boolean lowStock;
        private List<ProductLocationInventory> locations;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductLocationInventory {
        private String warehouseLocation;
        private String city;
        private String country;
        private BigDecimal availableQuantity;
        private BigDecimal reservedQuantity;
        private Boolean lowStock;
    }
}
