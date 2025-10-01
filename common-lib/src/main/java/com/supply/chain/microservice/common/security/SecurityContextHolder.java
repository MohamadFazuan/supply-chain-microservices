package com.supply.chain.microservice.common.security;

import org.springframework.stereotype.Component;

/**
 * Thread-local security context holder following the Singleton pattern
 * Provides access to current user's security information across the request lifecycle
 */
@Component
public class SecurityContextHolder {
    
    private static final ThreadLocal<SecurityContext> contextHolder = new ThreadLocal<>();
    
    public static void setContext(SecurityContext context) {
        contextHolder.set(context);
    }
    
    public static SecurityContext getContext() {
        SecurityContext context = contextHolder.get();
        return context != null ? context : new SecurityContext();
    }
    
    public static void clearContext() {
        contextHolder.remove();
    }
    
    public static String getCurrentUsername() {
        SecurityContext context = getContext();
        return context.getUsername();
    }
    
    public static String getCurrentUserId() {
        SecurityContext context = getContext();
        return context.getUserId();
    }
    
    public static boolean isAuthenticated() {
        SecurityContext context = getContext();
        return context.isAuthenticated();
    }
    
    public static boolean hasRole(String role) {
        SecurityContext context = getContext();
        return context.hasRole(role);
    }
    
    public static boolean hasAuthority(String authority) {
        SecurityContext context = getContext();
        return context.hasAuthority(authority);
    }
}
