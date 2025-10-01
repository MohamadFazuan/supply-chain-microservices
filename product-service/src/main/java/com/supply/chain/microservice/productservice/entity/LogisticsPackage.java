package com.supply.chain.microservice.productservice.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
import java.time.LocalDateTime;

/**
 * Logistics Package Entity
 * Represents bundled logistics service packages
 */
@Entity
@Table(name = "logistics_packages", indexes = {
    @Index(name = "idx_product", columnList = "product"),
    @Index(name = "idx_package_code", columnList = "packageCode"),
    @Index(name = "idx_package_type", columnList = "packageType"),
    @Index(name = "idx_service_level", columnList = "serviceLevel"),
    @Index(name = "idx_territory", columnList = "serviceTerritory"),
    @Index(name = "idx_duration", columnList = "contractDurationMonths"),
    @Index(name = "idx_package_status", columnList = "packageStatus")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogisticsPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull(message = "Product is required")
    private Product product;

    @Column(name = "package_code", unique = true, nullable = false, length = 100)
    @NotBlank(message = "Package code is required")
    @Size(max = 100, message = "Package code must not exceed 100 characters")
    private String packageCode;

    @Column(name = "package_name", nullable = false, length = 200)
    @NotBlank(message = "Package name is required")
    @Size(max = 200, message = "Package name must not exceed 200 characters")
    private String packageName;

    @Enumerated(EnumType.STRING)
    @Column(name = "package_type", nullable = false)
    @NotNull(message = "Package type is required")
    private PackageType packageType;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_level", nullable = false)
    @NotNull(message = "Service level is required")
    private ServiceLevel serviceLevel;

    // Geographic Coverage
    @Column(name = "service_territory", nullable = false, length = 100)
    @NotBlank(message = "Service territory is required")
    private String serviceTerritory;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "covered_countries", columnDefinition = "JSON")
    private JsonNode coveredCountries;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "service_zones", columnDefinition = "JSON")
    private JsonNode serviceZones;

    // Package Details
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "included_services", columnDefinition = "JSON")
    private JsonNode includedServices;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "service_features", columnDefinition = "JSON")
    private JsonNode serviceFeatures;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "optional_services", columnDefinition = "JSON")
    private JsonNode optionalServices;

    // Capacity and Limits
    @Column(name = "max_monthly_shipments")
    private Integer maxMonthlyShipments;

    @Column(name = "max_weight_per_shipment", precision = 15, scale = 3)
    private BigDecimal maxWeightPerShipment;

    @Column(name = "max_monthly_volume", precision = 15, scale = 3)
    private BigDecimal maxMonthlyVolume;

    @Column(name = "min_monthly_commitment", precision = 15, scale = 2)
    private BigDecimal minMonthlyCommitment;

    // Pricing
    @Column(name = "monthly_base_fee", precision = 15, scale = 2)
    private BigDecimal monthlyBaseFee;

    @Column(name = "setup_fee", precision = 10, scale = 2)
    private BigDecimal setupFee;

    @Column(name = "per_shipment_rate", precision = 10, scale = 2)
    private BigDecimal perShipmentRate;

    @Column(name = "per_kg_rate", precision = 8, scale = 4)
    private BigDecimal perKgRate;

    @Column(name = "volume_discount_tier", precision = 5, scale = 2)
    private BigDecimal volumeDiscountTier;

    // Contract Terms
    @Column(name = "contract_duration_months")
    private Integer contractDurationMonths;

    @Column(name = "auto_renewal_enabled")
    @Builder.Default
    private Boolean autoRenewalEnabled = false;

    @Column(name = "cancellation_notice_days")
    private Integer cancellationNoticeDays;

    @Column(name = "early_termination_fee", precision = 10, scale = 2)
    private BigDecimal earlyTerminationFee;

    // SLA and Performance
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sla_commitments", columnDefinition = "JSON")
    private JsonNode slaCommitments;

    @Column(name = "on_time_delivery_guarantee", precision = 5, scale = 2)
    private BigDecimal onTimeDeliveryGuarantee;

    @Column(name = "damage_claim_coverage", precision = 15, scale = 2)
    private BigDecimal damageClaimCoverage;

    // Requirements and Restrictions
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "customer_requirements", columnDefinition = "JSON")
    private JsonNode customerRequirements;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "restricted_items", columnDefinition = "JSON")
    private JsonNode restrictedItems;

    @Column(name = "dangerous_goods_allowed")
    @Builder.Default
    private Boolean dangerousGoodsAllowed = false;

    @Column(name = "temperature_controlled")
    @Builder.Default
    private Boolean temperatureControlled = false;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(name = "package_status")
    @Builder.Default
    private PackageStatus packageStatus = PackageStatus.ACTIVE;

    @Column(name = "effective_start_date")
    private LocalDateTime effectiveStartDate;

    @Column(name = "effective_end_date")
    private LocalDateTime effectiveEndDate;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Package Types
     */
    public enum PackageType {
        COMPREHENSIVE("Comprehensive Logistics Package"),
        TRANSPORT_ONLY("Transportation Only"),
        WAREHOUSING_ONLY("Warehousing Only"),
        FREIGHT_FORWARDING("Freight Forwarding Package"),
        LAST_MILE("Last Mile Delivery"),
        E_COMMERCE("E-commerce Fulfillment"),
        COLD_CHAIN("Cold Chain Logistics"),
        PROJECT_CARGO("Project Cargo Handling"),
        CUSTOMS_CLEARANCE("Customs & Compliance");

        private final String displayName;

        PackageType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Service Levels
     */
    public enum ServiceLevel {
        PREMIUM("Premium Service"),
        STANDARD("Standard Service"),
        ECONOMY("Economy Service"),
        EXPRESS("Express Service"),
        BASIC("Basic Service");

        private final String displayName;

        ServiceLevel(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Package Status
     */
    public enum PackageStatus {
        ACTIVE("Active"),
        INACTIVE("Inactive"),
        PENDING_APPROVAL("Pending Approval"),
        SUSPENDED("Suspended"),
        DISCONTINUED("Discontinued");

        private final String displayName;

        PackageStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Check if package is currently available
     */
    public boolean isCurrentlyAvailable() {
        if (packageStatus != PackageStatus.ACTIVE) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();

        if (effectiveStartDate != null && now.isBefore(effectiveStartDate)) {
            return false;
        }

        if (effectiveEndDate != null && now.isAfter(effectiveEndDate)) {
            return false;
        }

        return true;
    }

    /**
     * Check if package supports given shipment count
     */
    public boolean canHandleShipmentCount(Integer shipmentCount) {
        if (maxMonthlyShipments == null || shipmentCount == null) return true;
        return maxMonthlyShipments >= shipmentCount;
    }

    /**
     * Check if package can handle given weight
     */
    public boolean canHandleWeight(BigDecimal weight) {
        if (maxWeightPerShipment == null || weight == null) return true;
        return maxWeightPerShipment.compareTo(weight) >= 0;
    }

    /**
     * Check if package can handle given monthly volume
     */
    public boolean canHandleMonthlyVolume(BigDecimal volume) {
        if (maxMonthlyVolume == null || volume == null) return true;
        return maxMonthlyVolume.compareTo(volume) >= 0;
    }

    /**
     * Check if package supports dangerous goods
     */
    public boolean supportsDangerousGoods() {
        return dangerousGoodsAllowed != null && dangerousGoodsAllowed;
    }

    /**
     * Check if package supports temperature control
     */
    public boolean supportsTemperatureControl() {
        return temperatureControlled != null && temperatureControlled;
    }

    /**
     * Check if package has auto-renewal
     */
    public boolean hasAutoRenewal() {
        return autoRenewalEnabled != null && autoRenewalEnabled;
    }

    /**
     * Calculate monthly cost estimate
     */
    public BigDecimal calculateMonthlyCostEstimate(Integer shipmentsPerMonth, BigDecimal totalWeight) {
        BigDecimal totalCost = BigDecimal.ZERO;

        // Monthly base fee
        if (monthlyBaseFee != null) {
            totalCost = totalCost.add(monthlyBaseFee);
        }

        // Per shipment charges
        if (perShipmentRate != null && shipmentsPerMonth != null) {
            BigDecimal shipmentCost = perShipmentRate.multiply(BigDecimal.valueOf(shipmentsPerMonth));
            totalCost = totalCost.add(shipmentCost);
        }

        // Per kg charges
        if (perKgRate != null && totalWeight != null) {
            BigDecimal weightCost = perKgRate.multiply(totalWeight);
            totalCost = totalCost.add(weightCost);
        }

        // Apply volume discount if applicable
        if (volumeDiscountTier != null && totalCost.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = totalCost.multiply(volumeDiscountTier)
                                          .divide(BigDecimal.valueOf(100));
            totalCost = totalCost.subtract(discount);
        }

        return totalCost;
    }

    /**
     * Calculate total contract value
     */
    public BigDecimal calculateTotalContractValue(BigDecimal estimatedMonthlyCost) {
        if (contractDurationMonths == null || estimatedMonthlyCost == null) {
            return null;
        }

        BigDecimal totalValue = estimatedMonthlyCost.multiply(BigDecimal.valueOf(contractDurationMonths));

        // Add setup fee if applicable
        if (setupFee != null) {
            totalValue = totalValue.add(setupFee);
        }

        return totalValue;
    }

    /**
     * Check if minimum monthly commitment is met
     */
    public boolean meetsMinimumCommitment(BigDecimal monthlySpend) {
        if (minMonthlyCommitment == null || monthlySpend == null) return true;
        return monthlySpend.compareTo(minMonthlyCommitment) >= 0;
    }

    /**
     * Get package description
     */
    public String getPackageDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append(packageName);
        
        if (packageType != null) {
            desc.append(" (").append(packageType.getDisplayName()).append(")");
        }
        
        if (serviceLevel != null) {
            desc.append(" - ").append(serviceLevel.getDisplayName());
        }
        
        return desc.toString();
    }

    /**
     * Check if package is premium level
     */
    public boolean isPremiumLevel() {
        return serviceLevel == ServiceLevel.PREMIUM;
    }

    /**
     * Check if package is express level
     */
    public boolean isExpressLevel() {
        return serviceLevel == ServiceLevel.EXPRESS;
    }

    /**
     * Check if package is comprehensive
     */
    public boolean isComprehensivePackage() {
        return packageType == PackageType.COMPREHENSIVE;
    }

    /**
     * Check if package is transport only
     */
    public boolean isTransportOnlyPackage() {
        return packageType == PackageType.TRANSPORT_ONLY;
    }

    /**
     * Check if package is warehousing only
     */
    public boolean isWarehousingOnlyPackage() {
        return packageType == PackageType.WAREHOUSING_ONLY;
    }

    /**
     * Check if package includes e-commerce features
     */
    public boolean isECommercePackage() {
        return packageType == PackageType.E_COMMERCE;
    }

    /**
     * Check if package is cold chain
     */
    public boolean isColdChainPackage() {
        return packageType == PackageType.COLD_CHAIN;
    }

    /**
     * Get contract end date estimate
     */
    public LocalDateTime getEstimatedContractEndDate() {
        if (effectiveStartDate == null || contractDurationMonths == null) {
            return null;
        }
        return effectiveStartDate.plusMonths(contractDurationMonths);
    }
}
