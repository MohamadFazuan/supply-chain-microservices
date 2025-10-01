package com.supply.chain.microservice.productservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security configuration for Product Service
 * Configures JWT authentication, authorization, and CORS
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Security filter chain configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()
                .requestMatchers("/health", "/info", "/metrics").permitAll()
                
                // Product read operations - require PRODUCT_READ authority
                .requestMatchers(HttpMethod.GET, "/api/v1/products/**").hasAuthority("PRODUCT_READ")
                
                // Product write operations - require specific authorities
                .requestMatchers(HttpMethod.POST, "/api/v1/products").hasAuthority("PRODUCT_CREATE")
                .requestMatchers(HttpMethod.PUT, "/api/v1/products/**").hasAuthority("PRODUCT_UPDATE")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/products/**").hasAuthority("PRODUCT_DELETE")
                
                // Product management operations - require PRODUCT_MANAGE authority
                .requestMatchers(HttpMethod.POST, "/api/v1/products/*/activate").hasAuthority("PRODUCT_MANAGE")
                .requestMatchers(HttpMethod.POST, "/api/v1/products/*/deactivate").hasAuthority("PRODUCT_MANAGE")
                
                // Category operations
                .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").hasAuthority("CATEGORY_READ")
                .requestMatchers(HttpMethod.POST, "/api/v1/categories").hasAuthority("CATEGORY_CREATE")
                .requestMatchers(HttpMethod.PUT, "/api/v1/categories/**").hasAuthority("CATEGORY_UPDATE")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/categories/**").hasAuthority("CATEGORY_DELETE")
                
                // Inventory operations
                .requestMatchers(HttpMethod.GET, "/api/v1/inventory/**").hasAuthority("INVENTORY_READ")
                .requestMatchers(HttpMethod.POST, "/api/v1/inventory/**").hasAuthority("INVENTORY_CREATE")
                .requestMatchers(HttpMethod.PUT, "/api/v1/inventory/**").hasAuthority("INVENTORY_UPDATE")
                
                // Pricing operations
                .requestMatchers(HttpMethod.GET, "/api/v1/pricing/**").hasAuthority("PRICING_READ")
                .requestMatchers(HttpMethod.POST, "/api/v1/pricing/**").hasAuthority("PRICING_CREATE")
                .requestMatchers(HttpMethod.PUT, "/api/v1/pricing/**").hasAuthority("PRICING_UPDATE")
                
                // Fleet management operations
                .requestMatchers(HttpMethod.GET, "/api/v1/fleet/**").hasAuthority("FLEET_READ")
                .requestMatchers(HttpMethod.POST, "/api/v1/fleet/**").hasAuthority("FLEET_CREATE")
                .requestMatchers(HttpMethod.PUT, "/api/v1/fleet/**").hasAuthority("FLEET_UPDATE")
                
                // Route management operations
                .requestMatchers(HttpMethod.GET, "/api/v1/routes/**").hasAuthority("ROUTE_READ")
                .requestMatchers(HttpMethod.POST, "/api/v1/routes/**").hasAuthority("ROUTE_CREATE")
                .requestMatchers(HttpMethod.PUT, "/api/v1/routes/**").hasAuthority("ROUTE_UPDATE")
                
                // Admin operations - require ADMIN role
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                
                // All other requests must be authenticated
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("Security filter chain configured with JWT authentication");
        return http.build();
    }

    /**
     * CORS configuration
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow specific origins in production, use patterns for development
        configuration.setAllowedOriginPatterns(List.of(
            "http://localhost:*",
            "https://*.tiongnam.com",
            "https://*.logistics.local"
        ));
        
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers",
            "X-Correlation-ID",
            "X-Request-ID"
        ));
        
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials",
            "X-Correlation-ID",
            "X-Request-ID",
            "X-Total-Count"
        ));
        
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // 1 hour
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        log.info("CORS configuration registered for all endpoints");
        return source;
    }
}
