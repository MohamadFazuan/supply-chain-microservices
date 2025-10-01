package com.supply.chain.microservice.productservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.supply.chain.microservice.productservice.entity.Product;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for product search criteria
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchCriteria {

    private String searchTerm;
    private Long categoryId;
    private List<Long> categoryIds;
    private Product.ProductType productType;
    private List<Product.ProductType> productTypes;
    private Product.ProductStatus status;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal minWeight;
    private BigDecimal maxWeight;
    private Boolean featured;
    private Boolean dangerousGoods;
    private Boolean temperatureControlled;
    private String location;
    private String manufacturer;
    private String tags;

    /**
     * Check if criteria is empty
     */
    public boolean isEmpty() {
        return searchTerm == null && categoryId == null && 
               (categoryIds == null || categoryIds.isEmpty()) &&
               productType == null && (productTypes == null || productTypes.isEmpty()) &&
               status == null && minPrice == null && maxPrice == null &&
               minWeight == null && maxWeight == null && featured == null &&
               dangerousGoods == null && temperatureControlled == null &&
               location == null && manufacturer == null && tags == null;
    }
}
