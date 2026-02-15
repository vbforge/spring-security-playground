package com.vbforge.security.oauth2.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * OAuth2 Resource Server Security Configuration for Module 5
 *
 * Key Features:
 * - OAuth2 Resource Server with JWT
 * - External identity provider (Keycloak)
 * - Automatic JWT validation (signature, expiration)
 * - Role mapping from Keycloak to Spring Security
 * - STATELESS session management
 * - No local authentication (delegated to Keycloak)
 *
 * Authentication Flow:
 * 1. User authenticates with Keycloak (external)
 * 2. Keycloak returns JWT token
 * 3. Client includes token in Authorization header
 * 4. Spring validates token with Keycloak's public key
 * 5. Extracts roles and authorities from token
 * 6. Sets authentication in SecurityContext
 *
 * Differences from Module 4 (Our JWT):
 * - Module 4: We generate and validate JWT
 * - Module 5: Keycloak generates, we only validate
 */
@Configuration
@EnableWebSecurity
public class OAuth2ResourceServerConfig {

    /**
     * Configure Security Filter Chain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF (not needed for stateless OAuth2)
                .csrf(csrf -> csrf.disable())

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/actuator/health"
                        ).permitAll()

                        // Admin-only endpoints
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // User and Admin can access
                        .requestMatchers("/api/products/**", "/api/tags/**").hasAnyRole("USER", "ADMIN")

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )

                // OAuth2 Resource Server configuration
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )

                // STATELESS session management
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }

    /**
     * JWT Authentication Converter
     *
     * Converts JWT token to Spring Security Authentication.
     * Maps Keycloak roles to Spring Security authorities.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter());
        return converter;
    }

    /**
     * JWT Granted Authorities Converter
     *
     * Extracts roles from Keycloak JWT token.
     * Keycloak stores roles in: realm_access.roles
     */
    @Bean
    public Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter() {
        JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();

        return jwt -> {
            // Get default authorities (from scope claim)
            Collection<GrantedAuthority> authorities = defaultConverter.convert(jwt);

            // Extract Keycloak realm roles
            Collection<GrantedAuthority> keycloakAuthorities = extractKeycloakRoles(jwt);

            // Combine both
            return Stream.concat(authorities.stream(), keycloakAuthorities.stream())
                    .toList();
        };
    }

    /**
     * Extract roles from Keycloak JWT
     *
     * Keycloak JWT structure:
     * {
     *   "realm_access": {
     *     "roles": ["user", "admin"]
     *   }
     * }
     *
     * Covariance explanation:
     * 1) .map(...) creates Stream<SimpleGrantedAuthority>
     * 2) .collect(Collectors.toList()) returns List<SimpleGrantedAuthority>
     * 3) BUT List<SimpleGrantedAuthority> IS-A Collection<GrantedAuthority>
     * 4) SimpleGrantedAuthority implements GrantedAuthority
     *
     * This is correct Java generics - called covariance in return types.
     */
    private Collection<GrantedAuthority> extractKeycloakRoles(Jwt jwt) {
        var realmAccess = jwt.getClaimAsMap("realm_access");

        if (realmAccess == null || !realmAccess.containsKey("roles")) {
            return java.util.Collections.emptyList();
        }

        @SuppressWarnings("unchecked")
        var roles = (Collection<String>) realmAccess.get("roles");

        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Custom Authentication Entry Point (401)
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
                        "message": "Valid OAuth2 token required. Please authenticate with Keycloak.",
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
     * Custom Access Denied Handler (403)
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