package com.supply.chain.microservice.productservice.repository;

import com.supply.chain.microservice.productservice.entity.ProductInventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ProductInventory entities
 */
@Repository
public interface ProductInventoryRepository extends JpaRepository<ProductInventory, Long> {

    /**
     * Find inventory by product ID
     */
    List<ProductInventory> findByProductId(Long productId);

    /**
     * Find inventory by product ID and location
     */
    Optional<ProductInventory> findByProductIdAndWarehouseLocationIgnoreCase(Long productId, String warehouseLocation);

    /**
     * Find inventory with low stock
     */
    @Query("SELECT pi FROM ProductInventory pi WHERE pi.availableQuantity <= pi.lowStockThreshold")
    List<ProductInventory> findLowStockInventory();

    /**
     * Find inventory with low stock for specific product
     */
    @Query("SELECT pi FROM ProductInventory pi WHERE pi.product.id = :productId AND pi.availableQuantity <= pi.lowStockThreshold")
    List<ProductInventory> findLowStockInventoryForProduct(@Param("productId") Long productId);

    /**
     * Find inventory by location
     */
    @Query("SELECT pi FROM ProductInventory pi WHERE pi.warehouseLocation LIKE %:location% OR pi.city LIKE %:location%")
    Page<ProductInventory> findByLocation(@Param("location") String location, Pageable pageable);

    /**
     * Find available inventory (quantity > 0)
     */
    @Query("SELECT pi FROM ProductInventory pi WHERE pi.availableQuantity > 0")
    Page<ProductInventory> findAvailableInventory(Pageable pageable);

    /**
     * Find available inventory for product
     */
    @Query("SELECT pi FROM ProductInventory pi WHERE pi.product.id = :productId AND pi.availableQuantity > 0")
    List<ProductInventory> findAvailableInventoryForProduct(@Param("productId") Long productId);

    /**
     * Get total available quantity for product
     */
    @Query("SELECT COALESCE(SUM(pi.availableQuantity), 0) FROM ProductInventory pi WHERE pi.product.id = :productId")
    BigDecimal getTotalAvailableQuantityForProduct(@Param("productId") Long productId);

    /**
     * Get total reserved quantity for product
     */
    @Query("SELECT COALESCE(SUM(pi.reservedQuantity), 0) FROM ProductInventory pi WHERE pi.product.id = :productId")
    BigDecimal getTotalReservedQuantityForProduct(@Param("productId") Long productId);

    /**
     * Find inventory by multiple locations
     */
    @Query("SELECT pi FROM ProductInventory pi WHERE pi.warehouseLocation IN :locations")
    List<ProductInventory> findByLocations(@Param("locations") List<String> locations);

    /**
     * Find inventory updated after date
     */
    @Query("SELECT pi FROM ProductInventory pi WHERE pi.updatedAt > :fromDate ORDER BY pi.updatedAt DESC")
    List<ProductInventory> findRecentlyUpdatedInventory(@Param("fromDate") LocalDateTime fromDate);

    /**
     * Find inventory by city
     */
    List<ProductInventory> findByCityIgnoreCase(String city);

    /**
     * Find inventory by country
     */
    List<ProductInventory> findByCountryIgnoreCase(String country);

    /**
     * Check if inventory exists for product and location
     */
    @Query("SELECT COUNT(pi) > 0 FROM ProductInventory pi WHERE pi.product.id = :productId AND pi.warehouseLocation = :location")
    boolean existsByProductIdAndLocation(@Param("productId") Long productId, @Param("location") String location);

    /**
     * Reserve inventory
     */
    @Modifying
    @Query("UPDATE ProductInventory pi SET pi.reservedQuantity = pi.reservedQuantity + :quantity, " +
           "pi.availableQuantity = pi.availableQuantity - :quantity, pi.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE pi.id = :inventoryId AND pi.availableQuantity >= :quantity")
    int reserveInventory(@Param("inventoryId") Long inventoryId, @Param("quantity") BigDecimal quantity);

    /**
     * Release reserved inventory
     */
    @Modifying
    @Query("UPDATE ProductInventory pi SET pi.reservedQuantity = pi.reservedQuantity - :quantity, " +
           "pi.availableQuantity = pi.availableQuantity + :quantity, pi.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE pi.id = :inventoryId AND pi.reservedQuantity >= :quantity")
    int releaseReservedInventory(@Param("inventoryId") Long inventoryId, @Param("quantity") BigDecimal quantity);

