package com.vbforge.security.restbasic.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * REST API Security Configuration for Module 3
 *
 * Key Features (Different from Module 2):
 * - HTTP Basic Authentication (like Module 1, but configured)
 * - STATELESS session management (NO sessions!)
 * - CSRF disabled (not needed for stateless REST APIs)
 * - Custom 401/403 JSON error responses
 * - Role-based access control (USER and ADMIN)
 * - Every request must authenticate (no session cookies)
 *
 * This is the industry-standard approach for REST APIs.
 */
@Configuration
@EnableWebSecurity
public class RestSecurityConfig {

    /**
     * Configure Security Filter Chain
     *
     * Main differences from Module 2:
     * - sessionManagement = STATELESS
     * - CSRF disabled
     * - httpBasic instead of formLogin
     * - Custom AuthenticationEntryPoint for 401
     * - Custom AccessDeniedHandler for 403
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CRITICAL: Disable CSRF for stateless REST APIs
                .csrf(csrf -> csrf.disable())

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Swagger UI endpoints (public for API documentation)
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // Admin-only endpoints
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // User and Admin can access these
                        .requestMatchers("/api/products/**", "/api/tags/**").hasAnyRole("USER", "ADMIN")

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )

                // HTTP Basic Authentication
                .httpBasic(httpBasic -> httpBasic
                        .authenticationEntryPoint(authenticationEntryPoint())
                )

                // CRITICAL: STATELESS session management
                // This means NO sessions, NO cookies, authenticate every request
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Custom 403 handler (wrong role)
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(accessDeniedHandler())
                );

        return http.build();
    }

    /**
     * Custom Authentication Entry Point
     *
     * Returns proper 401 JSON response instead of browser dialog.
     * Called when user is NOT authenticated.
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
                        "message": "Authentication required. Please provide valid credentials.",
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
     * Returns proper 403 JSON response.
     * Called when user IS authenticated but lacks required role.
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
                        "message": "Access denied. You don't have sufficient permissions to access this resource.",
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
     * Password Encoder Bean
     *
     * BCrypt for secure password hashing.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * In-Memory User Details Service
     *
     * Same users as Module 2:
     * - user/password (USER role)
     * - admin/admin (ADMIN role)
     *
     * In production, this would be database-backed.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        // Regular user with USER role
        UserDetails user = User.builder()
                .username("user")
                .password(passwordEncoder().encode("password"))
                .roles("USER")
                .build();

        // Admin user with ADMIN role
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder().encode("admin"))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(user, admin);
    }
}