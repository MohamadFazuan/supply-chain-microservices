package com.supply.chain.microservice.common.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Security interceptor using the Interceptor pattern
 * Extracts and sets security context from request headers
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityContextInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            // Extract user information from headers (set by API Gateway)
            String username = request.getHeader("X-User-Username");
            String userId = request.getHeader("X-User-Id");
            String email = request.getHeader("X-User-Email");
            String rolesHeader = request.getHeader("X-User-Roles");
            String authoritiesHeader = request.getHeader("X-User-Authorities");
            String tenantId = request.getHeader("X-Tenant-Id");
            String sessionId = request.getHeader("X-Session-Id");
            
            if (username != null) {
                SecurityContext context = new SecurityContext();
                context.setUsername(username);
                context.setUserId(userId);
                context.setEmail(email);
                context.setTenantId(tenantId);
                context.setSessionId(sessionId);
                context.setAuthenticated(true);
                
                if (rolesHeader != null && !rolesHeader.isEmpty()) {
                    context.setRoles(rolesHeader.split(","));
                }
                
                if (authoritiesHeader != null && !authoritiesHeader.isEmpty()) {
                    context.setAuthorities(authoritiesHeader.split(","));
                }
                
                SecurityContextHolder.setContext(context);
                log.debug("Security context set for user: {}", username);
            } else {
                log.debug("No user information found in headers");
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("Error setting security context", e);
            return true; // Don't block request, let security annotations handle authorization
        }
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) {
        // Clear security context after request processing
        SecurityContextHolder.clearContext();
    }
}
