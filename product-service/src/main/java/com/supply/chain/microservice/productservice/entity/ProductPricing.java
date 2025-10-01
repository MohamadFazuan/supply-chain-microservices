package com.supply.chain.microservice.productservice.entity;

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
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Product Pricing Entity
 * Manages tiered pricing for different customer segments and quantities
 */
@Entity
@Table(name = "product_pricing", indexes = {
    @Index(name = "idx_product", columnList = "product"),
    @Index(name = "idx_pricing_tier", columnList = "pricingTier"),
    @Index(name = "idx_effective_dates", columnList = "effectiveFrom, effectiveTo"),
    @Index(name = "idx_active", columnList = "active"),
    @Index(name = "idx_customer_segment", columnList = "customerSegment")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPricing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull(message = "Product is required")
    private Product product;

    @Column(name = "pricing_tier", nullable = false, length = 50)
    @NotBlank(message = "Pricing tier is required")
    @Builder.Default
    private String pricingTier = "STANDARD";

    // Pricing Details
    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.0", message = "Unit price must be non-negative")
    private BigDecimal unitPrice;

    @Column(name = "minimum_quantity")
    @Min(value = 1, message = "Minimum quantity must be at least 1")
    @Builder.Default
    private Integer minimumQuantity = 1;

    @Column(name = "maximum_quantity")
    @Min(value = 1, message = "Maximum quantity must be at least 1")
    private Integer maximumQuantity;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "Discount percentage must be non-negative")
    @DecimalMax(value = "100.0", message = "Discount percentage cannot exceed 100%")
    @Builder.Default
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    // Validity
    @Column(name = "effective_from", nullable = false)
    @NotNull(message = "Effective from date is required")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean active = true;

    // Special Conditions
    @Column(name = "customer_segment", length = 50)
    private String customerSegment;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "location_specific", columnDefinition = "JSON")
    private JsonNode locationSpecific;

    @Column(name = "special_conditions", columnDefinition = "TEXT")
    private String specialConditions;

    // Tracking
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    /**
     * Common pricing tiers
     */
    public static class PricingTier {
        public static final String STANDARD = "STANDARD";
        public static final String BULK = "BULK";
        public static final String ENTERPRISE = "ENTERPRISE";
        public static final String VIP = "VIP";
        public static final String PROMOTIONAL = "PROMOTIONAL";
    }

    /**
     * Common customer segments
     */
    public static class CustomerSegment {
        public static final String STANDARD = "STANDARD";
        public static final String ENTERPRISE = "ENTERPRISE";
        public static final String SME = "SME";
        public static final String GOVERNMENT = "GOVERNMENT";
        public static final String PARTNER = "PARTNER";
    }

    /**
     * Check if pricing is currently valid
     */
    public boolean isCurrentlyValid() {
        if (!active) return false;
        
        LocalDate today = LocalDate.now();
        
        if (effectiveFrom != null && today.isBefore(effectiveFrom)) {
            return false;
        }
        
        if (effectiveTo != null && today.isAfter(effectiveTo)) {
            return false;
        }
        
        return true;
    }

    /**
     * Check if quantity falls within the pricing tier range
     */
    public boolean appliesToQuantity(int quantity) {
        if (minimumQuantity != null && quantity < minimumQuantity) {
            return false;
        }
        
        if (maximumQuantity != null && quantity > maximumQuantity) {
            return false;
        }
        
        return true;
    }

    /**
     * Check if pricing applies to customer segment
     */
    public boolean appliesToCustomerSegment(String segment) {
        return customerSegment == null || customerSegment.equals(segment);
    }

    /**
     * Calculate effective price after discount
     */
    public BigDecimal getEffectivePrice() {
        if (discountPercentage == null || discountPercentage.compareTo(BigDecimal.ZERO) == 0) {
            return unitPrice;
        }
        
        BigDecimal discountAmount = unitPrice.multiply(discountPercentage)
                                            .divide(BigDecimal.valueOf(100));
        return unitPrice.subtract(discountAmount);
    }

    /**
     * Calculate total cost for given quantity
     */
    public BigDecimal calculateTotalCost(int quantity) {
        return getEffectivePrice().multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Calculate discount amount for given quantity
     */
    public BigDecimal calculateDiscountAmount(int quantity) {
        if (discountPercentage == null || discountPercentage.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal totalBeforeDiscount = unitPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal discountAmount = totalBeforeDiscount.multiply(discountPercentage)
                                                      .divide(BigDecimal.valueOf(100));
        return discountAmount;
    }

    /**
     * Check if this is a bulk pricing tier
     */
    public boolean isBulkPricing() {
        return PricingTier.BULK.equals(pricingTier) || 
               (minimumQuantity != null && minimumQuantity > 1);
    }

    /**
     * Check if this is promotional pricing
     */
    public boolean isPromotionalPricing() {
        return PricingTier.PROMOTIONAL.equals(pricingTier) ||
               (discountPercentage != null && discountPercentage.compareTo(BigDecimal.ZERO) > 0);
    }

    /**
     * Check if pricing has quantity restrictions
     */
    public boolean hasQuantityRestrictions() {
        return minimumQuantity != null || maximumQuantity != null;
    }

    /**
     * Get quantity range description
     */
    public String getQuantityRangeDescription() {
        if (!hasQuantityRestrictions()) {
            return "No quantity restrictions";
        }
        
        StringBuilder sb = new StringBuilder();
        if (minimumQuantity != null) {
            sb.append("Min: ").append(minimumQuantity);
        }
        if (maximumQuantity != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("Max: ").append(maximumQuantity);
        }
        return sb.toString();
    }

    /**
     * Alias for unitPrice (for compatibility)
     */
    public BigDecimal getBasePrice() {
        return this.unitPrice;
    }

    /**
     * Alias for effectiveFrom (for compatibility)
     */
    public LocalDate getEffectiveFromDate() {
        return this.effectiveFrom;
    }

    /**
     * Alias for effectiveTo (for compatibility)
     */
    public LocalDate getEffectiveToDate() {
        return this.effectiveTo;
    }
}
