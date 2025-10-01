package com.supply.chain.microservice.productservice.dto;

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
 * DTO for updating an existing product
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateRequest {

    @Size(max = 200, message = "Product name must not exceed 200 characters")
    private String productName;

    @Size(max = 100, message = "SKU must not exceed 100 characters")
    private String sku;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private Long categoryId;

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

    private Boolean dangerousGoods;

    private Boolean temperatureControlled;

    private Boolean fragile;

    private Boolean featured;

    private Integer displayOrder;

    private String imageUrl;

    private Map<String, Object> specifications;

    private Map<String, Object> attributes;

    private String tags;

    private Product.ProductStatus status;
}
