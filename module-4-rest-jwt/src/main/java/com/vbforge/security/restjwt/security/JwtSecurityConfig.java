package com.vbforge.security.restjwt.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * JWT Security Configuration for Module 4
 *
 * Key Features:
 * - JWT token-based authentication
 * - STATELESS session management
 * - Custom JWT authentication filter
 * - Login endpoint for obtaining JWT tokens
 * - CSRF disabled (not needed for stateless)
 * - Custom 401/403 JSON error responses
 *
 * Authentication Flow:
 * 1. User calls /auth/login with credentials
 * 2. Server validates and returns JWT token
 * 3. Client includes token in Authorization header (Bearer <token>)
 * 4. JwtAuthenticationFilter validates token on each request
 * 5. User is authenticated without database lookup (token contains info)
 *
 * How Spring Works Here:
 * Spring creates UserDetailsServiceConfig beans first:
 *
 * PasswordEncoder ✅
 * UserDetailsService ✅
 *
 * Spring creates authenticationProvider bean:
 *
 * Injects UserDetailsService from step 1
 * Injects PasswordEncoder from step 1
 *
 * Spring creates securityFilterChain bean:
 *
 * Injects HttpSecurity
 * Injects AuthenticationProvider from step 2
 *
 * No circular dependency! Clean and Spring-like! ✨
 *
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class JwtSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Configure Security Filter Chain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationProvider authenticationProvider) throws Exception {
        http
                // Disable CSRF (not needed for stateless JWT)
                .csrf(csrf -> csrf.disable())

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints (no authentication required)
                        .requestMatchers(
                                "/auth/**",              // Login/register endpoints
                                "/swagger-ui/**",        // Swagger UI
                                "/v3/api-docs/**",       // API documentation
                                "/swagger-ui.html"
                        ).permitAll()

                        // Admin-only endpoints
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // User and Admin can access these
                        .requestMatchers("/api/products/**", "/api/tags/**").hasAnyRole("USER", "ADMIN")

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )

                // STATELESS session management (no sessions!)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Custom exception handlers
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )

                // Set authentication provider (injected as parameter)
                .authenticationProvider(authenticationProvider)

                // Add JWT filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Authentication Provider
     *
     * Tells Spring Security how to load users and verify passwords.
     */
    @Bean
    public AuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,                                      // Spring auto-injects
            PasswordEncoder passwordEncoder) {                                          // Spring auto-injects
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    /**
     * Authentication Manager
     *
     * Used by login endpoint to authenticate users.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Custom Authentication Entry Point
     *
     * Returns 401 JSON response when authentication is required.
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            String jsonResponse = String.format(
                    """
                    {
                        "timestamp": "%s",
                        "status": 401,
                        "error": "Unauthorized",
                        "message": "Authentication required. Please provide a valid JWT token.",
                        "path": "%s"
                    }
                    """,
                    java.time.LocalDateTime.now().toString(),
                    request.getRequestURI()
            );

            response.getWriter().write(jsonResponse);
        };
    }

    /**
     * Custom Access Denied Handler
     *
     * Returns 403 JSON response when user lacks required role.
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            String jsonResponse = String.format(
                    """
                    {
                        "timestamp": "%s",
                        "status": 403,
                        "error": "Forbidden",
                        "message": "Access denied. You don't have sufficient permissions.",
                        "path": "%s"
                    }
                    """,
                    java.time.LocalDateTime.now().toString(),
                    request.getRequestURI()
            );

            response.getWriter().write(jsonResponse);
        };
    }
}