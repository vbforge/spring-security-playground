package com.vbforge.security.restjwt.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbforge.security.restjwt.dto.AuthResponse;
import com.vbforge.security.restjwt.dto.LoginRequest;
import com.vbforge.security.restjwt.dto.ProductDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Security integration tests for Module 4 - REST + JWT.
 * 
 * Tests:
 * - JWT token generation via login
 * - JWT token validation
 * - Token-based authentication
 * - Stateless behavior (no sessions)
 * - Role-based access control
 * - Token expiration handling
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JwtSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ========================================
    // Login & Token Generation Tests
    // ========================================

    /**
     * Test: Successful login returns JWT token
     */
    @Test
    void whenValidCredentials_thenReturnJwtToken() throws Exception {
        LoginRequest loginRequest = new LoginRequest("user", "password");

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.username").value("user"))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.expiresIn").exists())
                .andReturn();

        // Verify token is not empty
        AuthResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AuthResponse.class
        );
        assertThat(response.getToken()).isNotEmpty();
        assertThat(response.getToken().split("\\.")).hasSize(3); // JWT has 3 parts
    }

    /**
     * Test: Failed login with invalid credentials
     */
    @Test
    void whenInvalidCredentials_thenReturn401() throws Exception {
        LoginRequest loginRequest = new LoginRequest("user", "wrongpassword");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    /**
     * Test: Admin user login returns correct roles
     */
    @Test
    void whenAdminLogin_thenReturnAdminRole() throws Exception {
        LoginRequest loginRequest = new LoginRequest("admin", "admin");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"));
    }

    // ========================================
    // JWT Token Usage Tests
    // ========================================

    /**
     * Test: Use JWT token to access protected endpoint
     */
    @Test
    void whenValidToken_thenAccessProtectedEndpoint() throws Exception {
        // 1. Login and get token
        String token = loginAndGetToken("user", "password");

        // 2. Use token to access protected endpoint
        mockMvc.perform(get("/api/products")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    /**
     * Test: Request without token returns 401
     */
    @Test
    void whenNoToken_then401() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    /**
     * Test: Invalid token returns 401
     */
    @Test
    void whenInvalidToken_then401() throws Exception {
        mockMvc.perform(get("/api/products")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test: Token without "Bearer " prefix
     */
    @Test
    void whenTokenWithoutBearerPrefix_then401() throws Exception {
        String token = loginAndGetToken("user", "password");

        mockMvc.perform(get("/api/products")
                        .header("Authorization", token)) // Missing "Bearer "
                .andExpect(status().isUnauthorized());
    }

    // ========================================
    // Stateless Behavior Tests
    // ========================================

    /**
     * Test: No session is created (stateless)
     */
    @Test
    void whenUsingJwt_thenNoSessionCreated() throws Exception {
        String token = loginAndGetToken("user", "password");

        MvcResult result = mockMvc.perform(get("/api/products")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        // Verify no JSESSIONID cookie
        assertThat(result.getResponse().getCookie("JSESSIONID")).isNull();
    }

    /**
     * Test: Token works for multiple requests
     */
    @Test
    void whenTokenGenerated_thenWorksForMultipleRequests() throws Exception {
        String token = loginAndGetToken("user", "password");

        // Request 1
        mockMvc.perform(get("/api/products")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Request 2 - same token still works
        mockMvc.perform(get("/api/tags")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Request 3
        mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user"));
    }

    // ========================================
    // Role-Based Access Control Tests
    // ========================================

    /**
     * Test: USER role can access products
     */
    @Test
    void whenUserRole_thenCanAccessProducts() throws Exception {
        String token = loginAndGetToken("user", "password");

        mockMvc.perform(get("/api/products")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    /**
     * Test: USER role cannot access admin endpoints (403)
     */
    @Test
    void whenUserRole_thenCannotAccessAdmin() throws Exception {
        String token = loginAndGetToken("user", "password");

        mockMvc.perform(get("/api/admin/stats")
                        .header("Authorization", "Bearer " + token))
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
        String token = loginAndGetToken("admin", "admin");

        mockMvc.perform(get("/api/admin/stats")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    /**
     * Test: ADMIN role can also access user endpoints
     */
    @Test
    void whenAdminRole_thenCanAccessUserEndpoints() throws Exception {
        String token = loginAndGetToken("admin", "admin");

        mockMvc.perform(get("/api/products")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    // ========================================
    // CRUD Operations with JWT Tests
    // ========================================

    /**
     * Test: Complete CRUD flow with JWT authentication
     */
    @Test
    void whenJwtAuthenticated_thenCrudOperationsWork() throws Exception {
        String token = loginAndGetToken("user", "password");

        // CREATE
        ProductDTO createDTO = ProductDTO.builder()
                .name("JWT Test Product")
                .description("Created with JWT token")
                .price(new BigDecimal("299.99"))
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("JWT Test Product"))
                .andReturn();

        ProductDTO createdProduct = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                ProductDTO.class
        );

        // READ
        mockMvc.perform(get("/api/products/" + createdProduct.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("JWT Test Product"));

        // UPDATE
        ProductDTO updateDTO = ProductDTO.builder()
                .name("Updated JWT Product")
                .price(new BigDecimal("349.99"))
                .build();

        mockMvc.perform(put("/api/products/" + createdProduct.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated JWT Product"));

        // DELETE
        mockMvc.perform(delete("/api/products/" + createdProduct.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    // ========================================
    // Current User Info Tests
    // ========================================

    /**
     * Test: /auth/me returns current user info
     */
    @Test
    void whenAuthenticated_thenGetCurrentUserInfo() throws Exception {
        String token = loginAndGetToken("user", "password");

        mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user"))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
    }

    /**
     * Test: /auth/me without token returns 401
     */
    @Test
    void whenNotAuthenticated_thenCannotGetCurrentUserInfo() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    // ========================================
    // CSRF Not Required Tests
    // ========================================

    /**
     * Test: POST works without CSRF token (JWT authentication)
     */
    @Test
    void whenJwtAuth_thenCsrfNotRequired() throws Exception {
        String token = loginAndGetToken("user", "password");

        ProductDTO productDTO = ProductDTO.builder()
                .name("No CSRF Test")
                .description("CSRF not needed with JWT")
                .price(new BigDecimal("99.99"))
                .build();

        // No .with(csrf()) needed!
        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isCreated());
    }

    // ========================================
    // Public Endpoints Tests
    // ========================================

    /**
     * Test: Login endpoint is public
     */
    @Test
    void whenAccessingLogin_thenNoAuthRequired() throws Exception {
        LoginRequest loginRequest = new LoginRequest("user", "password");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

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

    // ========================================
    // Helper Methods
    // ========================================

    /**
     * Helper method to login and extract JWT token
     */
    private String loginAndGetToken(String username, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(username, password);

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AuthResponse.class
        );

        return response.getToken();
    }
}