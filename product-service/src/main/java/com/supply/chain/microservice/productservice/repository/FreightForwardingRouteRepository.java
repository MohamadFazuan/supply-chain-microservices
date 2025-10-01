package com.supply.chain.microservice.productservice.repository;

import com.supply.chain.microservice.productservice.entity.FreightForwardingRoute;
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
 * Repository for FreightForwardingRoute entities
 */
@Repository
public interface FreightForwardingRouteRepository extends JpaRepository<FreightForwardingRoute, Long> {

    /**
     * Find route by route code
     */
    Optional<FreightForwardingRoute> findByRouteCodeIgnoreCase(String routeCode);

    /**
     * Find routes by origin and destination
     */
    @Query("SELECT ffr FROM FreightForwardingRoute ffr WHERE " +
           "ffr.originCountry = :originCountry AND ffr.originCity = :originCity AND " +
           "ffr.destinationCountry = :destinationCountry AND ffr.destinationCity = :destinationCity " +
           "AND ffr.routeStatus = 'ACTIVE'")
    List<FreightForwardingRoute> findByOriginAndDestination(@Param("originCountry") String originCountry,
                                                           @Param("originCity") String originCity,
                                                           @Param("destinationCountry") String destinationCountry,
                                                           @Param("destinationCity") String destinationCity);

    /**
     * Find routes by transport mode
     */
    @Query("SELECT ffr FROM FreightForwardingRoute ffr WHERE ffr.primaryTransportMode = :transportMode AND ffr.routeStatus = 'ACTIVE'")
    Page<FreightForwardingRoute> findByTransportMode(@Param("transportMode") FreightForwardingRoute.TransportMode transportMode, 
                                                     Pageable pageable);

    /**
     * Find routes by service type
     */
    @Query("SELECT ffr FROM FreightForwardingRoute ffr WHERE ffr.serviceType = :serviceType AND ffr.routeStatus = 'ACTIVE'")
    List<FreightForwardingRoute> findByServiceType(@Param("serviceType") FreightForwardingRoute.ServiceType serviceType);

    /**
     * Find available routes
     */
    @Query("SELECT ffr FROM FreightForwardingRoute ffr WHERE ffr.routeStatus = 'ACTIVE' " +
           "AND (ffr.seasonalStartDate IS NULL OR ffr.seasonalStartDate <= CURRENT_DATE) " +
           "AND (ffr.seasonalEndDate IS NULL OR ffr.seasonalEndDate >= CURRENT_DATE)")
    List<FreightForwardingRoute> findAvailableRoutes();

    /**
     * Find routes by origin country
     */
    @Query("SELECT ffr FROM FreightForwardingRoute ffr WHERE ffr.originCountry = :country AND ffr.routeStatus = 'ACTIVE'")
    List<FreightForwardingRoute> findByOriginCountry(@Param("country") String country);

    /**
     * Find routes by destination country
     */
    @Query("SELECT ffr FROM FreightForwardingRoute ffr WHERE ffr.destinationCountry = :country AND ffr.routeStatus = 'ACTIVE'")
    List<FreightForwardingRoute> findByDestinationCountry(@Param("country") String country);

    /**
     * Find routes by price range
     */
    @Query("SELECT ffr FROM FreightForwardingRoute ffr WHERE ffr.baseRate BETWEEN :minPrice AND :maxPrice AND ffr.routeStatus = 'ACTIVE'")
    List<FreightForwardingRoute> findByPriceRange(@Param("minPrice") BigDecimal minPrice, 
                                                 @Param("maxPrice") BigDecimal maxPrice);

    /**
     * Find routes with dangerous goods support
     */
    @Query("SELECT ffr FROM FreightForwardingRoute ffr WHERE ffr.dangerousGoodsAllowed = true AND ffr.routeStatus = 'ACTIVE'")
    List<FreightForwardingRoute> findDangerousGoodsRoutes();

    /**
     * Find express routes
     */
    @Query("SELECT ffr FROM FreightForwardingRoute ffr WHERE ffr.serviceType = 'EXPRESS' AND ffr.routeStatus = 'ACTIVE'")
    List<FreightForwardingRoute> findExpressRoutes();

    /**
     * Find routes by transit time
     */
    @Query("SELECT ffr FROM FreightForwardingRoute ffr WHERE ffr.transitTimeDays <= :maxDays AND ffr.routeStatus = 'ACTIVE'")
    List<FreightForwardingRoute> findByMaxTransitTime(@Param("maxDays") Integer maxDays);

    /**
     * Check route availability for weight
     */
    @Query("SELECT ffr FROM FreightForwardingRoute ffr WHERE " +
           "(ffr.maxWeightPerShipment IS NULL OR ffr.maxWeightPerShipment >= :weight) " +
           "AND ffr.routeStatus = 'ACTIVE'")
    List<FreightForwardingRoute> findRoutesForWeight(@Param("weight") BigDecimal weight);
}
