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
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Freight Forwarding Route Entity
 * Manages shipping routes for freight forwarding products
 */
@Entity
@Table(name = "freight_forwarding_routes", indexes = {
    @Index(name = "idx_product", columnList = "product"),
    @Index(name = "idx_route_code", columnList = "routeCode"),
    @Index(name = "idx_origin", columnList = "originCountry, originCity"),
    @Index(name = "idx_destination", columnList = "destinationCountry, destinationCity"),
    @Index(name = "idx_transport_mode", columnList = "primaryTransportMode"),
    @Index(name = "idx_service_type", columnList = "serviceType"),
    @Index(name = "idx_route_status", columnList = "routeStatus")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FreightForwardingRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull(message = "Product is required")
    private Product product;

    @Column(name = "route_code", unique = true, nullable = false, length = 100)
    @NotBlank(message = "Route code is required")
    @Size(max = 100, message = "Route code must not exceed 100 characters")
    private String routeCode;

    // Route Details
    @Column(name = "origin_country", nullable = false, length = 50)
    @NotBlank(message = "Origin country is required")
    private String originCountry;

    @Column(name = "origin_city", nullable = false, length = 100)
    @NotBlank(message = "Origin city is required")
    private String originCity;

    @Column(name = "origin_port_code", length = 20)
    private String originPortCode;

    @Column(name = "destination_country", nullable = false, length = 50)
    @NotBlank(message = "Destination country is required")
    private String destinationCountry;

    @Column(name = "destination_city", nullable = false, length = 100)
    @NotBlank(message = "Destination city is required")
    private String destinationCity;

    @Column(name = "destination_port_code", length = 20)
    private String destinationPortCode;

    // Transportation Mode
    @Enumerated(EnumType.STRING)
    @Column(name = "primary_transport_mode", nullable = false)
    @NotNull(message = "Primary transport mode is required")
    private TransportMode primaryTransportMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false)
    @NotNull(message = "Service type is required")
    private ServiceType serviceType;

    // Service Details
    @Column(name = "transit_time_days")
    private Integer transitTimeDays;

    @Column(name = "frequency", length = 50)
    private String frequency;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "available_days", columnDefinition = "JSON")
    private JsonNode availableDays;

    @Column(name = "cutoff_hours", length = 10)
    private String cutoffHours;

    // Capacity
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "container_types", columnDefinition = "JSON")
    private JsonNode containerTypes;

    @Column(name = "max_weight_per_shipment", precision = 15, scale = 3)
    private BigDecimal maxWeightPerShipment;

    @Column(name = "dangerous_goods_allowed")
    @Builder.Default
    private Boolean dangerousGoodsAllowed = false;

    // Pricing
    @Column(name = "base_rate", precision = 15, scale = 2)
    private BigDecimal baseRate;

    @Column(name = "fuel_surcharge_rate", precision = 5, scale = 2)
    private BigDecimal fuelSurchargeRate;

    @Column(name = "documentation_fee", precision = 10, scale = 2)
    private BigDecimal documentationFee;

    @Column(name = "insurance_rate", precision = 5, scale = 4)
    private BigDecimal insuranceRate;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(name = "route_status")
    @Builder.Default
    private RouteStatus routeStatus = RouteStatus.ACTIVE;

    @Column(name = "seasonal_start_date")
    private LocalDate seasonalStartDate;

    @Column(name = "seasonal_end_date")
    private LocalDate seasonalEndDate;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Transportation Modes
     */
    public enum TransportMode {
        SEA("Sea Freight"),
        AIR("Air Freight"),
        LAND("Land Transport"),
        MULTIMODAL("Multimodal Transport");

        private final String displayName;

        TransportMode(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Service Types for freight forwarding
     */
    public enum ServiceType {
        FCL("Full Container Load"),
        LCL("Less than Container Load"),
        EXPRESS("Express Service"),
        STANDARD("Standard Service"),
        ECONOMY("Economy Service");

        private final String displayName;

        ServiceType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Route Status
     */
    public enum RouteStatus {
        ACTIVE("Active"),
        SUSPENDED("Suspended"),
        SEASONAL("Seasonal"),
        DISCONTINUED("Discontinued");

        private final String displayName;

        RouteStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Check if route is currently available
     */
    public boolean isCurrentlyAvailable() {
        if (routeStatus != RouteStatus.ACTIVE && routeStatus != RouteStatus.SEASONAL) {
            return false;
        }

        if (routeStatus == RouteStatus.SEASONAL) {
            return isInSeasonalPeriod();
        }

        return true;
    }

    /**
     * Check if route is in seasonal period
     */
    public boolean isInSeasonalPeriod() {
        if (seasonalStartDate == null && seasonalEndDate == null) {
            return true; // No seasonal restrictions
        }

        LocalDate today = LocalDate.now();

        if (seasonalStartDate != null && today.isBefore(seasonalStartDate)) {
            return false;
        }

        if (seasonalEndDate != null && today.isAfter(seasonalEndDate)) {
            return false;
        }

        return true;
    }

    /**
     * Check if route supports dangerous goods
     */
    public boolean supportsDangerousGoods() {
        return dangerousGoodsAllowed != null && dangerousGoodsAllowed;
    }

    /**
     * Check if route can handle the given weight
     */
    public boolean canHandleWeight(BigDecimal weight) {
        if (maxWeightPerShipment == null || weight == null) return true;
        return maxWeightPerShipment.compareTo(weight) >= 0;
    }

    /**
     * Get full route description
     */
    public String getRouteDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append(originCity).append(", ").append(originCountry);
        desc.append(" → ");
        desc.append(destinationCity).append(", ").append(destinationCountry);
        
        if (primaryTransportMode != null) {
            desc.append(" (").append(primaryTransportMode.getDisplayName()).append(")");
        }
        
        return desc.toString();
    }

    /**
     * Get origin location with port code
     */
    public String getOriginLocation() {
        StringBuilder location = new StringBuilder();
        location.append(originCity).append(", ").append(originCountry);
        if (originPortCode != null && !originPortCode.trim().isEmpty()) {
            location.append(" (").append(originPortCode).append(")");
        }
        return location.toString();
    }

    /**
     * Get destination location with port code
     */
    public String getDestinationLocation() {
        StringBuilder location = new StringBuilder();
        location.append(destinationCity).append(", ").append(destinationCountry);
        if (destinationPortCode != null && !destinationPortCode.trim().isEmpty()) {
            location.append(" (").append(destinationPortCode).append(")");
        }
        return location.toString();
    }

    /**
     * Calculate total estimated cost for given weight and value
     */
    public BigDecimal calculateEstimatedCost(BigDecimal weight, BigDecimal cargoValue) {
        BigDecimal totalCost = BigDecimal.ZERO;

        // Base rate
        if (baseRate != null) {
            totalCost = totalCost.add(baseRate);
        }

        // Fuel surcharge (as percentage of base rate)
        if (fuelSurchargeRate != null && baseRate != null) {
            BigDecimal fuelSurcharge = baseRate.multiply(fuelSurchargeRate)
                                              .divide(BigDecimal.valueOf(100));
            totalCost = totalCost.add(fuelSurcharge);
        }

        // Documentation fee
        if (documentationFee != null) {
            totalCost = totalCost.add(documentationFee);
        }

        // Insurance (as percentage of cargo value)
        if (insuranceRate != null && cargoValue != null) {
            BigDecimal insurance = cargoValue.multiply(insuranceRate)
                                           .divide(BigDecimal.valueOf(100));
            totalCost = totalCost.add(insurance);
        }

        return totalCost;
    }

    /**
     * Check if route is express service
     */
    public boolean isExpressService() {
        return serviceType == ServiceType.EXPRESS;
    }

    /**
     * Check if route is container service
     */
    public boolean isContainerService() {
        return serviceType == ServiceType.FCL || serviceType == ServiceType.LCL;
    }

    /**
     * Check if route is sea freight
     */
    public boolean isSeaFreight() {
        return primaryTransportMode == TransportMode.SEA;
    }

    /**
     * Check if route is air freight
     */
    public boolean isAirFreight() {
        return primaryTransportMode == TransportMode.AIR;
    }

    /**
     * Check if route is land transport
     */
    public boolean isLandTransport() {
        return primaryTransportMode == TransportMode.LAND;
    }

    /**
     * Check if route is multimodal
     */
    public boolean isMultimodal() {
        return primaryTransportMode == TransportMode.MULTIMODAL;
    }

    /**
     * Get estimated delivery date from pickup date
     */
    public LocalDate getEstimatedDeliveryDate(LocalDate pickupDate) {
        if (transitTimeDays == null || pickupDate == null) return null;
        return pickupDate.plusDays(transitTimeDays);
    }
}
