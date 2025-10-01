package com.supply.chain.microservice.productservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.supply.chain.microservice.productservice.entity.Product;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for product availability response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductAvailabilityResponse {

    private Long productId;
    private String productSku;
    private String productName;
    private Product.ProductStatus status;
    private BigDecimal totalAvailable;
    private BigDecimal totalReserved;
    private Boolean isAvailable;
    private List<ProductLocationAvailability> inventoryLocations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductLocationAvailability {
        private String location;
        private String city;
        private String country;
        private BigDecimal availableQuantity;
        private BigDecimal reservedQuantity;
        private BigDecimal lowStockThreshold;
        private Boolean isLowStock;
    }
}
