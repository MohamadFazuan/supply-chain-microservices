package com.supply.chain.microservice.productservice.repository;

import com.supply.chain.microservice.productservice.entity.TransportationFleet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository for TransportationFleet entities
 */
@Repository
public interface TransportationFleetRepository extends JpaRepository<TransportationFleet, Long> {

    /**
     * Find fleet by vehicle registration
     */
    Optional<TransportationFleet> findByVehicleRegistrationIgnoreCase(String vehicleRegistration);

    /**
     * Find fleet by product ID
     */
    List<TransportationFleet> findByProductId(Long productId);

    /**
     * Find fleet by vehicle type
     */
    @Query("SELECT tf FROM TransportationFleet tf WHERE tf.vehicleType = :vehicleType AND tf.fleetStatus = 'ACTIVE'")
    Page<TransportationFleet> findByVehicleType(@Param("vehicleType") TransportationFleet.VehicleType vehicleType, Pageable pageable);

    /**
     * Find available fleet
     */
    @Query("SELECT tf FROM TransportationFleet tf WHERE tf.fleetStatus = 'ACTIVE' AND tf.currentStatus = 'AVAILABLE'")
    List<TransportationFleet> findAvailableFleet();

    /**
     * Find fleet by capacity range
     */
    @Query("SELECT tf FROM TransportationFleet tf WHERE tf.maxWeightCapacity BETWEEN :minCapacity AND :maxCapacity AND tf.fleetStatus = 'ACTIVE'")
    List<TransportationFleet> findByCapacityRange(@Param("minCapacity") BigDecimal minCapacity, 
                                                 @Param("maxCapacity") BigDecimal maxCapacity);

    /**
     * Find fleet by location
     */
    @Query("SELECT tf FROM TransportationFleet tf WHERE tf.currentLocation LIKE %:location% AND tf.fleetStatus = 'ACTIVE'")
    List<TransportationFleet> findByLocation(@Param("location") String location);

    /**
     * Find fleet requiring maintenance
     */
    @Query("SELECT tf FROM TransportationFleet tf WHERE tf.currentStatus = 'MAINTENANCE_REQUIRED' OR tf.nextMaintenanceDate < CURRENT_DATE")
    List<TransportationFleet> findFleetRequiringMaintenance();

    /**
     * Count fleet by status
     */
    @Query("SELECT COUNT(tf) FROM TransportationFleet tf WHERE tf.currentStatus = :status")
    long countByCurrentStatus(@Param("status") TransportationFleet.FleetCurrentStatus status);

    /**
     * Find fleet by service routes
     */
    @Query("SELECT tf FROM TransportationFleet tf WHERE JSON_CONTAINS(tf.serviceRoutes, :route) AND tf.fleetStatus = 'ACTIVE'")
    List<TransportationFleet> findByServiceRoute(@Param("route") String route);
}