    /**
     * Update available quantity
     */
    @Modifying
    @Query("UPDATE ProductInventory pi SET pi.availableQuantity = :quantity, pi.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE pi.id = :inventoryId")
    int updateAvailableQuantity(@Param("inventoryId") Long inventoryId, @Param("quantity") BigDecimal quantity);

    /**
     * Find inventory requiring reorder
     */
    @Query("SELECT pi FROM ProductInventory pi WHERE pi.availableQuantity <= pi.reorderPoint")
    List<ProductInventory> findInventoryRequiringReorder();

    /**
     * Find overstocked inventory
     */
    @Query("SELECT pi FROM ProductInventory pi WHERE pi.maxStockLevel IS NOT NULL AND " +
           "(pi.availableQuantity + pi.reservedQuantity) > pi.maxStockLevel")
    List<ProductInventory> findOverstockedInventory();

    /**
     * Get inventory statistics by location
     */
    @Query("SELECT pi.warehouseLocation, COUNT(pi), SUM(pi.availableQuantity), AVG(pi.availableQuantity) " +
           "FROM ProductInventory pi GROUP BY pi.warehouseLocation")
    List<Object[]> getInventoryStatisticsByLocation();

    /**
     * Get inventory statistics by product
     */
    @Query("SELECT p.productName, COUNT(pi), SUM(pi.availableQuantity), AVG(pi.availableQuantity) " +
           "FROM ProductInventory pi JOIN pi.product p GROUP BY p.id, p.productName")
    List<Object[]> getInventoryStatisticsByProduct();

    /**
     * Find inventory with availability between dates
     */
    @Query("SELECT pi FROM ProductInventory pi WHERE " +
           "(:fromDate IS NULL OR pi.availableFromDate IS NULL OR pi.availableFromDate <= :fromDate) AND " +
           "(:toDate IS NULL OR pi.availableUntilDate IS NULL OR pi.availableUntilDate >= :toDate)")
    List<ProductInventory> findAvailableBetweenDates(@Param("fromDate") LocalDateTime fromDate, 
                                                    @Param("toDate") LocalDateTime toDate);

    /**
     * Find expired inventory
     */
    @Query("SELECT pi FROM ProductInventory pi WHERE pi.availableUntilDate IS NOT NULL AND pi.availableUntilDate < CURRENT_TIMESTAMP")
    List<ProductInventory> findExpiredInventory();

    /**
     * Count inventory locations for product
     */
    @Query("SELECT COUNT(DISTINCT pi.warehouseLocation) FROM ProductInventory pi WHERE pi.product.id = :productId")
    long countInventoryLocationsForProduct(@Param("productId") Long productId);

    /**
     * Find inventory with specific quantity threshold
     */
    @Query("SELECT pi FROM ProductInventory pi WHERE pi.availableQuantity BETWEEN :minQuantity AND :maxQuantity")
    List<ProductInventory> findByQuantityRange(@Param("minQuantity") BigDecimal minQuantity, 
                                              @Param("maxQuantity") BigDecimal maxQuantity);

    /**
     * Get inventory value by location
     */
    @Query("SELECT pi.warehouseLocation, SUM(pi.availableQuantity * p.weight) " +
           "FROM ProductInventory pi JOIN pi.product p " +
           "WHERE p.weight IS NOT NULL " +
           "GROUP BY pi.warehouseLocation")
    List<Object[]> getInventoryValueByLocation();

    /**
     * Find products without inventory
     */
    @Query("SELECT p FROM Product p WHERE p.id NOT IN " +
           "(SELECT DISTINCT pi.product.id FROM ProductInventory pi)")
    List<com.supply.chain.microservice.productservice.entity.Product> findProductsWithoutInventory();

    /**
     * Bulk update low stock thresholds
     */
    @Modifying
    @Query("UPDATE ProductInventory pi SET pi.lowStockThreshold = :threshold WHERE pi.warehouseLocation = :location")
    int updateLowStockThresholdByLocation(@Param("location") String location, @Param("threshold") BigDecimal threshold);
}
