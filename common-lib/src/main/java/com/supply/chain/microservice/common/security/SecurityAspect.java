package com.supply.chain.microservice.common.security;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * Security aspect implementing the Aspect-Oriented Programming pattern
 * Intercepts method calls to enforce role-based security
 */
@Aspect
@Component
@Slf4j
public class SecurityAspect {
    
    @Around("@annotation(requireRole)")
    public Object enforceRoleSecurity(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        SecurityContext context = SecurityContextHolder.getContext();
        
        if (!context.isAuthenticated()) {
            log.warn("Unauthenticated user attempted to access secured method: {}", 
                    joinPoint.getSignature().getName());
            throw new AccessDeniedException("Authentication required");
        }
        
        String[] requiredRoles = requireRole.value();
        boolean requireAll = requireRole.requireAll();
        
        if (requireAll) {
            // All roles required (AND logic)
            for (String role : requiredRoles) {
                if (!context.hasRole(role)) {
                    log.warn("User {} missing required role {} for method {}", 
                            context.getUsername(), role, joinPoint.getSignature().getName());
                    throw new AccessDeniedException("Insufficient privileges");
                }
            }
        } else {
            // Any role sufficient (OR logic)
            boolean hasRequiredRole = false;
            for (String role : requiredRoles) {
                if (context.hasRole(role)) {
                    hasRequiredRole = true;
                    break;
                }
            }
            
            if (!hasRequiredRole) {
                log.warn("User {} missing any required role for method {}", 
                        context.getUsername(), joinPoint.getSignature().getName());
                throw new AccessDeniedException("Insufficient privileges");
            }
        }
        
        log.debug("User {} authorized to access method {}", 
                context.getUsername(), joinPoint.getSignature().getName());
        
        return joinPoint.proceed();
    }
}
