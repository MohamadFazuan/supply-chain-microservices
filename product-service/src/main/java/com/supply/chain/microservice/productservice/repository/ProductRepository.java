package com.supply.chain.microservice.productservice.repository;

import com.supply.chain.microservice.productservice.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Product entities
 * Provides data access layer for product management
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    /**
     * Find product by SKU
     */
    Optional<Product> findBySkuIgnoreCase(String sku);

    /**
     * Find products by category
     */
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    /**
     * Find products by category code
     */
    @Query("SELECT p FROM Product p JOIN p.category c WHERE c.categoryCode = :categoryCode AND p.status = 'ACTIVE'")
    Page<Product> findByCategoryCode(@Param("categoryCode") String categoryCode, Pageable pageable);

    /**
     * Find active products
     */
    Page<Product> findByStatus(Product.ProductStatus status, Pageable pageable);

    /**
     * Find products by product type
     */
    Page<Product> findByProductType(Product.ProductType productType, Pageable pageable);

    /**
     * Search products by name or description
     */
    @Query("SELECT p FROM Product p WHERE " +
           "(LOWER(p.productName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "p.status = 'ACTIVE'")
    Page<Product> searchProducts(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find products with price range
     */
    @Query("SELECT DISTINCT p FROM Product p JOIN p.pricing pr WHERE " +
           "pr.basePrice BETWEEN :minPrice AND :maxPrice AND p.status = 'ACTIVE'")
    Page<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice, 
                                  @Param("maxPrice") BigDecimal maxPrice, 
                                  Pageable pageable);

    /**
     * Find products by multiple categories
     */
    @Query("SELECT p FROM Product p WHERE p.category.id IN :categoryIds AND p.status = 'ACTIVE'")
    Page<Product> findByCategories(@Param("categoryIds") List<Long> categoryIds, Pageable pageable);

    /**
     * Find featured products
     */
    @Query("SELECT p FROM Product p WHERE p.featured = true AND p.status = 'ACTIVE' ORDER BY p.displayOrder, p.createdAt DESC")
    List<Product> findFeaturedProducts(Pageable pageable);

    /**
     * Find products requiring approval
     */
    @Query("SELECT p FROM Product p WHERE p.status IN ('PENDING_APPROVAL', 'UNDER_REVIEW')")
    Page<Product> findProductsRequiringApproval(Pageable pageable);

    /**
     * Find products by SKU pattern
     */
    @Query("SELECT p FROM Product p WHERE p.sku LIKE :skuPattern")
    List<Product> findBySkuPattern(@Param("skuPattern") String skuPattern);

    /**
     * Count products by category
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId AND p.status = 'ACTIVE'")
    long countByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * Count products by status
     */
    long countByStatus(Product.ProductStatus status);

    /**
     * Find products with low stock
     */
    @Query("SELECT DISTINCT p FROM Product p JOIN p.inventory inv WHERE " +
           "inv.availableQuantity <= inv.lowStockThreshold AND p.status = 'ACTIVE'")
    List<Product> findProductsWithLowStock();

    /**
     * Find products without inventory
     */
    @Query("SELECT p FROM Product p WHERE p.id NOT IN " +
           "(SELECT DISTINCT pi.product.id FROM ProductInventory pi) AND p.status = 'ACTIVE'")
    List<Product> findProductsWithoutInventory();

    /**
     * Find products by weight range
     */
    @Query("SELECT p FROM Product p WHERE " +
           "(:minWeight IS NULL OR p.weight >= :minWeight) AND " +
           "(:maxWeight IS NULL OR p.weight <= :maxWeight) AND " +
           "p.status = 'ACTIVE'")
    Page<Product> findByWeightRange(@Param("minWeight") BigDecimal minWeight, 
                                   @Param("maxWeight") BigDecimal maxWeight, 
                                   Pageable pageable);

    /**
     * Find products by dimensions
     */
    @Query("SELECT p FROM Product p WHERE " +
           "p.length <= :maxLength AND p.width <= :maxWidth AND p.height <= :maxHeight AND " +
           "p.status = 'ACTIVE'")
    Page<Product> findByMaxDimensions(@Param("maxLength") BigDecimal maxLength,
                                     @Param("maxWidth") BigDecimal maxWidth,
                                     @Param("maxHeight") BigDecimal maxHeight,
                                     Pageable pageable);

    /**
     * Find products suitable for dangerous goods
     */
    @Query("SELECT p FROM Product p WHERE p.dangerousGoods = true AND p.status = 'ACTIVE'")
    Page<Product> findDangerousGoodsProducts(Pageable pageable);

    /**
     * Find temperature controlled products
     */
    @Query("SELECT p FROM Product p WHERE p.temperatureControlled = true AND p.status = 'ACTIVE'")
    Page<Product> findTemperatureControlledProducts(Pageable pageable);

    /**
     * Find products updated after date
     */
    @Query("SELECT p FROM Product p WHERE p.updatedAt > :fromDate ORDER BY p.updatedAt DESC")
    List<Product> findRecentlyUpdatedProducts(@Param("fromDate") java.time.LocalDateTime fromDate);

    /**
     * Get product statistics by category
     */
    @Query("SELECT p.category.categoryName, COUNT(p), AVG(pr.basePrice) FROM Product p " +
           "LEFT JOIN p.pricing pr WHERE p.status = 'ACTIVE' " +
           "GROUP BY p.category.id, p.category.categoryName")
    List<Object[]> getProductStatisticsByCategory();

    /**
     * Find similar products (same category, exclude current product)
     */
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.id != :excludeId AND p.status = 'ACTIVE'")
    List<Product> findSimilarProducts(@Param("categoryId") Long categoryId, 
                                     @Param("excludeId") Long excludeId, 
                                     Pageable pageable);

    /**
     * Check if SKU exists (excluding specific product ID)
     */
    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.sku = :sku AND (:excludeId IS NULL OR p.id != :excludeId)")
    boolean existsBySkuAndIdNot(@Param("sku") String sku, @Param("excludeId") Long excludeId);

    /**
     * Find products by multiple product types
     */
    @Query("SELECT p FROM Product p WHERE p.productType IN :productTypes AND p.status = 'ACTIVE'")
    Page<Product> findByProductTypes(@Param("productTypes") List<Product.ProductType> productTypes, Pageable pageable);

    /**
     * Find products suitable for specific transport mode
     */
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN TransportationFleet tf ON tf.product.id = p.id " +
           "LEFT JOIN FreightForwardingRoute ffr ON ffr.product.id = p.id " +
           "WHERE (tf.vehicleType = :transportMode OR ffr.primaryTransportMode = :transportMode) " +
           "AND p.status = 'ACTIVE'")
    Page<Product> findByTransportMode(@Param("transportMode") String transportMode, Pageable pageable);

    /**
     * Find products available in specific location
     */
    @Query("SELECT DISTINCT p FROM Product p JOIN p.inventory inv " +
           "WHERE (inv.warehouseLocation LIKE %:location% OR inv.city LIKE %:location%) " +
           "AND inv.availableQuantity > 0 AND p.status = 'ACTIVE'")
    Page<Product> findAvailableInLocation(@Param("location") String location, Pageable pageable);

    /**
     * Complex search with multiple criteria
     */
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN p.category c " +
           "LEFT JOIN p.pricing pr " +
           "WHERE (:categoryId IS NULL OR c.id = :categoryId) " +
           "AND (:productType IS NULL OR p.productType = :productType) " +
           "AND (:minPrice IS NULL OR pr.basePrice >= :minPrice) " +
           "AND (:maxPrice IS NULL OR pr.basePrice <= :maxPrice) " +
           "AND (:searchTerm IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND p.status = 'ACTIVE'")
    Page<Product> searchWithCriteria(@Param("categoryId") Long categoryId,
                                   @Param("productType") Product.ProductType productType,
                                   @Param("minPrice") BigDecimal minPrice,
                                   @Param("maxPrice") BigDecimal maxPrice,
                                   @Param("searchTerm") String searchTerm,
                                   Pageable pageable);
}
