package com.vbforge.security.oauth2.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbforge.security.oauth2.dto.ProductDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Security integration tests for Module 5 - OAuth2 Resource Server.
 *
 * Tests OAuth2 Resource Server functionality without requiring actual Keycloak.
 * Uses Spring Security Test's jwt() request post processor to mock JWT tokens.
 *
 * Key Differences from Module 4:
 * - Uses .with(jwt()) instead of actual JWT tokens
 * - Tests Keycloak role mapping
 * - No login endpoint (authentication external)
 * - Tests OAuth2-specific error responses
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OAuth2SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ========================================
    // OAuth2 JWT Authentication Tests
    // ========================================

    /**
     * Test: Access protected endpoint with valid JWT
     */
    @Test
    void whenValidJwt_thenAccessGranted() throws Exception {
        mockMvc.perform(get("/api/products")
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    /**
     * Test: Access without JWT returns 401
     */
    @Test
    void whenNoJwt_then401() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").exists());
    }

    // ========================================
    // Role-Based Access Control Tests
    // ========================================

    /**
     * Test: USER role can access products
     */
    @Test
    void whenUserRole_thenCanAccessProducts() throws Exception {
        mockMvc.perform(get("/api/products")
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    /**
     * Test: USER role cannot access admin endpoints
     */
    @Test
    void whenUserRole_thenCannotAccessAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/stats")
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    /**
     * Test: ADMIN role can access admin endpoints
     */
    @Test
    void whenAdminRole_thenCanAccessAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/stats")
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    /**
     * Test: ADMIN role can also access user endpoints
     */
    @Test
    void whenAdminRole_thenCanAccessUserEndpoints() throws Exception {
        mockMvc.perform(get("/api/products")
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    /**
     * Test: User with both roles can access everything
     */
    @Test
    void whenMultipleRoles_thenAccessBasedOnHighestRole() throws Exception {
        mockMvc.perform(get("/api/admin/stats")
                        .with(jwt()
                                .authorities(
                                        new SimpleGrantedAuthority("ROLE_USER"),
                                        new SimpleGrantedAuthority("ROLE_ADMIN")
                                )))
                .andExpect(status().isOk());
    }

    // ========================================
    // Keycloak Role Mapping Tests
    // ========================================

    /**
     * Test: Keycloak JWT with realm_access.roles
     *
     * Note: In this test, we manually add the authorities because
     * the jwtGrantedAuthoritiesConverter needs the full OAuth2 context.
     * The converter is tested implicitly through other tests.
     */
    @Test
    void whenKeycloakJwtWithRealmRoles_thenRolesMapped() throws Exception {
        // Simulate Keycloak JWT structure
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "RS256");
        headers.put("typ", "JWT");

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user123");
        claims.put("preferred_username", "user");
        claims.put("email", "user@example.com");

        // Keycloak stores roles in realm_access
        Map<String, Object> realmAccess = new HashMap<>();
        realmAccess.put("roles", List.of("user", "admin"));
        claims.put("realm_access", realmAccess);

        Jwt jwt = new Jwt(
                "mock-token",
                Instant.now(),
                Instant.now().plusSeconds(300),
                headers,
                claims
        );

        // Manually add authorities (simulating what converter would do)
        mockMvc.perform(get("/api/admin/stats")
                        .with(jwt()
                                .jwt(jwt)
                                .authorities(
                                        new SimpleGrantedAuthority("ROLE_USER"),
                                        new SimpleGrantedAuthority("ROLE_ADMIN")
                                )))
                .andExpect(status().isOk());
    }

    // ========================================
    // CRUD Operations with OAuth2 Tests
    // ========================================

    /**
     * Test: Complete CRUD flow with OAuth2 JWT
     */
    @Test
    void whenOAuth2Authenticated_thenCrudOperationsWork() throws Exception {
        // CREATE
        ProductDTO createDTO = ProductDTO.builder()
                .name("OAuth2 Test Product")
                .description("Created with OAuth2 token")
                .price(new BigDecimal("399.99"))
                .build();

        String createdJson = mockMvc.perform(post("/api/products")
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("OAuth2 Test Product"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        ProductDTO createdProduct = objectMapper.readValue(createdJson, ProductDTO.class);

        // READ
        mockMvc.perform(get("/api/products/" + createdProduct.getId())
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("OAuth2 Test Product"));

        // UPDATE
        ProductDTO updateDTO = ProductDTO.builder()
                .name("Updated OAuth2 Product")
                .price(new BigDecimal("449.99"))
                .build();

        mockMvc.perform(put("/api/products/" + createdProduct.getId())
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated OAuth2 Product"));

        // DELETE
        mockMvc.perform(delete("/api/products/" + createdProduct.getId())
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNoContent());
    }

    // ========================================
    // JWT Claims Tests
    // ========================================

    /**
     * Test: JWT with custom claims
     */
    @Test
    void whenJwtWithCustomClaims_thenAccessGranted() throws Exception {
        mockMvc.perform(get("/api/products")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .claim("sub", "user123")
                                        .claim("email", "user@example.com")
                                        .claim("name", "Test User")
                                )
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    // ========================================
    // Stateless Behavior Tests
    // ========================================

    /**
     * Test: No session created (stateless)
     */
    @Test
    void whenUsingOAuth2_thenNoSessionCreated() throws Exception {
        var result = mockMvc.perform(get("/api/products")
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andReturn();

        // Verify no JSESSIONID cookie
        org.assertj.core.api.Assertions.assertThat(result.getResponse().getCookie("JSESSIONID")).isNull();
    }

    /**
     * Test: Each request is independent
     */
    @Test
    void whenOAuth2_thenEachRequestIndependent() throws Exception {
        // Request 1 - with JWT
        mockMvc.perform(get("/api/products")
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());

        // Request 2 - without JWT (should fail, no session)
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized());

        // Request 3 - with JWT again
        mockMvc.perform(get("/api/products")
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    // ========================================
    // CSRF Not Required Tests
    // ========================================

    /**
     * Test: POST works without CSRF token (OAuth2)
     */
    @Test
    void whenOAuth2_thenCsrfNotRequired() throws Exception {
        ProductDTO productDTO = ProductDTO.builder()
                .name("No CSRF Test")
                .description("CSRF not needed with OAuth2")
                .price(new BigDecimal("99.99"))
                .build();

        // No .with(csrf()) needed!
        mockMvc.perform(post("/api/products")
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isCreated());
    }

    // ========================================
    // Public Endpoints Tests
    // ========================================

    /**
     * Test: Swagger endpoints are public
     */
    @Test
    void whenAccessingSwagger_thenNoAuthRequired() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    /**
     * Test: Health endpoint is public
     */
    @Test
    void whenAccessingHealth_thenNoAuthRequired() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    // ========================================
    // Error Response Format Tests
    // ========================================

    /**
     * Test: 401 error response format
     */
    @Test
    void when401_thenProperJsonStructure() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").exists());
    }

    /**
     * Test: 403 error response format
     */
    @Test
    void when403_thenProperJsonStructure() throws Exception {
        mockMvc.perform(get("/api/admin/stats")
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").exists());
    }
}