package com.supply.chain.microservice.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Component
public class JwtTokenUtil {
    
    @Value("${jwt.secret:mySecretKey}")
    private String secret;
    
    @Value("${jwt.expiration:86400}") // 24 hours
    private Long expiration;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
    
    public String generateToken(JwtClaims jwtClaims) {
        Map<String, Object> claims = Map.of(
                "username", jwtClaims.getUsername(),
                "email", jwtClaims.getEmail(),
                "roles", jwtClaims.getRoles(),
                "authorities", jwtClaims.getAuthorities(),
                "serviceId", jwtClaims.getServiceId() != null ? jwtClaims.getServiceId() : ""
        );
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(jwtClaims.getSubject())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }
    
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("username", String.class));
    }
    
    public String getEmailFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("email", String.class));
    }
    
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("roles", List.class));
    }
    
    @SuppressWarnings("unchecked")
    public List<String> getAuthoritiesFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("authorities", List.class));
    }
    
    public String getServiceIdFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("serviceId", String.class));
    }
    
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }
    
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }
    
    public Boolean validateToken(String token, String username) {
        final String tokenUsername = getUsernameFromToken(token);
        return (tokenUsername.equals(username) && !isTokenExpired(token));
    }
    
    public Boolean validateServiceToken(String token, String serviceId) {
        try {
            final String tokenServiceId = getServiceIdFromToken(token);
            return (tokenServiceId != null && tokenServiceId.equals(serviceId) && !isTokenExpired(token));
        } catch (Exception e) {
            log.error("Error validating service token: {}", e.getMessage());
            return false;
        }
    }
}
