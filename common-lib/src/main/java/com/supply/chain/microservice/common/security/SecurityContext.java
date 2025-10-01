package com.supply.chain.microservice.common.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecurityContext {
    private String username;
    private String userId;
    private String email;
    private String[] roles;
    private String[] authorities;
    private String tenantId;
    private String sessionId;
    private boolean authenticated;
    
    public boolean hasRole(String role) {
        if (roles == null) return false;
        for (String userRole : roles) {
            if (userRole.equals(role)) return true;
        }
        return false;
    }
    
    public boolean hasAuthority(String authority) {
        if (authorities == null) return false;
        for (String userAuthority : authorities) {
            if (userAuthority.equals(authority)) return true;
        }
        return false;
    }
    
    public boolean hasAnyRole(String... roleNames) {
        for (String role : roleNames) {
            if (hasRole(role)) return true;
        }
        return false;
    }
}
