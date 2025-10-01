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
 * Transportation Fleet Entity
 * Manages fleet vehicles for transportation products
 */
@Entity
@Table(name = "transportation_fleet", indexes = {
    @Index(name = "idx_product", columnList = "product"),
    @Index(name = "idx_fleet_id", columnList = "fleetId"),
    @Index(name = "idx_vehicle_type", columnList = "vehicleType"),
    @Index(name = "idx_status", columnList = "operationalStatus"),
    @Index(name = "idx_driver", columnList = "assignedDriverId")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportationFleet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull(message = "Product is required")
    private Product product;

    @Column(name = "fleet_id", unique = true, nullable = false, length = 100)
    @NotBlank(message = "Fleet ID is required")
    @Size(max = 100, message = "Fleet ID must not exceed 100 characters")
    private String fleetId;

    @Column(name = "vehicle_type", nullable = false, length = 50)
    @NotBlank(message = "Vehicle type is required")
    private String vehicleType;

    // Vehicle Details
    @Column(name = "make", length = 50)
    private String make;

    @Column(name = "model", length = 50)
    private String model;

    @Column(name = "year_manufactured")
    private Integer yearManufactured;

    @Column(name = "license_plate", length = 20)
    private String licensePlate;

    // Capacity
    @Column(name = "cargo_capacity", precision = 15, scale = 3)
    private BigDecimal cargoCapacity;

    @Column(name = "passenger_capacity")
    private Integer passengerCapacity;

    @Column(name = "max_weight", precision = 15, scale = 3)
    private BigDecimal maxWeight;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(name = "operational_status")
    @Builder.Default
    private OperationalStatus operationalStatus = OperationalStatus.ACTIVE;

    @Column(name = "current_location", length = 100)
    private String currentLocation;

    @Column(name = "last_service_date")
    private LocalDate lastServiceDate;

    @Column(name = "next_service_due")
    private LocalDate nextServiceDue;

    // Driver Assignment
    @Column(name = "assigned_driver_id", length = 100)
    private String assignedDriverId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "driver_contact", columnDefinition = "JSON")
    private JsonNode driverContact;

    // Tracking
    @Column(name = "gps_enabled")
    @Builder.Default
    private Boolean gpsEnabled = true;

    @Column(name = "tracking_device_id", length = 100)
    private String trackingDeviceId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Operational Status for fleet vehicles
     */
    public enum OperationalStatus {
        ACTIVE("Active"),
        MAINTENANCE("In Maintenance"),
        RETIRED("Retired"),
        RESERVED("Reserved");

        private final String displayName;

        OperationalStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Common vehicle types
     */
    public static class VehicleType {
        public static final String BOX_TRUCK = "Box Truck";
        public static final String FLATBED_TRUCK = "Flatbed Truck";
        public static final String REFRIGERATED_TRUCK = "Refrigerated Truck";
        public static final String CONTAINER_TRUCK = "Container Truck";
        public static final String TANKER_TRUCK = "Tanker Truck";
        public static final String VAN = "Van";
        public static final String MOTORCYCLE = "Motorcycle";
        public static final String TRAILER = "Trailer";
    }

    /**
     * Check if vehicle is available for assignment
     */
    public boolean isAvailable() {
        return operationalStatus == OperationalStatus.ACTIVE && assignedDriverId != null;
    }

    /**
     * Check if vehicle needs maintenance
     */
    public boolean needsMaintenance() {
        if (nextServiceDue == null) return false;
        return LocalDate.now().isAfter(nextServiceDue) || LocalDate.now().isEqual(nextServiceDue);
    }

    /**
     * Check if vehicle is overdue for maintenance
     */
    public boolean isMaintenanceOverdue() {
        if (nextServiceDue == null) return false;
        return LocalDate.now().isAfter(nextServiceDue);
    }

    /**
     * Get vehicle age in years
     */
    public Integer getVehicleAge() {
        if (yearManufactured == null) return null;
        return LocalDate.now().getYear() - yearManufactured;
    }

    /**
     * Check if vehicle is GPS enabled and trackable
     */
    public boolean isTrackable() {
        return gpsEnabled && trackingDeviceId != null && !trackingDeviceId.trim().isEmpty();
    }

    /**
     * Get vehicle capacity utilization for given weight
     */
    public Double getCapacityUtilization(BigDecimal currentWeight) {
        if (maxWeight == null || maxWeight.compareTo(BigDecimal.ZERO) == 0) return null;
        if (currentWeight == null) return 0.0;
        
        return currentWeight.divide(maxWeight, 4, java.math.RoundingMode.HALF_UP)
                           .multiply(BigDecimal.valueOf(100))
                           .doubleValue();
    }

    /**
     * Check if vehicle can handle the given weight
     */
    public boolean canHandleWeight(BigDecimal weight) {
        if (maxWeight == null || weight == null) return false;
        return maxWeight.compareTo(weight) >= 0;
    }

    /**
     * Get remaining weight capacity
     */
    public BigDecimal getRemainingWeightCapacity(BigDecimal currentWeight) {
        if (maxWeight == null) return null;
        if (currentWeight == null) return maxWeight;
        return maxWeight.subtract(currentWeight);
    }

    /**
     * Check if vehicle is refrigerated
     */
    public boolean isRefrigerated() {
        return VehicleType.REFRIGERATED_TRUCK.equals(vehicleType);
    }

    /**
     * Check if vehicle is suitable for container transport
     */
    public boolean isContainerCapable() {
        return VehicleType.CONTAINER_TRUCK.equals(vehicleType) || 
               VehicleType.TRAILER.equals(vehicleType);
    }

    /**
     * Get vehicle identification string
     */
    public String getVehicleIdentification() {
        StringBuilder id = new StringBuilder();
        if (make != null) id.append(make);
        if (model != null) {
            if (id.length() > 0) id.append(" ");
            id.append(model);
        }
        if (licensePlate != null) {
            if (id.length() > 0) id.append(" - ");
            id.append(licensePlate);
        }
        return id.length() > 0 ? id.toString() : fleetId;
    }

    public enum FleetCurrentStatus {
        AVAILABLE,
        IN_TRANSIT,
        LOADING,
        UNLOADING,
        MAINTENANCE,
        OUT_OF_SERVICE,
        PARKED
    }
}
