package com.supply.chain.microservice.productservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.supply.chain.microservice.productservice.entity.Product;

import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO for creating a new product
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name must not exceed 200 characters")
    private String productName;

    @Size(max = 100, message = "SKU must not exceed 100 characters")
    private String sku;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull(message = "Product type is required")
    private Product.ProductType productType;

    @Positive(message = "Weight must be positive")
    private BigDecimal weight;

    @Positive(message = "Length must be positive")
    private BigDecimal length;

    @Positive(message = "Width must be positive")
    private BigDecimal width;

    @Positive(message = "Height must be positive")
    private BigDecimal height;

    private String manufacturer;

    private String model;

    private String serialNumber;

    private Boolean dangerousGoods = false;

    private Boolean temperatureControlled = false;

    private Boolean fragile = false;

    private Boolean featured = false;

    private Integer displayOrder;

    private String imageUrl;

    private Map<String, Object> specifications;

    private Map<String, Object> attributes;

    private String tags;

    // Pricing information
    @NotNull(message = "Base price is required")
    @Positive(message = "Base price must be positive")
    private BigDecimal basePrice;

    private String currency = "MYR";

    private Product.PricingTier pricingTier = Product.PricingTier.STANDARD;

    // Initial inventory (optional)
    private String warehouseLocation;

    private String city;

    private String country;

    @Positive(message = "Initial quantity must be positive")
    private BigDecimal initialQuantity;

    @Positive(message = "Low stock threshold must be positive")
    private BigDecimal lowStockThreshold;
}
