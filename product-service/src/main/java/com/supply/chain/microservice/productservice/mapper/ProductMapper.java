package com.supply.chain.microservice.productservice.mapper;

import com.supply.chain.microservice.productservice.dto.ProductCreateRequest;
import com.supply.chain.microservice.productservice.dto.ProductResponse;
import com.supply.chain.microservice.productservice.dto.ProductUpdateRequest;
import com.supply.chain.microservice.productservice.entity.Product;
import com.supply.chain.microservice.productservice.entity.ProductInventory;
import com.supply.chain.microservice.productservice.entity.ProductPricing;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * Mapper for Product entities and DTOs
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {

    /**
     * Convert Product entity to ProductResponse DTO
     */
    @Mapping(target = "category", source = "category")
    @Mapping(target = "pricing", source = "pricing")
    @Mapping(target = "inventorySummary", expression = "java(mapInventorySummary(product.getInventory()))")
    @Mapping(target = "currentPrice", expression = "java(getCurrentPrice(product.getPricing()))")
    @Mapping(target = "specifications", ignore = true)
    @Mapping(target = "attributes", ignore = true)
    @Mapping(target = "tags", ignore = true)
    ProductResponse toResponse(Product product);

    /**
     * Convert ProductCreateRequest to Product entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "pricing", ignore = true)
    @Mapping(target = "inventory", ignore = true)
    @Mapping(target = "attributes", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "specifications", ignore = true)
    @Mapping(target = "tags", ignore = true)
    Product toEntity(ProductCreateRequest request);

    /**
     * Update Product entity from ProductUpdateRequest
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "pricing", ignore = true)
    @Mapping(target = "inventory", ignore = true)
    @Mapping(target = "attributes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "specifications", ignore = true)
    @Mapping(target = "tags", ignore = true)
    Product updateEntity(@MappingTarget Product product, ProductUpdateRequest request);

    /**
     * Map category to category response
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "categoryCode", source = "categoryCode")
    @Mapping(target = "categoryName", source = "name")
    @Mapping(target = "hierarchyPath", expression = "java(category.getCategoryPath())")
    @Mapping(target = "level", source = "level")
    ProductResponse.ProductCategoryResponse toCategoryResponse(com.supply.chain.microservice.productservice.entity.ProductCategory category);

    /**
     * Map pricing list to pricing response list
     */
    List<ProductResponse.ProductPricingResponse> toPricingResponseList(List<ProductPricing> pricing);

    /**
     * Map pricing to pricing response
     */
    @Mapping(target = "status", expression = "java(mapBooleanToPricingStatus(pricing.getActive()))")
    ProductResponse.ProductPricingResponse toPricingResponse(ProductPricing pricing);

    /**
     * Map inventory summary
     */
    default ProductResponse.ProductInventorySummary mapInventorySummary(List<ProductInventory> inventory) {
        if (inventory == null || inventory.isEmpty()) {
            return ProductResponse.ProductInventorySummary.builder()
                .totalAvailableQuantity(java.math.BigDecimal.ZERO)
                .totalReservedQuantity(java.math.BigDecimal.ZERO)
                .locationCount(0)
                .lowStock(false)
                .locations(java.util.Collections.emptyList())
                .build();
        }

        java.math.BigDecimal totalAvailable = inventory.stream()
            .map(ProductInventory::getAvailableQuantity)
            .filter(qty -> qty != null)
            .map(java.math.BigDecimal::valueOf)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        java.math.BigDecimal totalReserved = inventory.stream()
            .map(ProductInventory::getReservedQuantity)
            .filter(qty -> qty != null)
            .map(java.math.BigDecimal::valueOf)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        boolean lowStock = inventory.stream().anyMatch(ProductInventory::isLowStock);

        List<ProductResponse.ProductLocationInventory> locations = inventory.stream()
            .map(inv -> ProductResponse.ProductLocationInventory.builder()
                .warehouseLocation(inv.getWarehouseLocation())
                .city(inv.getCity())
                .country(inv.getCountry())
                .availableQuantity(inv.getAvailableQuantity() != null ? java.math.BigDecimal.valueOf(inv.getAvailableQuantity()) : java.math.BigDecimal.ZERO)
                .reservedQuantity(inv.getReservedQuantity() != null ? java.math.BigDecimal.valueOf(inv.getReservedQuantity()) : java.math.BigDecimal.ZERO)
                .lowStock(inv.isLowStock())
                .build())
            .toList();

        return ProductResponse.ProductInventorySummary.builder()
            .totalAvailableQuantity(totalAvailable)
            .totalReservedQuantity(totalReserved)
            .locationCount(inventory.size())
            .lowStock(lowStock)
            .locations(locations)
            .build();
    }

    /**
     * Get current price from pricing list
     */
    default java.math.BigDecimal getCurrentPrice(List<ProductPricing> pricing) {
        if (pricing == null || pricing.isEmpty()) {
            return null;
        }

        java.time.LocalDate now = java.time.LocalDate.now();
        
        return pricing.stream()
            .filter(p -> Boolean.TRUE.equals(p.getActive()))
            .filter(p -> p.getEffectiveFromDate() == null || !p.getEffectiveFromDate().isAfter(now))
            .filter(p -> p.getEffectiveToDate() == null || !p.getEffectiveToDate().isBefore(now))
            .map(ProductPricing::getBasePrice)
            .findFirst()
            .orElse(null);
    }

    /**
     * Map Boolean active field to PricingStatus enum
     */
    default Product.PricingStatus mapBooleanToPricingStatus(Boolean active) {
        if (active == null) {
            return Product.PricingStatus.INACTIVE;
        }
        return Boolean.TRUE.equals(active) ? Product.PricingStatus.ACTIVE : Product.PricingStatus.INACTIVE;
    }
}
