package com.supply.chain.microservice.productservice.repository;

import com.supply.chain.microservice.productservice.entity.ProductPricing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ProductPricing entities
 */
@Repository
public interface ProductPricingRepository extends JpaRepository<ProductPricing, Long> {

    /**
     * Find pricing by product ID
     */
    List<ProductPricing> findByProductId(Long productId);

    /**
     * Find active pricing by product ID
     */
    @Query("SELECT pp FROM ProductPricing pp WHERE pp.product.id = :productId AND pp.status = 'ACTIVE'")
    List<ProductPricing> findActiveByProductId(@Param("productId") Long productId);

    /**
     * Find current effective pricing
     */
    @Query("SELECT pp FROM ProductPricing pp WHERE pp.product.id = :productId AND pp.status = 'ACTIVE' " +
           "AND (:currentDate BETWEEN pp.effectiveFromDate AND pp.effectiveToDate OR pp.effectiveToDate IS NULL)")
    List<ProductPricing> findCurrentEffectivePricing(@Param("productId") Long productId, 
                                                     @Param("currentDate") LocalDateTime currentDate);

    /**
     * Find pricing by tier
     */
    @Query("SELECT pp FROM ProductPricing pp WHERE pp.pricingTier = :tier AND pp.status = 'ACTIVE'")
    List<ProductPricing> findByPricingTier(@Param("tier") ProductPricing.PricingTier tier);

    /**
     * Find pricing by price range
     */
    @Query("SELECT pp FROM ProductPricing pp WHERE pp.basePrice BETWEEN :minPrice AND :maxPrice AND pp.status = 'ACTIVE'")
    Page<ProductPricing> findByPriceRange(@Param("minPrice") BigDecimal minPrice, 
                                         @Param("maxPrice") BigDecimal maxPrice, 
                                         Pageable pageable);

    /**
     * Find seasonal pricing
     */
    @Query("SELECT pp FROM ProductPricing pp WHERE pp.seasonalAdjustment IS NOT NULL AND pp.status = 'ACTIVE'")
    List<ProductPricing> findSeasonalPricing();

    /**
     * Find expired pricing
     */
    @Query("SELECT pp FROM ProductPricing pp WHERE pp.effectiveToDate IS NOT NULL AND pp.effectiveToDate < CURRENT_TIMESTAMP")
    List<ProductPricing> findExpiredPricing();

    /**
     * Find pricing requiring approval
     */
    @Query("SELECT pp FROM ProductPricing pp WHERE pp.status IN ('PENDING_APPROVAL', 'UNDER_REVIEW')")
    Page<ProductPricing> findPricingRequiringApproval(Pageable pageable);

    /**
     * Get lowest price for product
     */
    @Query("SELECT MIN(pp.basePrice) FROM ProductPricing pp WHERE pp.product.id = :productId AND pp.status = 'ACTIVE'")
    Optional<BigDecimal> findLowestPriceForProduct(@Param("productId") Long productId);

    /**
     * Get highest price for product
     */
    @Query("SELECT MAX(pp.basePrice) FROM ProductPricing pp WHERE pp.product.id = :productId AND pp.status = 'ACTIVE'")
    Optional<BigDecimal> findHighestPriceForProduct(@Param("productId") Long productId);
}
