package com.vbforge.security.websec.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Web Security Configuration for Module 2
 *
 * Key Features:
 * - Form-based login (custom login page)
 * - Session-based authentication (stateful)
 * - In-memory user storage
 * - Role-based access control (USER and ADMIN)
 * - CSRF protection enabled
 * - Remember-me functionality
 * - Logout handling
 *
 * This demonstrates traditional web application security.
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    /**
     * Configure Security Filter Chain
     *
     * This replaces the default auto-configuration from Module 1.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public pages (no authentication required)
                        .requestMatchers("/", "/home", "/login", "/error", "/access-denied").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()

                        // Admin-only pages
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/products/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/tags/**").hasAnyRole("USER", "ADMIN")

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )

                // Form login configuration
                .formLogin(form -> form
                        .loginPage("/login")              // Custom login page URL
                        .loginProcessingUrl("/perform_login")  // Where form submits
                        .defaultSuccessUrl("/products", true)   // Redirect after login
                        .failureUrl("/login?error=true")  // Redirect on failure
                        .permitAll()                      // Allow everyone to see login page
                )

                // Logout configuration
                .logout(logout -> logout
                        .logoutUrl("/logout")             // Logout endpoint
                        .logoutSuccessUrl("/login?logout=true")  // Redirect after logout
                        .invalidateHttpSession(true)      // Invalidate session
                        .deleteCookies("JSESSIONID")      // Delete session cookie
                        .permitAll()
                )

                // Remember-me functionality
                .rememberMe(remember -> remember
                        .key("uniqueAndSecretKey")        // Secret key for remember-me token
                        .tokenValiditySeconds(86400)      // 24 hours
                        .rememberMeParameter("remember-me")  // Form parameter name
                )

                // Exception handling
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/access-denied")  // 403 error page
                )

                // Session management
                .sessionManagement(session -> session
                        .maximumSessions(1)               // Only 1 session per user
                        .maxSessionsPreventsLogin(false)  // New login invalidates old session
                );

        return http.build();
    }

    /**
     * Password Encoder Bean
     *
     * BCrypt is the recommended encoder for production.
     * It automatically handles salting and is computationally expensive
     * to prevent brute-force attacks.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * In-Memory User Details Service
     *
     * Stores users in memory (not production-ready, but great for learning).
     *
     * Users:
     * 1. user/password - Role: USER
     * 2. admin/admin   - Role: ADMIN
     *
     * Passwords are BCrypt encoded.
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
                .roles("ADMIN")  // Has ADMIN role (also gets USER role automatically)
                .build();

        return new InMemoryUserDetailsManager(user, admin);
    }


}
























