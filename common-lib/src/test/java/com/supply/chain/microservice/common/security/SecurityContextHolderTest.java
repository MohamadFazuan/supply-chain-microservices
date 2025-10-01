package com.supply.chain.microservice.common.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Security Context Holder Tests")
class SecurityContextHolderTest {

    private SecurityContext testContext;

    @BeforeEach
    void setUp() {
        testContext = new SecurityContext();
        testContext.setUsername("testuser");
        testContext.setUserId("123");
        testContext.setEmail("test@example.com");
        testContext.setRoles(new String[]{"ROLE_USER", "ROLE_ADMIN"});
        testContext.setAuthorities(new String[]{"READ", "WRITE"});
        testContext.setAuthenticated(true);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should set and get security context")
    void setAndGetContext_withValidContext_shouldStoreAndRetrieve() {
        // When
        SecurityContextHolder.setContext(testContext);
        SecurityContext retrieved = SecurityContextHolder.getContext();

        // Then
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getUsername()).isEqualTo("testuser");
        assertThat(retrieved.getUserId()).isEqualTo("123");
        assertThat(retrieved.isAuthenticated()).isTrue();
    }

    @Test
    @DisplayName("Should return current username")
    void getCurrentUsername_withSetContext_shouldReturnUsername() {
        // Given
        SecurityContextHolder.setContext(testContext);

        // When
        String username = SecurityContextHolder.getCurrentUsername();

        // Then
        assertThat(username).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should return current user ID")
    void getCurrentUserId_withSetContext_shouldReturnUserId() {
        // Given
        SecurityContextHolder.setContext(testContext);

        // When
        String userId = SecurityContextHolder.getCurrentUserId();

        // Then
        assertThat(userId).isEqualTo("123");
    }

    @Test
    @DisplayName("Should check if authenticated")
    void isAuthenticated_withAuthenticatedContext_shouldReturnTrue() {
        // Given
        SecurityContextHolder.setContext(testContext);

        // When
        boolean isAuthenticated = SecurityContextHolder.isAuthenticated();

        // Then
        assertThat(isAuthenticated).isTrue();
    }

    @Test
    @DisplayName("Should clear security context")
    void clearContext_shouldRemoveCurrentContext() {
        // Given
        SecurityContextHolder.setContext(testContext);

        // When
        SecurityContextHolder.clearContext();
        SecurityContext context = SecurityContextHolder.getContext();

        // Then
        assertThat(context).isNotNull();
        assertThat(context.getUsername()).isNull();
        assertThat(context.isAuthenticated()).isFalse();
    }

    @Test
    @DisplayName("Should return empty context when none set")
    void getContext_withNoContextSet_shouldReturnEmptyContext() {
        // When
        SecurityContext context = SecurityContextHolder.getContext();

        // Then
        assertThat(context).isNotNull();
        assertThat(context.getUsername()).isNull();
        assertThat(context.isAuthenticated()).isFalse();
    }
}
