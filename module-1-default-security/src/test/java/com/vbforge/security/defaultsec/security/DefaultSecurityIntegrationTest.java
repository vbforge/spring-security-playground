package com.vbforge.security.defaultsec.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbforge.security.defaultsec.config.TestSecurityConfig;
import com.vbforge.security.defaultsec.dto.ProductDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Security integration tests for Module 1 - Default Security.
 *
 * Tests Spring Security's default auto-configuration:
 * - All endpoints require authentication
 * - HTTP Basic authentication is enabled
 * - Default user with role USER is created
 * - 401 Unauthorized for unauthenticated requests
 * - 403 Forbidden is not used in default config (only 401)
 *
 * Note: CSRF is disabled in tests via TestSecurityConfig
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)  // Import test security config
class DefaultSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Test: Accessing protected endpoint without authentication returns 401
     */
    @Test
    void whenNoAuthentication_thenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test: HTTP Basic authentication with valid credentials allows access
     */
    @Test
    void whenValidBasicAuth_thenSuccess() throws Exception {
        mockMvc.perform(get("/api/products")
                        .with(httpBasic("user", "password")))
                .andExpect(status().isOk());
    }

    /**
     * Test: HTTP Basic authentication with invalid credentials returns 401
     */
    @Test
    void whenInvalidBasicAuth_thenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/products")
                        .with(httpBasic("user", "wrongpassword")))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test: Using @WithMockUser for authenticated tests
     */
    @Test
    @WithMockUser
    void whenMockUser_thenCanAccessEndpoint() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
    }

    /**
     * Test: POST request without authentication returns 401
     */
    @Test
    void whenPostWithoutAuth_thenUnauthorized() throws Exception {
        ProductDTO productDTO = ProductDTO.builder()
                .name("Test Product")
                .description("Test description")
                .price(new BigDecimal("99.99"))
                .build();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test: POST request with authentication succeeds
     * Note: CSRF is disabled via TestSecurityConfig for easier testing
     */
    @Test
    @WithMockUser
    void whenPostWithAuth_thenCreated() throws Exception {
        ProductDTO productDTO = ProductDTO.builder()
                .name("Authenticated Product")
                .description("Created with auth")
                .price(new BigDecimal("149.99"))
                .build();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Authenticated Product"));
    }

    /**
     * Test: GET by ID without authentication returns 401
     */
    @Test
    void whenGetByIdWithoutAuth_thenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test: DELETE without authentication returns 401
     */
    @Test
    void whenDeleteWithoutAuth_thenUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test: All tag endpoints also require authentication
     */
    @Test
    void whenAccessTagsWithoutAuth_thenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void whenAccessTagsWithAuth_thenSuccess() throws Exception {
        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk());
    }
}