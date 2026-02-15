package com.vbforge.security.restbasic.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbforge.security.restbasic.dto.ProductDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Security integration tests for Module 3 - REST + Basic Auth (Stateless).
 *
 * Tests:
 * - Stateless authentication (no sessions)
 * - HTTP Basic Auth on every request
 * - Custom 401/403 JSON responses
 * - No CSRF protection (disabled)
 * - Role-based access control
 *
 * Key Differences from Module 2:
 * - Returns 401 JSON, not redirect to login
 * - No session cookies
 * - No CSRF tokens needed
 * - Every request must authenticate
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RestSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ========================================
    // Stateless Behavior Tests
    // ========================================

    /**
     * Test: No session is created (stateless)
     */
    @Test
    void whenAuthenticating_thenNoSessionCreated() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/products")
                        .with(httpBasic("user", "password")))
                .andExpect(status().isOk())
                .andReturn();

        // Verify no JSESSIONID cookie was created
        assertThat(result.getResponse().getCookie("JSESSIONID")).isNull();
    }

    /**
     * Test: Every request must authenticate (no session persistence)
     */
    @Test
    void whenSecondRequestWithoutAuth_thenUnauthorized() throws Exception {
        // First request with auth - succeeds
        mockMvc.perform(get("/api/products")
                        .with(httpBasic("user", "password")))
                .andExpect(status().isOk());

        // Second request WITHOUT auth - should fail (no session to reuse)
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized());
    }

    // ========================================
    // 401 Unauthorized Tests
    // ========================================

    /**
     * Test: No credentials returns 401 JSON response
     */
    @Test
    void whenNoCredentials_then401WithJson() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/products"));
    }

    /**
     * Test: Invalid credentials returns 401 JSON
     */
    @Test
    void whenInvalidCredentials_then401WithJson() throws Exception {
        mockMvc.perform(get("/api/products")
                        .with(httpBasic("user", "wrongpassword")))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    /**
     * Test: Valid credentials allow access
     */
    @Test
    void whenValidCredentials_thenSuccess() throws Exception {
        mockMvc.perform(get("/api/products")
                        .with(httpBasic("user", "password")))
                .andExpect(status().isOk());
    }

    // ========================================
    // 403 Forbidden Tests (Role-Based Access)
    // ========================================

    /**
     * Test: USER role trying to access ADMIN endpoint returns 403 JSON
     */
    @Test
    void whenUserRoleAccessingAdminEndpoint_then403WithJson() throws Exception {
        mockMvc.perform(get("/api/admin/stats")
                        .with(httpBasic("user", "password")))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/admin/stats"));
    }

    /**
     * Test: ADMIN role can access admin endpoints
     */
    @Test
    void whenAdminRoleAccessingAdminEndpoint_thenSuccess() throws Exception {
        mockMvc.perform(get("/api/admin/stats")
                        .with(httpBasic("admin", "admin")))
                .andExpect(status().isOk());
    }

    /**
     * Test: Both USER and ADMIN can access regular endpoints
     */
    @Test
    void whenUserOrAdminAccessingProducts_thenSuccess() throws Exception {
        // USER role
        mockMvc.perform(get("/api/products")
                        .with(httpBasic("user", "password")))
                .andExpect(status().isOk());

        // ADMIN role
        mockMvc.perform(get("/api/products")
                        .with(httpBasic("admin", "admin")))
                .andExpect(status().isOk());
    }

    // ========================================
    // CSRF Disabled Tests
    // ========================================

    /**
     * Test: POST works without CSRF token (CSRF disabled)
     */
    @Test
    void whenPostWithoutCsrf_thenSuccess() throws Exception {
        ProductDTO productDTO = ProductDTO.builder()
                .name("Test Product")
                .description("No CSRF needed")
                .price(new BigDecimal("99.99"))
                .build();

        mockMvc.perform(post("/api/products")
                        .with(httpBasic("user", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    /**
     * Test: PUT works without CSRF token
     */
    @Test
    @WithMockUser(roles = "USER")
    void whenPutWithoutCsrf_thenProcessed() throws Exception {
        ProductDTO productDTO = ProductDTO.builder()
                .name("Updated Product")
                .price(new BigDecimal("149.99"))
                .build();

        // Will return 404 since product doesn't exist, but CSRF didn't block it
        mockMvc.perform(put("/api/products/999")
                        .with(httpBasic("user", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isNotFound());
    }

    /**
     * Test: DELETE works without CSRF token
     */
    @Test
    void whenDeleteWithoutCsrf_thenProcessed() throws Exception {
        // Will return 404 since product doesn't exist, but CSRF didn't block it
        mockMvc.perform(delete("/api/products/999")
                        .with(httpBasic("user", "password")))
                .andExpect(status().isNotFound());
    }

    // ========================================
    // HTTP Basic Auth Tests
    // ========================================

    /**
     * Test: Authorization header format
     */
    @Test
    void whenAuthorizationHeader_thenAccepted() throws Exception {
        // HTTP Basic format: "Basic base64(username:password)"
        // user:password in base64 = dXNlcjpwYXNzd29yZA==

        mockMvc.perform(get("/api/products")
                        .header("Authorization", "Basic dXNlcjpwYXNzd29yZA=="))
                .andExpect(status().isOk());
    }

    /**
     * Test: Malformed Authorization header
     */
    @Test
    void whenMalformedAuthHeader_then401() throws Exception {
        mockMvc.perform(get("/api/products")
                        .header("Authorization", "Bearer something"))
                .andExpect(status().isUnauthorized());
    }

    // ========================================
    // Public Endpoints Tests
    // ========================================

    /**
     * Test: Swagger UI is public (no auth required)
     */
    @Test
    void whenAccessingSwagger_thenNoAuthRequired() throws Exception {
//        mockMvc.perform(get("/swagger-ui.html"))
//                .andExpect(status().is3xxRedirection()); // Redirects to /swagger-ui/index.html

        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));

    }

    /**
     * Test: API docs are public
     */
    @Test
    void whenAccessingApiDocs_thenNoAuthRequired() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    // ========================================
    // CRUD Operations with Auth Tests
    // ========================================

    /**
     * Test: Complete CRUD flow with authentication
     */
    @Test
    void whenCrudOperationsWithAuth_thenSuccess() throws Exception {
        // CREATE
        ProductDTO createDTO = ProductDTO.builder()
                .name("CRUD Test Product")
                .description("Testing CRUD")
                .price(new BigDecimal("199.99"))
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/products")
                        .with(httpBasic("user", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        ProductDTO createdProduct = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                ProductDTO.class
        );

        // READ
        mockMvc.perform(get("/api/products/" + createdProduct.getId())
                        .with(httpBasic("user", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("CRUD Test Product"));

        // UPDATE
        ProductDTO updateDTO = ProductDTO.builder()
                .name("Updated CRUD Product")
                .price(new BigDecimal("249.99"))
                .build();

        mockMvc.perform(put("/api/products/" + createdProduct.getId())
                        .with(httpBasic("user", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated CRUD Product"));

        // DELETE
        mockMvc.perform(delete("/api/products/" + createdProduct.getId())
                        .with(httpBasic("user", "password")))
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/api/products/" + createdProduct.getId())
                        .with(httpBasic("user", "password")))
                .andExpect(status().isNotFound());
    }

    // ========================================
    // Multiple Requests Test (Stateless Verification)
    // ========================================

    /**
     * Test: Multiple sequential requests all require authentication
     */
    @Test
    void whenMultipleRequests_thenEachMustAuthenticate() throws Exception {
        // Request 1 - with auth
        mockMvc.perform(get("/api/products")
                        .with(httpBasic("user", "password")))
                .andExpect(status().isOk());

        // Request 2 - without auth (should fail, no session)
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized());

        // Request 3 - with auth again
        mockMvc.perform(get("/api/products")
                        .with(httpBasic("user", "password")))
                .andExpect(status().isOk());

        // Request 4 - without auth (should fail again)
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized());
    }

    // ========================================
    // Error Response Format Tests
    // ========================================

    /**
     * Test: 401 error response contains all required fields
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
     * Test: 403 error response contains all required fields
     */
    @Test
    void when403_thenProperJsonStructure() throws Exception {
        mockMvc.perform(get("/api/admin/stats")
                        .with(httpBasic("user", "password")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").exists());
    }
}
