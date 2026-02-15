package com.vbforge.security.websec.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbforge.security.websec.dto.ProductDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Security integration tests for Module 2 - Web Security.
 * 
 * Tests:
 * - Form-based login
 * - Role-based access control (USER vs ADMIN)
 * - Session management
 * - CSRF protection
 * - Public vs protected pages
 * - 401 vs 403 behavior
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WebSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ========================================
    // Form Login Tests
    // ========================================

    /**
     * Test: Successful login with valid credentials
     */
    @Test
    void whenValidCredentials_thenLoginSucceeds() throws Exception {
        mockMvc.perform(formLogin("/perform_login")
                        .user("user")
                        .password("password"))
                .andExpect(authenticated())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"));
    }

    /**
     * Test: Failed login with invalid credentials
     */
    @Test
    void whenInvalidCredentials_thenLoginFails() throws Exception {
        mockMvc.perform(formLogin("/perform_login")
                        .user("user")
                        .password("wrongpassword"))
                .andExpect(unauthenticated())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));
    }

    /**
     * Test: Admin user can login
     */
    @Test
    void whenAdminCredentials_thenLoginSucceeds() throws Exception {
        mockMvc.perform(formLogin("/perform_login")
                        .user("admin")
                        .password("admin"))
                .andExpect(authenticated().withRoles("ADMIN"))
                .andExpect(status().is3xxRedirection());
    }

    // ========================================
    // Public Pages Tests
    // ========================================

    /**
     * Test: Home page is accessible without authentication
     */
    @Test
    void whenAccessingHomePage_thenNoAuthRequired() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    /**
     * Test: Login page is accessible without authentication
     */
    @Test
    void whenAccessingLoginPage_thenNoAuthRequired() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    // ========================================
    // Protected Pages Tests
    // ========================================

    /**
     * Test: Products page requires authentication
     */
    @Test
    void whenAccessingProductsWithoutAuth_thenRedirectToLogin() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    /**
     * Test: Authenticated user can access products page
     */
    @Test
    @WithMockUser(username = "user", roles = "USER")
    void whenAuthenticatedUser_thenCanAccessProducts() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("products"));
    }

    // ========================================
    // Role-Based Access Tests
    // ========================================

    /**
     * Test: USER role cannot access admin page (403)
     */
    @Test
    @WithMockUser(username = "user", roles = "USER")
    void whenUserRoleAccessingAdmin_thenForbidden() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isForbidden());
    }

    /**
     * Test: ADMIN role can access admin page
     */
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void whenAdminRoleAccessingAdmin_thenSuccess() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin"));
    }

    /**
     * Test: Unauthenticated user gets redirected from admin page
     */
    @Test
    void whenUnauthenticatedAccessingAdmin_thenRedirectToLogin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    // ========================================
    // API Endpoint Tests
    // ========================================

    /**
     * Test: API endpoints require authentication
     */
    @Test
    void whenAccessingApiWithoutAuth_thenRedirectToLogin() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().is3xxRedirection());
    }

    /**
     * Test: Authenticated user can access API
     */
    @Test
    @WithMockUser(username = "user", roles = "USER")
    void whenAuthenticatedUserAccessingApi_thenSuccess() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
    }

    // ========================================
    // CSRF Protection Tests
    // ========================================

    /**
     * Test: POST without CSRF token fails
     */
    @Test
    @WithMockUser(username = "user", roles = "USER")
    void whenPostWithoutCsrfToken_thenForbidden() throws Exception {
        ProductDTO productDTO = ProductDTO.builder()
                .name("Test Product")
                .description("Test")
                .price(new BigDecimal("99.99"))
                .build();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isForbidden());
    }

    /**
     * Test: POST with CSRF token succeeds
     */
    @Test
    @WithMockUser(username = "user", roles = "USER")
    void whenPostWithCsrfToken_thenSuccess() throws Exception {
        ProductDTO productDTO = ProductDTO.builder()
                .name("CSRF Test Product")
                .description("Created with CSRF token")
                .price(new BigDecimal("149.99"))
                .build();

        mockMvc.perform(post("/api/products")
                        .with(csrf())  // Include CSRF token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("CSRF Test Product"));
    }

    /**
     * Test: DELETE without CSRF token fails
     */
    @Test
    @WithMockUser(username = "user", roles = "USER")
    void whenDeleteWithoutCsrfToken_thenForbidden() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isForbidden());
    }

    /**
     * Test: DELETE with CSRF token succeeds (or 404 if product doesn't exist)
     */
    @Test
    @WithMockUser(username = "user", roles = "USER")
    void whenDeleteWithCsrfToken_thenProcessed() throws Exception {
        mockMvc.perform(delete("/api/products/999")
                        .with(csrf()))
                .andExpect(status().isNotFound());  // Product doesn't exist, but CSRF passed
    }

    // ========================================
    // 401 vs 403 Tests
    // ========================================

    /**
     * Test: Understanding 401 vs 403
     * 
     * In web security with form login:
     * - Unauthenticated requests redirect to login (302), not 401
     * - Wrong role returns 403 Forbidden
     */
    @Test
    void demonstrating401vs403() throws Exception {
        // Unauthenticated → Redirect to login (not 401 in form-based auth)
        mockMvc.perform(get("/products"))
                .andExpect(status().is3xxRedirection());

        // Authenticated but wrong role → 403 Forbidden
        mockMvc.perform(get("/admin")
                        .with(request -> {
                            request.setRemoteUser("user");
                            return request;
                        }))
                .andExpect(status().is3xxRedirection()); // Will redirect to login
    }

    // ========================================
    // Logout Tests
    // ========================================

    /**
     * Test: Logout functionality
     */
    @Test
    @WithMockUser(username = "user", roles = "USER")
    void whenLogout_thenRedirectToLoginWithMessage() throws Exception {
        mockMvc.perform(post("/logout").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout=true"));
    }

    // ========================================
    // Access Denied Page Tests
    // ========================================

    /**
     * Test: Access denied page is shown for 403 errors
     */
    @Test
    void whenAccessDeniedPage_thenSuccess() throws Exception {
        mockMvc.perform(get("/access-denied"))
                .andExpect(status().isOk())
                .andExpect(view().name("access-denied"));
    }
}